package net.minecraft.resources;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.server.packs.resources.ResourceManager;

public class RegistryReadOps<T> extends DelegatingOps<T> {
   private final RegistryResourceAccess resources;
   private final RegistryAccess registryAccess;
   private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache;
   private final RegistryReadOps<JsonElement> jsonOps;

   public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> p_179867_, ResourceManager p_179868_, RegistryAccess p_179869_) {
      return createAndLoad(p_179867_, RegistryResourceAccess.forResourceManager(p_179868_), p_179869_);
   }

   public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> p_195842_, RegistryResourceAccess p_195843_, RegistryAccess p_195844_) {
      RegistryReadOps<T> registryreadops = new RegistryReadOps<>(p_195842_, p_195843_, p_195844_, Maps.newIdentityHashMap());
      RegistryAccess.load(p_195844_, registryreadops);
      return registryreadops;
   }

   public static <T> RegistryReadOps<T> create(DynamicOps<T> p_179883_, ResourceManager p_179884_, RegistryAccess p_179885_) {
      return create(p_179883_, RegistryResourceAccess.forResourceManager(p_179884_), p_179885_);
   }

   public static <T> RegistryReadOps<T> create(DynamicOps<T> p_195868_, RegistryResourceAccess p_195869_, RegistryAccess p_195870_) {
      return new RegistryReadOps<>(p_195868_, p_195869_, p_195870_, Maps.newIdentityHashMap());
   }

   private RegistryReadOps(DynamicOps<T> p_195837_, RegistryResourceAccess p_195838_, RegistryAccess p_195839_, IdentityHashMap<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> p_195840_) {
      super(p_195837_);
      this.resources = p_195838_;
      this.registryAccess = p_195839_;
      this.readCache = p_195840_;
      this.jsonOps = p_195837_ == JsonOps.INSTANCE ? (RegistryReadOps<JsonElement>) this : new RegistryReadOps<>(JsonOps.INSTANCE, p_195838_, p_195839_, p_195840_);
   }

   protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T p_135678_, ResourceKey<? extends Registry<E>> p_135679_, Codec<E> p_135680_, boolean p_135681_) {
      Optional<WritableRegistry<E>> optional = this.registryAccess.ownedRegistry(p_135679_);
      if (!optional.isPresent()) {
         return DataResult.error("Unknown registry: " + p_135679_);
      } else {
         WritableRegistry<E> writableregistry = optional.get();
         DataResult<Pair<ResourceLocation, T>> dataresult = ResourceLocation.CODEC.decode(this.delegate, p_135678_);
         if (!dataresult.result().isPresent()) {
            return !p_135681_ ? DataResult.error("Inline definitions not allowed here") : p_135680_.decode(this, p_135678_).map((p_135647_) -> {
               return p_135647_.mapFirst((p_179881_) -> {
                  return () -> {
                     return p_179881_;
                  };
               });
            });
         } else {
            Pair<ResourceLocation, T> pair = dataresult.result().get();
            ResourceKey<E> resourcekey = ResourceKey.create(p_135679_, pair.getFirst());
            return this.readAndRegisterElement(p_135679_, writableregistry, p_135680_, resourcekey).map((p_135650_) -> {
               return Pair.of(p_135650_, pair.getSecond());
            });
         }
      }
   }

   public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> p_135663_, ResourceKey<? extends Registry<E>> p_135664_, Codec<E> p_135665_) {
      Collection<ResourceKey<E>> collection = this.resources.listResources(p_135664_);
      DataResult<MappedRegistry<E>> dataresult = DataResult.success(p_135663_, Lifecycle.stable());

      for(ResourceKey<E> resourcekey : collection) {
         dataresult = dataresult.flatMap((p_195861_) -> {
            return this.readAndRegisterElement(p_135664_, p_195861_, p_135665_, resourcekey).map((p_179876_) -> {
               return p_195861_;
            });
         });
      }

      return dataresult.setPartial(p_135663_);
   }

   private <E> DataResult<Supplier<E>> readAndRegisterElement(ResourceKey<? extends Registry<E>> p_195863_, WritableRegistry<E> p_195864_, Codec<E> p_195865_, ResourceKey<E> p_195866_) {
      RegistryReadOps.ReadCache<E> readcache = this.readCache(p_195863_);
      DataResult<Supplier<E>> dataresult = readcache.values.get(p_195866_);
      if (dataresult != null) {
         return dataresult;
      } else {
         readcache.values.put(p_195866_, DataResult.success(createPlaceholderGetter(p_195864_, p_195866_)));
         Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> optional = this.resources.parseElement(this.jsonOps, p_195863_, p_195866_, p_195865_);
         DataResult<Supplier<E>> dataresult1;
         if (optional.isEmpty()) {
            if (p_195864_.containsKey(p_195866_)) {
               dataresult1 = DataResult.success(createRegistryGetter(p_195864_, p_195866_), Lifecycle.stable());
            } else {
               dataresult1 = DataResult.error("Missing referenced custom/removed registry entry for registry " + p_195863_ + " named " + p_195866_.location());
            }
         } else {
            DataResult<RegistryResourceAccess.ParsedEntry<E>> dataresult2 = optional.get();
            Optional<RegistryResourceAccess.ParsedEntry<E>> optional1 = dataresult2.result();
            if (optional1.isPresent()) {
               RegistryResourceAccess.ParsedEntry<E> parsedentry = optional1.get();
               p_195864_.registerOrOverride(parsedentry.fixedId(), p_195866_, parsedentry.value(), dataresult2.lifecycle());
            }

            dataresult1 = dataresult2.map((p_195856_) -> {
               return createRegistryGetter(p_195864_, p_195866_);
            });
         }

         readcache.values.put(p_195866_, dataresult1);
         return dataresult1;
      }
   }

   private static <E> Supplier<E> createPlaceholderGetter(WritableRegistry<E> p_195851_, ResourceKey<E> p_195852_) {
      return Suppliers.memoize(() -> {
         E e = p_195851_.get(p_195852_);
         if (e == null) {
            throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + p_195852_);
         } else {
            return e;
         }
      });
   }

   private static <E> Supplier<E> createRegistryGetter(final Registry<E> p_195846_, final ResourceKey<E> p_195847_) {
      return new Supplier<E>() {
         public E get() {
            return p_195846_.get(p_195847_);
         }

         public String toString() {
            return p_195847_.toString();
         }
      };
   }

   private <E> RegistryReadOps.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> p_135700_) {
      return (RegistryReadOps.ReadCache<E>)this.readCache.computeIfAbsent(p_135700_, (p_195877_) -> {
         return new RegistryReadOps.ReadCache<E>();
      });
   }

   protected <E> DataResult<Registry<E>> registry(ResourceKey<? extends Registry<E>> p_135683_) {
      return this.registryAccess.ownedRegistry(p_135683_).map((p_195849_) -> {
         return DataResult.<Registry<E>>success(p_195849_, p_195849_.elementsLifecycle());
      }).orElseGet(() -> {
         return DataResult.error("Unknown registry: " + p_135683_);
      });
   }

   static final class ReadCache<E> {
      final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.newIdentityHashMap();
   }
}
