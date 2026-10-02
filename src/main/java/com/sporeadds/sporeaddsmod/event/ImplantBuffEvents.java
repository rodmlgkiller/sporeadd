package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.capabilities.PlayerImplantsCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd")
public class ImplantBuffEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player))
            return;

        player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
            // LEFT LEG: Jump boost
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.LEFT_LEG).isEmpty()) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 3, true, false));
            }

            // EYE IMPLANT: Glowing en equipo Spore si agachado
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.EYE).isEmpty()) {
                if (player.isCrouching() && player.tickCount % 20 == 0) { // Cada 1 segundo

                    int entitiesFound = 0;
                    int sporeEntitiesMarked = 0;

                    // Buscar TODAS las entidades en un radio de 25 bloques
                    net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
                            player.getX() - 25, player.getY() - 25, player.getZ() - 25,
                            player.getX() + 25, player.getY() + 25, player.getZ() + 25
                    );

                    // Obtener todas las LivingEntities en el área
                    java.util.List<LivingEntity> nearbyEntities = player.serverLevel().getEntitiesOfClass(
                            LivingEntity.class,
                            searchArea
                    );

                    for (LivingEntity entity : nearbyEntities) {
                        if (entity == player) continue; // Saltarse a sí mismo

                        double distance = player.distanceTo(entity);
                        if (distance <= 25.0) {
                            entitiesFound++;

                            // Verificar si la entidad está en el equipo "spore"
                            if (entity.getTeam() != null && "spore".equalsIgnoreCase(entity.getTeam().getName())) {
                                sporeEntitiesMarked++;
                                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));

                                // Debug: mostrar qué tipo de entidad fue marcada
                                String entityName = entity.getName().getString();
                                player.displayClientMessage(Component.literal(
                                        "§6Eye Implant: Marked " + entityName + " with glowing!"
                                ), true);
                            }
                        }
                    }

                    // Debug: informar al usuario del eye implant
                    player.displayClientMessage(Component.literal(
                            "§7Eye Implant: Found " + entitiesFound + " entities, " + sporeEntitiesMarked + " infected marked."
                    ), true);
                }
            }
        });
    }

    // ← EVENTO ESPECÍFICO PARA DAÑO DE CAÍDA
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
            // RIGHT LEG: Inmunidad daño de caída
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.RIGHT_LEG).isEmpty()) {
                event.setCanceled(true); // Cancela completamente el daño de caída
            }
        });
    }

    // ← EVENTO PARA DAÑO RECIBIDO (TORSO)
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
            // TORSO: -25% Daño recibido
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.TORSO).isEmpty()) {
                float originalDamage = event.getAmount();
                event.setAmount(originalDamage * 0.75f);
            }
        });
    }

    // ← EVENTO PARA DAÑO CAUSADO (RIGHT ARM + LEFT ARM)
    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
            // RIGHT ARM: +20% daño
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.RIGHT_ARM).isEmpty()) {
                event.setAmount(event.getAmount() * 1.20f);
            }

            // LEFT ARM: Efectos negativos random al golpear mobs
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.LEFT_ARM).isEmpty()) {
                LivingEntity target = event.getEntity();
                if (target != player && player.getRandom().nextFloat() < 0.25f) { // 25%
                    int rand = player.getRandom().nextInt(4);
                    switch (rand) {
                        case 0:
                            target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 3));
                            break;
                        case 1:
                            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 2));
                            break;
                        case 2:
                            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                            break;
                        case 3:
                            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                            break;
                    }
                }
            }
        });
    }
}
