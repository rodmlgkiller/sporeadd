package com.sporeadds.sporeaddsmod.client.screen;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MedicBlockCrafterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MedicBlockContructorMenu extends AbstractContainerMenu {

    private final MedicBlockCrafterEntity blockEntity;
    private final ContainerData data;

    public MedicBlockContructorMenu(int id, Inventory playerInv, MedicBlockCrafterEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.MEDIC_BLOCK_CONSTRUCTOR_MENU.get(), id);
        this.blockEntity = blockEntity;
        this.data = getBlockEntity().data;

        IItemHandler inv = blockEntity != null ? blockEntity.getInventory() : new ItemStackHandler(2);

        // Slot de input 0: REDSTONE_BLOCK
        this.addSlot(new SlotItemHandler(inv, 0, 17, 16) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == Items.REDSTONE_BLOCK;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPickup(Player player) {
                return !blockEntity.isCrafting();
            }
        });

        // Slot de input 1: IRON_BLOCK
        this.addSlot(new SlotItemHandler(inv, 1, 142, 16) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == Items.IRON_BLOCK;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPickup(Player player) {
                return !blockEntity.isCrafting();
            }
        });

        // Slots del inventario del jugador
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 44 + row * 18));

        // Hotbar
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 102));

        // Sincronizar el ContainerData
        addDataSlots(data);
    }

    // Inicia crafting desde el servidor
    public void startCraftingServer() {
        if (blockEntity != null && blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
            blockEntity.startCrafting();
        }
    }

    // Devuelve el cooldown
    public int getCooldown() {
        return blockEntity != null ? blockEntity.getCooldown() : 0;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < 2) { // slots de la TE
            if (!moveItemStackTo(sourceStack, 2, slots.size(), false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(sourceStack, 0, 2, false)) return ItemStack.EMPTY;

        if (sourceStack.isEmpty()) sourceSlot.set(ItemStack.EMPTY);
        else sourceSlot.setChanged();

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved();
    }

    public MedicBlockCrafterEntity getBlockEntity() {
        return blockEntity;
    }
}
