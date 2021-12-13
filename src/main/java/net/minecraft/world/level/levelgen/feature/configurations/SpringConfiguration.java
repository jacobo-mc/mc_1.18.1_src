package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;

public class SpringConfiguration implements FeatureConfiguration {
   public static final Codec<SpringConfiguration> CODEC = RecordCodecBuilder.create((p_68139_) -> {
      return p_68139_.group(FluidState.CODEC.fieldOf("state").forGetter((p_161205_) -> {
         return p_161205_.state;
      }), Codec.BOOL.fieldOf("requires_block_below").orElse(true).forGetter((p_161203_) -> {
         return p_161203_.requiresBlockBelow;
      }), Codec.INT.fieldOf("rock_count").orElse(4).forGetter((p_161201_) -> {
         return p_161201_.rockCount;
      }), Codec.INT.fieldOf("hole_count").orElse(1).forGetter((p_161199_) -> {
         return p_161199_.holeCount;
      }), Registry.BLOCK.byNameCodec().listOf().fieldOf("valid_blocks").xmap(ImmutableSet::copyOf, ImmutableList::copyOf).forGetter((p_161197_) -> {
         return (ImmutableSet<Block>)p_161197_.validBlocks;
      })).apply(p_68139_, SpringConfiguration::new);
   });
   public final FluidState state;
   public final boolean requiresBlockBelow;
   public final int rockCount;
   public final int holeCount;
   public final Set<Block> validBlocks;

   public SpringConfiguration(FluidState p_68131_, boolean p_68132_, int p_68133_, int p_68134_, Set<Block> p_68135_) {
      this.state = p_68131_;
      this.requiresBlockBelow = p_68132_;
      this.rockCount = p_68133_;
      this.holeCount = p_68134_;
      this.validBlocks = p_68135_;
   }
}
