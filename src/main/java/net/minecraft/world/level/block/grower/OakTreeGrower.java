package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class OakTreeGrower extends AbstractTreeGrower {
   protected ConfiguredFeature<?, ?> getConfiguredFeature(Random p_60038_, boolean p_60039_) {
      if (p_60038_.nextInt(10) == 0) {
         return p_60039_ ? TreeFeatures.FANCY_OAK_BEES_005 : TreeFeatures.FANCY_OAK;
      } else {
         return p_60039_ ? TreeFeatures.OAK_BEES_005 : TreeFeatures.OAK;
      }
   }
}