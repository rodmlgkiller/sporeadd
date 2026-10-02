package com.sporeadds.sporeaddsmod.client.actionwheel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class AmbientSoundDamper {

    private static final SoundSource[] DUCKED_CATEGORIES = {
            SoundSource.BLOCKS,
            SoundSource.HOSTILE,
            SoundSource.NEUTRAL,
            SoundSource.AMBIENT,
            SoundSource.WEATHER,
            SoundSource.RECORDS,
            SoundSource.MUSIC,
            SoundSource.PLAYERS,
            SoundSource.VOICE
    };

    private static final float DEFAULT_TARGET_MULTIPLIER = 0.10F;
    private static final int DEFAULT_TRANSITION_TICKS = 10;

    private static final Map<SoundSource, Double> ORIGINAL_VOLUMES = new EnumMap<>(SoundSource.class);

    private static boolean active = false;
    private static boolean ducking = false;
    private static int transitionTick = 0;
    private static float currentMultiplier = 1.0F;

    private static float targetMultiplier = DEFAULT_TARGET_MULTIPLIER;
    private static int transitionTicks = DEFAULT_TRANSITION_TICKS;

    private AmbientSoundDamper() {
    }

    public static void beginDuck() {
        beginDuck(DEFAULT_TRANSITION_TICKS, DEFAULT_TARGET_MULTIPLIER);
    }

    /**
     * @param transitionDurationTicks duración de la transición (ida y vuelta).
     * @param target                  multiplicador de volumen objetivo (0..1). 0.10 = -90%.
     */
    public static void beginDuck(int transitionDurationTicks, float target) {
        if (active) return;

        Options options = Minecraft.getInstance().options;

        for (SoundSource source : DUCKED_CATEGORIES) {
            ORIGINAL_VOLUMES.put(source, (double) options.getSoundSourceVolume(source));
        }

        targetMultiplier = Math.max(0.0F, Math.min(1.0F, target));
        transitionTicks = Math.max(1, transitionDurationTicks);

        active = true;
        ducking = true;
        transitionTick = 0;
    }

    public static void endDuck() {
        if (!active) return;
        ducking = false;
        transitionTick = 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!active) return;

        transitionTick++;
        float progress = Math.min(1.0F, transitionTick / (float) transitionTicks);

        float from = ducking ? 1.0F : targetMultiplier;
        float to = ducking ? targetMultiplier : 1.0F;

        currentMultiplier = from + (to - from) * progress;

        applyMultiplier(currentMultiplier);

        if (progress >= 1.0F && !ducking) {
            restoreOriginalVolumes();
            active = false;
        }
    }

    private static void applyMultiplier(float multiplier) {
        Options options = Minecraft.getInstance().options;

        for (SoundSource source : DUCKED_CATEGORIES) {
            double original = ORIGINAL_VOLUMES.getOrDefault(source, 1.0D);
            double scaled = original * multiplier;
            setVolumeReflective(options, source, scaled);
        }
    }

    private static void restoreOriginalVolumes() {
        Options options = Minecraft.getInstance().options;

        for (SoundSource source : DUCKED_CATEGORIES) {
            double original = ORIGINAL_VOLUMES.getOrDefault(source, 1.0D);
            setVolumeReflective(options, source, original);
        }
    }

    /**
     * Options no expone un setter público por categoría en 1.20.1,
     * así que accedemos al OptionInstance interno vía reflexión para
     * fijar el valor en memoria (no se guarda en options.txt).
     */
    private static void setVolumeReflective(Options options, SoundSource source, double value) {
        try {
            Field field = Options.class.getDeclaredField("soundSourceVolumes");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<SoundSource, net.minecraft.client.OptionInstance<Double>> map =
                    (Map<SoundSource, net.minecraft.client.OptionInstance<Double>>) field.get(options);

            net.minecraft.client.OptionInstance<Double> instance = map.get(source);
            if (instance != null) {
                instance.set(Math.max(0.0D, Math.min(1.0D, value)));
            }
        } catch (Exception ignored) {
        }
    }
}