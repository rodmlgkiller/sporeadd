package com.sporeadds.sporeaddsmod.client.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * One table of a TOML config: its sub-tables are buttons that open another {@code ConfigSectionScreen}, its values are
 * editable rows (toggle, number box, text box, enum cycle, list editor). Values are applied as soon as they are valid
 * and written to disk when a config screen is left.
 */
final class ConfigSectionScreen extends Screen {
    private static final int ROW_WIDTH = 400;
    private static final int LABEL_WIDTH = 170;
    private static final int CONTROL_WIDTH = 150;
    private static final int RESET_WIDTH = 46;
    private static final int GAP = 4;

    private final Screen parent;
    private final ForgeConfigSpec spec;
    private final UnmodifiableConfig node;
    private final List<String> path;

    private ConfigList list;
    private double savedScroll;

    ConfigSectionScreen(Screen parent, Component title, ForgeConfigSpec spec, UnmodifiableConfig node, List<String> path) {
        super(title);
        this.parent = parent;
        this.spec = spec;
        this.node = node;
        this.path = path;
    }

    @Override
    protected void init() {
        this.list = new ConfigList();

        List<ConfigUi.Node> children = ConfigUi.children(spec, node, path);
        for (ConfigUi.Node child : children) {
            if (child.isSection()) {
                list.add(new SectionRow(child));
            }
        }
        for (ConfigUi.Node child : children) {
            if (!child.isSection()) {
                list.add(createValueRow(child));
            }
        }
        list.setScrollAmount(savedScroll);
        this.addWidget(list);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private Row createValueRow(ConfigUi.Node child) {
        Object def = child.value().getDefault();
        if (def instanceof Boolean) {
            return new BooleanRow(child);
        }
        if (def instanceof Integer || def instanceof Long || def instanceof Double) {
            return new NumberRow(child, def.getClass());
        }
        if (def instanceof String) {
            return new StringRow(child);
        }
        if (def instanceof Enum<?>) {
            return new EnumRow(child);
        }
        if (def instanceof List<?> items) {
            if (items.stream().allMatch(o -> o instanceof String)) {
                return new ListRow(child, String.class);
            }
            if (items.stream().allMatch(o -> o instanceof Integer)) {
                return new ListRow(child, Integer.class);
            }
            if (items.stream().allMatch(o -> o instanceof Long)) {
                return new ListRow(child, Long.class);
            }
            if (items.stream().allMatch(o -> o instanceof Double)) {
                return new ListRow(child, Double.class);
            }
        }
        return new UnsupportedRow(child);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        ConfigUi.flush();
    }

    @Override
    public void onClose() {
        ConfigUi.flush();
        this.minecraft.setScreen(parent);
    }

    /** Opens a child screen, remembering the scroll position so coming back lands in the same place. */
    private void open(Screen child) {
        this.savedScroll = this.list.getScrollAmount();
        this.minecraft.setScreen(child);
    }

    /** 400 while the window is wide enough, narrower on small windows / big GUI scales (the list stays on screen). */
    private int rowWidth() {
        return Math.max(220, Math.min(ROW_WIDTH, this.width - 60));
    }

    // ------------------------------------------------------------------------------------------------------------

    private final class ConfigList extends ContainerObjectSelectionList<Row> {
        ConfigList() {
            super(ConfigSectionScreen.this.minecraft, ConfigSectionScreen.this.width, ConfigSectionScreen.this.height,
                    32, ConfigSectionScreen.this.height - 36, 24);
        }

        void add(Row row) {
            this.addEntry(row);
        }

        @Override
        public int getRowWidth() {
            return rowWidth();
        }

        @Override
        protected int getScrollbarPosition() {
            return ConfigSectionScreen.this.width / 2 + rowWidth() / 2 + 12;
        }
    }

    private abstract class Row extends ContainerObjectSelectionList.Entry<Row> {
        final Component label;
        final List<AbstractWidget> widgets = new ArrayList<>();

        Row(Component label) {
            this.label = label;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return widgets;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return widgets;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            String text = label.getString();
            int labelWidth = Math.min(LABEL_WIDTH, width - CONTROL_WIDTH - RESET_WIDTH - 2 * GAP);
            if (font.width(text) > labelWidth) {
                text = font.plainSubstrByWidth(text, labelWidth - font.width("...")) + "...";
            }
            graphics.drawString(font, text, left, top + (height - font.lineHeight) / 2, 0xFFFFFF);

            int x = left + labelWidth + GAP;
            for (AbstractWidget widget : widgets) {
                widget.setX(x);
                widget.setY(top);
                widget.render(graphics, mouseX, mouseY, partialTick);
                x += widget.getWidth() + GAP;
            }
        }
    }

    private final class SectionRow extends Row {
        SectionRow(ConfigUi.Node node) {
            super(Component.literal(ConfigUi.prettify(node.key())));
            Component title = Component.literal(ConfigUi.prettify(node.key()));
            widgets.add(Button.builder(Component.literal("Open  >"),
                            b -> open(new ConfigSectionScreen(ConfigSectionScreen.this, title, spec, node.section(), node.path())))
                    .bounds(0, 0, CONTROL_WIDTH, 20).build());
        }
    }

    private final class UnsupportedRow extends Row {
        UnsupportedRow(ConfigUi.Node node) {
            super(Component.literal(ConfigUi.prettify(node.key())));
            Button button = Button.builder(Component.literal("Edit the .toml file"), b -> { })
                    .bounds(0, 0, CONTROL_WIDTH, 20).build();
            button.active = false;
            widgets.add(button);
        }
    }

    /** A value of the config: label, one control, and a button that restores the default. */
    private abstract class ValueRow extends Row {
        @SuppressWarnings("unchecked")
        final ForgeConfigSpec.ConfigValue<Object> value;
        final ForgeConfigSpec.ValueSpec valueSpec;
        final Tooltip tooltip;

        @SuppressWarnings("unchecked")
        ValueRow(ConfigUi.Node node) {
            super(Component.literal(ConfigUi.prettify(node.key())));
            this.value = (ForgeConfigSpec.ConfigValue<Object>) node.value();
            this.valueSpec = node.spec();
            this.tooltip = Tooltip.create(ConfigUi.tooltip(node.value(), node.spec()));
        }

        void commit(Object newValue) {
            try {
                // A text box also fires on cursor moves and re-typed text: only write when the value really changes
                // (every set() rewrites the whole config file).
                if (java.util.Objects.equals(value.get(), newValue)) {
                    return;
                }
                value.set(newValue);
                ConfigUi.markDirty(spec);
            } catch (RuntimeException e) {
                ConfigUi.LOGGER.warn("[SporeAdds config] could not change {}: {}", value.getPath(), e.toString());
            }
        }

        Button resetButton() {
            return Button.builder(Component.literal("Reset"), b -> resetToDefault())
                    .bounds(0, 0, RESET_WIDTH, 20)
                    .tooltip(Tooltip.create(Component.literal("Restore the default value")))
                    .build();
        }

        abstract void resetToDefault();
    }

    private final class BooleanRow extends ValueRow {
        private final Button toggle;

        BooleanRow(ConfigUi.Node node) {
            super(node);
            this.toggle = Button.builder(text(), b -> {
                        commit(!((Boolean) value.get()));
                        b.setMessage(text());
                    })
                    .bounds(0, 0, CONTROL_WIDTH, 20).tooltip(tooltip).build();
            widgets.add(toggle);
            widgets.add(resetButton());
        }

        private Component text() {
            return Boolean.TRUE.equals(value.get())
                    ? Component.literal("ON").withStyle(net.minecraft.ChatFormatting.GREEN)
                    : Component.literal("OFF").withStyle(net.minecraft.ChatFormatting.RED);
        }

        @Override
        void resetToDefault() {
            commit(value.getDefault());
            toggle.setMessage(text());
        }
    }

    private final class NumberRow extends ValueRow {
        private final EditBox box;
        private final Class<?> type;

        NumberRow(ConfigUi.Node node, Class<?> type) {
            super(node);
            this.type = type;
            this.box = new EditBox(font, 0, 0, CONTROL_WIDTH, 20, label);
            box.setMaxLength(32);
            box.setValue(String.valueOf(value.get()));
            box.setTooltip(tooltip);
            box.setResponder(this::onTyped);
            widgets.add(box);
            widgets.add(resetButton());
        }

        private Object parse(String text) {
            try {
                String s = text.trim();
                if (type == Integer.class) {
                    return Integer.parseInt(s);
                }
                if (type == Long.class) {
                    return Long.parseLong(s);
                }
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private void onTyped(String text) {
            Object parsed = parse(text);
            boolean valid = parsed != null && (valueSpec == null || valueSpec.test(parsed));
            box.setTextColor(valid ? 0xE0E0E0 : 0xFF5555);
            if (valid) {
                commit(parsed);
            }
        }

        @Override
        void resetToDefault() {
            box.setValue(String.valueOf(value.getDefault()));
        }
    }

    private final class StringRow extends ValueRow {
        private final EditBox box;

        StringRow(ConfigUi.Node node) {
            super(node);
            this.box = new EditBox(font, 0, 0, CONTROL_WIDTH, 20, label);
            box.setMaxLength(2048);
            box.setValue(String.valueOf(value.get()));
            box.setTooltip(tooltip);
            box.setResponder(this::onTyped);
            widgets.add(box);
            widgets.add(resetButton());
        }

        private void onTyped(String text) {
            boolean valid = valueSpec == null || valueSpec.test(text);
            box.setTextColor(valid ? 0xE0E0E0 : 0xFF5555);
            if (valid) {
                commit(text);
            }
        }

        @Override
        void resetToDefault() {
            box.setValue(String.valueOf(value.getDefault()));
        }
    }

    private final class EnumRow extends ValueRow {
        private final Button cycle;

        EnumRow(ConfigUi.Node node) {
            super(node);
            this.cycle = Button.builder(text(), b -> {
                        Enum<?> current = (Enum<?>) value.get();
                        Object[] all = current.getDeclaringClass().getEnumConstants();
                        commit(all[(current.ordinal() + 1) % all.length]);
                        b.setMessage(text());
                    })
                    .bounds(0, 0, CONTROL_WIDTH, 20).tooltip(tooltip).build();
            widgets.add(cycle);
            widgets.add(resetButton());
        }

        private Component text() {
            return Component.literal(String.valueOf(value.get()));
        }

        @Override
        void resetToDefault() {
            commit(value.getDefault());
            cycle.setMessage(text());
        }
    }

    private final class ListRow extends ValueRow {
        private final Button edit;

        ListRow(ConfigUi.Node node, Class<?> elementType) {
            super(node);
            this.edit = Button.builder(text(), b ->
                            open(new ConfigListEditScreen(ConfigSectionScreen.this, label, spec, value, valueSpec, elementType)))
                    .bounds(0, 0, CONTROL_WIDTH, 20).tooltip(tooltip).build();
            widgets.add(edit);
            widgets.add(resetButton());
        }

        private Component text() {
            int size = ((List<?>) value.get()).size();
            return Component.literal("Edit list (" + size + ")...");
        }

        @Override
        void resetToDefault() {
            commit(value.getDefault());
            edit.setMessage(text());
        }
    }
}
