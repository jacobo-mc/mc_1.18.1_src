package net.minecraft.data.worldgen.features;

import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
   public static ConfiguredFeature<?, ?> bootstrap() {
      ConfiguredFeature<?, ?>[] configuredfeature = new ConfiguredFeature[]{AquaticFeatures.KELP, CaveFeatures.MOSS_PATCH_BONEMEAL, EndFeatures.CHORUS_PLANT, MiscOverworldFeatures.SPRING_LAVA_OVERWORLD, NetherFeatures.BASALT_BLOBS, OreFeatures.ORE_ANCIENT_DEBRIS_LARGE, PileFeatures.PILE_HAY, TreeFeatures.AZALEA_TREE, VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA};
      return Util.getRandom(configuredfeature, new Random());
   }

   private static BlockPredicate simplePatchPredicate(List<Block> p_195009_) {
      BlockPredicate blockpredicate;
      if (!p_195009_.isEmpty()) {
         blockpredicate = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(p_195009_, new BlockPos(0, -1, 0)));
      } else {
         blockpredicate = BlockPredicate.ONLY_IN_AIR_PREDICATE;
      }

      return blockpredicate;
   }

   public static RandomPatchConfiguration simpleRandomPatchConfiguration(int p_194992_, PlacedFeature p_194993_) {
      return new RandomPatchConfiguration(p_194992_, 7, 3, () -> {
         return p_194993_;
      });
   }

   public static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> p_195000_, List<Block> p_195001_, int p_195002_) {
      return simpleRandomPatchConfiguration(p_195002_, p_195000_.filtered(simplePatchPredicate(p_195001_)));
   }

   public static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> p_194997_, List<Block> p_194998_) {
      return simplePatchConfiguration(p_194997_, p_194998_, 96);
   }

   public static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> p_194995_) {
      return simplePatchConfiguration(p_194995_, List.of(), 96);
   }

   public static <FC extends FeatureConfiguration> ConfiguredFeature<FC, ?> register(String p_195006_, ConfiguredFeature<FC, ?> p_195007_) {
      return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, p_195006_, p_195007_);
   }
}