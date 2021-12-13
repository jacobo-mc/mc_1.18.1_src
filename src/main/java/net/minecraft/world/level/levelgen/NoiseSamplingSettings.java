package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NoiseSamplingSettings {
   private static final Codec<Double> SCALE_RANGE = Codec.doubleRange(0.001D, 1000.0D);
   public static final Codec<NoiseSamplingSettings> CODEC = RecordCodecBuilder.create((p_64503_) -> {
      return p_64503_.group(SCALE_RANGE.fieldOf("xz_scale").forGetter(NoiseSamplingSettings::xzScale), SCALE_RANGE.fieldOf("y_scale").forGetter(NoiseSamplingSettings::yScale), SCALE_RANGE.fieldOf("xz_factor").forGetter(NoiseSamplingSettings::xzFactor), SCALE_RANGE.fieldOf("y_factor").forGetter(NoiseSamplingSettings::yFactor)).apply(p_64503_, NoiseSamplingSettings::new);
   });
   private final double xzScale;
   private final double yScale;
   private final double xzFactor;
   private final double yFactor;

   public NoiseSamplingSettings(double p_64497_, double p_64498_, double p_64499_, double p_64500_) {
      this.xzScale = p_64497_;
      this.yScale = p_64498_;
      this.xzFactor = p_64499_;
      this.yFactor = p_64500_;
   }

   public double xzScale() {
      return this.xzScale;
   }

   public double yScale() {
      return this.yScale;
   }

   public double xzFactor() {
      return this.xzFactor;
   }

   public double yFactor() {
      return this.yFactor;
   }
}