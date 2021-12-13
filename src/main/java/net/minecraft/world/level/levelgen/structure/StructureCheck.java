package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureCheck {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int NO_STRUCTURE = -1;
   private final ChunkScanAccess storageAccess;
   private final RegistryAccess registryAccess;
   private final Registry<Biome> biomes;
   private final StructureManager structureManager;
   private final ResourceKey<Level> dimension;
   private final ChunkGenerator chunkGenerator;
   private final LevelHeightAccessor heightAccessor;
   private final BiomeSource biomeSource;
   private final long seed;
   private final DataFixer fixerUpper;
   private final Long2ObjectMap<Object2IntMap<StructureFeature<?>>> loadedChunks = new Long2ObjectOpenHashMap<>();
   private final Map<StructureFeature<?>, Long2BooleanMap> featureChecks = new HashMap<>();

   public StructureCheck(ChunkScanAccess p_197251_, RegistryAccess p_197252_, StructureManager p_197253_, ResourceKey<Level> p_197254_, ChunkGenerator p_197255_, LevelHeightAccessor p_197256_, BiomeSource p_197257_, long p_197258_, DataFixer p_197259_) {
      this.storageAccess = p_197251_;
      this.registryAccess = p_197252_;
      this.structureManager = p_197253_;
      this.dimension = p_197254_;
      this.chunkGenerator = p_197255_;
      this.heightAccessor = p_197256_;
      this.biomeSource = p_197257_;
      this.seed = p_197258_;
      this.fixerUpper = p_197259_;
      this.biomes = p_197252_.ownedRegistryOrThrow(Registry.BIOME_REGISTRY);
   }

   public <F extends StructureFeature<?>> StructureCheckResult checkStart(ChunkPos p_197274_, F p_197275_, boolean p_197276_) {
      long i = p_197274_.toLong();
      Object2IntMap<StructureFeature<?>> object2intmap = this.loadedChunks.get(i);
      if (object2intmap != null) {
         return this.checkStructureInfo(object2intmap, p_197275_, p_197276_);
      } else {
         StructureCheckResult structurecheckresult = this.tryLoadFromStorage(p_197274_, p_197275_, p_197276_, i);
         if (structurecheckresult != null) {
            return structurecheckresult;
         } else {
            boolean flag = this.featureChecks.computeIfAbsent(p_197275_, (p_197286_) -> {
               return new Long2BooleanOpenHashMap();
            }).computeIfAbsent(i, (p_197290_) -> {
               Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> multimap = this.chunkGenerator.getSettings().structures(p_197275_);

               for(Entry<ConfiguredStructureFeature<?, ?>, Collection<ResourceKey<Biome>>> entry : multimap.asMap().entrySet()) {
                  if (this.canCreateStructure(p_197274_, entry.getKey(), entry.getValue())) {
                     return true;
                  }
               }

               return false;
            });
            return !flag ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.CHUNK_LOAD_NEEDED;
         }
      }
   }

   private <FC extends FeatureConfiguration, F extends StructureFeature<FC>> boolean canCreateStructure(ChunkPos p_197267_, ConfiguredStructureFeature<FC, F> p_197268_, Collection<ResourceKey<Biome>> p_197269_) {
      Predicate<Biome> predicate = (p_197310_) -> {
         return this.biomes.getResourceKey(p_197310_).filter(p_197269_::contains).isPresent();
      };
      return p_197268_.feature.canGenerate(this.registryAccess, this.chunkGenerator, this.biomeSource, this.structureManager, this.seed, p_197267_, p_197268_.config, this.heightAccessor, predicate);
   }

   @Nullable
   private StructureCheckResult tryLoadFromStorage(ChunkPos p_197278_, StructureFeature<?> p_197279_, boolean p_197280_, long p_197281_) {
      CollectFields collectfields = new CollectFields(new CollectFields.WantedField(IntTag.TYPE, "DataVersion"), new CollectFields.WantedField("Level", "Structures", CompoundTag.TYPE, "Starts"), new CollectFields.WantedField("structures", CompoundTag.TYPE, "starts"));

      try {
         this.storageAccess.scanChunk(p_197278_, collectfields).join();
      } catch (Exception exception1) {
         LOGGER.warn("Failed to read chunk {}", p_197278_, exception1);
         return StructureCheckResult.CHUNK_LOAD_NEEDED;
      }

      Tag tag = collectfields.getResult();
      if (!(tag instanceof CompoundTag)) {
         return null;
      } else {
         CompoundTag compoundtag = (CompoundTag)tag;
         int i = ChunkStorage.getVersion(compoundtag);
         if (i <= 1493) {
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
         } else {
            ChunkStorage.injectDatafixingContext(compoundtag, this.dimension, this.chunkGenerator.getTypeNameForDataFixer());

            CompoundTag compoundtag1;
            try {
               compoundtag1 = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, compoundtag, i);
            } catch (Exception exception) {
               LOGGER.warn("Failed to partially datafix chunk {}", p_197278_, exception);
               return StructureCheckResult.CHUNK_LOAD_NEEDED;
            }

            Object2IntMap<StructureFeature<?>> object2intmap = this.loadStructures(compoundtag1);
            if (object2intmap == null) {
               return null;
            } else {
               this.storeFullResults(p_197281_, object2intmap);
               return this.checkStructureInfo(object2intmap, p_197279_, p_197280_);
            }
         }
      }
   }

   @Nullable
   private Object2IntMap<StructureFeature<?>> loadStructures(CompoundTag p_197312_) {
      if (!p_197312_.contains("structures", 10)) {
         return null;
      } else {
         CompoundTag compoundtag = p_197312_.getCompound("structures");
         if (!compoundtag.contains("starts", 10)) {
            return null;
         } else {
            CompoundTag compoundtag1 = compoundtag.getCompound("starts");
            if (compoundtag1.isEmpty()) {
               return Object2IntMaps.emptyMap();
            } else {
               Object2IntMap<StructureFeature<?>> object2intmap = new Object2IntOpenHashMap<>();

               for(String s : compoundtag1.getAllKeys()) {
                  String s1 = s.toLowerCase(Locale.ROOT);
                  StructureFeature<?> structurefeature = StructureFeature.STRUCTURES_REGISTRY.get(s1);
                  if (structurefeature != null) {
                     CompoundTag compoundtag2 = compoundtag1.getCompound(s);
                     if (!compoundtag2.isEmpty()) {
                        String s2 = compoundtag2.getString("id");
                        if (!"INVALID".equals(s2)) {
                           int i = compoundtag2.getInt("references");
                           object2intmap.put(structurefeature, i);
                        }
                     }
                  }
               }

               return object2intmap;
            }
         }
      }
   }

   private static Object2IntMap<StructureFeature<?>> deduplicateEmptyMap(Object2IntMap<StructureFeature<?>> p_197299_) {
      return p_197299_.isEmpty() ? Object2IntMaps.emptyMap() : p_197299_;
   }

   private StructureCheckResult checkStructureInfo(Object2IntMap<StructureFeature<?>> p_197305_, StructureFeature<?> p_197306_, boolean p_197307_) {
      int i = p_197305_.getOrDefault(p_197306_, -1);
      return i == -1 || p_197307_ && i != 0 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.START_PRESENT;
   }

   public void onStructureLoad(ChunkPos p_197283_, Map<StructureFeature<?>, StructureStart<?>> p_197284_) {
      long i = p_197283_.toLong();
      Object2IntMap<StructureFeature<?>> object2intmap = new Object2IntOpenHashMap<>();
      p_197284_.forEach((p_197302_, p_197303_) -> {
         if (p_197303_.isValid()) {
            object2intmap.put(p_197302_, p_197303_.getReferences());
         }

      });
      this.storeFullResults(i, object2intmap);
   }

   private void storeFullResults(long p_197264_, Object2IntMap<StructureFeature<?>> p_197265_) {
      this.loadedChunks.put(p_197264_, deduplicateEmptyMap(p_197265_));
      this.featureChecks.values().forEach((p_197262_) -> {
         p_197262_.remove(p_197264_);
      });
   }

   public void incrementReference(ChunkPos p_197271_, StructureFeature<?> p_197272_) {
      this.loadedChunks.compute(p_197271_.toLong(), (p_197296_, p_197297_) -> {
         if (p_197297_ == null || p_197297_.isEmpty()) {
            p_197297_ = new Object2IntOpenHashMap<>();
         }

         p_197297_.computeInt(p_197272_, (p_197292_, p_197293_) -> {
            return p_197293_ == null ? 1 : p_197293_ + 1;
         });
         return p_197297_;
      });
   }
}