package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class ChunkGenerator implements BiomeManager.NoiseBiomeSource {
   public static final Codec<ChunkGenerator> CODEC = Registry.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
   protected final BiomeSource biomeSource;
   protected final BiomeSource runtimeBiomeSource;
   private final StructureSettings settings;
   private final long strongholdSeed;
   private final List<ChunkPos> strongholdPositions = Lists.newArrayList();

   public ChunkGenerator(BiomeSource p_62149_, StructureSettings p_62150_) {
      this(p_62149_, p_62149_, p_62150_, 0L);
   }

   public ChunkGenerator(BiomeSource p_62144_, BiomeSource p_62145_, StructureSettings p_62146_, long p_62147_) {
      this.biomeSource = p_62144_;
      this.runtimeBiomeSource = p_62145_;
      this.settings = p_62146_;
      this.strongholdSeed = p_62147_;
   }

   private void generateStrongholds() {
      if (this.strongholdPositions.isEmpty()) {
         StrongholdConfiguration strongholdconfiguration = this.settings.stronghold();
         if (strongholdconfiguration != null && strongholdconfiguration.count() != 0) {
            List<Biome> list = Lists.newArrayList();

            for(Biome biome : this.biomeSource.possibleBiomes()) {
               if (validStrongholdBiome(biome)) {
                  list.add(biome);
               }
            }

            int k1 = strongholdconfiguration.distance();
            int l1 = strongholdconfiguration.count();
            int i = strongholdconfiguration.spread();
            Random random = new Random();
            random.setSeed(this.strongholdSeed);
            double d0 = random.nextDouble() * Math.PI * 2.0D;
            int j = 0;
            int k = 0;

            for(int l = 0; l < l1; ++l) {
               double d1 = (double)(4 * k1 + k1 * k * 6) + (random.nextDouble() - 0.5D) * (double)k1 * 2.5D;
               int i1 = (int)Math.round(Math.cos(d0) * d1);
               int j1 = (int)Math.round(Math.sin(d0) * d1);
               BlockPos blockpos = this.biomeSource.findBiomeHorizontal(SectionPos.sectionToBlockCoord(i1, 8), 0, SectionPos.sectionToBlockCoord(j1, 8), 112, list::contains, random, this.climateSampler());
               if (blockpos != null) {
                  i1 = SectionPos.blockToSectionCoord(blockpos.getX());
                  j1 = SectionPos.blockToSectionCoord(blockpos.getZ());
               }

               this.strongholdPositions.add(new ChunkPos(i1, j1));
               d0 += (Math.PI * 2D) / (double)i;
               ++j;
               if (j == i) {
                  ++k;
                  j = 0;
                  i += 2 * i / (k + 1);
                  i = Math.min(i, l1 - l);
                  d0 += random.nextDouble() * Math.PI * 2.0D;
               }
            }

         }
      }
   }

   private static boolean validStrongholdBiome(Biome p_187716_) {
      Biome.BiomeCategory biome$biomecategory = p_187716_.getBiomeCategory();
      return biome$biomecategory != Biome.BiomeCategory.OCEAN && biome$biomecategory != Biome.BiomeCategory.RIVER && biome$biomecategory != Biome.BiomeCategory.BEACH && biome$biomecategory != Biome.BiomeCategory.SWAMP && biome$biomecategory != Biome.BiomeCategory.NETHER && biome$biomecategory != Biome.BiomeCategory.THEEND;
   }

   protected abstract Codec<? extends ChunkGenerator> codec();

   public Optional<ResourceKey<Codec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
      return Registry.CHUNK_GENERATOR.getResourceKey(this.codec());
   }

   public abstract ChunkGenerator withSeed(long p_62156_);

   public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> p_196743_, Executor p_196744_, Blender p_196745_, StructureFeatureManager p_196746_, ChunkAccess p_196747_) {
      return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
         p_196747_.fillBiomesFromNoise(this.runtimeBiomeSource::getNoiseBiome, this.climateSampler());
         return p_196747_;
      }), Util.backgroundExecutor());
   }

   public abstract Climate.Sampler climateSampler();

   public Biome getNoiseBiome(int p_187755_, int p_187756_, int p_187757_) {
      return this.getBiomeSource().getNoiseBiome(p_187755_, p_187756_, p_187757_, this.climateSampler());
   }

   public abstract void applyCarvers(WorldGenRegion p_187691_, long p_187692_, BiomeManager p_187693_, StructureFeatureManager p_187694_, ChunkAccess p_187695_, GenerationStep.Carving p_187696_);

   @Nullable
   public BlockPos findNearestMapFeature(ServerLevel p_62162_, StructureFeature<?> p_62163_, BlockPos p_62164_, int p_62165_, boolean p_62166_) {
      if (p_62163_ == StructureFeature.STRONGHOLD) {
         this.generateStrongholds();
         BlockPos blockpos = null;
         double d1 = Double.MAX_VALUE;
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(ChunkPos chunkpos : this.strongholdPositions) {
            blockpos$mutableblockpos.set(SectionPos.sectionToBlockCoord(chunkpos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkpos.z, 8));
            double d0 = blockpos$mutableblockpos.distSqr(p_62164_);
            if (blockpos == null) {
               blockpos = new BlockPos(blockpos$mutableblockpos);
               d1 = d0;
            } else if (d0 < d1) {
               blockpos = new BlockPos(blockpos$mutableblockpos);
               d1 = d0;
            }
         }

         return blockpos;
      } else {
         StructureFeatureConfiguration structurefeatureconfiguration = this.settings.getConfig(p_62163_);
         ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> immutablemultimap = this.settings.structures(p_62163_);
         if (structurefeatureconfiguration != null && !immutablemultimap.isEmpty()) {
            Registry<Biome> registry = p_62162_.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
            Set<ResourceKey<Biome>> set = this.runtimeBiomeSource.possibleBiomes().stream().flatMap((p_187725_) -> {
               return registry.getResourceKey(p_187725_).stream();
            }).collect(Collectors.toSet());
            return immutablemultimap.values().stream().noneMatch(set::contains) ? null : p_62163_.getNearestGeneratedFeature(p_62162_, p_62162_.structureFeatureManager(), p_62164_, p_62165_, p_62166_, p_62162_.getSeed(), structurefeatureconfiguration);
         } else {
            return null;
         }
      }
   }

   public void applyBiomeDecoration(WorldGenLevel p_187712_, ChunkAccess p_187713_, StructureFeatureManager p_187714_) {
      ChunkPos chunkpos = p_187713_.getPos();
      if (!SharedConstants.debugVoidTerrain(chunkpos)) {
         SectionPos sectionpos = SectionPos.of(chunkpos, p_187712_.getMinSection());
         BlockPos blockpos = sectionpos.origin();
         Map<Integer, List<StructureFeature<?>>> map = Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy((p_187720_) -> {
            return p_187720_.step().ordinal();
         }));
         List<BiomeSource.StepFeatureData> list = this.biomeSource.featuresPerStep();
         WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
         long i = worldgenrandom.setDecorationSeed(p_187712_.getSeed(), blockpos.getX(), blockpos.getZ());
         Set<Biome> set = new ObjectArraySet<>();
         if (this instanceof FlatLevelSource) {
            set.addAll(this.biomeSource.possibleBiomes());
         } else {
            ChunkPos.rangeClosed(sectionpos.chunk(), 1).forEach((p_196730_) -> {
               ChunkAccess chunkaccess = p_187712_.getChunk(p_196730_.x, p_196730_.z);

               for(LevelChunkSection levelchunksection : chunkaccess.getSections()) {
                  levelchunksection.getBiomes().getAll(set::add);
               }

            });
            set.retainAll(this.biomeSource.possibleBiomes());
         }

         int j = list.size();

         try {
            Registry<PlacedFeature> registry = p_187712_.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            Registry<StructureFeature<?>> registry1 = p_187712_.registryAccess().registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY);
            int k = Math.max(GenerationStep.Decoration.values().length, j);

            for(int l = 0; l < k; ++l) {
               int i1 = 0;
               if (p_187714_.shouldGenerateFeatures()) {
                  for(StructureFeature<?> structurefeature : map.getOrDefault(l, Collections.emptyList())) {
                     worldgenrandom.setFeatureSeed(i, i1, l);
                     Supplier<String> supplier = () -> {
                        return registry1.getResourceKey(structurefeature).map(Object::toString).orElseGet(structurefeature::toString);
                     };

                     try {
                        p_187712_.setCurrentlyGenerating(supplier);
                        p_187714_.startsForFeature(sectionpos, structurefeature).forEach((p_196726_) -> {
                           p_196726_.placeInChunk(p_187712_, p_187714_, this, worldgenrandom, getWritableArea(p_187713_), chunkpos);
                        });
                     } catch (Exception exception) {
                        CrashReport crashreport1 = CrashReport.forThrowable(exception, "Feature placement");
                        crashreport1.addCategory("Feature").setDetail("Description", supplier::get);
                        throw new ReportedException(crashreport1);
                     }

                     ++i1;
                  }
               }

               if (l < j) {
                  IntSet intset = new IntArraySet();

                  for(Biome biome : set) {
                     List<List<Supplier<PlacedFeature>>> list2 = biome.getGenerationSettings().features();
                     if (l < list2.size()) {
                        List<Supplier<PlacedFeature>> list1 = list2.get(l);
                        BiomeSource.StepFeatureData biomesource$stepfeaturedata1 = list.get(l);
                        list1.stream().map(Supplier::get).forEach((p_196751_) -> {
                           intset.add(biomesource$stepfeaturedata1.indexMapping().applyAsInt(p_196751_));
                        });
                     }
                  }

                  int j1 = intset.size();
                  int[] aint = intset.toIntArray();
                  Arrays.sort(aint);
                  BiomeSource.StepFeatureData biomesource$stepfeaturedata = list.get(l);

                  for(int k1 = 0; k1 < j1; ++k1) {
                     int l1 = aint[k1];
                     PlacedFeature placedfeature = biomesource$stepfeaturedata.features().get(l1);
                     Supplier<String> supplier1 = () -> {
                        return registry.getResourceKey(placedfeature).map(Object::toString).orElseGet(placedfeature::toString);
                     };
                     worldgenrandom.setFeatureSeed(i, l1, l);

                     try {
                        p_187712_.setCurrentlyGenerating(supplier1);
                        placedfeature.placeWithBiomeCheck(p_187712_, this, worldgenrandom, blockpos);
                     } catch (Exception exception1) {
                        CrashReport crashreport2 = CrashReport.forThrowable(exception1, "Feature placement");
                        crashreport2.addCategory("Feature").setDetail("Description", supplier1::get);
                        throw new ReportedException(crashreport2);
                     }
                  }
               }
            }

            p_187712_.setCurrentlyGenerating((Supplier<String>)null);
         } catch (Exception exception2) {
            CrashReport crashreport = CrashReport.forThrowable(exception2, "Biome decoration");
            crashreport.addCategory("Generation").setDetail("CenterX", chunkpos.x).setDetail("CenterZ", chunkpos.z).setDetail("Seed", i);
            throw new ReportedException(crashreport);
         }
      }
   }

   private static BoundingBox getWritableArea(ChunkAccess p_187718_) {
      ChunkPos chunkpos = p_187718_.getPos();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      LevelHeightAccessor levelheightaccessor = p_187718_.getHeightAccessorForGeneration();
      int k = levelheightaccessor.getMinBuildHeight() + 1;
      int l = levelheightaccessor.getMaxBuildHeight() - 1;
      return new BoundingBox(i, k, j, i + 15, l, j + 15);
   }

   public abstract void buildSurface(WorldGenRegion p_187697_, StructureFeatureManager p_187698_, ChunkAccess p_187699_);

   public abstract void spawnOriginalMobs(WorldGenRegion p_62167_);

   public StructureSettings getSettings() {
      return this.settings;
   }

   public int getSpawnHeight(LevelHeightAccessor p_156157_) {
      return 64;
   }

   public BiomeSource getBiomeSource() {
      return this.runtimeBiomeSource;
   }

   public abstract int getGenDepth();

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome p_156158_, StructureFeatureManager p_156159_, MobCategory p_156160_, BlockPos p_156161_) {
      return p_156158_.getMobSettings().getMobs(p_156160_);
   }

   public void createStructures(RegistryAccess p_62200_, StructureFeatureManager p_62201_, ChunkAccess p_62202_, StructureManager p_62203_, long p_62204_) {
      ChunkPos chunkpos = p_62202_.getPos();
      SectionPos sectionpos = SectionPos.bottomOf(p_62202_);
      StructureFeatureConfiguration structurefeatureconfiguration = this.settings.getConfig(StructureFeature.STRONGHOLD);
      if (structurefeatureconfiguration != null) {
         StructureStart<?> structurestart = p_62201_.getStartForFeature(sectionpos, StructureFeature.STRONGHOLD, p_62202_);
         if (structurestart == null || !structurestart.isValid()) {
            StructureStart<?> structurestart1 = StructureFeatures.STRONGHOLD.generate(p_62200_, this, this.biomeSource, p_62203_, p_62204_, chunkpos, fetchReferences(p_62201_, p_62202_, sectionpos, StructureFeature.STRONGHOLD), structurefeatureconfiguration, p_62202_, ChunkGenerator::validStrongholdBiome);
            p_62201_.setStartForFeature(sectionpos, StructureFeature.STRONGHOLD, structurestart1, p_62202_);
         }
      }

      Registry<Biome> registry = p_62200_.registryOrThrow(Registry.BIOME_REGISTRY);

      label48:
      for(StructureFeature<?> structurefeature : Registry.STRUCTURE_FEATURE) {
         if (structurefeature != StructureFeature.STRONGHOLD) {
            StructureFeatureConfiguration structurefeatureconfiguration1 = this.settings.getConfig(structurefeature);
            if (structurefeatureconfiguration1 != null) {
               StructureStart<?> structurestart2 = p_62201_.getStartForFeature(sectionpos, structurefeature, p_62202_);
               if (structurestart2 == null || !structurestart2.isValid()) {
                  int i = fetchReferences(p_62201_, p_62202_, sectionpos, structurefeature);

                  for(Entry<ConfiguredStructureFeature<?, ?>, Collection<ResourceKey<Biome>>> entry : this.settings.structures(structurefeature).asMap().entrySet()) {
                     StructureStart<?> structurestart3 = entry.getKey().generate(p_62200_, this, this.biomeSource, p_62203_, p_62204_, chunkpos, i, structurefeatureconfiguration1, p_62202_, (p_196742_) -> {
                        return this.validBiome(registry, entry.getValue()::contains, p_196742_);
                     });
                     if (structurestart3.isValid()) {
                        p_62201_.setStartForFeature(sectionpos, structurefeature, structurestart3, p_62202_);
                        continue label48;
                     }
                  }

                  p_62201_.setStartForFeature(sectionpos, structurefeature, StructureStart.INVALID_START, p_62202_);
               }
            }
         }
      }

   }

   private static int fetchReferences(StructureFeatureManager p_187701_, ChunkAccess p_187702_, SectionPos p_187703_, StructureFeature<?> p_187704_) {
      StructureStart<?> structurestart = p_187701_.getStartForFeature(p_187703_, p_187704_, p_187702_);
      return structurestart != null ? structurestart.getReferences() : 0;
   }

   protected boolean validBiome(Registry<Biome> p_187736_, Predicate<ResourceKey<Biome>> p_187737_, Biome p_187738_) {
      return p_187736_.getResourceKey(p_187738_).filter(p_187737_).isPresent();
   }

   public void createReferences(WorldGenLevel p_62178_, StructureFeatureManager p_62179_, ChunkAccess p_62180_) {
      int i = 8;
      ChunkPos chunkpos = p_62180_.getPos();
      int j = chunkpos.x;
      int k = chunkpos.z;
      int l = chunkpos.getMinBlockX();
      int i1 = chunkpos.getMinBlockZ();
      SectionPos sectionpos = SectionPos.bottomOf(p_62180_);

      for(int j1 = j - 8; j1 <= j + 8; ++j1) {
         for(int k1 = k - 8; k1 <= k + 8; ++k1) {
            long l1 = ChunkPos.asLong(j1, k1);

            for(StructureStart<?> structurestart : p_62178_.getChunk(j1, k1).getAllStarts().values()) {
               try {
                  if (structurestart.isValid() && structurestart.getBoundingBox().intersects(l, i1, l + 15, i1 + 15)) {
                     p_62179_.addReferenceForFeature(sectionpos, structurestart.getFeature(), l1, p_62180_);
                     DebugPackets.sendStructurePacket(p_62178_, structurestart);
                  }
               } catch (Exception exception) {
                  CrashReport crashreport = CrashReport.forThrowable(exception, "Generating structure reference");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Structure");
                  crashreportcategory.setDetail("Id", () -> {
                     return Registry.STRUCTURE_FEATURE.getKey(structurestart.getFeature()).toString();
                  });
                  crashreportcategory.setDetail("Name", () -> {
                     return structurestart.getFeature().getFeatureName();
                  });
                  crashreportcategory.setDetail("Class", () -> {
                     return structurestart.getFeature().getClass().getCanonicalName();
                  });
                  throw new ReportedException(crashreport);
               }
            }
         }
      }

   }

   public abstract CompletableFuture<ChunkAccess> fillFromNoise(Executor p_187748_, Blender p_187749_, StructureFeatureManager p_187750_, ChunkAccess p_187751_);

   public abstract int getSeaLevel();

   public abstract int getMinY();

   public abstract int getBaseHeight(int p_156153_, int p_156154_, Heightmap.Types p_156155_, LevelHeightAccessor p_156156_);

   public abstract NoiseColumn getBaseColumn(int p_156150_, int p_156151_, LevelHeightAccessor p_156152_);

   public int getFirstFreeHeight(int p_156175_, int p_156176_, Heightmap.Types p_156177_, LevelHeightAccessor p_156178_) {
      return this.getBaseHeight(p_156175_, p_156176_, p_156177_, p_156178_);
   }

   public int getFirstOccupiedHeight(int p_156180_, int p_156181_, Heightmap.Types p_156182_, LevelHeightAccessor p_156183_) {
      return this.getBaseHeight(p_156180_, p_156181_, p_156182_, p_156183_) - 1;
   }

   public boolean hasStronghold(ChunkPos p_62173_) {
      this.generateStrongholds();
      return this.strongholdPositions.contains(p_62173_);
   }

   static {
      Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
      Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
      Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
   }
}