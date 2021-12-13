package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SpruceTreeGrower extends AbstractMegaTreeGrower {
   protected ConfiguredFeature<?, ?> getConfiguredFeature(Random p_60044_, boolean p_60045_) {
      return TreeFeatures.SPRUCE;
   }

   protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random p_60042_) {
      return p_60042_.nextBoolean() ? TreeFeatures.MEGA_SPRUCE : TreeFeatures.MEGA_PINE;
   }
}