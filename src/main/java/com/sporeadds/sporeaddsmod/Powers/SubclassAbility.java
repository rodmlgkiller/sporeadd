package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.core.Sentities;
import com.Harbinger.Spore.Sentities.Projectile.GunProjectiles.AssassinBullet;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import com.sporeadds.sporeaddsmod.entity.projectile.GasGlobProjectile;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.atomic.AtomicInteger;

public class SubclassAbility {

    /** Fired every 2 client ticks while holding the fire key during Caustic's skill 9 spray mode. */
    public static void fireCausticSpraySegment(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        GasGlobProjectile projectile = new GasGlobProjectile(ModEntities.GAS_GLOB_PROJECTILE.get(), player, level);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.4F, 4.0F);
        level.addFreshEntity(projectile);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "chemist_fuse")),
                SoundSource.PLAYERS, 0.5F, 1.6F);

        if (player.getRandom().nextFloat() < 0.3F) {
            Vec3 look = player.getLookAngle();
            double baseX = player.getX() + look.x * 0.5D;
            double baseY = player.getEyeY() - 0.2D;
            double baseZ = player.getZ() + look.z * 0.5D;

            int count = 2 + player.getRandom().nextInt(2);
            for (int i = 0; i < count; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5D) * 2.0D;
                double oy = (player.getRandom().nextDouble() - 0.5D) * 1.4D;
                double oz = (player.getRandom().nextDouble() - 0.5D) * 2.0D;

                level.sendParticles(SporeaddParticleTypes.GAS_SMALL.get(),
                        baseX + ox, baseY + oy, baseZ + oz,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public static void fireCausticShot(ServerPlayer player, int ticksCharged) {
        int chargeLevels = Math.min(25, ticksCharged / 3);
        if (chargeLevels <= 0) return;

        player.causeFoodExhaustion(1.5f + chargeLevels * 1.0F);

        AssassinBullet bullet = new AssassinBullet(Sentities.ASSASSIN_BULLET.get(), player.level());
        bullet.setOwner(player);

        Vec3 lookVec = player.getLookAngle();
        double startX = player.getX() + lookVec.x * 0.5D;
        double startY = player.getEyeY() - 0.1D + lookVec.y * 0.5D;
        double startZ = player.getZ() + lookVec.z * 0.5D;

        bullet.setPos(startX, startY, startZ);
        bullet.setXRot(player.getXRot());
        bullet.setYRot(player.getYRot());

        AtomicInteger playerLvl = new AtomicInteger(0);
        player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(lvlCap -> {
            playerLvl.set(lvlCap.getLevel());
        });

        int level = playerLvl.get();

        double damagePerCharge = switch (level) {
            case 0 -> 0.20D;
            case 1 -> 0.22D;
            case 2 -> 0.25D;
            case 3 -> 0.29D;
            case 4 -> 0.36D;
            case 5 -> 0.52D;
            case 6 -> 0.59D;
            case 7 -> 0.78D;
            case 8 -> 0.90D;
            default -> 1.20D;
        };

        double newDamage = bullet.getBaseDamage() + (chargeLevels * damagePerCharge);
        bullet.setBaseDamage(newDamage);

        CompoundTag bulletData = new CompoundTag();
        bullet.saveWithoutId(bulletData);
        bulletData.putDouble("damage", newDamage);
        bullet.load(bulletData);

        int divisor = switch (level) {
            case 0, 1 -> 76;
            case 2, 3 -> 38;
            case 4 -> 25;
            case 5 -> 19;
            case 6 -> 15;
            case 7 -> 12;
            case 8 -> 10;
            default -> 8;
        };

        int maxAmplifier = switch (level) {
            case 0, 1 -> 0; // Corrosion I
            case 2, 3 -> 1; // Corrosion II
            case 4 -> 2;    // Corrosion III
            case 5 -> 3;    // Corrosion IV
            case 6 -> 4;    // Corrosion V
            case 7 -> 5;    // Corrosion VI
            case 8 -> 6;    // Corrosion VII
            default -> 8;   // Corrosion IX
        };

        int amplifier = ticksCharged / divisor;
        amplifier = Math.min(amplifier, maxAmplifier);

        bullet.getPersistentData().putInt("CausticCharge", chargeLevels);
        bullet.getPersistentData().putInt("CausticAmplifier", amplifier);

        float velocity = 0.5F + (chargeLevels * 0.25F);
        bullet.shoot(lookVec.x, lookVec.y, lookVec.z, velocity, 0.5F);

        player.level().addFreshEntity(bullet);

        float extraScale = 1.0F + 0.5F * (chargeLevels / 25.0F);
        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.send(
                com.sporeadds.sporeaddsmod.network.PacketDistributor.TRACKING_ENTITY.with(() -> bullet),
                new com.sporeadds.sporeaddsmod.network.CausticShotScalePacket(bullet.getId(), extraScale)
        );

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(
                        ResourceLocation.fromNamespaceAndPath("spore", "assassin_bullet_block")
                ),
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }
}