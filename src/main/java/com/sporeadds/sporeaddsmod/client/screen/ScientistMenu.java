package com.sporeadds.sporeaddsmod.client.screen;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.ScientistBlockEntity;
import com.sporeadds.sporeaddsmod.blocks.modblocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;


public class ScientistMenu extends AbstractContainerMenu {
    public final ScientistBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;


    public ScientistMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public ScientistMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.SCIENTIST_MENU.get(), containerId);
        checkContainerSize(inv, 3);
        blockEntity = (ScientistBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        java.util.Optional.of(this.blockEntity.getItemHandler()).ifPresent(iItemHandler -> {
            // Slot azul (biomasa)
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 11, 20));
            // Slot papel
            this.addSlot(new SlotItemHandler(iItemHandler, 1, 89, 20));
            // Output slot
            this.addSlot(new SlotItemHandler(iItemHandler, 2, 124, 20));
        });

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invIndex) {
        Slot sourceSlot = slots.get(invIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (invIndex < 36) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, 36, 36 + 3, false)) {
                return ItemStack.EMPTY;  // FAIL
            }
        } else if (invIndex < 36 + 3) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, modblocks.SCIENTIST_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 97 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 155));
        }
    }

    public int getStoredBiomass() {
        return data.get(0);
    }

    public int getProcessingProgress() {
        return data.get(1);
    }
}