package com.sporeadds.sporeaddsmod.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

public class DecoyEntity extends ArmorStand {

    private static final EntityDataAccessor<Boolean> DUMMY = SynchedEntityData.defineId(DecoyEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int INVISIBILITY_DURATION_TICKS = Integer.MAX_VALUE;

    private UUID ownerUUID;

    public DecoyEntity(EntityType<? extends ArmorStand> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setInvulnerable(false);
        this.setShowArms(false);
        this.setNoBasePlate(true);

        this.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY,
                INVISIBILITY_DURATION_TICKS,
                0,
                false,
                false,
                false
        ));

        this.setInvisible(true);
    }

    public void setOwner(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DUMMY, true);
    }

    @Override
    public boolean isAffectedByPotions() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            playHitEffects(serverLevel);
            com.sporeadds.sporeaddsmod.abilities.DecoyAbility.onDecoyHurt(this, source);
        }

        return result;
    }

    private void playHitEffects(ServerLevel serverLevel) {
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LEAVES.defaultBlockState()),
                this.getX(), this.getY() + 1.0, this.getZ(),
                20, 0.4, 0.6, 0.4, 0.05
        );

        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SHORT_GRASS.defaultBlockState()),
                this.getX(), this.getY() + 0.5, this.getZ(),
                15, 0.4, 0.4, 0.4, 0.05
        );

        serverLevel.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.WOOD_HIT,
                SoundSource.HOSTILE,
                1.0F, 1.0F
        );

        serverLevel.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.GRASS_BREAK,
                SoundSource.HOSTILE,
                0.8F, 1.2F
        );

        serverLevel.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR,
                SoundSource.HOSTILE,
                0.6F, 1.5F
        );
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide()) {
            com.sporeadds.sporeaddsmod.abilities.DecoyAbility.onDecoyRemoved(this);
        }
        super.remove(reason);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }
}