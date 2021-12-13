package net.minecraft.network.protocol.status;

import net.minecraft.network.PacketListener;

public interface ServerStatusPacketListener extends PacketListener {
   void handlePingRequest(ServerboundPingRequestPacket p_134986_);

   void handleStatusRequest(ServerboundStatusRequestPacket p_134987_);
}