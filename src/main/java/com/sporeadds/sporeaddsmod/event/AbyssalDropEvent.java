package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AbyssalDropEvent {

    private static final String ABYSSAL_VORTEX_MARK = "sporeadd_abyssal_vortex_mark";

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        if (entity.getPersistentData().getBoolean(ABYSSAL_VORTEX_MARK)) {
            if (entity.getRandom().nextFloat() < 0.05F) {
                entity.spawnAtLocation(ModItems.MUTATION_ESSENCE.get());
            }
        }
    }
}