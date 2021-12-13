package net.minecraft.world.level.levelgen.structure.templatesystem;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

public abstract class StructureProcessor {
   @Nullable
   public abstract StructureTemplate.StructureBlockInfo processBlock(LevelReader p_74416_, BlockPos p_74417_, BlockPos p_74418_, StructureTemplate.StructureBlockInfo p_74419_, StructureTemplate.StructureBlockInfo p_74420_, StructurePlaceSettings p_74421_);

   protected abstract StructureProcessorType<?> getType();
}