package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.client.PunishmentSoundDamper;
import com.sporeadds.sporeaddsmod.client.hive.HiveSoundDamper;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Permite a {@link HiveSoundDamper} reducir el volumen de las categorías de sonido "de mundo"
 * durante la cinemática de la colmena, sin tocar el fichero options.txt y funcionando tras
 * reobfuscar (el enfoque de reflexión de {@code AmbientSoundDamper} no funciona en producción).
 */
@Mixin(Options.class)
public abstract class SoundVolumeDamperMixin {

    @Inject(method = "getSoundSourceVolume", at = @At("RETURN"), cancellable = true)
    private void sporeadds$hiveDuck(SoundSource source, CallbackInfoReturnable<Float> cir) {
        float multiplier = HiveSoundDamper.multiplierFor(source) * PunishmentSoundDamper.multiplierFor(source);
        if (multiplier >= 1.0F) return;
        cir.setReturnValue(cir.getReturnValueF() * multiplier);
    }
}
