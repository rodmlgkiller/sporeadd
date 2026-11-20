package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.OpenBiomassWheelPacket;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

public class BiomassCoreItem extends Item {

    private static final List<ResourceLocation> WEAPON_POOL = Arrays.asList(
            ResourceLocation.fromNamespaceAndPath("spore", "armads"),
            ResourceLocation.fromNamespaceAndPath("spore", "cleaver"),
            ResourceLocation.fromNamespaceAndPath("spore", "greatsword"),
            ResourceLocation.fromNamespaceAndPath("spore", "halberd"),
            ResourceLocation.fromNamespaceAndPath("spore", "mace"),
            ResourceLocation.fromNamespaceAndPath("spore", "maul"),
            ResourceLocation.fromNamespaceAndPath("spore", "rapier"),
            ResourceLocation.fromNamespaceAndPath("spore", "saber"),
            ResourceLocation.fromNamespaceAndPath("spore", "scythe"),
            ResourceLocation.fromNamespaceAndPath("spore", "sickle"),
            ResourceLocation.fromNamespaceAndPath("spore", "boomerang"),
            ResourceLocation.fromNamespaceAndPath("spore", "infected_spear")
    );

    private static final String[] MODES = {"weapon", "pickaxe", "shovel", "shield", "ranged"};
    private static final Random RAND = new Random();

    // Choices of the action wheel (biomass_core.action_wheel_enabled). A pool weapon is "weapon:<path>".
    private static final String CHOICE_KNIFE = "knife";
    private static final String CHOICE_PICKAXE = "pickaxe";
    private static final String CHOICE_SHOVEL = "shovel";
    private static final String CHOICE_SHIELD = "shield";
    private static final String CHOICE_BOW = "bow";
    private static final String CHOICE_CROSSBOW = "crossbow";
    private static final String CHOICE_WEAPON_PREFIX = "weapon:";

