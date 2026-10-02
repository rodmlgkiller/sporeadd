package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public class AbyssalRiptideDamageHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        var subjugation = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "subjugation")).orElse(null);
        if (subjugation == null || !player.hasEffect(subjugation)) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty() || !ItemNbt.hasTag(mainHand) || !ItemNbt.getTag(mainHand).getBoolean("AbyssalTempTrident")) return;

        if (!player.isAutoSpinAttack()) return;

        event.setNewDamage(event.getNewDamage() + 30.0F);
    }
}