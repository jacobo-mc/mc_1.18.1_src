package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

@OnlyIn(Dist.CLIENT)
public class WorldGenSettingsComponent implements Widget {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
   private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
   private static final Component MAP_FEATURES_INFO = new TranslatableComponent("selectWorld.mapFeatures.info");
   private static final Component SELECT_FILE_PROMPT = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
   private MultiLineLabel amplifiedWorldInfo = MultiLineLabel.EMPTY;
   private Font font;
   private int width;
   private EditBox seedEdit;
   private CycleButton<Boolean> featuresButton;
   private CycleButton<Boolean> bonusItemsButton;
   private CycleButton<WorldPreset> typeButton;
   private Button customWorldDummyButton;
   private Button customizeTypeButton;
   private Button importSettingsButton;
   private RegistryAccess.RegistryHolder registryHolder;
   private WorldGenSettings settings;
   private Optional<WorldPreset> preset;
   private OptionalLong seed;

   public WorldGenSettingsComponent(RegistryAccess.RegistryHolder p_101399_, WorldGenSettings p_101400_, Optional<WorldPreset> p_101401_, OptionalLong p_101402_) {
      this.registryHolder = p_101399_;
      this.settings = p_101400_;
      this.preset = p_101401_;
      this.seed = p_101402_;
   }

