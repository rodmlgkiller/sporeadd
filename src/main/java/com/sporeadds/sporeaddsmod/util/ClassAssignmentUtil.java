package com.sporeadds.sporeaddsmod.util;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.Powers.Levelstats;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;

public final class ClassAssignmentUtil {

    private static final ResourceLocation GAS_MASK_ID = ResourceLocation.fromNamespaceAndPath("spore", "gas_mask");

    private ClassAssignmentUtil() {
    }

    public static boolean applyClass(ServerPlayer player, String requestedIdentifier, String subclass) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).map(data -> {

            String previousIdentifier = data.getIdentifier();

            if (requestedIdentifier.equalsIgnoreCase(previousIdentifier) && subclass.equalsIgnoreCase(data.getSubclass())) {
                return false;
            }

            boolean wasSuccessful = SporeIdentifierUtil.setIdentifierAndSync(player, requestedIdentifier);

            if (!wasSuccessful && !requestedIdentifier.equalsIgnoreCase(previousIdentifier)) {
                return false;
            }

            SporeIdentifierUtil.setSubclassAndSync(player, subclass);
            Levelstats.forceReapplyStats(player);

            assignTeamByClass(player, requestedIdentifier);
            handleMedicItem(player, previousIdentifier, requestedIdentifier);
            handleScientistItem(player, previousIdentifier, requestedIdentifier);
            OriginSyncUtil.applyForIdentifier(player, requestedIdentifier);

            String displayName = subclass.equalsIgnoreCase("none")
                    ? requestedIdentifier
                    : requestedIdentifier + " - " + subclass;

            player.sendSystemMessage(
                    Component.translatable("message.sporeadd.class.updated", displayName)
            );

            // Elegir kommandant -> ceremonia de la colmena (pantalla oscura + "Very well..." + verwa).
            com.sporeadds.sporeaddsmod.hive.HiveDownedManager.maybeBeginVoluntaryInduction(
                    player, requestedIdentifier, previousIdentifier);

            return true;
        }).orElse(false);
    }

    private static void assignTeamByClass(ServerPlayer player, String identifier) {
        ServerScoreboard scoreboard = player.server.getScoreboard();
        String teamName = "kommandant".equalsIgnoreCase(identifier) ? "spore" : "mercs";

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
        }

        scoreboard.removePlayerFromTeam(player.getScoreboardName());
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
    }

    private static void handleMedicItem(ServerPlayer player, String previousIdentifier, String newIdentifier) {
        boolean wasMedic = "medic".equalsIgnoreCase(previousIdentifier);
        boolean isMedic = "medic".equalsIgnoreCase(newIdentifier);

        if (isMedic && !wasMedic) {
            giveMedicItems(player);
        } else if (!isMedic && wasMedic) {
            removeMedicItems(player);
        }
    }

    private static void handleScientistItem(ServerPlayer player, String previousIdentifier, String newIdentifier) {
        boolean wasScientist = "scientist".equalsIgnoreCase(previousIdentifier);
        boolean isScientist = "scientist".equalsIgnoreCase(newIdentifier);

        if (isScientist && !wasScientist) {
            giveScalpel(player);
        } else if (!isScientist && wasScientist) {
            removeScalpel(player);
        }
    }

    private static void giveScalpel(ServerPlayer player) {
        boolean alreadyHasScalpel = player.getInventory().items.stream()
                .anyMatch(stack -> stack.getItem() == ModItems.SCALPEL.get())
                || player.getInventory().offhand.stream()
                .anyMatch(stack -> stack.getItem() == ModItems.SCALPEL.get());

        if (!alreadyHasScalpel) {
            player.getInventory().add(new ItemStack(ModItems.SCALPEL.get()));
        }
    }

    private static void removeScalpel(ServerPlayer player) {
        player.getInventory().items.removeIf(stack -> stack.getItem() == ModItems.SCALPEL.get());
        player.getInventory().offhand.removeIf(stack -> stack.getItem() == ModItems.SCALPEL.get());
    }

    private static void giveMedicItems(ServerPlayer player) {
        equipMedicMask(player);
        player.getInventory().add(new ItemStack(ModItems.INJECTOR.get()));
        player.getInventory().add(new ItemStack(ModItems.THROWABLE_BANDAGES.get(), 16));
    }

    public static void equipMedicMask(ServerPlayer player) {
        Item gasMaskItem = BuiltInRegistries.ITEM.get(GAS_MASK_ID);

        if (gasMaskItem != null) {
            ItemStack gasMask = new ItemStack(gasMaskItem);
            ItemNbt.setTag(gasMask, buildGasMaskNbt());
            player.setItemSlot(EquipmentSlot.HEAD, gasMask);
        }
    }

    private static void removeMedicItems(ServerPlayer player) {
        ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD);
        if (headItem.getItem() == BuiltInRegistries.ITEM.get(GAS_MASK_ID)) {
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }

        player.getInventory().items.removeIf(stack ->
                stack.getItem() == ModItems.INJECTOR.get() || stack.getItem() == ModItems.THROWABLE_BANDAGES.get());
        player.getInventory().offhand.removeIf(stack ->
                stack.getItem() == ModItems.INJECTOR.get() || stack.getItem() == ModItems.THROWABLE_BANDAGES.get());
    }

    private static CompoundTag buildGasMaskNbt() {
        CompoundTag root = new CompoundTag();

        CompoundTag display = new CompoundTag();
        display.putString("Name", "[\"\",{\"text\":\"Insanity mask\",\"italic\":false,\"color\":\"dark_red\"}]");

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf("[\"\",{\"text\":\"After seeing the effects of the infection in others you never want to experience it for yourself (You cannot remove your gas mask)\",\"color\":\"red\"}]"));
        display.put("Lore", lore);
        root.put("display", display);

        ListTag enchantments = new ListTag();

        CompoundTag bindingCurse = new CompoundTag();
        bindingCurse.putInt("lvl", 1);
        bindingCurse.putString("id", "minecraft:binding_curse");
        enchantments.add(bindingCurse);

        CompoundTag vanishingCurse = new CompoundTag();
        vanishingCurse.putInt("lvl", 1);
        vanishingCurse.putString("id", "minecraft:vanishing_curse");
        enchantments.add(vanishingCurse);

        root.put("Enchantments", enchantments);
        root.putBoolean("Unbreakable", true);

        ListTag attributeModifiers = new ListTag();

        CompoundTag armorModifier = new CompoundTag();
        armorModifier.putString("AttributeName", "minecraft:generic.armor");
        armorModifier.putString("Name", "generic.armor");
        armorModifier.putString("Slot", "head");
        armorModifier.putDouble("Amount", 4.0D);
        armorModifier.putInt("Operation", 0);
        armorModifier.put("UUID", new IntArrayTag(new int[]{154321, 245632, 356743, 467854}));
        attributeModifiers.add(armorModifier);

        CompoundTag toughnessModifier = new CompoundTag();
        toughnessModifier.putString("AttributeName", "minecraft:generic.armor_toughness");
        toughnessModifier.putString("Name", "generic.armor_toughness");
        toughnessModifier.putString("Slot", "head");
        toughnessModifier.putDouble("Amount", 3.0D);
        toughnessModifier.putInt("Operation", 0);
        toughnessModifier.put("UUID", new IntArrayTag(new int[]{564321, 675432, 786543, 897654}));
        attributeModifiers.add(toughnessModifier);

        root.put("AttributeModifiers", attributeModifiers);
        return root;
    }
}