package com.sporeadds.sporeaddsmod.event.TentacleHandler;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.entity.Tentacle;
import com.sporeadds.sporeaddsmod.entity.projectile.TentacleProjectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class ConstrictionEvents {

    private static final String PREV_AIR_TAG = "sporeadds_prev_air";
    private static final String CONSTRICTION_DROWN_TAG = "sporeadds_constriction_drown";
    private static final int AIR_DRAIN_PER_TICK = 2;
    private static final float OUTGOING_DAMAGE_MULTIPLIER = 0.10F;
    private static final float MIN_DROWN_DAMAGE_HEALTH_RATIO = 0.10F;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        if (!entity.hasEffect(effects.CONSTRICTION.get())) {
            clearConstrictionTags(entity);
            return;
        }

        int currentAir = entity.getAirSupply();
        int prevAir = entity.getPersistentData().contains(PREV_AIR_TAG)
                ? entity.getPersistentData().getInt(PREV_AIR_TAG)
                : currentAir;

        boolean isPlayer = entity instanceof net.minecraft.world.entity.player.Player;
        boolean underwater = entity.isUnderWater();

        if (isPlayer && !underwater) {
            if (currentAir > prevAir) {
                currentAir = prevAir;
            }

            if (entity.tickCount % 3 == 0) {
                currentAir -= 1;
            }
        } else {
            if (currentAir > prevAir) {
                currentAir = prevAir;
            }

            currentAir -= AIR_DRAIN_PER_TICK;
        }

        entity.setAirSupply(currentAir);

        if (currentAir <= -20) {
            entity.getPersistentData().putBoolean(CONSTRICTION_DROWN_TAG, true);
            entity.hurt(
                    entity.damageSources().drown(),
                    Math.max(2.0F, entity.getMaxHealth() * MIN_DROWN_DAMAGE_HEALTH_RATIO)
            );
            entity.setAirSupply(0);
        }

        entity.getPersistentData().putInt(PREV_AIR_TAG, entity.getAirSupply());
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        if (attacker instanceof LivingEntity livingAttacker) {
            if (tryReleaseSporeGrab(victim, livingAttacker)) {
                event.setCanceled(true);
                return;
            }
        }

        if (victim.hasEffect(effects.CONSTRICTION.get())
                && source.is(DamageTypes.DROWN)
                && victim.getPersistentData().getBoolean(CONSTRICTION_DROWN_TAG)) {
            float minDamage = victim.getMaxHealth() * MIN_DROWN_DAMAGE_HEALTH_RATIO;
            event.setAmount(Math.max(event.getAmount(), minDamage));
        }

        if (victim instanceof Tentacle) {
            return;
        }

        if (attacker instanceof LivingEntity livingAttacker && livingAttacker.hasEffect(effects.CONSTRICTION.get())) {
            if (!source.is(DamageTypes.DROWN)) {
                event.setAmount(event.getAmount() * OUTGOING_DAMAGE_MULTIPLIER);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        if (!victim.hasEffect(effects.CONSTRICTION.get())) {
            return;
        }

        if (!source.is(DamageTypes.DROWN)) {
            return;
        }

        if (!victim.getPersistentData().getBoolean(CONSTRICTION_DROWN_TAG)) {
            return;
        }

        float minFinalDamage = victim.getMaxHealth() * MIN_DROWN_DAMAGE_HEALTH_RATIO;
        if (event.getAmount() < minFinalDamage) {
            event.setAmount(minFinalDamage);
        }

        victim.getPersistentData().remove(CONSTRICTION_DROWN_TAG);
    }

    private static boolean tryReleaseSporeGrab(LivingEntity victim, LivingEntity attacker) {
        if (!isOnSporeTeam(victim)) {
            return false;
        }

        Tentacle tentacle = null;
        LivingEntity trappedLiving = null;

        if (victim instanceof Tentacle hitTentacle) {
            Entity attached = hitTentacle.getAttachedTarget();
            if (!(attached instanceof LivingEntity attachedLiving) || !attachedLiving.isAlive()) {
                return false;
            }

            Entity owner = hitTentacle.getTentacleOwner();
            if (owner == null || owner.getId() != attacker.getId()) {
                return false;
            }

            tentacle = hitTentacle;
            trappedLiving = attachedLiving;
        } else {
            tentacle = findAttachedTentacle(victim);
            if (tentacle == null || !tentacle.isAlive()) {
                return false;
            }

            Entity owner = tentacle.getTentacleOwner();
            if (owner == null || owner.getId() != attacker.getId()) {
                return false;
            }

            trappedLiving = victim;
        }

        if (trappedLiving == null) {
            return false;
        }

        trappedLiving.removeEffect(effects.CONSTRICTION.get());

        TentacleProjectile projectile = findProjectileFor(attacker, trappedLiving);
        if (projectile != null && projectile.isAlive()) {
            projectile.releaseVictimAndReturn();
        }

        if (tentacle != null && tentacle.isAlive()) {
            tentacle.discard();
        }

        return true;
    }

    private static TentacleProjectile findProjectileFor(Entity owner, LivingEntity victim) {
        for (Entity entity : victim.level().getEntities(victim, victim.getBoundingBox().inflate(32.0D))) {
            if (entity instanceof TentacleProjectile projectile && projectile.isAlive()) {
                Entity projectileOwner = projectile.getOwnerById();
                Entity projectileVictim = projectile.getVictimById();

                if (projectileOwner != null
                        && projectileOwner.getId() == owner.getId()
                        && projectileVictim != null
                        && projectileVictim.getId() == victim.getId()) {
                    return projectile;
                }
            }
        }
        return null;
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

    private static boolean isOnSporeTeam(Entity entity) {
        return entity != null
                && entity.getTeam() != null
                && "spore".equalsIgnoreCase(entity.getTeam().getName());
    }

    private static void clearConstrictionTags(LivingEntity entity) {
        if (entity.getPersistentData().contains(PREV_AIR_TAG)) {
            entity.getPersistentData().remove(PREV_AIR_TAG);
        }
        if (entity.getPersistentData().contains(CONSTRICTION_DROWN_TAG)) {
            entity.getPersistentData().remove(CONSTRICTION_DROWN_TAG);
        }
    }
}