package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LiquidBlockRenderer {
   private static final float MAX_FLUID_HEIGHT = 0.8888889F;
   private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
   private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
   private TextureAtlasSprite waterOverlay;

   protected void setupSprites() {
      this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
      this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
      this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
      this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
      this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
   }

   private static boolean isNeighborSameFluid(BlockGetter p_110974_, BlockPos p_110975_, Direction p_110976_, FluidState p_110977_) {
      BlockPos blockpos = p_110975_.relative(p_110976_);
      FluidState fluidstate = p_110974_.getFluidState(blockpos);
      return fluidstate.getType().isSame(p_110977_.getType());
   }

   private static boolean isFaceOccludedByState(BlockGetter p_110979_, Direction p_110980_, float p_110981_, BlockPos p_110982_, BlockState p_110983_) {
      if (p_110983_.canOcclude()) {
         VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)p_110981_, 1.0D);
         VoxelShape voxelshape1 = p_110983_.getOcclusionShape(p_110979_, p_110982_);
         return Shapes.blockOccudes(voxelshape, voxelshape1, p_110980_);
      } else {
         return false;
      }
   }

   private static boolean isFaceOccludedByNeighbor(BlockGetter p_110969_, BlockPos p_110970_, Direction p_110971_, float p_110972_) {
      BlockPos blockpos = p_110970_.relative(p_110971_);
      BlockState blockstate = p_110969_.getBlockState(blockpos);
      return isFaceOccludedByState(p_110969_, p_110971_, p_110972_, blockpos, blockstate);
   }

   private static boolean isFaceOccludedBySelf(BlockGetter p_110960_, BlockPos p_110961_, BlockState p_110962_, Direction p_110963_) {
      return isFaceOccludedByState(p_110960_, p_110963_.getOpposite(), 1.0F, p_110961_, p_110962_);
   }

   public static boolean shouldRenderFace(BlockAndTintGetter p_110949_, BlockPos p_110950_, FluidState p_110951_, BlockState p_110952_, Direction p_110953_) {
      return !isFaceOccludedBySelf(p_110949_, p_110950_, p_110952_, p_110953_) && !isNeighborSameFluid(p_110949_, p_110950_, p_110953_, p_110951_);
   }

   public boolean tesselate(BlockAndTintGetter p_110955_, BlockPos p_110956_, VertexConsumer p_110957_, FluidState p_110958_) {
      boolean flag = p_110958_.is(FluidTags.LAVA);
      TextureAtlasSprite[] atextureatlassprite = flag ? this.lavaIcons : this.waterIcons;
      BlockState blockstate = p_110955_.getBlockState(p_110956_);
      int i = flag ? 16777215 : BiomeColors.getAverageWaterColor(p_110955_, p_110956_);
      float f = (float)(i >> 16 & 255) / 255.0F;
      float f1 = (float)(i >> 8 & 255) / 255.0F;
      float f2 = (float)(i & 255) / 255.0F;
      boolean flag1 = !isNeighborSameFluid(p_110955_, p_110956_, Direction.UP, p_110958_);
      boolean flag2 = shouldRenderFace(p_110955_, p_110956_, p_110958_, blockstate, Direction.DOWN) && !isFaceOccludedByNeighbor(p_110955_, p_110956_, Direction.DOWN, 0.8888889F);
      boolean flag3 = shouldRenderFace(p_110955_, p_110956_, p_110958_, blockstate, Direction.NORTH);
      boolean flag4 = shouldRenderFace(p_110955_, p_110956_, p_110958_, blockstate, Direction.SOUTH);
      boolean flag5 = shouldRenderFace(p_110955_, p_110956_, p_110958_, blockstate, Direction.WEST);
      boolean flag6 = shouldRenderFace(p_110955_, p_110956_, p_110958_, blockstate, Direction.EAST);
      if (!flag1 && !flag2 && !flag6 && !flag5 && !flag3 && !flag4) {
         return false;
      } else {
         boolean flag7 = false;
         float f3 = p_110955_.getShade(Direction.DOWN, true);
         float f4 = p_110955_.getShade(Direction.UP, true);
         float f5 = p_110955_.getShade(Direction.NORTH, true);
         float f6 = p_110955_.getShade(Direction.WEST, true);
         float f7 = this.getWaterHeight(p_110955_, p_110956_, p_110958_.getType());
         float f8 = this.getWaterHeight(p_110955_, p_110956_.south(), p_110958_.getType());
         float f9 = this.getWaterHeight(p_110955_, p_110956_.east().south(), p_110958_.getType());
         float f10 = this.getWaterHeight(p_110955_, p_110956_.east(), p_110958_.getType());
         double d0 = (double)(p_110956_.getX() & 15);
         double d1 = (double)(p_110956_.getY() & 15);
         double d2 = (double)(p_110956_.getZ() & 15);
         float f11 = 0.001F;
         float f12 = flag2 ? 0.001F : 0.0F;
         if (flag1 && !isFaceOccludedByNeighbor(p_110955_, p_110956_, Direction.UP, Math.min(Math.min(f7, f8), Math.min(f9, f10)))) {
            flag7 = true;
            f7 -= 0.001F;
            f8 -= 0.001F;
            f9 -= 0.001F;
            f10 -= 0.001F;
            Vec3 vec3 = p_110958_.getFlow(p_110955_, p_110956_);
            float f13;
            float f14;
            float f15;
            float f16;
            float f17;
            float f18;
            float f19;
            float f20;
            if (vec3.x == 0.0D && vec3.z == 0.0D) {
               TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
               f13 = textureatlassprite1.getU(0.0D);
               f17 = textureatlassprite1.getV(0.0D);
               f14 = f13;
               f18 = textureatlassprite1.getV(16.0D);
               f15 = textureatlassprite1.getU(16.0D);
               f19 = f18;
               f16 = f15;
               f20 = f17;
            } else {
               TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
               float f21 = (float)Mth.atan2(vec3.z, vec3.x) - ((float)Math.PI / 2F);
               float f22 = Mth.sin(f21) * 0.25F;
               float f23 = Mth.cos(f21) * 0.25F;
               float f24 = 8.0F;
               f13 = textureatlassprite.getU((double)(8.0F + (-f23 - f22) * 16.0F));
               f17 = textureatlassprite.getV((double)(8.0F + (-f23 + f22) * 16.0F));
               f14 = textureatlassprite.getU((double)(8.0F + (-f23 + f22) * 16.0F));
               f18 = textureatlassprite.getV((double)(8.0F + (f23 + f22) * 16.0F));
               f15 = textureatlassprite.getU((double)(8.0F + (f23 + f22) * 16.0F));
               f19 = textureatlassprite.getV((double)(8.0F + (f23 - f22) * 16.0F));
               f16 = textureatlassprite.getU((double)(8.0F + (f23 - f22) * 16.0F));
               f20 = textureatlassprite.getV((double)(8.0F + (-f23 - f22) * 16.0F));
            }

            float f44 = (f13 + f14 + f15 + f16) / 4.0F;
            float f45 = (f17 + f18 + f19 + f20) / 4.0F;
            float f46 = (float)atextureatlassprite[0].getWidth() / (atextureatlassprite[0].getU1() - atextureatlassprite[0].getU0());
            float f47 = (float)atextureatlassprite[0].getHeight() / (atextureatlassprite[0].getV1() - atextureatlassprite[0].getV0());
            float f48 = 4.0F / Math.max(f47, f46);
            f13 = Mth.lerp(f48, f13, f44);
            f14 = Mth.lerp(f48, f14, f44);
            f15 = Mth.lerp(f48, f15, f44);
            f16 = Mth.lerp(f48, f16, f44);
            f17 = Mth.lerp(f48, f17, f45);
            f18 = Mth.lerp(f48, f18, f45);
            f19 = Mth.lerp(f48, f19, f45);
            f20 = Mth.lerp(f48, f20, f45);
            int j = this.getLightColor(p_110955_, p_110956_);
            float f25 = f4 * f;
            float f26 = f4 * f1;
            float f27 = f4 * f2;
            this.vertex(p_110957_, d0 + 0.0D, d1 + (double)f7, d2 + 0.0D, f25, f26, f27, f13, f17, j);
            this.vertex(p_110957_, d0 + 0.0D, d1 + (double)f8, d2 + 1.0D, f25, f26, f27, f14, f18, j);
            this.vertex(p_110957_, d0 + 1.0D, d1 + (double)f9, d2 + 1.0D, f25, f26, f27, f15, f19, j);
            this.vertex(p_110957_, d0 + 1.0D, d1 + (double)f10, d2 + 0.0D, f25, f26, f27, f16, f20, j);
            if (p_110958_.shouldRenderBackwardUpFace(p_110955_, p_110956_.above())) {
               this.vertex(p_110957_, d0 + 0.0D, d1 + (double)f7, d2 + 0.0D, f25, f26, f27, f13, f17, j);
               this.vertex(p_110957_, d0 + 1.0D, d1 + (double)f10, d2 + 0.0D, f25, f26, f27, f16, f20, j);
               this.vertex(p_110957_, d0 + 1.0D, d1 + (double)f9, d2 + 1.0D, f25, f26, f27, f15, f19, j);
               this.vertex(p_110957_, d0 + 0.0D, d1 + (double)f8, d2 + 1.0D, f25, f26, f27, f14, f18, j);
            }
         }

         if (flag2) {
            float f35 = atextureatlassprite[0].getU0();
            float f36 = atextureatlassprite[0].getU1();
            float f37 = atextureatlassprite[0].getV0();
            float f39 = atextureatlassprite[0].getV1();
            int i1 = this.getLightColor(p_110955_, p_110956_.below());
            float f41 = f3 * f;
            float f42 = f3 * f1;
            float f43 = f3 * f2;
            this.vertex(p_110957_, d0, d1 + (double)f12, d2 + 1.0D, f41, f42, f43, f35, f39, i1);
            this.vertex(p_110957_, d0, d1 + (double)f12, d2, f41, f42, f43, f35, f37, i1);
            this.vertex(p_110957_, d0 + 1.0D, d1 + (double)f12, d2, f41, f42, f43, f36, f37, i1);
            this.vertex(p_110957_, d0 + 1.0D, d1 + (double)f12, d2 + 1.0D, f41, f42, f43, f36, f39, i1);
            flag7 = true;
         }

         int k = this.getLightColor(p_110955_, p_110956_);

         for(int l = 0; l < 4; ++l) {
            float f38;
            float f40;
            double d3;
            double d4;
            double d5;
            double d6;
            Direction direction;
            boolean flag8;
            if (l == 0) {
               f38 = f7;
               f40 = f10;
               d3 = d0;
               d5 = d0 + 1.0D;
               d4 = d2 + (double)0.001F;
               d6 = d2 + (double)0.001F;
               direction = Direction.NORTH;
               flag8 = flag3;
            } else if (l == 1) {
               f38 = f9;
               f40 = f8;
               d3 = d0 + 1.0D;
               d5 = d0;
               d4 = d2 + 1.0D - (double)0.001F;
               d6 = d2 + 1.0D - (double)0.001F;
               direction = Direction.SOUTH;
               flag8 = flag4;
            } else if (l == 2) {
               f38 = f8;
               f40 = f7;
               d3 = d0 + (double)0.001F;
               d5 = d0 + (double)0.001F;
               d4 = d2 + 1.0D;
               d6 = d2;
               direction = Direction.WEST;
               flag8 = flag5;
            } else {
               f38 = f10;
               f40 = f9;
               d3 = d0 + 1.0D - (double)0.001F;
               d5 = d0 + 1.0D - (double)0.001F;
               d4 = d2;
               d6 = d2 + 1.0D;
               direction = Direction.EAST;
               flag8 = flag6;
            }

            if (flag8 && !isFaceOccludedByNeighbor(p_110955_, p_110956_, direction, Math.max(f38, f40))) {
               flag7 = true;
               BlockPos blockpos = p_110956_.relative(direction);
               TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
               if (!flag) {
                  Block block = p_110955_.getBlockState(blockpos).getBlock();
                  if (block instanceof HalfTransparentBlock || block instanceof LeavesBlock) {
                     textureatlassprite2 = this.waterOverlay;
                  }
               }

               float f49 = textureatlassprite2.getU(0.0D);
               float f50 = textureatlassprite2.getU(8.0D);
               float f28 = textureatlassprite2.getV((double)((1.0F - f38) * 16.0F * 0.5F));
               float f29 = textureatlassprite2.getV((double)((1.0F - f40) * 16.0F * 0.5F));
               float f30 = textureatlassprite2.getV(8.0D);
               float f31 = l < 2 ? f5 : f6;
               float f32 = f4 * f31 * f;
               float f33 = f4 * f31 * f1;
               float f34 = f4 * f31 * f2;
               this.vertex(p_110957_, d3, d1 + (double)f38, d4, f32, f33, f34, f49, f28, k);
               this.vertex(p_110957_, d5, d1 + (double)f40, d6, f32, f33, f34, f50, f29, k);
               this.vertex(p_110957_, d5, d1 + (double)f12, d6, f32, f33, f34, f50, f30, k);
               this.vertex(p_110957_, d3, d1 + (double)f12, d4, f32, f33, f34, f49, f30, k);
               if (textureatlassprite2 != this.waterOverlay) {
                  this.vertex(p_110957_, d3, d1 + (double)f12, d4, f32, f33, f34, f49, f30, k);
                  this.vertex(p_110957_, d5, d1 + (double)f12, d6, f32, f33, f34, f50, f30, k);
                  this.vertex(p_110957_, d5, d1 + (double)f40, d6, f32, f33, f34, f50, f29, k);
                  this.vertex(p_110957_, d3, d1 + (double)f38, d4, f32, f33, f34, f49, f28, k);
               }
            }
         }

         return flag7;
      }
   }

   private void vertex(VertexConsumer p_110985_, double p_110986_, double p_110987_, double p_110988_, float p_110989_, float p_110990_, float p_110991_, float p_110992_, float p_110993_, int p_110994_) {
      p_110985_.vertex(p_110986_, p_110987_, p_110988_).color(p_110989_, p_110990_, p_110991_, 1.0F).uv(p_110992_, p_110993_).uv2(p_110994_).normal(0.0F, 1.0F, 0.0F).endVertex();
   }

   private int getLightColor(BlockAndTintGetter p_110946_, BlockPos p_110947_) {
      int i = LevelRenderer.getLightColor(p_110946_, p_110947_);
      int j = LevelRenderer.getLightColor(p_110946_, p_110947_.above());
      int k = i & 255;
      int l = j & 255;
      int i1 = i >> 16 & 255;
      int j1 = j >> 16 & 255;
      return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
   }

   private float getWaterHeight(BlockGetter p_110965_, BlockPos p_110966_, Fluid p_110967_) {
      int i = 0;
      float f = 0.0F;

      for(int j = 0; j < 4; ++j) {
         BlockPos blockpos = p_110966_.offset(-(j & 1), 0, -(j >> 1 & 1));
         if (p_110965_.getFluidState(blockpos.above()).getType().isSame(p_110967_)) {
            return 1.0F;
         }

         FluidState fluidstate = p_110965_.getFluidState(blockpos);
         if (fluidstate.getType().isSame(p_110967_)) {
            float f1 = fluidstate.getHeight(p_110965_, blockpos);
            if (f1 >= 0.8F) {
               f += f1 * 10.0F;
               i += 10;
            } else {
               f += f1;
               ++i;
            }
         } else if (!p_110965_.getBlockState(blockpos).getMaterial().isSolid()) {
            ++i;
         }
      }

      return f / (float)i;
   }
}