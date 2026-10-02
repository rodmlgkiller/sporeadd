package com.sporeadds.sporeaddsmod;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "sporeadd");

    public static final DeferredHolder<SoundEvent, SoundEvent> DETECTION = SOUND_EVENTS.register(
            "detection",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sporeadd", "detection"))
    );

    /** Sonido de "tecleo" de la cinemática de la colmena (uno por carácter). */
    public static final DeferredHolder<SoundEvent, SoundEvent> HIVE_TYPE = SOUND_EVENTS.register(
            "hive_type",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sporeadd", "hive_type"))
    );

    /** Latido en bucle (8 s, 8 pares de pulsaciones) del efecto "punishment". */
    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT = SOUND_EVENTS.register(
            "heartbeat",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sporeadd", "heartbeat"))
    );

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}