package com.sporeadds.mixin;

import com.Harbinger.Spore.Sitems.PCI;
import com.Harbinger.Spore.Sevents.HandlerEvents;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HandlerEvents.class, remap = false)
public class HandlerEvents_DefenseBypassMixin {

    // PCI damage per charge, freeze time and cooldown are shared with Poder12 and configurable
    // (kommandant_abilities_advanced.power_12_biomass_armor.pci in the config; defaults 6 / 600 / 20 + 10 per charge).

    @Inject(
            method = "DefenseBypass(Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onDefenseBypassSporePlayers(LivingDamageEvent.Pre event, CallbackInfo ci) {
        LivingEntity target = event.getEntity();

        boolean isSporePlayer = (
                target instanceof Player p &&
                        p.getTeam() != null &&
                        "spore".equalsIgnoreCase(p.getTeam().getName())
        );

        if (!isSporePlayer) return;
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;

        ItemStack weapon = attacker.getMainHandItem();
        if (!(weapon.getItem() instanceof PCI pci)) return;
        if (pci.getCharge(weapon) <= 0) return;
        if (attacker.getCooldowns().isOnCooldown(pci)) return;

        // Si todavía tiene armadura del Poder12, NO hacer nada aquí.
        // Poder12 ya lo resuelve en LivingDamageEvent.Pre.
        if (target instanceof ServerPlayer serverTarget) {
            var opt = PlayerDataProvider.PLAYER_DATA.get(serverTarget).resolve();
            if (opt.isPresent() && opt.get().getArmorHp() > 0) {
                return;
            }
        }

        int charge = pci.getCharge(weapon);
        float maxHealth = target.getMaxHealth();
        float maxAllowedDamage = maxHealth / 8.0F; // basado en vida máxima actual

        int damagePerCharge = com.sporeadds.sporeaddsmod.config.KommandantAdvancedConfig.ARMOR_PCI_DAMAGE_PER_CHARGE.get();
        int chargesToUse = (int) Math.floor(maxAllowedDamage / damagePerCharge);
        if (chargesToUse <= 0 && maxHealth > 0.0F) {
            chargesToUse = 1;
        }

        chargesToUse = Math.min(chargesToUse, charge);

        float finalDamage = chargesToUse * damagePerCharge;
        finalDamage = Math.min(finalDamage, maxAllowedDamage);

        event.setNewDamage(finalDamage);
        pci.setCharge(weapon, charge - chargesToUse);

        target.setTicksFrozen(com.sporeadds.sporeaddsmod.config.KommandantAdvancedConfig.ARMOR_PCI_FREEZE_TICKS.get());
        attacker.getCooldowns().addCooldown(pci, Math.max(
                com.sporeadds.sporeaddsmod.config.KommandantAdvancedConfig.ARMOR_PCI_MIN_COOLDOWN.get(),
                chargesToUse * com.sporeadds.sporeaddsmod.config.KommandantAdvancedConfig.ARMOR_PCI_COOLDOWN_PER_CHARGE.get()));
        pci.playSound(attacker);

        ci.cancel();
    }
}