package com.sporeadds.sporeaddsmod.client.screen;

import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.capabilities.PlayerImplantsCapability;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncImplantPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import javax.annotation.Nonnull;

public class ImplantMenu extends AbstractContainerMenu {

    private final Player player;
    private final Player targetPlayer;
    private final ImplantInventory implantInventory;

    public ImplantMenu(int windowId, Inventory playerInventory, net.minecraft.network.FriendlyByteBuf buf) {
        this(windowId, playerInventory, getTargetPlayer(playerInventory.player, buf.readUUID()));
    }

    private static Player getTargetPlayer(Player opener, java.util.UUID targetUUID) {
        if (opener.level().getPlayerByUUID(targetUUID) != null) {
            return opener.level().getPlayerByUUID(targetUUID);
        }
        return opener;
    }

    public ImplantMenu(int windowId, Inventory playerInventory, Player targetPlayer) {
        super(ModMenuTypes.IMPLANT_MENU.get(), windowId);
        this.player = playerInventory.player;
        this.targetPlayer = targetPlayer;
        this.implantInventory = new ImplantInventory(targetPlayer);

        this.addSlot(new ImplantSlot(implantInventory, 0, 108, 28, ModItems.EYE_IMPLANT.get()));
        this.addSlot(new ImplantSlot(implantInventory, 1, 108, 58, ModItems.TORSO_IMPLANT.get()));
        this.addSlot(new ImplantSlot(implantInventory, 2, 128, 58, ModItems.RIGHTARM_IMPLANT.get()));
        this.addSlot(new ImplantSlot(implantInventory, 3, 88, 58, ModItems.LEFTARM_IMPLANT.get()));
        this.addSlot(new ImplantSlot(implantInventory, 4, 120, 88, ModItems.RIGHTLEG_IMPLANT.get()));
        this.addSlot(new ImplantSlot(implantInventory, 5, 99, 88, ModItems.LEFTLEG_IMPLANT.get()));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 133 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 191));
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

            if (index < 6) {
                if (!this.moveItemStackTo(stack, 6, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean moved = false;
                for (int i = 0; i < 6; i++) {
                    Slot implantSlot = this.slots.get(i);
                    if (implantSlot instanceof ImplantSlot implant && implant.mayPlace(stack)) {
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
        if (!(closingPlayer instanceof ServerPlayer serverPlayer)) return;

        implantInventory.saveToPlayer();

        targetPlayer.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
            CompoundTag data = implants.serializeNBT();

            if (!this.player.equals(this.targetPlayer)) {
                NetworkHandle.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new SyncImplantPacket(targetPlayer.getUUID(), data)
                );

                if (targetPlayer instanceof ServerPlayer targetServerPlayer) {
                    NetworkHandle.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> targetServerPlayer),
                            new SyncImplantPacket(targetPlayer.getUUID(), data)
                    );
                }
            } else {
                NetworkHandle.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new SyncImplantPacket(targetPlayer.getUUID(), data)
                );
            }
        });
    }

    public static class ImplantSlot extends SlotItemHandler {
        private final Item allowedItem;

        public ImplantSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Item allowedItem) {
            super(itemHandler, index, xPosition, yPosition);
            this.allowedItem = allowedItem;
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() == allowedItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public static class ImplantInventory extends ItemStackHandler {
        private final Player player;
        /** true solo si al abrir el menú se pudo leer la capability del jugador. */
        private boolean loadedFromCapability = false;

        public ImplantInventory(Player player) {
            super(6);
            this.player = player;
            loadFromPlayer();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;   // un implante por ranura (evita apilado vía shift-click / insertItem)
        }

        private void loadFromPlayer() {
            player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
                for (PlayerImplantsCapability.ImplantType type : PlayerImplantsCapability.ImplantType.values()) {
                    setStackInSlot(type.getIndex(), implants.getImplant(type));
                }
                loadedFromCapability = true;
            });
        }

        public void saveToPlayer() {
            // Nunca sobrescribir la capability si nunca se llegó a cargar de ella: hacerlo
            // escribiría 6 ranuras vacías encima de implantes reales (bug de "desaparecen").
            if (!loadedFromCapability) {
                return;
            }
            player.getCapability(PlayerImplantsCapability.PLAYER_IMPLANTS).ifPresent(implants -> {
                for (PlayerImplantsCapability.ImplantType type : PlayerImplantsCapability.ImplantType.values()) {
                    ItemStack stack = getStackInSlot(type.getIndex());
                    implants.setImplant(type, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                }
            });
        }
    }
}