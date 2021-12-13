package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class JigsawConfiguration implements FeatureConfiguration {
   public static final Codec<JigsawConfiguration> CODEC = RecordCodecBuilder.create((p_67764_) -> {
      return p_67764_.group(StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(JigsawConfiguration::startPool), Codec.intRange(0, 7).fieldOf("size").forGetter(JigsawConfiguration::maxDepth)).apply(p_67764_, JigsawConfiguration::new);
   });
   private final Supplier<StructureTemplatePool> startPool;
   private final int maxDepth;

   public JigsawConfiguration(Supplier<StructureTemplatePool> p_67761_, int p_67762_) {
      this.startPool = p_67761_;
      this.maxDepth = p_67762_;
   }

   public int maxDepth() {
      return this.maxDepth;
   }

   public Supplier<StructureTemplatePool> startPool() {
      return this.startPool;
   }
}