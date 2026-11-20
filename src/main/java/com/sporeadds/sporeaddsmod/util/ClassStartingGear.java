package com.sporeadds.sporeaddsmod.util;

import com.sporeadds.sporeaddsmod.commands.ClassCommand;
import com.sporeadds.sporeaddsmod.config.ConfigItems;
import com.sporeadds.sporeaddsmod.config.StartingGearConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Gives and takes back the starting gear of each class, driven by {@link StartingGearConfig}.
 *
 * <p>This is the single implementation behind every way of changing class: the Training Book / Origins
 * ({@code ClassAssignmentUtil.applyClass}), the {@code /class} command ({@code ClassCommand}) and the hive surrender
 * that turns a player into a Kommandant ({@code HiveSurrenderTask}). The Medic's Insanity mask is equipped through
 * {@link ClassCommand#equipMedicMask}, which honours {@code medic_gas_mask_curse_of_binding}.
 */
public final class ClassStartingGear {
    private static final ResourceLocation GAS_MASK_ID = new ResourceLocation("spore", "gas_mask");

    private ClassStartingGear() {
    }

    /**
     * Call right after a player's class changed from {@code previousIdentifier} to {@code newIdentifier}: takes back the
     * kit of the class they left and gives the kit of the class they became. Nothing happens when the class did not change.
     */
    public static void onClassChanged(ServerPlayer player, String previousIdentifier, String newIdentifier) {
        if (newIdentifier != null && newIdentifier.equalsIgnoreCase(previousIdentifier)) {
            return;
        }
        removeKit(player, previousIdentifier);
        giveKit(player, newIdentifier);
    }

    /** Gives the kit of {@code classId} (no-op when the class has none, or its kit is switched off). */
    public static void giveKit(ServerPlayer player, String classId) {
        StartingGearConfig.Kit kit = StartingGearConfig.forClass(classId);
        if (kit == null || !kit.giveStartingGear.get()) {
            return;
        }

        if (kit == StartingGearConfig.MEDIC && StartingGearConfig.MEDIC_EQUIP_GAS_MASK.get()) {
            ClassCommand.equipMedicMask(player);
        }

        boolean skipCarried = kit.skipItemsAlreadyCarried.get();
        for (ConfigItems.Entry entry : ConfigItems.parseAll(kit.items.get())) {
            if (skipCarried && ConfigItems.count(player, entry.item()) > 0) {
                continue;
            }
            ConfigItems.give(player, List.of(entry));
        }
    }

    /** Takes back the kit of {@code classId}: the Medic's mask (if worn) and every stack of the kit's item types. */
    public static void removeKit(ServerPlayer player, String classId) {
        StartingGearConfig.Kit kit = StartingGearConfig.forClass(classId);
        if (kit == null || !kit.giveStartingGear.get() || !kit.removeOnClassChange.get()) {
            return;
        }

        if (kit == StartingGearConfig.MEDIC && StartingGearConfig.MEDIC_EQUIP_GAS_MASK.get()) {
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            if (head.getItem() == ForgeRegistries.ITEMS.getValue(GAS_MASK_ID)) {
                player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
        }

        Inventory inventory = player.getInventory();
        for (ConfigItems.Entry entry : ConfigItems.parseAll(kit.items.get())) {
            clear(inventory.items, entry.item());
            clear(inventory.offhand, entry.item());
        }
    }

    /** True when the Medic's Insanity mask must be (re-)equipped, e.g. after a respawn. */
    public static boolean shouldEquipMedicMask() {
        return StartingGearConfig.MEDIC.giveStartingGear.get() && StartingGearConfig.MEDIC_EQUIP_GAS_MASK.get();
    }

    /** Empties every slot holding {@code item}. Slots are cleared, never removed: the inventory lists must keep their size. */
    private static void clear(NonNullList<ItemStack> stacks, Item item) {
        for (int i = 0; i < stacks.size(); i++) {
            if (stacks.get(i).is(item)) {
                stacks.set(i, ItemStack.EMPTY);
            }
        }
    }
}
