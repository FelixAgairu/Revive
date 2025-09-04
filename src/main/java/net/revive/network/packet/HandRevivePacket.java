package net.revive.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.revive.ReviveMain;

public record HandRevivePacket(int handReviveTime) implements CustomPayload {

    public static final CustomPayload.Id<HandRevivePacket> PACKET_ID = new CustomPayload.Id<>(ReviveMain.identifierOf("hand_revive_packet"));

    public static final PacketCodec<RegistryByteBuf, HandRevivePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.handReviveTime);
    }, buf -> new HandRevivePacket(buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
