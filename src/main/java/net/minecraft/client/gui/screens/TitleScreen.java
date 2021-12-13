package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TitleScreen extends Screen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String DEMO_LEVEL_ID = "Demo_World";
   public static final String COPYRIGHT_TEXT = "Copyright Mojang AB. Do not distribute!";
   public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
   private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
   private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
   private final boolean minceraftEasterEgg;
   @Nullable
   private String splash;
   private Button resetDemoButton;
   private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
   private static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
   private Screen realmsNotificationsScreen;
   private int copyrightWidth;
   private int copyrightX;
   private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
   private final boolean fading;
   private long fadeInStart;

   public TitleScreen() {
      this(false);
   }

   public TitleScreen(boolean p_96733_) {
      super(new TranslatableComponent("narrator.screen.title"));
      this.fading = p_96733_;
      this.minceraftEasterEgg = (double)(new Random()).nextFloat() < 1.0E-4D;
   }

   private boolean realmsNotificationsEnabled() {
      return this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen != null;
   }

   public void tick() {
      if (this.realmsNotificationsEnabled()) {
         this.realmsNotificationsScreen.tick();
      }

   }

   public static CompletableFuture<Void> preloadResources(TextureManager p_96755_, Executor p_96756_) {
      return CompletableFuture.allOf(p_96755_.preload(MINECRAFT_LOGO, p_96756_), p_96755_.preload(MINECRAFT_EDITION, p_96756_), p_96755_.preload(PANORAMA_OVERLAY, p_96756_), CUBE_MAP.preload(p_96755_, p_96756_));
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected void init() {
      if (this.splash == null) {
         this.splash = this.minecraft.getSplashManager().getSplash();
      }

      this.copyrightWidth = this.font.width("Copyright Mojang AB. Do not distribute!");
      this.copyrightX = this.width - this.copyrightWidth - 2;
      int i = 24;
      int j = this.height / 4 + 48;
      if (this.minecraft.isDemo()) {
         this.createDemoMenuOptions(j, 24);
      } else {
         this.createNormalMenuOptions(j, 24);
      }

      this.addRenderableWidget(new ImageButton(this.width / 2 - 124, j + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (p_96791_) -> {
         this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
      }, new TranslatableComponent("narrator.button.language")));
      this.addRenderableWidget(new Button(this.width / 2 - 100, j + 72 + 12, 98, 20, new TranslatableComponent("menu.options"), (p_96788_) -> {
         this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
      }));
      this.addRenderableWidget(new Button(this.width / 2 + 2, j + 72 + 12, 98, 20, new TranslatableComponent("menu.quit"), (p_96786_) -> {
         this.minecraft.stop();
      }));
      this.addRenderableWidget(new ImageButton(this.width / 2 + 104, j + 72 + 12, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURE, 32, 64, (p_96784_) -> {
         this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options));
      }, new TranslatableComponent("narrator.button.accessibility")));
      this.minecraft.setConnectedToRealms(false);
      if (this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen == null) {
         this.realmsNotificationsScreen = new RealmsNotificationsScreen();
      }

      if (this.realmsNotificationsEnabled()) {
         this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
      }

   }

   private void createNormalMenuOptions(int p_96764_, int p_96765_) {
      this.addRenderableWidget(new Button(this.width / 2 - 100, p_96764_, 200, 20, new TranslatableComponent("menu.singleplayer"), (p_96781_) -> {
         this.minecraft.setScreen(new SelectWorldScreen(this));
      }));
      boolean flag = this.minecraft.allowsMultiplayer();
      Button.OnTooltip button$ontooltip = flag ? Button.NO_TOOLTIP : new Button.OnTooltip() {
         private final Component text = new TranslatableComponent("title.multiplayer.disabled");

         public void onTooltip(Button p_169458_, PoseStack p_169459_, int p_169460_, int p_169461_) {
            if (!p_169458_.active) {
               TitleScreen.this.renderTooltip(p_169459_, TitleScreen.this.minecraft.font.split(this.text, Math.max(TitleScreen.this.width / 2 - 43, 170)), p_169460_, p_169461_);
            }

         }

         public void narrateTooltip(Consumer<Component> p_169456_) {
            p_169456_.accept(this.text);
         }
      };
      (this.addRenderableWidget(new Button(this.width / 2 - 100, p_96764_ + p_96765_ * 1, 200, 20, new TranslatableComponent("menu.multiplayer"), (p_169450_) -> {
         Screen screen = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
         this.minecraft.setScreen(screen);
      }, button$ontooltip))).active = flag;
      (this.addRenderableWidget(new Button(this.width / 2 - 100, p_96764_ + p_96765_ * 2, 200, 20, new TranslatableComponent("menu.online"), (p_96776_) -> {
         this.realmsButtonClicked();
      }, button$ontooltip))).active = flag;
   }

   private void createDemoMenuOptions(int p_96773_, int p_96774_) {
      boolean flag = this.checkDemoWorldPresence();
      this.addRenderableWidget(new Button(this.width / 2 - 100, p_96773_, 200, 20, new TranslatableComponent("menu.playdemo"), (p_169444_) -> {
         if (flag) {
            this.minecraft.loadLevel("Demo_World");
         } else {
            RegistryAccess.RegistryHolder registryaccess$registryholder = RegistryAccess.builtin();
            this.minecraft.createLevel("Demo_World", MinecraftServer.DEMO_SETTINGS, registryaccess$registryholder, WorldGenSettings.demoSettings(registryaccess$registryholder));
         }

      }));
      this.resetDemoButton = this.addRenderableWidget(new Button(this.width / 2 - 100, p_96773_ + p_96774_ * 1, 200, 20, new TranslatableComponent("menu.resetdemo"), (p_169441_) -> {
         LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();

         try {
            LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = levelstoragesource.createAccess("Demo_World");

            try {
               LevelSummary levelsummary = levelstoragesource$levelstorageaccess.getSummary();
               if (levelsummary != null) {
                  this.minecraft.setScreen(new ConfirmScreen(this::confirmDemo, new TranslatableComponent("selectWorld.deleteQuestion"), new TranslatableComponent("selectWorld.deleteWarning", levelsummary.getLevelName()), new TranslatableComponent("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
               }
            } catch (Throwable throwable1) {
               if (levelstoragesource$levelstorageaccess != null) {
                  try {
                     levelstoragesource$levelstorageaccess.close();
                  } catch (Throwable throwable) {
                     throwable1.addSuppressed(throwable);
                  }
               }

               throw throwable1;
            }

            if (levelstoragesource$levelstorageaccess != null) {
               levelstoragesource$levelstorageaccess.close();
            }
         } catch (IOException ioexception) {
            SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
            LOGGER.warn("Failed to access demo world", (Throwable)ioexception);
         }

      }));
      this.resetDemoButton.active = flag;
   }

   private boolean checkDemoWorldPresence() {
      try {
         LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.minecraft.getLevelSource().createAccess("Demo_World");

         boolean flag;
         try {
            flag = levelstoragesource$levelstorageaccess.getSummary() != null;
         } catch (Throwable throwable1) {
            if (levelstoragesource$levelstorageaccess != null) {
               try {
                  levelstoragesource$levelstorageaccess.close();
               } catch (Throwable throwable) {
                  throwable1.addSuppressed(throwable);
               }
            }

            throw throwable1;
         }

         if (levelstoragesource$levelstorageaccess != null) {
            levelstoragesource$levelstorageaccess.close();
         }

         return flag;
      } catch (IOException ioexception) {
         SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
         LOGGER.warn("Failed to read demo world data", (Throwable)ioexception);
         return false;
      }
   }

   private void realmsButtonClicked() {
      this.minecraft.setScreen(new RealmsMainScreen(this));
   }

   public void render(PoseStack p_96739_, int p_96740_, int p_96741_, float p_96742_) {
      if (this.fadeInStart == 0L && this.fading) {
         this.fadeInStart = Util.getMillis();
      }

      float f = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
      this.panorama.render(p_96742_, Mth.clamp(f, 0.0F, 1.0F));
      int i = 274;
      int j = this.width / 2 - 137;
      int k = 30;
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(f, 0.0F, 1.0F)) : 1.0F);
      blit(p_96739_, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
      float f1 = this.fading ? Mth.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
      int l = Mth.ceil(f1 * 255.0F) << 24;
      if ((l & -67108864) != 0) {
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.setShaderTexture(0, MINECRAFT_LOGO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f1);
         if (this.minceraftEasterEgg) {
            this.blitOutlineBlack(j, 30, (p_169447_, p_169448_) -> {
               this.blit(p_96739_, p_169447_ + 0, p_169448_, 0, 0, 99, 44);
               this.blit(p_96739_, p_169447_ + 99, p_169448_, 129, 0, 27, 44);
               this.blit(p_96739_, p_169447_ + 99 + 26, p_169448_, 126, 0, 3, 44);
               this.blit(p_96739_, p_169447_ + 99 + 26 + 3, p_169448_, 99, 0, 26, 44);
               this.blit(p_96739_, p_169447_ + 155, p_169448_, 0, 45, 155, 44);
            });
         } else {
            this.blitOutlineBlack(j, 30, (p_96768_, p_96769_) -> {
               this.blit(p_96739_, p_96768_ + 0, p_96769_, 0, 0, 155, 44);
               this.blit(p_96739_, p_96768_ + 155, p_96769_, 0, 45, 155, 44);
            });
         }

         RenderSystem.setShaderTexture(0, MINECRAFT_EDITION);
         blit(p_96739_, j + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
         if (this.splash != null) {
            p_96739_.pushPose();
            p_96739_.translate((double)(this.width / 2 + 90), 70.0D, 0.0D);
            p_96739_.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
            float f2 = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) * 0.1F);
            f2 = f2 * 100.0F / (float)(this.font.width(this.splash) + 32);
            p_96739_.scale(f2, f2, f2);
            drawCenteredString(p_96739_, this.font, this.splash, 0, -8, 16776960 | l);
            p_96739_.popPose();
         }

         String s = "Minecraft " + SharedConstants.getCurrentVersion().getName();
         if (this.minecraft.isDemo()) {
            s = s + " Demo";
         } else {
            s = s + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
         }

         if (Minecraft.checkModStatus().shouldReportAsModified()) {
            s = s + I18n.get("menu.modded");
         }

         drawString(p_96739_, this.font, s, 2, this.height - 10, 16777215 | l);
         drawString(p_96739_, this.font, "Copyright Mojang AB. Do not distribute!", this.copyrightX, this.height - 10, 16777215 | l);
         if (p_96740_ > this.copyrightX && p_96740_ < this.copyrightX + this.copyrightWidth && p_96741_ > this.height - 10 && p_96741_ < this.height) {
            fill(p_96739_, this.copyrightX, this.height - 1, this.copyrightX + this.copyrightWidth, this.height, 16777215 | l);
         }

         for(GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener instanceof AbstractWidget) {
               ((AbstractWidget)guieventlistener).setAlpha(f1);
            }
         }

         super.render(p_96739_, p_96740_, p_96741_, p_96742_);
         if (this.realmsNotificationsEnabled() && f1 >= 1.0F) {
            this.realmsNotificationsScreen.render(p_96739_, p_96740_, p_96741_, p_96742_);
         }

      }
   }

   public boolean mouseClicked(double p_96735_, double p_96736_, int p_96737_) {
      if (super.mouseClicked(p_96735_, p_96736_, p_96737_)) {
         return true;
      } else if (this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(p_96735_, p_96736_, p_96737_)) {
         return true;
      } else {
         if (p_96735_ > (double)this.copyrightX && p_96735_ < (double)(this.copyrightX + this.copyrightWidth) && p_96736_ > (double)(this.height - 10) && p_96736_ < (double)this.height) {
            this.minecraft.setScreen(new WinScreen(false, Runnables.doNothing()));
         }

         return false;
      }
   }

   public void removed() {
      if (this.realmsNotificationsScreen != null) {
         this.realmsNotificationsScreen.removed();
      }

   }

   private void confirmDemo(boolean p_96778_) {
      if (p_96778_) {
         try {
            LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.minecraft.getLevelSource().createAccess("Demo_World");

            try {
               levelstoragesource$levelstorageaccess.deleteLevel();
            } catch (Throwable throwable1) {
               if (levelstoragesource$levelstorageaccess != null) {
                  try {
                     levelstoragesource$levelstorageaccess.close();
                  } catch (Throwable throwable) {
                     throwable1.addSuppressed(throwable);
                  }
               }

               throw throwable1;
            }

            if (levelstoragesource$levelstorageaccess != null) {
               levelstoragesource$levelstorageaccess.close();
            }
         } catch (IOException ioexception) {
            SystemToast.onWorldDeleteFailure(this.minecraft, "Demo_World");
            LOGGER.warn("Failed to delete demo world", (Throwable)ioexception);
         }
      }

      this.minecraft.setScreen(this);
   }
}