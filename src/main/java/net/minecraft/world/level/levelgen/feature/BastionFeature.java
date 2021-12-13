package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class BastionFeature extends JigsawFeature {
   private static final int BASTION_SPAWN_HEIGHT = 33;

   public BastionFeature(Codec<JigsawConfiguration> p_65226_) {
      super(p_65226_, 33, false, false, BastionFeature::checkLocation);
   }

   private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> p_197071_) {
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureSeed(p_197071_.seed(), p_197071_.chunkPos().x, p_197071_.chunkPos().z);
      return worldgenrandom.nextInt(5) >= 2;
   }
}