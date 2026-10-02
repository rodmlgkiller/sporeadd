package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CausticCombatHelper {

    public static boolean isCaustic(ServerPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String subclass = data.getSubclass();
                    return subclass != null && subclass.trim().equalsIgnoreCase("caustic");
                })
                .orElse(false);
    }

    public static boolean hasActiveArmor(ServerPlayer player) {
        return PlayerDataProvider.PLAYER_DATA.get(player)
                .map(data -> data.getArmorHp() > 0)
                .orElse(false);
    }

    public static ItemStack getDamageableMeleeWeaponFromSource(DamageSource source) {
        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return ItemStack.EMPTY;
        }

        if (attacker != directEntity) {
            return ItemStack.EMPTY;
        }

        ItemStack weapon = livingAttacker.getMainHandItem();
        if (weapon.isEmpty() || !weapon.isDamageableItem()) {
            return ItemStack.EMPTY;
        }

        return weapon;
    }

    public static boolean shouldUseCausticReaction(ServerPlayer player, DamageSource source) {
        if (!hasActiveArmor(player)) return false;
        if (!isCaustic(player)) return false;
        return !getDamageableMeleeWeaponFromSource(source).isEmpty();
    }
}