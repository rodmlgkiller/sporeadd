package com.sporeadds.sporeaddsmod.event;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class AbyssalRiptideDamageHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        var subjugation = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "subjugation"));
        if (subjugation == null || !player.hasEffect(subjugation)) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty() || !mainHand.hasTag() || !mainHand.getTag().getBoolean("AbyssalTempTrident")) return;

        if (!player.isAutoSpinAttack()) return;

        event.setAmount(event.getAmount() + 30.0F);
    }
}