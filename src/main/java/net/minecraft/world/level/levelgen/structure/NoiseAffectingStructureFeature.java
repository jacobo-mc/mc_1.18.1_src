package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public abstract class NoiseAffectingStructureFeature<C extends FeatureConfiguration> extends StructureFeature<C> {
   public NoiseAffectingStructureFeature(Codec<C> p_197226_, PieceGeneratorSupplier<C> p_197227_) {
      super(p_197226_, p_197227_);
   }

   public NoiseAffectingStructureFeature(Codec<C> p_197229_, PieceGeneratorSupplier<C> p_197230_, PostPlacementProcessor p_197231_) {
      super(p_197229_, p_197230_, p_197231_);
   }

   public BoundingBox adjustBoundingBox(BoundingBox p_192263_) {
      return super.adjustBoundingBox(p_192263_).inflatedBy(12);
   }
}