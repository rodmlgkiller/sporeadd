package com.sporeadds.sporeaddsmod.entity;

import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class MeatAbomination extends Organoid {

    public static final EntityDataAccessor<Float> BIOMASS =
            SynchedEntityData.defineId(MeatAbomination.class, EntityDataSerializers.FLOAT);

    private static final ResourceLocation WOMB_AMBIENT_ID = new ResourceLocation("spore", "womb_ambient");

    private float lastAppliedBiomass = -1.0F;

    public MeatAbomination(EntityType<? extends Organoid> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BIOMASS, 1.0F);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        return false;
    }

    @Override
    public boolean canDrownInFluidType(net.minecraftforge.fluids.FluidType type) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    public float getBiomass() {
        return this.entityData.get(BIOMASS);
    }

    private SoundEvent getWombAmbientSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(WOMB_AMBIENT_ID);
    }

    private void spawnRedDustParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double width = Math.max(0.35D, this.getBbWidth());
        double height = Math.max(0.35D, this.getBbHeight());

        int particleCount = Math.max(12, (int) Math.ceil((width + height) * 8.0D));
        float particleScale = 1.0F;

        double xSpread = Math.max(0.25D, width * 0.6D);
        double ySpread = Math.max(0.20D, height * 0.45D);
        double zSpread = Math.max(0.25D, width * 0.6D);

        serverLevel.sendParticles(
                new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), particleScale),
                this.getX(),
                this.getY() + (height * 0.5D),
                this.getZ(),
                particleCount,
                xSpread,
                ySpread,
                zSpread,
                0.01D
        );
    }

    private void applyBiomassStats(float biomass, boolean playEffects) {
        float clampedBiomass = Math.max(0.0F, biomass);

        if (playEffects && clampedBiomass < this.lastAppliedBiomass) {
            SoundEvent wombAmbient = getWombAmbientSound();
            if (wombAmbient != null) {
                this.level().playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        wombAmbient,
                        SoundSource.HOSTILE,
                        1.0F,
                        0.5F
                );
            }

            spawnRedDustParticles();
        }

        AttributeInstance maxHealthAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double oldMaxHealth = maxHealthAttr.getBaseValue();
            double newMaxHealth = Math.max(1.0D, clampedBiomass * 10.0D);
            maxHealthAttr.setBaseValue(newMaxHealth);

            if (newMaxHealth > oldMaxHealth) {
                this.heal((float) (newMaxHealth - oldMaxHealth));
            }

            if (this.getHealth() > newMaxHealth) {
                this.setHealth((float) newMaxHealth);
            } else if (this.getHealth() <= 0.0F) {
                this.setHealth((float) newMaxHealth);
            }
        }

        AttributeInstance armorAttr = this.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            double newArmor = Math.max(0.0D, clampedBiomass * 10.0D);
            armorAttr.setBaseValue(newArmor);
        }

        try {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            if (scaleData != null) {
                float visualScale = Math.max(0.1F, 1.0F + ((clampedBiomass - 1.0F) * 0.2F));
                scaleData.setTargetScale(visualScale);
            }
        } catch (Throwable ignored) {
        }

        this.refreshDimensions();
        this.lastAppliedBiomass = clampedBiomass;

        if (clampedBiomass <= 0.0F) {
            this.discard();
        }
    }

    public void setBiomass(float biomass) {
        float clampedBiomass = Math.max(0.0F, biomass);
        this.entityData.set(BIOMASS, clampedBiomass);
        applyBiomassStats(clampedBiomass, true);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);

        if (result && !this.level().isClientSide) {
            spawnRedDustParticles();
        }

        return result;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (BIOMASS.equals(key)) {
            applyBiomassStats(getBiomass(), false);
        }
    }

    @Override
    public int getExperienceReward() {
        return Math.max(0, (int) this.getBiomass());
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        int biomassInt = Math.max(0, (int) this.getBiomass());
        if (biomassInt <= 0) return;

        String[] possibleDrops = {
                "spore:tumor",
                "spore:mutated_fiber",
                "spore:innards",
                "spore:mutated_heart",
                "spore:organoid_membrane"
        };

        int totalDrops = biomassInt * 3;

        for (int i = 0; i < totalDrops; i++) {
            String randomItemName = possibleDrops[this.random.nextInt(possibleDrops.length)];
            Item dropItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(randomItemName));

            if (dropItem != null) {
                this.spawnAtLocation(dropItem);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Biomass", this.getBiomass());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Biomass")) {
            this.entityData.set(BIOMASS, Math.max(0.1F, tag.getFloat("Biomass")));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            float currentBiomass = Math.max(0.1F, this.getBiomass());
            if (Math.abs(currentBiomass - this.lastAppliedBiomass) > 0.001F) {
                applyBiomassStats(currentBiomass, false);
            }

            if (this.tickCount % 200 == 0) {
                SoundEvent wombAmbient = getWombAmbientSound();
                if (wombAmbient != null) {
                    this.level().playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            wombAmbient,
                            SoundSource.HOSTILE,
                            0.3F,
                            0.7F + (this.random.nextFloat() * 0.2F)
                    );
                }
            }
        }
    }
}