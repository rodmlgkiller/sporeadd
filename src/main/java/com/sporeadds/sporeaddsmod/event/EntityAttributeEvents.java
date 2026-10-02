package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.entity.DecoyEntity;
import com.sporeadds.sporeaddsmod.entity.MeatAbomination;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import com.sporeadds.sporeaddsmod.entity.StaticEntity;
import com.sporeadds.sporeaddsmod.entity.Tentacle;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.MOD)
public class EntityAttributeEvents {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COCOON.get(), StaticEntity.createAttributes().build());
        event.put(ModEntities.TENTACLE.get(), Tentacle.createAttributes().build());
        event.put(ModEntities.MEAT_ABOMINATION.get(), MeatAbomination.createAttributes().build());
        event.put(ModEntities.DECOY.get(), DecoyEntity.createAttributes().build());
    }
}