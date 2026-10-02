package com.sporeadds.sporeaddsmod.client.trainingbook;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.client.SporeKeyMapping;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.items.TrainingBookItem;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SelectClassPacket;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TrainingBookClassDetailScreen extends Screen {

    private static final int LINE_HEIGHT = 9;
    // Pages fill the whole text area (matches vanilla's own book capacity) except the final page,
    // which reserves room at the bottom for the slot counter / learn button footer.
    private static final int FULL_PAGE_LINES = 14;
    private static final int LAST_PAGE_LINES = 8;

    // Vertical offsets, relative to topPos, laid out so nothing overlaps:
    // title (12) -> page indicator (22) -> content (30..102) -> footer (108..150) -> nav row (157) -> index button (194)
    private static final int TITLE_Y = 12;
    private static final int PAGE_INDICATOR_Y = 22;
    private static final int FOOTER_SLOTS_Y = 108;
    private static final int FOOTER_REASON_Y = 120;
    private static final int LEARN_BUTTON_Y = 130;
    private static final int NAV_BUTTON_Y = 157;
    private static final int INDEX_BUTTON_Y = 194;

    private static final int ICON_SIZE = 16;
    private static final int ICON_SPACING = 22;

    private static Supplier<Item> byId(String path) {
        return () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", path));
    }

    /** Cadenas + las 9 syringes de Compounds (items del mod Spore). */
    private static List<Supplier<Item>> berserkerItems() {
        List<Supplier<Item>> list = new ArrayList<>();
        list.add(ModItems.REINFORCED_COMBAT_CHAINS);
        for (com.sporeadds.sporeaddsmod.Powers.berserker.CompoundType type
                : com.sporeadds.sporeaddsmod.Powers.berserker.CompoundType.values()) {
            ResourceLocation id = type.itemId();
            list.add(() -> BuiltInRegistries.ITEM.get(id));
        }
        return list;
    }

    private static final Map<String, List<Supplier<Item>>> CLASS_ITEMS = Map.<String, List<Supplier<Item>>>of(
            "kommandant", List.of(ModItems.BIOMASS_CORE),
            "ghost", List.of(ModItems.BUCKET_OF_REMAINS),
            "medic", List.of(
                    ModItems.INJECTOR, ModItems.THROWABLE_BANDAGES, ModItems.SYRINGE, ModItems.VACCINE,
                    byId("medic_block"), byId("medic_constructor_block")
            ),
            "scientist", List.of(
                    ModItems.IMPROVISED_LOCATOR, ModItems.CORE_MOUND_LOCATOR, ModItems.PROTO_LOCATOR,
                    ModItems.NANO_INJECTOR, ModItems.SCALPEL, ModItems.SURGICAL_IMPLANTATOR, ModItems.FREEZER_BLOCK,
                    byId("scientist_block")
            ),
            "berserker", berserkerItems()
    );

    private final String classId;
    private final InteractionHand hand;
    private final Screen parentScreen;

    private List<List<FormattedCharSequence>> pages;
    private int currentPage;

    private int leftPos;
    private int topPos;
    private int contentCenterX;
    private int textX;

    private List<ItemStack> sidebarItems;

    private Button learnButton;
    private Component disabledReason = Component.empty();

    public TrainingBookClassDetailScreen(String classId, InteractionHand hand, Screen parentScreen) {
        super(Component.translatable("class.sporeadd." + classId));
        this.classId = classId;
        this.hand = hand;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - TrainingBookScreen.IMAGE_WIDTH) / 2;
        this.topPos = (this.height - TrainingBookScreen.IMAGE_HEIGHT) / 2;
        this.contentCenterX = leftPos + TrainingBookScreen.TEXT_LEFT_OFFSET + TrainingBookScreen.TEXT_WIDTH / 2;
        this.textX = leftPos + TrainingBookScreen.TEXT_LEFT_OFFSET;

        Component description = Component.translatable("class.sporeadd." + classId + ".description", buildDescriptionArgs());
        List<FormattedCharSequence> allLines = this.font.split(description, TrainingBookScreen.TEXT_WIDTH);

        pages = paginate(allLines);
        currentPage = Math.min(currentPage, pages.size() - 1);

        sidebarItems = new ArrayList<>();
        for (Supplier<Item> item : CLASS_ITEMS.getOrDefault(classId.toLowerCase(), List.of())) {
            Item resolved = item.get();
            if (resolved != null) {
                sidebarItems.add(new ItemStack(resolved));
            }
        }

        rebuildBookWidgets();
    }

    private static List<List<FormattedCharSequence>> paginate(List<FormattedCharSequence> allLines) {
        List<List<FormattedCharSequence>> result = new ArrayList<>();
        int total = allLines.size();

        if (total == 0) {
            result.add(List.of());
            return result;
        }

        if (total <= LAST_PAGE_LINES) {
            result.add(allLines);
            return result;
        }

        // Find the fewest pages that can hold everything (body pages at full capacity, one final page capped
        // to leave room for the footer), then spread the body lines evenly across those pages instead of
        // dumping the leftover onto a single near-empty page right before the last one.
        int pageCount = 2;
        while ((pageCount - 1) * FULL_PAGE_LINES + LAST_PAGE_LINES < total) {
            pageCount++;
        }

        int bodyPages = pageCount - 1;
        int bodyLines = total - LAST_PAGE_LINES;
        int base = bodyLines / bodyPages;
        int extra = bodyLines % bodyPages;

        int index = 0;
        for (int i = 0; i < bodyPages; i++) {
            int take = base + (i < extra ? 1 : 0);
            result.add(allLines.subList(index, index + take));
            index += take;
        }
        result.add(allLines.subList(index, total));

        return result;
    }

    /**
     * Kommandant opens its abilities from the Ability Selector (key.sporeadd.open_menu);
     * every other class opens the Action Wheel (key.sporeadd.open_action_wheel) instead.
     */
    private Object[] buildDescriptionArgs() {
        net.minecraft.client.KeyMapping keyMapping = "kommandant".equalsIgnoreCase(classId)
                ? SporeKeyMapping.OPEN_MENU
                : SporeKeyMapping.OPEN_ACTION_WHEEL;

        // Bright yellow is nearly unreadable against the book's parchment background, so keybinds use dark blue instead.
        Component keyArg = keyMapping.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.DARK_BLUE);

        // %2$s -> probabilidad de "Call of the Hive" (valor del servidor; usado por kommandant).
        Component chanceArg = Component.literal(
                com.sporeadds.sporeaddsmod.client.hive.HiveClientConfig.getChancePercentText()
        ).withStyle(ChatFormatting.DARK_RED);

        return new Object[]{keyArg, chanceArg};
    }

    private void rebuildBookWidgets() {
        this.clearWidgets();

        PageButton prevButton = new PageButton(
                leftPos + 43, topPos + NAV_BUTTON_Y, false, b -> changePage(-1), true
        );
        prevButton.active = currentPage > 0;
        prevButton.visible = currentPage > 0;
        this.addRenderableWidget(prevButton);

        PageButton nextButton = new PageButton(
                leftPos + 116, topPos + NAV_BUTTON_Y, true, b -> changePage(1), true
        );
        nextButton.active = currentPage < pages.size() - 1;
        nextButton.visible = currentPage < pages.size() - 1;
        this.addRenderableWidget(nextButton);

        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.sporeadd.training_book.back"), b -> goBack())
                        .bounds(contentCenterX - 45, topPos + INDEX_BUTTON_Y, 90, 20)
                        .build()
        );

        learnButton = null;
        if (currentPage == pages.size() - 1) {
            learnButton = Button.builder(
                            Component.translatable("screen.sporeadd.training_book.learn"),
                            b -> onLearnClicked()
                    )
                    .bounds(contentCenterX - 55, topPos + LEARN_BUTTON_Y, 110, 20)
                    .build();
            this.addRenderableWidget(learnButton);
        }
    }

    private void changePage(int delta) {
        currentPage = Math.max(0, Math.min(pages.size() - 1, currentPage + delta));
        rebuildBookWidgets();
    }

    private void goBack() {
        if (this.minecraft != null) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.setScreen(parentScreen);
        }
    }

    private int getHeldBookUses() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return 0;
        }
        ItemStack stack = this.minecraft.player.getItemInHand(hand);
        if (!(stack.getItem() instanceof TrainingBookItem)) {
            return 0;
        }
        return TrainingBookItem.getUsesRemaining(stack);
    }

    private void updateLearnButtonState() {
        if (learnButton == null || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        boolean enabled = SporeAddsConfig.isClassEnabledInTrainingBook(classId);
        boolean kommandantLocked = SporeAddsConfig.TRAINING_BOOK_KOMMANDANT_LOCKED.get()
                && SporeClassUtil.hasClass(this.minecraft.player, "kommandant");
        boolean alreadyClass = SporeClassUtil.hasClass(this.minecraft.player, classId);
        boolean hasUses = getHeldBookUses() > 0;
        boolean hasSlot = !ClientTrainingBookData.hasData() || ClientTrainingBookData.hasFreeSlot(classId);

        if (!enabled) {
            disabledReason = Component.translatable("screen.sporeadd.training_book.reason.disabled");
        } else if (kommandantLocked) {
            disabledReason = Component.translatable("screen.sporeadd.training_book.reason.kommandant_locked");
        } else if (alreadyClass) {
            disabledReason = Component.translatable("screen.sporeadd.training_book.reason.already_class");
        } else if (!hasUses) {
            disabledReason = Component.translatable("screen.sporeadd.training_book.reason.no_uses");
        } else if (!hasSlot) {
            disabledReason = Component.translatable("screen.sporeadd.training_book.reason.no_slots");
        } else {
            disabledReason = Component.empty();
        }

        learnButton.active = enabled && !kommandantLocked && !alreadyClass && hasUses && hasSlot;
    }

    private void onLearnClicked() {
        if (this.minecraft == null) {
            return;
        }

        this.minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        NetworkHandle.INSTANCE.sendToServer(new SelectClassPacket(classId, hand));
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(null);
                        }
                    } else if (this.minecraft != null) {
                        this.minecraft.setScreen(this);
                    }
                },
                Component.translatable("screen.sporeadd.training_book.confirm.title"),
                Component.translatable("screen.sporeadd.training_book.confirm.message",
                        Component.translatable("class.sporeadd." + classId))
        ));
    }

    /** The vanilla blur would be drawn over everything these screens paint before super.render(). */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)  {
        this.renderTransparentBackground(graphics);

        graphics.blit(TrainingBookScreen.BOOK_TEXTURE, leftPos, topPos, 0, 0,
                TrainingBookScreen.IMAGE_WIDTH, TrainingBookScreen.IMAGE_HEIGHT);

        TrainingBookScreen.drawFittedCenteredNoShadow(
                graphics,
                this.font,
                this.title.copy().withStyle(TrainingBookColors.getColor(classId)),
                contentCenterX,
                topPos + TITLE_Y,
                TrainingBookScreen.TEXT_WIDTH,
                0xFFFFFF
        );

        TrainingBookScreen.drawFittedCenteredNoShadow(
                graphics,
                this.font,
                Component.translatable("screen.sporeadd.training_book.page_indicator", currentPage + 1, pages.size()),
                contentCenterX,
                topPos + PAGE_INDICATOR_Y,
                TrainingBookScreen.TEXT_WIDTH,
                0x555555
        );

        int contentTop = topPos + TrainingBookScreen.TEXT_TOP_OFFSET;
        List<FormattedCharSequence> lines = pages.get(currentPage);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(this.font, lines.get(i), textX, contentTop + i * LINE_HEIGHT, 0x000000, false);
        }

        if (currentPage == pages.size() - 1) {
            updateLearnButtonState();

            int maxSlots = ClientTrainingBookData.getMaxSlots(classId);
            int count = ClientTrainingBookData.getCount(classId);

            Component slotsText;
            if (!ClientTrainingBookData.hasData()) {
                slotsText = Component.translatable("screen.sporeadd.training_book.slots_loading");
            } else if (maxSlots < 0) {
                slotsText = Component.translatable("screen.sporeadd.training_book.slots_unlimited", count);
            } else {
                slotsText = Component.translatable("screen.sporeadd.training_book.slots", count, maxSlots);
            }

            TrainingBookScreen.drawFittedCenteredNoShadow(
                    graphics, this.font, slotsText, contentCenterX, topPos + FOOTER_SLOTS_Y, TrainingBookScreen.TEXT_WIDTH, 0x000000
            );

            if (!disabledReason.getString().isEmpty() && learnButton != null && !learnButton.active) {
                TrainingBookScreen.drawFittedCenteredNoShadow(
                        graphics,
                        this.font,
                        disabledReason.copy().withStyle(ChatFormatting.DARK_RED),
                        contentCenterX,
                        topPos + FOOTER_REASON_Y,
                        TrainingBookScreen.TEXT_WIDTH,
                        0xAA0000
                );
            }
        }

        ItemStack hoveredItem = renderSidebarItems(graphics, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTicks);

        if (hoveredItem != null) {
            graphics.renderTooltip(this.font, hoveredItem, mouseX, mouseY);
        }
    }

    private ItemStack renderSidebarItems(GuiGraphics graphics, int mouseX, int mouseY) {
        if (sidebarItems.isEmpty()) {
            return null;
        }

        int iconX = leftPos + TrainingBookScreen.IMAGE_WIDTH + 14;
        int iconY = topPos + 10;
        // Aprieta el espaciado cuando hay muchos items para que no se salgan del libro.
        int spacing = Math.min(ICON_SPACING,
                (TrainingBookScreen.IMAGE_HEIGHT - 24) / Math.max(1, sidebarItems.size()));

        ItemStack hovered = null;
        for (int i = 0; i < sidebarItems.size(); i++) {
            int y = iconY + i * spacing;
            ItemStack stack = sidebarItems.get(i);

            graphics.renderItem(stack, iconX, y);

            if (mouseX >= iconX && mouseX < iconX + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE) {
                hovered = stack;
            }
        }

        return hovered;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
