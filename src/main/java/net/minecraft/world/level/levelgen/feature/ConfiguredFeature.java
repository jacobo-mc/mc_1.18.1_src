package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfiguredFeature<FC extends FeatureConfiguration, F extends Feature<FC>> {
   public static final Codec<ConfiguredFeature<?, ?>> DIRECT_CODEC = Registry.FEATURE.byNameCodec().dispatch((p_65391_) -> {
      return p_65391_.feature;
   }, Feature::configuredCodec);
   public static final Codec<Supplier<ConfiguredFeature<?, ?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Codec<List<Supplier<ConfiguredFeature<?, ?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Logger LOGGER = LogManager.getLogger();
   public final F feature;
   public final FC config;

   public ConfiguredFeature(F p_65381_, FC p_65382_) {
      this.feature = p_65381_;
      this.config = p_65382_;
   }

   public F feature() {
      return this.feature;
   }

   public FC config() {
      return this.config;
   }

   public PlacedFeature placed(List<PlacementModifier> p_190822_) {
      return new PlacedFeature(() -> {
         return this;
      }, p_190822_);
   }

   public PlacedFeature placed(PlacementModifier... p_190824_) {
      return this.placed(List.of(p_190824_));
   }

   public PlacedFeature filteredByBlockSurvival(Block p_190818_) {
      return this.filtered(BlockPredicate.wouldSurvive(p_190818_.defaultBlockState(), BlockPos.ZERO));
   }

   public PlacedFeature onlyWhenEmpty() {
      return this.filtered(BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO));
   }

   public PlacedFeature filtered(BlockPredicate p_190820_) {
      return this.placed(BlockPredicateFilter.forPredicate(p_190820_));
   }

   public boolean place(WorldGenLevel p_65386_, ChunkGenerator p_65387_, Random p_65388_, BlockPos p_65389_) {
      return p_65386_.ensureCanWrite(p_65389_) ? this.feature.place(new FeaturePlaceContext<>(Optional.empty(), p_65386_, p_65387_, p_65388_, p_65389_, this.config)) : false;
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return Stream.concat(Stream.of(this), this.config.getFeatures());
   }

   public String toString() {
      return BuiltinRegistries.CONFIGURED_FEATURE.getResourceKey(this).map(Objects::toString).orElseGet(() -> {
         return DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, this).toString();
      });
   }
}