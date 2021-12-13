package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantmentTableBlock extends BaseEntityBlock {
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

   protected EnchantmentTableBlock(BlockBehaviour.Properties p_52953_) {
      super(p_52953_);
   }

   public boolean useShapeForLightOcclusion(BlockState p_52997_) {
      return true;
   }

   public VoxelShape getShape(BlockState p_52988_, BlockGetter p_52989_, BlockPos p_52990_, CollisionContext p_52991_) {
      return SHAPE;
   }

   public void animateTick(BlockState p_52981_, Level p_52982_, BlockPos p_52983_, Random p_52984_) {
      super.animateTick(p_52981_, p_52982_, p_52983_, p_52984_);

      for(int i = -2; i <= 2; ++i) {
         for(int j = -2; j <= 2; ++j) {
            if (i > -2 && i < 2 && j == -1) {
               j = 2;
            }

            if (p_52984_.nextInt(16) == 0) {
               for(int k = 0; k <= 1; ++k) {
                  BlockPos blockpos = p_52983_.offset(i, k, j);
                  if (p_52982_.getBlockState(blockpos).is(Blocks.BOOKSHELF)) {
                     if (!p_52982_.isEmptyBlock(p_52983_.offset(i / 2, 0, j / 2))) {
                        break;
                     }

                     p_52982_.addParticle(ParticleTypes.ENCHANT, (double)p_52983_.getX() + 0.5D, (double)p_52983_.getY() + 2.0D, (double)p_52983_.getZ() + 0.5D, (double)((float)i + p_52984_.nextFloat()) - 0.5D, (double)((float)k - p_52984_.nextFloat() - 1.0F), (double)((float)j + p_52984_.nextFloat()) - 0.5D);
                  }
               }
            }
         }
      }

   }

   public RenderShape getRenderShape(BlockState p_52986_) {
      return RenderShape.MODEL;
   }

   public BlockEntity newBlockEntity(BlockPos p_153186_, BlockState p_153187_) {
      return new EnchantmentTableBlockEntity(p_153186_, p_153187_);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153182_, BlockState p_153183_, BlockEntityType<T> p_153184_) {
      return p_153182_.isClientSide ? createTickerHelper(p_153184_, BlockEntityType.ENCHANTING_TABLE, EnchantmentTableBlockEntity::bookAnimationTick) : null;
   }

   public InteractionResult use(BlockState p_52974_, Level p_52975_, BlockPos p_52976_, Player p_52977_, InteractionHand p_52978_, BlockHitResult p_52979_) {
      if (p_52975_.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         p_52977_.openMenu(p_52974_.getMenuProvider(p_52975_, p_52976_));
         return InteractionResult.CONSUME;
      }
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState p_52993_, Level p_52994_, BlockPos p_52995_) {
      BlockEntity blockentity = p_52994_.getBlockEntity(p_52995_);
      if (blockentity instanceof EnchantmentTableBlockEntity) {
         Component component = ((Nameable)blockentity).getDisplayName();
         return new SimpleMenuProvider((p_52959_, p_52960_, p_52961_) -> {
            return new EnchantmentMenu(p_52959_, p_52960_, ContainerLevelAccess.create(p_52994_, p_52995_));
         }, component);
      } else {
         return null;
      }
   }

   public void setPlacedBy(Level p_52963_, BlockPos p_52964_, BlockState p_52965_, LivingEntity p_52966_, ItemStack p_52967_) {
      if (p_52967_.hasCustomHoverName()) {
         BlockEntity blockentity = p_52963_.getBlockEntity(p_52964_);
         if (blockentity instanceof EnchantmentTableBlockEntity) {
            ((EnchantmentTableBlockEntity)blockentity).setCustomName(p_52967_.getHoverName());
         }
      }

   }

   public boolean isPathfindable(BlockState p_52969_, BlockGetter p_52970_, BlockPos p_52971_, PathComputationType p_52972_) {
      return false;
   }
}