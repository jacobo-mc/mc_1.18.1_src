package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class RegistryAccess {
   static final Logger LOGGER = LogManager.getLogger();
   static final Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
      Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> builder = ImmutableMap.builder();
      put(builder, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
      put(builder, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
      put(builder, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC);
      put(builder, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC);
      put(builder, Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC);
      put(builder, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC);
      put(builder, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
      put(builder, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC);
      put(builder, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC);
      put(builder, Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC);
      return builder.build();
   });
   private static final RegistryAccess.RegistryHolder BUILTIN = Util.make(() -> {
      RegistryAccess.RegistryHolder registryaccess$registryholder = new RegistryAccess.RegistryHolder();
      DimensionType.registerBuiltin(registryaccess$registryholder);
      REGISTRIES.keySet().stream().filter((p_175518_) -> {
         return !p_175518_.equals(Registry.DIMENSION_TYPE_REGISTRY);
      }).forEach((p_175511_) -> {
         copyBuiltin(registryaccess$registryholder, p_175511_);
      });
      return registryaccess$registryholder;
   });

   public abstract <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> p_175507_);

   public <E> WritableRegistry<E> ownedRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> p_175513_) {
      return this.ownedRegistry(p_175513_).orElseThrow(() -> {
         return new IllegalStateException("Missing registry: " + p_175513_);
      });
   }

   public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> p_123085_) {
      Optional<? extends Registry<E>> optional = this.ownedRegistry(p_123085_);
      return optional.isPresent() ? optional : (Optional<? extends Registry<E>>)Registry.REGISTRY.getOptional(p_123085_.location());
   }

   public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> p_175516_) {
      return this.registry(p_175516_).orElseThrow(() -> {
         return new IllegalStateException("Missing registry: " + p_175516_);
      });
   }

   private static <E> void put(Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> p_123054_, ResourceKey<? extends Registry<E>> p_123055_, Codec<E> p_123056_) {
      p_123054_.put(p_123055_, new RegistryAccess.RegistryData<>(p_123055_, p_123056_, (Codec<E>)null));
   }

   private static <E> void put(Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> p_123058_, ResourceKey<? extends Registry<E>> p_123059_, Codec<E> p_123060_, Codec<E> p_123061_) {
      p_123058_.put(p_123059_, new RegistryAccess.RegistryData<>(p_123059_, p_123060_, p_123061_));
   }

   public static Iterable<RegistryAccess.RegistryData<?>> knownRegistries() {
      return REGISTRIES.values();
   }

   public static RegistryAccess.RegistryHolder builtin() {
      RegistryAccess.RegistryHolder registryaccess$registryholder = new RegistryAccess.RegistryHolder();
      RegistryResourceAccess.InMemoryStorage registryresourceaccess$inmemorystorage = new RegistryResourceAccess.InMemoryStorage();

      for(RegistryAccess.RegistryData<?> registrydata : REGISTRIES.values()) {
         addBuiltinElements(registryaccess$registryholder, registryresourceaccess$inmemorystorage, registrydata);
      }

      RegistryReadOps.createAndLoad(JsonOps.INSTANCE, registryresourceaccess$inmemorystorage, registryaccess$registryholder);
      return registryaccess$registryholder;
   }

   private static <E> void addBuiltinElements(RegistryAccess.RegistryHolder p_194615_, RegistryResourceAccess.InMemoryStorage p_194616_, RegistryAccess.RegistryData<E> p_194617_) {
      ResourceKey<? extends Registry<E>> resourcekey = p_194617_.key();
      boolean flag = !resourcekey.equals(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY) && !resourcekey.equals(Registry.DIMENSION_TYPE_REGISTRY);
      Registry<E> registry = BUILTIN.registryOrThrow(resourcekey);
      WritableRegistry<E> writableregistry = p_194615_.ownedRegistryOrThrow(resourcekey);

      for(Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
         ResourceKey<E> resourcekey1 = entry.getKey();
         E e = entry.getValue();
         if (flag) {
            p_194616_.add(BUILTIN, resourcekey1, p_194617_.codec(), registry.getId(e), e, registry.lifecycle(e));
         } else {
            writableregistry.registerMapping(registry.getId(e), resourcekey1, e, registry.lifecycle(e));
         }
      }

   }

   private static <R extends Registry<?>> void copyBuiltin(RegistryAccess.RegistryHolder p_123079_, ResourceKey<R> p_123080_) {
      Registry<R> registry = (Registry<R>)BuiltinRegistries.REGISTRY;
      Registry<?> registry1 = registry.getOrThrow(p_123080_);
      copy(p_123079_, registry1);
   }

   private static <E> void copy(RegistryAccess.RegistryHolder p_123069_, Registry<E> p_123070_) {
      WritableRegistry<E> writableregistry = p_123069_.ownedRegistryOrThrow(p_123070_.key());

      for(Entry<ResourceKey<E>, E> entry : p_123070_.entrySet()) {
         E e = entry.getValue();
         writableregistry.registerMapping(p_123070_.getId(e), entry.getKey(), e, p_123070_.lifecycle(e));
      }

   }

   public static void load(RegistryAccess p_175501_, RegistryReadOps<?> p_175502_) {
      for(RegistryAccess.RegistryData<?> registrydata : REGISTRIES.values()) {
         readRegistry(p_175502_, p_175501_, registrydata);
      }

   }

   private static <E> void readRegistry(RegistryReadOps<?> p_175504_, RegistryAccess p_175505_, RegistryAccess.RegistryData<E> p_175506_) {
      ResourceKey<? extends Registry<E>> resourcekey = p_175506_.key();
      MappedRegistry<E> mappedregistry = (MappedRegistry)p_175505_.<E>ownedRegistryOrThrow(resourcekey);
      DataResult<MappedRegistry<E>> dataresult = p_175504_.decodeElements(mappedregistry, p_175506_.key(), p_175506_.codec());
      dataresult.error().ifPresent((p_175499_) -> {
         throw new JsonParseException("Error loading registry data: " + p_175499_.message());
      });
   }

   public static record RegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> codec, @Nullable Codec<E> networkCodec) {
      public boolean sendToClient() {
         return this.networkCodec != null;
      }
   }

   public static final class RegistryHolder extends RegistryAccess {
      public static final Codec<RegistryAccess.RegistryHolder> NETWORK_CODEC = makeNetworkCodec();
      private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> registries;

      private static <E> Codec<RegistryAccess.RegistryHolder> makeNetworkCodec() {
         Codec<ResourceKey<? extends Registry<E>>> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
         Codec<MappedRegistry<E>> codec1 = codec.partialDispatch("type", (p_123134_) -> {
            return DataResult.success(p_123134_.key());
         }, (p_123145_) -> {
            return getNetworkCodec(p_123145_).map((p_175531_) -> {
               return MappedRegistry.networkCodec(p_123145_, Lifecycle.experimental(), p_175531_);
            });
         });
         UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> unboundedmapcodec = Codec.unboundedMap(codec, codec1);
         return captureMap(unboundedmapcodec);
      }

      private static <K extends ResourceKey<? extends Registry<?>>, V extends MappedRegistry<?>> Codec<RegistryAccess.RegistryHolder> captureMap(UnboundedMapCodec<K, V> p_123119_) {
         return p_123119_.xmap(RegistryAccess.RegistryHolder::new, (p_123136_) -> {
            return ((Map<K, V>)p_123136_.registries).entrySet().stream().filter((p_175526_) -> {
               return RegistryAccess.REGISTRIES.get(p_175526_.getKey()).sendToClient();
            }).collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
         });
      }

      private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> p_123138_) {
         return Optional.ofNullable(RegistryAccess.REGISTRIES.get(p_123138_)).map((p_123123_) -> {
            return (Codec<E>)p_123123_.networkCodec();
         }).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown or not serializable registry: " + p_123138_);
         });
      }

      public RegistryHolder() {
         this(RegistryAccess.REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), RegistryAccess.RegistryHolder::createRegistry)));
      }

      public static RegistryAccess readFromDisk(Dynamic<?> p_194623_) {
         return new RegistryAccess.RegistryHolder(RegistryAccess.REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), (p_194626_) -> {
            return parseRegistry(p_194626_, p_194623_);
         })));
      }

      private static <E> MappedRegistry<?> parseRegistry(ResourceKey<? extends Registry<?>> p_194630_, Dynamic<?> p_194631_) {
         return (MappedRegistry)RegistryLookupCodec.create((ResourceKey<? extends Registry<E>>)p_194630_).codec().parse(p_194631_).resultOrPartial(Util.prefix(p_194630_ + " registry: ", RegistryAccess.LOGGER::error)).orElseThrow(() -> {
            return new IllegalStateException("Failed to get " + p_194630_ + " registry");
         });
      }

      private RegistryHolder(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> p_123117_) {
         this.registries = p_123117_;
      }

      private static <E> MappedRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> p_123141_) {
         return new MappedRegistry(p_123141_, Lifecycle.stable());
      }

      public <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> p_175528_) {
         return Optional.ofNullable(this.registries.get(p_175528_)).map((p_194628_) -> {
            return (WritableRegistry<E>)p_194628_;
         });
      }
   }
}
