package com.sporeadds.sporeaddsmod.entity.projectile;

import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ThrowableBandagesEntity extends ThrowableItemProjectile {

    public ThrowableBandagesEntity(EntityType<? extends ThrowableBandagesEntity> type, Level level) {
        super(type, level);
    }

    public ThrowableBandagesEntity(Level level, LivingEntity thrower) {
        super(ModEntities.THROWABLE_BANDAGES.get(), thrower, level);
    }

    public ThrowableBandagesEntity(Level level, double x, double y, double z) {
        super(ModEntities.THROWABLE_BANDAGES.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.THROWABLE_BANDAGES.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (result.getEntity() instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 0));

            serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    target.getX(),
                    target.getY() + target.getBbHeight() + 0.2D,
                    target.getZ(),
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );

            Entity owner = this.getOwner();
            if (owner != null) {
                owner.level().playSound(
                        null,
                        owner.getX(), owner.getY(), owner.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        0.8F,
                        1.0F
                );

                for (int i = 0; i < 3; i++) {
                    ExperienceOrb orb = new ExperienceOrb(
                            serverLevel,
                            owner.getX() + (serverLevel.random.nextDouble() - 0.5D) * 0.4D,
                            owner.getY() + 0.5D,
                            owner.getZ() + (serverLevel.random.nextDouble() - 0.5D) * 0.4D,
                            11
                    );
                    serverLevel.addFreshEntity(orb);
                }
            }
        }

        this.spawnImpactEffects();
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.spawnImpactEffects();
        this.discard();
    }

    private void spawnImpactEffects() {
        Level level = this.level();

        if (level.isClientSide) {
            return;
        }

        level.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.SNOW_STEP,
                SoundSource.PLAYERS,
                1.0F,
                1.2F
        );

        if (level instanceof ServerLevel serverLevel) {
            BlockState carpetState = Blocks.WHITE_CARPET.defaultBlockState();

            serverLevel.sendParticles(
                    new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, carpetState),
                    this.getX(), this.getY(), this.getZ(),
                    12,
                    0.2, 0.2, 0.2,
                    0.05
            );
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03F;
    }
}