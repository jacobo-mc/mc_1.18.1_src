package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {
   private final byte[] encodeBuf = new byte[8192];
   private final Deflater deflater;
   private int threshold;

   public CompressionEncoder(int p_129448_) {
      this.threshold = p_129448_;
      this.deflater = new Deflater();
   }

   protected void encode(ChannelHandlerContext p_129452_, ByteBuf p_129453_, ByteBuf p_129454_) {
      int i = p_129453_.readableBytes();
      FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(p_129454_);
      if (i < this.threshold) {
         friendlybytebuf.writeVarInt(0);
         friendlybytebuf.writeBytes(p_129453_);
      } else {
         byte[] abyte = new byte[i];
         p_129453_.readBytes(abyte);
         friendlybytebuf.writeVarInt(abyte.length);
         this.deflater.setInput(abyte, 0, i);
         this.deflater.finish();

         while(!this.deflater.finished()) {
            int j = this.deflater.deflate(this.encodeBuf);
            friendlybytebuf.writeBytes(this.encodeBuf, 0, j);
         }

         this.deflater.reset();
      }

   }

   public int getThreshold() {
      return this.threshold;
   }

   public void setThreshold(int p_129450_) {
      this.threshold = p_129450_;
   }
}