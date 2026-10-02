package com.sporeadds.sporeaddsmod.combat;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class WeakPointManager {

    private static final Random RANDOM = new Random();
    private static final double MARKER_HIT_RADIUS = 0.3D;

    private static final int SOFTCAP_BLOCK_SIZE = 200;
    private static final float PLAYER_TARGET_MAX_BONUS_DAMAGE = 15.0F;

    private static final Map<Integer, Vec3> offsets = new HashMap<>();
    private static final Map<Integer, Integer> bonusValues = new HashMap<>();
    private static final Map<Integer, Boolean> isPlayerTarget = new HashMap<>();

    private static final net.minecraft.resources.ResourceLocation[] TIER_TEXTURES = {
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/particle/weak_point.png"),
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/particle/weak_point2.png"),
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/particle/weak_point3.png"),
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/particle/weak_point4.png"),
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/particle/weak_point5.png"),
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/particle/weak_point6.png"),
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/particle/weak_point7.png")
    };

    private static final int[] TIER_COLORS = {
            0x55AAFF, // azul claro
            0x00FFFF, // cyan
            0x55FF55, // verde brillante
            0xFFFF55, // amarillo brillante
            0xFF8000, // naranja brillante
            0xFF5555, // rojo brillante
            0xFF55FF  // rosa brillante
    };

    private static final net.minecraft.sounds.SoundEvent HIT_SOUND =
            net.minecraft.sounds.SoundEvents.GOAT_HORN_BREAK;

    private static final float[] TIER_PITCH = {
            1.5F, 1.3F, 1.1F, 0.9F, 0.7F, 0.5F, 0.3F
    };

    private static final float HIT_SOUND_VOLUME = 2.0F;

    private WeakPointManager() {
    }

    public static Vec3 randomOffset(LivingEntity entity) {
        double halfWidth = entity.getBbWidth() / 2.0D;
        double height = entity.getBbHeight();

        double x = (RANDOM.nextDouble() * 2 - 1) * halfWidth * 0.8D;
        double y = 0.2D + RANDOM.nextDouble() * (height * 0.8D);
        double z = (RANDOM.nextDouble() * 2 - 1) * halfWidth * 0.8D;

        return new Vec3(x, y, z);
    }

    public static void registerOrRefresh(LivingEntity entity, int bonusValue) {
        registerOrRefresh(entity, bonusValue, entity instanceof ServerPlayer);
    }

    public static void registerOrRefresh(LivingEntity entity, int bonusValue, boolean isPlayer) {
        int id = entity.getId();
        offsets.put(id, randomOffset(entity));
        bonusValues.put(id, bonusValue);
        isPlayerTarget.put(id, isPlayer);
    }

    public static void relocate(LivingEntity entity) {
        offsets.put(entity.getId(), randomOffset(entity));
    }

    public static void remove(int entityId) {
        offsets.remove(entityId);
        bonusValues.remove(entityId);
        isPlayerTarget.remove(entityId);
    }

    public static boolean hasWeakPoint(int entityId) {
        return offsets.containsKey(entityId);
    }

    public static Vec3 getOffset(int entityId) {
        return offsets.get(entityId);
    }

    public static int getBonusValue(int entityId) {
        return bonusValues.getOrDefault(entityId, 0);
    }

    public static Set<Integer> getTrackedIds() {
        return new HashSet<>(offsets.keySet());
    }

    public static LivingEntity resolveEntity(int entityId) {
        for (var level : net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            var entity = level.getEntity(entityId);
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    public static boolean tryHit(LivingEntity target, ServerPlayer attacker) {
        int id = target.getId();
        Vec3 offset = offsets.get(id);
        if (offset == null) {
            return false;
        }

        if (target.isBlocking()) {
            return false;
        }

        Vec3 markerWorldPos = target.position().add(offset);

        Vec3 eyePos = attacker.getEyePosition();
        Vec3 lookVec = attacker.getLookAngle();

        Vec3 toMarker = markerWorldPos.subtract(eyePos);
        double distanceAlongLook = toMarker.dot(lookVec);

        if (distanceAlongLook < 0) {
            return false;
        }

        Vec3 closestPointOnRay = eyePos.add(lookVec.scale(distanceAlongLook));
        double distance = closestPointOnRay.distanceTo(markerWorldPos);

        return distance <= MARKER_HIT_RADIUS;
    }

    private static float applySoftCap(int rawValue) {
        if (rawValue <= 0) {
            return 0.0F;
        }

        double result = 0.0D;
        int remaining = rawValue;
        int blockIndex = 0;

        while (remaining > 0) {
            int blockAmount = Math.min(remaining, SOFTCAP_BLOCK_SIZE);
            double weight = 1.0D / Math.pow(2, blockIndex);
            result += blockAmount * weight;
            remaining -= blockAmount;
            blockIndex++;
        }

        return (float) result;
    }

    public static float getDamageBonusFromRaw(int rawValue, boolean isPlayer) {
        float softCappedValue = applySoftCap(rawValue);
        float damageBonus = softCappedValue / 20.0F;

        if (isPlayer) {
            damageBonus = Math.min(damageBonus, PLAYER_TARGET_MAX_BONUS_DAMAGE);
        }

        return damageBonus;
    }

    public static float getDamageBonus(int entityId) {
        boolean isPlayer = isPlayerTarget.getOrDefault(entityId, false);
        return getDamageBonusFromRaw(getBonusValue(entityId), isPlayer);
    }

    public static int computeTier(float damageBonus) {
        if (damageBonus < 3.0F) return 0;
        if (damageBonus < 6.0F) return 1;
        if (damageBonus < 9.0F) return 2;
        if (damageBonus < 12.0F) return 3;
        if (damageBonus < 15.0F) return 4;
        if (damageBonus < 18.0F) return 5;
        return 6;
    }

    public static int getTier(int entityId) {
        return computeTier(getDamageBonus(entityId));
    }

    public static net.minecraft.resources.ResourceLocation getTexture(int entityId) {
        return TIER_TEXTURES[getTier(entityId)];
    }

    public static net.minecraft.resources.ResourceLocation getTierTexture(int tier) {
        return TIER_TEXTURES[Math.max(0, Math.min(TIER_TEXTURES.length - 1, tier))];
    }

    public static int getTierColor(int tier) {
        return TIER_COLORS[Math.max(0, Math.min(TIER_COLORS.length - 1, tier))];
    }

    public static net.minecraft.sounds.SoundEvent getHitSound(int entityId) {
        return HIT_SOUND;
    }

    public static float getHitSoundPitch(int entityId) {
        return TIER_PITCH[getTier(entityId)];
    }

    public static float getHitSoundVolume() {
        return HIT_SOUND_VOLUME;
    }
}