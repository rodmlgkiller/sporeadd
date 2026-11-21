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
        POWER5,
        POWER6,
        POWER7
    }

    /*
     * Orden configurable:
     * - POWER2: ejecución
     * - POWER5: alimentar un mound con biomasa (solo si power_5_manipulation.feed_mounds está activo)
     * - POWER4: cosecha biomasa
     * - POWER6: evolución forzada
     * - POWER7: montar entidad
     *
     * Cambia el orden de esta lista y cambias la prioridad real.
     */
    private static final List<InteractPower> PRIORITY_ORDER = List.of(
            InteractPower.POWER2,
            InteractPower.POWER5,
            InteractPower.POWER6,
            InteractPower.POWER7,
            InteractPower.POWER4

    );

    private static SkillClick skillOf(InteractPower power) {
        return switch (power) {
            case POWER2 -> SkillClick.POWER2_EXECUTE;
            case POWER4 -> SkillClick.POWER4_CONSUME;
            case POWER5 -> SkillClick.POWER5_FEED;
            case POWER6 -> SkillClick.POWER6_EVOLVE;
            case POWER7 -> SkillClick.POWER7_MOUNT;
        };
    }

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        Entity target = event.getTarget();
        if (target == null) return;

        for (InteractPower power : PRIORITY_ORDER) {
            // A skill rebound to its own key (Controls > SporeAdds) is not triggered by the Use click.
            if (!SkillBindings.useClickTriggers(player, skillOf(power))) continue;

            boolean consumed = switch (power) {
                case POWER2 -> Poder2.tryExecute(player, target);
                case POWER4 -> Poder4.tryHarvestEntity(player, target);
                case POWER5 -> Poder5.tryFeedMound(player, target);
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