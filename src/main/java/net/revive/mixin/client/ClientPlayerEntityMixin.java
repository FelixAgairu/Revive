package net.revive.mixin.client;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.revive.ReviveMain;
import net.revive.accessor.PlayerEntityAccessor;
import net.revive.network.packet.HandRevivePacket;
import net.revive.util.ReviveHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow
    @Mutable
    @Final
    protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "updatePostDeath", at = @At("HEAD"), cancellable = true)
    protected void updatePostDeathMixin(CallbackInfo info) {
        if (this.deathTime == 20 && ReviveMain.CONFIG.thirdPersonOnDeath && this.client.options.getPerspective().isFirstPerson())
            this.client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
        this.deathTime++;
        if (ReviveMain.CONFIG.timer == -1 || ReviveMain.CONFIG.timer > this.deathTime) {
            info.cancel();
        } else if (this.deathTime >= 20) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    // UseEntityCallback only ticks every few ticks when holding right click on server and client
    @Inject(method = "tick", at = @At("TAIL"))
    private void tickMixin(CallbackInfo info) {
        if (ReviveMain.CONFIG.allowReviveWithHand && this.isSneaking() && client.options.useKey.isPressed() && this.getMainHandStack().isEmpty() && client.player instanceof PlayerEntityAccessor playerEntityAccessor) {
            EntityHitResult entityHitResult = ProjectileUtil.raycast(this, this.getEyePos(), this.getEyePos().add(this.getRotationVec(1.0f).multiply(5.0D)), this.getBoundingBox().expand(2.0D),
                    entity -> !entity.isSpectator(), 10.0D);

            if (entityHitResult != null && entityHitResult.getEntity() instanceof PlayerEntity otherPlayer) {
                if (otherPlayer.isDead() && !((PlayerEntityAccessor) otherPlayer).canRevive()) {
                    playerEntityAccessor.setHandReviveTime(playerEntityAccessor.getHandReviveTime() + 1);
                    ClientPlayNetworking.send(new HandRevivePacket(1));
                } else if (playerEntityAccessor.getHandReviveTime() > 0) {
                    resetHandRevive();
                }
            } else {
                if (ReviveHelper.getClosestPlayer(this, 2.5D) instanceof PlayerEntity otherPlayer && otherPlayer.isDead() && !((PlayerEntityAccessor) otherPlayer).canRevive()) {
                    playerEntityAccessor.setHandReviveTime(playerEntityAccessor.getHandReviveTime() + 1);
                    ClientPlayNetworking.send(new HandRevivePacket(1));
                } else if (playerEntityAccessor.getHandReviveTime() > 0) {
                    resetHandRevive();
                }
            }
        } else if (((PlayerEntityAccessor) client.player).getHandReviveTime() > 0) {
            resetHandRevive();
        }
    }

    @Unique
    private void resetHandRevive() {
        ((PlayerEntityAccessor) client.player).setHandReviveTime(0);
        ClientPlayNetworking.send(new HandRevivePacket(0));
    }

}
