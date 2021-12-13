package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GeodeBlockSettings {
   public final BlockStateProvider fillingProvider;
   public final BlockStateProvider innerLayerProvider;
   public final BlockStateProvider alternateInnerLayerProvider;
   public final BlockStateProvider middleLayerProvider;
   public final BlockStateProvider outerLayerProvider;
   public final List<BlockState> innerPlacements;
   public final ResourceLocation cannotReplace;
   public final ResourceLocation invalidBlocks;
   public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create((p_158307_) -> {
      return p_158307_.group(BlockStateProvider.CODEC.fieldOf("filling_provider").forGetter((p_158323_) -> {
         return p_158323_.fillingProvider;
      }), BlockStateProvider.CODEC.fieldOf("inner_layer_provider").forGetter((p_158321_) -> {
         return p_158321_.innerLayerProvider;
      }), BlockStateProvider.CODEC.fieldOf("alternate_inner_layer_provider").forGetter((p_158319_) -> {
         return p_158319_.alternateInnerLayerProvider;
      }), BlockStateProvider.CODEC.fieldOf("middle_layer_provider").forGetter((p_158317_) -> {
         return p_158317_.middleLayerProvider;
      }), BlockStateProvider.CODEC.fieldOf("outer_layer_provider").forGetter((p_158315_) -> {
         return p_158315_.outerLayerProvider;
      }), ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter((p_158313_) -> {
         return p_158313_.innerPlacements;
      }), ResourceLocation.CODEC.fieldOf("cannot_replace").forGetter((p_158311_) -> {
         return p_158311_.cannotReplace;
      }), ResourceLocation.CODEC.fieldOf("invalid_blocks").forGetter((p_158309_) -> {
         return p_158309_.invalidBlocks;
      })).apply(p_158307_, GeodeBlockSettings::new);
   });

   public GeodeBlockSettings(BlockStateProvider p_158298_, BlockStateProvider p_158299_, BlockStateProvider p_158300_, BlockStateProvider p_158301_, BlockStateProvider p_158302_, List<BlockState> p_158303_, ResourceLocation p_158304_, ResourceLocation p_158305_) {
      this.fillingProvider = p_158298_;
      this.innerLayerProvider = p_158299_;
      this.alternateInnerLayerProvider = p_158300_;
      this.middleLayerProvider = p_158301_;
      this.outerLayerProvider = p_158302_;
      this.innerPlacements = p_158303_;
      this.cannotReplace = p_158304_;
      this.invalidBlocks = p_158305_;
   }
}