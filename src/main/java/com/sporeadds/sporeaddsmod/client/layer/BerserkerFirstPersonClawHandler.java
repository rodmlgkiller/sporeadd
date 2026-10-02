package com.sporeadds.sporeaddsmod.client.layer;

import com.sporeadds.sporeaddsmod.client.BerserkerClawRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Garras de Claws of Brutality en 1ª persona (mano del propio jugador). */
@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BerserkerFirstPersonClawHandler {

    private BerserkerFirstPersonClawHandler() {
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        if (player == null || !BerserkerClawRenderState.isActive(player.getId())) return;

        EntityRenderer<? super AbstractClientPlayer> renderer =
                Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof PlayerRenderer playerRenderer)) return;

        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
        HumanoidArm arm = event.getArm();
        ModelPart armPart = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        boolean slim = "slim".equals(player.getModelName());

        BerserkerClawRenderer.renderSingleHand(
                event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), armPart, arm, slim);
    }
}
