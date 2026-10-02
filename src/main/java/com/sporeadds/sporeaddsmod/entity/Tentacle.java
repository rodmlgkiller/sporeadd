package com.sporeadds.sporeaddsmod.entity;

import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.sporeadds.sporeaddsmod.entity.projectile.TentacleProjectile;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class Tentacle extends Organoid {

    private static final EntityDataAccessor<Integer> ATTACHED_TARGET_ID =
            SynchedEntityData.defineId(Tentacle.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(Tentacle.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> HIDE_RENDER =
            SynchedEntityData.defineId(Tentacle.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> TENTACLE_SLOT =
            SynchedEntityData.defineId(Tentacle.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> LAST_DAMAGE_TICK =
            SynchedEntityData.defineId(Tentacle.class, EntityDataSerializers.INT);

    private static final ResourceLocation LIMB_SLASH_SOUND_ID = ResourceLocation.fromNamespaceAndPath("spore", "limb_slash");
    private static final float DEATH_SOUND_VOLUME = 2.7F;
    private static final float DEATH_SOUND_PITCH = 0.5F;

    private boolean deathEffectsApplied = false;
    private boolean usePendingDamage = false;
    private float pendingDamage = -1.0F;

    public Tentacle(EntityType<? extends Organoid> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.refreshDimensions();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Organoid.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACHED_TARGET_ID, -1);
        this.entityData.define(OWNER_ID, -1);
        this.entityData.define(HIDE_RENDER, false);
        this.entityData.define(TENTACLE_SLOT, -1);
        this.entityData.define(LAST_DAMAGE_TICK, -99999);
    }

    public void setAttachedTargetId(int id) {
        this.entityData.set(ATTACHED_TARGET_ID, id);
    }

    public Entity getAttachedTarget() {
        int id = this.entityData.get(ATTACHED_TARGET_ID);
        return id == -1 ? null : this.level().getEntity(id);
    }

    public void setTentacleOwnerId(int id) {
        this.entityData.set(OWNER_ID, id);
    }

    public Entity getTentacleOwner() {
        int id = this.entityData.get(OWNER_ID);
        return id == -1 ? null : this.level().getEntity(id);
    }

    public boolean shouldHideRender() {
        return this.entityData.get(HIDE_RENDER);
    }

    public void setHideRender(boolean hide) {
        this.entityData.set(HIDE_RENDER, hide);
    }

    public void setPendingDamage(float damage) {
        this.pendingDamage = damage;
        this.usePendingDamage = true;
    }

    public void setTentacleSlot(int slot) {
        this.entityData.set(TENTACLE_SLOT, slot);
    }

    public int getTentacleSlot() {
        return this.entityData.get(TENTACLE_SLOT);
    }

    public void markDamagedNow() {
        this.entityData.set(LAST_DAMAGE_TICK, this.tickCount);
    }

    public boolean wasRecentlyDamaged(int ticks) {
        int lastDamageTick = this.entityData.get(LAST_DAMAGE_TICK);
        return lastDamageTick >= 0
                && this.tickCount >= lastDamageTick
                && (this.tickCount - lastDamageTick) <= ticks;
    }

    private int getOwnerSporeLevel() {
        Entity owner = this.getTentacleOwner();

        if (!(owner instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        return PlayerLevelProvider.PLAYER_LVL.get(serverPlayer)
                .map(cap -> cap.getLevel())
                .orElse(0);
    }

    private double getMaxHealthForOwnerLevel(int level) {
        return switch (level) {
            case 0 -> 10.0D;
            case 1 -> 12.0D;
            case 2 -> 14.0D;
            case 3 -> 16.0D;
            case 4 -> 18.0D;
            case 5 -> 22.0D;
            case 6 -> 26.0D;
            case 7 -> 35.0D;
            case 8 -> 40.0D;
            case 9 -> 50.0D;
            default -> 10.0D;
        };
    }

    public void applyOwnerLevelStats() {
        if (this.level().isClientSide) {
            return;
        }

        int ownerLevel = getOwnerSporeLevel();
        double maxHealth = getMaxHealthForOwnerLevel(ownerLevel);

        AttributeInstance maxHealthAttribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(maxHealth);
        }

        this.setHealth((float) maxHealth);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.isInWater()) {
            this.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    10,
                    2,
                    false,
                    false,
                    true
            ));
        }

        Entity attached = this.getAttachedTarget();
        if (!(attached instanceof LivingEntity living) || !living.isAlive()) {
            if (!this.level().isClientSide) {
                TentacleProjectile projectile = findProjectileForCurrentTarget();
                if (projectile != null && projectile.isAlive()) {
                    projectile.releaseVictimAndReturn();
                }
            }
            this.discard();
            return;
        }

        double x = living.getX();
        double y = living.getY() + (living.getBbHeight() * 0.5D) - 0.45D;
        double z = living.getZ();

        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(x, y, z);
        this.setYRot(living.getYRot());
        this.yBodyRot = living.getYRot();
        this.yHeadRot = living.getYRot();
        this.setNoGravity(true);
        this.fallDistance = 0.0F;
    }

    private TentacleProjectile findProjectileForCurrentTarget() {
        Entity owner = this.getTentacleOwner();
        Entity target = this.getAttachedTarget();

        if (owner == null || target == null) {
            return null;
        }

        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(48.0D))) {
            if (entity instanceof TentacleProjectile projectile && projectile.isAlive()) {
                Entity projectileOwner = projectile.getOwnerById();
                Entity projectileVictim = projectile.getVictimById();

                if (projectileOwner != null
                        && projectileVictim != null
                        && projectileOwner.getId() == owner.getId()
                        && projectileVictim.getId() == target.getId()) {
                    return projectile;
                }
            }
        }

        return null;
    }

    private void applyDeathVisuals() {
        if (this.deathEffectsApplied) {
            return;
        }

        this.deathEffectsApplied = true;

        if (this.level() instanceof ServerLevel serverLevel) {
            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(LIMB_SLASH_SOUND_ID);
            if (sound != null) {
                serverLevel.playSound(
                        null,
                        this.getX(),
                        this.getY() + this.getBbHeight() * 0.5D,
                        this.getZ(),
                        sound,
                        SoundSource.HOSTILE,
                        DEATH_SOUND_VOLUME,
                        DEATH_SOUND_PITCH
                );
            }

            DustParticleOptions redDust = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.2F);

            double centerX = this.getX();
            double centerY = this.getY() + this.getBbHeight() * 0.5D;
            double centerZ = this.getZ();

            serverLevel.sendParticles(redDust, centerX, centerY, centerZ, 18, 0.45D, 0.35D, 0.45D, 0.02D);
            serverLevel.sendParticles(redDust, centerX, centerY + 0.25D, centerZ, 12, 0.30D, 0.20D, 0.30D, 0.01D);
        }

        this.setHideRender(true);
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            applyDeathVisuals();
        }

        super.die(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean damaged;

        if (this.usePendingDamage) {
            this.usePendingDamage = false;
            float dmg = this.pendingDamage;
            this.pendingDamage = -1.0F;
            damaged = super.hurt(source, dmg);
        } else {
            float damageCap = this.getMaxHealth() / 3.0F;
            damaged = super.hurt(source, Math.min(amount, damageCap));
        }

        if (damaged) {
            markDamagedNow();
        }

        return damaged;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !(entity instanceof Player);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttachedTargetId", this.entityData.get(ATTACHED_TARGET_ID));
        tag.putInt("TentacleOwnerId", this.entityData.get(OWNER_ID));
        tag.putBoolean("HideRender", this.entityData.get(HIDE_RENDER));
        tag.putInt("TentacleSlot", this.entityData.get(TENTACLE_SLOT));
        tag.putInt("LastDamageTick", this.entityData.get(LAST_DAMAGE_TICK));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AttachedTargetId")) {
            this.entityData.set(ATTACHED_TARGET_ID, tag.getInt("AttachedTargetId"));
        }
        if (tag.contains("TentacleOwnerId")) {
            this.entityData.set(OWNER_ID, tag.getInt("TentacleOwnerId"));
        }
        if (tag.contains("HideRender")) {
            this.entityData.set(HIDE_RENDER, tag.getBoolean("HideRender"));
        }
        if (tag.contains("TentacleSlot")) {
            this.entityData.set(TENTACLE_SLOT, tag.getInt("TentacleSlot"));
        }
        if (tag.contains("LastDamageTick")) {
            this.entityData.set(LAST_DAMAGE_TICK, tag.getInt("LastDamageTick"));
        }
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        Item membrane = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "organoid_membrane"));
        Item tendons = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "tendons"));

        if (membrane != null && this.random.nextFloat() < 0.10F) {
            this.spawnAtLocation(new ItemStack(membrane, 1));
        }

        if (tendons != null && this.random.nextFloat() < 0.70F) {
            int amount = 1 + this.random.nextInt(2);
            this.spawnAtLocation(new ItemStack(tendons, amount));
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            Scoreboard scoreboard = serverLevel.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam("spore");
            if (team != null) {
                scoreboard.addPlayerToTeam(this.getStringUUID(), team);
            }
        }

        return data;
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }
}