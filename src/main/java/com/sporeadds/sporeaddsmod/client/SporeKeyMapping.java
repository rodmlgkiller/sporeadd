package com.sporeadds.sporeaddsmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class SporeKeyMapping {

    public static final String CATEGORY_SPOREADD = "key.categories.sporeadd";

    public static final KeyMapping OPEN_MENU = new KeyMapping("key.sporeadd.open_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY_SPOREADD);
    public static final KeyMapping OPEN_VERVA_MENU = new KeyMapping("key.sporeadd.open_verva_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY_SPOREADD);

    public static final KeyMapping POWER_1 = new KeyMapping("key.sporeadd.power1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_2 = new KeyMapping("key.sporeadd.power2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_3 = new KeyMapping("key.sporeadd.power3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_4 = new KeyMapping("key.sporeadd.power4", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_5 = new KeyMapping("key.sporeadd.power5", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_6 = new KeyMapping("key.sporeadd.power6", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_7 = new KeyMapping("key.sporeadd.power7", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_8 = new KeyMapping("key.sporeadd.power8", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_9 = new KeyMapping("key.sporeadd.power9", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_10 = new KeyMapping("key.sporeadd.power10", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_11 = new KeyMapping("key.sporeadd.power11", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_12 = new KeyMapping("key.sporeadd.power12", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping POWER_13 = new KeyMapping("key.sporeadd.power13", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY_SPOREADD);
    public static final KeyMapping OPEN_ACTION_WHEEL = new KeyMapping("key.sporeadd.open_action_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY_SPOREADD);

    /** Ataja el slot 1 / 2 del action wheel de la clase (por defecto B y N). */
    public static final KeyMapping ABILITY_1 = new KeyMapping("key.sporeadd.ability_1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY_SPOREADD);
    public static final KeyMapping ABILITY_2 = new KeyMapping("key.sporeadd.ability_2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY_SPOREADD);

    public static final KeyMapping CAUSTIC_SHOT = new KeyMapping("key.sporeadd.caustic_shot", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY_SPOREADD);

    static {
        net.neoforged.neoforge.client.settings.KeyModifier.NONE.toString();
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU);
        event.register(OPEN_VERVA_MENU);
        event.register(POWER_1);
        event.register(POWER_2);
        event.register(POWER_3);
        event.register(POWER_4);
        event.register(POWER_5);
        event.register(POWER_6);
        event.register(POWER_7);
        event.register(POWER_8);
        event.register(POWER_9);
        event.register(POWER_10);
        event.register(POWER_11);
        event.register(POWER_12);
        event.register(POWER_13);
        event.register(OPEN_ACTION_WHEEL);
        event.register(ABILITY_1);
        event.register(ABILITY_2);
        event.register(CAUSTIC_SHOT);
    }
}