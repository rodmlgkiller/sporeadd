package com.sporeadds.sporeaddsmod.client.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.sporeadds.sporeaddsmod.client.ClientGasSphereData;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class GasSphereNametagHandler {

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        Entity entity = event.getEntity();

        boolean isSporeEntity = entity instanceof Infected || entity instanceof UtilityEntity;
        boolean isKommandantPlayer = entity instanceof Player player && SporeClassUtil.hasClass(player, "kommandant");

        if (!isSporeEntity && !isKommandantPlayer) {
            return;
        }

        if (ClientGasSphereData.isInsideAny(entity.position())) {
            event.setCanRender(net.neoforged.neoforge.common.util.TriState.FALSE);
        }
    }
}
