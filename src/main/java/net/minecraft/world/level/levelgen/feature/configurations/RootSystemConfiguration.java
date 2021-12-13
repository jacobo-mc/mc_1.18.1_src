package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RootSystemConfiguration implements FeatureConfiguration {
   public static final Codec<RootSystemConfiguration> CODEC = RecordCodecBuilder.create((p_198371_) -> {
      return p_198371_.group(PlacedFeature.CODEC.fieldOf("feature").forGetter((p_161153_) -> {
         return p_161153_.treeFeature;
      }), Codec.intRange(1, 64).fieldOf("required_vertical_space_for_tree").forGetter((p_161151_) -> {
         return p_161151_.requiredVerticalSpaceForTree;
      }), Codec.intRange(1, 64).fieldOf("root_radius").forGetter((p_161149_) -> {
         return p_161149_.rootRadius;
      }), ResourceLocation.CODEC.fieldOf("root_replaceable").forGetter((p_161147_) -> {
         return p_161147_.rootReplaceable;
      }), BlockStateProvider.CODEC.fieldOf("root_state_provider").forGetter((p_161145_) -> {
         return p_161145_.rootStateProvider;
      }), Codec.intRange(1, 256).fieldOf("root_placement_attempts").forGetter((p_161143_) -> {
         return p_161143_.rootPlacementAttempts;
      }), Codec.intRange(1, 4096).fieldOf("root_column_max_height").forGetter((p_161141_) -> {
         return p_161141_.rootColumnMaxHeight;
      }), Codec.intRange(1, 64).fieldOf("hanging_root_radius").forGetter((p_161139_) -> {
         return p_161139_.hangingRootRadius;
      }), Codec.intRange(0, 16).fieldOf("hanging_roots_vertical_span").forGetter((p_161137_) -> {
         return p_161137_.hangingRootsVerticalSpan;
      }), BlockStateProvider.CODEC.fieldOf("hanging_root_state_provider").forGetter((p_161135_) -> {
         return p_161135_.hangingRootStateProvider;
      }), Codec.intRange(1, 256).fieldOf("hanging_root_placement_attempts").forGetter((p_161133_) -> {
         return p_161133_.hangingRootPlacementAttempts;
      }), Codec.intRange(1, 64).fieldOf("allowed_vertical_water_for_tree").forGetter((p_161131_) -> {
         return p_161131_.allowedVerticalWaterForTree;
      }), BlockPredicate.CODEC.fieldOf("allowed_tree_position").forGetter((p_198373_) -> {
         return p_198373_.allowedTreePosition;
      })).apply(p_198371_, RootSystemConfiguration::new);
   });
   public final Supplier<PlacedFeature> treeFeature;
   public final int requiredVerticalSpaceForTree;
   public final int rootRadius;
   public final ResourceLocation rootReplaceable;
   public final BlockStateProvider rootStateProvider;
   public final int rootPlacementAttempts;
   public final int rootColumnMaxHeight;
   public final int hangingRootRadius;
   public final int hangingRootsVerticalSpan;
   public final BlockStateProvider hangingRootStateProvider;
   public final int hangingRootPlacementAttempts;
   public final int allowedVerticalWaterForTree;
   public final BlockPredicate allowedTreePosition;

   public RootSystemConfiguration(Supplier<PlacedFeature> p_198357_, int p_198358_, int p_198359_, ResourceLocation p_198360_, BlockStateProvider p_198361_, int p_198362_, int p_198363_, int p_198364_, int p_198365_, BlockStateProvider p_198366_, int p_198367_, int p_198368_, BlockPredicate p_198369_) {
      this.treeFeature = p_198357_;
      this.requiredVerticalSpaceForTree = p_198358_;
      this.rootRadius = p_198359_;
      this.rootReplaceable = p_198360_;
      this.rootStateProvider = p_198361_;
      this.rootPlacementAttempts = p_198362_;
      this.rootColumnMaxHeight = p_198363_;
      this.hangingRootRadius = p_198364_;
      this.hangingRootsVerticalSpan = p_198365_;
      this.hangingRootStateProvider = p_198366_;
      this.hangingRootPlacementAttempts = p_198367_;
      this.allowedVerticalWaterForTree = p_198368_;
      this.allowedTreePosition = p_198369_;
   }
}