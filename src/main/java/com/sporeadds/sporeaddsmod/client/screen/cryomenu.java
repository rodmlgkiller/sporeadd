package com.sporeadds.sporeaddsmod.client.screen;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.cryoblocke;
import com.sporeadds.sporeaddsmod.blocks.modblocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class cryomenu extends AbstractContainerMenu {
    private final cryoblocke blockEntity;
    private final ContainerLevelAccess levelAccess;

    public cryomenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.CRYO_MENU.get(), containerId);
        this.blockEntity = (cryoblocke) entity;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        // 1. Slots del Bloque (3 filas x 9 columnas)
        // Usamos el IItemHandler de la BlockEntity
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    this.addSlot(new SlotItemHandler(handler, col + row * 9, 8 + col * 18, 18 + row * 18));
                }
            }
        });

        // 2. Inventario del Jugador (3 filas x 9 columnas)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 3. Hotbar del Jugador (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(levelAccess, player, modblocks.CRYO_BLOCK.get());
    }

    // LÃ³gica para el Shift+Click (indispensable para que no crashee)
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 27) { // Del bloque al jugador
                if (!this.moveItemStackTo(itemstack1, 27, this.slots.size(), true)) return ItemStack.EMPTY;
            } else { // Del jugador al bloque
                if (!this.moveItemStackTo(itemstack1, 0, 27, false)) return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }
}