package com.sporeadds.sporeaddsmod.event;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Protector;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.List;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class SporeTeamProtectionEvent {

    @SubscribeEvent
    public static void onPlayerHurt(LivingDamageEvent.Pre event) {
        // 1. Verificamos si quien recibe el daño es un Jugador
        if (event.getEntity() instanceof Player victim) {

            // 2. Verificamos si la víctima está en el team "spore"
            if (victim.getTeam() != null && victim.getTeam().getName().equals("spore")) {

                LivingEntity attacker = (LivingEntity) event.getSource().getEntity();

                // Si el atacante existe, está vivo y NO es la propia víctima (daño por caída, lava, etc.)
                if (attacker != null && attacker != victim) {

                    // 3. Escaneamos un radio inmenso (64 bloques) buscando Protectores
                    List<Protector> protectors = victim.level().getEntitiesOfClass(
                            Protector.class,
                            victim.getBoundingBox().inflate(64.0D)
                    );

                    // 4. Activamos la mecánica de rabia para cada Protector encontrado
                    for (Protector protector : protectors) {

                        // Asegurarnos de que el Protector no ataque a otro miembro del team spore por accidente
                        if (attacker instanceof Player pAttacker && pAttacker.getTeam() != null && pAttacker.getTeam().getName().equals("spore")) {
                            continue; // Ignorar fuego amigo
                        }

                        // Forzar al protector a fijar su objetivo en el agresor
                        protector.setTarget(attacker);

                        // Darle el buff de Velocidad I durante 5 segundos (100 ticks)
                        protector.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0));

                        // Si está usando el Mixin de guardaespaldas que hicimos,
                        // bajarle el escudo para que pueda correr inmediatamente a por el objetivo
                        protector.setShielded(false);
                    }
                }
            }
        }
    }
}