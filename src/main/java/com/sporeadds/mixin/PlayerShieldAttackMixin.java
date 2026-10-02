package com.sporeadds.mixin;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerShieldAttackMixin {

    private static final ResourceLocation FAMINED_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "famined");

    @Redirect(
            method = "blockUsingShield(Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;canDisableShield(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"
            )
    )
    private boolean sporeadd$preventFaminedShieldDisable(ItemStack weaponStack, ItemStack shieldStack, LivingEntity defender, LivingEntity attacker) {
        MobEffect famined = BuiltInRegistries.MOB_EFFECT.get(FAMINED_ID);

        if (famined != null && attacker instanceof Player player && player.hasEffect(famined)) {
            return false;
        }

        return weaponStack.canDisableShield(shieldStack, defender, attacker);
    }
}