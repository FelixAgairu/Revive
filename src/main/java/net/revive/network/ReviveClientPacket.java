package net.revive.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.revive.ReviveMain;
import net.revive.accessor.PlayerEntityAccessor;
import net.revive.network.packet.*;

@Environment(EnvType.CLIENT)
public class ReviveClientPacket {

    @SuppressWarnings("resource")
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ReviveSyncPacket.PACKET_ID, (payload, context) -> {
            int entityId = payload.entityId();
            int healthPoints = payload.healthPoints();
            context.client().execute(() -> {
                if (context.player().getId() == entityId) {
                    if (ReviveMain.CONFIG.thirdPersonOnDeath) {
                        context.client().options.setPerspective(Perspective.FIRST_PERSON);
                    }
                    ((PlayerEntityAccessor) context.player()).setCanRevive(false, false, false);
                    context.player().setHealth(healthPoints);
                    context.client().currentScreen.close();
                    context.player().deathTime = 0;
                    context.player().hurtTime = 0;
                    context.player().extinguish();
                } else if (context.player().getWorld().getEntityById(entityId) instanceof PlayerEntity playerEntity) {
                    ((PlayerEntityAccessor) playerEntity).setCanRevive(false, false, false);
                    playerEntity.setHealth(healthPoints);
                    playerEntity.deathTime = 0;
                    playerEntity.hurtTime = 0;
                    playerEntity.extinguish();
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(RevivablePacket.PACKET_ID, (payload, context) -> {
            boolean canRevive = payload.canRevive();
            boolean outOfWorld = payload.outOfWorld();
            boolean supportiveRevival = payload.supportiveRevival();
            context.client().execute(() -> {
                if (canRevive) {
                    for (int u = 0; u < 30; u++) {
                        context.player().getWorld().addParticle(ParticleTypes.END_ROD, context.player().getX() - 1.0D + context.player().getRandom().nextFloat() * 2F,
                                context.player().getRandomBodyY(), context.player().getZ() - 1.0D + context.player().getRandom().nextFloat() * 2F, 0.0D, 0.2D, 0.0D);
                    }
                }
                ((PlayerEntityAccessor) context.player()).setCanRevive(canRevive, outOfWorld, supportiveRevival);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(FirstPersonPacket.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().options.setPerspective(Perspective.FIRST_PERSON);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(HandRevivePacket.PACKET_ID, (payload, context) -> {
            int handReviveTime = payload.handReviveTime();
            context.client().execute(() -> {
                ((PlayerEntityAccessor) context.player()).setHandReviveTime(handReviveTime);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(RevivableSyncPacket.PACKET_ID, (payload, context) -> {
            int entityId = payload.entityId();
            boolean entityRevivable = payload.entityRevivable();
            context.client().execute(() -> {
                if (context.player().getWorld().getEntityById(entityId) instanceof PlayerEntityAccessor playerEntityAccessor) {
                    playerEntityAccessor.setCanRevive(entityRevivable, false, false);
                }
            });
        });
    }

}
