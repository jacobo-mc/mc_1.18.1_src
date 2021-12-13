package net.minecraft.tags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagContainer {
   static final Logger LOGGER = LogManager.getLogger();
   public static final TagContainer EMPTY = new TagContainer(ImmutableMap.of());
   private final Map<ResourceKey<? extends Registry<?>>, TagCollection<?>> collections;

   TagContainer(Map<ResourceKey<? extends Registry<?>>, TagCollection<?>> p_144435_) {
      this.collections = p_144435_;
   }

   @Nullable
   private <T> TagCollection<T> get(ResourceKey<? extends Registry<T>> p_144472_) {
      return (TagCollection<T>)this.collections.get(p_144472_);
   }

   public <T> TagCollection<T> getOrEmpty(ResourceKey<? extends Registry<T>> p_144453_) {
      return (TagCollection<T>)this.collections.getOrDefault(p_144453_, TagCollection.<T>empty());
   }

   public <T, E extends Exception> Tag<T> getTagOrThrow(ResourceKey<? extends Registry<T>> p_144459_, ResourceLocation p_144460_, Function<ResourceLocation, E> p_144461_) throws E {
      TagCollection<T> tagcollection = this.get(p_144459_);
      if (tagcollection == null) {
         throw p_144461_.apply(p_144460_);
      } else {
         Tag<T> tag = tagcollection.getTag(p_144460_);
         if (tag == null) {
            throw p_144461_.apply(p_144460_);
         } else {
            return tag;
         }
      }
   }

   public <T, E extends Exception> ResourceLocation getIdOrThrow(ResourceKey<? extends Registry<T>> p_144455_, Tag<T> p_144456_, Supplier<E> p_144457_) throws E {
      TagCollection<T> tagcollection = this.get(p_144455_);
      if (tagcollection == null) {
         throw p_144457_.get();
      } else {
         ResourceLocation resourcelocation = tagcollection.getId(p_144456_);
         if (resourcelocation == null) {
            throw p_144457_.get();
         } else {
            return resourcelocation;
         }
      }
   }

   public void getAll(TagContainer.CollectionConsumer p_144437_) {
      this.collections.forEach((p_144464_, p_144465_) -> {
         acceptCap(p_144437_, p_144464_, p_144465_);
      });
   }

   private static <T> void acceptCap(TagContainer.CollectionConsumer p_144439_, ResourceKey<? extends Registry<?>> p_144440_, TagCollection<?> p_144441_) {
      p_144439_.accept((ResourceKey<? extends Registry<T>>)p_144440_, (TagCollection<T>)p_144441_);
   }

   public void bindToGlobal() {
      StaticTags.resetAll(this);
      Blocks.rebuildCache();
   }

   public Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> serializeToNetwork(final RegistryAccess p_144443_) {
      final Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> map = Maps.newHashMap();
      this.getAll(new TagContainer.CollectionConsumer() {
         public <T> void accept(ResourceKey<? extends Registry<T>> p_144481_, TagCollection<T> p_144482_) {
            Optional<? extends Registry<T>> optional = p_144443_.registry(p_144481_);
            if (optional.isPresent()) {
               map.put(p_144481_, p_144482_.serializeToNetwork(optional.get()));
            } else {
               TagContainer.LOGGER.error("Unknown registry {}", (Object)p_144481_);
            }

         }
      });
      return map;
   }

   public static TagContainer deserializeFromNetwork(RegistryAccess p_144450_, Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> p_144451_) {
      TagContainer.Builder tagcontainer$builder = new TagContainer.Builder();
      p_144451_.forEach((p_144469_, p_144470_) -> {
         addTagsFromPayload(p_144450_, tagcontainer$builder, p_144469_, p_144470_);
      });
      return tagcontainer$builder.build();
   }

   private static <T> void addTagsFromPayload(RegistryAccess p_144445_, TagContainer.Builder p_144446_, ResourceKey<? extends Registry<? extends T>> p_144447_, TagCollection.NetworkPayload p_144448_) {
      Optional<? extends Registry<? extends T>> optional = p_144445_.registry(p_144447_);
      if (optional.isPresent()) {
         p_144446_.add(p_144447_, TagCollection.createFromNetwork(p_144448_, optional.get()));
      } else {
         LOGGER.error("Unknown registry {}", (Object)p_144447_);
      }

   }

   public static class Builder {
      private final ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, TagCollection<?>> result = ImmutableMap.builder();

      public <T> TagContainer.Builder add(ResourceKey<? extends Registry<? extends T>> p_144487_, TagCollection<T> p_144488_) {
         this.result.put(p_144487_, p_144488_);
         return this;
      }

      public TagContainer build() {
         return new TagContainer(this.result.build());
      }
   }

   @FunctionalInterface
   interface CollectionConsumer {
      <T> void accept(ResourceKey<? extends Registry<T>> p_144489_, TagCollection<T> p_144490_);
   }
}
