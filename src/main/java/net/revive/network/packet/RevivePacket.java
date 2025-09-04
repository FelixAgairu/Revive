package net.revive.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.revive.ReviveMain;

public record RevivePacket() implements CustomPayload {

    public static final CustomPayload.Id<RevivePacket> PACKET_ID = new CustomPayload.Id<>(ReviveMain.identifierOf("revive_packet"));

    public static final PacketCodec<RegistryByteBuf, RevivePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
    }, buf -> new RevivePacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
