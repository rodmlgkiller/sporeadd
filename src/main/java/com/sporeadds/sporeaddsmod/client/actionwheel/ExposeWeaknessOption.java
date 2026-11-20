package com.sporeadds.sporeaddsmod.client.actionwheel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ExposeWeaknessOption {

    private static final ResourceLocation ICON_TEXTURE =
            new ResourceLocation("sporeadd", "textures/mob_effect/expose_weakness.png");

    // Cooldown: scientist.expose_weakness.cooldown_ticks (this client-side option is not used by the action wheel at the
    // moment; the server enforces the real cooldown).

    private static final Map<UUID, Long> lastUseTick = new HashMap<>();

    private ExposeWeaknessOption() {
    }

    public static ActionWheelOption create() {
        Component label = Component.translatable("gui.sporeadds.action_wheel.expose_weakness")
                .withStyle(ChatFormatting.AQUA);

        return new ActionWheelOption(
                label,
                ExposeWeaknessOption::renderIcon,
                ExposeWeaknessOption::onSelect
        );
    }

    private static void renderIcon(GuiGraphics graphics, int x, int y, int size) {
        PoseStack pose = graphics.pose();
        pose.pushPose();

        RenderSystem.setShaderTexture(0, ICON_TEXTURE);
        graphics.blit(ICON_TEXTURE, x, y, 0, 0, size, size, size, size);

        pose.popPose();
    }

    private static boolean isOnCooldown(UUID playerId) {
        long now = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        Long last = lastUseTick.get(playerId);
        return last != null && (now - last) < ScientistConfig.exposeWeaknessCooldownTicks();
    }

    private static void onSelect() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        UUID playerId = mc.player.getUUID();

        if (isOnCooldown(playerId)) {
            return;
        }

        long now = mc.level != null ? mc.level.getGameTime() : 0L;
        lastUseTick.put(playerId, now);

        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.sendToServer(
                new com.sporeadds.sporeaddsmod.network.ExposeWeaknessPacket()
        );
    }
}