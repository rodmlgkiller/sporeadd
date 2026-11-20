package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Server-side gate for the Kommandant abilities: ability N may only be used once the player's evolution level has reached
 * {@link KommandantPowersConfig#unlockLevel(int)} (default N - 4, configurable in kommandant_abilities.unlock_levels).
 * The ability selector and the key handler use the same numbers on the client; this is what the server enforces.
 */
public final class PowerUnlocks {

    private PowerUnlocks() {
    }

    /** The player's evolution level (0 when the capability is missing). */
    public static int levelOf(Player player) {
        return PlayerLevelProvider.PLAYER_LVL.get(player)
                .map(levelCap -> levelCap.getLevel())
                .orElse(0);
    }

    /** True when the player has reached the unlock level of ability {@code power}. */
    public static boolean isUnlocked(Player player, int power) {
        return KommandantPowersConfig.isUnlocked(levelOf(player), power);
    }

    /** Like {@link #isUnlocked} but tells the player why when it is not (for abilities used by an explicit action). */
    public static boolean checkOrWarn(ServerPlayer player, int power) {
        if (isUnlocked(player, power)) {
            return true;
        }
        player.sendSystemMessage(Component.translatableWithFallback(
                "message.sporeadd.power.locked",
                "This ability unlocks at evolution level %s.",
                KommandantPowersConfig.unlockLevel(power)));
        return false;
    }
}
