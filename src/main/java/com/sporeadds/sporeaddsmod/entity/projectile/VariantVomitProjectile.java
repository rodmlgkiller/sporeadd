package com.sporeadds.sporeaddsmod.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class VariantVomitProjectile extends ThrowableProjectile {

    private static final EntityDataAccessor<String> VARIANT =
            SynchedEntityData.defineId(VariantVomitProjectile.class, EntityDataSerializers.STRING);

    private static final float DEFAULT_IMPACT_VOLUME = 1.0F;
    private static final float FAR_IMPACT_VOLUME = 2.0F; // Usado para el alcance de ~16 bloques de Gore

    public VariantVomitProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public VariantVomitProjectile(EntityType<? extends ThrowableProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(VARIANT, "default");
    }

    public String getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(String variant) {
        this.entityData.set(VARIANT, variant);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("VomitVariant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("VomitVariant")) {
            this.setVariant(tag.getString("VomitVariant"));
        }
    }

    private float getProjectileVisualScale() {
        float width = this.getBbWidth();
        float height = this.getBbHeight();
        float base = Math.max(width, height) / 0.25F;
        return Mth.clamp(base, 0.35F, 6.0F);
    }

    private int scaledCount(int baseCount) {
        float scale = getProjectileVisualScale();
        return Math.max(1, Mth.ceil(baseCount * (0.6F + scale * 0.7F)));
    }

    private double scaledOffset(double baseOffset) {
        return baseOffset * getProjectileVisualScale();
    }

    private double scaledSpeed(double baseSpeed) {
        return baseSpeed * (0.65D + getProjectileVisualScale() * 0.35D);
    }

    private void playImpactSound(SoundEvent sound, float volume, float pitch) {
        if (sound != null) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    sound,
                    SoundSource.NEUTRAL,
                    volume,
                    pitch
            );
        }
    }

    private void resetIFrames(Entity target) {
        target.invulnerableTime = 0;

        if (target instanceof LivingEntity livingTarget) {
            livingTarget.invulnerableTime = 0;
            livingTarget.hurtTime = 0;
            livingTarget.hurtDuration = 0;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            String variant = this.getVariant();
            float scale = getProjectileVisualScale();
            Vec3 motion = this.getDeltaMovement();

            if ("bone".equalsIgnoreCase(variant)) {
                int count = Math.max(1, Mth.ceil(scale * 2.0F));

                for (int i = 0; i < count; i++) {
                    double ox = (this.random.nextDouble() - 0.5D) * (0.18D * scale);
                    double oy = (this.random.nextDouble() - 0.5D) * (0.12D * scale);
                    double oz = (this.random.nextDouble() - 0.5D) * (0.18D * scale);

                    double vx = -motion.x * 0.15D + (this.random.nextDouble() - 0.5D) * (0.03D * scale);
                    double vy = -motion.y * 0.15D + (this.random.nextDouble() - 0.5D) * (0.03D * scale);
                    double vz = -motion.z * 0.15D + (this.random.nextDouble() - 0.5D) * (0.03D * scale);

                    this.level().addParticle(
                            new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE)),
                            this.getX() + ox,
                            this.getY() + 0.05D * scale + oy,
                            this.getZ() + oz,
                            vx, vy, vz
                    );
                }
            } else if ("caustic".equalsIgnoreCase(variant)) {
                int count = Math.max(1, Mth.ceil(scale * 1.5F));

                for (int i = 0; i < count; i++) {
                    double ox = (this.random.nextDouble() - 0.5D) * (0.10D * scale);
                    double oy = (this.random.nextDouble() - 0.5D) * (0.10D * scale);
                    double oz = (this.random.nextDouble() - 0.5D) * (0.10D * scale);

                    this.level().addParticle(
                            ParticleTypes.EFFECT,
                            this.getX() + ox,
                            this.getY() + oy,
                            this.getZ() + oz,
                            0.0D,
                            0.8D * Math.min(scale, 2.0F),
                            0.0D
                    );
                }
            } else if ("gore".equalsIgnoreCase(variant)) {
                // Sangre para la variante gore
                int count = Math.max(1, Mth.ceil(scale * 1.5F));
                ParticleType<?> bloodType = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("spore", "blood_particle"));

                if (bloodType instanceof ParticleOptions bloodOption) {
                    for (int i = 0; i < count; i++) {
                        double ox = (this.random.nextDouble() - 0.5D) * (0.15D * scale);
                        double oy = (this.random.nextDouble() - 0.5D) * (0.15D * scale);
                        double oz = (this.random.nextDouble() - 0.5D) * (0.15D * scale);

                        this.level().addParticle(
                                bloodOption,
                                this.getX() + ox,
                                this.getY() + oy,
                                this.getZ() + oz,
                                0.0D, 0.0D, 0.0D
                        );
                    }
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;

        Entity target = result.getEntity();
        String variant = this.getVariant();

        resetIFrames(target);
        target.hurt(this.damageSources().thrown(this, this.getOwner()), 5.0F);
        resetIFrames(target);

        if (target instanceof LivingEntity livingTarget) {
            switch (variant.toLowerCase()) {
                case "caustic" -> {
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    playImpactSound(SoundEvents.SLIME_ATTACK, DEFAULT_IMPACT_VOLUME, 1.5F);
                }
                case "abyssal" -> {
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    playImpactSound(SoundEvents.WITHER_SHOOT, DEFAULT_IMPACT_VOLUME, 0.5F);
                }
                case "bone" -> {
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                    playImpactSound(SoundEvents.GOAT_HORN_BREAK, DEFAULT_IMPACT_VOLUME, 0.5F);
                }
                case "gore" -> {
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                    SoundEvent organoidDamage = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "organoid_damage"));
                    // Usamos FAR_IMPACT_VOLUME (2.0F) para que se escuche desde ~16 bloques
                    playImpactSound(organoidDamage, FAR_IMPACT_VOLUME, 0.5F);
                }
                default -> {
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                    playImpactSound(SoundEvents.SLIME_SQUISH, DEFAULT_IMPACT_VOLUME, 1.0F);
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) return;

        String variant = this.getVariant();

        switch (variant.toLowerCase()) {
            case "caustic" -> playImpactSound(SoundEvents.BREWING_STAND_BREW, DEFAULT_IMPACT_VOLUME, 1.0F);
            case "abyssal" -> playImpactSound(SoundEvents.SCULK_BLOCK_BREAK, DEFAULT_IMPACT_VOLUME, 0.8F);
            case "bone" -> playImpactSound(SoundEvents.BONE_BLOCK_BREAK, DEFAULT_IMPACT_VOLUME, 0.5F);
            case "gore" -> {
                SoundEvent organoidDamage = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "organoid_damage"));
                playImpactSound(organoidDamage, FAR_IMPACT_VOLUME, 1.0F);
            }
            default -> playImpactSound(SoundEvents.HONEY_BLOCK_BREAK, DEFAULT_IMPACT_VOLUME, 1.0F);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            String variant = this.getVariant();

            if ("bone".equalsIgnoreCase(variant)) {
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE)),
                        this.getX(), this.getY(), this.getZ(),
                        scaledCount(12),
                        scaledOffset(0.18D), scaledOffset(0.18D), scaledOffset(0.18D),
                        scaledSpeed(0.06D)
                );
            } else if ("caustic".equalsIgnoreCase(variant)) {
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SLIME_BALL)),
                        this.getX(), this.getY(), this.getZ(),
                        scaledCount(8),
                        scaledOffset(0.20D), scaledOffset(0.20D), scaledOffset(0.20D),
                        scaledSpeed(0.10D)
                );
            } else if ("gore".equalsIgnoreCase(variant)) {
                ParticleType<?> bloodType = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("spore", "blood_particle"));
                if (bloodType instanceof ParticleOptions bloodOption) {
                    serverLevel.sendParticles(
                            bloodOption,
                            this.getX(), this.getY(), this.getZ(),
                            scaledCount(10),
                            scaledOffset(0.25D), scaledOffset(0.25D), scaledOffset(0.25D),
                            scaledSpeed(0.05D)
                    );
                }
            }

            this.discard();
        }
    }
}