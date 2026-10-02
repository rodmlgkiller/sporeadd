package com.sporeadds.sporeaddsmod.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.ModSounds;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Cliente del efecto {@link com.sporeadds.sporeaddsmod.effects.PunishmentEffect}:
 *  - reproduce en bucle el sonido {@code sporeadd:heartbeat} (8 s, 8 pares de pulsaciones)
 *    mientras el jugador local tiene el efecto, y lo detiene al acabar / al salir.
 *  - lleva un "reloj de fase" alineado con el inicio del audio, del que
 *    {@link #beatIntensity(float)} deriva el pulso (0..1) al que se sincroniza el viñeteado
 *    de {@link PunishmentOverlay}.
 *
 * <p>El "tier" (0..2, según el amplifier del efecto) acelera a la vez el audio y la animación,
 * de forma que siguen sincronizados:
 * <ul>
 *   <li>tier 0 (amplifier 0): pitch 1.0, animación x1.0</li>
 *   <li>tier 1 (amplifier 1): pitch 1.5, animación x1.5 (+50%)</li>
 *   <li>tier 2 (amplifier &ge; 2): pitch 2.0, animación x2.0 (+100%)</li>
 * </ul>
 * El color y el tamaño del borde los decide {@link PunishmentOverlay} a partir del mismo tier.
 */
@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class PunishmentClientHandler {

    /** Duración base del sonido en ticks (8 s * 20 tps) a pitch 1.0. */
    private static final int CYCLE_TICKS = 160;
    /** Ticks por par de pulsaciones (uno de los 8) a pitch 1.0. */
    private static final float PAIR_TICKS = CYCLE_TICKS / 8.0F;

    private static final int MAX_TIER = 2;

    private static boolean active;
    private static int activeTier = -1;
    /** Fase 0..CYCLE_TICKS; avanza a {@code speedFactor(tier)} por tick. */
    private static double phase;
    private static SimpleSoundInstance heartbeat;

    private PunishmentClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        MobEffectInstance inst = mc.player == null ? null : mc.player.getEffect(effects.PUNISHMENT);

        if (inst == null) {
            if (active) stop(mc);
            return;
        }

        int tier = Mth.clamp(inst.getAmplifier(), 0, MAX_TIER);
        if (!active || tier != activeTier) {
            start(mc, tier);   // (re)lanza el audio con el pitch del tier y reinicia la fase
        }

        phase = (phase + speedFactor(activeTier)) % CYCLE_TICKS;

        // Si el sonido dejó de sonar (fin de mundo, recarga de recursos, etc.) lo relanzamos.
        if (heartbeat != null && !mc.getSoundManager().isActive(heartbeat)) {
            mc.getSoundManager().play(heartbeat);
            phase = 0.0D;
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        stop(Minecraft.getInstance());
    }

    private static void start(Minecraft mc, int tier) {
        stopSound(mc);
        active = true;
        activeTier = tier;
        phase = 0.0D;
        heartbeat = new SimpleSoundInstance(
                ModSounds.HEARTBEAT.get().getLocation(),
                SoundSource.MASTER,          // MASTER: no lo amortigua PunishmentSoundDamper
                1.0F, speedFactor(tier),     // volumen, pitch (1.0 / 1.5 / 2.0)
                RandomSource.create(),
                true,                        // en bucle
                0,
                SoundInstance.Attenuation.NONE,
                0.0D, 0.0D, 0.0D,
                true                         // relativo al oyente
        );
        mc.getSoundManager().play(heartbeat);
    }

    private static void stop(Minecraft mc) {
        active = false;
        activeTier = -1;
        phase = 0.0D;
        stopSound(mc);
    }

    private static void stopSound(Minecraft mc) {
        if (heartbeat != null) {
            mc.getSoundManager().stop(heartbeat);
            heartbeat = null;
        }
    }

    /** Factor de velocidad (audio y animación) del tier: 1.0 / 1.5 / 2.0. */
    public static float speedFactor(int tier) {
        if (tier <= 0) return 1.0F;
        return tier == 1 ? 1.5F : 2.0F;
    }

    /** Tier visual actual (0..2). 0 si el efecto no está activo. */
    public static int tier() {
        return active ? activeTier : 0;
    }

    /**
     * Intensidad del latido en [0,1] en el instante actual, sincronizada con los 8 pares de
     * pulsaciones del audio. 0 si el efecto no está activo.
     */
    public static float beatIntensity(float partialTick) {
        if (!active) return 0.0F;
        double cyclePos = (phase + partialTick * speedFactor(activeTier)) % CYCLE_TICKS;
        float f = (float) ((cyclePos % PAIR_TICKS) / PAIR_TICKS);   // 0..1 dentro de un par
        float lub = triangle(f, 0.00F, 0.12F);
        float dub = 0.70F * triangle(f, 0.30F, 0.12F);
        return Math.max(lub, dub);
    }

    /** Pico triangular centrado en {@code c} con semiancho {@code w}, sobre un eje cíclico [0,1). */
    private static float triangle(float x, float c, float w) {
        float d = Math.abs(x - c);
        d = Math.min(d, 1.0F - d);
        return Math.max(0.0F, 1.0F - d / w);
    }
}