   public void init(CreateWorldScreen p_101430_, Minecraft p_101431_, Font p_101432_) {
      this.font = p_101432_;
      this.width = p_101430_.width;
      this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
      this.seedEdit.setValue(toString(this.seed));
      this.seedEdit.setResponder((p_101465_) -> {
         this.seed = this.parseSeed();
      });
      p_101430_.addWidget(this.seedEdit);
      int i = this.width / 2 - 155;
      int j = this.width / 2 + 5;
      this.featuresButton = p_101430_.addRenderableWidget(CycleButton.onOffBuilder(this.settings.generateFeatures()).withCustomNarration((p_170280_) -> {
         return CommonComponents.joinForNarration(p_170280_.createDefaultNarrationMessage(), new TranslatableComponent("selectWorld.mapFeatures.info"));
      }).create(i, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), (p_170282_, p_170283_) -> {
         this.settings = this.settings.withFeaturesToggled();
      }));
      this.featuresButton.visible = false;
      this.typeButton = p_101430_.addRenderableWidget(CycleButton.builder(WorldPreset::description).withValues(WorldPreset.PRESETS.stream().filter(WorldPreset::isVisibleByDefault).collect(Collectors.toList()), WorldPreset.PRESETS).withCustomNarration((p_170264_) -> {
         return p_170264_.getValue() == WorldPreset.AMPLIFIED ? CommonComponents.joinForNarration(p_170264_.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT) : p_170264_.createDefaultNarrationMessage();
      }).create(j, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), (p_170274_, p_170275_) -> {
         this.preset = Optional.of(p_170275_);
         this.settings = p_170275_.create(this.registryHolder, this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest());
         p_101430_.refreshWorldGenSettingsVisibility();
      }));
      this.preset.ifPresent(this.typeButton::setValue);
      this.typeButton.visible = false;
      this.customWorldDummyButton = p_101430_.addRenderableWidget(new Button(j, 100, 150, 20, CommonComponents.optionNameValue(new TranslatableComponent("selectWorld.mapType"), CUSTOM_WORLD_DESCRIPTION), (p_170262_) -> {
      }));
      this.customWorldDummyButton.active = false;
      this.customWorldDummyButton.visible = false;
      this.customizeTypeButton = p_101430_.addRenderableWidget(new Button(j, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), (p_170248_) -> {
         WorldPreset.PresetEditor worldpreset$preseteditor = WorldPreset.EDITORS.get(this.preset);
         if (worldpreset$preseteditor != null) {
            p_101431_.setScreen(worldpreset$preseteditor.createEditScreen(p_101430_, this.settings));
         }

      }));
      this.customizeTypeButton.visible = false;
      this.bonusItemsButton = p_101430_.addRenderableWidget(CycleButton.onOffBuilder(this.settings.generateBonusChest() && !p_101430_.hardCore).create(i, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), (p_170266_, p_170267_) -> {
         this.settings = this.settings.withBonusChestToggled();
      }));
      this.bonusItemsButton.visible = false;
      this.importSettingsButton = p_101430_.addRenderableWidget(new Button(i, 185, 150, 20, new TranslatableComponent("selectWorld.import_worldgen_settings"), (p_170271_) -> {
         String s = TinyFileDialogs.tinyfd_openFileDialog(SELECT_FILE_PROMPT.getString(), (CharSequence)null, (PointerBuffer)null, (CharSequence)null, false);
         if (s != null) {
            RegistryAccess.RegistryHolder registryaccess$registryholder = RegistryAccess.builtin();
            PackRepository packrepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(p_101430_.getTempDataPackDir().toFile(), PackSource.WORLD));

            ServerResources serverresources;
            try {
               MinecraftServer.configurePackRepository(packrepository, p_101430_.dataPacks, false);
               CompletableFuture<ServerResources> completablefuture = ServerResources.loadResources(packrepository.openAllSelected(), registryaccess$registryholder, Commands.CommandSelection.INTEGRATED, 2, Util.backgroundExecutor(), p_101431_);
               p_101431_.managedBlock(completablefuture::isDone);
               serverresources = completablefuture.get();
            } catch (ExecutionException | InterruptedException interruptedexception) {
               LOGGER.error("Error loading data packs when importing world settings", (Throwable)interruptedexception);
               Component component = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
               Component component1 = new TextComponent(interruptedexception.getMessage());
               p_101431_.getToasts().addToast(SystemToast.multiline(p_101431_, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component, component1));
               packrepository.close();
               return;
            }

            RegistryReadOps<JsonElement> registryreadops = RegistryReadOps.createAndLoad(JsonOps.INSTANCE, serverresources.getResourceManager(), registryaccess$registryholder);
            JsonParser jsonparser = new JsonParser();

            DataResult<WorldGenSettings> dataresult;
            try {
               BufferedReader bufferedreader = Files.newBufferedReader(Paths.get(s));

               try {
                  JsonElement jsonelement = jsonparser.parse(bufferedreader);
                  dataresult = WorldGenSettings.CODEC.parse(registryreadops, jsonelement);
               } catch (Throwable throwable1) {
                  if (bufferedreader != null) {
                     try {
                        bufferedreader.close();
                     } catch (Throwable throwable) {
                        throwable1.addSuppressed(throwable);
                     }
                  }

                  throw throwable1;
               }

               if (bufferedreader != null) {
                  bufferedreader.close();
               }
            } catch (JsonIOException | JsonSyntaxException | IOException ioexception) {
               dataresult = DataResult.error("Failed to parse file: " + ioexception.getMessage());
            }

            if (dataresult.error().isPresent()) {
               Component component3 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
               String s1 = dataresult.error().get().message();
               LOGGER.error("Error parsing world settings: {}", (Object)s1);
               Component component2 = new TextComponent(s1);
               p_101431_.getToasts().addToast(SystemToast.multiline(p_101431_, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component3, component2));
            }

            serverresources.close();
            Lifecycle lifecycle = dataresult.lifecycle();
            dataresult.resultOrPartial(LOGGER::error).ifPresent((p_170254_) -> {
               BooleanConsumer booleanconsumer = (p_170260_) -> {
                  p_101431_.setScreen(p_101430_);
                  if (p_170260_) {
                     this.importSettings(registryaccess$registryholder, p_170254_);
                  }

               };
               if (lifecycle == Lifecycle.stable()) {
                  this.importSettings(registryaccess$registryholder, p_170254_);
               } else if (lifecycle == Lifecycle.experimental()) {
                  p_101431_.setScreen(new ConfirmScreen(booleanconsumer, new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.title"), new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.question")));
               } else {
                  p_101431_.setScreen(new ConfirmScreen(booleanconsumer, new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.title"), new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.question")));
               }

            });
         }
      }));
      this.importSettingsButton.visible = false;
      this.amplifiedWorldInfo = MultiLineLabel.create(p_101432_, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
   }

   private void importSettings(RegistryAccess.RegistryHolder p_101443_, WorldGenSettings p_101444_) {
      this.registryHolder = p_101443_;
      this.settings = p_101444_;
      this.preset = WorldPreset.of(p_101444_);
      this.selectWorldTypeButton(true);
      this.seed = OptionalLong.of(p_101444_.seed());
      this.seedEdit.setValue(toString(this.seed));
   }

   public void tick() {
      this.seedEdit.tick();
   }

   public void render(PoseStack p_101407_, int p_101408_, int p_101409_, float p_101410_) {
      if (this.featuresButton.visible) {
         this.font.drawShadow(p_101407_, MAP_FEATURES_INFO, (float)(this.width / 2 - 150), 122.0F, -6250336);
      }

      this.seedEdit.render(p_101407_, p_101408_, p_101409_, p_101410_);
      if (this.preset.equals(Optional.of(WorldPreset.AMPLIFIED))) {
         this.amplifiedWorldInfo.renderLeftAligned(p_101407_, this.typeButton.x + 2, this.typeButton.y + 22, 9, 10526880);
      }

   }

   protected void updateSettings(WorldGenSettings p_101405_) {
      this.settings = p_101405_;
   }

   private static String toString(OptionalLong p_101448_) {
      return p_101448_.isPresent() ? Long.toString(p_101448_.getAsLong()) : "";
   }

   private static OptionalLong parseLong(String p_101446_) {
      try {
         return OptionalLong.of(Long.parseLong(p_101446_));
      } catch (NumberFormatException numberformatexception) {
         return OptionalLong.empty();
      }
   }

   public WorldGenSettings makeSettings(boolean p_101455_) {
      OptionalLong optionallong = this.parseSeed();
      return this.settings.withSeed(p_101455_, optionallong);
   }

   private OptionalLong parseSeed() {
      String s = this.seedEdit.getValue();
      OptionalLong optionallong;
      if (StringUtils.isEmpty(s)) {
         optionallong = OptionalLong.empty();
      } else {
         OptionalLong optionallong1 = parseLong(s);
         if (optionallong1.isPresent() && optionallong1.getAsLong() != 0L) {
            optionallong = optionallong1;
         } else {
            optionallong = OptionalLong.of((long)s.hashCode());
         }
      }

      return optionallong;
   }

   public boolean isDebug() {
      return this.settings.isDebug();
   }

   public void setVisibility(boolean p_170288_) {
      this.selectWorldTypeButton(p_170288_);
      if (this.settings.isDebug()) {
         this.featuresButton.visible = false;
         this.bonusItemsButton.visible = false;
         this.customizeTypeButton.visible = false;
         this.importSettingsButton.visible = false;
      } else {
         this.featuresButton.visible = p_170288_;
         this.bonusItemsButton.visible = p_170288_;
         this.customizeTypeButton.visible = p_170288_ && WorldPreset.EDITORS.containsKey(this.preset);
         this.importSettingsButton.visible = p_170288_;
      }

      this.seedEdit.setVisible(p_170288_);
   }

   private void selectWorldTypeButton(boolean p_170290_) {
      if (this.preset.isPresent()) {
         this.typeButton.visible = p_170290_;
         this.customWorldDummyButton.visible = false;
      } else {
         this.typeButton.visible = false;
         this.customWorldDummyButton.visible = p_170290_;
      }

   }

   public RegistryAccess.RegistryHolder registryHolder() {
      return this.registryHolder;
   }

   void updateDataPacks(ServerResources p_101453_) {
      RegistryAccess.RegistryHolder registryaccess$registryholder = RegistryAccess.builtin();
      RegistryWriteOps<JsonElement> registrywriteops = RegistryWriteOps.create(JsonOps.INSTANCE, this.registryHolder);
      RegistryReadOps<JsonElement> registryreadops = RegistryReadOps.createAndLoad(JsonOps.INSTANCE, p_101453_.getResourceManager(), registryaccess$registryholder);
      DataResult<WorldGenSettings> dataresult = WorldGenSettings.CODEC.encodeStart(registrywriteops, this.settings).flatMap((p_170278_) -> {
         return WorldGenSettings.CODEC.parse(registryreadops, p_170278_);
      });
      dataresult.resultOrPartial(Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error)).ifPresent((p_170286_) -> {
         this.settings = p_170286_;
         this.registryHolder = registryaccess$registryholder;
      });
   }

   public void switchToHardcore() {
      this.bonusItemsButton.active = false;
      this.bonusItemsButton.setValue(false);
   }

   public void switchOutOfHardcode() {
      this.bonusItemsButton.active = true;
      this.bonusItemsButton.setValue(this.settings.generateBonusChest());
   }
}