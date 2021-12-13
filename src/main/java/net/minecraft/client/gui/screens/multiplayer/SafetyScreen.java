package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SafetyScreen extends Screen {
   private final Screen previous;
   private static final Component TITLE = (new TranslatableComponent("multiplayerWarning.header")).withStyle(ChatFormatting.BOLD);
   private static final Component CONTENT = new TranslatableComponent("multiplayerWarning.message");
   private static final Component CHECK = new TranslatableComponent("multiplayerWarning.check");
   private static final Component NARRATION = TITLE.copy().append("\n").append(CONTENT);
   private Checkbox stopShowing;
   private MultiLineLabel message = MultiLineLabel.EMPTY;

   public SafetyScreen(Screen p_99743_) {
      super(NarratorChatListener.NO_TITLE);
      this.previous = p_99743_;
   }

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, CONTENT, this.width - 50);
      int i = (this.message.getLineCount() + 1) * 9 * 2;
      this.addRenderableWidget(new Button(this.width / 2 - 155, 100 + i, 150, 20, CommonComponents.GUI_PROCEED, (p_99754_) -> {
         if (this.stopShowing.selected()) {
            this.minecraft.options.skipMultiplayerWarning = true;
            this.minecraft.options.save();
         }

         this.minecraft.setScreen(new JoinMultiplayerScreen(this.previous));
      }));
      this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20, CommonComponents.GUI_BACK, (p_99750_) -> {
         this.minecraft.setScreen(this.previous);
      }));
      this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, CHECK, false);
      this.addRenderableWidget(this.stopShowing);
   }

   public Component getNarrationMessage() {
      return NARRATION;
   }

   public void render(PoseStack p_99745_, int p_99746_, int p_99747_, float p_99748_) {
      this.renderDirtBackground(0);
      drawString(p_99745_, this.font, TITLE, 25, 30, 16777215);
      this.message.renderLeftAligned(p_99745_, 25, 70, 9 * 2, 16777215);
      super.render(p_99745_, p_99746_, p_99747_, p_99748_);
   }
}