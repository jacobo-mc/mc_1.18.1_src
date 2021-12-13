package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class FossilFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<FossilFeatureConfiguration> CODEC = RecordCodecBuilder.create((p_159816_) -> {
      return p_159816_.group(ResourceLocation.CODEC.listOf().fieldOf("fossil_structures").forGetter((p_159830_) -> {
         return p_159830_.fossilStructures;
      }), ResourceLocation.CODEC.listOf().fieldOf("overlay_structures").forGetter((p_159828_) -> {
         return p_159828_.overlayStructures;
      }), StructureProcessorType.LIST_CODEC.fieldOf("fossil_processors").forGetter((p_159826_) -> {
         return p_159826_.fossilProcessors;
      }), StructureProcessorType.LIST_CODEC.fieldOf("overlay_processors").forGetter((p_159822_) -> {
         return p_159822_.overlayProcessors;
      }), Codec.intRange(0, 7).fieldOf("max_empty_corners_allowed").forGetter((p_159818_) -> {
         return p_159818_.maxEmptyCornersAllowed;
      })).apply(p_159816_, FossilFeatureConfiguration::new);
   });
   public final List<ResourceLocation> fossilStructures;
   public final List<ResourceLocation> overlayStructures;
   public final Supplier<StructureProcessorList> fossilProcessors;
   public final Supplier<StructureProcessorList> overlayProcessors;
   public final int maxEmptyCornersAllowed;

   public FossilFeatureConfiguration(List<ResourceLocation> p_159810_, List<ResourceLocation> p_159811_, Supplier<StructureProcessorList> p_159812_, Supplier<StructureProcessorList> p_159813_, int p_159814_) {
      if (p_159810_.isEmpty()) {
         throw new IllegalArgumentException("Fossil structure lists need at least one entry");
      } else if (p_159810_.size() != p_159811_.size()) {
         throw new IllegalArgumentException("Fossil structure lists must be equal lengths");
      } else {
         this.fossilStructures = p_159810_;
         this.overlayStructures = p_159811_;
         this.fossilProcessors = p_159812_;
         this.overlayProcessors = p_159813_;
         this.maxEmptyCornersAllowed = p_159814_;
      }
   }

   public FossilFeatureConfiguration(List<ResourceLocation> p_159804_, List<ResourceLocation> p_159805_, StructureProcessorList p_159806_, StructureProcessorList p_159807_, int p_159808_) {
      this(p_159804_, p_159805_, () -> {
         return p_159806_;
      }, () -> {
         return p_159807_;
      }, p_159808_);
   }
}