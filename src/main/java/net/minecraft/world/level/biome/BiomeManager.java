package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;

public class BiomeManager {
   public static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
   private static final int ZOOM_BITS = 2;
   private static final int ZOOM = 4;
   private static final int ZOOM_MASK = 3;
   private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
   private final long biomeZoomSeed;

   public BiomeManager(BiomeManager.NoiseBiomeSource p_186677_, long p_186678_) {
      this.noiseBiomeSource = p_186677_;
      this.biomeZoomSeed = p_186678_;
   }

   public static long obfuscateSeed(long p_47878_) {
      return Hashing.sha256().hashLong(p_47878_).asLong();
   }

   public BiomeManager withDifferentSource(BiomeManager.NoiseBiomeSource p_186688_) {
      return new BiomeManager(p_186688_, this.biomeZoomSeed);
   }

   public Biome getBiome(BlockPos p_47882_) {
      int i = p_47882_.getX() - 2;
      int j = p_47882_.getY() - 2;
      int k = p_47882_.getZ() - 2;
      int l = i >> 2;
      int i1 = j >> 2;
      int j1 = k >> 2;
      double d0 = (double)(i & 3) / 4.0D;
      double d1 = (double)(j & 3) / 4.0D;
      double d2 = (double)(k & 3) / 4.0D;
      int k1 = 0;
      double d3 = Double.POSITIVE_INFINITY;

      for(int l1 = 0; l1 < 8; ++l1) {
         boolean flag = (l1 & 4) == 0;
         boolean flag1 = (l1 & 2) == 0;
         boolean flag2 = (l1 & 1) == 0;
         int i2 = flag ? l : l + 1;
         int j2 = flag1 ? i1 : i1 + 1;
         int k2 = flag2 ? j1 : j1 + 1;
         double d4 = flag ? d0 : d0 - 1.0D;
         double d5 = flag1 ? d1 : d1 - 1.0D;
         double d6 = flag2 ? d2 : d2 - 1.0D;
         double d7 = getFiddledDistance(this.biomeZoomSeed, i2, j2, k2, d4, d5, d6);
         if (d3 > d7) {
            k1 = l1;
            d3 = d7;
         }
      }

      int l2 = (k1 & 4) == 0 ? l : l + 1;
      int i3 = (k1 & 2) == 0 ? i1 : i1 + 1;
      int j3 = (k1 & 1) == 0 ? j1 : j1 + 1;
      return this.noiseBiomeSource.getNoiseBiome(l2, i3, j3);
   }

   public Biome getNoiseBiomeAtPosition(double p_47870_, double p_47871_, double p_47872_) {
      int i = QuartPos.fromBlock(Mth.floor(p_47870_));
      int j = QuartPos.fromBlock(Mth.floor(p_47871_));
      int k = QuartPos.fromBlock(Mth.floor(p_47872_));
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   public Biome getNoiseBiomeAtPosition(BlockPos p_47884_) {
      int i = QuartPos.fromBlock(p_47884_.getX());
      int j = QuartPos.fromBlock(p_47884_.getY());
      int k = QuartPos.fromBlock(p_47884_.getZ());
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   public Biome getNoiseBiomeAtQuart(int p_47874_, int p_47875_, int p_47876_) {
      return this.noiseBiomeSource.getNoiseBiome(p_47874_, p_47875_, p_47876_);
   }

   private static double getFiddledDistance(long p_186680_, int p_186681_, int p_186682_, int p_186683_, double p_186684_, double p_186685_, double p_186686_) {
      long $$7 = LinearCongruentialGenerator.next(p_186680_, (long)p_186681_);
      $$7 = LinearCongruentialGenerator.next($$7, (long)p_186682_);
      $$7 = LinearCongruentialGenerator.next($$7, (long)p_186683_);
      $$7 = LinearCongruentialGenerator.next($$7, (long)p_186681_);
      $$7 = LinearCongruentialGenerator.next($$7, (long)p_186682_);
      $$7 = LinearCongruentialGenerator.next($$7, (long)p_186683_);
      double d0 = getFiddle($$7);
      $$7 = LinearCongruentialGenerator.next($$7, p_186680_);
      double d1 = getFiddle($$7);
      $$7 = LinearCongruentialGenerator.next($$7, p_186680_);
      double d2 = getFiddle($$7);
      return Mth.square(p_186686_ + d2) + Mth.square(p_186685_ + d1) + Mth.square(p_186684_ + d0);
   }

   private static double getFiddle(long p_186690_) {
      double d0 = (double)Math.floorMod(p_186690_ >> 24, 1024) / 1024.0D;
      return (d0 - 0.5D) * 0.9D;
   }

   public interface NoiseBiomeSource {
      Biome getNoiseBiome(int p_47885_, int p_47886_, int p_47887_);
   }
}