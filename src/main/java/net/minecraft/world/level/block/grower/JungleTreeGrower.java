package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class JungleTreeGrower extends AbstractMegaTreeGrower {
   protected ConfiguredFeature<?, ?> getConfiguredFeature(Random p_60034_, boolean p_60035_) {
      return TreeFeatures.JUNGLE_TREE_NO_VINE;
   }

   protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random p_60032_) {
      return TreeFeatures.MEGA_JUNGLE_TREE;
   }
}