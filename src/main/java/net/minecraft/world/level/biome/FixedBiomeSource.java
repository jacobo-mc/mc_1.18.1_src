package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public class FixedBiomeSource extends BiomeSource implements BiomeManager.NoiseBiomeSource {
   public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC.fieldOf("biome").xmap(FixedBiomeSource::new, (p_48278_) -> {
      return p_48278_.biome;
   }).stable().codec();
   private final Supplier<Biome> biome;

   public FixedBiomeSource(Biome p_48255_) {
      this(() -> {
         return p_48255_;
      });
   }

   public FixedBiomeSource(Supplier<Biome> p_48257_) {
      super(ImmutableList.of(p_48257_.get()));
      this.biome = p_48257_;
   }

   protected Codec<? extends BiomeSource> codec() {
      return CODEC;
   }

   public BiomeSource withSeed(long p_48274_) {
      return this;
   }

   public Biome getNoiseBiome(int p_187044_, int p_187045_, int p_187046_, Climate.Sampler p_187047_) {
      return this.biome.get();
   }

   public Biome getNoiseBiome(int p_48280_, int p_48281_, int p_48282_) {
      return this.biome.get();
   }

   @Nullable
   public BlockPos findBiomeHorizontal(int p_187028_, int p_187029_, int p_187030_, int p_187031_, int p_187032_, Predicate<Biome> p_187033_, Random p_187034_, boolean p_187035_, Climate.Sampler p_187036_) {
      if (p_187033_.test(this.biome.get())) {
         return p_187035_ ? new BlockPos(p_187028_, p_187029_, p_187030_) : new BlockPos(p_187028_ - p_187031_ + p_187034_.nextInt(p_187031_ * 2 + 1), p_187029_, p_187030_ - p_187031_ + p_187034_.nextInt(p_187031_ * 2 + 1));
      } else {
         return null;
      }
   }

   public Set<Biome> getBiomesWithin(int p_187038_, int p_187039_, int p_187040_, int p_187041_, Climate.Sampler p_187042_) {
      return Sets.newHashSet(this.biome.get());
   }
}