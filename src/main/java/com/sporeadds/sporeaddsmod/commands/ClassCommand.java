package com.sporeadds.sporeaddsmod.commands;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.Powers.Levelstats;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.util.OriginSyncUtil;
import com.sporeadds.sporeaddsmod.util.SporeIdentifierUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = "sporeadd")
public class ClassCommand {

    private static final List<String> KOMMANDANT_SUBCLASSES = List.of("caustic", "none", "abyssal", "gluttonous");
    private static final ResourceLocation GAS_MASK_ID = ResourceLocation.fromNamespaceAndPath("spore", "gas_mask");

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("class")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("identifier", StringArgumentType.word())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggest(SporeIdentifierData.VALID_IDS, builder))
                                .executes(ctx -> setClassAction(ctx, "none", false))
                                .then(Commands.literal("animated")
                                        .executes(ctx -> setClassAction(ctx, "none", true)))
                                .then(Commands.argument("subclass", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String currentClass = context.getArgument("identifier", String.class);
                                            if ("kommandant".equalsIgnoreCase(currentClass)) {
                                                return SharedSuggestionProvider.suggest(KOMMANDANT_SUBCLASSES, builder);
                                            }
                                            return SharedSuggestionProvider.suggest(List.of("none"), builder);
                                        })
                                        .executes(ctx -> setClassAction(ctx, StringArgumentType.getString(ctx, "subclass"), false))
                                        .then(Commands.literal("animated")
                                                .executes(ctx -> setClassAction(ctx, StringArgumentType.getString(ctx, "subclass"), true)))
                                )
                        )
                )
        );
    }

    private static int setClassAction(CommandContext<CommandSourceStack> context, String subclass, boolean animated) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");
        String requestedIdentifier = StringArgumentType.getString(context, "identifier");

        SporeIdentifierProvider.SPORE_IDENTIFIER.get(targetPlayer).ifPresent(data -> {

            String previousIdentifier = data.getIdentifier();

            if (requestedIdentifier.equalsIgnoreCase(previousIdentifier) && subclass.equalsIgnoreCase(data.getSubclass())) {
                source.sendFailure(Component.translatable(
                        "command.sporeadd.class.error.already_has",
                        requestedIdentifier + " (" + subclass + ")"
                ));
                return;
            }

            boolean wasSuccessful = SporeIdentifierUtil.setIdentifierAndSync(targetPlayer, requestedIdentifier);

            if (wasSuccessful || requestedIdentifier.equalsIgnoreCase(previousIdentifier)) {

                SporeIdentifierUtil.setSubclassAndSync(targetPlayer, subclass);
                Levelstats.forceReapplyStats(targetPlayer);

                assignTeamByClass(targetPlayer, requestedIdentifier);
                handleMedicItem(targetPlayer, previousIdentifier, requestedIdentifier);
                handleScientistItem(targetPlayer, previousIdentifier, requestedIdentifier);
                OriginSyncUtil.applyForIdentifier(targetPlayer, requestedIdentifier);

                String displayName = subclass.equalsIgnoreCase("none")
                        ? requestedIdentifier
                        : requestedIdentifier + " - " + subclass;

                source.sendSuccess(() ->
                                Component.translatable(
                                        "command.sporeadd.class.set.success",
                                        displayName,
                                        targetPlayer.getScoreboardName()
                                ),
                        true
                );

                targetPlayer.sendSystemMessage(
                        Component.translatable(
                                "message.sporeadd.class.updated",
                                displayName
                        )
                );

                // Por defecto /class NO lanza la ceremonia de la colmena (sin animación ni teletransporte).
                // Con el literal "animated" sí: /class <jugador> kommandant [<subclase>] animated
                if (animated) {
                    com.sporeadds.sporeaddsmod.hive.HiveDownedManager.maybeBeginVoluntaryInduction(
                            targetPlayer, requestedIdentifier, previousIdentifier);
                }

            } else {
                source.sendFailure(
                        Component.translatable(
                                "command.sporeadd.class.error.invalid_identifier",
                                requestedIdentifier
                        )
                );
            }
        });

        return 1;
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
            ItemStack scalpel = new ItemStack(ModItems.SCALPEL.get());
            player.getInventory().add(scalpel);
        }
    }

    private static void removeScalpel(ServerPlayer player) {
        player.getInventory().items.removeIf(stack -> stack.getItem() == ModItems.SCALPEL.get());
        player.getInventory().offhand.removeIf(stack -> stack.getItem() == ModItems.SCALPEL.get());
    }

    private static void giveMedicItems(ServerPlayer player) {
        equipMedicMask(player);

        ItemStack injector = new ItemStack(ModItems.INJECTOR.get());
        player.getInventory().add(injector);

        ItemStack bandages = new ItemStack(ModItems.THROWABLE_BANDAGES.get(), 16);
        player.getInventory().add(bandages);
    }

    public static void equipMedicMask(ServerPlayer player) {
        Item gasMaskItem = BuiltInRegistries.ITEM.get(GAS_MASK_ID);

        if (gasMaskItem != null) {
            ItemStack gasMask = new ItemStack(gasMaskItem);
            com.sporeadds.sporeaddsmod.util.GasMaskFactory.decorate(gasMask, player.level().registryAccess(), SporeAddsConfig.MEDIC_GAS_MASK_CURSE_OF_BINDING.get());
            player.setItemSlot(EquipmentSlot.HEAD, gasMask);
        }
    }

    private static boolean isGasMask(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == BuiltInRegistries.ITEM.get(GAS_MASK_ID);
    }

    /**
     * Añade o quita Curse of Binding de una máscara de gas ya equipada, según
     * {@link SporeAddsConfig#MEDIC_GAS_MASK_CURSE_OF_BINDING}, sin tocar el resto de su NBT
     * (nombre, lore, Vanishing Curse, modificadores de atributo, Unbreakable...).
     */
    public static void syncGasMaskCurseOfBinding(ItemStack gasMask, net.minecraft.core.HolderLookup.Provider access) {
        if (!isGasMask(gasMask)) return;

        boolean shouldHaveCurse = SporeAddsConfig.MEDIC_GAS_MASK_CURSE_OF_BINDING.get();
        boolean hasCurse = com.sporeadds.sporeaddsmod.util.EnchantUtil.level(gasMask, access, Enchantments.BINDING_CURSE) > 0;

        if (shouldHaveCurse == hasCurse) return;

        if (shouldHaveCurse) {
            com.sporeadds.sporeaddsmod.util.EnchantUtil.add(gasMask, access, Enchantments.BINDING_CURSE, 1);
        } else {
            com.sporeadds.sporeaddsmod.util.EnchantUtil.remove(gasMask, access, Enchantments.BINDING_CURSE);
        }
    }

    /** Igual, pero sobre lo que el jugador lleve puesto en la cabeza (si es una máscara de gas). */
    public static void syncGasMaskCurseOfBinding(ServerPlayer player) {
        syncGasMaskCurseOfBinding(player.getItemBySlot(EquipmentSlot.HEAD), player.level().registryAccess());
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

    
}