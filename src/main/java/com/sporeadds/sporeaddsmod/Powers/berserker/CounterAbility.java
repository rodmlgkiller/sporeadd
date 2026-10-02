package com.sporeadds.sporeaddsmod.Powers.berserker;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncBerserkerCooldownPacket;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Berserker - "Counter".
 *
 * Al activarse: partículas y sonido alrededor del jugador. Durante 0.5 s (o hasta el primer
 * golpe de una entidad, lo que ocurra antes) el siguiente ataque se anula por completo:
 *  - cuerpo a cuerpo: devuelve el 50% del daño al atacante.
 *  - proyectil: lo redirige de vuelta al que lo disparó a la misma rapidez.
 * Si expira sin golpe -> falla, cooldown de 5 s. Si repele algo -> cooldown = daño/4 segundos.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class CounterAbility {

    private static final int WINDOW_TICKS = 10;               // 0.5 s
    private static final int FAIL_COOLDOWN_TICKS = 100;       // 5 s
    private static final int MIN_SUCCESS_COOLDOWN_TICKS = 20; // 1 s
    private static final float MELEE_REFLECT = 0.5F;

    private static final Map<UUID, Long> WINDOW_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN_UNTIL = new HashMap<>();

    private CounterAbility() {
    }

    public static void tryActivate(ServerPlayer player) {
        if (!SporeClassUtil.hasClass(player, "berserker")) return;

        UUID id = player.getUUID();
        long now = player.level().getGameTime();

        if (WINDOW_UNTIL.containsKey(id)) return;
        Long cd = COOLDOWN_UNTIL.get(id);
        if (cd != null && now < cd) return;

        WINDOW_UNTIL.put(id, now + WINDOW_TICKS);
        activationFx(player);
        player.displayClientMessage(
                Component.translatable("message.sporeadd.berserker.counter.activated").withStyle(ChatFormatting.GOLD), true);
    }

    public static void forget(UUID id) {
        WINDOW_UNTIL.remove(id);
        COOLDOWN_UNTIL.remove(id);
    }

    // ------------------------------------------------------------------ hits

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isWindowActive(player)) return;

        DamageSource src = event.getSource();
        if (src.getDirectEntity() instanceof Projectile) return;         // lo gestiona onProjectileImpact
        if (!(src.getEntity() instanceof LivingEntity attacker)) return; // solo ataques de entidad

        event.setCanceled(true);

        float amount = Math.max(0.0F, event.getAmount());
        attacker.hurt(player.damageSources().thorns(player), amount * MELEE_REFLECT);
        onCounterSuccess(player, amount);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof ServerPlayer player)) return;
        if (!isWindowActive(player)) return;

        Projectile proj = event.getProjectile();
        event.setCanceled(true);   // el proyectil atraviesa sin dañar

        Entity shooter = proj.getOwner();

        Vec3 vel = proj.getDeltaMovement();
        double speed = vel.length();
        if (speed < 0.05D) speed = 0.05D;

        Vec3 dir = shooter != null ? shooter.getEyePosition().subtract(proj.position()) : vel.reverse();
        if (dir.lengthSqr() < 1.0E-4D) dir = vel.reverse();
        Vec3 back = dir.normalize().scale(speed);

        proj.setDeltaMovement(back);
        proj.hasImpulse = true;
        faceMotion(proj, back);
        proj.setOwner(player);
        if (proj instanceof AbstractArrow arrow) {
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        }

        onCounterSuccess(player, estimateProjectileDamage(proj, speed));
    }

    private static void onCounterSuccess(ServerPlayer player, float hitDamage) {
        UUID id = player.getUUID();
        WINDOW_UNTIL.remove(id);

        long now = player.level().getGameTime();
        int cdTicks = Math.max(MIN_SUCCESS_COOLDOWN_TICKS, Mth.ceil(hitDamage / 4.0F) * 20);
        COOLDOWN_UNTIL.put(id, now + cdTicks);
        sendCooldown(player, cdTicks);

        repelFx(player);
        player.displayClientMessage(
                Component.translatable("message.sporeadd.berserker.counter.repelled").withStyle(ChatFormatting.GOLD), true);
    }

    // ------------------------------------------------------------------ window expiry

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (WINDOW_UNTIL.isEmpty()) return;
        if (event.getServer() == null) return;

        Iterator<Map.Entry<UUID, Long>> it = WINDOW_UNTIL.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                it.remove();
                continue;
            }
            if (player.level().getGameTime() > entry.getValue()) {
                it.remove();
                COOLDOWN_UNTIL.put(entry.getKey(), player.level().getGameTime() + FAIL_COOLDOWN_TICKS);
                sendCooldown(player, FAIL_COOLDOWN_TICKS);
                player.displayClientMessage(
                        Component.translatable("message.sporeadd.berserker.counter.failed").withStyle(ChatFormatting.GRAY), true);
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    private static boolean isWindowActive(ServerPlayer player) {
        Long until = WINDOW_UNTIL.get(player.getUUID());
        return until != null && player.level().getGameTime() <= until;
    }

    private static void sendCooldown(ServerPlayer player, int ticks) {
        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncBerserkerCooldownPacket(com.sporeadds.sporeaddsmod.client.BerserkerCooldownClientState.COUNTER, ticks)
        );
    }

    private static void faceMotion(Entity entity, Vec3 v) {
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
        entity.setYRot((float) (Mth.atan2(v.x, v.z) * (180.0D / Math.PI)));
        entity.setXRot((float) (Mth.atan2(v.y, horiz) * (180.0D / Math.PI)));
        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();
    }

    private static float estimateProjectileDamage(Projectile proj, double speed) {
        if (proj instanceof AbstractArrow arrow) {
            return (float) Math.max(1.0D, Math.ceil(speed * Math.max(1.0D, arrow.getBaseDamage())));
        }
        if (proj instanceof ThrownTrident) {
            return 8.0F;
        }
        return 4.0F;
    }

    private static void activationFx(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        double x = player.getX(), y = player.getY() + player.getBbHeight() * 0.5D, z = player.getZ();

        for (int i = 0; i < 36; i++) {
            double a = (Math.PI * 2.0D) * i / 36.0D;
            level.sendParticles(ParticleTypes.CRIT,
                    x + Math.cos(a) * 0.9D, player.getY() + 0.1D, z + Math.sin(a) * 0.9D, 1, 0, 0, 0, 0);
        }
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, x, y, z, 25, 0.5D, 0.7D, 0.5D, 0.05D);
        level.playSound(null, x, y, z, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9F, 1.5F);
        level.playSound(null, x, y, z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5F, 1.7F);
    }

    private static void repelFx(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        double x = player.getX(), y = player.getY() + player.getBbHeight() * 0.5D, z = player.getZ();

        level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 4, 0.4D, 0.3D, 0.4D, 0.0D);
        level.sendParticles(ParticleTypes.CRIT, x, y, z, 40, 0.6D, 0.6D, 0.6D, 0.25D);
        level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.9F);
        level.playSound(null, x, y, z, SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.7F, 1.2F);
    }
}
