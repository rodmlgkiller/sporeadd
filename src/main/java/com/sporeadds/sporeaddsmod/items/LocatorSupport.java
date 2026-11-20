package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.config.ConfigItems;
import com.sporeadds.sporeaddsmod.config.LocatorConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Cost, cooldown and tooltip helpers shared by the three locators. Everything is driven by {@link LocatorConfig}.
 *
 * <p>The tooltip helpers keep using the original translated lines while the configuration still holds the original
 * numbers, so a default install looks exactly as before in every language; they only fall back to generated English
 * text when a server owner changed the cost or the cooldown.
 */
final class LocatorSupport {
    private LocatorSupport() {
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Cost
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Checks the locator's cost and, when {@code consume} is true, takes it from the player. An empty cost is free.
     *
     * @param legacyKey translation key of the original "not enough redstone" message (it takes the amount as its
     *                  only argument); may be null
     * @return true when the locator may fire; false (after telling the player) when the cost is not carried
     */
    static boolean tryPay(ServerPlayer player, List<ConfigItems.Entry> cost, boolean consume, String legacyKey) {
        if (cost.isEmpty()) {
            return true;
        }
        // Main inventory only, like the original locators (the offhand never counted).
        if (!ConfigItems.has(player, cost, false)) {
            player.displayClientMessage(missingMessage(cost, legacyKey), true);
            return false;
        }
        if (consume) {
            ConfigItems.take(player, cost, false);
        }
        return true;
    }

    private static Component missingMessage(List<ConfigItems.Entry> cost, String legacyKey) {
        if (legacyKey != null && cost.size() == 1 && cost.get(0).item() == Items.REDSTONE_BLOCK) {
            return Component.translatable(legacyKey, cost.get(0).count()).withStyle(ChatFormatting.RED);
        }
        return Component.literal("Not enough items! Needed: ").append(describe(cost)).withStyle(ChatFormatting.RED);
    }

    /** "30x Block of Redstone, 2x Diamond". */
    static Component describe(List<ConfigItems.Entry> cost) {
        MutableComponent out = Component.empty();
        for (int i = 0; i < cost.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            ConfigItems.Entry entry = cost.get(i);
            out.append(entry.count() + "x ").append(entry.item().getDescription());
        }
        return out;
    }

    /**
     * Tooltip line for the cost. While the cost is still the original one ({@code legacyCount} x {@code legacyItem},
     * consumed) the original translated line {@code legacyKey} is returned.
     */
    static Component costLine(List<ConfigItems.Entry> cost, boolean consume, String legacyKey, Item legacyItem, int legacyCount) {
        if (cost.isEmpty()) {
            return Component.literal("Free to use").withStyle(ChatFormatting.GREEN);
        }
        if (consume && legacyKey != null && cost.size() == 1
                && cost.get(0).item() == legacyItem && cost.get(0).count() == legacyCount) {
            return Component.translatable(legacyKey).withStyle(ChatFormatting.RED);
        }
        MutableComponent line = Component.literal(consume ? "Consumes: " : "Requires (not consumed): ").append(describe(cost));
        if (consume) {
            line.append(" from inventory");
        }
        return line.withStyle(ChatFormatting.RED);
    }

    /** Tooltip line for a cost that has no original translated line (the Proto Locator, which used to be free). */
    static Component customCostLine(List<ConfigItems.Entry> cost, boolean consume) {
        return costLine(cost, consume, null, Items.AIR, 0);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Cooldown / durations
    // ---------------------------------------------------------------------------------------------------------------

    /** "Cooldown: 30 s". */
    static Component cooldownLine(int cooldownTicks) {
        return Component.literal("Cooldown: " + formatTicks(cooldownTicks)).withStyle(ChatFormatting.RED);
    }

    /** Compact duration: 36000 -> "30 min", 100 -> "5 s", 4300 -> "3 min 35 s". */
    static String formatTicks(long ticks) {
        long totalSeconds = Math.max(0L, (ticks + 19L) / 20L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        StringBuilder sb = new StringBuilder();
        if (hours > 0L) {
            sb.append(hours).append(" h ");
        }
        if (minutes > 0L) {
            sb.append(minutes).append(" min ");
        }
        if (seconds > 0L || sb.length() == 0) {
            sb.append(seconds).append(" s ");
        }
        return sb.toString().trim();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Proto Locator texts (its cooldown runs on the wall clock and is stored in the item)
    // ---------------------------------------------------------------------------------------------------------------

    /** Action-bar message while the Proto Locator is cooling down. */
    static Component protoCooldownMessage(long timeLeftMillis, long cooldownMillis) {
        if (cooldownMillis >= 60_000L) {
            int minutesLeft = (int) (timeLeftMillis / (60 * 1000)) + 1;
            return Component.translatable("message.sporeadds.proto_locator.cooldown", minutesLeft)
                    .withStyle(ChatFormatting.RED);
        }
        long secondsLeft = timeLeftMillis / 1000L + 1L;
        return Component.literal("Proto Locator on cooldown! " + secondsLeft + " seconds remaining")
                .withStyle(ChatFormatting.RED);
    }

    /** Tooltip line while the Proto Locator is cooling down. */
    static Component protoCooldownTooltip(long timeLeftMillis, long cooldownMillis) {
        if (cooldownMillis >= 60_000L) {
            int minutesLeft = (int) (timeLeftMillis / (60 * 1000)) + 1;
            return Component.translatable("tooltip.sporeadds.proto_locator.cooldown", minutesLeft)
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        }
        long secondsLeft = timeLeftMillis / 1000L + 1L;
        return Component.literal("Cooldown: " + secondsLeft + " seconds")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    }

    /** "Ready again in ..." message shown after a scan; null when the Proto Locator has no cooldown. */
    static Component protoReadyAgain(int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return null;
        }
        if (cooldownTicks == LocatorConfig.DEFAULT_PROTO_COOLDOWN_TICKS) {
            return Component.translatable("message.sporeadds.proto_locator.ready_in_30").withStyle(ChatFormatting.GRAY);
        }
        return Component.literal("Proto Locator will be ready again in " + formatTicks(cooldownTicks) + ".")
                .withStyle(ChatFormatting.GRAY);
    }

    /** Tooltip line "Marks targets for: ..."; the original translated line while the duration is the original. */
    static Component protoMarkedLine(int markedTicks) {
        if (markedTicks == LocatorConfig.DEFAULT_PROTO_MARKED_DURATION_TICKS) {
            return Component.translatable("tooltip.sporeadds.proto_locator.duration").withStyle(ChatFormatting.GREEN);
        }
        return Component.literal("Marks targets for: " + formatTicks(markedTicks)).withStyle(ChatFormatting.GREEN);
    }

    /** Tooltip line "Drawback: ... Cooldown"; null when the Proto Locator has no cooldown. */
    static Component protoDrawbackLine(int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return null;
        }
        if (cooldownTicks == LocatorConfig.DEFAULT_PROTO_COOLDOWN_TICKS) {
            return Component.translatable("tooltip.sporeadds.proto_locator.cost").withStyle(ChatFormatting.RED);
        }
        return Component.literal("Drawback: " + formatTicks(cooldownTicks) + " Cooldown").withStyle(ChatFormatting.RED);
    }
}
