package com.sporeadds.sporeaddsmod.client.hive;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumSet;

/**
 * Reducción de sonido para la cinemática de la colmena.
 *
 * A diferencia de {@code AmbientSoundDamper} (que usa reflexión con un nombre de campo que
 * NO existe tras reobfuscar, por lo que en producción no hace nada), esto funciona vía el
 * mixin {@code SoundVolumeDamperMixin} sobre {@code Options#getSoundSourceVolume}: mientras
 * el "duck" está activo, ese método devuelve el volumen original multiplicado por
 * {@link #multiplierFor(SoundSource)}. Además, cada tick se fuerza a los sonidos ya en
 * reproducción a recalcular su volumen para que la transición se note en la música/ambiente.
 *
 * MASTER no se reduce (ahí van los sonidos de la propia cinemática, p.ej. el tecleo).
 */
@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class HiveSoundDamper {

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

    private static final EnumSet<SoundSource> DUCKED_SET = EnumSet.noneOf(SoundSource.class);

    static {
        for (SoundSource source : DUCKED_CATEGORIES) {
            DUCKED_SET.add(source);
        }
    }

    private static boolean active = false;
    private static boolean ducking = false;
    private static int tick = 0;
    private static int transitionTicks = 60;
    private static float targetMultiplier = 0.10F;
    private static float currentMultiplier = 1.0F;

    private HiveSoundDamper() {
    }

    public static void beginDuck(int transitionDurationTicks, float target) {
        if (active) return;
        transitionTicks = Math.max(1, transitionDurationTicks);
        targetMultiplier = Math.max(0.0F, Math.min(1.0F, target));
        currentMultiplier = 1.0F;
        active = true;
        ducking = true;
        tick = 0;
    }

    public static void endDuck() {
        if (!active) return;
        ducking = false;
        tick = 0;
    }

    /** Llamado desde el mixin. 1.0 = sin cambio. */
    public static float multiplierFor(SoundSource source) {
        return (active && source != null && DUCKED_SET.contains(source)) ? currentMultiplier : 1.0F;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !active) return;

        tick++;
        float progress = Math.min(1.0F, tick / (float) transitionTicks);
        float from = ducking ? 1.0F : targetMultiplier;
        float to = ducking ? targetMultiplier : 1.0F;
        currentMultiplier = from + (to - from) * progress;

        pushPlayingSounds();

        if (progress >= 1.0F && !ducking) {
            active = false;
            currentMultiplier = 1.0F;
            pushPlayingSounds();
        }
    }

    private static void pushPlayingSounds() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null) return;
        for (SoundSource source : DUCKED_CATEGORIES) {
            // Para categorías != MASTER el float se ignora: fuerza recalcular calculateVolume(),
            // que ahora lee el getSoundSourceVolume ya reducido por el mixin.
            mc.getSoundManager().updateSourceVolume(source, 1.0F);
        }
    }
}
