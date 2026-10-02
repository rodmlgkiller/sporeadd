package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import com.sporeadds.sporeaddsmod.blocks.modblocks;
import com.sporeadds.sporeaddsmod.client.screen.MedicBlockContructorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class MedicBlockCrafterEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(2);

    private int cooldown = -1;
    private boolean isCrafting = false;

    // 👇 Ahora el ContainerData
    public final ContainerData data;

    public MedicBlockCrafterEntity(BlockPos pos, BlockState state) {
        super(modblocksentity.MEDIC_BLOCK_CONSTRUCTOR_ENTITY.get(), pos, state);

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> MedicBlockCrafterEntity.this.cooldown;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) MedicBlockCrafterEntity.this.cooldown = value;
            }

            @Override
            public int getCount() {
                return 1; // solo sincronizamos cooldown
            }
        };
    }


    public static void tick(Level level, BlockPos pos, BlockState state, MedicBlockCrafterEntity be) {
        if (level.isClientSide) return;

        if (be.cooldown > 0) {
            be.cooldown--;
            be.setChanged(); // marca cambios
            if (be.cooldown == 0) {
                be.finishCrafting(level, pos, state);
            }
        }
    }

    public boolean hasRequiredItems() {
        return !inventory.getStackInSlot(0).isEmpty() && !inventory.getStackInSlot(1).isEmpty();
    }

    public void startCrafting() {
        if (!isCrafting() && hasRequiredItems()) {
            cooldown = 20 * 60 * 15; // 15 minutos
            isCrafting = true;
            setChanged();
        }
    }

    private void finishCrafting(Level level, BlockPos pos, BlockState state) {
        inventory.extractItem(0, 1, false);
        inventory.extractItem(1, 1, false);

        level.setBlock(pos, modblocks.MEDIC_BLOCK.get().defaultBlockState(), 3);

        cooldown = -1;
        isCrafting = false;
        setChanged();
    }

    public boolean isCrafting() {
        return cooldown > 0;
    }

    public int getCooldown() {
        return cooldown;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Cooldown", cooldown);
        tag.putBoolean("IsCrafting", isCrafting);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        cooldown = tag.getInt("Cooldown");
        isCrafting = tag.getBoolean("IsCrafting");
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Medic Block Constructor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        // 👇 pasamos data para que el menú reciba cooldown sincronizado
        return new MedicBlockContructorMenu(id, inv, this, this.data);
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    public ContainerData getContainerData() {
        return data;
    }
}
