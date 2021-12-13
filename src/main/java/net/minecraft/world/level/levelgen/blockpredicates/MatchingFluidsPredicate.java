package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate extends StateTestingPredicate {
   private final List<Fluid> fluids;
   public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create((p_190504_) -> {
      return stateTestingCodec(p_190504_).and(Registry.FLUID.byNameCodec().listOf().fieldOf("fluids").forGetter((p_190502_) -> {
         return p_190502_.fluids;
      })).apply(p_190504_, MatchingFluidsPredicate::new);
   });

   public MatchingFluidsPredicate(Vec3i p_190496_, List<Fluid> p_190497_) {
      super(p_190496_);
      this.fluids = p_190497_;
   }

   protected boolean test(BlockState p_190500_) {
      return this.fluids.contains(p_190500_.getFluidState().getType());
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.MATCHING_FLUIDS;
   }
}