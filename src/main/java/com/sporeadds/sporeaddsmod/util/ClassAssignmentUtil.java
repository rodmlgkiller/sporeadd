package com.sporeadds.sporeaddsmod.util;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.minecraft.core.registries.BuiltInRegistries;

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
        Item gasMaskItem = BuiltInRegistries.ITEM.get(GAS_MASK_ID);

        if (gasMaskItem != null) {
            ItemStack gasMask = new ItemStack(gasMaskItem);
            com.sporeadds.sporeaddsmod.util.GasMaskFactory.decorate(gasMask, player.level().registryAccess(), true);
            player.setItemSlot(EquipmentSlot.HEAD, gasMask);
        }
    }

}