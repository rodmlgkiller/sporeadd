package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PowerEntityInteractResolver {

    public enum InteractPower {
        POWER2,
        POWER4,
        POWER6,
        POWER7
    }

    /*
     * Orden configurable:
     * - POWER2: ejecución
     * - POWER4: cosecha biomasa
     * - POWER6: evolución forzada
     * - POWER7: montar entidad
     *
     * Cambia el orden de esta lista y cambias la prioridad real.
     */
    private static final List<InteractPower> PRIORITY_ORDER = List.of(
            InteractPower.POWER2,
            InteractPower.POWER6,
            InteractPower.POWER7,
            InteractPower.POWER4

    );

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        Entity target = event.getTarget();
        if (target == null) return;

        for (InteractPower power : PRIORITY_ORDER) {
            boolean consumed = switch (power) {
                case POWER2 -> Poder2.tryExecute(player, target);
                case POWER4 -> Poder4.tryHarvestEntity(player, target);
                case POWER6 -> Poder6.tryForceEvolve(player, target);
                case POWER7 -> Poder7.tryMount(player, target);
            };

            if (consumed) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }
    }
}