package com.sporeadds.sporeaddsmod.entity.projectile;

import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import com.sporeadds.sporeaddsmod.items.ReinforcedCombatChainsItem;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ChainProjectileEntity extends ThrowableProjectile {

    private static final EntityDataAccessor<Boolean> ATTACHED =
            SynchedEntityData.defineId(ChainProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(ChainProjectileEntity.class, EntityDataSerializers.INT);

    private static final double MAX_CHAIN_DISTANCE = 80.0D;
    private static final double PULL_START_DISTANCE = 15.0D;

    private static final int SOFT_APPROACH_TICKS = 14;
    private static final double SOFT_APPROACH_EXTRA_DISTANCE = 2.5D;
    private static final double BASE_FORCE_MULTIPLIER = 0.26D;
    private static final double RELATIVE_DAMPING = 0.26D;

    private static final float ADVANTAGE_SCALE = 0.025F;
    private static final float ADVANTAGE_CLAMP = 0.9F;
    private static final float NON_PLAYER_ADVANTAGE_MULTIPLIER = 1.35F;
    private static final float NON_PLAYER_ADVANTAGE_CLAMP = 0.95F;
    private static final float PLAYER_VS_PLAYER_ADVANTAGE_CLAMP = 0.5F;
    private static final float STRONG_PULL_THRESHOLD = 0.4F;

    private static final double LOSING_TARGET_LATERAL_DAMPING = 0.5D;
    private static final double LOSING_OWNER_LATERAL_DAMPING = 0.5D;

    private static final double ENDPOINT_CAP_1 = 0.12D;
    private static final double ENDPOINT_CAP_2 = 0.20D;
    private static final double ENDPOINT_CAP_3 = 0.30D;
    private static final double ENDPOINT_CAP_4 = 0.48D;

    private static final byte EVENT_CHAIN_SHAKE = 60;
    private static final byte EVENT_CHAIN_BREAK = 61;

    private UUID itemUUID;
    private int tickCounter = 0;
    private int attachTicks = 0;
    public int shakeTimer = 0;

    public ChainProjectileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public ChainProjectileEntity(Level level, Player shooter, UUID itemUUID) {
        super(ModEntities.CHAIN_PROJECTILE.get(), shooter, level);
        this.itemUUID = itemUUID;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ATTACHED, false);
        this.entityData.define(TARGET_ID, -1);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (this.level().isClientSide || this.entityData.get(ATTACHED)) {
            return;
        }

        Entity target = result.getEntity();

        if (target instanceof LivingEntity livingTarget && livingTarget.isBlocking()) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            this.discard();
            return;
        }

        Entity owner = this.getOwner();
        DamageSources damageSources = this.damageSources();
        DamageSource chainDamage = damageSources.thrown(this, owner != null ? owner : this);

        target.hurt(chainDamage, 3.0F);

        this.level().playSound(
                null,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                SoundEvents.CHAIN_HIT,
                SoundSource.PLAYERS,
                1.6F,
                0.85F + this.random.nextFloat() * 0.2F
        );

        this.level().playSound(
                null,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                SoundEvents.ANVIL_LAND,
                SoundSource.PLAYERS,
                0.55F,
                0.7F + this.random.nextFloat() * 0.15F
        );

        this.entityData.set(TARGET_ID, target.getId());
        this.entityData.set(ATTACHED, true);
        this.attachTicks = 0;
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level().isClientSide || this.entityData.get(ATTACHED)) {
            return;
        }

        BlockState state = this.level().getBlockState(result.getBlockPos());

        this.level().playSound(
                null,
                result.getLocation().x,
                result.getLocation().y,
                result.getLocation().z,
                SoundEvents.CHAIN_HIT,
                SoundSource.PLAYERS,
                0.8F,
                0.75F + this.random.nextFloat() * 0.15F
        );

        this.level().playSound(
                null,
                result.getBlockPos(),
                state.getSoundType().getBreakSound(),
                SoundSource.BLOCKS,
                0.45F,
                0.9F + this.random.nextFloat() * 0.2F
        );

        this.discard();
    }

    @Override
    public void tick() {
        if (this.level().isClientSide && this.shakeTimer > 0) {
            this.shakeTimer--;
        }

        if (!this.entityData.get(ATTACHED)) {
            super.tick();

            if (this.tickCount > 100 && !this.level().isClientSide) {
                this.discard();
            }
            return;
        }

        this.baseTick();

        if (this.level().isClientSide) {
            return;
        }

        Entity owner = this.getOwner();
        Entity target = this.level().getEntity(this.entityData.get(TARGET_ID));

        if (!(owner instanceof Player ownerPlayer) || !owner.isAlive() || target == null || !target.isAlive()) {
            this.discard();
            return;
        }

        this.attachTicks++;

        ownerPlayer.addEffect(new MobEffectInstance(effects.ENCHAINED, 5, 0, false, false, true));
        if (target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(effects.ENCHAINED, 5, 0, false, false, true));
        }

        ItemStack offhandStack = ownerPlayer.getOffhandItem();
        boolean inOffhand = this.isMatchingChain(offhandStack);

        if (!inOffhand) {
            this.triggerMassiveBreak(ownerPlayer, target, true);
            return;
        }

        double dist = owner.distanceTo(target);
        if (dist > MAX_CHAIN_DISTANCE) {
            this.triggerMassiveBreak(ownerPlayer, target, false);
            return;
        }

        double allowedDistance = PULL_START_DISTANCE;
        if (this.attachTicks < SOFT_APPROACH_TICKS) {
            float phase = 1.0F - (float) this.attachTicks / SOFT_APPROACH_TICKS;
            allowedDistance += SOFT_APPROACH_EXTRA_DISTANCE * phase;
        }

        this.cancelSeparatingMotionIfNeeded(owner, target, allowedDistance);

        if (dist > PULL_START_DISTANCE) {
            this.applyChainPull(owner, target, dist, allowedDistance);
        }

        if (this.tickCounter++ >= 20) {
            this.tickCounter = 0;
            this.handleDurabilityTick(ownerPlayer, target, offhandStack);
        }

        this.setPos(target.getX(), target.getY() + target.getBbHeight() / 2.0D, target.getZ());
    }

    private void cancelSeparatingMotionIfNeeded(Entity owner, Entity target, double allowedDistance) {
        Vec3 ownerPos = owner.position();
        Vec3 targetPos = target.position();
        Vec3 chainVector = targetPos.subtract(ownerPos);
        double currentDistance = chainVector.length();

        if (currentDistance <= 1.0E-4D) {
            return;
        }

        Vec3 chainDir = chainVector.normalize();
        Vec3 ownerMotion = owner.getDeltaMovement();
        Vec3 targetMotion = target.getDeltaMovement();

        double ownerAway = ownerMotion.dot(chainDir.scale(-1.0D));
        double targetAway = targetMotion.dot(chainDir);

        if (currentDistance >= allowedDistance) {
            if (ownerAway > 0.0D) {
                ownerMotion = removeSeparatingComponent(ownerMotion, chainDir.scale(-1.0D), ownerAway);
            }

            if (targetAway > 0.0D) {
                targetMotion = removeSeparatingComponent(targetMotion, chainDir, targetAway);
            }

            owner.setDeltaMovement(ownerMotion);
            target.setDeltaMovement(targetMotion);
            owner.hasImpulse = true;
            target.hasImpulse = true;
            syncPlayerMotion(owner);
            syncPlayerMotion(target);
        }
    }

    private void applyChainPull(Entity owner, Entity target, double dist, double allowedDistance) {
        double stretch = dist - PULL_START_DISTANCE;

        float ownerPower = getEntityPower(owner);
        float targetPower = getEntityPower(target);
        float powerDiff = ownerPower - targetPower;

        float bias = Mth.clamp(powerDiff * ADVANTAGE_SCALE, -ADVANTAGE_CLAMP, ADVANTAGE_CLAMP);

        boolean ownerIsPlayer = owner instanceof Player;
        boolean targetIsPlayer = target instanceof Player;

        if (ownerIsPlayer && targetIsPlayer) {
            bias = Mth.clamp(bias, -PLAYER_VS_PLAYER_ADVANTAGE_CLAMP, PLAYER_VS_PLAYER_ADVANTAGE_CLAMP);
        } else if (!targetIsPlayer) {
            bias = Mth.clamp(bias * NON_PLAYER_ADVANTAGE_MULTIPLIER, -NON_PLAYER_ADVANTAGE_CLAMP, NON_PLAYER_ADVANTAGE_CLAMP);
        }

        Vec3 dirToOwner = owner.position().subtract(target.position()).normalize();
        Vec3 dirToTarget = target.position().subtract(owner.position()).normalize();

        float t = Mth.clamp((float) this.attachTicks / SOFT_APPROACH_TICKS, 0.0F, 1.0F);
        double warmupFactor = 0.06D + 0.94D * t * t * t;

        double baseForce = stretch * BASE_FORCE_MULTIPLIER * warmupFactor;
        Vec3 relativeVelocity = target.getDeltaMovement().subtract(owner.getDeltaMovement());
        double relativeAlongChain = relativeVelocity.dot(dirToOwner);
        double dampedForce = Math.max(0.0D, baseForce - relativeAlongChain * RELATIVE_DAMPING);

        double forceOnOwner = dampedForce * (1.0D - bias);
        double forceOnTarget = dampedForce * (1.0D + bias);

        double ownerCap;
        double targetCap;

        if (this.attachTicks < 6) {
            ownerCap = ENDPOINT_CAP_1;
            targetCap = ENDPOINT_CAP_1;
        } else if (this.attachTicks < 12) {
            ownerCap = ENDPOINT_CAP_2;
            targetCap = ENDPOINT_CAP_2;
        } else if (this.attachTicks < 20) {
            ownerCap = ENDPOINT_CAP_3;
            targetCap = ENDPOINT_CAP_3;
        } else {
            ownerCap = ENDPOINT_CAP_4;
            targetCap = ENDPOINT_CAP_4;
        }

        forceOnOwner = Mth.clamp(forceOnOwner, 0.0D, ownerCap);
        forceOnTarget = Mth.clamp(forceOnTarget, 0.0D, targetCap);

        Vec3 currentTargetMov = target.getDeltaMovement();
        if (bias > STRONG_PULL_THRESHOLD) {
            currentTargetMov = currentTargetMov.multiply(LOSING_TARGET_LATERAL_DAMPING, 1.0D, LOSING_TARGET_LATERAL_DAMPING);
        }

        Vec3 currentOwnerMov = owner.getDeltaMovement();
        if (bias < -STRONG_PULL_THRESHOLD) {
            currentOwnerMov = currentOwnerMov.multiply(LOSING_OWNER_LATERAL_DAMPING, 1.0D, LOSING_OWNER_LATERAL_DAMPING);
        }

        target.setDeltaMovement(currentTargetMov.add(dirToOwner.scale(forceOnTarget)));
        owner.setDeltaMovement(currentOwnerMov.add(dirToTarget.scale(forceOnOwner)));

        target.hasImpulse = true;
        owner.hasImpulse = true;

        enforceMaximumSeparation(owner, target, allowedDistance);
        syncPlayerMotion(owner);
        syncPlayerMotion(target);
    }

    private void handleDurabilityTick(Player ownerPlayer, Entity target, ItemStack chainStack) {
        int chainsAttached = 0;

        for (Entity e : this.level().getEntitiesOfClass(ChainProjectileEntity.class, target.getBoundingBox().inflate(15.0D))) {
            if (e instanceof ChainProjectileEntity cpe
                    && cpe.entityData.get(ATTACHED)
                    && cpe.entityData.get(TARGET_ID).equals(this.entityData.get(TARGET_ID))) {
                chainsAttached++;
            }
        }

        chainStack.hurtAndBreak(chainsAttached, ownerPlayer, player -> this.triggerMassiveBreak(ownerPlayer, target, false));

        if (this.isAlive()) {
            float damageRatio = (float) chainStack.getDamageValue() / (float) chainStack.getMaxDamage();
            float pitch = 1.0F + damageRatio;
            this.level().playSound(null, target.blockPosition(), SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 2.0F, pitch);
            this.level().broadcastEntityEvent(this, EVENT_CHAIN_SHAKE);
        }
    }

    private float getEntityPower(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return living.getBbWidth() + living.getBbHeight();
        }
        return 40.0F;
    }

    private void syncPlayerMotion(Entity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.hurtMarked = true;
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }
    }

    private void enforceMaximumSeparation(Entity owner, Entity target, double allowedDistance) {
        Vec3 ownerPos = owner.position();
        Vec3 targetPos = target.position();
        Vec3 delta = targetPos.subtract(ownerPos);
        double dist = delta.length();

        if (dist <= allowedDistance || dist < 1.0E-4D) {
            return;
        }

        Vec3 dir = delta.normalize();
        Vec3 ownerMotion = owner.getDeltaMovement();
        Vec3 targetMotion = target.getDeltaMovement();

        double ownerSeparating = ownerMotion.dot(dir.scale(-1.0D));
        double targetSeparating = targetMotion.dot(dir);

        if (ownerSeparating > 0.0D) {
            ownerMotion = removeSeparatingComponent(ownerMotion, dir.scale(-1.0D), ownerSeparating);
        }

        if (targetSeparating > 0.0D) {
            targetMotion = removeSeparatingComponent(targetMotion, dir, targetSeparating);
        }

        owner.setDeltaMovement(ownerMotion);
        target.setDeltaMovement(targetMotion);
        owner.hasImpulse = true;
        target.hasImpulse = true;
    }

    private Vec3 removeSeparatingComponent(Vec3 motion, Vec3 separatingDir, double separatingAmount) {
        return motion.subtract(separatingDir.scale(separatingAmount));
    }

    private void triggerMassiveBreak(Player player, Entity target, boolean destroyItem) {
        if (destroyItem) {
            boolean foundAndDestroyed = false;

            if (this.isMatchingChain(player.getOffhandItem())) {
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                foundAndDestroyed = true;
            }

            ItemStack carriedStack = player.containerMenu.getCarried();
            if (!foundAndDestroyed && this.isMatchingChain(carriedStack)) {
                player.containerMenu.setCarried(ItemStack.EMPTY);
                foundAndDestroyed = true;
            }

            if (!foundAndDestroyed) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (this.isMatchingChain(stack)) {
                        stack.shrink(1);
                        foundAndDestroyed = true;
                        break;
                    }
                }
            }

            if (!foundAndDestroyed && this.isMatchingChain(player.getMainHandItem())) {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                foundAndDestroyed = true;
            }

            if (!foundAndDestroyed) {
                for (ItemEntity item : this.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32.0D))) {
                    if (this.isMatchingChain(item.getItem())) {
                        item.discard();
                        break;
                    }
                }
            }
        }

        this.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 5.0F, 0.5F);
        if (target != null) {
            this.level().playSound(null, target.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 5.0F, 0.5F);
        }

        this.level().broadcastEntityEvent(this, EVENT_CHAIN_BREAK);
        this.discard();
    }

    private boolean isMatchingChain(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (!(stack.getItem() instanceof ReinforcedCombatChainsItem)) {
            return false;
        }

        if (!stack.hasTag() || !stack.getTag().hasUUID("ChainUUID")) {
            return false;
        }

        UUID stackId = stack.getTag().getUUID("ChainUUID");
        return stackId.equals(this.itemUUID);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_CHAIN_SHAKE) {
            this.shakeTimer = 8;
            this.spawnChainParticles(this.getOwner(), 8);

            Entity target = this.level().getEntity(this.entityData.get(TARGET_ID));
            if (target != null) {
                this.spawnChainParticles(target, 8);
            }
            return;
        }

        if (id == EVENT_CHAIN_BREAK) {
            this.spawnChainParticles(this.getOwner(), 50);

            Entity target = this.level().getEntity(this.entityData.get(TARGET_ID));
            if (target != null) {
                this.spawnChainParticles(target, 50);
            }
            return;
        }

        super.handleEntityEvent(id);
    }

    private void spawnChainParticles(Entity entity, int count) {
        if (entity == null) {
            return;
        }

        for (int i = 0; i < count; i++) {
            double dx = entity.getX() + (this.random.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5D;
            double dy = entity.getY() + this.random.nextDouble() * entity.getBbHeight();
            double dz = entity.getZ() + (this.random.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5D;

            this.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CHAIN.defaultBlockState()),
                    dx, dy, dz,
                    (this.random.nextDouble() - 0.5D) * 0.5D,
                    this.random.nextDouble() * 0.5D,
                    (this.random.nextDouble() - 0.5D) * 0.5D
            );
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (this.itemUUID != null) {
            tag.putUUID("ItemUUID", this.itemUUID);
        }

        tag.putInt("AttachTicks", this.attachTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("ItemUUID")) {
            this.itemUUID = tag.getUUID("ItemUUID");
        }

        this.attachTicks = tag.getInt("AttachTicks");
    }

    public boolean isAttached() {
        return this.entityData.get(ATTACHED);
    }

    public int getTargetId() {
        return this.entityData.get(TARGET_ID);
    }

    public UUID getItemUUID() {
        return this.itemUUID;
    }
}