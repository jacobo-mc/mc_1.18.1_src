package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WeightedPlacedFeature {
   public static final Codec<WeightedPlacedFeature> CODEC = RecordCodecBuilder.create((p_191187_) -> {
      return p_191187_.group(PlacedFeature.CODEC.fieldOf("feature").flatXmap(ExtraCodecs.nonNullSupplierCheck(), ExtraCodecs.nonNullSupplierCheck()).forGetter((p_191193_) -> {
         return p_191193_.feature;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter((p_191189_) -> {
         return p_191189_.chance;
      })).apply(p_191187_, WeightedPlacedFeature::new);
   });
   public final Supplier<PlacedFeature> feature;
   public final float chance;

   public WeightedPlacedFeature(PlacedFeature p_191176_, float p_191177_) {
      this(() -> {
         return p_191176_;
      }, p_191177_);
   }

   private WeightedPlacedFeature(Supplier<PlacedFeature> p_191179_, float p_191180_) {
      this.feature = p_191179_;
      this.chance = p_191180_;
   }

   public boolean place(WorldGenLevel p_191182_, ChunkGenerator p_191183_, Random p_191184_, BlockPos p_191185_) {
      return this.feature.get().place(p_191182_, p_191183_, p_191184_, p_191185_);
   }
}