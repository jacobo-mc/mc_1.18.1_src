package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry extends LootPoolSingletonContainer {
   final Tag<Item> tag;
   final boolean expand;

   TagEntry(Tag<Item> p_79824_, boolean p_79825_, int p_79826_, int p_79827_, LootItemCondition[] p_79828_, LootItemFunction[] p_79829_) {
      super(p_79826_, p_79827_, p_79828_, p_79829_);
      this.tag = p_79824_;
      this.expand = p_79825_;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.TAG;
   }

   public void createItemStack(Consumer<ItemStack> p_79854_, LootContext p_79855_) {
      this.tag.getValues().forEach((p_79852_) -> {
         p_79854_.accept(new ItemStack(p_79852_));
      });
   }

   private boolean expandTag(LootContext p_79846_, Consumer<LootPoolEntry> p_79847_) {
      if (!this.canRun(p_79846_)) {
         return false;
      } else {
         for(final Item item : this.tag.getValues()) {
            p_79847_.accept(new LootPoolSingletonContainer.EntryBase() {
               public void createItemStack(Consumer<ItemStack> p_79869_, LootContext p_79870_) {
                  p_79869_.accept(new ItemStack(item));
               }
            });
         }

         return true;
      }
   }

   public boolean expand(LootContext p_79861_, Consumer<LootPoolEntry> p_79862_) {
      return this.expand ? this.expandTag(p_79861_, p_79862_) : super.expand(p_79861_, p_79862_);
   }

   public static LootPoolSingletonContainer.Builder<?> tagContents(Tag<Item> p_165163_) {
      return simpleBuilder((p_165166_, p_165167_, p_165168_, p_165169_) -> {
         return new TagEntry(p_165163_, false, p_165166_, p_165167_, p_165168_, p_165169_);
      });
   }

   public static LootPoolSingletonContainer.Builder<?> expandTag(Tag<Item> p_79857_) {
      return simpleBuilder((p_79841_, p_79842_, p_79843_, p_79844_) -> {
         return new TagEntry(p_79857_, true, p_79841_, p_79842_, p_79843_, p_79844_);
      });
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<TagEntry> {
      public void serializeCustom(JsonObject p_79888_, TagEntry p_79889_, JsonSerializationContext p_79890_) {
         super.serializeCustom(p_79888_, p_79889_, p_79890_);
         p_79888_.addProperty("name", SerializationTags.getInstance().getIdOrThrow(Registry.ITEM_REGISTRY, p_79889_.tag, () -> {
            return new IllegalStateException("Unknown item tag");
         }).toString());
         p_79888_.addProperty("expand", p_79889_.expand);
      }

      protected TagEntry deserialize(JsonObject p_79873_, JsonDeserializationContext p_79874_, int p_79875_, int p_79876_, LootItemCondition[] p_79877_, LootItemFunction[] p_79878_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_79873_, "name"));
         Tag<Item> tag = SerializationTags.getInstance().getTagOrThrow(Registry.ITEM_REGISTRY, resourcelocation, (p_165172_) -> {
            return new JsonParseException("Can't find tag: " + p_165172_);
         });
         boolean flag = GsonHelper.getAsBoolean(p_79873_, "expand");
         return new TagEntry(tag, flag, p_79875_, p_79876_, p_79877_, p_79878_);
      }
   }
}