package com.sporeadds.sporeaddsmod.client.gui;

import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.blocks.modblocks;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.RaidControlerBlockEntity;
import com.sporeadds.sporeaddsmod.client.screen.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RaidControlerMenu extends AbstractContainerMenu {

    public final RaidControlerBlockEntity blockEntity;
    private final Player player;

    private static final int SLOT_COUNT = 3;
    private static final int PLAYER_INV_START = 3;
    private static final int PLAYER_INV_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;

    public RaidControlerMenu(int windowId, Inventory playerInventory, RaidControlerBlockEntity blockEntity) {
        super((MenuType<?>) ModMenuTypes.RAID_CONTROLER.get(), windowId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 10, 20));
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 1, 10, 44));
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 2, 10, 68));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    public RaidControlerMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, data));
    }

    private static RaidControlerBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);

        if (blockEntity instanceof RaidControlerBlockEntity raidBe) {
            return raidBe;
        }

        throw new IllegalStateException("BlockEntity no encontrada en " + pos);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 97 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int slot = 0; slot < 9; ++slot) {
            addSlot(new Slot(playerInventory, slot, 8 + slot * 18, 155));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            originalStack = stackInSlot.copy();

            if (index < SLOT_COUNT) {
                if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.getItem() == ModItems.OVERCHARGED_SCENT_SPAWN_EGG.get()) {
                if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.getItem() == ModItems.POTENCY_BAIT_I.get()
                    || stackInSlot.getItem() == ModItems.POTENCY_BAIT_II.get()
                    || stackInSlot.getItem() == ModItems.POTENCY_BAIT_III.get()) {
                if (!moveItemStackTo(stackInSlot, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.getItem() == ModItems.BIOMASS_BAIT.get()) {
                if (!moveItemStackTo(stackInSlot, 2, 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_INV_START && index < PLAYER_INV_END) {
                if (!moveItemStackTo(stackInSlot, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player,
                modblocks.RAID_CONTROLER.get()
        );
    }
}