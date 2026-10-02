package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.items.MutagenicCompoundItem;
import com.sporeadds.sporeaddsmod.items.MutagenicCompoundVariant;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd")
public class KommandantCompoundDropHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        LivingEntity target = event.getEntity();

        if (event.getSource().getEntity() == null) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (target == player) {
            return;
        }

        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            if (!"kommandant".equalsIgnoreCase(data.getIdentifier())) {
                return;
            }

            double chance = SporeAddsConfig.KOMMANDANT_MUTAGENIC_COMPOUND_DROP_CHANCE.get();
            chance = Math.max(0.0D, Math.min(1.0D, chance));

            if (target.level().random.nextDouble() >= chance) {
                return;
            }

            MutagenicCompoundVariant[] variants = MutagenicCompoundVariant.values();
            MutagenicCompoundVariant randomVariant =
                    variants[target.level().random.nextInt(variants.length)];

            ItemStack drop = new ItemStack(ModItems.MUTAGENIC_COMPOUND.get());
            MutagenicCompoundItem.setVariant(drop, randomVariant);

            target.spawnAtLocation(drop);
        });
    }
}