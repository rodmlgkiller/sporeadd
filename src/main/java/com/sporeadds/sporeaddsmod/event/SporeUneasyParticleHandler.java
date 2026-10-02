package com.sporeadds.sporeaddsmod.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class SporeUneasyParticleHandler {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        // Solo ejecutar en el lado del servidor
        if (entity.level().isClientSide) return;

        // Generar partículas solo cada 5 ticks (4 veces por segundo)
        // Esto es para no saturar la red ni causar lag enviando partículas en cada milisegundo
        if (entity.tickCount % 5 != 0) return;

        // Verificar si la entidad tiene el efecto spore:uneasy
        MobEffect uneasy = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "uneasy"));
        if (uneasy == null || !entity.hasEffect(uneasy)) return;

        ServerLevel level = (ServerLevel) entity.level();

        // Recorrer todos los jugadores del servidor para ver quién merece ver las partículas
        for (ServerPlayer viewer : level.players()) {

            // Verificar si el espectador está en el equipo "spore"
            if (viewer.getTeam() != null && viewer.getTeam().getName().equals("spore")) {

                // Asegurarse de que el espectador esté cerca (32 bloques de distancia máximo)
                // 1024 es 32 al cuadrado (distanceToSqr es mucho más rápido para el procesador que distanceTo)
                if (viewer.distanceToSqr(entity) < 1024.0D) {

                    // Enviar las partículas EXCLUSIVAMENTE a este jugador
                    // Parámetros: (jugador, tipo, verDeLejos, X, Y, Z, cantidad, offsetX, offsetY, offsetZ, velocidad)
                    level.sendParticles(viewer, ParticleTypes.WITCH, false,
                            entity.getX(),
                            entity.getY() + (entity.getBbHeight() * 0.5), // Mitad del cuerpo
                            entity.getZ(),
                            2,     // Cantidad de partículas por ciclo
                            0.4D,  // Dispersión en X
                            0.5D,  // Dispersión en Y
                            0.4D,  // Dispersión en Z
                            0.0D   // Velocidad de la partícula
                    );
                }
            }
        }
    }
}