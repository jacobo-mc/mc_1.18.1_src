package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestServer extends MinecraftServer {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int PROGRESS_REPORT_INTERVAL = 20;
   private final List<GameTestBatch> testBatches;
   private final BlockPos spawnPos;
   private static final GameRules TEST_GAME_RULES = Util.make(new GameRules(), (p_177615_) -> {
      p_177615_.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, (MinecraftServer)null);
      p_177615_.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, (MinecraftServer)null);
   });
   private static final LevelSettings TEST_SETTINGS = new LevelSettings("Test Level", GameType.CREATIVE, false, Difficulty.NORMAL, true, TEST_GAME_RULES, DataPackConfig.DEFAULT);
   @Nullable
   private MultipleTestTracker testTracker;

   public GameTestServer(Thread p_177594_, LevelStorageSource.LevelStorageAccess p_177595_, PackRepository p_177596_, ServerResources p_177597_, Collection<GameTestBatch> p_177598_, BlockPos p_177599_, RegistryAccess.RegistryHolder p_177600_) {
      this(p_177594_, p_177595_, p_177596_, p_177597_, p_177598_, p_177599_, p_177600_, p_177600_.registryOrThrow(Registry.BIOME_REGISTRY), p_177600_.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY));
   }

   private GameTestServer(Thread p_177602_, LevelStorageSource.LevelStorageAccess p_177603_, PackRepository p_177604_, ServerResources p_177605_, Collection<GameTestBatch> p_177606_, BlockPos p_177607_, RegistryAccess.RegistryHolder p_177608_, Registry<Biome> p_177609_, Registry<DimensionType> p_177610_) {
      super(p_177602_, p_177608_, p_177603_, new PrimaryLevelData(TEST_SETTINGS, new WorldGenSettings(0L, false, false, WorldGenSettings.withOverworld(p_177610_, DimensionType.defaultDimensions(p_177608_, 0L), new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(p_177609_)))), Lifecycle.stable()), p_177604_, Proxy.NO_PROXY, DataFixers.getDataFixer(), p_177605_, (MinecraftSessionService)null, (GameProfileRepository)null, (GameProfileCache)null, LoggerChunkProgressListener::new);
      this.testBatches = Lists.newArrayList(p_177606_);
      this.spawnPos = p_177607_;
      if (p_177606_.isEmpty()) {
         throw new IllegalArgumentException("No test batches were given!");
      }
   }

   public boolean initServer() {
      this.setPlayerList(new PlayerList(this, this.registryHolder, this.playerDataStorage, 1) {
      });
      this.loadLevel();
      ServerLevel serverlevel = this.overworld();
      serverlevel.setDefaultSpawnPos(this.spawnPos, 0.0F);
      int i = 20000000;
      serverlevel.setWeatherParameters(20000000, 20000000, false, false);
      return true;
   }

   public void tickServer(BooleanSupplier p_177619_) {
      super.tickServer(p_177619_);
      ServerLevel serverlevel = this.overworld();
      if (!this.haveTestsStarted()) {
         this.startTests(serverlevel);
      }

      if (serverlevel.getGameTime() % 20L == 0L) {
         LOGGER.info(this.testTracker.getProgressBar());
      }

      if (this.testTracker.isDone()) {
         this.halt(false);
         LOGGER.info(this.testTracker.getProgressBar());
         GlobalTestReporter.finish();
         LOGGER.info("========= {} GAME TESTS COMPLETE ======================", (int)this.testTracker.getTotalCount());
         if (this.testTracker.hasFailedRequired()) {
            LOGGER.info("{} required tests failed :(", (int)this.testTracker.getFailedRequiredCount());
            this.testTracker.getFailedRequired().forEach((p_177627_) -> {
               LOGGER.info("   - {}", (Object)p_177627_.getTestName());
            });
         } else {
            LOGGER.info("All {} required tests passed :)", (int)this.testTracker.getTotalCount());
         }

         if (this.testTracker.hasFailedOptional()) {
            LOGGER.info("{} optional tests failed", (int)this.testTracker.getFailedOptionalCount());
            this.testTracker.getFailedOptional().forEach((p_177621_) -> {
               LOGGER.info("   - {}", (Object)p_177621_.getTestName());
            });
         }

         LOGGER.info("====================================================");
      }

   }

   public SystemReport fillServerSystemReport(SystemReport p_177613_) {
      p_177613_.setDetail("Type", "Game test server");
      return p_177613_;
   }

   public void onServerExit() {
      super.onServerExit();
      System.exit(this.testTracker.getFailedRequiredCount());
   }

   public void onServerCrash(CrashReport p_177623_) {
      System.exit(1);
   }

   private void startTests(ServerLevel p_177625_) {
      Collection<GameTestInfo> collection = GameTestRunner.runTestBatches(this.testBatches, new BlockPos(0, -60, 0), Rotation.NONE, p_177625_, GameTestTicker.SINGLETON, 8);
      this.testTracker = new MultipleTestTracker(collection);
      LOGGER.info("{} tests are now running!", (int)this.testTracker.getTotalCount());
   }

   private boolean haveTestsStarted() {
      return this.testTracker != null;
   }

   public boolean isHardcore() {
      return false;
   }

   public int getOperatorUserPermissionLevel() {
      return 0;
   }

   public int getFunctionCompilationLevel() {
      return 4;
   }

   public boolean shouldRconBroadcast() {
      return false;
   }

   public boolean isDedicatedServer() {
      return false;
   }

   public int getRateLimitPacketsPerSecond() {
      return 0;
   }

   public boolean isEpollEnabled() {
      return false;
   }

   public boolean isCommandBlockEnabled() {
      return true;
   }

   public boolean isPublished() {
      return false;
   }

   public boolean shouldInformAdmins() {
      return false;
   }

   public boolean isSingleplayerOwner(GameProfile p_177617_) {
      return false;
   }
}