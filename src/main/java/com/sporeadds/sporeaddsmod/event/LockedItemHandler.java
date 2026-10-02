package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class LockedItemHandler {

    private static final String TAG_LOCKED = "SporeLocked";
    private static final String TAG_EXPIRE_TIME = "SporeExpireTime";

    public static void giveTemporaryLockedItem(Player player, ItemStack stack, long durationTicks) {
        CompoundTag tag = ItemNbt.getOrCreateTag(stack);
        tag.putBoolean(TAG_LOCKED, true);
        tag.putLong(TAG_EXPIRE_TIME, player.level().getGameTime() + durationTicks);

        player.getInventory().add(stack);
    }

    public static boolean isLocked(ItemStack stack) {
        return ItemNbt.hasTag(stack) && ItemNbt.getTag(stack).getBoolean(TAG_LOCKED);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        long currentTime = player.level().getGameTime();

        // Eliminar el item si caducó
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isLocked(stack)) {
                if (ItemNbt.getTag(stack).contains(TAG_EXPIRE_TIME) && currentTime >= ItemNbt.getTag(stack).getLong(TAG_EXPIRE_TIME)) {
                    stack.shrink(stack.getCount());
                }
            }
        }

        // Devolver el item si lo han movido a un cofre/horno (Escaneo activo)
        if (player.containerMenu != null && player.containerMenu != player.inventoryMenu) {
            for (Slot slot : player.containerMenu.slots) {
                if (slot.container != player.getInventory() && isLocked(slot.getItem())) {
                    ItemStack lockedItem = slot.getItem().copy();
                    slot.set(ItemStack.EMPTY);
                    player.getInventory().add(lockedItem);
                    player.inventoryMenu.broadcastChanges();
                }
            }
        }
    }

    // Comprobación extra de seguridad cuando el jugador va a cerrar un menú (Cofre, Mesa, etc.)
    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        // Escaneamos el contenedor que está a punto de cerrarse
        for (Slot slot : event.getContainer().slots) {
            // Si el slot no es del jugador y tiene un item bloqueado
            if (slot.container != player.getInventory() && isLocked(slot.getItem())) {
                ItemStack lockedItem = slot.getItem().copy();
                slot.set(ItemStack.EMPTY);
                player.getInventory().add(lockedItem);
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity entityItem = event.getEntity();
        if (isLocked(entityItem.getItem())) {
            event.setCanceled(true);
            event.getPlayer().getInventory().add(entityItem.getItem().copy());
            entityItem.discard();
        }
    }

    @SubscribeEvent
    public static void onPlayerDeathDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Collection<ItemEntity> drops = event.getDrops();
        List<ItemEntity> toRemove = new ArrayList<>();

        for (ItemEntity drop : drops) {
            if (isLocked(drop.getItem())) {
                toRemove.add(drop);
            }
        }
        drops.removeAll(toRemove);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player clone = event.getEntity();

        if (event.isWasDeath()) {
            for (int i = 0; i < original.getInventory().getContainerSize(); i++) {
                ItemStack stack = original.getInventory().getItem(i);
                if (isLocked(stack)) {
                    if (!ItemNbt.getTag(stack).contains(TAG_EXPIRE_TIME) || clone.level().getGameTime() < ItemNbt.getTag(stack).getLong(TAG_EXPIRE_TIME)) {
                        clone.getInventory().add(stack.copy());
                    }
                }
            }
        }
    }

    // Evita interactuar de manera general con entidades (como Item Frames o Armor Stands)
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isLocked(event.getItemStack())) {
            if (event.getTarget() instanceof ItemFrame || event.getTarget() instanceof ArmorStand) {
                event.setCanceled(true);
            }
        }
    }

    // Evita interacciones específicas (necesario para las hitbox de los Armor Stands)
    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isLocked(event.getItemStack())) {
            if (event.getTarget() instanceof ItemFrame || event.getTarget() instanceof ArmorStand) {
                event.setCanceled(true);
            }
        }
    }
}