package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class DimensionType {
   public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
   public static final int MIN_HEIGHT = 16;
   public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
   public static final int MAX_Y = (Y_SIZE >> 1) - 1;
   public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
   public static final int WAY_ABOVE_MAX_Y = MAX_Y << 4;
   public static final int WAY_BELOW_MIN_Y = MIN_Y << 4;
   public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
   public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
   public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
   public static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.<DimensionType>create((p_63914_) -> {
      return p_63914_.group(Codec.LONG.optionalFieldOf("fixed_time").xmap((p_156696_) -> {
         return p_156696_.map(OptionalLong::of).orElseGet(OptionalLong::empty);
      }, (p_156698_) -> {
         return p_156698_.isPresent() ? Optional.of(p_156698_.getAsLong()) : Optional.empty();
      }).forGetter((p_156731_) -> {
         return p_156731_.fixedTime;
      }), Codec.BOOL.fieldOf("has_skylight").forGetter(DimensionType::hasSkyLight), Codec.BOOL.fieldOf("has_ceiling").forGetter(DimensionType::hasCeiling), Codec.BOOL.fieldOf("ultrawarm").forGetter(DimensionType::ultraWarm), Codec.BOOL.fieldOf("natural").forGetter(DimensionType::natural), Codec.doubleRange((double)1.0E-5F, 3.0E7D).fieldOf("coordinate_scale").forGetter(DimensionType::coordinateScale), Codec.BOOL.fieldOf("piglin_safe").forGetter(DimensionType::piglinSafe), Codec.BOOL.fieldOf("bed_works").forGetter(DimensionType::bedWorks), Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(DimensionType::respawnAnchorWorks), Codec.BOOL.fieldOf("has_raids").forGetter(DimensionType::hasRaids), Codec.intRange(MIN_Y, MAX_Y).fieldOf("min_y").forGetter(DimensionType::minY), Codec.intRange(16, Y_SIZE).fieldOf("height").forGetter(DimensionType::height), Codec.intRange(0, Y_SIZE).fieldOf("logical_height").forGetter(DimensionType::logicalHeight), ResourceLocation.CODEC.fieldOf("infiniburn").forGetter((p_156729_) -> {
         return p_156729_.infiniburn;
      }), ResourceLocation.CODEC.fieldOf("effects").orElse(OVERWORLD_EFFECTS).forGetter((p_156725_) -> {
         return p_156725_.effectsLocation;
      }), Codec.FLOAT.fieldOf("ambient_light").forGetter((p_156721_) -> {
         return p_156721_.ambientLight;
      })).apply(p_63914_, DimensionType::new);
   }).comapFlatMap(DimensionType::guardY, Function.identity());
   private static final int MOON_PHASES = 8;
   public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
   public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
   public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
   public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
   protected static final DimensionType DEFAULT_OVERWORLD = create(OptionalLong.empty(), true, false, false, true, 1.0D, false, false, true, false, true, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);
   protected static final DimensionType DEFAULT_NETHER = create(OptionalLong.of(18000L), false, true, true, false, 8.0D, false, true, false, true, false, 0, 256, 128, BlockTags.INFINIBURN_NETHER.getName(), NETHER_EFFECTS, 0.1F);
   protected static final DimensionType DEFAULT_END = create(OptionalLong.of(6000L), false, false, false, false, 1.0D, true, false, false, false, true, 0, 256, 256, BlockTags.INFINIBURN_END.getName(), END_EFFECTS, 0.0F);
   public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves"));
   protected static final DimensionType DEFAULT_OVERWORLD_CAVES = create(OptionalLong.empty(), true, true, false, true, 1.0D, false, false, true, false, true, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);
   public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
   private final OptionalLong fixedTime;
   private final boolean hasSkylight;
   private final boolean hasCeiling;
   private final boolean ultraWarm;
   private final boolean natural;
   private final double coordinateScale;
   private final boolean createDragonFight;
   private final boolean piglinSafe;
   private final boolean bedWorks;
   private final boolean respawnAnchorWorks;
   private final boolean hasRaids;
   private final int minY;
   private final int height;
   private final int logicalHeight;
   private final ResourceLocation infiniburn;
   private final ResourceLocation effectsLocation;
   private final float ambientLight;
   private final transient float[] brightnessRamp;

   private static DataResult<DimensionType> guardY(DimensionType p_156719_) {
      if (p_156719_.height() < 16) {
         return DataResult.error("height has to be at least 16");
      } else if (p_156719_.minY() + p_156719_.height() > MAX_Y + 1) {
         return DataResult.error("min_y + height cannot be higher than: " + (MAX_Y + 1));
      } else if (p_156719_.logicalHeight() > p_156719_.height()) {
         return DataResult.error("logical_height cannot be higher than height");
      } else if (p_156719_.height() % 16 != 0) {
         return DataResult.error("height has to be multiple of 16");
      } else {
         return p_156719_.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(p_156719_);
      }
   }

   private DimensionType(OptionalLong p_156656_, boolean p_156657_, boolean p_156658_, boolean p_156659_, boolean p_156660_, double p_156661_, boolean p_156662_, boolean p_156663_, boolean p_156664_, boolean p_156665_, int p_156666_, int p_156667_, int p_156668_, ResourceLocation p_156669_, ResourceLocation p_156670_, float p_156671_) {
      this(p_156656_, p_156657_, p_156658_, p_156659_, p_156660_, p_156661_, false, p_156662_, p_156663_, p_156664_, p_156665_, p_156666_, p_156667_, p_156668_, p_156669_, p_156670_, p_156671_);
   }

   public static DimensionType create(OptionalLong p_188325_, boolean p_188326_, boolean p_188327_, boolean p_188328_, boolean p_188329_, double p_188330_, boolean p_188331_, boolean p_188332_, boolean p_188333_, boolean p_188334_, boolean p_188335_, int p_188336_, int p_188337_, int p_188338_, ResourceLocation p_188339_, ResourceLocation p_188340_, float p_188341_) {
      DimensionType dimensiontype = new DimensionType(p_188325_, p_188326_, p_188327_, p_188328_, p_188329_, p_188330_, p_188331_, p_188332_, p_188333_, p_188334_, p_188335_, p_188336_, p_188337_, p_188338_, p_188339_, p_188340_, p_188341_);
      guardY(dimensiontype).error().ifPresent((p_156692_) -> {
         throw new IllegalStateException(p_156692_.message());
      });
      return dimensiontype;
   }

   /** @deprecated */
   @Deprecated
   private DimensionType(OptionalLong p_188296_, boolean p_188297_, boolean p_188298_, boolean p_188299_, boolean p_188300_, double p_188301_, boolean p_188302_, boolean p_188303_, boolean p_188304_, boolean p_188305_, boolean p_188306_, int p_188307_, int p_188308_, int p_188309_, ResourceLocation p_188310_, ResourceLocation p_188311_, float p_188312_) {
      this.fixedTime = p_188296_;
      this.hasSkylight = p_188297_;
      this.hasCeiling = p_188298_;
      this.ultraWarm = p_188299_;
      this.natural = p_188300_;
      this.coordinateScale = p_188301_;
      this.createDragonFight = p_188302_;
      this.piglinSafe = p_188303_;
      this.bedWorks = p_188304_;
      this.respawnAnchorWorks = p_188305_;
      this.hasRaids = p_188306_;
      this.minY = p_188307_;
      this.height = p_188308_;
      this.logicalHeight = p_188309_;
      this.infiniburn = p_188310_;
      this.effectsLocation = p_188311_;
      this.ambientLight = p_188312_;
      this.brightnessRamp = fillBrightnessRamp(p_188312_);
   }

   private static float[] fillBrightnessRamp(float p_63901_) {
      float[] afloat = new float[16];

      for(int i = 0; i <= 15; ++i) {
         float f = (float)i / 15.0F;
         float f1 = f / (4.0F - 3.0F * f);
         afloat[i] = Mth.lerp(p_63901_, f1, 1.0F);
      }

      return afloat;
   }

   /** @deprecated */
   @Deprecated
   public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> p_63912_) {
      Optional<Number> optional = p_63912_.asNumber().result();
      if (optional.isPresent()) {
         int i = optional.get().intValue();
         if (i == -1) {
            return DataResult.success(Level.NETHER);
         }

         if (i == 0) {
            return DataResult.success(Level.OVERWORLD);
         }

         if (i == 1) {
            return DataResult.success(Level.END);
         }
      }

      return Level.RESOURCE_KEY_CODEC.parse(p_63912_);
   }

   public static RegistryAccess registerBuiltin(RegistryAccess p_188316_) {
      WritableRegistry<DimensionType> writableregistry = p_188316_.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
      writableregistry.register(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, Lifecycle.stable());
      writableregistry.register(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
      writableregistry.register(NETHER_LOCATION, DEFAULT_NETHER, Lifecycle.stable());
      writableregistry.register(END_LOCATION, DEFAULT_END, Lifecycle.stable());
      return p_188316_;
   }

   public static MappedRegistry<LevelStem> defaultDimensions(RegistryAccess p_188318_, long p_188319_) {
      return defaultDimensions(p_188318_, p_188319_, true);
   }

   public static MappedRegistry<LevelStem> defaultDimensions(RegistryAccess p_188321_, long p_188322_, boolean p_188323_) {
      MappedRegistry<LevelStem> mappedregistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
      Registry<DimensionType> registry = p_188321_.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
      Registry<Biome> registry1 = p_188321_.registryOrThrow(Registry.BIOME_REGISTRY);
      Registry<NoiseGeneratorSettings> registry2 = p_188321_.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
      Registry<NormalNoise.NoiseParameters> registry3 = p_188321_.registryOrThrow(Registry.NOISE_REGISTRY);
      mappedregistry.register(LevelStem.NETHER, new LevelStem(() -> {
         return registry.getOrThrow(NETHER_LOCATION);
      }, new NoiseBasedChunkGenerator(registry3, MultiNoiseBiomeSource.Preset.NETHER.biomeSource(registry1, p_188323_), p_188322_, () -> {
         return registry2.getOrThrow(NoiseGeneratorSettings.NETHER);
      })), Lifecycle.stable());
      mappedregistry.register(LevelStem.END, new LevelStem(() -> {
         return registry.getOrThrow(END_LOCATION);
      }, new NoiseBasedChunkGenerator(registry3, new TheEndBiomeSource(registry1, p_188322_), p_188322_, () -> {
         return registry2.getOrThrow(NoiseGeneratorSettings.END);
      })), Lifecycle.stable());
      return mappedregistry;
   }

   public static double getTeleportationScale(DimensionType p_63909_, DimensionType p_63910_) {
      double d0 = p_63909_.coordinateScale();
      double d1 = p_63910_.coordinateScale();
      return d0 / d1;
   }

   /** @deprecated */
   @Deprecated
   public String getFileSuffix() {
      return this.equalTo(DEFAULT_END) ? "_end" : "";
   }

   public static Path getStorageFolder(ResourceKey<Level> p_196976_, Path p_196977_) {
      if (p_196976_ == Level.OVERWORLD) {
         return p_196977_;
      } else if (p_196976_ == Level.END) {
         return p_196977_.resolve("DIM1");
      } else {
         return p_196976_ == Level.NETHER ? p_196977_.resolve("DIM-1") : p_196977_.resolve("dimensions").resolve(p_196976_.location().getNamespace()).resolve(p_196976_.location().getPath());
      }
   }

   public boolean hasSkyLight() {
      return this.hasSkylight;
   }

   public boolean hasCeiling() {
      return this.hasCeiling;
   }

   public boolean ultraWarm() {
      return this.ultraWarm;
   }

   public boolean natural() {
      return this.natural;
   }

   public double coordinateScale() {
      return this.coordinateScale;
   }

   public boolean piglinSafe() {
      return this.piglinSafe;
   }

   public boolean bedWorks() {
      return this.bedWorks;
   }

   public boolean respawnAnchorWorks() {
      return this.respawnAnchorWorks;
   }

   public boolean hasRaids() {
      return this.hasRaids;
   }

   public int minY() {
      return this.minY;
   }

   public int height() {
      return this.height;
   }

   public int logicalHeight() {
      return this.logicalHeight;
   }

   public boolean createDragonFight() {
      return this.createDragonFight;
   }

   public boolean hasFixedTime() {
      return this.fixedTime.isPresent();
   }

   public float timeOfDay(long p_63905_) {
      double d0 = Mth.frac((double)this.fixedTime.orElse(p_63905_) / 24000.0D - 0.25D);
      double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
      return (float)(d0 * 2.0D + d1) / 3.0F;
   }

   public int moonPhase(long p_63937_) {
      return (int)(p_63937_ / 24000L % 8L + 8L) % 8;
   }

   public float brightness(int p_63903_) {
      return this.brightnessRamp[p_63903_];
   }

   public Tag<Block> infiniburn() {
      Tag<Block> tag = BlockTags.getAllTags().getTag(this.infiniburn);
      return (Tag<Block>)(tag != null ? tag : BlockTags.INFINIBURN_OVERWORLD);
   }

   public ResourceLocation effectsLocation() {
      return this.effectsLocation;
   }

   public boolean equalTo(DimensionType p_63907_) {
      if (this == p_63907_) {
         return true;
      } else {
         return this.hasSkylight == p_63907_.hasSkylight && this.hasCeiling == p_63907_.hasCeiling && this.ultraWarm == p_63907_.ultraWarm && this.natural == p_63907_.natural && this.coordinateScale == p_63907_.coordinateScale && this.createDragonFight == p_63907_.createDragonFight && this.piglinSafe == p_63907_.piglinSafe && this.bedWorks == p_63907_.bedWorks && this.respawnAnchorWorks == p_63907_.respawnAnchorWorks && this.hasRaids == p_63907_.hasRaids && this.minY == p_63907_.minY && this.height == p_63907_.height && this.logicalHeight == p_63907_.logicalHeight && Float.compare(p_63907_.ambientLight, this.ambientLight) == 0 && this.fixedTime.equals(p_63907_.fixedTime) && this.infiniburn.equals(p_63907_.infiniburn) && this.effectsLocation.equals(p_63907_.effectsLocation);
      }
   }
}
