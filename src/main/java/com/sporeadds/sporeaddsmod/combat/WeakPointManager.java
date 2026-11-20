package com.sporeadds.sporeaddsmod.combat;

import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import com.sporeadds.sporeaddsmod.network.SyncWeakPointPacket;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class WeakPointManager {

    private static final Random RANDOM = new Random();

    // Weak point size (hit radius), amount, damage formula (soft cap, data per point, player cap, flat/percent) and
    // the critical hit limit are configurable: see ScientistConfig (scientist.weak_points.*).

    /** Weak points of each exposed entity (offset from the entity position), one list per entity id. */
    private static final Map<Integer, List<Vec3>> offsets = new HashMap<>();
    private static final Map<Integer, Integer> bonusValues = new HashMap<>();
    private static final Map<Integer, Boolean> isPlayerTarget = new HashMap<>();
    /** Critical hits landed on each exposed entity since it was last exposed (for scientist.weak_points.max_critical_hits). */
    private static final Map<Integer, Integer> criticalHitCounts = new HashMap<>();

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
        int amount = ScientistConfig.weakPointAmount();
        List<Vec3> points = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            points.add(randomOffset(entity));
        }
        offsets.put(id, points);
        bonusValues.put(id, bonusValue);
        isPlayerTarget.put(id, isPlayer);
        criticalHitCounts.remove(id);
    }

    /** Moves every weak point of the entity to a new random spot. */
    public static void relocate(LivingEntity entity) {
        List<Vec3> old = offsets.get(entity.getId());
        int count = old != null && !old.isEmpty() ? old.size() : ScientistConfig.weakPointAmount();
        List<Vec3> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            points.add(randomOffset(entity));
        }
        offsets.put(entity.getId(), points);
    }

    /** Moves only the weak point at {@code index} (the one that was just hit) to a new random spot. */
    public static void relocate(LivingEntity entity, int index) {
        List<Vec3> points = offsets.get(entity.getId());
        if (points == null || index < 0 || index >= points.size()) {
            return;
        }
        points.set(index, randomOffset(entity));
    }

    public static void remove(int entityId) {
        offsets.remove(entityId);
        bonusValues.remove(entityId);
        isPlayerTarget.remove(entityId);
        criticalHitCounts.remove(entityId);
    }

    public static boolean hasWeakPoint(int entityId) {
        return offsets.containsKey(entityId);
    }

    /** The first weak point of the entity (offset from its position), or null if it has none. */
    public static Vec3 getOffset(int entityId) {
        List<Vec3> points = offsets.get(entityId);
        return points == null || points.isEmpty() ? null : points.get(0);
    }

    /** Every weak point of the entity (copy), empty if it has none. */
    public static List<Vec3> getOffsets(int entityId) {
        List<Vec3> points = offsets.get(entityId);
        return points == null ? List.of() : List.copyOf(points);
    }

    /** Counts one more critical hit on the entity and returns how many it has taken since it was exposed. */
    public static int recordCriticalHit(int entityId) {
        return criticalHitCounts.merge(entityId, 1, Integer::sum);
    }

    /** The packet that tells the clients where this entity's weak points are, how tough they look (tier) and their size. */
    public static SyncWeakPointPacket syncPacketFor(int entityId) {
        return new SyncWeakPointPacket(
                entityId,
                true,
                getOffsets(entityId),
                getTier(entityId),
                (float) ScientistConfig.weakPointHitRadius()
        );
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
        return findHitIndex(target, attacker) >= 0;
    }

    /**
     * Index of the weak point of {@code target} that the attacker's line of sight passes closest to (within the weak
     * point size, scientist.weak_points.hit_radius), or -1 if the attacker is not hitting any.
     */
    public static int findHitIndex(LivingEntity target, ServerPlayer attacker) {
        int id = target.getId();
        List<Vec3> points = offsets.get(id);
        if (points == null || points.isEmpty()) {
            return -1;
        }

        if (target.isBlocking()) {
            return -1;
        }

        Vec3 eyePos = attacker.getEyePosition();
        Vec3 lookVec = attacker.getLookAngle();
        double hitRadius = ScientistConfig.weakPointHitRadius();

        int bestIndex = -1;
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i < points.size(); i++) {
            Vec3 markerWorldPos = target.position().add(points.get(i));

            Vec3 toMarker = markerWorldPos.subtract(eyePos);
            double distanceAlongLook = toMarker.dot(lookVec);

            if (distanceAlongLook < 0) {
                continue;
            }

            Vec3 closestPointOnRay = eyePos.add(lookVec.scale(distanceAlongLook));
            double distance = closestPointOnRay.distanceTo(markerWorldPos);

            if (distance <= hitRadius && distance < bestDistance) {
                bestIndex = i;
                bestDistance = distance;
            }
        }

        return bestIndex;
    }

    private static float applySoftCap(int rawValue, ScientistConfig.ResearchRules rules) {
        if (rawValue <= 0) {
            return 0.0F;
        }

        double result = 0.0D;
        int remaining = rawValue;
        double weight = 1.0D;

        while (remaining > 0) {
            int blockAmount = Math.min(remaining, rules.softCapBlockSize);
            result += blockAmount * weight;
            remaining -= blockAmount;
            weight *= rules.softCapDecay;
            if (weight == 0.0D) {
                break;
            }
        }

        return (float) result;
    }

    public static float getDamageBonusFromRaw(int rawValue, boolean isPlayer) {
        return getDamageBonusFromRaw(rawValue, isPlayer, ScientistConfig.ResearchRules.current());
    }

    /** Bonus points for a raw value (Data + kills * kill value), with the rules of a given snapshot (the book passes the server's). */
    public static float getDamageBonusFromRaw(int rawValue, boolean isPlayer, ScientistConfig.ResearchRules rules) {
        float softCappedValue = applySoftCap(rawValue, rules);
        float damageBonus = softCappedValue / (float) rules.dataPerDamagePoint;

        if (isPlayer) {
            damageBonus = Math.min(damageBonus, rules.maxBonusVsPlayers);
        }

        return damageBonus;
    }

    /**
     * Adds a weak point bonus (in points) to the damage of a hit: flat (damage + points, the original rule) or, with
     * scientist.weak_points.damage.mode = PERCENT, as a percentage of the hit.
     */
    public static float applyBonus(float damage, float bonusPoints) {
        if (ScientistConfig.DAMAGE_MODE.get() == ScientistConfig.DamageMode.PERCENT) {
            float percentPerPoint = ScientistConfig.DAMAGE_PERCENT_PER_POINT.get().floatValue();
            return damage * (1.0F + bonusPoints * percentPerPoint / 100.0F);
        }
        return damage + bonusPoints;
    }

    /** The bonus as the book shows it: the points themselves in FLAT mode, the percentage they are worth in PERCENT mode. */
    public static float displayBonus(float bonusPoints, ScientistConfig.ResearchRules rules) {
        return rules.percentMode ? bonusPoints * rules.percentPerPoint : bonusPoints;
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