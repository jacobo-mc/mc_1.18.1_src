package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class StrongholdConfiguration {
   public static final Codec<StrongholdConfiguration> CODEC = RecordCodecBuilder.create((p_68159_) -> {
      return p_68159_.group(Codec.intRange(0, 1023).fieldOf("distance").forGetter(StrongholdConfiguration::distance), Codec.intRange(0, 1023).fieldOf("spread").forGetter(StrongholdConfiguration::spread), Codec.intRange(1, 4095).fieldOf("count").forGetter(StrongholdConfiguration::count)).apply(p_68159_, StrongholdConfiguration::new);
   });
   private final int distance;
   private final int spread;
   private final int count;

   public StrongholdConfiguration(int p_68154_, int p_68155_, int p_68156_) {
      this.distance = p_68154_;
      this.spread = p_68155_;
      this.count = p_68156_;
   }

   public int distance() {
      return this.distance;
   }

   public int spread() {
      return this.spread;
   }

   public int count() {
      return this.count;
   }
}