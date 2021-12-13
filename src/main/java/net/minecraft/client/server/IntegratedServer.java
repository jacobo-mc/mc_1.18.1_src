package net.minecraft.client.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.stats.Stats;
import net.minecraft.util.ModCheck;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class IntegratedServer extends MinecraftServer {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int MIN_SIM_DISTANCE = 2;
   private final Minecraft minecraft;
   private boolean paused = true;
   private int publishedPort = -1;
   @Nullable
   private GameType publishedGameType;
   @Nullable
   private LanServerPinger lanPinger;
   @Nullable
   private UUID uuid;
   private int previousSimulationDistance = 0;

   public IntegratedServer(Thread p_120022_, Minecraft p_120023_, RegistryAccess.RegistryHolder p_120024_, LevelStorageSource.LevelStorageAccess p_120025_, PackRepository p_120026_, ServerResources p_120027_, WorldData p_120028_, MinecraftSessionService p_120029_, GameProfileRepository p_120030_, GameProfileCache p_120031_, ChunkProgressListenerFactory p_120032_) {
      super(p_120022_, p_120024_, p_120025_, p_120028_, p_120026_, p_120023_.getProxy(), p_120023_.getFixerUpper(), p_120027_, p_120029_, p_120030_, p_120031_, p_120032_);
      this.setSingleplayerName(p_120023_.getUser().getName());
      this.setDemo(p_120023_.isDemo());
      this.setPlayerList(new IntegratedPlayerList(this, this.registryHolder, this.playerDataStorage));
      this.minecraft = p_120023_;
   }

   public boolean initServer() {
      LOGGER.info("Starting integrated minecraft server version {}", (Object)SharedConstants.getCurrentVersion().getName());
      this.setUsesAuthentication(true);
      this.setPvpAllowed(true);
      this.setFlightAllowed(true);
      this.initializeKeyPair();
      this.loadLevel();
      this.setMotd(this.getSingleplayerName() + " - " + this.getWorldData().getLevelName());
      return true;
   }

   public void tickServer(BooleanSupplier p_120049_) {
      boolean flag = this.paused;
      this.paused = Minecraft.getInstance().isPaused();
      ProfilerFiller profilerfiller = this.getProfiler();
      if (!flag && this.paused) {
         profilerfiller.push("autoSave");
         LOGGER.info("Saving and pausing game...");
         this.saveEverything(false, false, false);
         profilerfiller.pop();
      }

      boolean flag1 = Minecraft.getInstance().getConnection() != null;
      if (flag1 && this.paused) {
         this.tickPaused();
      } else {
         super.tickServer(p_120049_);
         int i = Math.max(2, this.minecraft.options.renderDistance);
         if (i != this.getPlayerList().getViewDistance()) {
            LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
            this.getPlayerList().setViewDistance(i);
         }

         int j = Math.max(2, this.minecraft.options.simulationDistance);
         if (j != this.previousSimulationDistance) {
            LOGGER.info("Changing simulation distance to {}, from {}", j, this.previousSimulationDistance);
            this.getPlayerList().setSimulationDistance(j);
            this.previousSimulationDistance = j;
         }

      }
   }

   private void tickPaused() {
      for(ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
         serverplayer.awardStat(Stats.TOTAL_WORLD_TIME);
      }

   }

   public boolean shouldRconBroadcast() {
      return true;
   }

   public boolean shouldInformAdmins() {
      return true;
   }

   public File getServerDirectory() {
      return this.minecraft.gameDirectory;
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

   public void onServerCrash(CrashReport p_120051_) {
      this.minecraft.delayCrash(() -> {
         return p_120051_;
      });
   }

   public SystemReport fillServerSystemReport(SystemReport p_174970_) {
      p_174970_.setDetail("Type", "Integrated Server (map_client.txt)");
      p_174970_.setDetail("Is Modded", () -> {
         return this.getModdedStatus().fullDescription();
      });
      return p_174970_;
   }

   public ModCheck getModdedStatus() {
      return Minecraft.checkModStatus().merge(super.getModdedStatus());
   }

   public boolean publishServer(@Nullable GameType p_120041_, boolean p_120042_, int p_120043_) {
      try {
         this.minecraft.prepareForMultiplayer();
         this.getConnection().startTcpServerListener((InetAddress)null, p_120043_);
         LOGGER.info("Started serving on {}", (int)p_120043_);
         this.publishedPort = p_120043_;
         this.lanPinger = new LanServerPinger(this.getMotd(), "" + p_120043_);
         this.lanPinger.start();
         this.publishedGameType = p_120041_;
         this.getPlayerList().setAllowCheatsForAllPlayers(p_120042_);
         int i = this.getProfilePermissions(this.minecraft.player.getGameProfile());
         this.minecraft.player.setPermissionLevel(i);

         for(ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
            this.getCommands().sendCommands(serverplayer);
         }

         return true;
      } catch (IOException ioexception) {
         return false;
      }
   }

   public void stopServer() {
      super.stopServer();
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public void halt(boolean p_120053_) {
      this.executeBlocking(() -> {
         for(ServerPlayer serverplayer : Lists.newArrayList(this.getPlayerList().getPlayers())) {
            if (!serverplayer.getUUID().equals(this.uuid)) {
               this.getPlayerList().remove(serverplayer);
            }
         }

      });
      super.halt(p_120053_);
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public boolean isPublished() {
      return this.publishedPort > -1;
   }

   public int getPort() {
      return this.publishedPort;
   }

   public void setDefaultGameType(GameType p_120039_) {
      super.setDefaultGameType(p_120039_);
      this.publishedGameType = null;
   }

   public boolean isCommandBlockEnabled() {
      return true;
   }

   public int getOperatorUserPermissionLevel() {
      return 2;
   }

   public int getFunctionCompilationLevel() {
      return 2;
   }

   public void setUUID(UUID p_120047_) {
      this.uuid = p_120047_;
   }

   public boolean isSingleplayerOwner(GameProfile p_120045_) {
      return p_120045_.getName().equalsIgnoreCase(this.getSingleplayerName());
   }

   public int getScaledTrackingDistance(int p_120056_) {
      return (int)(this.minecraft.options.entityDistanceScaling * (float)p_120056_);
   }

   public boolean forceSynchronousWrites() {
      return this.minecraft.options.syncWrites;
   }

   @Nullable
   public GameType getForcedGameType() {
      return this.isPublished() ? MoreObjects.firstNonNull(this.publishedGameType, this.worldData.getGameType()) : null;
   }
}