    public BiomassCoreItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.DARK_RED);
    }

    private boolean hasKommandantClass(Player player) {
        if (!(player instanceof ServerPlayer sp)) return false;

        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(sp)
                .map(data -> "kommandant".equals(data.getIdentifier()))
                .orElse(false);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.BIOMASS_CORE_REQUIRES_ORIGIN.get(), "kommandant"));

        String currentMode = ItemNbt.getOrCreateTag(stack).getString("MoldMode");
        if (currentMode.isEmpty()) currentMode = "weapon";

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.biomass_core.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.biomass_core.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (SporeAddsConfig.BIOMASS_CORE_ACTION_WHEEL.get()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.biomass_core.wheel_use").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(
                Component.translatable(
                        "tooltip.sporeadds.biomass_core.current_mode",
                        Component.translatable("tooltip.sporeadds.biomass_core.mode." + currentMode)
                ).withStyle(ChatFormatting.AQUA)
        );
        tooltip.add(Component.translatable("tooltip.sporeadds.biomass_core.change_mode").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.sporeadds.biomass_core.use").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack coreStack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.pass(coreStack);

        if (SporeAddsConfig.BIOMASS_CORE_REQUIRES_ORIGIN.get()) {
            if (!hasKommandantClass(player)) {
                player.displayClientMessage(
                        Component.translatable("message.sporeadds.biomass_core.unknown_usage")
                                .withStyle(ChatFormatting.GRAY),
                        true
                );
                return InteractionResultHolder.fail(coreStack);
            }
        }

        CompoundTag tag = ItemNbt.getOrCreateTag(coreStack);
        String currentMode = tag.getString("MoldMode");
        if (currentMode.isEmpty()) currentMode = "weapon";

        if (player.isShiftKeyDown()) {
            int currentIndex = Arrays.asList(MODES).indexOf(currentMode);
            int nextIndex = (currentIndex + 1) % MODES.length;
            String nextMode = MODES[nextIndex];

            tag.putString("MoldMode", nextMode);
            player.displayClientMessage(
                    Component.translatable(
                            "message.sporeadds.biomass_core.mode_changed",
                            Component.translatable("tooltip.sporeadds.biomass_core.mode." + nextMode)
                    ).withStyle(ChatFormatting.GREEN),
                    true
            );

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);

            return InteractionResultHolder.success(coreStack);
        }

        // Action wheel (off by default): instead of molding the mode's item (a random weapon / bow / crossbow) at once,
        // ask the client to show a wheel so the player picks what to create. The server decides this, so only the
        // server's config matters; the pick comes back through BiomassCoreMoldPacket.
        if (SporeAddsConfig.BIOMASS_CORE_ACTION_WHEEL.get() && player instanceof ServerPlayer wheelPlayer) {
            NetworkHandle.INSTANCE.send(PacketDistributor.PLAYER.with(() -> wheelPlayer), new OpenBiomassWheelPacket());
            return InteractionResultHolder.success(coreStack);
        }

        int plvl = getPlayerLevel(player);

        ItemStack moldedItem = ItemStack.EMPTY;

        switch (currentMode) {
            case "pickaxe":
                moldedItem = buildTool(level, "combat_pickaxe", plvl);
                break;

            case "shovel":
                moldedItem = buildTool(level, "combat_shovel", plvl);
                break;

            case "shield":
                moldedItem = buildShield(level, plvl);
                break;

            case "ranged":
                moldedItem = RAND.nextBoolean() ? buildBow(level, plvl) : buildCrossbow(level, plvl);
                break;

            case "weapon":
            default:
                if (plvl <= 4) {
                    moldedItem = buildKnife(level, plvl);
                } else {
                    moldedItem = buildPoolWeapon(level, WEAPON_POOL.get(RAND.nextInt(WEAPON_POOL.size())), plvl);
                }
                break;
        }

        return finishMolding(level, player, coreStack, moldedItem, plvl);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Action wheel support
    // ------------------------------------------------------------------------------------------------------------

    /**
     * What the wheel offers at spore level {@code plvl}: the same items the plain right click can mold, but chosen
     * instead of random (a knife up to level 4, the whole weapon pool from level 5, plus the tools, shield and ranged weapons).
     */
    public static List<String> wheelChoices(int plvl) {
        List<String> choices = new ArrayList<>();
        if (plvl <= 4) {
            choices.add(CHOICE_KNIFE);
        } else {
            for (ResourceLocation weapon : WEAPON_POOL) {
                choices.add(CHOICE_WEAPON_PREFIX + weapon.getPath());
            }
        }
        choices.add(CHOICE_PICKAXE);
        choices.add(CHOICE_SHOVEL);
        choices.add(CHOICE_SHIELD);
        choices.add(CHOICE_BOW);
        choices.add(CHOICE_CROSSBOW);
        return choices;
    }

    /** Registry id of the item a wheel choice molds (for the wheel icon); null for an unknown choice. */
    @Nullable
    public static ResourceLocation choiceItemId(String choice) {
        if (choice == null) return null;
        return switch (choice) {
            case CHOICE_KNIFE -> ResourceLocation.fromNamespaceAndPath("spore", "knife");
            case CHOICE_PICKAXE -> ResourceLocation.fromNamespaceAndPath("spore", "combat_pickaxe");
            case CHOICE_SHOVEL -> ResourceLocation.fromNamespaceAndPath("spore", "combat_shovel");
            case CHOICE_SHIELD -> ResourceLocation.fromNamespaceAndPath("spore", "shield");
            case CHOICE_BOW -> ResourceLocation.fromNamespaceAndPath("spore", "infected_bow");
            case CHOICE_CROSSBOW -> ResourceLocation.fromNamespaceAndPath("spore", "infected_crossbow");
            default -> {
                if (choice.startsWith(CHOICE_WEAPON_PREFIX)) {
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath("spore", choice.substring(CHOICE_WEAPON_PREFIX.length()));
                    yield WEAPON_POOL.contains(id) ? id : null;
                }
                yield null;
            }
        };
    }

    /** Server side of the wheel: the player picked {@code choice}; mold it from the Biomass Core they are holding. */
    public static void handleMoldChoice(ServerPlayer player, String choice) {
        if (!SporeAddsConfig.BIOMASS_CORE_ACTION_WHEEL.get()) {
            return;
        }
        InteractionHand hand = player.getMainHandItem().getItem() instanceof BiomassCoreItem
                ? InteractionHand.MAIN_HAND
                : (player.getOffhandItem().getItem() instanceof BiomassCoreItem ? InteractionHand.OFF_HAND : null);
        if (hand == null) {
            return;
        }
        ItemStack coreStack = player.getItemInHand(hand);
        BiomassCoreItem core = (BiomassCoreItem) coreStack.getItem();

        if (SporeAddsConfig.BIOMASS_CORE_REQUIRES_ORIGIN.get() && !core.hasKommandantClass(player)) {
            player.displayClientMessage(
                    Component.translatable("message.sporeadds.biomass_core.unknown_usage").withStyle(ChatFormatting.GRAY),
                    true
            );
            return;
        }

        int plvl = getPlayerLevel(player);
        if (!wheelChoices(plvl).contains(choice)) {
            return; // not offered at this level (or a bogus packet)
        }

        Level level = player.level();
        ItemStack molded = switch (choice) {
            case CHOICE_KNIFE -> buildKnife(level, plvl);
            case CHOICE_PICKAXE -> buildTool(level, "combat_pickaxe", plvl);
            case CHOICE_SHOVEL -> buildTool(level, "combat_shovel", plvl);
            case CHOICE_SHIELD -> buildShield(level, plvl);
            case CHOICE_BOW -> buildBow(level, plvl);
            case CHOICE_CROSSBOW -> buildCrossbow(level, plvl);
            default -> buildPoolWeapon(level, choiceItemId(choice), plvl);
        };

        core.finishMolding(level, player, coreStack, molded, plvl);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Building the molded item (shared by the plain right click and the wheel)
    // ------------------------------------------------------------------------------------------------------------

    private static int getPlayerLevel(Player player) {
        final int[] playerLevel = {0};
        PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelCap -> {
            playerLevel[0] = levelCap.getLevel();
        });
        return playerLevel[0];
    }

    private static ItemStack stackOf(ResourceLocation id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id));
    }

    private static ItemStack buildTool(Level level, String path, int plvl) {
        boolean isLevel9 = plvl >= 9;
        ItemStack tool = stackOf(ResourceLocation.fromNamespaceAndPath("spore", path));
        if (plvl > 0) {
            if (!isLevel9) addEnch(level, tool, Enchantments.UNBREAKING, Math.min(plvl, 5));
            addEnch(level, tool, Enchantments.EFFICIENCY, Math.min(plvl, 5));
        }
        return tool;
    }

    private static ItemStack buildShield(Level level, int plvl) {
        ItemStack shield = stackOf(ResourceLocation.fromNamespaceAndPath("spore", "shield"));
        if (plvl > 0 && plvl < 9) {
            addEnch(level, shield, Enchantments.UNBREAKING, plvl);
        }
        return shield;
    }

    private static ItemStack buildBow(Level level, int plvl) {
        boolean isLevel9 = plvl >= 9;
        ItemStack bow = stackOf(ResourceLocation.fromNamespaceAndPath("spore", "infected_bow"));
        if (plvl > 0 && !isLevel9) addEnch(level, bow, Enchantments.UNBREAKING, Math.min(plvl, 5));

        if (plvl >= 5) addEnch(level, bow, Enchantments.INFINITY, 1);
        if (plvl > 4) addEnch(level, bow, Enchantments.POWER, plvl - 4);
        return bow;
    }

    private static ItemStack buildCrossbow(Level level, int plvl) {
        boolean isLevel9 = plvl >= 9;
        ItemStack crossbow = stackOf(ResourceLocation.fromNamespaceAndPath("spore", "infected_crossbow"));
        if (plvl > 0 && !isLevel9) addEnch(level, crossbow, Enchantments.UNBREAKING, Math.min(plvl, 5));
        if (plvl > 0) addEnch(level, crossbow, Enchantments.QUICK_CHARGE, Math.min(plvl, 4));
        if (plvl >= 5) addEnch(level, crossbow, Enchantments.MULTISHOT, 1);
        return crossbow;
    }

    private static ItemStack buildKnife(Level level, int plvl) {
        ItemStack knife = stackOf(ResourceLocation.fromNamespaceAndPath("spore", "knife"));
        if (plvl > 0) {
            addEnch(level, knife, Enchantments.UNBREAKING, plvl);
            addEnch(level, knife, Enchantments.SHARPNESS, plvl);
        }
        return knife;
    }

    private static ItemStack buildPoolWeapon(Level level, ResourceLocation weaponId, int plvl) {
        boolean isLevel9 = plvl >= 9;
        ItemStack weapon = stackOf(weaponId);

        int enchLvl = plvl - 4;

        if (enchLvl > 0) {
            addEnch(level, weapon, Enchantments.SHARPNESS, enchLvl);
            if (!isLevel9) addEnch(level, weapon, Enchantments.UNBREAKING, enchLvl);
        }
        return weapon;
    }

    /** Last step of every molding: sanity check, level-9 unbreakable, vanishing curse, sound, consume the core, drop the item. */
    private InteractionResultHolder<ItemStack> finishMolding(Level level, Player player, ItemStack coreStack, ItemStack moldedItem, int plvl) {
        if (moldedItem.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.sporeadds.biomass_core.failed")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(coreStack);
        }

        if (plvl >= 9) {
            moldedItem.set(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true));
        }

        addEnch(level, moldedItem, Enchantments.VANISHING_CURSE, 1);

        ResourceLocation soundLoc = ResourceLocation.fromNamespaceAndPath("spore", "hyper_damage");
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(soundLoc),
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 2.0F);

        coreStack.shrink(1);
        ItemEntity drop = new ItemEntity(level, player.getX(), player.getY() + 1.0, player.getZ(), moldedItem);
        level.addFreshEntity(drop);

        player.displayClientMessage(
                Component.translatable("message.sporeadds.biomass_core.molded")
                        .withStyle(ChatFormatting.GOLD),
                true
        );
        return InteractionResultHolder.success(ItemStack.EMPTY);
    }

    private static void addEnch(Level world, ItemStack stack, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> ench, int enchLevel) {
        com.sporeadds.sporeaddsmod.util.EnchantUtil.add(stack, world.registryAccess(), ench, enchLevel);
    }
}
