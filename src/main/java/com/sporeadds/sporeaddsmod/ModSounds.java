package com.sporeadds.sporeaddsmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "sporeadd");

    public static final RegistryObject<SoundEvent> DETECTION = SOUND_EVENTS.register(
            "detection",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("sporeadd", "detection"))
    );

    /** Sonido de "tecleo" de la cinemática de la colmena (uno por carácter). */
    public static final RegistryObject<SoundEvent> HIVE_TYPE = SOUND_EVENTS.register(
            "hive_type",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("sporeadd", "hive_type"))
    );

    /** Latido en bucle (8 s, 8 pares de pulsaciones) del efecto "punishment". */
    public static final RegistryObject<SoundEvent> HEARTBEAT = SOUND_EVENTS.register(
            "heartbeat",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("sporeadd", "heartbeat"))
    );

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}