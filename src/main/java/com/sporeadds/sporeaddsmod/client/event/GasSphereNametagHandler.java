package com.sporeadds.sporeaddsmod.client.event;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.sporeadds.sporeaddsmod.client.ClientGasSphereData;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
            event.setResult(Event.Result.DENY);
        }
    }
}
