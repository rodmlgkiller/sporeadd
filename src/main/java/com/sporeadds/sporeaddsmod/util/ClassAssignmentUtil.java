package com.sporeadds.sporeaddsmod.util;

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
import net.minecraftforge.registries.ForgeRegistries;

public final class ClassAssignmentUtil {

    private static final ResourceLocation GAS_MASK_ID = new ResourceLocation("spore", "gas_mask");

    private ClassAssignmentUtil() {
    }

    public static boolean applyClass(ServerPlayer player, String requestedIdentifier, String subclass) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).map(data -> {

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
            // Starting gear (Medic kit, Scientist scalpel, ...) is configurable: see StartingGearConfig.
            ClassStartingGear.onClassChanged(player, previousIdentifier, requestedIdentifier);
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

    public static void equipMedicMask(ServerPlayer player) {
        Item gasMaskItem = ForgeRegistries.ITEMS.getValue(GAS_MASK_ID);

        if (gasMaskItem != null) {
            ItemStack gasMask = new ItemStack(gasMaskItem);
            gasMask.setTag(buildGasMaskNbt());
            player.setItemSlot(EquipmentSlot.HEAD, gasMask);
        }
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