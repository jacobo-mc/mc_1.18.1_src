package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.QuartPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
   private static final Blender EMPTY = new Blender((WorldGenRegion)null, List.of(), List.of()) {
      public TerrainInfo blendOffsetAndFactor(int p_190228_, int p_190229_, TerrainInfo p_190230_) {
         return p_190230_;
      }

      public double blendDensity(int p_190223_, int p_190224_, int p_190225_, double p_190226_) {
         return p_190226_;
      }

      public BiomeResolver getBiomeResolver(BiomeResolver p_190232_) {
         return p_190232_;
      }
   };
   private static final NormalNoise SHIFT_NOISE = NormalNoise.create(new XoroshiroRandomSource(42L), BuiltinRegistries.NOISE.getOrThrow(Noises.SHIFT));
   private static final int HEIGHT_BLENDING_RANGE_CELLS = QuartPos.fromSection(7) - 1;
   private static final int HEIGHT_BLENDING_RANGE_CHUNKS = QuartPos.toSection(HEIGHT_BLENDING_RANGE_CELLS + 3);
   private static final int DENSITY_BLENDING_RANGE_CELLS = 2;
   private static final int DENSITY_BLENDING_RANGE_CHUNKS = QuartPos.toSection(5);
   private static final double BLENDING_FACTOR = 10.0D;
   private static final double BLENDING_JAGGEDNESS = 0.0D;
   private static final double OLD_CHUNK_Y_RADIUS = (double)BlendingData.AREA_WITH_OLD_GENERATION.getHeight() / 2.0D;
   private static final double OLD_CHUNK_CENTER_Y = (double)BlendingData.AREA_WITH_OLD_GENERATION.getMinBuildHeight() + OLD_CHUNK_Y_RADIUS;
   private static final double OLD_CHUNK_XZ_RADIUS = 8.0D;
   private final WorldGenRegion region;
   private final List<Blender.PositionedBlendingData> heightData;
   private final List<Blender.PositionedBlendingData> densityData;

   public static Blender empty() {
      return EMPTY;
   }

   public static Blender of(@Nullable WorldGenRegion p_190203_) {
      if (p_190203_ == null) {
         return EMPTY;
      } else {
         List<Blender.PositionedBlendingData> list = Lists.newArrayList();
         List<Blender.PositionedBlendingData> list1 = Lists.newArrayList();
         ChunkPos chunkpos = p_190203_.getCenter();

         for(int i = -HEIGHT_BLENDING_RANGE_CHUNKS; i <= HEIGHT_BLENDING_RANGE_CHUNKS; ++i) {
            for(int j = -HEIGHT_BLENDING_RANGE_CHUNKS; j <= HEIGHT_BLENDING_RANGE_CHUNKS; ++j) {
               int k = chunkpos.x + i;
               int l = chunkpos.z + j;
               BlendingData blendingdata = BlendingData.getOrUpdateBlendingData(p_190203_, k, l);
               if (blendingdata != null) {
                  Blender.PositionedBlendingData blender$positionedblendingdata = new Blender.PositionedBlendingData(k, l, blendingdata);
                  list.add(blender$positionedblendingdata);
                  if (i >= -DENSITY_BLENDING_RANGE_CHUNKS && i <= DENSITY_BLENDING_RANGE_CHUNKS && j >= -DENSITY_BLENDING_RANGE_CHUNKS && j <= DENSITY_BLENDING_RANGE_CHUNKS) {
                     list1.add(blender$positionedblendingdata);
                  }
               }
            }
         }

         return list.isEmpty() && list1.isEmpty() ? EMPTY : new Blender(p_190203_, list, list1);
      }
   }

   Blender(WorldGenRegion p_190150_, List<Blender.PositionedBlendingData> p_190151_, List<Blender.PositionedBlendingData> p_190152_) {
      this.region = p_190150_;
      this.heightData = p_190151_;
      this.densityData = p_190152_;
   }

   public TerrainInfo blendOffsetAndFactor(int p_190190_, int p_190191_, TerrainInfo p_190192_) {
      int i = QuartPos.fromBlock(p_190190_);
      int j = QuartPos.fromBlock(p_190191_);
      double d0 = this.getBlendingDataValue(i, 0, j, BlendingData::getHeight);
      if (d0 != Double.MAX_VALUE) {
         return new TerrainInfo(heightToOffset(d0), 10.0D, 0.0D);
      } else {
         MutableDouble mutabledouble = new MutableDouble(0.0D);
         MutableDouble mutabledouble1 = new MutableDouble(0.0D);
         MutableDouble mutabledouble2 = new MutableDouble(Double.POSITIVE_INFINITY);

         for(Blender.PositionedBlendingData blender$positionedblendingdata : this.heightData) {
            blender$positionedblendingdata.blendingData.iterateHeights(QuartPos.fromSection(blender$positionedblendingdata.chunkX), QuartPos.fromSection(blender$positionedblendingdata.chunkZ), (p_190199_, p_190200_, p_190201_) -> {
               double d6 = Mth.length((double)(i - p_190199_), (double)(j - p_190200_));
               if (!(d6 > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
                  if (d6 < mutabledouble2.doubleValue()) {
                     mutabledouble2.setValue(d6);
                  }

                  double d7 = 1.0D / (d6 * d6 * d6 * d6);
                  mutabledouble1.add(p_190201_ * d7);
                  mutabledouble.add(d7);
               }
            });
         }

         if (mutabledouble2.doubleValue() == Double.POSITIVE_INFINITY) {
            return p_190192_;
         } else {
            double d5 = mutabledouble1.doubleValue() / mutabledouble.doubleValue();
            double d1 = Mth.clamp(mutabledouble2.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0D, 1.0D);
            d1 = 3.0D * d1 * d1 - 2.0D * d1 * d1 * d1;
            double d2 = Mth.lerp(d1, heightToOffset(d5), p_190192_.offset());
            double d3 = Mth.lerp(d1, 10.0D, p_190192_.factor());
            double d4 = Mth.lerp(d1, 0.0D, p_190192_.jaggedness());
            return new TerrainInfo(d2, d3, d4);
         }
      }
   }

   private static double heightToOffset(double p_190155_) {
      double d0 = 1.0D;
      double d1 = p_190155_ + 0.5D;
      double d2 = Mth.positiveModulo(d1, 8.0D);
      return 1.0D * (32.0D * (d1 - 128.0D) - 3.0D * (d1 - 120.0D) * d2 + 3.0D * d2 * d2) / (128.0D * (32.0D - 3.0D * d2));
   }

   public double blendDensity(int p_190170_, int p_190171_, int p_190172_, double p_190173_) {
      int i = QuartPos.fromBlock(p_190170_);
      int j = p_190171_ / 8;
      int k = QuartPos.fromBlock(p_190172_);
      double d0 = this.getBlendingDataValue(i, j, k, BlendingData::getDensity);
      if (d0 != Double.MAX_VALUE) {
         return d0;
      } else {
         MutableDouble mutabledouble = new MutableDouble(0.0D);
         MutableDouble mutabledouble1 = new MutableDouble(0.0D);
         MutableDouble mutabledouble2 = new MutableDouble(Double.POSITIVE_INFINITY);

         for(Blender.PositionedBlendingData blender$positionedblendingdata : this.densityData) {
            blender$positionedblendingdata.blendingData.iterateDensities(QuartPos.fromSection(blender$positionedblendingdata.chunkX), QuartPos.fromSection(blender$positionedblendingdata.chunkZ), j - 1, j + 1, (p_190186_, p_190187_, p_190188_, p_190189_) -> {
               double d3 = Mth.length((double)(i - p_190186_), (double)((j - p_190187_) * 2), (double)(k - p_190188_));
               if (!(d3 > 2.0D)) {
                  if (d3 < mutabledouble2.doubleValue()) {
                     mutabledouble2.setValue(d3);
                  }

                  double d4 = 1.0D / (d3 * d3 * d3 * d3);
                  mutabledouble1.add(p_190189_ * d4);
                  mutabledouble.add(d4);
               }
            });
         }

         if (mutabledouble2.doubleValue() == Double.POSITIVE_INFINITY) {
            return p_190173_;
         } else {
            double d2 = mutabledouble1.doubleValue() / mutabledouble.doubleValue();
            double d1 = Mth.clamp(mutabledouble2.doubleValue() / 3.0D, 0.0D, 1.0D);
            return Mth.lerp(d1, d2, p_190173_);
         }
      }
   }

   private double getBlendingDataValue(int p_190175_, int p_190176_, int p_190177_, Blender.CellValueGetter p_190178_) {
      int i = QuartPos.toSection(p_190175_);
      int j = QuartPos.toSection(p_190177_);
      boolean flag = (p_190175_ & 3) == 0;
      boolean flag1 = (p_190177_ & 3) == 0;
      double d0 = this.getBlendingDataValue(p_190178_, i, j, p_190175_, p_190176_, p_190177_);
      if (d0 == Double.MAX_VALUE) {
         if (flag && flag1) {
            d0 = this.getBlendingDataValue(p_190178_, i - 1, j - 1, p_190175_, p_190176_, p_190177_);
         }

         if (d0 == Double.MAX_VALUE) {
            if (flag) {
               d0 = this.getBlendingDataValue(p_190178_, i - 1, j, p_190175_, p_190176_, p_190177_);
            }

            if (d0 == Double.MAX_VALUE && flag1) {
               d0 = this.getBlendingDataValue(p_190178_, i, j - 1, p_190175_, p_190176_, p_190177_);
            }
         }
      }

      return d0;
   }

   private double getBlendingDataValue(Blender.CellValueGetter p_190212_, int p_190213_, int p_190214_, int p_190215_, int p_190216_, int p_190217_) {
      BlendingData blendingdata = BlendingData.getOrUpdateBlendingData(this.region, p_190213_, p_190214_);
      return blendingdata != null ? p_190212_.get(blendingdata, p_190215_ - QuartPos.fromSection(p_190213_), p_190216_, p_190217_ - QuartPos.fromSection(p_190214_)) : Double.MAX_VALUE;
   }

   public BiomeResolver getBiomeResolver(BiomeResolver p_190204_) {
      return (p_190207_, p_190208_, p_190209_, p_190210_) -> {
         Biome biome = this.blendBiome(p_190207_, p_190208_, p_190209_);
         return biome == null ? p_190204_.getNoiseBiome(p_190207_, p_190208_, p_190209_, p_190210_) : biome;
      };
   }

   @Nullable
   private Biome blendBiome(int p_190167_, int p_190168_, int p_190169_) {
      double d0 = (double)p_190167_ + SHIFT_NOISE.getValue((double)p_190167_, 0.0D, (double)p_190169_) * 12.0D;
      double d1 = (double)p_190169_ + SHIFT_NOISE.getValue((double)p_190169_, (double)p_190167_, 0.0D) * 12.0D;
      MutableDouble mutabledouble = new MutableDouble(Double.POSITIVE_INFINITY);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      MutableObject<ChunkPos> mutableobject = new MutableObject<>();

      for(Blender.PositionedBlendingData blender$positionedblendingdata : this.heightData) {
         blender$positionedblendingdata.blendingData.iterateHeights(QuartPos.fromSection(blender$positionedblendingdata.chunkX), QuartPos.fromSection(blender$positionedblendingdata.chunkZ), (p_190163_, p_190164_, p_190165_) -> {
            double d3 = Mth.length(d0 - (double)p_190163_, d1 - (double)p_190164_);
            if (!(d3 > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
               if (d3 < mutabledouble.doubleValue()) {
                  mutableobject.setValue(new ChunkPos(blender$positionedblendingdata.chunkX, blender$positionedblendingdata.chunkZ));
                  blockpos$mutableblockpos.set(p_190163_, QuartPos.fromBlock(Mth.floor(p_190165_)), p_190164_);
                  mutabledouble.setValue(d3);
               }

            }
         });
      }

      if (mutabledouble.doubleValue() == Double.POSITIVE_INFINITY) {
         return null;
      } else {
         double d2 = Mth.clamp(mutabledouble.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0D, 1.0D);
         if (d2 > 0.5D) {
            return null;
         } else {
            ChunkAccess chunkaccess = this.region.getChunk((mutableobject.getValue()).x, (mutableobject.getValue()).z);
            return chunkaccess.getNoiseBiome(Math.min(blockpos$mutableblockpos.getX() & 3, 3), blockpos$mutableblockpos.getY(), Math.min(blockpos$mutableblockpos.getZ() & 3, 3));
         }
      }
   }

   public static void generateBorderTicks(WorldGenRegion p_197032_, ChunkAccess p_197033_) {
      ChunkPos chunkpos = p_197033_.getPos();
      boolean flag = p_197033_.isOldNoiseGeneration();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 0, chunkpos.getMinBlockZ());
      int i = BlendingData.AREA_WITH_OLD_GENERATION.getMinBuildHeight();
      int j = BlendingData.AREA_WITH_OLD_GENERATION.getMaxBuildHeight() - 1;
      if (flag) {
         for(int k = 0; k < 16; ++k) {
            for(int l = 0; l < 16; ++l) {
               generateBorderTick(p_197033_, blockpos$mutableblockpos.setWithOffset(blockpos, k, i - 1, l));
               generateBorderTick(p_197033_, blockpos$mutableblockpos.setWithOffset(blockpos, k, i, l));
               generateBorderTick(p_197033_, blockpos$mutableblockpos.setWithOffset(blockpos, k, j, l));
               generateBorderTick(p_197033_, blockpos$mutableblockpos.setWithOffset(blockpos, k, j + 1, l));
            }
         }
      }

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (p_197032_.getChunk(chunkpos.x + direction.getStepX(), chunkpos.z + direction.getStepZ()).isOldNoiseGeneration() != flag) {
            int i1 = direction == Direction.EAST ? 15 : 0;
            int j1 = direction == Direction.WEST ? 0 : 15;
            int k1 = direction == Direction.SOUTH ? 15 : 0;
            int l1 = direction == Direction.NORTH ? 0 : 15;

            for(int i2 = i1; i2 <= j1; ++i2) {
               for(int j2 = k1; j2 <= l1; ++j2) {
                  int k2 = Math.min(j, p_197033_.getHeight(Heightmap.Types.MOTION_BLOCKING, i2, j2)) + 1;

                  for(int l2 = i; l2 < k2; ++l2) {
                     generateBorderTick(p_197033_, blockpos$mutableblockpos.setWithOffset(blockpos, i2, l2, j2));
                  }
               }
            }
         }
      }

   }

   private static void generateBorderTick(ChunkAccess p_197041_, BlockPos p_197042_) {
      BlockState blockstate = p_197041_.getBlockState(p_197042_);
      if (blockstate.is(BlockTags.LEAVES)) {
         p_197041_.markPosForPostprocessing(p_197042_);
      }

      FluidState fluidstate = p_197041_.getFluidState(p_197042_);
      if (!fluidstate.isEmpty()) {
         p_197041_.markPosForPostprocessing(p_197042_);
      }

   }

   public static void addAroundOldChunksCarvingMaskFilter(WorldGenLevel p_197035_, ProtoChunk p_197036_) {
      ChunkPos chunkpos = p_197036_.getPos();
      Blender.DistanceGetter blender$distancegetter = makeOldChunkDistanceGetter(p_197036_.isOldNoiseGeneration(), BlendingData.sideByGenerationAge(p_197035_, chunkpos.x, chunkpos.z, true));
      if (blender$distancegetter != null) {
         CarvingMask.Mask carvingmask$mask = (p_197045_, p_197046_, p_197047_) -> {
            double d0 = (double)p_197045_ + 0.5D + SHIFT_NOISE.getValue((double)p_197045_, (double)p_197046_, (double)p_197047_) * 4.0D;
            double d1 = (double)p_197046_ + 0.5D + SHIFT_NOISE.getValue((double)p_197046_, (double)p_197047_, (double)p_197045_) * 4.0D;
            double d2 = (double)p_197047_ + 0.5D + SHIFT_NOISE.getValue((double)p_197047_, (double)p_197045_, (double)p_197046_) * 4.0D;
            return blender$distancegetter.getDistance(d0, d1, d2) < 4.0D;
         };
         Stream.of(GenerationStep.Carving.values()).map(p_197036_::getOrCreateCarvingMask).forEach((p_197039_) -> {
            p_197039_.setAdditionalMask(carvingmask$mask);
         });
      }
   }

   @Nullable
   public static Blender.DistanceGetter makeOldChunkDistanceGetter(boolean p_197059_, Set<Direction8> p_197060_) {
      if (!p_197059_ && p_197060_.isEmpty()) {
         return null;
      } else {
         List<Blender.DistanceGetter> list = Lists.newArrayList();
         if (p_197059_) {
            list.add(makeOffsetOldChunkDistanceGetter((Direction8)null));
         }

         p_197060_.forEach((p_197057_) -> {
            list.add(makeOffsetOldChunkDistanceGetter(p_197057_));
         });
         return (p_197052_, p_197053_, p_197054_) -> {
            double d0 = Double.POSITIVE_INFINITY;

            for(Blender.DistanceGetter blender$distancegetter : list) {
               double d1 = blender$distancegetter.getDistance(p_197052_, p_197053_, p_197054_);
               if (d1 < d0) {
                  d0 = d1;
               }
            }

            return d0;
         };
      }
   }

   private static Blender.DistanceGetter makeOffsetOldChunkDistanceGetter(@Nullable Direction8 p_197049_) {
      double d0 = 0.0D;
      double d1 = 0.0D;
      if (p_197049_ != null) {
         for(Direction direction : p_197049_.getDirections()) {
            d0 += (double)(direction.getStepX() * 16);
            d1 += (double)(direction.getStepZ() * 16);
         }
      }

      double d3 = d0;
      double d2 = d1;
      return (p_197021_, p_197022_, p_197023_) -> {
         return distanceToCube(p_197021_ - 8.0D - d3, p_197022_ - OLD_CHUNK_CENTER_Y, p_197023_ - 8.0D - d2, 8.0D, OLD_CHUNK_Y_RADIUS, 8.0D);
      };
   }

   private static double distanceToCube(double p_197025_, double p_197026_, double p_197027_, double p_197028_, double p_197029_, double p_197030_) {
      double d0 = Math.abs(p_197025_) - p_197028_;
      double d1 = Math.abs(p_197026_) - p_197029_;
      double d2 = Math.abs(p_197027_) - p_197030_;
      return Mth.length(Math.max(0.0D, d0), Math.max(0.0D, d1), Math.max(0.0D, d2));
   }

   interface CellValueGetter {
      double get(BlendingData p_190234_, int p_190235_, int p_190236_, int p_190237_);
   }

   public interface DistanceGetter {
      double getDistance(double p_197062_, double p_197063_, double p_197064_);
   }

   static record PositionedBlendingData(int chunkX, int chunkZ, BlendingData blendingData) {
   }
}