package com.sporeadds.sporeaddsmod.client.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Small helpers shared by the SporeAdds config screens (Forge 1.20.1 has no built-in config GUI). */
final class ConfigUi {

    /** One entry of a config section: either a nested section or a value. */
    record Node(String key, List<String> path, Object raw, ForgeConfigSpec.ValueSpec spec) {
        boolean isSection() {
            return raw instanceof UnmodifiableConfig;
        }

        UnmodifiableConfig section() {
            return (UnmodifiableConfig) raw;
        }

        ForgeConfigSpec.ConfigValue<?> value() {
            return (ForgeConfigSpec.ConfigValue<?>) raw;
        }
    }

    static final Logger LOGGER = LogUtils.getLogger();

    // Specs whose values were edited and still have to be written to disk.
    private static final Set<ForgeConfigSpec> DIRTY = new LinkedHashSet<>();

    private ConfigUi() {
    }

    /** The children of one section, in definition order. */
    static List<Node> children(ForgeConfigSpec spec, UnmodifiableConfig values, List<String> prefix) {
        List<Node> out = new ArrayList<>();
        for (Map.Entry<String, Object> e : values.valueMap().entrySet()) {
            List<String> path = new ArrayList<>(prefix);
            path.add(e.getKey());
            Object raw = e.getValue();
            if (raw instanceof ForgeConfigSpec.ConfigValue<?>) {
                Object specObj = spec.getSpec().get(path);
                out.add(new Node(e.getKey(), path, raw, specObj instanceof ForgeConfigSpec.ValueSpec vs ? vs : null));
            } else if (raw instanceof UnmodifiableConfig) {
                out.add(new Node(e.getKey(), path, raw, null));
            }
        }
        return out;
    }

    /** "damage_multiplier" -> "Damage Multiplier"; "GLUTTONOUS_table" -> "Gluttonous Table". */
    static String prettify(String key) {
        StringBuilder out = new StringBuilder(key.length());
        boolean startOfWord = true;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '_' || c == '-' || c == '.') {
                out.append(' ');
                startOfWord = true;
            } else if (startOfWord) {
                out.append(Character.toUpperCase(c));
                startOfWord = false;
            } else {
                out.append(Character.toLowerCase(c));
            }
        }
        return out.toString();
    }

    /** Hover text of a value: the comment written in the config class, its range and its default. */
    static Component tooltip(ForgeConfigSpec.ConfigValue<?> value, ForgeConfigSpec.ValueSpec spec) {
        StringBuilder sb = new StringBuilder();
        String comment = spec == null ? null : spec.getComment();
        if (comment != null && !comment.isBlank()) {
            sb.append(comment.strip());
        }
        // defineInRange already ends the comment with "Range: min ~ max": do not print it twice.
        if (spec != null && (comment == null || !comment.contains("Range:"))) {
            Object range = spec.<Integer>getRange();
            if (range instanceof ForgeConfigSpec.Range<?> r) {
                sb.append("\n\nRange: ").append(r.getMin()).append(" ~ ").append(r.getMax());
            }
        }
        sb.append(sb.length() == 0 ? "" : "\n").append("Default: ").append(describe(value.getDefault()));
        return Component.literal(sb.toString());
    }

    static String describe(Object value) {
        if (value instanceof List<?> list) {
            return list.isEmpty() ? "(empty list)" : list.size() + " entries";
        }
        return String.valueOf(value);
    }

    static void markDirty(ForgeConfigSpec spec) {
        DIRTY.add(spec);
    }

    /** Writes every edited config to disk. Called whenever one of the screens is left. */
    static void flush() {
        for (ForgeConfigSpec spec : DIRTY) {
            if (spec.isLoaded()) {
                try {
                    spec.save();
                } catch (RuntimeException e) {
                    // A disk problem must not close the game from inside a screen.
                    LOGGER.warn("[SporeAdds config] could not save the config file: {}", e.toString());
                }
            }
        }
        DIRTY.clear();
    }
}
