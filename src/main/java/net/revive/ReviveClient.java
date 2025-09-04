package net.revive;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.revive.accessor.PlayerEntityAccessor;
import net.revive.network.ReviveClientPacket;

@Environment(EnvType.CLIENT)
public class ReviveClient implements ClientModInitializer {

    private static final Identifier REVIVE_BARS_TEXTURE = Identifier.of("revive:textures/gui/revive_bars.png");

    @Override
    public void onInitializeClient() {
        ReviveClientPacket.init();

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && ReviveMain.CONFIG.allowReviveWithHand) {
                if (client.player.isSneaking() && client.options.useKey.isPressed() && client.player.getMainHandStack().isEmpty()) {
                    if (client.player instanceof PlayerEntityAccessor playerEntityAccessor && playerEntityAccessor.getHandReviveTime() > 0) {
                        renderReviveBar(context, playerEntityAccessor.getHandReviveTime());
                    }
                }
            }
        });
    }

    private static void renderReviveBar(DrawContext context, int reviveTime) {
        reviveTime = (int) (reviveTime / (float) ReviveMain.CONFIG.allowReviveWithHandTime * 74.0f);
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 51.0f);
        context.drawTexture(REVIVE_BARS_TEXTURE, context.getScaledWindowWidth() / 2 - 37, context.getScaledWindowHeight() - 57, 0, 0, 74, 5);
        if (reviveTime > 0) {
            context.drawTexture(REVIVE_BARS_TEXTURE, context.getScaledWindowWidth() / 2 - 37, context.getScaledWindowHeight() - 57, 0, 5, reviveTime, 5);
        }
        context.getMatrices().pop();
    }

}
