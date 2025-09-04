package net.revive.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.revive.ReviveMain;
import net.revive.accessor.PlayerEntityAccessor;
import net.revive.network.packet.RevivablePacket;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {

    public PotionItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void useMixin(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        PotionContentsComponent potionContentsComponent = user.getStackInHand(hand).getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

        if (potionContentsComponent.matches(ReviveMain.REVIVIFY_POTION) || potionContentsComponent.matches(ReviveMain.SUPPORTIVE_REVIVIFY_POTION)) {
            EntityHitResult entityHitResult = ProjectileUtil.raycast(user, user.getEyePos(), user.getEyePos().add(user.getRotationVec(1.0f).multiply(5.0D)), user.getBoundingBox().expand(2.0D),
                    entity -> !entity.isSpectator(), 10.0D);
            if (entityHitResult != null) {
                if (entityHitResult.getEntity() instanceof PlayerEntity playerEntity && playerEntity.isDead()
                        && !((PlayerEntityAccessor) playerEntity).canRevive()) {
                    if (!world.isClient()) {
                        ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, new RevivablePacket(true, false, potionContentsComponent.matches(ReviveMain.SUPPORTIVE_REVIVIFY_POTION)));
                        world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), ReviveMain.REVIVE_SOUND_EVENT, SoundCategory.PLAYERS, 1.0F, 0.9F + playerEntity.getWorld().getRandom().nextFloat() * 0.2F, playerEntity.getRandom().nextLong());
                    }
                    if (!user.getAbilities().creativeMode) {
                        user.getStackInHand(hand).decrement(1);
                    }
                    info.setReturnValue(TypedActionResult.consume(user.getStackInHand(hand)));
                }
            } else if (world.getClosestPlayer(user, 2.5D) instanceof PlayerEntity playerEntity && playerEntity.isDead() && !((PlayerEntityAccessor) playerEntity).canRevive()) {
                if (!world.isClient()) {
                    ServerPlayNetworking.send((ServerPlayerEntity) playerEntity,
                            new RevivablePacket(true, false, potionContentsComponent.matches(ReviveMain.SUPPORTIVE_REVIVIFY_POTION)));
                    world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), ReviveMain.REVIVE_SOUND_EVENT, SoundCategory.PLAYERS, 1.0F, 0.9F + playerEntity.getWorld().getRandom().nextFloat() * 0.2F, playerEntity.getRandom().nextLong());
                }
                if (!user.getAbilities().creativeMode) {
                    user.getStackInHand(hand).decrement(1);
                }
                info.setReturnValue(TypedActionResult.consume(user.getStackInHand(hand)));
            }
        }
    }
}

// if (world instanceof ServerWorld) {
// for (int u = 0; u < 20; u++)
// world.addParticle(ParticleTypes.END_ROD, (double) playerEntity.getX() - 0.5D + world.random.nextFloat(), playerEntity.getRandomBodyY(),
// (double) playerEntity.getZ() - 0.5D + world.random.nextFloat(), 0.1D * world.random.nextFloat(), 0.3D * world.random.nextFloat(), 0.1D * world.random.nextFloat());
