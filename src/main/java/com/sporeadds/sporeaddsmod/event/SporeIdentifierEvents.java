package com.sporeadds.sporeaddsmod.event;

import com.Harbinger.Spore.Sentities.Projectile.GunProjectiles.AssassinBullet;
import com.sporeadds.sporeaddsmod.util.SporeIdentifierUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SporeIdentifierEvents {

    private static final Set<UUID> PLAYERS_TO_SYNC = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PLAYERS_TO_SYNC.add(serverPlayer.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PLAYERS_TO_SYNC.add(serverPlayer.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PLAYERS_TO_SYNC.add(serverPlayer.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        if (event.player instanceof ServerPlayer serverPlayer) {
            UUID uuid = serverPlayer.getUUID();
            if (PLAYERS_TO_SYNC.contains(uuid)) {
                SporeIdentifierUtil.sync(serverPlayer);
                PLAYERS_TO_SYNC.remove(uuid);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity sourceEntity = event.getSource().getDirectEntity();

        if (sourceEntity instanceof AssassinBullet bullet) {
            CompoundTag data = bullet.getPersistentData();

            if (data.contains("CausticCharge")) {
                int chargeLevels = data.getInt("CausticCharge");
                int amplifier = data.getInt("CausticAmplifier");

                int durationSeconds = 10 + chargeLevels;
                int durationTicks = durationSeconds * 20;

                MobEffect corrosion = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS
                        .getValue(new ResourceLocation("spore", "corrosion"));

                if (corrosion != null) {
                    event.getEntity().addEffect(new MobEffectInstance(corrosion, durationTicks, amplifier));
                }

                if (event.getEntity().level() instanceof ServerLevel serverLevel) {
                    double x = event.getEntity().getX();
                    double y = event.getEntity().getY() + event.getEntity().getBbHeight() * 0.5D;
                    double z = event.getEntity().getZ();

                    serverLevel.sendParticles(
                            new DustParticleOptions(new Vector3f(0.0f, 1.0f, 0.0f), 1.0f),
                            x, y, z,
                            25, 0.25D, 0.35D, 0.25D, 0.0D
                    );

                    serverLevel.playSound(
                            null,
                            x, y, z,
                            net.minecraftforge.registries.ForgeRegistries.SOUND_EVENTS.getValue(
                                    new ResourceLocation("spore", "chemist_fuse")
                            ),
                            net.minecraft.sounds.SoundSource.PLAYERS,
                            1.0F,
                            2.0F
                    );
                }
            }
        }
    }
}