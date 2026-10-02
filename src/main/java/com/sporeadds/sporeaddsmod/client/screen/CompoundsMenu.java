package com.sporeadds.sporeaddsmod.client.screen;

import com.sporeadds.sporeaddsmod.Powers.berserker.CompoundType;
import com.sporeadds.sporeaddsmod.capabilities.CompoundsCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/** Inventario portatil de "Compounds" del berserker: 6 ranuras de syringes + inventario del jugador. */
public class CompoundsMenu extends AbstractContainerMenu {

    public static final int SLOT_COUNT = 6;

    // GUI "berserker": primera syringe en (8,56); slots (8,56)-(23,71), (37,56)-(52,71),
    // (66,56)-(81,71)... => paso de 29 px a la derecha, en una fila de 6.
    private static final int SLOT_START_X = 8;
    private static final int SLOT_Y_POS = 56;
    private static final int SLOT_PITCH_X = 29;

    private final Player player;
    private final CompoundsInventory compounds;

    public CompoundsMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(windowId, playerInventory, playerInventory.player);
    }

    public CompoundsMenu(int windowId, Inventory playerInventory, Player owner) {
        super(ModMenuTypes.COMPOUNDS_MENU.get(), windowId);
        this.player = playerInventory.player;
        this.compounds = new CompoundsInventory(owner);

        for (int i = 0; i < SLOT_COUNT; i++) {
            this.addSlot(new CompoundSlot(compounds, i, SLOT_START_X + i * SLOT_PITCH_X, SLOT_Y_POS));
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean moved = false;
                for (int i = 0; i < SLOT_COUNT; i++) {
                    Slot compoundSlot = this.slots.get(i);
                    if (compoundSlot instanceof CompoundSlot cs && cs.mayPlace(stack) && !cs.hasItem()) {
                        if (this.moveItemStackTo(stack, i, i + 1, false)) {
                            moved = true;
                            break;
                        }
                    }
                }
                if (!moved) return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public void removed(Player closingPlayer) {
        super.removed(closingPlayer);
        if (closingPlayer.level().isClientSide) return;
        compounds.saveToPlayer();
        if (closingPlayer instanceof net.minecraft.server.level.ServerPlayer sp) {
            com.sporeadds.sporeaddsmod.Powers.berserker.CompoundEffects.syncToClient(sp);
        }
    }

    public static class CompoundSlot extends SlotItemHandler {
        public CompoundSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return CompoundType.isSyringe(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public static class CompoundsInventory extends ItemStackHandler {
        private final Player owner;

        public CompoundsInventory(Player owner) {
            super(SLOT_COUNT);
            this.owner = owner;
            loadFromPlayer();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;   // una sola syringe por ranura (evita apilado por shift-click / insertItem)
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return CompoundType.isSyringe(stack);
        }

        private void loadFromPlayer() {
            owner.getCapability(CompoundsCapability.PLAYER_COMPOUNDS).ifPresent(store -> {
                for (int i = 0; i < SLOT_COUNT; i++) {
                    ItemStack s = store.getStack(i);
                    setStackInSlot(i, s.isEmpty() ? ItemStack.EMPTY : s.copy());
                }
            });
        }

        public void saveToPlayer() {
            owner.getCapability(CompoundsCapability.PLAYER_COMPOUNDS).ifPresent(store -> {
                for (int i = 0; i < SLOT_COUNT; i++) {
                    ItemStack stack = getStackInSlot(i);
                    store.setStack(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                }
            });
        }
    }
}
