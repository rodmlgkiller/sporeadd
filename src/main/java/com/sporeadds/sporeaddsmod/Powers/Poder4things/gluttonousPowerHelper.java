package com.sporeadds.sporeaddsmod.Powers.Poder4things;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class gluttonousPowerHelper {

    private gluttonousPowerHelper() {
    }

    public static boolean isSubclassgluttonous(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "gluttonous".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);
    }

    public static boolean isPower4Enabled(Player player) {
        return player.getCapability(PlayerDataProvider.PLAYER_DATA)
                .map(data -> data.getSwitch().length() > 4 && data.getSwitch().charAt(4) == '1')
                .orElse(false);
    }

    public static boolean isBlockedByPower2(Player player, LivingEntity target) {
        return player.getCapability(PlayerDataProvider.PLAYER_DATA)
                .map(data -> data.getSwitch().length() > 2
                        && data.getSwitch().charAt(2) == '1'
                        && target.getHealth() < 5.0f)
                .orElse(false);
    }
}