package com.sporeadds.sporeaddsmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerData;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.client.BerserkerClawRenderState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class KommandantEyeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public KommandantEyeLayer(@NotNull RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    private static boolean isKommandant(AbstractClientPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String id = data.getIdentifier();
                    return id != null && id.equals("kommandant");
                })
                .orElse(false);
    }

    private static boolean isCaustic(AbstractClientPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String subclass = data.getSubclass();
                    return subclass != null && subclass.equalsIgnoreCase("caustic");
                })
                .orElse(false);
    }

    private static boolean isAbyssal(AbstractClientPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String subclass = data.getSubclass();
                    return subclass != null && subclass.equalsIgnoreCase("abyssal");
                })
                .orElse(false);
    }

    private static boolean isGluttonous(AbstractClientPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String subclass = data.getSubclass();
                    return subclass != null && subclass.equalsIgnoreCase("gluttonous");
                })
                .orElse(false);
    }

    private static ResourceLocation getBaseTexture(int type) {
        return switch (type) {
            case 2 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_base2.png");
            case 3 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_base3.png");
            case 4 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_base4.png");
            default -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_base.png");
        };
    }

    private static ResourceLocation getGlowTexture(int type, boolean caustic, boolean abyssal, boolean gluttonous) {
        if (abyssal) {
            return switch (type) {
                case 2 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow2abyssal.png");
                case 3 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow3abyssal.png");
                case 4 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow4abyssal.png");
                default -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glowabyssal.png");
            };
        }

        if (caustic) {
            return switch (type) {
                case 2 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow2caustic.png");
                case 3 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow3caustic.png");
                case 4 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow4caustic.png");
                default -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glowcaustic.png");
            };
        }

        if (gluttonous) {
            return switch (type) {
                case 2 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow2gluttonous.png");
                case 3 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow3gluttonous.png");
                case 4 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow4gluttonous.png");
                default -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glowgluttonous.png");
            };
        }

        return switch (type) {
            case 2 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow2.png");
            case 3 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow3.png");
            case 4 -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow4.png");
            default -> ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/eyes_glow.png");
        };
    }

    @Override
    public void render(@NotNull PoseStack stack, @NotNull MultiBufferSource buffer, int packedLight,
                       @NotNull AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        if (!player.isAlive()) return;
        if (player.isInvisible()) return;
        // Se muestra a los kommandant y, además, a cualquiera con el modo Claws of Brutality activo.
        if (!isKommandant(player) && !BerserkerClawRenderState.isActive(player.getId())) return;

        PlayerData data = PlayerDataProvider.get(player);
        int baseType = data.getEyeBaseType();
        int glowType = data.getEyeGlowType();
        boolean caustic = isCaustic(player);
        boolean abyssal = isAbyssal(player);
        boolean gluttonous = isGluttonous(player);

        int packedOverlay = LivingEntityRenderer.getOverlayCoords(player, 0);
        ModelPart head = this.getParentModel().head;

        VertexConsumer base = buffer.getBuffer(RenderType.entityCutoutNoCull(getBaseTexture(baseType)));
        head.render(stack, base, packedLight, packedOverlay);

        VertexConsumer glow = buffer.getBuffer(RenderType.eyes(getGlowTexture(glowType, caustic, abyssal, gluttonous)));
        head.render(stack, glow, packedLight, packedOverlay);
    }
}