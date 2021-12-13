package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.dimension.DimensionType;

public record NoiseSettings(int minY, int height, NoiseSamplingSettings noiseSamplingSettings, NoiseSlider topSlideSettings, NoiseSlider bottomSlideSettings, int noiseSizeHorizontal, int noiseSizeVertical, boolean islandNoiseOverride, boolean isAmplified, boolean largeBiomes, TerrainShaper terrainShaper) {
   public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.<NoiseSettings>create((p_64536_) -> {
      return p_64536_.group(Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y").forGetter(NoiseSettings::minY), Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(NoiseSettings::height), NoiseSamplingSettings.CODEC.fieldOf("sampling").forGetter(NoiseSettings::noiseSamplingSettings), NoiseSlider.CODEC.fieldOf("top_slide").forGetter(NoiseSettings::topSlideSettings), NoiseSlider.CODEC.fieldOf("bottom_slide").forGetter(NoiseSettings::bottomSlideSettings), Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal), Codec.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical), Codec.BOOL.optionalFieldOf("island_noise_override", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::islandNoiseOverride), Codec.BOOL.optionalFieldOf("amplified", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::isAmplified), Codec.BOOL.optionalFieldOf("large_biomes", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::largeBiomes), TerrainShaper.CODEC.fieldOf("terrain_shaper").forGetter(NoiseSettings::terrainShaper)).apply(p_64536_, NoiseSettings::new);
   }).comapFlatMap(NoiseSettings::guardY, Function.identity());

   private static DataResult<NoiseSettings> guardY(NoiseSettings p_158721_) {
      if (p_158721_.minY() + p_158721_.height() > DimensionType.MAX_Y + 1) {
         return DataResult.error("min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
      } else if (p_158721_.height() % 16 != 0) {
         return DataResult.error("height has to be a multiple of 16");
      } else {
         return p_158721_.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(p_158721_);
      }
   }

   public static NoiseSettings create(int p_189200_, int p_189201_, NoiseSamplingSettings p_189202_, NoiseSlider p_189203_, NoiseSlider p_189204_, int p_189205_, int p_189206_, boolean p_189207_, boolean p_189208_, boolean p_189209_, TerrainShaper p_189210_) {
      NoiseSettings noisesettings = new NoiseSettings(p_189200_, p_189201_, p_189202_, p_189203_, p_189204_, p_189205_, p_189206_, p_189207_, p_189208_, p_189209_, p_189210_);
      guardY(noisesettings).error().ifPresent((p_158719_) -> {
         throw new IllegalStateException(p_158719_.message());
      });
      return noisesettings;
   }

   public int getCellHeight() {
      return QuartPos.toBlock(this.noiseSizeVertical());
   }

   public int getCellWidth() {
      return QuartPos.toBlock(this.noiseSizeHorizontal());
   }

   public int getCellCountY() {
      return this.height() / this.getCellHeight();
   }

   public int getMinCellY() {
      return Mth.intFloorDiv(this.minY(), this.getCellHeight());
   }
}
