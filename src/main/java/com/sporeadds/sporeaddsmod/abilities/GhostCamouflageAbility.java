package com.sporeadds.sporeaddsmod.abilities;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.GhostConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd")
public class GhostCamouflageAbility {

    private GhostCamouflageAbility() {
    }

    private static boolean isCamouflaged(ServerPlayer player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(SporeIdentifierData::isCamouflaged)
                .orElse(false);
    }

    private static ServerPlayer resolveAttacker(LivingHurtEvent event) {
        Entity source = event.getSource().getEntity();

        if (source instanceof ServerPlayer player) {
            return player;
        }

        Entity direct = event.getSource().getDirectEntity();

        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) {
            return player;
        }

        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurtRangedBonus(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }

        ServerPlayer attacker = resolveAttacker(event);

        if (attacker == null) {
            return;
        }

        if (!isCamouflaged(attacker)) {
            return;
        }

        LivingEntity target = event.getEntity();
        double distance = attacker.distanceTo(target);

        if (distance <= GhostConfig.disguiseRangedMinDistance()) {
            return;
        }

        event.setAmount(event.getAmount() * GhostConfig.disguiseRangedDamageMultiplier());
    }
}