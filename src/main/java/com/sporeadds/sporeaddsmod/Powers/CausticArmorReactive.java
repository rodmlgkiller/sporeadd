package com.sporeadds.sporeaddsmod.Powers;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber
public class CausticArmorReactive {

    private static final DustParticleOptions DUST_VERDE = new DustParticleOptions(
            new net.minecraft.world.phys.Vec3(0.2F, 1.0F, 0.2F).toVector3f(), 0.8F
    );

    private static boolean isCaustic(ServerPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String subclass = data.getSubclass();
                    return subclass != null && subclass.trim().equalsIgnoreCase("caustic");
                })
                .orElse(false);
    }

    @SubscribeEvent
    public static void onPlayerAttacked(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (event.getSource().equals(player.level().damageSources().generic())) return;

        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            if (data.getArmorHp() <= 0) return;
            if (!isCaustic(player)) return;

            Entity attacker = event.getSource().getEntity();
            Entity directEntity = event.getSource().getDirectEntity();

            if (!(attacker instanceof LivingEntity livingAttacker)) return;
            if (attacker != directEntity) return;

            ItemStack weapon = livingAttacker.getMainHandItem();
            if (weapon.isEmpty() || !weapon.isDamageableItem()) return;

            ServerLevel serverLevel = (ServerLevel) player.level();

            for (int i = 0; i < 80; i++) {
                double rx = (player.getRandom().nextDouble() - 0.5) * 1.2;
                double ry = player.getRandom().nextDouble() * 1.5;
                double rz = (player.getRandom().nextDouble() - 0.5) * 1.2;
                serverLevel.sendParticles(
                        DUST_VERDE,
                        player.getX() + rx,
                        player.getY() + 1.0D + ry,
                        player.getZ() + rz,
                        1,
                        0, 0, 0, 0
                );
            }

            int maxDurability = weapon.getMaxDamage();
            int damageToApply = Math.max(1, (int) Math.ceil(maxDurability * 0.03D));
            int newDamage = weapon.getDamageValue() + damageToApply;

            if (newDamage >= maxDurability) {
                weapon.shrink(1);
            } else {
                weapon.setDamageValue(newDamage);
            }

            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "chemist_fuse")),
                    SoundSource.PLAYERS,
                    1.0F,
                    2.0F
            );
        });
    }
}