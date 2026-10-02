package com.sporeadds.sporeaddsmod.entity.projectile;

import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import com.sporeadds.sporeaddsmod.entity.Tentacle;
import com.sporeadds.sporeaddsmod.event.TentacleHandler.AbyssalTentacleRegenEvents;
import com.sporeadds.sporeaddsmod.network.AbyssalTentaclePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class TentacleProjectile extends AbstractArrow {

    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(TentacleProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(TentacleProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VICTIM_ID = SynchedEntityData.defineId(TentacleProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TENTACLE_ID = SynchedEntityData.defineId(TentacleProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TENTACLE_SLOT = SynchedEntityData.defineId(TentacleProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SHOT = SynchedEntityData.defineId(TentacleProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> POST_HIT_LIFE = SynchedEntityData.defineId(TentacleProjectile.class, EntityDataSerializers.INT);


    private static final float DEFAULT_PLAYER_WIDTH = 0.6F;
    private static final float DEFAULT_PLAYER_HEIGHT = 1.8F;
    private static final double MAX_DISTANCE_FROM_OWNER = 80.0D;
    private static final double MAX_DISTANCE_FROM_OWNER_PROYECTILE = 40.0D;
    private static final double RETURN_SPEED = 1.0D;
    private static final double STOP_PULL_DISTANCE = 4.0D;
    private static final double PROJECTILE_OFFSET_FROM_VICTIM = 0.35D;
    private static final double SOLO_RETURN_FINISH_DISTANCE = 1.5D;
    private static final int TENTACLE_RESOLVE_GRACE_TICKS = 10;

    private static final double HOLD_HEIGHT_ABOVE_OWNER = 2.35D;
    private static final double HOLD_VERTICAL_SPEED = 0.08D;
    private static final double HOLD_HORIZONTAL_SPEED = 0.02D;
    private static final double HOLD_MAX_UPWARD_DELTA = 0.10D;

    private static final double HELD_SLOT_SIDE_OFFSET = 2.6D;
    private static final double HELD_SLOT_FORWARD_OFFSET = 1.35D;
    private static final double MIN_HELD_ENTITY_SEPARATION = 1.5D;
    private static final double HELD_SEPARATION_PUSH = 0.16D;
    private static final double HELD_SEPARATION_SEARCH_RADIUS = 12.0D;



    private boolean slotResolved = false;

    public TentacleProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public TentacleProjectile(EntityType<? extends AbstractArrow> entityType, Level level, LivingEntity shooter, float damage) {
        super(entityType, shooter, level);
        this.entityData.set(OWNER_ID, shooter.getId());
        this.setDamageAmount(damage);
    }

    public Float getDamageAmount() {
        return this.entityData.get(DAMAGE);
    }

    public void setDamageAmount(Float value) {
        this.entityData.set(DAMAGE, value);
    }

    public void setTentacleSlot(int slot) {
        this.entityData.set(TENTACLE_SLOT, slot);
    }

    public int getTentacleSlot() {
        return this.entityData.get(TENTACLE_SLOT);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DAMAGE, 0.0F);
        builder.define(OWNER_ID, -1);
        builder.define(VICTIM_ID, -1);
        builder.define(TENTACLE_ID, -1);
        builder.define(TENTACLE_SLOT, -1);
        builder.define(SHOT, false);
        builder.define(POST_HIT_LIFE, 0);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getWaterInertia() {
        return 1.0F;
    }



    private boolean isOwner(Entity target) {
        Entity owner = this.getOwnerById();
        if (owner == null || target == null) {
            return false;
        }
        return target == owner || target.getId() == owner.getId();
    }

    private boolean isOrganoid(Entity target) {
        return target instanceof Organoid;
    }

    private boolean isAlreadyAttached() {
        return this.entityData.get(SHOT) || this.getVictimById() != null;
    }

    private boolean isBeingGrabbedByTentacle(Entity target) {
        if (target == null) {
            return false;
        }

        Tentacle attachedTentacle = getAttachedTentacleFor(target);
        return attachedTentacle != null && attachedTentacle.isAlive();
    }

    private boolean isVictimOfAnotherTentacleProjectile(Entity target) {
        if (target == null || this.level() == null) {
            return false;
        }

        for (Entity entity : this.level().getEntities(this, target.getBoundingBox().inflate(8.0D))) {
            if (!(entity instanceof TentacleProjectile otherProjectile) || otherProjectile == this) {
                continue;
            }

            Entity otherVictim = otherProjectile.getVictimById();
            if (otherVictim != null && otherVictim.getId() == target.getId()) {
                return true;
            }
        }

        return false;
    }

    public Tentacle getActiveTentacleEntity() {
        Tentacle trackedTentacle = this.getTrackedTentacle();
        if (trackedTentacle != null && trackedTentacle.isAlive()) {
            return trackedTentacle;
        }

        Entity victim = this.getVictimById();
        if (victim != null) {
            Tentacle fallbackTentacle = getAttachedTentacleFor(victim);
            if (fallbackTentacle != null && fallbackTentacle.isAlive()) {
                return fallbackTentacle;
            }
        }

        return null;
    }

    private boolean isTentacleInDeathAnimation(Entity target) {
        if (target == null) {
            return false;
        }

        Tentacle attachedTentacle = getAttachedTentacleFor(target);
        if (attachedTentacle == null) {
            return false;
        }

        // Ignora si el tentáculo no está vivo
        if (!attachedTentacle.isAlive()) {
            return true;
        }

        // Si el tentáculo está vivo, consultamos el estado del slot en la capability del owner
        Entity owner = attachedTentacle.getTentacleOwner();
        if (owner == null || !(owner instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        int slot = attachedTentacle.getTentacleSlot();
        if (slot < 1 || slot > AbyssalTentaclePacket.ABSOLUTE_MAX_TENTACLES) {
            return false;
        }

        CompoundTag data = serverPlayer.getPersistentData();
        String state = AbyssalTentaclePacket.getSlotState(data, slot);

        // Ignora si el estado es DEAD
        return AbyssalTentaclePacket.STATE_DEAD.equals(state);
    }

    private boolean shouldIgnoreTarget(Entity target) {
        return target == null
                || target instanceof Tentacle
                || isOrganoid(target)
                || isOwner(target)
                || isAlreadyAttached()
                || isBeingGrabbedByTentacle(target)
                || isVictimOfAnotherTentacleProjectile(target)
                || isTentacleInDeathAnimation(target);
    }

    private boolean isBlockedByShield(LivingEntity target) {
        if (!target.isBlocking()) {
            return false;
        }

        Vec3 view = target.getViewVector(1.0F);
        Vec3 projectileToTarget = this.position().subtract(target.position()).normalize();

        return projectileToTarget.dot(view) < -0.1D;
    }

    private void onShieldBlocked(LivingEntity target) {
        target.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.0F);
        triggerReturnToOwner();
    }

    private double getVictimPullSpeed(LivingEntity victim) {
        double speedMultiplier = RETURN_SPEED;

        if (victim.isInWater()) {
            speedMultiplier *= 0.75D;
        } else {
            speedMultiplier *= 0.50D;
        }

        double widthFactor = victim.getBbWidth() / DEFAULT_PLAYER_WIDTH;
        double heightFactor = victim.getBbHeight() / DEFAULT_PLAYER_HEIGHT;

        double sizeFactor = (widthFactor * 0.35D) + (heightFactor * 0.65D);

        double reduction = Mth.clamp((sizeFactor - 1.0D) * 0.40D, 0.0D, 0.90D);

        return Math.max(0.0D, speedMultiplier * (1.0D - reduction));
    }

    private Vec3 getOwnerHoldAnchor(Entity owner) {
        Vec3 forward = owner.getLookAngle();
        Vec3 flatForward = new Vec3(forward.x, 0.0D, forward.z);
        if (flatForward.lengthSqr() < 0.0001D) {
            flatForward = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            flatForward = flatForward.normalize();
        }

        Vec3 right = new Vec3(-flatForward.z, 0.0D, flatForward.x);
        double sideOffset = switch (Mth.clamp(getTentacleSlot(), 1, 3)) {
            case 1 -> -HELD_SLOT_SIDE_OFFSET;
            case 2 -> 0.0D;
            case 3 -> HELD_SLOT_SIDE_OFFSET;
            default -> 0.0D;
        };

        return owner.position()
                .add(0.0D, owner.getBbHeight() / 2.0D + HOLD_HEIGHT_ABOVE_OWNER, 0.0D)
                .add(flatForward.scale(HELD_SLOT_FORWARD_OFFSET))
                .add(right.scale(sideOffset));
    }

    private Vec3 getCloseHoldMotion(Entity owner, LivingEntity victim) {
        Vec3 ownerHoldPos = getOwnerHoldAnchor(owner);
        Vec3 victimPos = victim.position().add(0.0D, victim.getBbHeight() * 0.55D, 0.0D);

        Vec3 offset = ownerHoldPos.subtract(victimPos);

        double horizontalX = offset.x * HOLD_HORIZONTAL_SPEED;
        double horizontalZ = offset.z * HOLD_HORIZONTAL_SPEED;
        double verticalY = Mth.clamp(offset.y * HOLD_VERTICAL_SPEED, -0.08D, HOLD_MAX_UPWARD_DELTA);

        return new Vec3(horizontalX, verticalY, horizontalZ);
    }

    private Vec3 getHeldEntitySeparationMotion(Entity owner, LivingEntity victim) {
        Vec3 separation = Vec3.ZERO;
        Vec3 victimPos = victim.position().add(0.0D, victim.getBbHeight() * 0.55D, 0.0D);

        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(HELD_SEPARATION_SEARCH_RADIUS))) {
            if (!(entity instanceof TentacleProjectile otherProjectile) || otherProjectile == this) {
                continue;
            }

            if (!otherProjectile.entityData.get(SHOT)) {
                continue;
            }

            Entity otherOwner = otherProjectile.getOwnerById();
            if (otherOwner == null || otherOwner.getId() != owner.getId()) {
                continue;
            }

            Entity otherVictimEntity = otherProjectile.getVictimById();
            if (!(otherVictimEntity instanceof LivingEntity otherVictim) || !otherVictim.isAlive() || otherVictim == victim) {
                continue;
            }

            Vec3 otherPos = otherVictim.position().add(0.0D, otherVictim.getBbHeight() * 0.55D, 0.0D);

            Vec3 horizontalOffset = new Vec3(
                    victimPos.x - otherPos.x,
                    0.0D,
                    victimPos.z - otherPos.z
            );

            double distance = horizontalOffset.length();

            if (distance < 0.0001D) {
                double angle = (victim.getId() * 31 + otherVictim.getId() * 17) * 0.12D;
                horizontalOffset = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                distance = 1.0D;
            }

            if (distance < MIN_HELD_ENTITY_SEPARATION) {
                double strength = 1.0D - (distance / MIN_HELD_ENTITY_SEPARATION);
                separation = separation.add(horizontalOffset.normalize().scale(HELD_SEPARATION_PUSH * strength));
            }
        }

        return separation;
    }

    public void releaseVictimAndReturn() {
        if (!this.level().isClientSide) {
            Tentacle trackedTentacle = this.getTrackedTentacle();
            if (trackedTentacle != null && trackedTentacle.isAlive()) {
                trackedTentacle.discard();
            } else {
                Entity victim = this.getVictimById();
                if (victim != null) {
                    Tentacle fallbackTentacle = getAttachedTentacleFor(victim);
                    if (fallbackTentacle != null && fallbackTentacle.isAlive()) {
                        fallbackTentacle.discard();
                    }
                }
            }
        }

        setSlotState(AbyssalTentaclePacket.STATE_RETURNING);

        this.entityData.set(VICTIM_ID, -1);
        this.entityData.set(TENTACLE_ID, -1);
        this.entityData.set(SHOT, true);
        this.entityData.set(POST_HIT_LIFE, 0);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.hurtMarked = true;
        this.playSound(SoundEvents.SLIME_BLOCK_STEP, 1.0F, 1.0F);
    }

    private void setSlotState(String state) {
        if (this.level().isClientSide) {
            return;
        }

        Entity owner = this.getOwnerById();
        int slot = this.getTentacleSlot();

        if (!(owner instanceof ServerPlayer serverPlayer) || slot < 1 || slot > AbyssalTentaclePacket.ABSOLUTE_MAX_TENTACLES) {
            return;
        }

        CompoundTag data = serverPlayer.getPersistentData();
        AbyssalTentaclePacket.normalizeSlotStates(data);
        AbyssalTentaclePacket.setSlotState(data, slot, state);
    }

    private void resolveSlotAsReady() {
        if (this.slotResolved) {
            return;
        }

        setSlotState(AbyssalTentaclePacket.STATE_READY);
        this.slotResolved = true;
    }

    private void resolveSlotAsDead() {
        if (this.slotResolved) {
            return;
        }

        setSlotState(AbyssalTentaclePacket.STATE_DEAD);

        Entity owner = this.getOwnerById();
        int slot = this.getTentacleSlot();

        if (owner instanceof ServerPlayer serverPlayer && slot >= 1 && slot <= AbyssalTentaclePacket.ABSOLUTE_MAX_TENTACLES) {
            CompoundTag data = serverPlayer.getPersistentData();
            AbyssalTentacleRegenEvents.startRegenTimer(data, slot, serverPlayer);
        }

        this.slotResolved = true;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (shouldIgnoreTarget(target)) {
            return false;
        }

        return super.canHitEntity(target);
    }

    public Entity getOwnerById() {
        int i = this.entityData.get(OWNER_ID);
        return i == -1 ? null : this.level().getEntity(i);
    }

    public Entity getVictimById() {
        int i = this.entityData.get(VICTIM_ID);
        return i == -1 ? null : this.level().getEntity(i);
    }

    public Tentacle getTrackedTentacle() {
        int i = this.entityData.get(TENTACLE_ID);
        if (i == -1) {
            return null;
        }

        Entity entity = this.level().getEntity(i);
        return entity instanceof Tentacle tentacle ? tentacle : null;
    }

    private void triggerReturnToOwner() {
        setSlotState(AbyssalTentaclePacket.STATE_RETURNING);

        this.entityData.set(VICTIM_ID, -1);
        this.entityData.set(TENTACLE_ID, -1);
        this.entityData.set(SHOT, true);
        this.entityData.set(POST_HIT_LIFE, 0);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.hurtMarked = true;
        this.playSound(SoundEvents.SLIME_BLOCK_STEP, 1.0F, 1.0F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setDamageAmount(tag.getFloat("TentacleDamage"));
        this.entityData.set(OWNER_ID, tag.getInt("OwnerId"));
        this.entityData.set(VICTIM_ID, tag.getInt("VictimId"));
        this.entityData.set(TENTACLE_ID, tag.getInt("TentacleId"));
        this.entityData.set(TENTACLE_SLOT, tag.getInt("TentacleSlot"));
        this.entityData.set(SHOT, tag.getBoolean("Shot"));
        this.entityData.set(POST_HIT_LIFE, tag.getInt("PostHitLife"));
        this.slotResolved = tag.getBoolean("SlotResolved");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("TentacleDamage", this.getDamageAmount());
        tag.putInt("OwnerId", this.entityData.get(OWNER_ID));
        tag.putInt("VictimId", this.entityData.get(VICTIM_ID));
        tag.putInt("TentacleId", this.entityData.get(TENTACLE_ID));
        tag.putInt("TentacleSlot", this.entityData.get(TENTACLE_SLOT));
        tag.putBoolean("Shot", this.entityData.get(SHOT));
        tag.putInt("PostHitLife", this.entityData.get(POST_HIT_LIFE));
        tag.putBoolean("SlotResolved", this.slotResolved);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = this.getOwnerById();

        if (shouldIgnoreTarget(target)) {
            return;
        }

        if (!(target instanceof LivingEntity living)) {
            return;
        }

        if (isBlockedByShield(living)) {
            onShieldBlocked(living);
            return;
        }

        Tentacle tentacle = null;
        if (!this.level().isClientSide) {
            tentacle = spawnAttachedTentacle(living, owner);
            if (tentacle == null) {
                resolveSlotAsDead();
                return;
            }
        }

        boolean sameSporeTeam = isOnSporeTeam(target);

        this.entityData.set(VICTIM_ID, living.getId());
        this.entityData.set(TENTACLE_ID, tentacle != null ? tentacle.getId() : -1);
        this.entityData.set(POST_HIT_LIFE, 0);
        this.entityData.set(SHOT, true);
        setSlotState(AbyssalTentaclePacket.STATE_DEPLOYED);

        if (!sameSporeTeam) {
            living.hurt(this.damageSources().arrow(this, this.getOwner()), this.getDamageAmount());
            living.invulnerableTime = 0;
        }

        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.hurtMarked = true;
        this.playSound(SoundEvents.SLIME_BLOCK_STEP, 1.0F, 1.0F);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!isAlreadyAttached()) {
            triggerReturnToOwner();
        }
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.SLIME_BLOCK_STEP;
    }

    private boolean isOnSporeTeam(Entity entity) {
        return entity != null && entity.getTeam() != null && "spore".equalsIgnoreCase(entity.getTeam().getName());
    }

    private Tentacle getAttachedTentacleFor(Entity target) {
        for (Entity entity : this.level().getEntities(target, target.getBoundingBox().inflate(1.5D))) {
            if (entity instanceof Tentacle tentacle) {
                Entity attachedTarget = tentacle.getAttachedTarget();
                if (attachedTarget != null && attachedTarget.getId() == target.getId()) {
                    return tentacle;
                }
            }
        }
        return null;
    }

    private void discardWithTentacles() {
        if (!this.level().isClientSide) {
            Tentacle trackedTentacle = this.getTrackedTentacle();
            if (trackedTentacle != null && trackedTentacle.isAlive()) {
                trackedTentacle.discard();
            } else {
                Entity victim = this.getVictimById();
                if (victim != null) {
                    Tentacle tentacle = getAttachedTentacleFor(victim);
                    if (tentacle != null && tentacle.isAlive()) {
                        tentacle.discard();
                    }
                }
            }
        }
        this.discard();
    }

    private float computeTentacleBaseScale(Entity target) {
        float widthFactor = target.getBbWidth() / DEFAULT_PLAYER_WIDTH;
        float heightFactor = target.getBbHeight() / DEFAULT_PLAYER_HEIGHT;
        float combinedScale = (widthFactor * 0.4F) + (heightFactor * 0.6F);
        return Mth.clamp(combinedScale, 0.35F, 8.0F);
    }

    private void applyTentacleBaseScale(Tentacle tentacle, Entity target) {
        float scale = computeTentacleBaseScale(target);
        ScaleData baseScale = ScaleTypes.BASE.getScaleData(tentacle);
        baseScale.setScale(scale);
        tentacle.refreshDimensions();
    }

    private Tentacle spawnAttachedTentacle(LivingEntity target, Entity owner) {
        if (this.level().isClientSide) {
            return null;
        }

        if (isOwner(target) || isOrganoid(target)) {
            return null;
        }

        Tentacle existing = getAttachedTentacleFor(target);
        if (existing != null) {
            return existing;
        }

        Tentacle tentacle = ModEntities.TENTACLE.get().create(this.level());
        if (tentacle == null) {
            return null;
        }

        double x = target.getX();
        double y = target.getY() + (target.getBbHeight() * 0.5D) - 0.45D;
        double z = target.getZ();

        tentacle.moveTo(x, y, z, target.getYRot(), target.getXRot());
        tentacle.setAttachedTargetId(target.getId());

        if (owner != null) {
            tentacle.setTentacleOwnerId(owner.getId());
            tentacle.applyOwnerLevelStats();
        }

        applyTentacleBaseScale(tentacle, target);
        this.level().addFreshEntity(tentacle);
        return tentacle;
    }

    @Override
    public void tick() {
        super.tick();

        Entity owner = this.getOwnerById();
        Entity victim = this.getVictimById();
        Tentacle trackedTentacle = this.getTrackedTentacle();

        this.setNoGravity(this.isInWater() || this.entityData.get(SHOT));

        // 1. Si NO ha impactado aún, comprueba distancia para volver
        if (owner != null && !this.entityData.get(SHOT)) {
            if (this.distanceTo(owner) >= MAX_DISTANCE_FROM_OWNER_PROYECTILE) {
                triggerReturnToOwner();
            }
        }

        // 2. Si HA impactado
        if (this.entityData.get(SHOT)) {
            int life = this.entityData.get(POST_HIT_LIFE) + 1;
            this.entityData.set(POST_HIT_LIFE, life);

            if (victim != null && trackedTentacle == null) {
                Tentacle fallbackTentacle = getAttachedTentacleFor(victim);
                if (fallbackTentacle != null && fallbackTentacle.isAlive()) {
                    trackedTentacle = fallbackTentacle;
                    this.entityData.set(TENTACLE_ID, fallbackTentacle.getId());
                }
            }

            if (victim != null && life > TENTACLE_RESOLVE_GRACE_TICKS) {
                if (victim.isAlive()) {
                    if (trackedTentacle == null || !trackedTentacle.isAlive()) {
                        resolveSlotAsDead();
                        this.discard();
                        return;
                    }
                } else {
                    triggerReturnToOwner();
                    victim = null;
                    trackedTentacle = null;
                }
            }
        }

        // 3. Lógica de movimiento / tracción / retorno
        if (owner != null && this.entityData.get(SHOT)) {
            Vec3 ownerPos = owner.position().add(0.0D, owner.getBbHeight() / 2.0D, 0.0D);

            if (victim instanceof LivingEntity livingVictim && livingVictim.isAlive()) {
                if (trackedTentacle == null) {
                    if (this.entityData.get(POST_HIT_LIFE) > TENTACLE_RESOLVE_GRACE_TICKS) {
                        resolveSlotAsDead();
                        this.discard();
                    }
                    return;
                }

                if (!trackedTentacle.isAlive()) {
                    resolveSlotAsDead();
                    this.discard();
                    return;
                }

                Vec3 victimPos = livingVictim.position().add(0.0D, livingVictim.getBbHeight() * 0.55D, 0.0D);
                Vec3 victimToOwner = ownerPos.subtract(victimPos);
                double victimOwnerDistance = victimToOwner.length();

                // --- NUEVO CÓDIGO AÑADIDO: Comprobar la distancia absoluta de rotura ---
                if (victimOwnerDistance > MAX_DISTANCE_FROM_OWNER) {
                    releaseVictimAndReturn();
                    return;
                }
                // ------------------------------------------------------------------------

                if (victimOwnerDistance > STOP_PULL_DISTANCE) {
                    double speedMultiplier = getVictimPullSpeed(livingVictim);

                    if (speedMultiplier <= 0.0D) {
                        // (La comprobación de MAX_DISTANCE ya se hace arriba, pero lo dejamos por seguridad)
                        this.setDeltaMovement(Vec3.ZERO);
                        this.hurtMarked = true;

                        Vec3 towardOwner = victimToOwner.lengthSqr() > 0.0001D
                                ? victimToOwner.normalize().scale(PROJECTILE_OFFSET_FROM_VICTIM)
                                : Vec3.ZERO;

                        this.setPos(
                                victimPos.x + towardOwner.x,
                                victimPos.y + towardOwner.y,
                                victimPos.z + towardOwner.z
                        );
                        return;
                    }

                    Vec3 pullMotion = victimToOwner.normalize().scale(speedMultiplier);
                    livingVictim.fallDistance = 0.0F;
                    livingVictim.setDeltaMovement(pullMotion);
                    livingVictim.hurtMarked = true;

                    this.setDeltaMovement(pullMotion);
                    this.hurtMarked = true;

                    Vec3 towardOwner = victimToOwner.normalize().scale(PROJECTILE_OFFSET_FROM_VICTIM);
                    this.setPos(
                            victimPos.x + towardOwner.x,
                            victimPos.y + towardOwner.y,
                            victimPos.z + towardOwner.z
                    );
                } else {
                    double speedMultiplier = getVictimPullSpeed(livingVictim);

                    if (speedMultiplier <= 0.0D) {
                        this.setDeltaMovement(Vec3.ZERO);
                        this.hurtMarked = true;

                        Vec3 towardOwner = victimToOwner.lengthSqr() > 0.0001D
                                ? victimToOwner.normalize().scale(PROJECTILE_OFFSET_FROM_VICTIM)
                                : Vec3.ZERO;

                        this.setPos(
                                victimPos.x + towardOwner.x,
                                victimPos.y + towardOwner.y,
                                victimPos.z + towardOwner.z
                        );
                        return;
                    }

                    Vec3 holdMotion = getCloseHoldMotion(owner, livingVictim)
                            .add(getHeldEntitySeparationMotion(owner, livingVictim));

                    livingVictim.fallDistance = 0.0F;
                    livingVictim.setDeltaMovement(holdMotion);
                    livingVictim.hurtMarked = true;

                    this.setDeltaMovement(Vec3.ZERO);
                    this.hurtMarked = true;

                    Vec3 targetAnchor = getOwnerHoldAnchor(owner).add(getHeldEntitySeparationMotion(owner, livingVictim));
                    Vec3 projectileOffset = victimPos.subtract(targetAnchor).lengthSqr() > 0.0001D
                            ? victimPos.subtract(targetAnchor).normalize().scale(PROJECTILE_OFFSET_FROM_VICTIM)
                            : Vec3.ZERO;

                    this.setPos(
                            victimPos.x - projectileOffset.x,
                            victimPos.y - projectileOffset.y,
                            victimPos.z - projectileOffset.z
                    );
                }
            } else {
                // Return to owner if no valid victim
                Vec3 direction = ownerPos.subtract(this.position());
                double distance = direction.length();

                if (distance > SOLO_RETURN_FINISH_DISTANCE) {
                    setSlotState(AbyssalTentaclePacket.STATE_RETURNING);

                    Vec3 motion = direction.normalize().scale(RETURN_SPEED);
                    this.setDeltaMovement(motion);
                    this.hurtMarked = true;
                    this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
                } else {
                    resolveSlotAsReady();
                    this.discard();
                }
            }
        } else if (this.entityData.get(SHOT) && victim == null) {
            resolveSlotAsDead();
            this.discard();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            Tentacle trackedTentacle = this.getTrackedTentacle();
            if (trackedTentacle != null && trackedTentacle.isAlive()) {
                trackedTentacle.discard();
            } else {
                Entity victim = this.getVictimById();
                if (victim != null) {
                    Tentacle tentacle = getAttachedTentacleFor(victim);
                    if (tentacle != null && tentacle.isAlive()) {
                        tentacle.discard();
                    }
                }
            }
        }
        super.remove(reason);
    }

}