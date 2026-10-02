package com.sporeadds.mixin;

import com.Harbinger.Spore.ExtremelySusThings.BooleanCache;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(value = Utilities.class, remap = false)
public class UtilitiesMixin {

    // Interceptamos la CREACIÓN del BooleanCache dentro del bloque static de Utilities (<clinit>).
    // Esto nos permite cambiar el Predicate que se le pasa al constructor por el nuestro.
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "NEW",
                    target = "com/Harbinger/Spore/ExtremelySusThings/BooleanCache"
            )
    )
    private static BooleanCache<LivingEntity> sporeadds$redirectTargetSelector(int capacity, Predicate<LivingEntity> originalPredicate) {

        // Creamos nuestro propio predicado que contiene nuestra lógica personalizada
        Predicate<LivingEntity> customPredicate = (entity) -> {

            if (entity instanceof Player player) {
                // Comprobar si el jugador está en el equipo Spore
                if (player.getTeam() != null && "spore".equalsIgnoreCase(player.getTeam().getName())) {
                    return false;
                }

                // --- AÑADIDO: Comprobar si el jugador tiene el efecto de camuflaje ---
                if (player.getPersistentData().getBoolean("wasCamouflaged")) {
                    return false;
                }
            }

            // Lógica original restaurada para el resto de entidades
            if (entity instanceof Infected ||
                    entity instanceof UtilityEntity ||
                    entity instanceof TrueCalamity) {
                return false;
            } else if ((entity instanceof AbstractFish || entity instanceof Animal)
                    && !com.Harbinger.Spore.core.SConfig.SERVER.at_an.get()) {
                return false;
            } else if (!com.Harbinger.Spore.core.SConfig.SERVER.blacklist.get().isEmpty()) {
                for(String string : com.Harbinger.Spore.core.SConfig.SERVER.blacklist.get()){
                    if (string.endsWith(":") && entity.getEncodeId() != null){
                        String[] mod = string.split(":");
                        String[] iterations = entity.getEncodeId().split(":");
                        if (Objects.equals(mod[0], iterations[0])){
                            return false;
                        }
                    }
                }
                return !com.Harbinger.Spore.core.SConfig.SERVER.blacklist.get().contains(entity.getEncodeId());
            }
            return true;
        };

        // Devolvemos la nueva instancia de BooleanCache, pero usará nuestro predicado
        return new BooleanCache<>(capacity, customPredicate);
    }
}