package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketListener;

public interface ServerLoginPacketListener extends PacketListener {
   void handleHello(ServerboundHelloPacket p_134823_);

   void handleKey(ServerboundKeyPacket p_134824_);

   void handleCustomQueryPacket(ServerboundCustomQueryPacket p_134822_);
}