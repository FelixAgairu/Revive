package net.revive.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.revive.ReviveMain;

// Syncs player after revive
public record ReviveSyncPacket(int entityId, int healthPoints) implements CustomPayload {

    public static final CustomPayload.Id<ReviveSyncPacket> PACKET_ID = new CustomPayload.Id<>(ReviveMain.identifierOf("revive_sync_packet"));

    public static final PacketCodec<RegistryByteBuf, ReviveSyncPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.entityId);
        buf.writeInt(value.healthPoints);
    }, buf -> new ReviveSyncPacket(buf.readInt(), buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
