package com.sporeadds.sporeaddsmod.config;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Shared helper for config lists of items: costs, starting gear, loot, ...
 *
 * <p>Entry format used by every item list in the config: {@code modid:item} or {@code modid:item;count}
 * (count defaults to 1). Entries that do not resolve to a registered item (a typo, an optional mod that is not
 * installed) are skipped and reported once in the log; they never crash anything.
 *
 * <p>Parse at USE time (the config and the registries are not ready while the spec is being built).
 */
public final class ConfigItems {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
    /** Syntax only: safe to run while the spec is built, before any registry is filled. */
    private static final Pattern SYNTAX = Pattern.compile("^([a-z0-9_.\\-]+:)?[a-z0-9_./\\-]+(\\s*;\\s*\\d+)?$");

    private ConfigItems() {
    }

    /** One resolved {@code item;count} entry. */
    public record Entry(Item item, int count) {
        public ItemStack stack() {
            return new ItemStack(item, count);
        }
    }

    /** Validator for {@code defineListAllowEmpty}: checks the syntax only, never the registry. */
    public static boolean isValidEntry(Object o) {
        return o instanceof String s && SYNTAX.matcher(s.trim()).matches();
    }

    public static Optional<Entry> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.trim().split(";");
        ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
        if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) {
            warnOnce(raw);
            return Optional.empty();
        }
        int count = 1;
        if (parts.length > 1) {
            try {
                count = Math.max(1, Integer.parseInt(parts[1].trim()));
            } catch (NumberFormatException e) {
                warnOnce(raw);
                return Optional.empty();
            }
        }
        return Optional.of(new Entry(ForgeRegistries.ITEMS.getValue(id), count));
    }

    public static List<Entry> parseAll(List<? extends String> raw) {
        List<Entry> out = new ArrayList<>();
        if (raw != null) {
            for (String s : raw) {
                parse(s).ifPresent(out::add);
            }
        }
        return out;
    }

    /** How many of {@code item} the player carries (main inventory + offhand). */
    public static int count(Player player, Item item) {
        return count(player, item, true);
    }

    /** How many of {@code item} the player carries: the main inventory (hotbar included) and, optionally, the offhand. */
    public static int count(Player player, Item item, boolean includeOffhand) {
        int n = 0;
        for (ItemStack s : player.getInventory().items) {
            if (s.is(item)) {
                n += s.getCount();
            }
        }
        if (includeOffhand) {
            for (ItemStack s : player.getInventory().offhand) {
                if (s.is(item)) {
                    n += s.getCount();
                }
            }
        }
        return n;
    }

    /** True when the player carries every entry (several entries of the same item add up). */
    public static boolean has(Player player, List<Entry> cost) {
        return has(player, cost, true);
    }

    /** Like {@link #has(Player, List)} but the offhand only counts when {@code includeOffhand} is true. */
    public static boolean has(Player player, List<Entry> cost, boolean includeOffhand) {
        for (Entry e : cost) {
            int needed = 0;
            for (Entry other : cost) {
                if (other.item() == e.item()) {
                    needed += other.count();
                }
            }
            if (count(player, e.item(), includeOffhand) < needed) {
                return false;
            }
        }
        return true;
    }

    /** Removes the entries from the player's inventory. Check {@link #has} first. */
    public static void take(Player player, List<Entry> cost) {
        take(player, cost, true);
    }

    /** Like {@link #take(Player, List)} but the offhand is only used when {@code includeOffhand} is true. */
    public static void take(Player player, List<Entry> cost, boolean includeOffhand) {
        for (Entry e : cost) {
            int left = e.count();
            for (ItemStack s : player.getInventory().items) {
                if (left <= 0) {
                    break;
                }
                if (s.is(e.item())) {
                    int t = Math.min(left, s.getCount());
                    s.shrink(t);
                    left -= t;
                }
            }
            if (!includeOffhand) {
                continue;
            }
            for (ItemStack s : player.getInventory().offhand) {
                if (left <= 0) {
                    break;
                }
                if (s.is(e.item())) {
                    int t = Math.min(left, s.getCount());
                    s.shrink(t);
                    left -= t;
                }
            }
        }
    }

    /** Puts the entries into the inventory; whatever does not fit is dropped at the player's feet. */
    public static void give(Player player, List<Entry> items) {
        for (Entry e : items) {
            ItemStack stack = e.stack();
            if (!player.getInventory().add(stack) && !stack.isEmpty()) {
                player.drop(stack, false);
            }
        }
    }

    private static void warnOnce(String raw) {
        if (WARNED.add(raw)) {
            LOGGER.warn("[SporeAdds config] ignoring item entry '{}' (unknown item or bad format; expected modid:item or modid:item;count)", raw);
        }
    }
}
