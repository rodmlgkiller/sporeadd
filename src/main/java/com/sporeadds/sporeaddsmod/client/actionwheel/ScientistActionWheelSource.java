package com.sporeadds.sporeaddsmod.client.actionwheel;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class ScientistActionWheelSource {

    private static final ResourceLocation FIELD_RESEARCH_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/field_research.png");

    private static final ResourceLocation EXPOSE_WEAKNESS_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/expose_weakness.png");

    private ScientistActionWheelSource() {
    }

    public static List<ActionWheelOption> getOptionsIfApplicable(Player player) {
        boolean isScientist = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);

        if (!isScientist) {
            return List.of();
        }

        ActionWheelOption fieldResearch = new ActionWheelOption(
                Component.translatable("power.sporeadd.field_research")
                        .withStyle(ChatFormatting.AQUA),
                ScientistActionWheelSource::renderFieldResearchIcon,
                ScientistActionWheelSource::activateFieldResearch
        );

        ActionWheelOption exposeWeakness = new ActionWheelOption(
                Component.translatable("gui.sporeadds.action_wheel.expose_weakness")
                        .withStyle(ChatFormatting.AQUA),
                ScientistActionWheelSource::renderExposeWeaknessIcon,
                ScientistActionWheelSource::activateExposeWeakness
        );

        return List.of(fieldResearch, exposeWeakness);
    }

    private static void renderFieldResearchIcon(GuiGraphics graphics, int x, int y, int size) {
        graphics.blit(FIELD_RESEARCH_ICON, x, y, 0, 0, size, size, size, size);
    }

    private static void renderExposeWeaknessIcon(GuiGraphics graphics, int x, int y, int size) {
        graphics.blit(EXPOSE_WEAKNESS_ICON, x, y, 0, 0, size, size, size, size);
    }

    private static void activateFieldResearch() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.BOOK_PUT, 1.0F)
            );
        }

        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.sendToServer(
                new com.sporeadds.sporeaddsmod.network.ActivateFieldResearchPacket()
        );
    }

    private static void activateExposeWeakness() {
        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.sendToServer(
                new com.sporeadds.sporeaddsmod.network.ExposeWeaknessPacket()
        );
    }
}