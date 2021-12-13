package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceLocationArgument implements ArgumentType<ResourceLocation> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType((p_107010_) -> {
      return new TranslatableComponent("advancement.advancementNotFound", p_107010_);
   });
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType((p_107005_) -> {
      return new TranslatableComponent("recipe.notFound", p_107005_);
   });
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType((p_106998_) -> {
      return new TranslatableComponent("predicate.unknown", p_106998_);
   });
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_ATTRIBUTE = new DynamicCommandExceptionType((p_106991_) -> {
      return new TranslatableComponent("attribute.unknown", p_106991_);
   });
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM_MODIFIER = new DynamicCommandExceptionType((p_171026_) -> {
      return new TranslatableComponent("item_modifier.unknown", p_171026_);
   });

   public static ResourceLocationArgument id() {
      return new ResourceLocationArgument();
   }

   public static Advancement getAdvancement(CommandContext<CommandSourceStack> p_106988_, String p_106989_) throws CommandSyntaxException {
      ResourceLocation resourcelocation = p_106988_.getArgument(p_106989_, ResourceLocation.class);
      Advancement advancement = p_106988_.getSource().getServer().getAdvancements().getAdvancement(resourcelocation);
      if (advancement == null) {
         throw ERROR_UNKNOWN_ADVANCEMENT.create(resourcelocation);
      } else {
         return advancement;
      }
   }

   public static Recipe<?> getRecipe(CommandContext<CommandSourceStack> p_106995_, String p_106996_) throws CommandSyntaxException {
      RecipeManager recipemanager = p_106995_.getSource().getServer().getRecipeManager();
      ResourceLocation resourcelocation = p_106995_.getArgument(p_106996_, ResourceLocation.class);
      return recipemanager.byKey(resourcelocation).orElseThrow(() -> {
         return ERROR_UNKNOWN_RECIPE.create(resourcelocation);
      });
   }

   public static LootItemCondition getPredicate(CommandContext<CommandSourceStack> p_107002_, String p_107003_) throws CommandSyntaxException {
      ResourceLocation resourcelocation = p_107002_.getArgument(p_107003_, ResourceLocation.class);
      PredicateManager predicatemanager = p_107002_.getSource().getServer().getPredicateManager();
      LootItemCondition lootitemcondition = predicatemanager.get(resourcelocation);
      if (lootitemcondition == null) {
         throw ERROR_UNKNOWN_PREDICATE.create(resourcelocation);
      } else {
         return lootitemcondition;
      }
   }

   public static LootItemFunction getItemModifier(CommandContext<CommandSourceStack> p_171032_, String p_171033_) throws CommandSyntaxException {
      ResourceLocation resourcelocation = p_171032_.getArgument(p_171033_, ResourceLocation.class);
      ItemModifierManager itemmodifiermanager = p_171032_.getSource().getServer().getItemModifierManager();
      LootItemFunction lootitemfunction = itemmodifiermanager.get(resourcelocation);
      if (lootitemfunction == null) {
         throw ERROR_UNKNOWN_ITEM_MODIFIER.create(resourcelocation);
      } else {
         return lootitemfunction;
      }
   }

   public static Attribute getAttribute(CommandContext<CommandSourceStack> p_107007_, String p_107008_) throws CommandSyntaxException {
      ResourceLocation resourcelocation = p_107007_.getArgument(p_107008_, ResourceLocation.class);
      return Registry.ATTRIBUTE.getOptional(resourcelocation).orElseThrow(() -> {
         return ERROR_UNKNOWN_ATTRIBUTE.create(resourcelocation);
      });
   }

   public static ResourceLocation getId(CommandContext<CommandSourceStack> p_107012_, String p_107013_) {
      return p_107012_.getArgument(p_107013_, ResourceLocation.class);
   }

   public ResourceLocation parse(StringReader p_106986_) throws CommandSyntaxException {
      return ResourceLocation.read(p_106986_);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}