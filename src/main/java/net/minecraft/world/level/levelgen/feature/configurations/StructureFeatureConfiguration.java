package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public class StructureFeatureConfiguration {
   public static final Codec<StructureFeatureConfiguration> CODEC = RecordCodecBuilder.<StructureFeatureConfiguration>create((p_68175_) -> {
      return p_68175_.group(Codec.intRange(0, 4096).fieldOf("spacing").forGetter((p_161211_) -> {
         return p_161211_.spacing;
      }), Codec.intRange(0, 4096).fieldOf("separation").forGetter((p_161209_) -> {
         return p_161209_.separation;
      }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter((p_161207_) -> {
         return p_161207_.salt;
      })).apply(p_68175_, StructureFeatureConfiguration::new);
   }).comapFlatMap((p_68173_) -> {
      return p_68173_.spacing <= p_68173_.separation ? DataResult.error("Spacing has to be larger than separation") : DataResult.success(p_68173_);
   }, Function.identity());
   private final int spacing;
   private final int separation;
   private final int salt;

   public StructureFeatureConfiguration(int p_68168_, int p_68169_, int p_68170_) {
      this.spacing = p_68168_;
      this.separation = p_68169_;
      this.salt = p_68170_;
   }

   public int spacing() {
      return this.spacing;
   }

   public int separation() {
      return this.separation;
   }

   public int salt() {
      return this.salt;
   }
}
