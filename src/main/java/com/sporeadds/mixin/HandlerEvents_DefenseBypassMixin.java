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

    private static final int PCI_DAMAGE_PER_CHARGE = 6; // 3 * 2

    @Inject(
            method = "DefenseBypass(Lnet/minecraftforge/event/entity/living/LivingDamageEvent;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onDefenseBypassSporePlayers(LivingDamageEvent event, CallbackInfo ci) {
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
        // Poder12 ya lo resuelve en LivingHurtEvent.
        if (target instanceof ServerPlayer serverTarget) {
            var opt = PlayerDataProvider.PLAYER_DATA.get(serverTarget).resolve();
            if (opt.isPresent() && opt.get().getArmorHp() > 0) {
                return;
            }
        }

        int charge = pci.getCharge(weapon);
        float maxHealth = target.getMaxHealth();
        float maxAllowedDamage = maxHealth / 8.0F; // basado en vida máxima actual

        int chargesToUse = (int) Math.floor(maxAllowedDamage / PCI_DAMAGE_PER_CHARGE);
        if (chargesToUse <= 0 && maxHealth > 0.0F) {
            chargesToUse = 1;
        }

        chargesToUse = Math.min(chargesToUse, charge);

        float finalDamage = chargesToUse * PCI_DAMAGE_PER_CHARGE;
        finalDamage = Math.min(finalDamage, maxAllowedDamage);

        event.setAmount(finalDamage);
        pci.setCharge(weapon, charge - chargesToUse);

        target.setTicksFrozen(600);
        attacker.getCooldowns().addCooldown(pci, Math.max(20, chargesToUse * 10));
        pci.playSound(attacker);

        ci.cancel();
    }
}