package net.minecraft.data;

import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuiltinRegistries {
   protected static final Logger LOGGER = LogManager.getLogger();
   private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.newLinkedHashMap();
   private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry<>(ResourceKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental());
   public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
   public static final Registry<ConfiguredWorldCarver<?>> CONFIGURED_CARVER = registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, () -> {
      return Carvers.CAVE;
   });
   public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, FeatureUtils::bootstrap);
   public static final Registry<ConfiguredStructureFeature<?, ?>> CONFIGURED_STRUCTURE_FEATURE = registerSimple(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, StructureFeatures::bootstrap);
   public static final Registry<PlacedFeature> PLACED_FEATURE = registerSimple(Registry.PLACED_FEATURE_REGISTRY, PlacementUtils::bootstrap);
   public static final Registry<StructureProcessorList> PROCESSOR_LIST = registerSimple(Registry.PROCESSOR_LIST_REGISTRY, () -> {
      return ProcessorLists.ZOMBIE_PLAINS;
   });
   public static final Registry<StructureTemplatePool> TEMPLATE_POOL = registerSimple(Registry.TEMPLATE_POOL_REGISTRY, Pools::bootstrap);
   public static final Registry<Biome> BIOME = registerSimple(Registry.BIOME_REGISTRY, () -> {
      return Biomes.PLAINS;
   });
   public static final Registry<NoiseGeneratorSettings> NOISE_GENERATOR_SETTINGS = registerSimple(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings::bootstrap);
   public static final Registry<NormalNoise.NoiseParameters> NOISE = registerSimple(Registry.NOISE_REGISTRY, NoiseData::bootstrap);

   private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> p_123894_, Supplier<T> p_123895_) {
      return registerSimple(p_123894_, Lifecycle.stable(), p_123895_);
   }

   private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> p_123885_, Lifecycle p_123886_, Supplier<T> p_123887_) {
      return internalRegister(p_123885_, new MappedRegistry<>(p_123885_, p_123886_), p_123887_, p_123886_);
   }

   private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> p_123889_, R p_123890_, Supplier<T> p_123891_, Lifecycle p_123892_) {
      ResourceLocation resourcelocation = p_123889_.location();
      LOADERS.put(resourcelocation, p_123891_);
      WritableRegistry<R> writableregistry = (WritableRegistry<R>)WRITABLE_REGISTRY;
      return (R)writableregistry.register((ResourceKey)p_123889_, p_123890_, p_123892_);
   }

   public static <T> T register(Registry<? super T> p_123877_, String p_123878_, T p_123879_) {
      return register(p_123877_, new ResourceLocation(p_123878_), p_123879_);
   }

   public static <V, T extends V> T register(Registry<V> p_123881_, ResourceLocation p_123882_, T p_123883_) {
      return register(p_123881_, ResourceKey.create(p_123881_.key(), p_123882_), p_123883_);
   }

   public static <V, T extends V> T register(Registry<V> p_194656_, ResourceKey<V> p_194657_, T p_194658_) {
      return ((WritableRegistry<V>)p_194656_).register(p_194657_, p_194658_, Lifecycle.stable());
   }

   public static <V, T extends V> T registerMapping(Registry<V> p_194664_, ResourceKey<V> p_194665_, T p_194666_) {
      return ((WritableRegistry<V>)p_194664_).register(p_194665_, p_194666_, Lifecycle.stable());
   }

   public static void bootstrap() {
   }

   static {
      LOADERS.forEach((p_194660_, p_194661_) -> {
         if (p_194661_.get() == null) {
            LOGGER.error("Unable to bootstrap registry '{}'", (Object)p_194660_);
         }

      });
      Registry.checkRegistry(WRITABLE_REGISTRY);
   }
}
