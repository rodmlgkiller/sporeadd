package com.sporeadds.sporeaddsmod.client.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;

import java.util.Map;
import java.util.Set;

/**
 * SporeAdds' page of NeoForge's generic configuration screen (Mods -> SporeAdds -> Config).
 *
 * <p>NeoForge's screen looks every option name up in the language files and prints the raw translation key when it is
 * missing. This mod has hundreds of options and no per-option translations, so this subclass falls back to a readable
 * name made from the option's key ({@code damage_multiplier} -> "Damage Multiplier"). The tooltips already fall back to
 * the comment written in the config class, so they need nothing.
 */
public class SporeConfigSectionScreen extends ConfigurationSectionScreen {

    // The literals below are NeoForge's own (private) UI keys, so buttons keep their standard look and translations.
    private static final String SECTION = "neoforge.configuration.uitext.section";
    private static final String SECTION_TEXT = "neoforge.configuration.uitext.sectiontext";

    /** Top page of one config file (what NeoForge's ConfigurationScreen opens for Common / Client). */
    public SporeConfigSectionScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
    }

    private SporeConfigSectionScreen(Context context, Screen parent, Map<String, Object> valueSpecs, String key,
                                     Set<? extends UnmodifiableConfig.Entry> entries, Component title) {
        super(context, parent, valueSpecs, key, entries, title);
    }

    @Override
    protected MutableComponent getTranslationComponent(String key) {
        String translationKey = getTranslationKey(key);
        if (I18n.exists(translationKey)) {
            return Component.translatable(translationKey);
        }
        return Component.literal(prettify(key));
    }

    /** Same as the parent's version, but opens another SporeConfigSectionScreen so the readable names apply at every depth. */
    @Override
    protected Element createSection(String key, UnmodifiableConfig subconfig, UnmodifiableConfig subsection) {
        if (subconfig.isEmpty()) {
            return null;
        }
        return new Element(Component.translatable(SECTION, getTranslationComponent(key)), getTooltipComponent(key, null),
                Button.builder(Component.translatable(SECTION, Component.translatable(SECTION_TEXT)),
                                button -> minecraft.setScreen(sectionCache.computeIfAbsent(key,
                                        k -> new SporeConfigSectionScreen(context, this, subconfig.valueMap(), key,
                                                subsection.entrySet(), getTranslationComponent(key)).rebuild())))
                        .tooltip(Tooltip.create(getTooltipComponent(key, null)))
                        .width(Button.DEFAULT_WIDTH)
                        .build(),
                false);
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
}
