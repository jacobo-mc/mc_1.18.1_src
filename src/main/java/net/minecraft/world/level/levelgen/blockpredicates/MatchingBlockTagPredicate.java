package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MatchingBlockTagPredicate extends StateTestingPredicate {
   final Tag<Block> tag;
   public static final Codec<MatchingBlockTagPredicate> CODEC = RecordCodecBuilder.create((p_198347_) -> {
      return stateTestingCodec(p_198347_).and(Tag.codec(() -> {
         return SerializationTags.getInstance().getOrEmpty(Registry.BLOCK_REGISTRY);
      }).fieldOf("tag").forGetter((p_198345_) -> {
         return p_198345_.tag;
      })).apply(p_198347_, MatchingBlockTagPredicate::new);
   });

   protected MatchingBlockTagPredicate(Vec3i p_198339_, Tag<Block> p_198340_) {
      super(p_198339_);
      this.tag = p_198340_;
   }

   protected boolean test(BlockState p_198343_) {
      return p_198343_.is(this.tag);
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.MATCHING_BLOCK_TAG;
   }
}