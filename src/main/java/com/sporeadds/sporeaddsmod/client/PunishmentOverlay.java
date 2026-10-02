package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Viñeteado pulsante en los bordes de la pantalla mientras el jugador tiene el efecto
 * {@link com.sporeadds.sporeaddsmod.effects.PunishmentEffect}. El pulso va sincronizado con el
 * latido de {@link PunishmentClientHandler}. Según el "tier" (0..2, amplifier del efecto):
 * <ul>
 *   <li>tier 0: negro, tamaño base, animación x1.0</li>
 *   <li>tier 1: granate, más grande, animación x1.5</li>
 *   <li>tier 2: granate brillante, aún más grande, animación x2.0</li>
 * </ul>
 * (La velocidad de animación la aporta {@code PunishmentClientHandler.beatIntensity}.)
 */
@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class PunishmentOverlay {

    private static final int ALPHA_MAX = 255;

    /** Opacidad base por tier (0-255), antes de aplicar el pulso. */
    private static final int[] TIER_BASE_ALPHA = { 150, 200, 245 };
    /** Color RGB del borde por tier: negro, granate, granate brillante. */
    private static final int[] TIER_RGB = { 0x000000, 0x66060C, 0xC01A22 };
    /** Fracción de min(ancho, alto) que ocupa la banda por tier. */
    private static final float[] TIER_SIZE = { 0.40F, 0.52F, 0.64F };

    private PunishmentOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        MobEffectInstance inst = mc.player.getEffect(effects.PUNISHMENT);
        if (inst == null) return;

        int tier = Mth.clamp(inst.getAmplifier(), 0, 2);

        // Pulso sincronizado con el latido del audio; base alta para que el borde no desaparezca.
        float beat = PunishmentClientHandler.beatIntensity(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        float pulse = 0.66F + 0.34F * beat;

        int peak = Math.min(ALPHA_MAX, Math.round(TIER_BASE_ALPHA[tier] * pulse));
        if (peak <= 0) return;

        int rgb = TIER_RGB[tier] & 0xFFFFFF;

        GuiGraphics g = event.getGuiGraphics();
        int w = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int band = Math.max(24, Math.round(Math.min(w, h) * TIER_SIZE[tier]));

        for (int i = 0; i < band; i++) {
            float t = 1.0F - (i / (float) band);   // 1 en el borde, 0 hacia el centro
            float f = t * (2.0F - t);              // ease-out: la zona oscura llega más adentro
            int a = Math.round(peak * f);
            if (a <= 0) continue;
            int color = (a << 24) | rgb;

            g.fill(0, i, w, i + 1, color);                 // borde superior
            g.fill(0, h - 1 - i, w, h - i, color);         // borde inferior
            g.fill(i, 0, i + 1, h, color);                 // borde izquierdo
            g.fill(w - 1 - i, 0, w - i, h, color);         // borde derecho
        }
    }
}
