package com.sporeadds.sporeaddsmod.Powers;

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

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BiomassOnKill {

    private static final double ALMOST_BIOMASS_PER_LOW_MAX_HEALTH_KILL = 0.2D;
    private static final float LOW_MAX_HEALTH_THRESHOLD = 4.0F;

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

        creditedPlayer.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            if (victimMaxHealth < LOW_MAX_HEALTH_THRESHOLD) {
                spore.addAlmostBiomass(ALMOST_BIOMASS_PER_LOW_MAX_HEALTH_KILL);
            } else {
                spore.addSpore(1);
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