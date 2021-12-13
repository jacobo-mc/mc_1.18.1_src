package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchConfiguration implements FeatureConfiguration {
   public static final Codec<VegetationPatchConfiguration> CODEC = RecordCodecBuilder.create((p_161304_) -> {
      return p_161304_.group(ResourceLocation.CODEC.fieldOf("replaceable").forGetter((p_161324_) -> {
         return p_161324_.replaceable;
      }), BlockStateProvider.CODEC.fieldOf("ground_state").forGetter((p_161322_) -> {
         return p_161322_.groundState;
      }), PlacedFeature.CODEC.fieldOf("vegetation_feature").forGetter((p_161320_) -> {
         return p_161320_.vegetationFeature;
      }), CaveSurface.CODEC.fieldOf("surface").forGetter((p_161318_) -> {
         return p_161318_.surface;
      }), IntProvider.codec(1, 128).fieldOf("depth").forGetter((p_161316_) -> {
         return p_161316_.depth;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("extra_bottom_block_chance").forGetter((p_161314_) -> {
         return p_161314_.extraBottomBlockChance;
      }), Codec.intRange(1, 256).fieldOf("vertical_range").forGetter((p_161312_) -> {
         return p_161312_.verticalRange;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("vegetation_chance").forGetter((p_161310_) -> {
         return p_161310_.vegetationChance;
      }), IntProvider.CODEC.fieldOf("xz_radius").forGetter((p_161308_) -> {
         return p_161308_.xzRadius;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("extra_edge_column_chance").forGetter((p_161306_) -> {
         return p_161306_.extraEdgeColumnChance;
      })).apply(p_161304_, VegetationPatchConfiguration::new);
   });
   public final ResourceLocation replaceable;
   public final BlockStateProvider groundState;
   public final Supplier<PlacedFeature> vegetationFeature;
   public final CaveSurface surface;
   public final IntProvider depth;
   public final float extraBottomBlockChance;
   public final int verticalRange;
   public final float vegetationChance;
   public final IntProvider xzRadius;
   public final float extraEdgeColumnChance;

   public VegetationPatchConfiguration(ResourceLocation p_161293_, BlockStateProvider p_161294_, Supplier<PlacedFeature> p_161295_, CaveSurface p_161296_, IntProvider p_161297_, float p_161298_, int p_161299_, float p_161300_, IntProvider p_161301_, float p_161302_) {
      this.replaceable = p_161293_;
      this.groundState = p_161294_;
      this.vegetationFeature = p_161295_;
      this.surface = p_161296_;
      this.depth = p_161297_;
      this.extraBottomBlockChance = p_161298_;
      this.verticalRange = p_161299_;
      this.vegetationChance = p_161300_;
      this.xzRadius = p_161301_;
      this.extraEdgeColumnChance = p_161302_;
   }
}