package com.sporeadds.sporeaddsmod.client.actionwheel;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ActionWheelProvider {
    List<ActionWheelOption> getActionWheelOptions(Player player, ItemStack stack);
}