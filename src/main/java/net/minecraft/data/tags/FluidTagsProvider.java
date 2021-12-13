package net.minecraft.data.tags;

import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTagsProvider extends TagsProvider<Fluid> {
   public FluidTagsProvider(DataGenerator p_126523_) {
      super(p_126523_, Registry.FLUID);
   }

   protected void addTags() {
      this.tag(FluidTags.WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
      this.tag(FluidTags.LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
   }

   protected Path getPath(ResourceLocation p_126526_) {
      return this.generator.getOutputFolder().resolve("data/" + p_126526_.getNamespace() + "/tags/fluids/" + p_126526_.getPath() + ".json");
   }

   public String getName() {
      return "Fluid Tags";
   }
}