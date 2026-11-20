package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.entity.Tentacle;
import com.sporeadds.sporeaddsmod.entity.projectile.TentacleProjectile;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BiomassOnKill {

    // The two numbers below used to be constants (0.2 and 4.0); they are now
    // kommandant_abilities.biomass_from_kills.low_health_biomass / low_health_threshold.

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();

        ServerPlayer creditedPlayer = null;

        if (attacker instanceof ServerPlayer player) {
            creditedPlayer = player;
        } else {
            Tentacle tentacle = findAttachedTentacle(victim);
            if (tentacle != null && tentacle.isAlive()) {
                Entity owner = tentacle.getTentacleOwner();
                if (owner instanceof ServerPlayer playerOwner) {
                    creditedPlayer = playerOwner;
                }
            }
        }

        if (creditedPlayer == null) {
            return;
        }

        boolean isTentacleKill =
                direct instanceof TentacleProjectile ||
                        direct instanceof Tentacle ||
                        findAttachedTentacle(victim) != null;

        boolean isAllowedMeleeKill =
                !source.is(DamageTypes.ARROW) &&
                        !source.is(DamageTypes.MAGIC) &&
                        !source.is(DamageTypes.EXPLOSION) &&
                        !source.is(DamageTypes.TRIDENT);

        if (!isAllowedMeleeKill && !isTentacleKill) {
            return;
        }

        float victimMaxHealth = victim.getMaxHealth();
        Optional<Double> override = KommandantPowersConfig.killBiomassOverride(ForgeRegistries.ENTITY_TYPES.getKey(victim.getType()));

        creditedPlayer.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            if (override.isPresent()) {
                spore.addBiomass(override.get());
            } else if (KommandantPowersConfig.KILL_ONLY_LISTED.get()) {
                // only_listed_entities: nothing for entities that are not in the override list.
            } else if (victimMaxHealth < KommandantPowersConfig.KILL_LOW_HEALTH_THRESHOLD.get().floatValue()) {
                spore.addAlmostBiomass(KommandantPowersConfig.KILL_LOW_HEALTH_BIOMASS.get());
            } else {
                spore.addSpore(KommandantPowersConfig.KILL_BIOMASS.get());
            }
        });
    }

    private static Tentacle findAttachedTentacle(LivingEntity target) {
        for (Entity entity : target.level().getEntities(target, target.getBoundingBox().inflate(1.5D))) {
            if (entity instanceof Tentacle tentacle) {
                Entity attached = tentacle.getAttachedTarget();
                if (attached != null && attached.getId() == target.getId()) {
                    return tentacle;
                }
            }
        }
        return null;
    }
}