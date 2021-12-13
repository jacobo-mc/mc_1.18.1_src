package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
   private final DimensionType dimensionType;
   private final ResourceKey<Level> dimension;
   private final long seed;
   private final GameType playerGameType;
   @Nullable
   private final GameType previousPlayerGameType;
   private final boolean isDebug;
   private final boolean isFlat;
   private final boolean keepAllPlayerData;

   public ClientboundRespawnPacket(DimensionType p_132938_, ResourceKey<Level> p_132939_, long p_132940_, GameType p_132941_, @Nullable GameType p_132942_, boolean p_132943_, boolean p_132944_, boolean p_132945_) {
      this.dimensionType = p_132938_;
      this.dimension = p_132939_;
      this.seed = p_132940_;
      this.playerGameType = p_132941_;
      this.previousPlayerGameType = p_132942_;
      this.isDebug = p_132943_;
      this.isFlat = p_132944_;
      this.keepAllPlayerData = p_132945_;
   }

   public ClientboundRespawnPacket(FriendlyByteBuf p_179191_) {
      this.dimensionType = p_179191_.readWithCodec(DimensionType.CODEC).get();
      this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, p_179191_.readResourceLocation());
      this.seed = p_179191_.readLong();
      this.playerGameType = GameType.byId(p_179191_.readUnsignedByte());
      this.previousPlayerGameType = GameType.byNullableId(p_179191_.readByte());
      this.isDebug = p_179191_.readBoolean();
      this.isFlat = p_179191_.readBoolean();
      this.keepAllPlayerData = p_179191_.readBoolean();
   }

   public void write(FriendlyByteBuf p_132954_) {
      p_132954_.writeWithCodec(DimensionType.CODEC, () -> {
         return this.dimensionType;
      });
      p_132954_.writeResourceLocation(this.dimension.location());
      p_132954_.writeLong(this.seed);
      p_132954_.writeByte(this.playerGameType.getId());
      p_132954_.writeByte(GameType.getNullableId(this.previousPlayerGameType));
      p_132954_.writeBoolean(this.isDebug);
      p_132954_.writeBoolean(this.isFlat);
      p_132954_.writeBoolean(this.keepAllPlayerData);
   }

   public void handle(ClientGamePacketListener p_132951_) {
      p_132951_.handleRespawn(this);
   }

   public DimensionType getDimensionType() {
      return this.dimensionType;
   }

   public ResourceKey<Level> getDimension() {
      return this.dimension;
   }

   public long getSeed() {
      return this.seed;
   }

   public GameType getPlayerGameType() {
      return this.playerGameType;
   }

   @Nullable
   public GameType getPreviousPlayerGameType() {
      return this.previousPlayerGameType;
   }

   public boolean isDebug() {
      return this.isDebug;
   }

   public boolean isFlat() {
      return this.isFlat;
   }

   public boolean shouldKeepAllPlayerData() {
      return this.keepAllPlayerData;
   }
}