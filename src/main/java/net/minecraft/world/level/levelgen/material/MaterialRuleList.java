package net.minecraft.world.level.levelgen.material;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;

public class MaterialRuleList implements WorldGenMaterialRule {
   private final List<WorldGenMaterialRule> materialRuleList;

   public MaterialRuleList(List<WorldGenMaterialRule> p_191547_) {
      this.materialRuleList = p_191547_;
   }

   @Nullable
   public BlockState apply(NoiseChunk p_191549_, int p_191550_, int p_191551_, int p_191552_) {
      for(WorldGenMaterialRule worldgenmaterialrule : this.materialRuleList) {
         BlockState blockstate = worldgenmaterialrule.apply(p_191549_, p_191550_, p_191551_, p_191552_);
         if (blockstate != null) {
            return blockstate;
         }
      }

      return null;
   }
}