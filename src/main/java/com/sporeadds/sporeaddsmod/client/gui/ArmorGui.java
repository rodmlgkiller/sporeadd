package com.sporeadds.sporeaddsmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataClient;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.Powers.Poder12;
import com.sporeadds.sporeaddsmod.Powers.Poder12Variants;
import com.sporeadds.sporeaddsmod.config.SporeAddsClientConfig;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;

public class ArmorGui {
    private static final ResourceLocation ARMOR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/spore_armor.png");

    public static final IGuiOverlay ARMOR_BAR = (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (SporeAddsConfig.ARMOR_BOSSBAR_ENABLED.get()) {
            return;
        }

        if (!SporeClassUtil.hasClass(mc.player, "kommandant")) {
            return;
        }

        mc.player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(armor -> {
            int armorHp = PlayerDataClient.getClientArmorHp();

            // Obtenemos el máximo de armadura específico de este jugador
            int maxArmorHp = Poder12Variants.getMaxArmor(mc.player);

            // SOLUCIÓN AL ERROR: Creamos una variable inmutable que asegura que no haya división por cero
            // Al hacerla aquí afuera y no modificarla, Java la considera 'effectively final'
            final int safeMaxArmorHp = Math.max(1, maxArmorHp);

            int guiWidth = mc.getWindow().getGuiScaledWidth();
            int guiHeight = mc.getWindow().getGuiScaledHeight();

            int spriteW = 64;
            int spriteH = 32;

            int x = (guiWidth - spriteW) / 2 + SporeAddsClientConfig.ARMOR_BAR_X.get();
            int y = guiHeight - spriteH + SporeAddsClientConfig.ARMOR_BAR_Y.get();

            float alpha = SporeAddsClientConfig.ARMOR_BAR_OPACITY.get().floatValue();

            mc.player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(level -> {
                if (level.getLevel() >= 8 || armorHp > 0) {
                    RenderSystem.enableBlend();
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, ARMOR_TEXTURE);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

                    // --- CÁLCULO DE PROPORCIÓN DINÁMICO ---
                    // Usamos safeMaxArmorHp para calcular la fase sin errores de lambda
                    int fase = ((safeMaxArmorHp - armorHp) * 41) / safeMaxArmorHp;

                    // Nos aseguramos de no salirnos de los límites de la textura
                    fase = Math.max(0, Math.min(41, fase));
                    int yOffset = fase * spriteH;

                    if (armorHp != 0) {
                        poseStack.blit(ARMOR_TEXTURE, x, y, 0, yOffset, spriteW, spriteH, spriteW, 1344);
                    } else {
                        poseStack.blit(ARMOR_TEXTURE, x, y, 0, 1312, spriteW, spriteH, spriteW, 1344);
                    }

                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.disableBlend();
                }
            });
        });
    };
}