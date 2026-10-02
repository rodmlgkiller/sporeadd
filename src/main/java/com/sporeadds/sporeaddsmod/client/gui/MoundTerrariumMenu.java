package com.sporeadds.sporeaddsmod.client.gui;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MoundTerrariumBlockEntity;
import com.sporeadds.sporeaddsmod.blocks.modblocks;
import com.sporeadds.sporeaddsmod.client.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class MoundTerrariumMenu extends AbstractContainerMenu {

    private final MoundTerrariumBlockEntity blockEntity;
    private final Player playerEntity;
    private final IItemHandler playerInventory;
    private final ContainerData data; // Para sincronizar las variables a la Screen

    // Constructor para el cliente
    public MoundTerrariumMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, (MoundTerrariumBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }

    // Constructor para el servidor
    public MoundTerrariumMenu(int id, Inventory inv, MoundTerrariumBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MOUND_TERRARIUM_MENU.get(), id);
        this.blockEntity = entity;
        this.playerEntity = inv.player;
        this.playerInventory = new InvWrapper(inv);
        this.data = data;

        // Obtenemos el inventario del bloque
        IItemHandler blockInventory = this.blockEntity.getCapability(net.neoforged.neoforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);

        // Slot 0: Biomass (Solo acepta spore:biomass) - X=19, Y=25
        this.addSlot(new SlotItemHandler(blockInventory, 0, 19, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                return id != null && id.toString().equals("spore:biomass");
            }
        });

        // Slot 1: Botellas vacías (Solo acepta botellas de cristal) - X=80, Y=76
        this.addSlot(new SlotItemHandler(blockInventory, 1, 80, 76) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.GLASS_BOTTLE);
            }
        });

        // Slot 2: Resultado (No se puede meter nada) - X=80, Y=25
        this.addSlot(new SlotItemHandler(blockInventory, 2, 80, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Slot de salida
            }
        });

        // Añadir inventario del jugador
        layoutPlayerInventorySlots(8, 97);

        // Registramos la data para sincronización
        this.addDataSlots(data);
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new SlotItemHandler(playerInventory, col + row * 9 + 9, leftCol + col * 18, topRow + row * 18));
            }
        }
        int hotbarY = topRow + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new SlotItemHandler(playerInventory, col, leftCol + col * 18, hotbarY));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemstack1.getItem());

            if (index < 3) {
                if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                if (itemId != null && itemId.toString().equals("spore:biomass")) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) return ItemStack.EMPTY;
                }
                else if (itemstack1.is(Items.GLASS_BOTTLE)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) return ItemStack.EMPTY;
                }
                else if (index < 3 + 27) {
                    if (!this.moveItemStackTo(itemstack1, 3 + 27, this.slots.size(), false)) return ItemStack.EMPTY;
                } else if (index >= 3 + 27 && index < this.slots.size()) {
                    if (!this.moveItemStackTo(itemstack1, 3, 3 + 27, false)) return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, modblocks.MOUND_TERRARIUM.get());
    }

    // --- GETTERS PARA LA SCREEN ---
    public int getHp() { return this.data.get(0); }
    public int getStomach() { return this.data.get(1); }
    public int getScent() { return this.data.get(2); }
}