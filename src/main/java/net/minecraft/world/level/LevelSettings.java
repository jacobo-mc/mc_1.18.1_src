package net.minecraft.world.level;

import com.mojang.serialization.Dynamic;
import net.minecraft.world.Difficulty;

public final class LevelSettings {
   private final String levelName;
   private final GameType gameType;
   private final boolean hardcore;
   private final Difficulty difficulty;
   private final boolean allowCommands;
   private final GameRules gameRules;
   private final DataPackConfig dataPackConfig;

   public LevelSettings(String p_46910_, GameType p_46911_, boolean p_46912_, Difficulty p_46913_, boolean p_46914_, GameRules p_46915_, DataPackConfig p_46916_) {
      this.levelName = p_46910_;
      this.gameType = p_46911_;
      this.hardcore = p_46912_;
      this.difficulty = p_46913_;
      this.allowCommands = p_46914_;
      this.gameRules = p_46915_;
      this.dataPackConfig = p_46916_;
   }

   public static LevelSettings parse(Dynamic<?> p_46925_, DataPackConfig p_46926_) {
      GameType gametype = GameType.byId(p_46925_.get("GameType").asInt(0));
      return new LevelSettings(p_46925_.get("LevelName").asString(""), gametype, p_46925_.get("hardcore").asBoolean(false), p_46925_.get("Difficulty").asNumber().map((p_46928_) -> {
         return Difficulty.byId(p_46928_.byteValue());
      }).result().orElse(Difficulty.NORMAL), p_46925_.get("allowCommands").asBoolean(gametype == GameType.CREATIVE), new GameRules(p_46925_.get("GameRules")), p_46926_);
   }

   public String levelName() {
      return this.levelName;
   }

   public GameType gameType() {
      return this.gameType;
   }

   public boolean hardcore() {
      return this.hardcore;
   }

   public Difficulty difficulty() {
      return this.difficulty;
   }

   public boolean allowCommands() {
      return this.allowCommands;
   }

   public GameRules gameRules() {
      return this.gameRules;
   }

   public DataPackConfig getDataPackConfig() {
      return this.dataPackConfig;
   }

   public LevelSettings withGameType(GameType p_46923_) {
      return new LevelSettings(this.levelName, p_46923_, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
   }

   public LevelSettings withDifficulty(Difficulty p_46919_) {
      return new LevelSettings(this.levelName, this.gameType, this.hardcore, p_46919_, this.allowCommands, this.gameRules, this.dataPackConfig);
   }

   public LevelSettings withDataPackConfig(DataPackConfig p_46921_) {
      return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, p_46921_);
   }

   public LevelSettings copy() {
      return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules.copy(), this.dataPackConfig);
   }
}