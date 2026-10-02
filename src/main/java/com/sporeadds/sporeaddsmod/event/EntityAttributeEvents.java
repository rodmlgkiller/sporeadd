package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.entity.DecoyEntity;
import com.sporeadds.sporeaddsmod.entity.MeatAbomination;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import com.sporeadds.sporeaddsmod.entity.StaticEntity;
import com.sporeadds.sporeaddsmod.entity.Tentacle;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityAttributeEvents {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COCOON.get(), StaticEntity.createAttributes().build());
        event.put(ModEntities.TENTACLE.get(), Tentacle.createAttributes().build());
        event.put(ModEntities.MEAT_ABOMINATION.get(), MeatAbomination.createAttributes().build());
        event.put(ModEntities.DECOY.get(), DecoyEntity.createAttributes().build());
    }
}