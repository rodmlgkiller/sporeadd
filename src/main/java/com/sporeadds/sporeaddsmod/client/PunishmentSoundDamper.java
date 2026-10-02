package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.effects.PunishmentEffect;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Reduce el volumen de las categorías de sonido "de mundo" mientras el jugador tiene el efecto
 * {@link PunishmentEffect}: -30% por nivel, hasta un máximo del 90%. Funciona a través del mixin
 * {@code SoundVolumeDamperMixin} sobre {@code Options#getSoundSourceVolume} (igual que
 * {@link com.sporeadds.sporeaddsmod.client.hive.HiveSoundDamper}). MASTER no se reduce.
 */
@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class PunishmentSoundDamper {

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

    private static float currentMultiplier = 1.0F;

    private PunishmentSoundDamper() {
    }

    private static boolean isDucked(SoundSource source) {
        for (SoundSource s : DUCKED_CATEGORIES) {
            if (s == source) return true;
        }
        return false;
    }

    /** Llamado desde el mixin. 1.0 = sin cambio. */
    public static float multiplierFor(SoundSource source) {
        return (currentMultiplier < 1.0F && source != null && isDucked(source)) ? currentMultiplier : 1.0F;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        float target = 1.0F;
        if (mc.player != null) {
            MobEffectInstance inst = mc.player.getEffect(effects.PUNISHMENT.get());
            if (inst != null) {
                int level = inst.getAmplifier() + 1;
                float reduction = Math.min(PunishmentEffect.SOUND_REDUCTION_MAX,
                        PunishmentEffect.SOUND_REDUCTION_PER_LEVEL * level);
                target = 1.0F - reduction;
            }
        }

        if (Math.abs(target - currentMultiplier) > 0.001F) {
            currentMultiplier = target;
            pushPlayingSounds(mc);
        }
    }

    private static void pushPlayingSounds(Minecraft mc) {
        if (mc.getSoundManager() == null) return;
        for (SoundSource source : DUCKED_CATEGORIES) {
            mc.getSoundManager().updateSourceVolume(source, 1.0F);
        }
    }
}
