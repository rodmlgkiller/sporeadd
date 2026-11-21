package com.sporeadds.sporeaddsmod.Powers;

/**
 * The skills that are activated with a click: Kommandant powers 2 (Contaminate and Extract), 4, 5 (feeding and harvesting),
 * 6 and 7. Each one has its own key mapping in Controls > SporeAdds; the default is the click the skill has always used
 * (the right mouse button, Shift + right mouse button for harvesting), so nothing changes until a player rebinds it.
 *
 * <p>The server decides what triggers a skill from the mode the client reports (see {@link SkillBindings}):
 * <ul>
 *   <li>{@link #MODE_USE}: the binding is the vanilla Use button, so the interaction events trigger the skill, as before;</li>
 *   <li>{@link #MODE_USE_SNEAK}: the same, but only while sneaking;</li>
 *   <li>{@link #MODE_KEY}: the skill has its own input, the client sends a {@code SkillClickPacket} when it is pressed and the
 *       interaction events no longer trigger it.</li>
 * </ul>
 */
public enum SkillClick {
    POWER2_EXECUTE("power2_click", false),
    POWER4_CONSUME("power4_click", false),
    POWER5_FEED("power5_feed_click", false),
    POWER5_HARVEST("power5_harvest_click", true),
    POWER6_EVOLVE("power6_click", false),
    POWER7_MOUNT("power7_click", false);

    public static final byte MODE_USE = 0;
    public static final byte MODE_USE_SNEAK = 1;
    public static final byte MODE_KEY = 2;

    public static final int COUNT = values().length;

    private final String keyName;
    private final boolean defaultSneak;

    SkillClick(String keyName, boolean defaultSneak) {
        this.keyName = keyName;
        this.defaultSneak = defaultSneak;
    }

    /** Lang key of this skill's entry in the Controls screen. */
    public String keyTranslation() {
        return "key.sporeadd." + keyName;
    }

    /** true when the default binding is Shift + the Use button. */
    public boolean defaultSneak() {
        return defaultSneak;
    }

    public byte defaultMode() {
        return defaultSneak ? MODE_USE_SNEAK : MODE_USE;
    }

    public static SkillClick byOrdinal(int ordinal) {
        SkillClick[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : null;
    }
}
