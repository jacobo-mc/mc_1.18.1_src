package net.minecraft.world.level;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface WorldGenLevel extends ServerLevelAccessor {
   long getSeed();

   List<? extends StructureStart<?>> startsForFeature(SectionPos p_186616_, StructureFeature<?> p_186617_);

   default boolean ensureCanWrite(BlockPos p_181157_) {
      return true;
   }

   default void setCurrentlyGenerating(@Nullable Supplier<String> p_186618_) {
   }
}