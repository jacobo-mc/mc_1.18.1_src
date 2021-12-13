package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ListPoolElement extends StructurePoolElement {
   public static final Codec<ListPoolElement> CODEC = RecordCodecBuilder.create((p_69072_) -> {
      return p_69072_.group(StructurePoolElement.CODEC.listOf().fieldOf("elements").forGetter((p_161648_) -> {
         return p_161648_.elements;
      }), projectionCodec()).apply(p_69072_, ListPoolElement::new);
   });
   private final List<StructurePoolElement> elements;

   public ListPoolElement(List<StructurePoolElement> p_69061_, StructureTemplatePool.Projection p_69062_) {
      super(p_69062_);
      if (p_69061_.isEmpty()) {
         throw new IllegalArgumentException("Elements are empty");
      } else {
         this.elements = p_69061_;
         this.setProjectionOnEachElement(p_69062_);
      }
   }

   public Vec3i getSize(StructureManager p_161655_, Rotation p_161656_) {
      int i = 0;
      int j = 0;
      int k = 0;

      for(StructurePoolElement structurepoolelement : this.elements) {
         Vec3i vec3i = structurepoolelement.getSize(p_161655_, p_161656_);
         i = Math.max(i, vec3i.getX());
         j = Math.max(j, vec3i.getY());
         k = Math.max(k, vec3i.getZ());
      }

      return new Vec3i(i, j, k);
   }

   public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager p_69089_, BlockPos p_69090_, Rotation p_69091_, Random p_69092_) {
      return this.elements.get(0).getShuffledJigsawBlocks(p_69089_, p_69090_, p_69091_, p_69092_);
   }

   public BoundingBox getBoundingBox(StructureManager p_69085_, BlockPos p_69086_, Rotation p_69087_) {
      Stream<BoundingBox> stream = this.elements.stream().filter((p_161650_) -> {
         return p_161650_ != EmptyPoolElement.INSTANCE;
      }).map((p_161661_) -> {
         return p_161661_.getBoundingBox(p_69085_, p_69086_, p_69087_);
      });
      return BoundingBox.encapsulatingBoxes(stream::iterator).orElseThrow(() -> {
         return new IllegalStateException("Unable to calculate boundingbox for ListPoolElement");
      });
   }

   public boolean place(StructureManager p_69074_, WorldGenLevel p_69075_, StructureFeatureManager p_69076_, ChunkGenerator p_69077_, BlockPos p_69078_, BlockPos p_69079_, Rotation p_69080_, BoundingBox p_69081_, Random p_69082_, boolean p_69083_) {
      for(StructurePoolElement structurepoolelement : this.elements) {
         if (!structurepoolelement.place(p_69074_, p_69075_, p_69076_, p_69077_, p_69078_, p_69079_, p_69080_, p_69081_, p_69082_, p_69083_)) {
            return false;
         }
      }

      return true;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.LIST;
   }

   public StructurePoolElement setProjection(StructureTemplatePool.Projection p_69067_) {
      super.setProjection(p_69067_);
      this.setProjectionOnEachElement(p_69067_);
      return this;
   }

   public String toString() {
      return "List[" + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
   }

   private void setProjectionOnEachElement(StructureTemplatePool.Projection p_69094_) {
      this.elements.forEach((p_161653_) -> {
         p_161653_.setProjection(p_69094_);
      });
   }
}