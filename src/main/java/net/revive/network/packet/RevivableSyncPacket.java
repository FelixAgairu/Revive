package net.revive.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.revive.ReviveMain;

// Syncs revivability of a player
public record RevivableSyncPacket(int entityId, boolean entityRevivable) implements CustomPayload {

    public static final CustomPayload.Id<RevivableSyncPacket> PACKET_ID = new CustomPayload.Id<>(ReviveMain.identifierOf("revivable_sync_packet"));

    public static final PacketCodec<RegistryByteBuf, RevivableSyncPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.entityId);
        buf.writeBoolean(value.entityRevivable);
    }, buf -> new RevivableSyncPacket(buf.readInt(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
