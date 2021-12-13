package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

public interface CollisionContext {
   static CollisionContext empty() {
      return EntityCollisionContext.EMPTY;
   }

   static CollisionContext of(Entity p_82751_) {
      return new EntityCollisionContext(p_82751_);
   }

   boolean isDescending();

   boolean isAbove(VoxelShape p_82755_, BlockPos p_82756_, boolean p_82757_);

   boolean isHoldingItem(Item p_82752_);

   boolean canStandOnFluid(FluidState p_82753_, FlowingFluid p_82754_);
}