package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public class NoiseSlider {
   public static final Codec<NoiseSlider> CODEC = RecordCodecBuilder.create((p_189236_) -> {
      return p_189236_.group(Codec.DOUBLE.fieldOf("target").forGetter((p_189242_) -> {
         return p_189242_.target;
      }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("size").forGetter((p_189240_) -> {
         return p_189240_.size;
      }), Codec.INT.fieldOf("offset").forGetter((p_189238_) -> {
         return p_189238_.offset;
      })).apply(p_189236_, NoiseSlider::new);
   });
   private final double target;
   private final int size;
   private final int offset;

   public NoiseSlider(double p_189229_, int p_189230_, int p_189231_) {
      this.target = p_189229_;
      this.size = p_189230_;
      this.offset = p_189231_;
   }

   public double applySlide(double p_189233_, int p_189234_) {
      if (this.size <= 0) {
         return p_189233_;
      } else {
         double d0 = (double)(p_189234_ - this.offset) / (double)this.size;
         return Mth.clampedLerp(this.target, p_189233_, d0);
      }
   }
}