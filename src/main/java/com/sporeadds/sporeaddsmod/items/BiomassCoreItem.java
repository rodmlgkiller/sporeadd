package com.sporeadds.sporeaddsmod.items;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
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

    public BiomassCoreItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.DARK_RED);
    }

    private boolean hasKommandantClass(Player player) {
        if (!(player instanceof ServerPlayer sp)) return false;

        return sp.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equals(data.getIdentifier()))
                .orElse(false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.BIOMASS_CORE_REQUIRES_ORIGIN.get(), "kommandant"));

        String currentMode = stack.getOrCreateTag().getString("MoldMode");
        if (currentMode.isEmpty()) currentMode = "weapon";

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.biomass_core.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.biomass_core.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
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

        CompoundTag tag = coreStack.getOrCreateTag();
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
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);

            return InteractionResultHolder.success(coreStack);
        }

        final int[] playerLevel = {0};
        player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
            playerLevel[0] = levelCap.getLevel();
        });

        int plvl = playerLevel[0];
        boolean isLevel9 = (plvl >= 9);

        ItemStack moldedItem = ItemStack.EMPTY;

        switch (currentMode) {
            case "pickaxe":
                moldedItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "combat_pickaxe")));
                if (plvl > 0) {
                    if (!isLevel9) addEnch(moldedItem, Enchantments.UNBREAKING, Math.min(plvl, 5));
                    addEnch(moldedItem, Enchantments.BLOCK_EFFICIENCY, Math.min(plvl, 5));
                }
                break;

            case "shovel":
                moldedItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "combat_shovel")));
                if (plvl > 0) {
                    if (!isLevel9) addEnch(moldedItem, Enchantments.UNBREAKING, Math.min(plvl, 5));
                    addEnch(moldedItem, Enchantments.BLOCK_EFFICIENCY, Math.min(plvl, 5));
                }
                break;

            case "shield":
                moldedItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "shield")));
                if (plvl > 0 && !isLevel9) {
                    addEnch(moldedItem, Enchantments.UNBREAKING, plvl);
                }
                break;

            case "ranged":
                boolean isBow = RAND.nextBoolean();
                if (isBow) {
                    moldedItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "infected_bow")));
                    if (plvl > 0 && !isLevel9) addEnch(moldedItem, Enchantments.UNBREAKING, Math.min(plvl, 5));

                    if (plvl >= 5) addEnch(moldedItem, Enchantments.INFINITY_ARROWS, 1);
                    if (plvl > 4) addEnch(moldedItem, Enchantments.POWER_ARROWS, plvl - 4);
                } else {
                    moldedItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "infected_crossbow")));
                    if (plvl > 0 && !isLevel9) addEnch(moldedItem, Enchantments.UNBREAKING, Math.min(plvl, 5));
                    if (plvl > 0) addEnch(moldedItem, Enchantments.QUICK_CHARGE, Math.min(plvl, 4));
                    if (plvl >= 5) addEnch(moldedItem, Enchantments.MULTISHOT, 1);
                }
                break;

            case "weapon":
            default:
                if (plvl <= 4) {
                    moldedItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "knife")));
                    if (plvl > 0) {
                        addEnch(moldedItem, Enchantments.UNBREAKING, plvl);
                        addEnch(moldedItem, Enchantments.SHARPNESS, plvl);
                    }
                } else {
                    ResourceLocation weaponRL = WEAPON_POOL.get(RAND.nextInt(WEAPON_POOL.size()));
                    moldedItem = new ItemStack(BuiltInRegistries.ITEM.get(weaponRL));

                    int enchLvl = plvl - 4;

                    if (enchLvl > 0) {
                        addEnch(moldedItem, Enchantments.SHARPNESS, enchLvl);
                        if (!isLevel9) addEnch(moldedItem, Enchantments.UNBREAKING, enchLvl);
                    }
                }
                break;
        }

        if (moldedItem.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.sporeadds.biomass_core.failed")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(coreStack);
        }

        if (isLevel9) {
            CompoundTag nbt = moldedItem.getOrCreateTag();
            nbt.putBoolean("Unbreakable", true);
        }

        addEnch(moldedItem, Enchantments.VANISHING_CURSE, 1);

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

    private void addEnch(ItemStack stack, Enchantment ench, int level) {
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        map.put(ench, level);
        EnchantmentHelper.setEnchantments(map, stack);
    }
}