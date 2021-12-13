package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class FeaturePoolElement extends StructurePoolElement {
   public static final Codec<FeaturePoolElement> CODEC = RecordCodecBuilder.create((p_68893_) -> {
      return p_68893_.group(PlacedFeature.CODEC.fieldOf("feature").forGetter((p_161605_) -> {
         return p_161605_.feature;
      }), projectionCodec()).apply(p_68893_, FeaturePoolElement::new);
   });
   private final Supplier<PlacedFeature> feature;
   private final CompoundTag defaultJigsawNBT;

   protected FeaturePoolElement(Supplier<PlacedFeature> p_68887_, StructureTemplatePool.Projection p_68888_) {
      super(p_68888_);
      this.feature = p_68887_;
      this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
   }

   private CompoundTag fillDefaultJigsawNBT() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("name", "minecraft:bottom");
      compoundtag.putString("final_state", "minecraft:air");
      compoundtag.putString("pool", "minecraft:empty");
      compoundtag.putString("target", "minecraft:empty");
      compoundtag.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
      return compoundtag;
   }

   public Vec3i getSize(StructureManager p_161607_, Rotation p_161608_) {
      return Vec3i.ZERO;
   }

   public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager p_68913_, BlockPos p_68914_, Rotation p_68915_, Random p_68916_) {
      List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
      list.add(new StructureTemplate.StructureBlockInfo(p_68914_, Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)), this.defaultJigsawNBT));
      return list;
   }

   public BoundingBox getBoundingBox(StructureManager p_68909_, BlockPos p_68910_, Rotation p_68911_) {
      Vec3i vec3i = this.getSize(p_68909_, p_68911_);
      return new BoundingBox(p_68910_.getX(), p_68910_.getY(), p_68910_.getZ(), p_68910_.getX() + vec3i.getX(), p_68910_.getY() + vec3i.getY(), p_68910_.getZ() + vec3i.getZ());
   }

   public boolean place(StructureManager p_68895_, WorldGenLevel p_68896_, StructureFeatureManager p_68897_, ChunkGenerator p_68898_, BlockPos p_68899_, BlockPos p_68900_, Rotation p_68901_, BoundingBox p_68902_, Random p_68903_, boolean p_68904_) {
      return this.feature.get().place(p_68896_, p_68898_, p_68903_, p_68899_);
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.FEATURE;
   }

   public String toString() {
      return "Feature[" + this.feature.get() + "]";
   }
}