package net.revive.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import net.revive.ReviveMain;
import net.revive.accessor.PlayerEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityAccessor {

    @Shadow
    public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Unique
    private boolean canRevive = false;
    @Unique
    private boolean outOfWorld = false;
    @Unique
    private boolean supportiveRevival = false;
    @Unique
    private int handReviveTime = 0;

    public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        this.canRevive = nbt.getBoolean("CanRevive");
        this.outOfWorld = nbt.getBoolean("OutOfWorld");
        this.supportiveRevival = nbt.getBoolean("SupportiveRevival");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putBoolean("CanRevive", this.canRevive);
        nbt.putBoolean("OutOfWorld", this.outOfWorld);
        nbt.putBoolean("SupportiveRevival", this.supportiveRevival);
    }

    @Override
    protected void updatePostDeath() {
        ++this.deathTime;
        if (!this.getWorld().isClient()) {
            if (ReviveMain.CONFIG.timer != -1 && ReviveMain.CONFIG.timer < this.deathTime) {
                if (!ReviveMain.CONFIG.dropLoot) {
                    this.drop((ServerWorld) this.getWorld(), this.getDamageSources().generic());
                }
                setCanRevive(false, false, false);
                this.getWorld().sendEntityStatus(this, (byte) 60);
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    public void setCanRevive(boolean canRevive, boolean outOfWorld, boolean supportiveRevival) {
        this.canRevive = canRevive;
        this.outOfWorld = outOfWorld;
        this.supportiveRevival = supportiveRevival;
    }

    @Override
    public boolean canRevive() {
        return this.canRevive;
    }

    @Override
    public boolean isOutOfWorld() {
        return this.outOfWorld;
    }

    @Override
    public boolean isSupportiveRevival() {
        return this.supportiveRevival;
    }

    @Override
    public void setHandReviveTime(int handReviveTime) {
        this.handReviveTime = handReviveTime;
    }

    @Override
    public int getHandReviveTime() {
        return this.handReviveTime;
    }

}
