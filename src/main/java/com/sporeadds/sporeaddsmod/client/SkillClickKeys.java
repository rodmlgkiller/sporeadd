package com.sporeadds.sporeaddsmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sporeadds.sporeaddsmod.Powers.SkillClick;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SkillBindingsPacket;
import com.sporeadds.sporeaddsmod.network.SkillClickPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

/**
 * Client side of the rebindable skill clicks (Controls > SporeAdds): works out what every {@link SkillClick} is bound to, tells the
 * server, sends the skill's own key when it has one, and words the skill descriptions after the current binding.
 *
 * <p>A skill bound to the same input as the vanilla Use button (the default: right mouse button) behaves exactly as before, the
 * server reacts to the interaction itself. Bound to anything else, the skill listens to its own input here (raw input events, so a
 * clash with another key mapping cannot swallow the press) and the server runs it on whatever the player is looking at.
 */
@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SkillClickKeys {
    private static byte[] lastSent;

    private SkillClickKeys() {
    }

    public static KeyMapping mapping(SkillClick skill) {
        return switch (skill) {
            case POWER2_EXECUTE -> SporeKeyMapping.POWER2_CLICK;
            case POWER4_CONSUME -> SporeKeyMapping.POWER4_CLICK;
            case POWER5_FEED -> SporeKeyMapping.POWER5_FEED_CLICK;
            case POWER5_HARVEST -> SporeKeyMapping.POWER5_HARVEST_CLICK;
            case POWER6_EVOLVE -> SporeKeyMapping.POWER6_CLICK;
            case POWER7_MOUNT -> SporeKeyMapping.POWER7_CLICK;
        };
    }

    /** MODE_USE / MODE_USE_SNEAK when the skill shares the vanilla Use input (see {@link SkillClick}), MODE_KEY otherwise. */
    public static byte modeOf(SkillClick skill) {
        KeyMapping m = mapping(skill);
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            KeyMapping use = mc.options.keyUse;
            if (m.getKey().equals(use.getKey()) && use.getKeyModifier() == KeyModifier.NONE) {
                if (m.getKeyModifier() == KeyModifier.NONE) {
                    return SkillClick.MODE_USE;
                }
                if (m.getKeyModifier() == KeyModifier.SHIFT) {
                    return SkillClick.MODE_USE_SNEAK;
                }
            }
        }
        return SkillClick.MODE_KEY;
    }

    private static byte[] currentModes() {
        byte[] modes = new byte[SkillClick.COUNT];
        for (SkillClick skill : SkillClick.values()) {
            modes[skill.ordinal()] = modeOf(skill);
        }
        return modes;
    }

    // ---------------------------------------------------------------------------------------------------- wording

    private static String mouseName(InputConstants.Key key) {
        if (key.getType() == InputConstants.Type.MOUSE) {
            if (key.getValue() == GLFW.GLFW_MOUSE_BUTTON_LEFT) return "left";
            if (key.getValue() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) return "right";
            if (key.getValue() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return "middle";
        }
        return null;
    }

    private static Component withModifier(KeyModifier modifier, Component base) {
        if (modifier == KeyModifier.NONE) {
            return base;
        }
        String name = switch (modifier) {
            case SHIFT -> "shift";
            case CONTROL -> "control";
            default -> "alt";
        };
        return Component.translatable("keybind.sporeadd.modifier_combo", Component.translatable("keybind.sporeadd.modifier." + name), base);
    }

    /**
     * What to do, as a verb phrase that goes in front of the target: "Right-click", "Shift + Right-click", "Press [R] on"... so a
     * description reads "{verb} an entity with less than 5 HP".
     */
    public static Component verb(SkillClick skill) {
        KeyMapping m = mapping(skill);
        if (m.isUnbound()) {
            return Component.translatable("keybind.sporeadd.verb.unbound");
        }
        String mouse = mouseName(m.getKey());
        if (mouse != null) {
            return withModifier(m.getKeyModifier(), Component.translatable("keybind.sporeadd.verb." + mouse));
        }
        return Component.translatable("keybind.sporeadd.verb.press", m.getTranslatedKeyMessage());
    }

    /** The input itself, for headings: "Right-Click", "Shift + Right-Click", "[R]"... */
    public static Component label(SkillClick skill) {
        KeyMapping m = mapping(skill);
        if (m.isUnbound()) {
            return Component.translatable("keybind.sporeadd.label.unbound");
        }
        String mouse = mouseName(m.getKey());
        if (mouse != null) {
            return withModifier(m.getKeyModifier(), Component.translatable("keybind.sporeadd.label." + mouse));
        }
        return m.getTranslatedKeyMessage();
    }

    // ---------------------------------------------------------------------------------------------------- events

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        lastSent = null;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        lastSent = null;
    }

    /** Keeps the server informed of what every skill is bound to (once after joining, then whenever Controls change it). */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        byte[] modes = currentModes();
        if (!Arrays.equals(modes, lastSent)) {
            NetworkHandle.INSTANCE.sendToServer(new SkillBindingsPacket(modes));
            lastSent = modes;
        }
    }

    @SubscribeEvent
    public static void onMouse(InputEvent.MouseButton.Post event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            pressed(InputConstants.Type.MOUSE.getOrCreate(event.getButton()));
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            pressed(InputConstants.getKey(event.getKey(), event.getScanCode()));
        }
    }

    private static void pressed(InputConstants.Key input) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        for (SkillClick skill : SkillClick.values()) {
            if (modeOf(skill) == SkillClick.MODE_KEY && mapping(skill).isActiveAndMatches(input)) {
                NetworkHandle.INSTANCE.sendToServer(new SkillClickPacket(skill.ordinal()));
            }
        }
    }
}
