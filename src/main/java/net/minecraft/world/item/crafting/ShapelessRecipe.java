package net.minecraft.world.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe {
   private final ResourceLocation id;
   final String group;
   final ItemStack result;
   final NonNullList<Ingredient> ingredients;

   public ShapelessRecipe(ResourceLocation p_44246_, String p_44247_, ItemStack p_44248_, NonNullList<Ingredient> p_44249_) {
      this.id = p_44246_;
      this.group = p_44247_;
      this.result = p_44248_;
      this.ingredients = p_44249_;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHAPELESS_RECIPE;
   }

   public String getGroup() {
      return this.group;
   }

   public ItemStack getResultItem() {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      return this.ingredients;
   }

   public boolean matches(CraftingContainer p_44262_, Level p_44263_) {
      StackedContents stackedcontents = new StackedContents();
      int i = 0;

      for(int j = 0; j < p_44262_.getContainerSize(); ++j) {
         ItemStack itemstack = p_44262_.getItem(j);
         if (!itemstack.isEmpty()) {
            ++i;
            stackedcontents.accountStack(itemstack, 1);
         }
      }

      return i == this.ingredients.size() && stackedcontents.canCraft(this, (IntList)null);
   }

   public ItemStack assemble(CraftingContainer p_44260_) {
      return this.result.copy();
   }

   public boolean canCraftInDimensions(int p_44252_, int p_44253_) {
      return p_44252_ * p_44253_ >= this.ingredients.size();
   }

   public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
      public ShapelessRecipe fromJson(ResourceLocation p_44290_, JsonObject p_44291_) {
         String s = GsonHelper.getAsString(p_44291_, "group", "");
         NonNullList<Ingredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(p_44291_, "ingredients"));
         if (nonnulllist.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
         } else if (nonnulllist.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
         } else {
            ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(p_44291_, "result"));
            return new ShapelessRecipe(p_44290_, s, itemstack, nonnulllist);
         }
      }

      private static NonNullList<Ingredient> itemsFromJson(JsonArray p_44276_) {
         NonNullList<Ingredient> nonnulllist = NonNullList.create();

         for(int i = 0; i < p_44276_.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(p_44276_.get(i));
            if (!ingredient.isEmpty()) {
               nonnulllist.add(ingredient);
            }
         }

         return nonnulllist;
      }

      public ShapelessRecipe fromNetwork(ResourceLocation p_44293_, FriendlyByteBuf p_44294_) {
         String s = p_44294_.readUtf();
         int i = p_44294_.readVarInt();
         NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

         for(int j = 0; j < nonnulllist.size(); ++j) {
            nonnulllist.set(j, Ingredient.fromNetwork(p_44294_));
         }

         ItemStack itemstack = p_44294_.readItem();
         return new ShapelessRecipe(p_44293_, s, itemstack, nonnulllist);
      }

      public void toNetwork(FriendlyByteBuf p_44281_, ShapelessRecipe p_44282_) {
         p_44281_.writeUtf(p_44282_.group);
         p_44281_.writeVarInt(p_44282_.ingredients.size());

         for(Ingredient ingredient : p_44282_.ingredients) {
            ingredient.toNetwork(p_44281_);
         }

         p_44281_.writeItem(p_44282_.result);
      }
   }
}