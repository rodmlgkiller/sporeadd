package com.sporeadds.sporeaddsmod.effects;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncgluttonousCrosshairRenderPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FaminedEffect extends MobEffect {

    private static final int gluttonous_ARMOR_CAP = 150;
    private static final int RESISTANCE_DURATION_TICKS = 400;

    private static final Set<UUID> APPLIED_THIS_INSTANCE = new HashSet<>();

    public FaminedEffect() {
        super(MobEffectCategory.HARMFUL, 0x5C4A3D);
    }

    @Override
    public void fillEffectCures(java.util.Set<net.neoforged.neoforge.common.EffectCure> cures, net.minecraft.world.effect.MobEffectInstance effectInstance) {
        cures.remove(net.neoforged.neoforge.common.EffectCures.MILK);
        cures.remove(net.neoforged.neoforge.common.EffectCures.HONEY_BOTTLE);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayer player)) {
            return true;
        }

        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        boolean isgluttonous = cap.isPresent()
                && "gluttonous".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());

        if (!isgluttonous) {
            return true;
        }

        UUID playerId = player.getUUID();

        if (!player.hasEffect(this)) {
            APPLIED_THIS_INSTANCE.remove(playerId);
            return true;
        }

        if (!APPLIED_THIS_INSTANCE.contains(playerId)) {
            PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
                int armorHp = data.getArmorHp();

                if (armorHp > gluttonous_ARMOR_CAP) {
                    int drainedAmount = armorHp - gluttonous_ARMOR_CAP;
                    data.setArmorHpAndSync(gluttonous_ARMOR_CAP, player);

                    int resistanceAmplifier = Math.max(0, (drainedAmount / 100) - 1);
                    resistanceAmplifier = Math.min(2, resistanceAmplifier);

                    if (drainedAmount >= 100) {
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_RESISTANCE,
                                RESISTANCE_DURATION_TICKS,
                                resistanceAmplifier,
                                false,
                                true,
                                true
                        ));
                    }
                }
            });

            APPLIED_THIS_INSTANCE.add(playerId);
        }

        if (player.tickCount % 10 == 0) {
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.NEAR.with(() ->
                            new PacketDistributor.TargetPoint(
                                    player.getX(),
                                    player.getY(),
                                    player.getZ(),
                                    32.0D,
                                    player.level().dimension()
                            )),
                    new SyncgluttonousCrosshairRenderPacket(player.getId())
            );

            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncgluttonousCrosshairRenderPacket(player.getId())
            );
        }

        MobEffectInstance current = player.getEffect(this);
        if (current == null || current.getDuration() <= 1) {
            APPLIED_THIS_INSTANCE.remove(playerId);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}