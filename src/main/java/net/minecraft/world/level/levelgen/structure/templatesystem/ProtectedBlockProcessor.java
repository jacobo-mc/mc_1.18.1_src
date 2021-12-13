package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ProtectedBlockProcessor extends StructureProcessor {
   public final ResourceLocation cannotReplace;
   public static final Codec<ProtectedBlockProcessor> CODEC = ResourceLocation.CODEC.xmap(ProtectedBlockProcessor::new, (p_163762_) -> {
      return p_163762_.cannotReplace;
   });

   public ProtectedBlockProcessor(ResourceLocation p_163752_) {
      this.cannotReplace = p_163752_;
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader p_163755_, BlockPos p_163756_, BlockPos p_163757_, StructureTemplate.StructureBlockInfo p_163758_, StructureTemplate.StructureBlockInfo p_163759_, StructurePlaceSettings p_163760_) {
      return Feature.isReplaceable(this.cannotReplace).test(p_163755_.getBlockState(p_163759_.pos)) ? p_163759_ : null;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.PROTECTED_BLOCKS;
   }
}