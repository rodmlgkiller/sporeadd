package com.sporeadds.sporeaddsmod.client;

import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

/** Contenido (solo del jugador local) del inventario de Compounds, para el HUD de Brutality. */
public final class CompoundsClientState {

    public static final int SIZE = 6;

    private static final ItemStack[] STACKS = new ItemStack[SIZE];

    static {
        Arrays.fill(STACKS, ItemStack.EMPTY);
    }

    private CompoundsClientState() {
    }

    public static void set(ItemStack[] stacks) {
        for (int i = 0; i < SIZE; i++) {
            ItemStack s = (stacks != null && i < stacks.length && stacks[i] != null) ? stacks[i] : ItemStack.EMPTY;
            STACKS[i] = s;
        }
    }

    public static ItemStack get(int slot) {
        if (slot < 0 || slot >= SIZE) return ItemStack.EMPTY;
        return STACKS[slot];
    }

    public static void clear() {
        Arrays.fill(STACKS, ItemStack.EMPTY);
    }
}
