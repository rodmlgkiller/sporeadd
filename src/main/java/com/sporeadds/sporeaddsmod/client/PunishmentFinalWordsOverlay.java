package com.sporeadds.sporeaddsmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Con {@code punishment} amplifier 2 (3er castigo del Proto), llena la pantalla de copias de la
 * frase final apareciendo y desapareciendo en distintos ángulos y posiciones.
 */
@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class PunishmentFinalWordsOverlay {

    /** {xFrac, yFrac, ánguloº, ticks de ciclo, desfase 0..1}. */
    private static final float[][] SLOTS = {
            {0.20F, 0.18F, -22F, 70F, 0.00F},
            {0.78F, 0.12F,  17F, 85F, 0.35F},
            {0.12F, 0.55F,  -8F, 60F, 0.60F},
            {0.86F, 0.60F,  26F, 95F, 0.15F},
            {0.30F, 0.82F,  12F, 75F, 0.80F},
            {0.70F, 0.86F, -19F, 65F, 0.45F},
            {0.50F, 0.35F,   5F, 110F, 0.25F},
            {0.50F, 0.68F, -13F, 90F, 0.70F},
    };

    private static final int COLOR_RGB = 0xC01A22; // granate brillante, como el tier 2

    private PunishmentFinalWordsOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        MobEffectInstance inst = mc.player.getEffect(effects.PUNISHMENT.get());
        if (inst == null || inst.getAmplifier() < 2) return;

        String text = Component.translatable("overlay.sporeadd.punishment.final_words").getString();
        Font font = mc.font;
        GuiGraphics g = event.getGuiGraphics();
        int w = event.getWindow().getGuiScaledWidth();
        int h = event.getWindow().getGuiScaledHeight();
        float time = mc.player.tickCount + event.getPartialTick();

        for (float[] slot : SLOTS) {
            float cycle = ((time / slot[3]) + slot[4]) % 1.0F;
            float fade = Mth.sin(cycle * Mth.PI);          // 0 -> 1 -> 0
            if (fade <= 0.03F) continue;

            int alpha = (int) (fade * 235.0F) & 0xFF;
            if (alpha < 8) continue;
            int color = (alpha << 24) | COLOR_RGB;

            float scale = 0.85F + 0.35F * fade;

            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(slot[0] * w, slot[1] * h, 0.0F);
            pose.mulPose(Axis.ZP.rotationDegrees(slot[2]));
            pose.scale(scale, scale, 1.0F);
            g.drawString(font, text, -font.width(text) / 2, -font.lineHeight / 2, color, true);
            pose.popPose();
        }
    }
}
