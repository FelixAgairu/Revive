package net.revive.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.revive.ReviveMain;

public record RevivablePacket(boolean canRevive, boolean outOfWorld, boolean supportiveRevival) implements CustomPayload {

    public static final CustomPayload.Id<RevivablePacket> PACKET_ID = new CustomPayload.Id<>(ReviveMain.identifierOf("revivable_packet"));

    public static final PacketCodec<RegistryByteBuf, RevivablePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBoolean(value.canRevive);
        buf.writeBoolean(value.outOfWorld);
        buf.writeBoolean(value.supportiveRevival);
    }, buf -> new RevivablePacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
