package com.sporeadds.sporeaddsmod.event;

import net.minecraft.core.Holder;

import net.neoforged.fml.common.EventBusSubscriber;

import com.Harbinger.Spore.core.Seffects;
import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class BombImpactHandler {

    @SubscribeEvent
    public static void onBombImpact(ProjectileImpactEvent event) {
        if (event.getEntity() instanceof FleshBomb bomb && !bomb.level().isClientSide()) {
            ServerLevel level = (ServerLevel) bomb.level();
            int bombType = bomb.getBombType();

            // Interceptamos Fuego (1) y Ácido (3)
            if (bombType == 1 || bombType == 3) {
                int radius = bomb.getExplosion();
                float dmg = bomb.getDamage();
                Vec3 hitLocation = event.getRayTraceResult().getLocation();

                // --- LÓGICA DE LA BOMBA DE FUEGO ---
                if (bombType == 1) {
                    int fireSeconds = 8;
                    if (dmg >= 20.0F) fireSeconds = 20;
                    else if (dmg >= 15.0F) fireSeconds = 16;
                    else if (dmg >= 12.0F) fireSeconds = 10;

                    AABB aabb = new AABB(hitLocation, hitLocation).inflate(radius);
                    for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                        if (target.distanceToSqr(hitLocation) <= radius * radius) {
                            target.igniteForSeconds(fireSeconds);
                        }
                    }
                }

                // --- LÓGICA DE LA BOMBA DE ÁCIDO (Charco persistente) ---
                else if (bombType == 3) {
                    int cloudDuration = 200; // 10 segundos por defecto
                    int effectAmplifier = 0; // Nivel 1 de corrosión
                    int effectDuration = 100; // 5 segundos aplicados al bicho que lo pise

                    // Escalado según las variantes de tu GUI
                    if (dmg >= 20.0F) { // Nuke Acid
                        cloudDuration = 600; // El charco dura 30s
                        effectAmplifier = 3; // Corrosión Nivel 4
                        effectDuration = 300; // Dura 15s en el cuerpo
                    } else if (dmg >= 15.0F) { // Heavy Acid
                        cloudDuration = 400; // El charco dura 20s
                        effectAmplifier = 2; // Corrosión Nivel 3
                        effectDuration = 200;
                    } else if (dmg >= 12.0F) { // Carrier Acid
                        cloudDuration = 300; // El charco dura 15s
                        effectAmplifier = 1; // Corrosión Nivel 2
                        effectDuration = 150;
                    }

                    // Obtenemos el efecto de corrosión del mod base
                    Holder<MobEffect> corrosionEffect = Seffects.CORROSION;

                    if (corrosionEffect != null) {
                        // Creamos la nube de efecto
                        AreaEffectCloud acidCloud = new AreaEffectCloud(level, hitLocation.x, hitLocation.y, hitLocation.z);

                        // Si la bomba fue lanzada por alguien, se lo asignamos para que registre las muertes a su nombre
                        if (bomb.getOwner() instanceof LivingEntity owner) {
                            acidCloud.setOwner(owner);
                        }

                        // Configuración visual y física del charco
                        acidCloud.setRadius((float) radius * 1.2F); // Un poco más ancho que la explosión base
                        acidCloud.setDuration(cloudDuration);
                        acidCloud.setWaitTime(0); // Aplica el efecto instantáneamente al pisarlo

                        // Esto hace que el charco se encoja lentamente con el tiempo (como las pociones de Minecraft)
                        acidCloud.setRadiusPerTick(-acidCloud.getRadius() / (float) acidCloud.getDuration());

                        // Agregamos el efecto de corrosión a la nube
                        acidCloud.addEffect(new MobEffectInstance(corrosionEffect, effectDuration, effectAmplifier));

                        // Hacemos aparecer el charco en el mundo
                        level.addFreshEntity(acidCloud);
                    }
                }
            }
        }
    }
}