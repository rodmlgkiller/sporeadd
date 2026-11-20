package com.sporeadds.sporeaddsmod.event;

import com.mojang.logging.LogUtils;
import com.sporeadds.sporeaddsmod.capabilities.PlayerImplantsCapability;
import com.sporeadds.sporeaddsmod.config.ImplantConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies the attribute modifiers configured in {@link ImplantConfig} ({@code implant_stats.<implant>.attribute_modifiers})
 * to a player according to the implants they have installed.
 *
 * <p>Every modifier is TRANSIENT (never saved with the player, so nothing can survive uninstalling the mod or a crash)
 * and its name starts with {@link #NAME_PREFIX}; its id is derived from the implant type and the position in the list.
 * {@link #sync} is a full reconcile: it compares what the player carries with what the config + the installed implants
 * ask for, removes our modifiers that are no longer wanted (entry deleted, amount or operation edited, implant removed,
 * built-in config reloaded) and adds the missing ones. It is called about once per second per player and whenever the
 * implants GUI closes, so a config reload is picked up within a second without leaving anything behind.
 */
public final class ImplantAttributeSync {
    /** Every modifier added here carries this name prefix: that is how leftovers are recognised. */
    private static final String NAME_PREFIX = "sporeadd_implant/";

    /** Players that may carry modifiers of ours. Only an optimisation: nothing wanted and nothing touched = nothing to scan. */
    private static final Set<UUID> TOUCHED = ConcurrentHashMap.newKeySet();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean failureLogged;

    private ImplantAttributeSync() {
    }

    /**
     * Reconciles the player's implant attribute modifiers with the config. Never throws: this runs from the player tick,
     * and a misbehaving attribute (e.g. from another mod) must not be able to crash the server.
     */
    public static void sync(ServerPlayer player, PlayerImplantsCapability.IPlayerImplants implants) {
        try {
            reconcile(player, implants);
        } catch (RuntimeException e) {
            if (!failureLogged) {
                failureLogged = true;
                LOGGER.error("[SporeAdds] could not apply the implant attribute modifiers (further errors are not logged)", e);
            }
        }
    }

    private static void reconcile(ServerPlayer player, PlayerImplantsCapability.IPlayerImplants implants) {
        Map<Attribute, List<AttributeModifier>> wanted = new HashMap<>();
        for (PlayerImplantsCapability.ImplantType type : PlayerImplantsCapability.ImplantType.values()) {
            if (implants.getImplant(type).isEmpty()) {
                continue;
            }
            String typeId = type.name().toLowerCase(Locale.ROOT);
            List<ImplantConfig.ModifierSpec> specs = ImplantConfig.attributeModifiers(typeId);
            for (int i = 0; i < specs.size(); i++) {
                ImplantConfig.ModifierSpec spec = specs.get(i);
                String name = NAME_PREFIX + typeId + "/" + i;
                UUID id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
                wanted.computeIfAbsent(spec.attribute(), key -> new ArrayList<>())
                        .add(new AttributeModifier(id, name, spec.amount(), spec.operation()));
            }
        }

        UUID playerId = player.getUUID();
        if (wanted.isEmpty() && !TOUCHED.contains(playerId)) {
            return;
        }

        boolean changed = false;
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            List<AttributeModifier> want = wanted.getOrDefault(attribute, List.of());

            // Remove our modifiers that are no longer wanted (or whose amount / operation changed).
            for (AttributeModifier existing : new ArrayList<>(instance.getModifiers())) {
                String existingName = existing.getName();
                if (existingName != null && existingName.startsWith(NAME_PREFIX) && !isWanted(want, existing)) {
                    instance.removeModifier(existing.getId());
                    changed = true;
                }
            }
            // Add the ones that are missing.
            for (AttributeModifier modifier : want) {
                if (instance.getModifier(modifier.getId()) == null) {
                    instance.addTransientModifier(modifier);
                    changed = true;
                }
            }
        }

        if (wanted.isEmpty()) {
            TOUCHED.remove(playerId);
        } else {
            TOUCHED.add(playerId);
        }

        // A removed max-health modifier must not leave the player with more health than their maximum.
        if (changed && player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    /** Forgets a player that left (the entity, and with it every transient modifier, is gone). */
    public static void forget(UUID playerId) {
        TOUCHED.remove(playerId);
    }

    private static boolean isWanted(List<AttributeModifier> wanted, AttributeModifier existing) {
        for (AttributeModifier candidate : wanted) {
            if (candidate.getId().equals(existing.getId())
                    && candidate.getAmount() == existing.getAmount()
                    && candidate.getOperation() == existing.getOperation()) {
                return true;
            }
        }
        return false;
    }
}
