package com.sporeadds.sporeaddsmod.event;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "sporeadd")
public class KommandantOxygenHandler {

    public static final double OXYGEN_MULTIPLIER = 0.75;
    private static final Map<UUID, Double> serverDebtMap = new ConcurrentHashMap<>();

    private static boolean isKommandant(Player player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            return "kommandant".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getIdentifier());
        }
        return false;
    }

    private static boolean isAbyssal(Player player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            return "abyssal".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        Player player = event.getEntity();

        if (player.level().isClientSide()) return;

        if (!isKommandant(player)) {
            serverDebtMap.remove(player.getUUID());
            return;
        }

        if (isAbyssal(player)) {
            serverDebtMap.remove(player.getUUID());
            player.setAirSupply(player.getMaxAirSupply());
            return;
        }

        boolean underwater = player.isUnderWater() || player.isEyeInFluid(FluidTags.WATER);
        if (!underwater) {
            serverDebtMap.remove(player.getUUID());
            return;
        }

        int currentAir = player.getAirSupply();
        int maxAir = player.getMaxAirSupply();

        if (currentAir >= maxAir || currentAir < -20) {
            serverDebtMap.remove(player.getUUID());
            return;
        }

        UUID id = player.getUUID();
        double debt = serverDebtMap.getOrDefault(id, 0.0);

        if (OXYGEN_MULTIPLIER < 1.0) {
            debt += (1.0 - OXYGEN_MULTIPLIER);
            int toRestore = (int) debt;
            debt -= toRestore;

            if (toRestore > 0) {
                player.setAirSupply(Math.min(maxAir, currentAir + toRestore));
            }

            if (debt == 0.0) serverDebtMap.remove(id);
            else serverDebtMap.put(id, debt);
        } else if (OXYGEN_MULTIPLIER > 1.0) {
            debt += (OXYGEN_MULTIPLIER - 1.0);
            int toSubtract = (int) debt;
            debt -= toSubtract;

            if (toSubtract > 0) {
                player.setAirSupply(Math.max(-20, currentAir - toSubtract));
            }

            if (debt == 0.0) serverDebtMap.remove(id);
            else serverDebtMap.put(id, debt);
        }
    }
}