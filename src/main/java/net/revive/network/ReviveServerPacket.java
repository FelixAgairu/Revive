package net.revive.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.revive.ReviveMain;
import net.revive.accessor.PlayerEntityAccessor;
import net.revive.network.packet.*;
import net.revive.util.ReviveHelper;

public class ReviveServerPacket {

    public static void init() {
        PayloadTypeRegistry.playS2C().register(FirstPersonPacket.PACKET_ID, FirstPersonPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(RevivablePacket.PACKET_ID, RevivablePacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ReviveSyncPacket.PACKET_ID, ReviveSyncPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(HandRevivePacket.PACKET_ID, HandRevivePacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(RevivableSyncPacket.PACKET_ID, RevivableSyncPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RevivePacket.PACKET_ID, RevivePacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(HandRevivePacket.PACKET_ID, HandRevivePacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RevivePacket.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                ReviveHelper.revivePlayer(context.player());
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(HandRevivePacket.PACKET_ID, (payload, context) -> {
            int handReviveTime = payload.handReviveTime();
            context.server().execute(() -> {
                if (context.player() instanceof PlayerEntityAccessor playerEntityAccessor) {
                    if (handReviveTime == 0) {
                        playerEntityAccessor.setHandReviveTime(0);
                    } else {
                        playerEntityAccessor.setHandReviveTime(playerEntityAccessor.getHandReviveTime() + 1);
                        if (playerEntityAccessor.getHandReviveTime() >= ReviveMain.CONFIG.allowReviveWithHandTime) {
                            playerEntityAccessor.setHandReviveTime(0);
                            ServerPlayNetworking.send(context.player(), new HandRevivePacket(0));

                            EntityHitResult entityHitResult = ProjectileUtil.raycast(context.player(), context.player().getEyePos(), context.player().getEyePos().add(context.player().getRotationVec(1.0f).multiply(5.0D)), context.player().getBoundingBox().expand(2.0D),
                                    entity -> !entity.isSpectator(), 10.0D);

                            ServerPlayerEntity otherPlayer = null;
                            if (entityHitResult != null && entityHitResult.getEntity() instanceof ServerPlayerEntity serverPlayerEntity) {
                                otherPlayer = serverPlayerEntity;
                            } else if (ReviveHelper.getClosestPlayer(context.player(), 2.5D) instanceof ServerPlayerEntity serverPlayerEntity) {
                                otherPlayer = serverPlayerEntity;
                            }
                            if (otherPlayer != null && otherPlayer.isDead() && !((PlayerEntityAccessor) otherPlayer).canRevive()) {
                                ReviveHelper.setPlayerRevivable(otherPlayer, true, false, false);
                            }
                        }
                    }
                }
            });
        });
    }

}
