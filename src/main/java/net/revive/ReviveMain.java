package net.revive;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import net.revive.accessor.PlayerEntityAccessor;
import net.revive.config.ReviveConfig;
import net.revive.effect.AftermathEffect;
import net.revive.effect.LivelyAftermathEffect;
import net.revive.network.ReviveServerPacket;
import net.revive.network.packet.FirstPersonPacket;
import net.revive.network.packet.RevivablePacket;
import net.revive.screen.PlayerLootScreenHandler;

public class ReviveMain implements ModInitializer {

    public static final boolean isBackSlotLoaded = FabricLoader.getInstance().isModLoaded("backslot");

    public static ReviveConfig CONFIG = new ReviveConfig();
    public static final Item REVIVE_ITEM = new Item(new Item.Settings());

    public static final Identifier REVIVE_SOUND = identifierOf("revive");
    public static SoundEvent REVIVE_SOUND_EVENT = SoundEvent.of(REVIVE_SOUND);

    public static final RegistryEntry<StatusEffect> AFTERMATH_EFFECT = register("revive:aftermath",
            new AftermathEffect(StatusEffectCategory.HARMFUL, 11838975)
                    .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Identifier.ofVanilla("effect.speed"), -0.30F, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, Identifier.ofVanilla("effect.strength"), -5.0F, EntityAttributeModifier.Operation.ADD_VALUE));

    public static final RegistryEntry<StatusEffect> LIVELY_AFTERMATH_EFFECT = register("revive:lively_aftermath", new LivelyAftermathEffect(StatusEffectCategory.HARMFUL, 10323199)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Identifier.ofVanilla("effect.speed"), -0.15F, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final RegistryEntry<Potion> REVIVIFY_POTION = register("revivify_potion", new Potion(new StatusEffectInstance(AFTERMATH_EFFECT, 600)));
    public static final RegistryEntry<Potion> SUPPORTIVE_REVIVIFY_POTION = register("supportive_revivify_potion", new Potion(new StatusEffectInstance(LIVELY_AFTERMATH_EFFECT, 600)));

    @Override
    public void onInitialize() {
        AutoConfig.register(ReviveConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ReviveConfig.class).getConfig();
        ReviveServerPacket.init();
        Registry.register(Registries.ITEM, Identifier.of("revive", "revival_star"), REVIVE_ITEM);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(REVIVE_ITEM));
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CLERIC, 1, (factories -> {
            factories.add((entity, random) -> new TradeOffer(new TradedItem(Items.EMERALD, 28), new ItemStack(REVIVE_ITEM), 4, 1, 0.4F));
        }));
        Registry.register(Registries.SOUND_EVENT, REVIVE_SOUND, REVIVE_SOUND_EVENT);
        FabricBrewingRecipeRegistryBuilder.BUILD.register((builder) -> {
            builder.registerPotionRecipe(Potions.STRONG_REGENERATION, ReviveMain.REVIVE_ITEM, ReviveMain.SUPPORTIVE_REVIVIFY_POTION);
            builder.registerPotionRecipe(Potions.MUNDANE, Items.GOLDEN_APPLE, ReviveMain.REVIVIFY_POTION);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (handler.getPlayer() instanceof PlayerEntityAccessor playerEntityAccessor) {
                ServerPlayNetworking.send(handler.player, new RevivablePacket(playerEntityAccessor.canRevive(), playerEntityAccessor.isOutOfWorld(), playerEntityAccessor.isSupportiveRevival()));
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (ReviveMain.CONFIG.thirdPersonOnDeath) {
                ServerPlayNetworking.send(newPlayer, new FirstPersonPacket());
            }
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof PlayerEntity otherPlayerEntity && otherPlayerEntity.deathTime > 20 && (ReviveMain.CONFIG.allowLootablePlayer || ReviveMain.CONFIG.allowReviveWithHand) && player.getMainHandStack().get(DataComponentTypes.POTION_CONTENTS) == null) {
                if (!world.isClient()) {
                    if (ReviveMain.CONFIG.allowReviveWithHand) {
                        if (!player.isSneaking()) {
                            if (!((PlayerEntityAccessor) otherPlayerEntity).canRevive()) {
                                player.sendMessage(Text.translatable("text.revive.info"), true);
                            }
                            if (ReviveMain.CONFIG.allowLootablePlayer) {
                                player.openHandledScreen(
                                        new SimpleNamedScreenHandlerFactory((syncId, inv, p) -> new PlayerLootScreenHandler(syncId, inv, otherPlayerEntity.getInventory()), otherPlayerEntity.getName()));
                                return ActionResult.SUCCESS;
                            }
                            return ActionResult.PASS;
                        }
                    } else if (ReviveMain.CONFIG.allowLootablePlayer) {
                        player.openHandledScreen(
                                new SimpleNamedScreenHandlerFactory((syncId, inv, p) -> new PlayerLootScreenHandler(syncId, inv, otherPlayerEntity.getInventory()), otherPlayerEntity.getName()));
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    public static Identifier identifierOf(String name) {
        return Identifier.of("revive", name);
    }

    private static RegistryEntry<StatusEffect> register(String id, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(id), statusEffect);
    }

    private static RegistryEntry<Potion> register(String name, Potion potion) {
        return Registry.registerReference(Registries.POTION, Identifier.of(name), potion);
    }

}
