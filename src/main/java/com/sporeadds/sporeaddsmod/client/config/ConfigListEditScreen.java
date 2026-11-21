package com.sporeadds.sporeaddsmod.client.config;

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
 * Editor for a config value that is a list of strings: one text box per entry, a remove button each, and an "Add entry"
 * button. "Done" is only available while every entry is valid for the option (the same check the config file gets).
 */
final class ConfigListEditScreen extends Screen {
    private static final int ROW_WIDTH = 440;
    private static final int REMOVE_WIDTH = 24;
    private static final int GAP = 4;

    private final Screen parent;
    private final ForgeConfigSpec rootSpec;
    private final ForgeConfigSpec.ConfigValue<Object> value;
    private final ForgeConfigSpec.ValueSpec valueSpec;
    private final Class<?> elementType;
    private final List<String> items = new ArrayList<>();

    private EntryList list;
    private Button doneButton;

    @SuppressWarnings("unchecked")
    ConfigListEditScreen(Screen parent, Component title, ForgeConfigSpec rootSpec,
                         ForgeConfigSpec.ConfigValue<Object> value, ForgeConfigSpec.ValueSpec valueSpec, Class<?> elementType) {
        super(title);
        this.elementType = elementType;
        this.parent = parent;
        this.rootSpec = rootSpec;
        this.value = value;
        this.valueSpec = valueSpec;
        for (Object o : (List<?>) value.get()) {
            items.add(String.valueOf(o));
        }
    }

    @Override
    protected void init() {
        this.list = new EntryList();
        rebuildRows();
        this.addWidget(list);

        int y = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("Add entry"), b -> {
                    items.add("");
                    rebuildRows();
                    list.setScrollAmount(Double.MAX_VALUE);
                    refreshDone();
                })
                .bounds(this.width / 2 - 155, y, 100, 20).build());
        this.doneButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> apply())
                .bounds(this.width / 2 - 50, y, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.onClose())
                .bounds(this.width / 2 + 55, y, 100, 20).build());
        refreshDone();
    }

    private void rebuildRows() {
        double scroll = list.getScrollAmount();
        // A discarded row can keep the list's focus (a mouse click on its X sets the focus after the handler returns), so
        // Enter / Space / typing would still reach its widgets with an index that no longer matches the entries.
        for (EntryRow old : list.children()) {
            old.stale = true;
        }
        list.clear();
        for (int i = 0; i < items.size(); i++) {
            list.add(new EntryRow(i));
        }
        list.setScrollAmount(scroll);
    }

    /** The entry as the list's element type (String, Integer, Long or Double), or null when it does not parse. */
    private Object parse(String entry) {
        try {
            String s = entry.trim();
            if (elementType == Integer.class) {
                return Integer.parseInt(s);
            }
            if (elementType == Long.class) {
                return Long.parseLong(s);
            }
            if (elementType == Double.class) {
                return Double.parseDouble(s);
            }
            return entry;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isValid(String entry) {
        Object parsed = parse(entry);
        return parsed != null && (valueSpec == null || valueSpec.test(List.of(parsed)));
    }

    private boolean allValid() {
        for (String s : items) {
            if (!isValid(s)) {
                return false;
            }
        }
        return true;
    }

    private void refreshDone() {
        if (doneButton != null) {
            doneButton.active = allValid();
        }
    }

    private void apply() {
        if (!allValid()) {
            return;
        }
        List<Object> parsed = new ArrayList<>();
        for (String s : items) {
            parsed.add(parse(s));
        }
        value.set(parsed);
        ConfigUi.markDirty(rootSpec);
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal(items.size() + " entries"), this.width / 2, 20, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    /** 440 while the window is wide enough, narrower on small windows / big GUI scales (rows and X buttons stay on screen). */
    private int rowWidth() {
        return Math.max(220, Math.min(ROW_WIDTH, this.width - 60));
    }

    // ------------------------------------------------------------------------------------------------------------

    private final class EntryList extends ContainerObjectSelectionList<EntryRow> {
        EntryList() {
            super(ConfigListEditScreen.this.minecraft, ConfigListEditScreen.this.width, ConfigListEditScreen.this.height,
                    34, ConfigListEditScreen.this.height - 36, 24);
        }

        void add(EntryRow row) {
            this.addEntry(row);
        }

        void clear() {
            this.clearEntries();
        }

        @Override
        public int getRowWidth() {
            return rowWidth();
        }

        @Override
        protected int getScrollbarPosition() {
            return ConfigListEditScreen.this.width / 2 + rowWidth() / 2 + 12;
        }
    }

    private final class EntryRow extends ContainerObjectSelectionList.Entry<EntryRow> {
        private final EditBox box;
        private final Button remove;
        private final List<AbstractWidget> widgets = new ArrayList<>();
        /** Set when the rows were rebuilt: this row is gone and its index no longer points at its entry. */
        private boolean stale;

        EntryRow(int index) {
            this.box = new EditBox(font, 0, 0, rowWidth() - REMOVE_WIDTH - GAP, 20, Component.literal("Entry " + (index + 1)));
            box.setMaxLength(1024);
            box.setValue(items.get(index));
            box.setTextColor(isValid(items.get(index)) ? 0xE0E0E0 : 0xFF5555);
            box.setResponder(text -> {
                if (stale) {
                    return;
                }
                items.set(index, text);
                box.setTextColor(isValid(text) ? 0xE0E0E0 : 0xFF5555);
                refreshDone();
            });
            this.remove = Button.builder(Component.literal("X"), b -> {
                        if (stale) {
                            return;
                        }
                        items.remove(index);
                        rebuildRows();
                        refreshDone();
                    })
                    .bounds(0, 0, REMOVE_WIDTH, 20)
                    .tooltip(Tooltip.create(Component.literal("Remove this entry")))
                    .build();
            widgets.add(box);
            widgets.add(remove);
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
            box.setX(left);
            box.setY(top);
            box.render(graphics, mouseX, mouseY, partialTick);
            remove.setX(left + box.getWidth() + GAP);
            remove.setY(top);
            remove.render(graphics, mouseX, mouseY, partialTick);
        }
    }
}
