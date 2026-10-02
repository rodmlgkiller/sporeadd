package com.sporeadds.sporeaddsmod.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Utilidad para invertir en el tiempo una {@link AnimationDefinition} ya existente. */
public final class AnimationUtils {

    private AnimationUtils() {
    }

    /**
     * Devuelve una copia de {@code source} que reproduce exactamente la misma curva pero en
     * sentido inverso: lo que en {@code source} ocurre en el instante {@code t} ocurre aquí en
     * {@code length - t}. Sirve, por ejemplo, para convertir una animación de "abrirse" en su
     * "cerrarse" sin tener que exportar una segunda animación desde Blockbench.
     *
     * <p>La curva de cada tramo (incluido CATMULLROM, que depende de los vecinos) se conserva
     * correctamente porque se invierten a la vez el orden de los keyframes y su marca de tiempo;
     * una curva de Catmull-Rom recorrida así es exactamente la misma curva trazada al revés.
     */
    public static AnimationDefinition reverse(AnimationDefinition source) {
        float length = source.lengthInSeconds();
        Map<String, List<AnimationChannel>> reversedBones = new HashMap<>();

        for (Map.Entry<String, List<AnimationChannel>> entry : source.boneAnimations().entrySet()) {
            reversedBones.put(entry.getKey(), entry.getValue().stream()
                    .map(channel -> reverseChannel(channel, length))
                    .toList());
        }

        return new AnimationDefinition(length, source.looping(), reversedBones);
    }

    private static AnimationChannel reverseChannel(AnimationChannel channel, float length) {
        Keyframe[] original = channel.keyframes();
        int last = original.length - 1;
        Keyframe[] reversed = new Keyframe[original.length];

        for (int i = 0; i <= last; i++) {
            Keyframe mirroredPoint = original[last - i];
            // La curva del tramo que llega a este keyframe invertido es la que en el original
            // llegaba al keyframe que ahora queda "por delante" en el tiempo original (i=0 no
            // tiene tramo previo, así que su interpolación nunca se usa; se rellena igualmente).
            AnimationChannel.Interpolation interpolation = (i == 0)
                    ? mirroredPoint.interpolation()
                    : original[last - i + 1].interpolation();

            reversed[i] = new Keyframe(length - mirroredPoint.timestamp(), mirroredPoint.target(), interpolation);
        }

        return new AnimationChannel(channel.target(), reversed);
    }
}
