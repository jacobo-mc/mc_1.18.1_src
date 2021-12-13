package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
   public static final DynamicCommandExceptionType ERROR_INVALID_BIOME = new DynamicCommandExceptionType((p_137850_) -> {
      return new TranslatableComponent("commands.locatebiome.invalid", p_137850_);
   });
   private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType((p_137846_) -> {
      return new TranslatableComponent("commands.locatebiome.notFound", p_137846_);
   });
   private static final int MAX_SEARCH_RADIUS = 6400;
   private static final int SEARCH_STEP = 8;

   public static void register(CommandDispatcher<CommandSourceStack> p_137837_) {
      p_137837_.register(Commands.literal("locatebiome").requires((p_137841_) -> {
         return p_137841_.hasPermission(2);
      }).then(Commands.argument("biome", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_BIOMES).executes((p_137839_) -> {
         return locateBiome(p_137839_.getSource(), p_137839_.getArgument("biome", ResourceLocation.class));
      })));
   }

   private static int locateBiome(CommandSourceStack p_137843_, ResourceLocation p_137844_) throws CommandSyntaxException {
      Biome biome = p_137843_.getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOptional(p_137844_).orElseThrow(() -> {
         return ERROR_INVALID_BIOME.create(p_137844_);
      });
      BlockPos blockpos = new BlockPos(p_137843_.getPosition());
      BlockPos blockpos1 = p_137843_.getLevel().findNearestBiome(biome, blockpos, 6400, 8);
      String s = p_137844_.toString();
      if (blockpos1 == null) {
         throw ERROR_BIOME_NOT_FOUND.create(s);
      } else {
         return LocateCommand.showLocateResult(p_137843_, s, blockpos, blockpos1, "commands.locatebiome.success");
      }
   }
}