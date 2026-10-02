package com.sporeadds.sporeaddsmod.event.TentacleHandler;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.entity.Tentacle;
import com.sporeadds.sporeaddsmod.entity.projectile.TentacleProjectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd")
public class GrabEntity {

    private static final int CONSTRICTION_REAPPLY_INTERVAL = 5;
    private static final int CONSTRICTION_DURATION = 10;
    private static final int TENTACLE_THREAT_INTERVAL = 5;

    private static final String REDIRECT_TO_TENTACLE_TAG = "sporeadds_redirect_to_tentacle";
    private static final String REDIRECT_TO_ATTACHED_TAG = "sporeadds_redirect_to_attached";

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }

        DamageSource source = event.getSource();
        Entity directEntity = source.getDirectEntity();
        Entity causingEntity = source.getEntity();
        float amount = event.getAmount();

        if (amount <= 0.0F) {
            return;
        }

        if (target.getPersistentData().getBoolean(REDIRECT_TO_TENTACLE_TAG)) {
            target.getPersistentData().remove(REDIRECT_TO_TENTACLE_TAG);
            return;
        }

        if (target.getPersistentData().getBoolean(REDIRECT_TO_ATTACHED_TAG)) {
            target.getPersistentData().remove(REDIRECT_TO_ATTACHED_TAG);
            return;
        }

        if (isOnSporeTeam(target)) {
            if (directEntity instanceof Tentacle || directEntity instanceof TentacleProjectile) {
                return;
            }

            if (causingEntity instanceof Tentacle || causingEntity instanceof TentacleProjectile) {
                return;
            }

            Tentacle tentacle = findAttachedTentacle(target);
            if (tentacle == null || !tentacle.isAlive()) {
                return;
            }

            event.setCanceled(true);

            tentacle.getPersistentData().putBoolean(REDIRECT_TO_TENTACLE_TAG, true);
            tentacle.invulnerableTime = 0;
            tentacle.setPendingDamage(amount);
            tentacle.hurt(target.damageSources().generic(), amount);
            return;
        }

        if (target instanceof Tentacle tentacle) {
            Entity attached = tentacle.getAttachedTarget();
            if (!(attached instanceof LivingEntity attachedLiving) || !attachedLiving.isAlive()) {
                return;
            }

            Entity sporeAttacker = resolveSporeAttacker(source);
            if (sporeAttacker == null) {
                return;
            }

            if (directEntity instanceof TentacleProjectile || causingEntity instanceof TentacleProjectile) {
                return;
            }

            if (isOnSporeTeam(attachedLiving)) {
                attachedLiving.removeEffect(effects.CONSTRICTION);

                TentacleProjectile projectile = findProjectileFor(sporeAttacker, attachedLiving);
                if (projectile != null && projectile.isAlive()) {
                    projectile.releaseVictimAndReturn();
                }

                tentacle.discard();
                event.setCanceled(true);
                return;
            }

            event.setCanceled(true);

            attachedLiving.getPersistentData().putBoolean(REDIRECT_TO_ATTACHED_TAG, true);
            attachedLiving.invulnerableTime = 0;
            attachedLiving.hurt(target.damageSources().generic(), amount);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        if (!(entity instanceof Tentacle tentacle)) {
            return;
        }

        Entity attached = tentacle.getAttachedTarget();
        if (!(attached instanceof LivingEntity attachedLiving) || !attachedLiving.isAlive()) {
            return;
        }

        if (isOnSporeTeam(attachedLiving)) {
            return;
        }

        if (entity.tickCount % CONSTRICTION_REAPPLY_INTERVAL == 0) {
            attachedLiving.addEffect(new MobEffectInstance(
                    effects.CONSTRICTION,
                    CONSTRICTION_DURATION,
                    0,
                    false,
                    true,
                    true
            ));
        }

        if (entity.tickCount % TENTACLE_THREAT_INTERVAL == 0) {
            markTentacleAsThreat(tentacle, attachedLiving);
        }
    }

    private static void markTentacleAsThreat(Tentacle tentacle, LivingEntity attachedLiving) {
        if (isOnSporeTeam(attachedLiving) || !tentacle.isAlive()) {
            return;
        }

        Entity owner = tentacle.getTentacleOwner();
        attachedLiving.setLastHurtByMob(tentacle);

        if (attachedLiving instanceof Mob mob) {
            mob.setTarget(tentacle);
        }

        if (owner instanceof LivingEntity livingOwner) {
            attachedLiving.setLastHurtByMob(livingOwner);
        }
    }

    private static Entity resolveSporeAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        Entity causing = source.getEntity();

        if (direct != null && isOnSporeTeam(direct)) {
            return direct;
        }

        if (causing != null && isOnSporeTeam(causing)) {
            return causing;
        }

        return null;
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
}