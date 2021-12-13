package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StaticTagHelper<T> {
   private final ResourceKey<? extends Registry<T>> key;
   private final String directory;
   private TagCollection<T> source = TagCollection.empty();
   private final List<StaticTagHelper.Wrapper<T>> wrappers = Lists.newArrayList();

   public StaticTagHelper(ResourceKey<? extends Registry<T>> p_144329_, String p_144330_) {
      this.key = p_144329_;
      this.directory = p_144330_;
   }

   public Tag.Named<T> bind(String p_13245_) {
      StaticTagHelper.Wrapper<T> wrapper = new StaticTagHelper.Wrapper<>(new ResourceLocation(p_13245_));
      this.wrappers.add(wrapper);
      return wrapper;
   }

   public void resetToEmpty() {
      this.source = TagCollection.empty();
      Tag<T> tag = SetTag.empty();
      this.wrappers.forEach((p_13235_) -> {
         p_13235_.rebind((p_144335_) -> {
            return tag;
         });
      });
   }

   public void reset(TagContainer p_13243_) {
      TagCollection<T> tagcollection = p_13243_.getOrEmpty(this.key);
      this.source = tagcollection;
      this.wrappers.forEach((p_13241_) -> {
         p_13241_.rebind(tagcollection::getTag);
      });
   }

   public TagCollection<T> getAllTags() {
      return this.source;
   }

   public Set<ResourceLocation> getMissingTags(TagContainer p_13248_) {
      TagCollection<T> tagcollection = p_13248_.getOrEmpty(this.key);
      Set<ResourceLocation> set = this.wrappers.stream().map(StaticTagHelper.Wrapper::getName).collect(Collectors.toSet());
      ImmutableSet<ResourceLocation> immutableset = ImmutableSet.copyOf(tagcollection.getAvailableTags());
      return Sets.difference(set, immutableset);
   }

   public ResourceKey<? extends Registry<T>> getKey() {
      return this.key;
   }

   public String getDirectory() {
      return this.directory;
   }

   protected void addToCollection(TagContainer.Builder p_144337_) {
      p_144337_.add(this.key, TagCollection.of(this.wrappers.stream().collect(Collectors.toMap(Tag.Named::getName, (p_144332_) -> {
         return p_144332_;
      }))));
   }

   static class Wrapper<T> implements Tag.Named<T> {
      @Nullable
      private Tag<T> tag;
      protected final ResourceLocation name;

      Wrapper(ResourceLocation p_13253_) {
         this.name = p_13253_;
      }

      public ResourceLocation getName() {
         return this.name;
      }

      private Tag<T> resolve() {
         if (this.tag == null) {
            throw new IllegalStateException("Tag " + this.name + " used before it was bound");
         } else {
            return this.tag;
         }
      }

      void rebind(Function<ResourceLocation, Tag<T>> p_13261_) {
         this.tag = p_13261_.apply(this.name);
      }

      public boolean contains(T p_13259_) {
         return this.resolve().contains(p_13259_);
      }

      public List<T> getValues() {
         return this.resolve().getValues();
      }
   }
}