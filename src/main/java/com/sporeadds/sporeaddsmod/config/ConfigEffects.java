package com.sporeadds.sporeaddsmod.config;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Shared helper for config lists of potion effects applied by abilities.
 *
 * <p>Entry format used by every effect list in the config: {@code modid:effect;durationTicks;amplifier}.
 * {@code durationTicks} defaults to 200 (10 s) and {@code amplifier} to 0 (level I) when omitted. Entries that do not
 * resolve to a registered effect are skipped and reported once in the log.
 *
 * <p>Parse at USE time (the config and the registries are not ready while the spec is being built).
 */
public final class ConfigEffects {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
    /** Syntax only: safe to run while the spec is built, before any registry is filled. */
    private static final Pattern SYNTAX = Pattern.compile("^([a-z0-9_.\\-]+:)?[a-z0-9_./\\-]+(\\s*;\\s*\\d+){0,2}$");

    private ConfigEffects() {
    }

    /** One resolved effect entry (1.21 effects are registry holders). */
    public record Entry(Holder<MobEffect> effect, int durationTicks, int amplifier) {
        public MobEffectInstance instance() {
            return new MobEffectInstance(effect, durationTicks, amplifier);
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
        Optional<Holder.Reference<MobEffect>> effect = id == null ? Optional.empty() : BuiltInRegistries.MOB_EFFECT.getHolder(id);
        if (effect.isEmpty()) {
            warnOnce(raw);
            return Optional.empty();
        }
        try {
            int duration = parts.length > 1 ? Math.max(1, Integer.parseInt(parts[1].trim())) : 200;
            int amplifier = parts.length > 2 ? Math.max(0, Integer.parseInt(parts[2].trim())) : 0;
            return Optional.of(new Entry(effect.get(), duration, amplifier));
        } catch (NumberFormatException e) {
            warnOnce(raw);
            return Optional.empty();
        }
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

    /** Applies every entry to {@code target} (server side). */
    public static void apply(LivingEntity target, List<Entry> effects) {
        for (Entry e : effects) {
            target.addEffect(e.instance());
        }
    }

    private static void warnOnce(String raw) {
        if (WARNED.add(raw)) {
            LOGGER.warn("[SporeAdds config] ignoring effect entry '{}' (unknown effect or bad format; expected modid:effect;durationTicks;amplifier)", raw);
        }
    }
}
