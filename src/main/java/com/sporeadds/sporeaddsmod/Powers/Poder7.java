package com.sporeadds.sporeaddsmod.Powers;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.Calamities.Verfalldrachen;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.entity.StaticEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = "sporeadd")
public class Poder7 {

    private static final Map<Integer, Integer> ticksSinceJump = new HashMap<>();

    private static final double MOVEMENT_STRENGTH = 0.35;
    private static final double JUMP_STRENGTH = 1.0;

    private static final Set<String> FLYING_MOBS = Set.of(
            "spore:busser",
            "spore:hindenburg",
            "spore:gargoyle",
            "spore:verfalldrachen"
    );

    private static final Set<String> AQUATIC_MOBS = Set.of(
            "spore:inf_drowned",
            "spore:bloater",
            "spore:naiad",
            "spore:leviathan",
            "spore:kraken"
    );

    private static final Set<String> BLACKLISTED_MOBS = Set.of(
            "spore:arena_tendril",
            "spore:corpse_piece",
            "spore:illusion",
            "spore:tendril",
            "spore:claw",
            "spore:nuke",
            "spore:scent",
            "spore:wave",
            "spore:hohlfresser"
    );

    private static boolean canBeControlledAsFlying(LivingEntity mob) {
        if (mob instanceof Verfalldrachen dragon) {
            return dragon.getRightWing() > 0.0F && dragon.getLeftWing() > 0.0F;
        }

        ResourceLocation mobId = mob.getType().builtInRegistryHolder().key().location();
        return FLYING_MOBS.contains(mobId.toString());
    }

    private static boolean isCocoon(Entity entity) {
        return entity instanceof StaticEntity;
    }

