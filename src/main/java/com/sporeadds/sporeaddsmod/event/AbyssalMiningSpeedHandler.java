package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd")
public class AbyssalMiningSpeedHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();

        boolean isAbyssal = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data ->
                        "kommandant".equalsIgnoreCase(data.getIdentifier()) &&
                                "abyssal".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);

        if (!isAbyssal) {
            return;
        }

        boolean underwater = player.isEyeInFluidType(net.neoforged.neoforge.common.ForgeMod.WATER_TYPE.get());
        boolean airborne = !player.onGround();

        float speed = event.getOriginalSpeed();

        // Si está bajo el agua sin apoyo, vanilla suele penalizar x5 y x5 otra vez.
        // Aquí restauramos la velocidad original multiplicando según corresponda.
        if (underwater) {
            speed *= 5.0F;
        }

        if (airborne) {
            speed *= 5.0F;
        }

        event.setNewSpeed(speed);
    }
}