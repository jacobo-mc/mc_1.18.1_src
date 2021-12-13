package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class CoralFeature extends Feature<NoneFeatureConfiguration> {
   public CoralFeature(Codec<NoneFeatureConfiguration> p_65429_) {
      super(p_65429_);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> p_159536_) {
      Random random = p_159536_.random();
      WorldGenLevel worldgenlevel = p_159536_.level();
      BlockPos blockpos = p_159536_.origin();
      BlockState blockstate = BlockTags.CORAL_BLOCKS.getRandomElement(random).defaultBlockState();
      return this.placeFeature(worldgenlevel, random, blockpos, blockstate);
   }

   protected abstract boolean placeFeature(LevelAccessor p_65430_, Random p_65431_, BlockPos p_65432_, BlockState p_65433_);

   protected boolean placeCoralBlock(LevelAccessor p_65447_, Random p_65448_, BlockPos p_65449_, BlockState p_65450_) {
      BlockPos blockpos = p_65449_.above();
      BlockState blockstate = p_65447_.getBlockState(p_65449_);
      if ((blockstate.is(Blocks.WATER) || blockstate.is(BlockTags.CORALS)) && p_65447_.getBlockState(blockpos).is(Blocks.WATER)) {
         p_65447_.setBlock(p_65449_, p_65450_, 3);
         if (p_65448_.nextFloat() < 0.25F) {
            p_65447_.setBlock(blockpos, BlockTags.CORALS.getRandomElement(p_65448_).defaultBlockState(), 2);
         } else if (p_65448_.nextFloat() < 0.05F) {
            p_65447_.setBlock(blockpos, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(p_65448_.nextInt(4) + 1)), 2);
         }

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (p_65448_.nextFloat() < 0.2F) {
               BlockPos blockpos1 = p_65449_.relative(direction);
               if (p_65447_.getBlockState(blockpos1).is(Blocks.WATER)) {
                  BlockState blockstate1 = BlockTags.WALL_CORALS.getRandomElement(p_65448_).defaultBlockState();
                  if (blockstate1.hasProperty(BaseCoralWallFanBlock.FACING)) {
                     blockstate1 = blockstate1.setValue(BaseCoralWallFanBlock.FACING, direction);
                  }

                  p_65447_.setBlock(blockpos1, blockstate1, 2);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}