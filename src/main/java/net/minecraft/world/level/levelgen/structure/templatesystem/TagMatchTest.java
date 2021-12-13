package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TagMatchTest extends RuleTest {
   public static final Codec<TagMatchTest> CODEC = Tag.codec(() -> {
      return SerializationTags.getInstance().getOrEmpty(Registry.BLOCK_REGISTRY);
   }).fieldOf("tag").xmap(TagMatchTest::new, (p_74700_) -> {
      return p_74700_.tag;
   }).codec();
   private final Tag<Block> tag;

   public TagMatchTest(Tag<Block> p_74694_) {
      this.tag = p_74694_;
   }

   public boolean test(BlockState p_74697_, Random p_74698_) {
      return p_74697_.is(this.tag);
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.TAG_TEST;
   }
}