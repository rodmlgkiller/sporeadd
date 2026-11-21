package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server side of the rebindable skill clicks: what each connected player has bound every {@link SkillClick} to (a mode per
 * skill, reported by the client). A player that never reported anything (vanilla-like client, first ticks after joining)
 * has the defaults, which behave exactly like the code always did.
 */
@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public final class SkillBindings {
    private static final Map<UUID, byte[]> MODES = new ConcurrentHashMap<>();

    private SkillBindings() {
    }

    public static void set(UUID player, byte[] modes) {
        if (modes == null || modes.length != SkillClick.COUNT) {
            return;
        }
        byte[] clean = new byte[SkillClick.COUNT];
        for (int i = 0; i < clean.length; i++) {
            byte m = modes[i];
            clean[i] = (m == SkillClick.MODE_USE || m == SkillClick.MODE_USE_SNEAK || m == SkillClick.MODE_KEY)
                    ? m : SkillClick.byOrdinal(i).defaultMode();
        }
        MODES.put(player, clean);
    }

    public static byte mode(Player player, SkillClick skill) {
        byte[] modes = MODES.get(player.getUUID());
        return modes == null ? skill.defaultMode() : modes[skill.ordinal()];
    }

    /**
     * true when a plain Use click (right click) may trigger {@code skill} now: it is bound to the Use button, and for the
     * sneak variant the player has to be crouching. A skill with its own key never reacts to the Use click.
     */
    public static boolean useClickTriggers(Player player, SkillClick skill) {
        return switch (mode(player, skill)) {
            case SkillClick.MODE_USE -> true;
            case SkillClick.MODE_USE_SNEAK -> player.isCrouching();
            default -> false;
        };
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        MODES.remove(event.getEntity().getUUID());
    }
}