    public static boolean tryMount(Player player, Entity target) {
        if (player.level().isClientSide()) return false;
        if (target == null) return false;

        AtomicBoolean enabled = new AtomicBoolean(false);
        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            if (data.getSwitch().length() > 7 && data.getSwitch().charAt(7) == '1') {
                enabled.set(true);
            }
        });

        if (!enabled.get()) return false;

        if (!(target instanceof Infected || target instanceof Calamity || target instanceof UtilityEntity)) {
            return false;
        }

        ResourceLocation targetId = target.getType().builtInRegistryHolder().key().location();
        if (BLACKLISTED_MOBS.contains(targetId.toString())) {
            player.displayClientMessage(
                    Component.translatable("message.sporeadd.general.not_good_idea")
                            .withStyle(ChatFormatting.DARK_RED),
                    true
            );
            return true;
        }

        if (!target.isVehicle() && !player.isPassenger()) {
            player.startRiding(target, true);
            return true;
        }

        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        Player player = event.getEntity();

        if (player.getVehicle() instanceof LivingEntity riddenMob &&
                (riddenMob instanceof Infected || riddenMob instanceof Calamity || riddenMob instanceof UtilityEntity)) {

            if (isCocoon(riddenMob)) {
                return;
            }

            float forward = player.zza;
            float strafing = player.xxa;

            handleMountMovement(player, riddenMob, forward, strafing);

            AtomicInteger playerLevel = new AtomicInteger(0);
            PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelData -> {
                playerLevel.set(levelData.getLevel());
            });

            int resistanceAmplifier = 0;
            if (playerLevel.get() >= 8) {
                resistanceAmplifier = 2;
            } else if (playerLevel.get() > 6) {
                resistanceAmplifier = 1;
            }

            MobEffectInstance resistance = riddenMob.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (resistance == null
                    || resistance.getAmplifier() < resistanceAmplifier
                    || resistance.getDuration() < 5) {
                riddenMob.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        20,
                        resistanceAmplifier,
                        true,
                        false
                ));
            }

            ResourceLocation mobId = riddenMob.getType().builtInRegistryHolder().key().location();
            boolean isFlyer = canBeControlledAsFlying(riddenMob);
            boolean isAquatic = AQUATIC_MOBS.contains(mobId.toString());

            if (!isFlyer && !(isAquatic && riddenMob.isInWater())) {
                int id = riddenMob.getId();
                if (ticksSinceJump.containsKey(id)) {
                    boolean onGround = riddenMob.onGround();

                    if (!onGround) {
                        double y = riddenMob.getDeltaMovement().y;
                        if (y > -1.1) {
                            riddenMob.setDeltaMovement(
                                    riddenMob.getDeltaMovement().x,
                                    y - 0.16,
                                    riddenMob.getDeltaMovement().z
                            );
                            riddenMob.hasImpulse = true;
                            riddenMob.hurtMarked = true;
                        }
                        ticksSinceJump.put(id, ticksSinceJump.get(id) + 1);
                    } else {
                        if (ticksSinceJump.get(id) > 2) {
                            ticksSinceJump.remove(id);
                        } else {
                            ticksSinceJump.put(id, ticksSinceJump.get(id) + 1);
                        }
                    }
                }
            }
        }
    }

    public static void handleMountMovement(Player player, LivingEntity mob, float forward, float strafing) {
        if (player.level().isClientSide()) return;

        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            if (data.getSwitch().length() > 7 && data.getSwitch().charAt(7) == '1') {

                mob.setYRot(player.getYRot());
                mob.setXRot(player.getXRot() * 0.5f);
                mob.yRotO = mob.getYRot();
                mob.xRotO = mob.getXRot();
                mob.yBodyRot = player.getYRot();
                mob.yHeadRot = player.getYRot();

                if (mob instanceof Mob mobEntity) {
                    mobEntity.getNavigation().stop();
                    mobEntity.setTarget(null);
                }

                boolean isFlyer = canBeControlledAsFlying(mob);

                if (mob instanceof Verfalldrachen dragon) {
                    dragon.setTarget(null);
                    dragon.setFlying(isFlyer);

                    boolean shouldFlap = isFlyer && !dragon.onGround();

                    if (shouldFlap) {
                        if (dragon.getFlapAnimationTicks() <= 1) {
                            dragon.triggerFlap();
                            dragon.level().broadcastEntityEvent(dragon, (byte) 6);
                        }
                    }
                }

                if (forward == 0 && strafing == 0) {
                    mob.xxa = 0.0F;
                    mob.zza = 0.0F;
                    mob.setSpeed(0.0F);

                    Vec3 currentDelta = mob.getDeltaMovement();
                    mob.setDeltaMovement(currentDelta.x * 0.1, currentDelta.y, currentDelta.z * 0.1);
                } else {
                    ResourceLocation mobId = mob.getType().builtInRegistryHolder().key().location();
                    boolean isAquatic = AQUATIC_MOBS.contains(mobId.toString());
                    boolean isInWater = mob.isInWater();
                    double currentStrength = (isAquatic && isInWater) ? (MOVEMENT_STRENGTH * 1.25) : MOVEMENT_STRENGTH;

                    MobEffectInstance slowness = mob.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    if (slowness != null) {
                        int amp = slowness.getAmplifier();
                        double slowMultiplier = Math.max(0.0, 1.0 - (0.15 * (amp + 1)));
                        currentStrength *= slowMultiplier;
                    }

                    MobEffectInstance speed = mob.getEffect(MobEffects.MOVEMENT_SPEED);
                    if (speed != null) {
                        int amp = speed.getAmplifier();
                        double speedMultiplier = 1.0 + (0.20 * (amp + 1));
                        currentStrength *= speedMultiplier;
                    }

                    if (mob.getTicksFrozen() > 0 && mob.getTicksRequiredToFreeze() > 0) {
                        float freezePercent = Mth.clamp(
                                (float) mob.getTicksFrozen() / (float) mob.getTicksRequiredToFreeze(),
                                0.0F,
                                1.0F
                        );

                        double freezeMultiplier = 1.0 - (0.5 * freezePercent);
                        currentStrength *= freezeMultiplier;
                    }

                    float yRotRad = player.getYRot() * ((float) Math.PI / 180F);
                    double xDir = (strafing * Mth.cos(yRotRad)) - (forward * Mth.sin(yRotRad));
                    double zDir = (forward * Mth.cos(yRotRad)) + (strafing * Mth.sin(yRotRad));

                    double distance = Math.sqrt(xDir * xDir + zDir * zDir);
                    if (distance > 0) {
                        xDir /= distance;
                        zDir /= distance;
                    }

                    double currentY = mob.getDeltaMovement().y;
                    Vec3 movVector = new Vec3(xDir * currentStrength, currentY, zDir * currentStrength);

                    mob.setDeltaMovement(movVector);
                    mob.hasImpulse = true;

                    mob.xxa = strafing * 0.5F;
                    mob.zza = forward;
                    mob.setSpeed((float) currentStrength);
                }
            }
        });
    }

    public static void handleMountJumpInput(Player player, LivingEntity mob, boolean isAscending, boolean isFirstPress) {
        if (player.level().isClientSide()) return;

        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            if (data.getSwitch().length() > 7 && data.getSwitch().charAt(7) == '1') {

                if (isCocoon(mob)) {
                    return;
                }

                ResourceLocation mobId = mob.getType().builtInRegistryHolder().key().location();

                boolean isFlyer = canBeControlledAsFlying(mob);
                boolean isAquatic = AQUATIC_MOBS.contains(mobId.toString());
                boolean isInWater = mob.isInWater();
                boolean isOnGround = mob.onGround();

                if (mob instanceof Verfalldrachen dragon) {
                    dragon.setTarget(null);
                    dragon.setFlying(isFlyer);
                }

                double currentX = mob.getDeltaMovement().x;
                double currentZ = mob.getDeltaMovement().z;
                double currentY = mob.getDeltaMovement().y;

                if (isFlyer || (isAquatic && isInWater)) {
                    double verticalStrength = (isAquatic && isInWater) ? 0.125 : 0.1;

                    if (isAscending) {
                        double newY = Math.min(currentY + verticalStrength, 0.4);
                        mob.setDeltaMovement(new Vec3(currentX, newY, currentZ));
                    } else {
                        double newY = Math.max(currentY - verticalStrength, -0.6);
                        mob.setDeltaMovement(new Vec3(currentX, newY, currentZ));
                    }
                    mob.hasImpulse = true;
                    mob.hurtMarked = true;
                } else {
                    int ticksAirborne = ticksSinceJump.getOrDefault(mob.getId(), 0);

                    if (isAscending && isFirstPress && isOnGround && ticksAirborne == 0) {
                        mob.setDeltaMovement(new Vec3(currentX, JUMP_STRENGTH, currentZ));
                        mob.hasImpulse = true;
                        mob.hurtMarked = true;

                        ticksSinceJump.put(mob.getId(), 1);
                    }
                }
            }
        });
    }
}