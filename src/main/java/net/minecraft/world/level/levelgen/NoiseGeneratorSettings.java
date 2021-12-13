package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public final class NoiseGeneratorSettings {
   public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create((p_64475_) -> {
      return p_64475_.group(StructureSettings.CODEC.fieldOf("structures").forGetter(NoiseGeneratorSettings::structureSettings), NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings), BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::getDefaultBlock), BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::getDefaultFluid), SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule").forGetter(NoiseGeneratorSettings::surfaceRule), Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel), Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration), Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled), Codec.BOOL.fieldOf("noise_caves_enabled").forGetter(NoiseGeneratorSettings::isNoiseCavesEnabled), Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(NoiseGeneratorSettings::isOreVeinsEnabled), Codec.BOOL.fieldOf("noodle_caves_enabled").forGetter(NoiseGeneratorSettings::isNoodleCavesEnabled), Codec.BOOL.fieldOf("legacy_random_source").forGetter(NoiseGeneratorSettings::useLegacyRandomSource)).apply(p_64475_, NoiseGeneratorSettings::new);
   });
   public static final Codec<Supplier<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
   private final WorldgenRandom.Algorithm randomSource;
   private final StructureSettings structureSettings;
   private final NoiseSettings noiseSettings;
   private final BlockState defaultBlock;
   private final BlockState defaultFluid;
   private final SurfaceRules.RuleSource surfaceRule;
   private final int seaLevel;
   private final boolean disableMobGeneration;
   private final boolean aquifersEnabled;
   private final boolean noiseCavesEnabled;
   private final boolean oreVeinsEnabled;
   private final boolean noodleCavesEnabled;
   public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld"));
   public static final ResourceKey<NoiseGeneratorSettings> LARGE_BIOMES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("large_biomes"));
   public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("amplified"));
   public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("nether"));
   public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("end"));
   public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("caves"));
   public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("floating_islands"));

   private NoiseGeneratorSettings(StructureSettings p_188873_, NoiseSettings p_188874_, BlockState p_188875_, BlockState p_188876_, SurfaceRules.RuleSource p_188877_, int p_188878_, boolean p_188879_, boolean p_188880_, boolean p_188881_, boolean p_188882_, boolean p_188883_, boolean p_188884_) {
      this.structureSettings = p_188873_;
      this.noiseSettings = p_188874_;
      this.defaultBlock = p_188875_;
      this.defaultFluid = p_188876_;
      this.surfaceRule = p_188877_;
      this.seaLevel = p_188878_;
      this.disableMobGeneration = p_188879_;
      this.aquifersEnabled = p_188880_;
      this.noiseCavesEnabled = p_188881_;
      this.oreVeinsEnabled = p_188882_;
      this.noodleCavesEnabled = p_188883_;
      this.randomSource = p_188884_ ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
   }

   public StructureSettings structureSettings() {
      return this.structureSettings;
   }

   public NoiseSettings noiseSettings() {
      return this.noiseSettings;
   }

   public BlockState getDefaultBlock() {
      return this.defaultBlock;
   }

   public BlockState getDefaultFluid() {
      return this.defaultFluid;
   }

   public SurfaceRules.RuleSource surfaceRule() {
      return this.surfaceRule;
   }

   public int seaLevel() {
      return this.seaLevel;
   }

   /** @deprecated */
   @Deprecated
   protected boolean disableMobGeneration() {
      return this.disableMobGeneration;
   }

   public boolean isAquifersEnabled() {
      return this.aquifersEnabled;
   }

   public boolean isNoiseCavesEnabled() {
      return this.noiseCavesEnabled;
   }

   public boolean isOreVeinsEnabled() {
      return this.oreVeinsEnabled;
   }

   public boolean isNoodleCavesEnabled() {
      return this.noodleCavesEnabled;
   }

   public boolean useLegacyRandomSource() {
      return this.randomSource == WorldgenRandom.Algorithm.LEGACY;
   }

   public RandomSource createRandomSource(long p_188886_) {
      return this.getRandomSource().newInstance(p_188886_);
   }

   public WorldgenRandom.Algorithm getRandomSource() {
      return this.randomSource;
   }

   public boolean stable(ResourceKey<NoiseGeneratorSettings> p_64477_) {
      return Objects.equals(this, BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(p_64477_));
   }

   private static void register(ResourceKey<NoiseGeneratorSettings> p_198263_, NoiseGeneratorSettings p_198264_) {
      BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, p_198263_.location(), p_198264_);
   }

   public static NoiseGeneratorSettings bootstrap() {
      return BuiltinRegistries.NOISE_GENERATOR_SETTINGS.iterator().next();
   }

   private static NoiseGeneratorSettings end() {
      return new NoiseGeneratorSettings(new StructureSettings(false), NoiseSettings.create(0, 128, new NoiseSamplingSettings(2.0D, 1.0D, 80.0D, 160.0D), new NoiseSlider(-23.4375D, 64, -46), new NoiseSlider(-0.234375D, 7, 1), 2, 1, true, false, false, TerrainProvider.end()), Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), SurfaceRuleData.end(), 0, true, false, false, false, false, true);
   }

   private static NoiseGeneratorSettings nether() {
      Map<StructureFeature<?>, StructureFeatureConfiguration> map = Maps.newHashMap(StructureSettings.DEFAULTS);
      map.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(25, 10, 34222645));
      return new NoiseGeneratorSettings(new StructureSettings(Optional.empty(), map), NoiseSettings.create(0, 128, new NoiseSamplingSettings(1.0D, 3.0D, 80.0D, 60.0D), new NoiseSlider(0.9375D, 3, 0), new NoiseSlider(2.5D, 4, -1), 1, 2, false, false, false, TerrainProvider.nether()), Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), SurfaceRuleData.nether(), 32, false, false, false, false, false, true);
   }

   private static NoiseGeneratorSettings overworld(boolean p_198266_, boolean p_198267_) {
      return new NoiseGeneratorSettings(new StructureSettings(true), NoiseSettings.create(-64, 384, new NoiseSamplingSettings(1.0D, 1.0D, 80.0D, 160.0D), new NoiseSlider(-0.078125D, 2, p_198266_ ? 0 : 8), new NoiseSlider(p_198266_ ? 0.4D : 0.1171875D, 3, 0), 1, 2, false, p_198266_, p_198267_, TerrainProvider.overworld(p_198266_)), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), SurfaceRuleData.overworld(), 63, false, true, true, true, true, false);
   }

   private static NoiseGeneratorSettings caves() {
      return new NoiseGeneratorSettings(new StructureSettings(false), NoiseSettings.create(-64, 192, new NoiseSamplingSettings(1.0D, 3.0D, 80.0D, 60.0D), new NoiseSlider(0.9375D, 3, 0), new NoiseSlider(2.5D, 4, -1), 1, 2, false, false, false, TerrainProvider.caves()), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), SurfaceRuleData.overworldLike(false, true, true), 32, false, false, false, false, false, true);
   }

   private static NoiseGeneratorSettings floatingIslands() {
      return new NoiseGeneratorSettings(new StructureSettings(true), NoiseSettings.create(0, 256, new NoiseSamplingSettings(2.0D, 1.0D, 80.0D, 160.0D), new NoiseSlider(-23.4375D, 64, -46), new NoiseSlider(-0.234375D, 7, 1), 2, 1, false, false, false, TerrainProvider.floatingIslands()), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), SurfaceRuleData.overworldLike(false, false, false), -64, false, false, false, false, false, true);
   }

   static {
      register(OVERWORLD, overworld(false, false));
      register(LARGE_BIOMES, overworld(false, true));
      register(AMPLIFIED, overworld(true, false));
      register(NETHER, nether());
      register(END, end());
      register(CAVES, caves());
      register(FLOATING_ISLANDS, floatingIslands());
   }
}