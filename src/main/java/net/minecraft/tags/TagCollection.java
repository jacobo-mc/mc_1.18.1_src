package net.minecraft.tags;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface TagCollection<T> {
   Map<ResourceLocation, Tag<T>> getAllTags();

   @Nullable
   default Tag<T> getTag(ResourceLocation p_13405_) {
      return this.getAllTags().get(p_13405_);
   }

   Tag<T> getTagOrEmpty(ResourceLocation p_13409_);

   @Nullable
   default ResourceLocation getId(Tag.Named<T> p_144407_) {
      return p_144407_.getName();
   }

   @Nullable
   ResourceLocation getId(Tag<T> p_13393_);

   default boolean hasTag(ResourceLocation p_144424_) {
      return this.getAllTags().containsKey(p_144424_);
   }

   default Collection<ResourceLocation> getAvailableTags() {
      return this.getAllTags().keySet();
   }

   default Collection<ResourceLocation> getMatchingTags(T p_13395_) {
      List<ResourceLocation> list = Lists.newArrayList();

      for(Entry<ResourceLocation, Tag<T>> entry : this.getAllTags().entrySet()) {
         if (entry.getValue().contains(p_13395_)) {
            list.add(entry.getKey());
         }
      }

      return list;
   }

   default TagCollection.NetworkPayload serializeToNetwork(Registry<T> p_144412_) {
      Map<ResourceLocation, Tag<T>> map = this.getAllTags();
      Map<ResourceLocation, IntList> map1 = Maps.newHashMapWithExpectedSize(map.size());
      map.forEach((p_144416_, p_144417_) -> {
         List<T> list = p_144417_.getValues();
         IntList intlist = new IntArrayList(list.size());

         for(T t : list) {
            intlist.add(p_144412_.getId(t));
         }

         map1.put(p_144416_, intlist);
      });
      return new TagCollection.NetworkPayload(map1);
   }

   static <T> TagCollection<T> createFromNetwork(TagCollection.NetworkPayload p_144409_, Registry<? extends T> p_144410_) {
      Map<ResourceLocation, Tag<T>> map = Maps.newHashMapWithExpectedSize(p_144409_.tags.size());
      p_144409_.tags.forEach((p_144421_, p_144422_) -> {
         Builder<T> builder = ImmutableSet.builder();

         for(int i : p_144422_) {
            builder.add(p_144410_.byId(i));
         }

         map.put(p_144421_, Tag.fromSet(builder.build()));
      });
      return of(map);
   }

   static <T> TagCollection<T> empty() {
      return of(ImmutableBiMap.of());
   }

   static <T> TagCollection<T> of(Map<ResourceLocation, Tag<T>> p_13397_) {
      final BiMap<ResourceLocation, Tag<T>> bimap = ImmutableBiMap.copyOf(p_13397_);
      return new TagCollection<T>() {
         private final Tag<T> empty = SetTag.empty();

         public Tag<T> getTagOrEmpty(ResourceLocation p_13419_) {
            return bimap.getOrDefault(p_13419_, this.empty);
         }

         @Nullable
         public ResourceLocation getId(Tag<T> p_13417_) {
            return p_13417_ instanceof Tag.Named ? ((Tag.Named)p_13417_).getName() : bimap.inverse().get(p_13417_);
         }

         public Map<ResourceLocation, Tag<T>> getAllTags() {
            return bimap;
         }
      };
   }

   public static class NetworkPayload {
      final Map<ResourceLocation, IntList> tags;

      NetworkPayload(Map<ResourceLocation, IntList> p_144427_) {
         this.tags = p_144427_;
      }

      public void write(FriendlyByteBuf p_144429_) {
         p_144429_.writeMap(this.tags, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeIntIdList);
      }

      public static TagCollection.NetworkPayload read(FriendlyByteBuf p_144431_) {
         return new TagCollection.NetworkPayload(p_144431_.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readIntIdList));
      }
   }
}