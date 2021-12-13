package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ExtraCodecs.nonEmptyList(PlacedFeature.LIST_CODEC).fieldOf("features").xmap(SimpleRandomFeatureConfiguration::new, (p_68095_) -> {
      return p_68095_.features;
   }).codec();
   public final List<Supplier<PlacedFeature>> features;

   public SimpleRandomFeatureConfiguration(List<Supplier<PlacedFeature>> p_68093_) {
      this.features = p_68093_;
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return this.features.stream().flatMap((p_68097_) -> {
         return p_68097_.get().getFeatures();
      });
   }
}