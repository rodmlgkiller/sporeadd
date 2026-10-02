package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.core.Holder;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;

import java.util.List;

public class SeasonedEffect extends MobEffect {

    private static final ResourceLocation MARKER_ID = ResourceLocation.fromNamespaceAndPath("spore", "marker");
    private static final ResourceLocation PROTO_ID = ResourceLocation.fromNamespaceAndPath("spore", "proto");

    private static final float SPORE_NO_TARGET_HEALTH_THRESHOLD = 30.0F;
    private static final float PLAYER_MUST_BE_BELOW_HEALTH = 5.0F;
    private static final String SPORE_TEAM_NAME = "spore";

    public SeasonedEffect() {
        super(MobEffectCategory.HARMFUL, 0x550000);
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (living.level().isClientSide) return true;

        Holder<MobEffect> marker = BuiltInRegistries.MOB_EFFECT.getHolder(MARKER_ID).orElse(null);
        if (marker != null) {
            living.addEffect(new MobEffectInstance(marker, 100, 5, false, false));
        }

        AABB bounds = living.getBoundingBox().inflate(42.0D);
        List<LivingEntity> sporeEntities = living.level().getEntitiesOfClass(
                LivingEntity.class,
                bounds,
                entity -> entity instanceof Infected || entity instanceof UtilityEntity
        );

        boolean canBeTargeted = canBeTargetedBySpore(living);

        for (LivingEntity sporeEntity : sporeEntities) {
            if (!(sporeEntity instanceof Mob mob)) continue;

            LivingEntity currentTarget = mob.getTarget();

            if (sporeEntity == living) {
                if (currentTarget == living) {
                    mob.setTarget(null);
                }
                continue;
            }

            if (currentTarget == mob) {
                mob.setTarget(null);
                currentTarget = null;
            }

            if (!canBeTargeted) {
                if (currentTarget == living) {
                    mob.setTarget(null);
                }
                continue;
            }

            if (currentTarget != living && !(currentTarget instanceof Player)) {
                mob.setTarget(living);
                currentTarget = living;
            }

            if (currentTarget == living && isNonPlayerSporeTeamEntity(mob)) {
                mob.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        40,
                        0,
                        false,
                        false,
                        true
                ));
            }
        }
        return true;
    }

    @Override
    public void removeAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap) {
        super.removeAttributeModifiers(attributeMap);

        if (living.level().isClientSide) return;
        clearSporeTargets(living);
    }

    private static void clearSporeTargets(LivingEntity living) {
        AABB bounds = living.getBoundingBox().inflate(42.0D);
        List<LivingEntity> sporeEntities = living.level().getEntitiesOfClass(
                LivingEntity.class,
                bounds,
                entity -> entity instanceof Infected || entity instanceof UtilityEntity
        );

        for (LivingEntity sporeEntity : sporeEntities) {
            if (!(sporeEntity instanceof Mob mob)) continue;

            if (mob.getTarget() == living) {
                mob.setTarget(null);
            }
        }
    }

    private static boolean canBeTargetedBySpore(LivingEntity living) {
        ResourceLocation entityId = living.getType().builtInRegistryHolder().key().location();

        if (PROTO_ID.equals(entityId)) {
            return false;
        }

        if (living instanceof Player player) {
            Team team = player.getTeam();
            boolean isSporeTeam = team != null && SPORE_TEAM_NAME.equalsIgnoreCase(team.getName());

            if (isSporeTeam && player.getHealth() > PLAYER_MUST_BE_BELOW_HEALTH) {
                return false;
            }

            return true;
        }

        if (living instanceof Infected || living instanceof UtilityEntity) {
            return living.getHealth() <= SPORE_NO_TARGET_HEALTH_THRESHOLD;
        }

        return true;
    }

    private static boolean isNonPlayerSporeTeamEntity(LivingEntity living) {
        if (living instanceof Player) {
            return false;
        }

        Team team = living.getTeam();
        return team != null && SPORE_TEAM_NAME.equalsIgnoreCase(team.getName());
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}