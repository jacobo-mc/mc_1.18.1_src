package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class LocateCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.locate.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> p_137859_) {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("locate").requires((p_137861_) -> {
         return p_137861_.hasPermission(2);
      });

      for(Entry<String, StructureFeature<?>> entry : StructureFeature.STRUCTURES_REGISTRY.entrySet()) {
         literalargumentbuilder = literalargumentbuilder.then(Commands.literal(entry.getKey()).executes((p_137876_) -> {
            return locate(p_137876_.getSource(), entry.getValue());
         }));
      }

      p_137859_.register(literalargumentbuilder);
   }

   private static int locate(CommandSourceStack p_137863_, StructureFeature<?> p_137864_) throws CommandSyntaxException {
      BlockPos blockpos = new BlockPos(p_137863_.getPosition());
      BlockPos blockpos1 = p_137863_.getLevel().findNearestMapFeature(p_137864_, blockpos, 100, false);
      if (blockpos1 == null) {
         throw ERROR_FAILED.create();
      } else {
         return showLocateResult(p_137863_, p_137864_.getFeatureName(), blockpos, blockpos1, "commands.locate.success");
      }
   }

   public static int showLocateResult(CommandSourceStack p_137866_, String p_137867_, BlockPos p_137868_, BlockPos p_137869_, String p_137870_) {
      int i = Mth.floor(dist(p_137868_.getX(), p_137868_.getZ(), p_137869_.getX(), p_137869_.getZ()));
      Component component = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", p_137869_.getX(), "~", p_137869_.getZ())).withStyle((p_137873_) -> {
         return p_137873_.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + p_137869_.getX() + " ~ " + p_137869_.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip")));
      });
      p_137866_.sendSuccess(new TranslatableComponent(p_137870_, p_137867_, component, i), false);
      return i;
   }

   private static float dist(int p_137854_, int p_137855_, int p_137856_, int p_137857_) {
      int i = p_137856_ - p_137854_;
      int j = p_137857_ - p_137855_;
      return Mth.sqrt((float)(i * i + j * j));
   }
}