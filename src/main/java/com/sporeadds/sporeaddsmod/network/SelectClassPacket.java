package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.ConfigItems;
import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.data.ClassPopulationData;
import com.sporeadds.sporeaddsmod.items.TrainingBookItem;
import com.sporeadds.sporeaddsmod.util.ClassAssignmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class SelectClassPacket {

    private final String identifier;
    private final InteractionHand hand;

    public SelectClassPacket(String identifier, InteractionHand hand) {
        this.identifier = identifier;
        this.hand = hand;
    }

    public static void encode(SelectClassPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.identifier);
        buffer.writeEnum(packet.hand);
    }

    public static SelectClassPacket decode(FriendlyByteBuf buffer) {
        return new SelectClassPacket(buffer.readUtf(), buffer.readEnum(InteractionHand.class));
    }

    public static void handle(SelectClassPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            handleServer(player, packet.identifier.toLowerCase(), packet.hand);
        });
        context.setPacketHandled(true);
    }

    private static void handleServer(ServerPlayer player, String requestedIdentifier, InteractionHand hand) {
        if (!SporeIdentifierData.VALID_IDS.contains(requestedIdentifier) || requestedIdentifier.equals("none")) {
            fail(player, "message.sporeadd.training_book.error.invalid_identifier");
            return;
        }

        if (!SporeAddsConfig.isClassEnabledInTrainingBook(requestedIdentifier)) {
            fail(player, "message.sporeadd.training_book.error.class_disabled");
            return;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof TrainingBookItem)) {
            fail(player, "message.sporeadd.training_book.error.not_holding_book");
            return;
        }

        if (TrainingBookItem.getUsesRemaining(stack) <= 0) {
            fail(player, "message.sporeadd.training_book.error.no_uses");
            return;
        }

        SporeIdentifierData data = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).resolve().orElse(null);
        if (data == null) {
            return;
        }

        if (SporeAddsConfig.TRAINING_BOOK_KOMMANDANT_LOCKED.get() && "kommandant".equalsIgnoreCase(data.getIdentifier())) {
            fail(player, "message.sporeadd.training_book.error.kommandant_locked");
            return;
        }

        if (requestedIdentifier.equalsIgnoreCase(data.getIdentifier())) {
            fail(player, "message.sporeadd.training_book.error.same_class");
            return;
        }

        int maxSlots = SporeAddsConfig.getTrainingBookMaxSlots(requestedIdentifier);
        if (maxSlots >= 0) {
            int currentCount = ClassPopulationData.get(player.server).countClass(requestedIdentifier);
            if (currentCount >= maxSlots) {
                fail(player, "message.sporeadd.training_book.error.no_slots");
                return;
            }
        }

        // class_gain.training_book_requirements: optional extra price of becoming a Kommandant (default: none).
        boolean kommandantRequested = "kommandant".equalsIgnoreCase(requestedIdentifier);
        List<ConfigItems.Entry> extraCost = kommandantRequested
                ? ConfigItems.parseAll(KommandantPowersConfig.BOOK_REQUIRED_ITEMS.get())
                : List.of();
        int extraXpLevels = kommandantRequested ? KommandantPowersConfig.BOOK_REQUIRED_XP_LEVELS.get() : 0;

        if (!extraCost.isEmpty() && !ConfigItems.has(player, extraCost)) {
            player.sendSystemMessage(Component.translatableWithFallback(
                    "message.sporeadd.training_book.error.missing_items",
                    "You do not carry what the hive demands of a new Kommandant.").withStyle(ChatFormatting.RED));
            return;
        }
        if (extraXpLevels > 0 && player.experienceLevel < extraXpLevels) {
            player.sendSystemMessage(Component.translatableWithFallback(
                    "message.sporeadd.training_book.error.missing_xp",
                    "You need %s experience levels to become a Kommandant.", extraXpLevels).withStyle(ChatFormatting.RED));
            return;
        }

        if (!TrainingBookItem.consumeUse(stack)) {
            fail(player, "message.sporeadd.training_book.error.no_uses");
            return;
        }

        boolean success = ClassAssignmentUtil.applyClass(player, requestedIdentifier, "none");
        if (!success) {
            TrainingBookItem.setUsesRemaining(stack, TrainingBookItem.getUsesRemaining(stack) + 1);
            fail(player, "message.sporeadd.training_book.error.invalid_identifier");
            return;
        }

        // The class change went through: now the price is paid.
        if (!extraCost.isEmpty()) {
            ConfigItems.take(player, extraCost);
        }
        if (extraXpLevels > 0) {
            player.giveExperienceLevels(-extraXpLevels);
        }

        SyncClassCountsPacket.broadcast(player.server);
    }

    private static void fail(ServerPlayer player, String translationKey) {
        player.sendSystemMessage(Component.translatable(translationKey).withStyle(ChatFormatting.RED));
    }
}
