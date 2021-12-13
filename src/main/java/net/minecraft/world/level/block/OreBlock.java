package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class OreBlock extends Block {
   private final UniformInt xpRange;

   public OreBlock(BlockBehaviour.Properties p_55140_) {
      this(p_55140_, UniformInt.of(0, 0));
   }

   public OreBlock(BlockBehaviour.Properties p_153992_, UniformInt p_153993_) {
      super(p_153992_);
      this.xpRange = p_153993_;
   }

   public void spawnAfterBreak(BlockState p_55142_, ServerLevel p_55143_, BlockPos p_55144_, ItemStack p_55145_) {
      super.spawnAfterBreak(p_55142_, p_55143_, p_55144_, p_55145_);
      if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, p_55145_) == 0) {
         int i = this.xpRange.sample(p_55143_.random);
         if (i > 0) {
            this.popExperience(p_55143_, p_55144_, i);
         }
      }

   }
}