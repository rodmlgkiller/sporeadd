package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.capabilities.PlayerImplantsCapability;
import com.sporeadds.sporeaddsmod.config.ConfigEffects;
import com.sporeadds.sporeaddsmod.config.ImplantConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Built-in abilities of the surgical implants. Every number comes from {@link ImplantConfig}
 * ({@code implant_stats.*}); the defaults are the original hard-coded values. The optional attribute modifiers of each
 * implant are applied by {@link ImplantAttributeSync}.
 */
@Mod.EventBusSubscriber(modid = "sporeadd")
public class ImplantBuffEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player))
            return;

        player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
            // Atributos configurables (implant_stats.<implante>.attribute_modifiers): reconciliación cada segundo.
            if (player.tickCount % 20 == 0) {
                ImplantAttributeSync.sync(player, implants);
            }

            // LEFT LEG: Jump boost
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.LEFT_LEG).isEmpty()
                    && ImplantConfig.LEFT_LEG_ENABLED.get()) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40,
                        ImplantConfig.LEFT_LEG_JUMP_BOOST_AMPLIFIER.get(), true, false));
            }

            // EYE IMPLANT: Glowing en equipo Spore si agachado
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.EYE).isEmpty()
                    && ImplantConfig.EYE_ENABLED.get()) {
                if (player.isCrouching() && player.tickCount % ImplantConfig.EYE_SCAN_INTERVAL_TICKS.get() == 0) { // Cada 1 segundo (por defecto)

                    int entitiesFound = 0;
                    int sporeEntitiesMarked = 0;

                    double radius = ImplantConfig.EYE_SCAN_RADIUS.get();
                    int glowingTicks = ImplantConfig.EYE_GLOWING_TICKS.get();

                    // Buscar TODAS las entidades en un radio de 25 bloques (por defecto)
                    net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
                            player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                            player.getX() + radius, player.getY() + radius, player.getZ() + radius
                    );

                    // Obtener todas las LivingEntities en el área
                    java.util.List<LivingEntity> nearbyEntities = player.serverLevel().getEntitiesOfClass(
                            LivingEntity.class,
                            searchArea
                    );

                    for (LivingEntity entity : nearbyEntities) {
                        if (entity == player) continue; // Saltarse a sí mismo

                        double distance = player.distanceTo(entity);
                        if (distance <= radius) {
                            entitiesFound++;

                            // Verificar si la entidad está en el equipo "spore"
                            if (entity.getTeam() != null && "spore".equalsIgnoreCase(entity.getTeam().getName())) {
                                sporeEntitiesMarked++;
                                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, glowingTicks, 0, true, false));

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

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ImplantAttributeSync.forget(event.getEntity().getUUID());
    }

    // ← EVENTO ESPECÍFICO PARA DAÑO DE CAÍDA
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
            // RIGHT LEG: Inmunidad daño de caída (por defecto; multiplicador configurable)
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.RIGHT_LEG).isEmpty()
                    && ImplantConfig.RIGHT_LEG_ENABLED.get()) {
                float multiplier = ImplantConfig.RIGHT_LEG_FALL_DAMAGE_MULTIPLIER.get().floatValue();
                if (multiplier <= 0.0f) {
                    event.setCanceled(true); // Cancela completamente el daño de caída
                } else {
                    event.setDamageMultiplier(event.getDamageMultiplier() * multiplier);
                }
            }
        });
    }

    // ← EVENTO PARA DAÑO RECIBIDO (TORSO)
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
            // TORSO: -25% Daño recibido (por defecto)
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.TORSO).isEmpty()
                    && ImplantConfig.TORSO_ENABLED.get()) {
                float originalDamage = event.getAmount();
                event.setAmount(originalDamage * ImplantConfig.TORSO_DAMAGE_TAKEN_MULTIPLIER.get().floatValue());
            }
        });
    }

    // ← EVENTO PARA DAÑO CAUSADO (RIGHT ARM + LEFT ARM)
    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
            // RIGHT ARM: +20% daño (por defecto)
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.RIGHT_ARM).isEmpty()
                    && ImplantConfig.RIGHT_ARM_ENABLED.get()) {
                event.setAmount(event.getAmount() * ImplantConfig.RIGHT_ARM_DAMAGE_DEALT_MULTIPLIER.get().floatValue());
            }

            // LEFT ARM: Efectos negativos random al golpear mobs
            if (!implants.getImplant(PlayerImplantsCapability.ImplantType.LEFT_ARM).isEmpty()
                    && ImplantConfig.LEFT_ARM_ENABLED.get()) {
                LivingEntity target = event.getEntity();
                if (target != player && player.getRandom().nextFloat() < ImplantConfig.LEFT_ARM_EFFECT_CHANCE.get().floatValue()) { // 25% por defecto
                    List<ConfigEffects.Entry> effects = ConfigEffects.parseAll(ImplantConfig.LEFT_ARM_EFFECTS.get());
                    if (!effects.isEmpty()) {
                        // Por defecto: veneno IV, debilidad III, lentitud III (6 s) o ceguera (2 s), todos equiprobables.
                        int rand = player.getRandom().nextInt(effects.size());
                        target.addEffect(effects.get(rand).instance());
                    }
                }
            }
        });
    }
}
