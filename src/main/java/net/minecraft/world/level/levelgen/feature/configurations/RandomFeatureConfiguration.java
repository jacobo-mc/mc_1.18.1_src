package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<RandomFeatureConfiguration> CODEC = RecordCodecBuilder.create((p_67898_) -> {
      return p_67898_.apply2(RandomFeatureConfiguration::new, WeightedPlacedFeature.CODEC.listOf().fieldOf("features").forGetter((p_161053_) -> {
         return p_161053_.features;
      }), PlacedFeature.CODEC.fieldOf("default").forGetter((p_161051_) -> {
         return p_161051_.defaultFeature;
      }));
   });
   public final List<WeightedPlacedFeature> features;
   public final Supplier<PlacedFeature> defaultFeature;

   public RandomFeatureConfiguration(List<WeightedPlacedFeature> p_191296_, PlacedFeature p_191297_) {
      this(p_191296_, () -> {
         return p_191297_;
      });
   }

   private RandomFeatureConfiguration(List<WeightedPlacedFeature> p_67889_, Supplier<PlacedFeature> p_67890_) {
      this.features = p_67889_;
      this.defaultFeature = p_67890_;
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return Stream.concat(this.features.stream().flatMap((p_191299_) -> {
         return p_191299_.feature.get().getFeatures();
      }), this.defaultFeature.get().getFeatures());
   }
}