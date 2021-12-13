package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class LegacySinglePoolElement extends SinglePoolElement {
   public static final Codec<LegacySinglePoolElement> CODEC = RecordCodecBuilder.create((p_69055_) -> {
      return p_69055_.group(templateCodec(), processorsCodec(), projectionCodec()).apply(p_69055_, LegacySinglePoolElement::new);
   });

   protected LegacySinglePoolElement(Either<ResourceLocation, StructureTemplate> p_69046_, Supplier<StructureProcessorList> p_69047_, StructureTemplatePool.Projection p_69048_) {
      super(p_69046_, p_69047_, p_69048_);
   }

   protected StructurePlaceSettings getSettings(Rotation p_69051_, BoundingBox p_69052_, boolean p_69053_) {
      StructurePlaceSettings structureplacesettings = super.getSettings(p_69051_, p_69052_, p_69053_);
      structureplacesettings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      structureplacesettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
      return structureplacesettings;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.LEGACY;
   }

   public String toString() {
      return "LegacySingle[" + this.template + "]";
   }
}