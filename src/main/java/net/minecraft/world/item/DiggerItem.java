package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DiggerItem extends TieredItem implements Vanishable {
   private final Tag<Block> blocks;
   protected final float speed;
   private final float attackDamageBaseline;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   protected DiggerItem(float p_150810_, float p_150811_, Tier p_150812_, Tag<Block> p_150813_, Item.Properties p_150814_) {
      super(p_150812_, p_150814_);
      this.blocks = p_150813_;
      this.speed = p_150812_.getSpeed();
      this.attackDamageBaseline = p_150810_ + p_150812_.getAttackDamageBonus();
      Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamageBaseline, AttributeModifier.Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)p_150811_, AttributeModifier.Operation.ADDITION));
      this.defaultModifiers = builder.build();
   }

   public float getDestroySpeed(ItemStack p_41004_, BlockState p_41005_) {
      return this.blocks.contains(p_41005_.getBlock()) ? this.speed : 1.0F;
   }

   public boolean hurtEnemy(ItemStack p_40994_, LivingEntity p_40995_, LivingEntity p_40996_) {
      p_40994_.hurtAndBreak(2, p_40996_, (p_41007_) -> {
         p_41007_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
      });
      return true;
   }

   public boolean mineBlock(ItemStack p_40998_, Level p_40999_, BlockState p_41000_, BlockPos p_41001_, LivingEntity p_41002_) {
      if (!p_40999_.isClientSide && p_41000_.getDestroySpeed(p_40999_, p_41001_) != 0.0F) {
         p_40998_.hurtAndBreak(1, p_41002_, (p_40992_) -> {
            p_40992_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
         });
      }

      return true;
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot p_40990_) {
      return p_40990_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_40990_);
   }

   public float getAttackDamage() {
      return this.attackDamageBaseline;
   }

   public boolean isCorrectToolForDrops(BlockState p_150816_) {
      int i = this.getTier().getLevel();
      if (i < 3 && p_150816_.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
         return false;
      } else if (i < 2 && p_150816_.is(BlockTags.NEEDS_IRON_TOOL)) {
         return false;
      } else {
         return i < 1 && p_150816_.is(BlockTags.NEEDS_STONE_TOOL) ? false : p_150816_.is(this.blocks);
      }
   }
}