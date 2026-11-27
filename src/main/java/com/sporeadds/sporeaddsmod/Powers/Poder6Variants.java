package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.entity.MeatAbomination;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import org.joml.Vector3f;

public class Poder6Variants {

    public static boolean tryActivateVariant(ServerPlayer player, Entity target) {
        if (isgluttonous(player)) {
            return tryActivategluttonous(player, target);
        }
        return false;
    }

    public static boolean isgluttonous(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            return "gluttonous".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    private static int getTargetKills(Entity target) {
        if (target instanceof Infected infected) {
            return Math.max(0, infected.getKills());
        }

        CompoundTag tag = new CompoundTag();
        target.saveWithoutId(tag);
        if (tag.contains("kills")) {
            return Math.max(0, tag.getInt("kills"));
        }

        CompoundTag persistentData = target.getPersistentData();
        if (persistentData.contains("kills")) {
            return Math.max(0, persistentData.getInt("kills"));
        }

        return 0;
    }

    private static int applyKillDiscount(int baseCost, int kills) {
        return Math.max(1, baseCost - Math.max(0, kills));
    }

    private static boolean tryActivategluttonous(ServerPlayer player, Entity target) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return false;
        if (!(target instanceof LivingEntity livingTarget)) return false;

        long now = System.currentTimeMillis();
        long lastUse = Poder6.cooldowns.getOrDefault(player.getUUID(), 0L);
        if (now - lastUse < Poder6.COOLDOWN_MS) {
            return false;
        }

        if (target instanceof Calamity || target instanceof Organoid) {
            return false;
        }

        boolean isValidTarget = target instanceof Infected
                || target instanceof UtilityEntity
                || target instanceof Scamper;

        if (!isValidTarget) return false;

        Poder6.cooldowns.put(player.getUUID(), now);

        float hp = livingTarget.getHealth();
        int baseBiomass = 1;
        if (hp > 40.0F) {
            baseBiomass = 3;
        } else if (hp > 20.0F) {
            baseBiomass = 2;
        }

        SoundEvent evolveHurt = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "evolve_hurt"));
        if (evolveHurt != null) {
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), evolveHurt, SoundSource.HOSTILE, 1.0F, 0.5F);
        }

        SoundEvent hyperDamage = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "hyper_damage"));
        if (hyperDamage != null) {
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), hyperDamage, SoundSource.HOSTILE, 1.0F, 0.5F);
        }

        spawnConversionParticles(serverLevel, livingTarget);

        EntityType<?> meatType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "meat_abomination"));
        if (meatType != null) {
            Entity newEntity = meatType.create(serverLevel);
            if (newEntity instanceof MeatAbomination meatAbomination) {
                float randomYaw = serverLevel.random.nextFloat() * 360.0F;
                meatAbomination.moveTo(target.getX(), target.getY(), target.getZ(), randomYaw, 0.0F);
                meatAbomination.setYHeadRot(randomYaw);
                meatAbomination.setYBodyRot(randomYaw);
                meatAbomination.setBiomass((float) baseBiomass);
                serverLevel.addFreshEntity(meatAbomination);
            }
        }

        target.discard();
        return true;
    }

    private static void spawnConversionParticles(ServerLevel level, LivingEntity target) {
        DustParticleOptions redDust = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.5F);

        double width = target.getBbWidth();
        double height = target.getBbHeight();

        level.sendParticles(
                redDust,
                target.getX(),
                target.getY() + height / 2.0D,
                target.getZ(),
                50,
                width, height / 2.0D, width,
                0.1D
        );

        var bloodType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.fromNamespaceAndPath("spore", "blood_particle"));
        if (bloodType instanceof ParticleOptions bloodParticle) {
            level.sendParticles(
                    bloodParticle,
                    target.getX(),
                    target.getY() + height / 2.0D,
                    target.getZ(),
                    30,
                    width, height / 2.0D, width,
                    0.15D
            );
        }
    }
}