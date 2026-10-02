package com.sporeadds.sporeaddsmod.Powers;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.Powers.Poder4things.gluttonousEntityLists;
import com.sporeadds.sporeaddsmod.Powers.Poder4things.gluttonousHarvestLogic;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSpore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ForgeMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Poder9Variants {

    private static final double EFFECT_RADIUS_ABYSSAL = 15.0;
    private static final double EFFECT_RADIUS_gluttonous = 42.0;
    private static final double gluttonous_DEBUFF_RADIUS = 12.0;
    private static final int gluttonous_MAX_TARGETS = 4;
    private static final int gluttonous_WEAKNESS_DURATION = 5 * 20;
    private static final int gluttonous_WEAKNESS_AMPLIFIER = 2;

    private static final ResourceLocation HOWLER_GROWL_ID = ResourceLocation.fromNamespaceAndPath("spore", "howler_growl");
    private static final ResourceLocation MUTATION_ESSENCE_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "mutation_essence");
    private static final ResourceLocation BLOOD_PARTICLE_ID = ResourceLocation.fromNamespaceAndPath("spore", "blood_particle");

    private static final int ABYSSAL_SWIM_SLOW_DURATION = 250;
    private static final UUID ABYSSAL_SWIM_SLOW_UUID = UUID.fromString("7c4ee536-2f4e-4c4f-9f56-0d6f7b4e91a1");
    private static final String ABYSSAL_VORTEX_MARK = "sporeadd_abyssal_vortex_mark";

    private static final String gluttonous_CALL_ACTIVE_TAG = "sporeadd_gluttonous_call_active";
    private static final String gluttonous_CALL_OWNER_TAG = "sporeadd_gluttonous_call_owner";

    public static void executegluttonousCall(ServerPlayer player, ServerLevel serverLevel, PlayerSpore spore) {
        spore.addSpore(-Poder9.PHASE_COST);
        PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(lvl ->
                NeoForge.EVENT_BUS.register(new gluttonousCallTask(player, serverLevel))
        );
    }

    public static void executeAbyssalVortex(ServerPlayer player, ServerLevel serverLevel, PlayerSpore spore) {
        if (!player.isInWater()) return;
        spore.addSpore(-Poder9.PHASE_COST);
        PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(lvl ->
                NeoForge.EVENT_BUS.register(new AbyssalVortexTask(player, serverLevel, lvl.getLevel()))
        );
    }

    private static boolean isAlreadyClaimedBygluttonousCall(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(gluttonous_CALL_ACTIVE_TAG);
    }

    private static void markgluttonousCallClaim(LivingEntity entity, ServerPlayer player) {
        entity.getPersistentData().putBoolean(gluttonous_CALL_ACTIVE_TAG, true);
        entity.getPersistentData().putUUID(gluttonous_CALL_OWNER_TAG, player.getUUID());
    }

    private static void cleargluttonousCallClaim(LivingEntity entity) {
        entity.getPersistentData().remove(gluttonous_CALL_ACTIVE_TAG);
        entity.getPersistentData().remove(gluttonous_CALL_OWNER_TAG);
    }

    private static class gluttonousCallTask {

        private static final int MAX_TICKS = 10 * 20;
        private static final double ATTRACT_DIST = 2.0D;
        private static final double MOVE_SPEED = 1.4D;
        private static final int GOAL_PRIORITY = 0;

        private final ServerPlayer player;
        private final ServerLevel level;
        private final List<LivingEntity> calledEntities = new ArrayList<>();
        private final Map<Mob, gluttonousFollowPlayerGoal> activeGoals = new HashMap<>();

        private int ticksElapsed = 0;
        private boolean initialized = false;

        public gluttonousCallTask(ServerPlayer player, ServerLevel level) {
            this.player = player;
            this.level = level;
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {

            if (!initialized) {
                initialized = true;
                initializegluttonousTargets();
                if (calledEntities.isEmpty()) {
                    NeoForge.EVENT_BUS.unregister(this);
                    return;
                }
            }

            if (!player.isAlive()) {
                restoreAll();
                NeoForge.EVENT_BUS.unregister(this);
                return;
            }

            ticksElapsed++;

            spawnPlayerSonarPulse();

            List<LivingEntity> toRemove = new ArrayList<>();
            for (LivingEntity entity : calledEntities) {
                if (handleCalledEntityTick(entity)) {
                    toRemove.add(entity);
                }
            }

            for (LivingEntity removed : toRemove) {
                detachGoal(removed);
            }

            calledEntities.removeAll(toRemove);

            if (ticksElapsed >= MAX_TICKS || calledEntities.isEmpty()) {
                restoreAll();
                NeoForge.EVENT_BUS.unregister(this);
            }
        }

        private void initializegluttonousTargets() {
            playgluttonousCallStartEffects(level, player);
            applyOpeningWeakness();

            AABB area = new AABB(
                    player.getX() - EFFECT_RADIUS_gluttonous, player.getY() - EFFECT_RADIUS_gluttonous, player.getZ() - EFFECT_RADIUS_gluttonous,
                    player.getX() + EFFECT_RADIUS_gluttonous, player.getY() + EFFECT_RADIUS_gluttonous, player.getZ() + EFFECT_RADIUS_gluttonous
            );

            List<LivingEntity> candidates = level.getEntitiesOfClass(
                    LivingEntity.class,
                    area,
                    Poder9Variants::isValidgluttonousTarget
            );

            candidates.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)));

            int max = Math.min(gluttonous_MAX_TARGETS, candidates.size());
            for (int i = 0; i < max; i++) {
                LivingEntity entity = candidates.get(i);
                markgluttonousCallClaim(entity, player);
                calledEntities.add(entity);
                prepareCalledEntity(entity);
            }
        }

        private void applyOpeningWeakness() {
            AABB debuffArea = new AABB(
                    player.getX() - gluttonous_DEBUFF_RADIUS, player.getY() - gluttonous_DEBUFF_RADIUS, player.getZ() - gluttonous_DEBUFF_RADIUS,
                    player.getX() + gluttonous_DEBUFF_RADIUS, player.getY() + gluttonous_DEBUFF_RADIUS, player.getZ() + gluttonous_DEBUFF_RADIUS
            );

            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, debuffArea, entity -> entity != player);

            for (LivingEntity entity : nearby) {
                if (entity.getTeam() != null && "spore".equalsIgnoreCase(entity.getTeam().getName())) {
                    continue;
                }

                entity.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        gluttonous_WEAKNESS_DURATION,
                        gluttonous_WEAKNESS_AMPLIFIER,
                        false,
                        true,
                        true
                ));
            }
        }

        private boolean handleCalledEntityTick(LivingEntity entity) {
            if (!entity.isAlive()) {
                return true;
            }

            if (entity instanceof Mob mob) {
                mob.setTarget(null);
            }

            return tryHarvestAndDrop(entity);
        }

        private void prepareCalledEntity(LivingEntity entity) {
            if (entity instanceof Mob mob) {
                mob.setTarget(null);
                mob.getNavigation().stop();

                gluttonousFollowPlayerGoal goal = new gluttonousFollowPlayerGoal(mob, player, MOVE_SPEED);
                mob.goalSelector.addGoal(GOAL_PRIORITY, goal);
                activeGoals.put(mob, goal);
            }

            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MAX_TICKS, 1, false, true));

            level.playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.HOSTILE,
                    1.0F,
                    0.5F + level.random.nextFloat() * 0.5F
            );
        }

        private boolean tryHarvestAndDrop(LivingEntity entity) {
            if (entity.distanceTo(player) > ATTRACT_DIST) {
                return false;
            }

            if (!gluttonousHarvestLogic.tryHarvestEntity(player, entity, false)) {
                return false;
            }

            cleargluttonousCallClaim(entity);
            maybeDropMutationEssence(level, entity);
            return true;
        }

        private void spawnPlayerSonarPulse() {
            ParticleOptions bloodParticle = (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(BLOOD_PARTICLE_ID);
            if (bloodParticle == null) {
                return;
            }

            AABB box = player.getBoundingBox();
            double inflateXZ = 0.15D;
            double inflateY = 0.05D;

            double minX = box.minX - inflateXZ;
            double maxX = box.maxX + inflateXZ;
            double minY = box.minY + inflateY;
            double maxY = box.maxY + inflateY;
            double minZ = box.minZ - inflateXZ;
            double maxZ = box.maxZ + inflateXZ;

            int particleCount = 12 + level.random.nextInt(8);

            for (int i = 0; i < particleCount; i++) {
                double px = minX + level.random.nextDouble() * (maxX - minX);
                double py = minY + level.random.nextDouble() * Math.max(0.1D, (maxY - minY));
                double pz = minZ + level.random.nextDouble() * (maxZ - minZ);

                double vx = (level.random.nextDouble() - 0.5D) * 0.04D;
                double vy = (level.random.nextDouble() - 0.5D) * 0.03D;
                double vz = (level.random.nextDouble() - 0.5D) * 0.04D;

                level.sendParticles(
                        bloodParticle,
                        px, py, pz,
                        1,
                        vx, vy, vz,
                        0.0D
                );
            }
        }

        private void detachGoal(LivingEntity entity) {
            cleargluttonousCallClaim(entity);

            if (!(entity instanceof Mob mob)) return;

            gluttonousFollowPlayerGoal goal = activeGoals.remove(mob);
            if (goal != null) {
                mob.goalSelector.removeGoal(goal);
            }

            mob.setTarget(null);
            mob.getNavigation().stop();
        }

        private void restoreAll() {
            for (LivingEntity entity : calledEntities) {
                if (!entity.isAlive()) continue;

                detachGoal(entity);
                entity.removeEffect(MobEffects.MOVEMENT_SPEED);
            }

            activeGoals.clear();
        }
    }

    private static class gluttonousFollowPlayerGoal extends Goal {
        private final Mob mob;
        private final ServerPlayer player;
        private final double speed;

        public gluttonousFollowPlayerGoal(Mob mob, ServerPlayer player, double speed) {
            this.mob = mob;
            this.player = player;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            return mob.isAlive() && player.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mob.isAlive() && player.isAlive();
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public void start() {
            mob.setTarget(null);
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
            mob.setTarget(null);
        }

        @Override
        public void tick() {
            mob.setTarget(null);
            mob.getLookControl().setLookAt(player, 30.0F, 30.0F);
            mob.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), speed);
        }
    }

    private static boolean isValidgluttonousTarget(LivingEntity entity) {
        if (entity instanceof Player) return false;
        if (isAlreadyClaimedBygluttonousCall(entity)) return false;

        ResourceLocation id = EntityType.getKey(entity.getType());
        if (id == null) return false;
        if (gluttonousEntityLists.MEAT_ABOMINATION_ID.equals(id)) return false;
        if (!gluttonousEntityLists.isHarvestableEntity(id)) return false;

        if (entity.getTeam() == null) return false;
        return "spore".equalsIgnoreCase(entity.getTeam().getName());
    }

    private static void playgluttonousCallStartEffects(ServerLevel level, ServerPlayer player) {
        SoundEvent howlerGrowl = BuiltInRegistries.SOUND_EVENT.get(HOWLER_GROWL_ID);
        if (howlerGrowl != null) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    howlerGrowl,
                    SoundSource.MASTER,
                    4.2F,
                    0.75F
            );
        }

        ParticleOptions bloodParticle = (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(BLOOD_PARTICLE_ID);
        if (bloodParticle != null) {
            AABB box = player.getBoundingBox();
            double inflateXZ = 0.2D;

            level.sendParticles(
                    bloodParticle,
                    player.getX(),
                    box.minY + (player.getBbHeight() * 0.5D),
                    player.getZ(),
                    30,
                    (box.getXsize() * 0.5D) + inflateXZ,
                    player.getBbHeight() * 0.5D,
                    (box.getZsize() * 0.5D) + inflateXZ,
                    0.01D
            );
        }
    }

    private static void maybeDropMutationEssence(ServerLevel level, LivingEntity entity) {
        if (level.random.nextFloat() >= 0.05F) return;

        Item mutationEssence = BuiltInRegistries.ITEM.get(MUTATION_ESSENCE_ID);
        if (mutationEssence == null || mutationEssence == Items.AIR) return;

        level.addFreshEntity(new ItemEntity(
                level,
                entity.getX(),
                entity.getY() + 0.5D,
                entity.getZ(),
                new ItemStack(mutationEssence)
        ));
    }

    private static class AbyssalVortexTask {
        private final ServerPlayer player;
        private final ServerLevel level;
        private final int playerLevel;
        private int ticksElapsed = 0;
        private static final int MAX_TICKS = 20;
        private final double startX;
        private final double startY;
        private final double startZ;

        public AbyssalVortexTask(ServerPlayer player, ServerLevel level, int playerLevel) {
            this.player = player;
            this.level = level;
            this.playerLevel = playerLevel;
            this.startX = player.getX();
            this.startY = player.getY();
            this.startZ = player.getZ();

            level.playSound(
                    null,
                    startX,
                    startY,
                    startZ,
                    SoundEvents.CONDUIT_ACTIVATE,
                    SoundSource.MASTER,
                    2.0f,
                    2.0f
            );
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {

            if (!player.isAlive() || !player.isInWater()) {
                NeoForge.EVENT_BUS.unregister(this);
                return;
            }

            ticksElapsed++;
            player.setDeltaMovement(Vec3.ZERO);

            if (ticksElapsed % 4 == 0) {
                level.playSound(
                        null,
                        startX,
                        startY,
                        startZ,
                        SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE,
                        SoundSource.MASTER,
                        2.0f,
                        0.5f + (ticksElapsed / 20.0f) * 1.5f
                );
            }

            double currentRadius = EFFECT_RADIUS_ABYSSAL * (1.0 - (ticksElapsed / (double) MAX_TICKS));

            for (int i = 0; i < 80; i++) {
                double theta = level.random.nextDouble() * 2 * Math.PI;
                double phi = Math.acos(2 * level.random.nextDouble() - 1);
                double dx = Math.sin(phi) * Math.cos(theta);
                double dy = Math.cos(phi);
                double dz = Math.sin(phi) * Math.sin(theta);

                double px = startX + dx * currentRadius;
                double py = startY + 1.0D + dy * currentRadius;
                double pz = startZ + dz * currentRadius;

                level.sendParticles(ParticleTypes.BUBBLE, px, py, pz, 0, -dx * 0.6D, -dy * 0.6D, -dz * 0.6D, 1.0D);
                if (level.random.nextFloat() < 0.2F) {
                    level.sendParticles(ParticleTypes.BUBBLE_POP, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }

            if (ticksElapsed >= MAX_TICKS) {
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, startX, startY + 1.0D, startZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                executeAbyssalVortex(player, level, playerLevel);
                NeoForge.EVENT_BUS.unregister(this);
            }
        }
    }

    private static class RemoveSwimSpeedModifierTask {
        private final LivingEntity living;
        private final UUID modifierId;
        private final ServerLevel level;
        private int ticksRemaining;

        public RemoveSwimSpeedModifierTask(LivingEntity living, ServerLevel level, UUID modifierId, int ticksRemaining) {
            this.living = living;
            this.level = level;
            this.modifierId = modifierId;
            this.ticksRemaining = ticksRemaining;
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {
            if (living == null || !living.isAlive()) {
                NeoForge.EVENT_BUS.unregister(this);
                return;
            }

            ticksRemaining--;
            if (ticksRemaining > 0) return;

            var attr = living.getAttribute(ForgeMod.SWIM_SPEED.get());
            if (attr != null) {
                attr.removeModifier(modifierId);
            }

            NeoForge.EVENT_BUS.unregister(this);
        }
    }

    private static void executeAbyssalVortex(ServerPlayer player, ServerLevel level, int playerLevel) {
        int py = player.blockPosition().getY();
        int topY = Math.min(level.getMaxBuildHeight() - 2, py + 50);
        int bottomY = Math.max(level.getMinBuildHeight(), py - 50);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.MASTER, 4.0f, 0.5f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.MASTER, 3.0f, 0.8f);

        int surfaceY = findWaterSurfaceY(level, player.blockPosition(), topY);
        spawnAbyssalVortexParticles(level, player, bottomY, topY, surfaceY);

        AABB area = new AABB(
                player.getX() - EFFECT_RADIUS_ABYSSAL, bottomY, player.getZ() - EFFECT_RADIUS_ABYSSAL,
                player.getX() + EFFECT_RADIUS_ABYSSAL, topY, player.getZ() + EFFECT_RADIUS_ABYSSAL
        );

        List<Entity> entities = level.getEntities(player, area, entity -> entity != player);

        for (Entity entity : entities) {
            if (entity.getTeam() != null && "spore".equalsIgnoreCase(entity.getTeam().getName())) continue;

            if (entity instanceof Boat boat) {
                boat.discard();
                continue;
            }

            if (entity instanceof LivingEntity living && living.isInWater()) {
                living.getPersistentData().putBoolean(ABYSSAL_VORTEX_MARK, true);

                var attr = living.getAttribute(ForgeMod.SWIM_SPEED.get());
                if (attr != null) {
                    attr.removeModifier(ABYSSAL_SWIM_SLOW_UUID);

                    double swimPenalty = -0.5D;
                    if (playerLevel > 5) {
                        swimPenalty -= 0.1D * (playerLevel - 5);
                    }

                    attr.addTransientModifier(new AttributeModifier(
                            ABYSSAL_SWIM_SLOW_UUID,
                            "abyssal_swim_slow",
                            swimPenalty,
                            AttributeModifier.Operation.ADDITION
                    ));

                    NeoForge.EVENT_BUS.register(
                            new RemoveSwimSpeedModifierTask(living, level, ABYSSAL_SWIM_SLOW_UUID, ABYSSAL_SWIM_SLOW_DURATION)
                    );
                }

                double dx = player.getX() - living.getX();
                double dz = player.getZ() - living.getZ();
                Vec3 pull = new Vec3(dx, 0, dz);

                if (pull.lengthSqr() > 0.01D) {
                    pull = pull.normalize().scale(2.0D);
                }

                Vec3 motion = living.getDeltaMovement();
                living.setDeltaMovement(motion.x * 0.1D + pull.x, -4.0D, motion.z * 0.1D + pull.z);
                living.hurtMarked = true;
                living.fallDistance = 0.0F;
            }
        }
    }

    private static int findWaterSurfaceY(ServerLevel level, BlockPos center, int topY) {
        int x = center.getX();
        int z = center.getZ();

        for (int y = topY; y >= center.getY() - 50; y--) {
            if (level.getFluidState(new BlockPos(x, y, z)).is(net.minecraft.tags.FluidTags.WATER)) {
                return y + 1;
            }
        }

        return center.getY();
    }

    private static void spawnAbyssalVortexParticles(ServerLevel level, ServerPlayer player, int bottomY, int topY, int surfaceY) {
        double x = player.getX();
        double z = player.getZ();

        for (int y = bottomY; y <= topY; y++) {
            if (!level.getFluidState(new BlockPos((int) x, y, (int) z)).is(net.minecraft.tags.FluidTags.WATER)) {
                continue;
            }

            double radius = 1.0D + ((double) (y - bottomY) / Math.max(1.0D, (topY - bottomY))) * EFFECT_RADIUS_ABYSSAL;

            for (int i = 0; i < 35; i++) {
                double angle = (i / 35.0D) * (Math.PI * 2.0D) + (level.random.nextDouble() * 0.2D);

                double px = x + Math.cos(angle) * radius;
                double pz = z + Math.sin(angle) * radius;
                double py = y + level.random.nextDouble();

                double vx = -Math.sin(angle) * 1.2D - Math.cos(angle) * 0.5D;
                double vz = Math.cos(angle) * 1.2D - Math.sin(angle) * 0.5D;

                level.sendParticles(ParticleTypes.CURRENT_DOWN, px, py, pz, 0, vx, -1.5D, vz, 1.0D);

                if (level.random.nextFloat() < 0.6F) {
                    level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, px, py, pz, 0, vx * 0.5D, -1.2D, vz * 0.5D, 1.0D);
                }
            }
        }

        if (surfaceY > bottomY && surfaceY <= topY + 1) {
            for (int i = 0; i < 300; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2.0D;
                double r = Math.sqrt(level.random.nextDouble()) * EFFECT_RADIUS_ABYSSAL;

                double px = x + Math.cos(angle) * r;
                double pz = z + Math.sin(angle) * r;
                double py = surfaceY + (level.random.nextDouble() * 0.3D);

                level.sendParticles(ParticleTypes.BUBBLE_POP, px, py, pz, 1, 0.0D, 0.1D, 0.0D, 0.05D);

                if (level.random.nextFloat() < 0.3F) {
                    level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 1,
                            (Math.cos(angle) * -0.2D), -0.3D, (Math.sin(angle) * -0.2D), 0.1D);
                }
            }
        }
    }

    // =========================================================================
    // CAUSTIC - skill 9 spray mode activation
    // =========================================================================

    /**
     * Skill 9 for Caustic no longer does the old AOE corrosion/dissolution burst: it now fills the
     * shared Caustic ability-charge bar to full and switches that ability into a "spray mode" (handled
     * client-side in AbilityKeyHandler + server-side via SubclassAbility.fireCausticSpraySegment) until
     * the ammo is spent, at which point it reverts to the normal single charged shot.
     */
    public static void activateCausticSprayMode(ServerPlayer player, ServerLevel level) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "chemist_fuse")),
                SoundSource.PLAYERS, 1.0F, 1.0F);

        var gas = com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes.GAS.get();
        for (int i = 0; i < 16; i++) {
            double theta = level.random.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * level.random.nextDouble() - 1);
            double dx = Math.sin(phi) * Math.cos(theta);
            double dy = Math.cos(phi);
            double dz = Math.sin(phi) * Math.sin(theta);

            level.sendParticles(gas,
                    player.getX(), player.getEyeY(), player.getZ(),
                    1, dx * 0.3D, dy * 0.3D, dz * 0.3D, 0.15D);
        }

        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.send(
                com.sporeadds.sporeaddsmod.network.PacketDistributor.PLAYER.with(() -> player),
                new com.sporeadds.sporeaddsmod.network.CausticSprayModePacket()
        );
    }
}