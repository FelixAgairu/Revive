package net.revive.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.revive.accessor.PlayerEntityAccessor;
import net.revive.util.ReviveHelper;

public class LivelyAftermathEffect extends StatusEffect {

    public LivelyAftermathEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient() && entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).getHungerManager().add(amplifier + 1, 1.0f);
        }
        return super.applyUpdateEffect(entity, amplifier);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        if (!entity.getWorld().isClient() && entity.isDead() && entity instanceof PlayerEntity && !((PlayerEntityAccessor) entity).canRevive()) {
            ReviveHelper.setPlayerRevivable((ServerPlayerEntity) entity, true, false, true);
        }
    }

}
