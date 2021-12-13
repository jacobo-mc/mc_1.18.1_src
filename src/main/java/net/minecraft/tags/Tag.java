package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public interface Tag<T> {
   static <T> Codec<Tag<T>> codec(Supplier<TagCollection<T>> p_13291_) {
      return ResourceLocation.CODEC.flatXmap((p_13297_) -> {
         return Optional.ofNullable(p_13291_.get().getTag(p_13297_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown tag: " + p_13297_);
         });
      }, (p_13294_) -> {
         return Optional.ofNullable(p_13291_.get().getId(p_13294_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown tag: " + p_13294_);
         });
      });
   }

   boolean contains(T p_13287_);

   List<T> getValues();

   default T getRandomElement(Random p_13289_) {
      List<T> list = this.getValues();
      return list.get(p_13289_.nextInt(list.size()));
   }

   static <T> Tag<T> fromSet(Set<T> p_13301_) {
      return SetTag.create(p_13301_);
   }

   public static class Builder {
      private final List<Tag.BuilderEntry> entries = Lists.newArrayList();

      public static Tag.Builder tag() {
         return new Tag.Builder();
      }

      public Tag.Builder add(Tag.BuilderEntry p_13306_) {
         this.entries.add(p_13306_);
         return this;
      }

      public Tag.Builder add(Tag.Entry p_13308_, String p_13309_) {
         return this.add(new Tag.BuilderEntry(p_13308_, p_13309_));
      }

      public Tag.Builder addElement(ResourceLocation p_13328_, String p_13329_) {
         return this.add(new Tag.ElementEntry(p_13328_), p_13329_);
      }

      public Tag.Builder addOptionalElement(ResourceLocation p_144380_, String p_144381_) {
         return this.add(new Tag.OptionalElementEntry(p_144380_), p_144381_);
      }

      public Tag.Builder addTag(ResourceLocation p_13336_, String p_13337_) {
         return this.add(new Tag.TagEntry(p_13336_), p_13337_);
      }

      public Tag.Builder addOptionalTag(ResourceLocation p_144383_, String p_144384_) {
         return this.add(new Tag.OptionalTagEntry(p_144383_), p_144384_);
      }

      public <T> Either<Collection<Tag.BuilderEntry>, Tag<T>> build(Function<ResourceLocation, Tag<T>> p_144372_, Function<ResourceLocation, T> p_144373_) {
         ImmutableSet.Builder<T> builder = ImmutableSet.builder();
         List<Tag.BuilderEntry> list = Lists.newArrayList();

         for(Tag.BuilderEntry tag$builderentry : this.entries) {
            if (!tag$builderentry.getEntry().build(p_144372_, p_144373_, builder::add)) {
               list.add(tag$builderentry);
            }
         }

         return list.isEmpty() ? Either.right(Tag.fromSet(builder.build())) : Either.left(list);
      }

      public Stream<Tag.BuilderEntry> getEntries() {
         return this.entries.stream();
      }

      public void visitRequiredDependencies(Consumer<ResourceLocation> p_144367_) {
         this.entries.forEach((p_144378_) -> {
            p_144378_.entry.visitRequiredDependencies(p_144367_);
         });
      }

      public void visitOptionalDependencies(Consumer<ResourceLocation> p_144375_) {
         this.entries.forEach((p_144370_) -> {
            p_144370_.entry.visitOptionalDependencies(p_144375_);
         });
      }

      public Tag.Builder addFromJson(JsonObject p_13313_, String p_13314_) {
         JsonArray jsonarray = GsonHelper.getAsJsonArray(p_13313_, "values");
         List<Tag.Entry> list = Lists.newArrayList();

         for(JsonElement jsonelement : jsonarray) {
            list.add(parseEntry(jsonelement));
         }

         if (GsonHelper.getAsBoolean(p_13313_, "replace", false)) {
            this.entries.clear();
         }

         list.forEach((p_13319_) -> {
            this.entries.add(new Tag.BuilderEntry(p_13319_, p_13314_));
         });
         return this;
      }

      private static Tag.Entry parseEntry(JsonElement p_13311_) {
         String s;
         boolean flag;
         if (p_13311_.isJsonObject()) {
            JsonObject jsonobject = p_13311_.getAsJsonObject();
            s = GsonHelper.getAsString(jsonobject, "id");
            flag = GsonHelper.getAsBoolean(jsonobject, "required", true);
         } else {
            s = GsonHelper.convertToString(p_13311_, "id");
            flag = true;
         }

         if (s.startsWith("#")) {
            ResourceLocation resourcelocation1 = new ResourceLocation(s.substring(1));
            return (Tag.Entry)(flag ? new Tag.TagEntry(resourcelocation1) : new Tag.OptionalTagEntry(resourcelocation1));
         } else {
            ResourceLocation resourcelocation = new ResourceLocation(s);
            return (Tag.Entry)(flag ? new Tag.ElementEntry(resourcelocation) : new Tag.OptionalElementEntry(resourcelocation));
         }
      }

      public JsonObject serializeToJson() {
         JsonObject jsonobject = new JsonObject();
         JsonArray jsonarray = new JsonArray();

         for(Tag.BuilderEntry tag$builderentry : this.entries) {
            tag$builderentry.getEntry().serializeTo(jsonarray);
         }

         jsonobject.addProperty("replace", false);
         jsonobject.add("values", jsonarray);
         return jsonobject;
      }
   }

   public static class BuilderEntry {
      final Tag.Entry entry;
      private final String source;

      BuilderEntry(Tag.Entry p_13341_, String p_13342_) {
         this.entry = p_13341_;
         this.source = p_13342_;
      }

      public Tag.Entry getEntry() {
         return this.entry;
      }

      public String getSource() {
         return this.source;
      }

      public String toString() {
         return this.entry + " (from " + this.source + ")";
      }
   }

   public static class ElementEntry implements Tag.Entry {
      private final ResourceLocation id;

      public ElementEntry(ResourceLocation p_13351_) {
         this.id = p_13351_;
      }

      public <T> boolean build(Function<ResourceLocation, Tag<T>> p_13355_, Function<ResourceLocation, T> p_13356_, Consumer<T> p_13357_) {
         T t = p_13356_.apply(this.id);
         if (t == null) {
            return false;
         } else {
            p_13357_.accept(t);
            return true;
         }
      }

      public void serializeTo(JsonArray p_13353_) {
         p_13353_.add(this.id.toString());
      }

      public boolean verifyIfPresent(Predicate<ResourceLocation> p_144387_, Predicate<ResourceLocation> p_144388_) {
         return p_144387_.test(this.id);
      }

      public String toString() {
         return this.id.toString();
      }
   }

   public interface Entry {
      <T> boolean build(Function<ResourceLocation, Tag<T>> p_13360_, Function<ResourceLocation, T> p_13361_, Consumer<T> p_13362_);

      void serializeTo(JsonArray p_13359_);

      default void visitRequiredDependencies(Consumer<ResourceLocation> p_144389_) {
      }

      default void visitOptionalDependencies(Consumer<ResourceLocation> p_144392_) {
      }

      boolean verifyIfPresent(Predicate<ResourceLocation> p_144390_, Predicate<ResourceLocation> p_144391_);
   }

   public interface Named<T> extends Tag<T> {
      ResourceLocation getName();
   }

   public static class OptionalElementEntry implements Tag.Entry {
      private final ResourceLocation id;

      public OptionalElementEntry(ResourceLocation p_13365_) {
         this.id = p_13365_;
      }

      public <T> boolean build(Function<ResourceLocation, Tag<T>> p_13369_, Function<ResourceLocation, T> p_13370_, Consumer<T> p_13371_) {
         T t = p_13370_.apply(this.id);
         if (t != null) {
            p_13371_.accept(t);
         }

         return true;
      }

      public void serializeTo(JsonArray p_13367_) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("id", this.id.toString());
         jsonobject.addProperty("required", false);
         p_13367_.add(jsonobject);
      }

      public boolean verifyIfPresent(Predicate<ResourceLocation> p_144394_, Predicate<ResourceLocation> p_144395_) {
         return true;
      }

      public String toString() {
         return this.id + "?";
      }
   }

   public static class OptionalTagEntry implements Tag.Entry {
      private final ResourceLocation id;

      public OptionalTagEntry(ResourceLocation p_13375_) {
         this.id = p_13375_;
      }

      public <T> boolean build(Function<ResourceLocation, Tag<T>> p_13379_, Function<ResourceLocation, T> p_13380_, Consumer<T> p_13381_) {
         Tag<T> tag = p_13379_.apply(this.id);
         if (tag != null) {
            tag.getValues().forEach(p_13381_);
         }

         return true;
      }

      public void serializeTo(JsonArray p_13377_) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("id", "#" + this.id);
         jsonobject.addProperty("required", false);
         p_13377_.add(jsonobject);
      }

      public String toString() {
         return "#" + this.id + "?";
      }

      public void visitOptionalDependencies(Consumer<ResourceLocation> p_144400_) {
         p_144400_.accept(this.id);
      }

      public boolean verifyIfPresent(Predicate<ResourceLocation> p_144397_, Predicate<ResourceLocation> p_144398_) {
         return true;
      }
   }

   public static class TagEntry implements Tag.Entry {
      private final ResourceLocation id;

      public TagEntry(ResourceLocation p_13385_) {
         this.id = p_13385_;
      }

      public <T> boolean build(Function<ResourceLocation, Tag<T>> p_13389_, Function<ResourceLocation, T> p_13390_, Consumer<T> p_13391_) {
         Tag<T> tag = p_13389_.apply(this.id);
         if (tag == null) {
            return false;
         } else {
            tag.getValues().forEach(p_13391_);
            return true;
         }
      }

      public void serializeTo(JsonArray p_13387_) {
         p_13387_.add("#" + this.id);
      }

      public String toString() {
         return "#" + this.id;
      }

      public boolean verifyIfPresent(Predicate<ResourceLocation> p_144404_, Predicate<ResourceLocation> p_144405_) {
         return p_144405_.test(this.id);
      }

      public void visitRequiredDependencies(Consumer<ResourceLocation> p_144402_) {
         p_144402_.accept(this.id);
      }
   }
}