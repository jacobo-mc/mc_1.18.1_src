package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

class MatchingBlocksPredicate extends StateTestingPredicate {
   private final List<Block> blocks;
   public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create((p_190491_) -> {
      return stateTestingCodec(p_190491_).and(Registry.BLOCK.byNameCodec().listOf().fieldOf("blocks").forGetter((p_190489_) -> {
         return p_190489_.blocks;
      })).apply(p_190491_, MatchingBlocksPredicate::new);
   });

   public MatchingBlocksPredicate(Vec3i p_190483_, List<Block> p_190484_) {
      super(p_190483_);
      this.blocks = p_190484_;
   }

   protected boolean test(BlockState p_190487_) {
      return this.blocks.contains(p_190487_.getBlock());
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.MATCHING_BLOCKS;
   }
}