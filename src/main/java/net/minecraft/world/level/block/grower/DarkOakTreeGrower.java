package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower {
   @Nullable
   protected ConfiguredFeature<?, ?> getConfiguredFeature(Random p_60028_, boolean p_60029_) {
      return null;
   }

   @Nullable
   protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random p_60026_) {
      return TreeFeatures.DARK_OAK;
   }
}