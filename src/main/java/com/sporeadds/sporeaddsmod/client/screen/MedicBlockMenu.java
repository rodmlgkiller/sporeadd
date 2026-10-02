package com.sporeadds.sporeaddsmod.client.screen;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MedicBlockEntity;
import com.sporeadds.sporeaddsmod.blocks.modblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class MedicBlockMenu extends AbstractContainerMenu {

    public final MedicBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public MedicBlockMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public MedicBlockMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MEDIC_BLOCK_MENU.get(), pContainerId);
        checkContainerSize(inv, 2);
        this.blockEntity = ((MedicBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        if (inv.player != null && !inv.player.isSpectator()) {
            blockEntity.getOpenersCounter().incrementOpeners(inv.player, blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState());
        }

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 42, 117));
            this.addSlot(new OutputSlot(iItemHandler, 1, 76, 117));
        });

        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 2;

    @Override
    public @NotNull ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < TE_INVENTORY_FIRST_SLOT_INDEX) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX,
                    TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) return ItemStack.EMPTY;
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX,
                    VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;

        if (sourceStack.isEmpty()) sourceSlot.set(ItemStack.EMPTY);
        else sourceSlot.setChanged();

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int arrowWidth = 65;

        if (maxProgress <= 0) return 0;

        return maxProgress != 0 && progress != 0 ? progress * arrowWidth / maxProgress : 0;
    }

    @Override
    public boolean stillValid(Player pplayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pplayer, modblocks.MEDIC_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; i++)
            for (int l = 0; l < 9; l++)
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 157 + i * 18));
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 215));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.isSpectator()) {
            blockEntity.getOpenersCounter().decrementOpeners(player, blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState());
        }
    }
}
