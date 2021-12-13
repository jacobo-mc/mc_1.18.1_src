package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class FlatLevelSource extends ChunkGenerator {
   public static final Codec<FlatLevelSource> CODEC = FlatLevelGeneratorSettings.CODEC.fieldOf("settings").xmap(FlatLevelSource::new, FlatLevelSource::settings).codec();
   private final FlatLevelGeneratorSettings settings;

   public FlatLevelSource(FlatLevelGeneratorSettings p_64168_) {
      super(new FixedBiomeSource(p_64168_.getBiomeFromSettings()), new FixedBiomeSource(p_64168_.getBiome()), p_64168_.structureSettings(), 0L);
      this.settings = p_64168_;
   }

   protected Codec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public ChunkGenerator withSeed(long p_64180_) {
      return this;
   }

   public FlatLevelGeneratorSettings settings() {
      return this.settings;
   }

   public void buildSurface(WorldGenRegion p_188554_, StructureFeatureManager p_188555_, ChunkAccess p_188556_) {
   }

   public int getSpawnHeight(LevelHeightAccessor p_158279_) {
      return p_158279_.getMinBuildHeight() + Math.min(p_158279_.getHeight(), this.settings.getLayers().size());
   }

   protected boolean validBiome(Registry<Biome> p_188558_, Predicate<ResourceKey<Biome>> p_188559_, Biome p_188560_) {
      return p_188558_.getResourceKey(this.settings.getBiome()).filter(p_188559_).isPresent();
   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_188562_, Blender p_188563_, StructureFeatureManager p_188564_, ChunkAccess p_188565_) {
      List<BlockState> list = this.settings.getLayers();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      Heightmap heightmap = p_188565_.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
      Heightmap heightmap1 = p_188565_.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

      for(int i = 0; i < Math.min(p_188565_.getHeight(), list.size()); ++i) {
         BlockState blockstate = list.get(i);
         if (blockstate != null) {
            int j = p_188565_.getMinBuildHeight() + i;

            for(int k = 0; k < 16; ++k) {
               for(int l = 0; l < 16; ++l) {
                  p_188565_.setBlockState(blockpos$mutableblockpos.set(k, j, l), blockstate, false);
                  heightmap.update(k, j, l, blockstate);
                  heightmap1.update(k, j, l, blockstate);
               }
            }
         }
      }

      return CompletableFuture.completedFuture(p_188565_);
   }

   public int getBaseHeight(int p_158274_, int p_158275_, Heightmap.Types p_158276_, LevelHeightAccessor p_158277_) {
      List<BlockState> list = this.settings.getLayers();

      for(int i = Math.min(list.size(), p_158277_.getMaxBuildHeight()) - 1; i >= 0; --i) {
         BlockState blockstate = list.get(i);
         if (blockstate != null && p_158276_.isOpaque().test(blockstate)) {
            return p_158277_.getMinBuildHeight() + i + 1;
         }
      }

      return p_158277_.getMinBuildHeight();
   }

   public NoiseColumn getBaseColumn(int p_158270_, int p_158271_, LevelHeightAccessor p_158272_) {
      return new NoiseColumn(p_158272_.getMinBuildHeight(), this.settings.getLayers().stream().limit((long)p_158272_.getHeight()).map((p_64189_) -> {
         return p_64189_ == null ? Blocks.AIR.defaultBlockState() : p_64189_;
      }).toArray((p_64171_) -> {
         return new BlockState[p_64171_];
      }));
   }

   public Climate.Sampler climateSampler() {
      return (p_188541_, p_188542_, p_188543_) -> {
         return Climate.target(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      };
   }

   public void applyCarvers(WorldGenRegion p_188547_, long p_188548_, BiomeManager p_188549_, StructureFeatureManager p_188550_, ChunkAccess p_188551_, GenerationStep.Carving p_188552_) {
   }

   public void spawnOriginalMobs(WorldGenRegion p_188545_) {
   }

   public int getMinY() {
      return 0;
   }

   public int getGenDepth() {
      return 384;
   }

   public int getSeaLevel() {
      return -63;
   }
}