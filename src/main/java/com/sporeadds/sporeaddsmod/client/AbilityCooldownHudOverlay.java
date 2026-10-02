package com.sporeadds.sporeaddsmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class AbilityCooldownHudOverlay {

    private static final int LINE_SPACING = 12;
    private static final int MEDIC_COOLDOWN_COLOR = 0xFF5555;

    private AbilityCooldownHudOverlay() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (Minecraft.getInstance().isPaused()) {
            return;
        }

        CamouflageCooldownClientState.tick();
        DecoyCooldownClientState.tick();
        FieldResearchCooldownClientState.tick();
        ExposeWeaknessCooldownClientState.tick();
        DelayedDefibrillationCooldownClientState.tick();
        SelfDefibrillateCooldownClientState.tick();
        BerserkerCooldownClientState.tick();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int baseY = screenHeight - 60;
        int nextLine = 0;

        if (CamouflageCooldownClientState.isOnCooldown()) {
            int seconds = CamouflageCooldownClientState.getRemainingTicks() / 20 + 1;
            Component text = Component.translatable("ability.sporeadds.ghost.camouflage_cooldown", seconds);

            graphics.drawCenteredString(
                    mc.font,
                    text,
                    centerX,
                    baseY - (nextLine * LINE_SPACING),
                    0x55FF55
            );
            nextLine++;
        }

        if (DecoyCooldownClientState.isOnCooldown()) {
            int seconds = DecoyCooldownClientState.getRemainingTicks() / 20 + 1;
            Component text = Component.translatable("ability.sporeadds.ghost.decoy_cooldown", seconds);

            graphics.drawCenteredString(
                    mc.font,
                    text,
                    centerX,
                    baseY - (nextLine * LINE_SPACING),
                    0x55FF55
            );
            nextLine++;
        }

        if (FieldResearchCooldownClientState.isOnCooldown()) {
            int seconds = FieldResearchCooldownClientState.getRemainingTicks() / 20 + 1;
            Component text = Component.translatable("ability.sporeadds.scientist.field_research_cooldown", seconds);

            graphics.drawCenteredString(
                    mc.font,
                    text,
                    centerX,
                    baseY - (nextLine * LINE_SPACING),
                    0x55FFFF
            );
            nextLine++;
        }

        if (ExposeWeaknessCooldownClientState.isOnCooldown()) {
            int seconds = ExposeWeaknessCooldownClientState.getRemainingTicks() / 20 + 1;
            Component text = Component.translatable("ability.sporeadds.scientist.expose_weakness_cooldown", seconds);

            graphics.drawCenteredString(
                    mc.font,
                    text,
                    centerX,
                    baseY - (nextLine * LINE_SPACING),
                    0x00FFFF
            );
            nextLine++;
        }

        if (DelayedDefibrillationCooldownClientState.isOnCooldown()) {
            int seconds = DelayedDefibrillationCooldownClientState.getRemainingTicks() / 20 + 1;
            Component text = Component.translatable("ability.sporeadds.medic.delayed_defibrillation_cooldown", seconds);

            graphics.drawCenteredString(
                    mc.font,
                    text,
                    centerX,
                    baseY - (nextLine * LINE_SPACING),
                    MEDIC_COOLDOWN_COLOR
            );
            nextLine++;
        }

        if (SelfDefibrillateCooldownClientState.isOnCooldown()) {
            int seconds = SelfDefibrillateCooldownClientState.getRemainingTicks() / 20 + 1;
            Component text = Component.translatable("ability.sporeadds.medic.self_defibrillate.cooldown", seconds);

            graphics.drawCenteredString(
                    mc.font,
                    text,
                    centerX,
                    baseY - (nextLine * LINE_SPACING),
                    MEDIC_COOLDOWN_COLOR
            );
            nextLine++;
        }

        if (BerserkerCooldownClientState.isCounterOnCooldown()) {
            int seconds = BerserkerCooldownClientState.getCounterTicks() / 20 + 1;
            graphics.drawCenteredString(mc.font,
                    Component.translatable("ability.sporeadds.berserker.counter_cooldown", seconds),
                    centerX, baseY - (nextLine * LINE_SPACING), 0xFFAA00);
            nextLine++;
        }

        if (BerserkerCooldownClientState.isClawsOnCooldown()) {
            int seconds = BerserkerCooldownClientState.getClawsTicks() / 20 + 1;
            graphics.drawCenteredString(mc.font,
                    Component.translatable("ability.sporeadds.berserker.claws_cooldown", seconds),
                    centerX, baseY - (nextLine * LINE_SPACING), 0xFF5555);
            nextLine++;
        }
    }
}