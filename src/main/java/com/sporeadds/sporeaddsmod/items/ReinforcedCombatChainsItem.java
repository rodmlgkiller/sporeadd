package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.entity.projectile.ChainProjectileEntity;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ReinforcedCombatChainsItem extends Item {

    private static final String TAG_CHAIN_UUID = "ChainUUID";

    public ReinforcedCombatChainsItem(Properties properties) {
        super(properties.durability(25));
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING
                || enchantment == Enchantments.MENDING
                || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(ClassTooltipUtil.classRequirement(
                SporeAddsConfig.REINFORCED_COMBAT_CHAINS_REQUIRES_ORIGIN.get(), "berserker"));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadd.reinforced_combat_chains.line1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadd.reinforced_combat_chains.line2").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.sporeadd.reinforced_combat_chains.line3").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("tooltip.sporeadd.reinforced_combat_chains.line4").withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.sporeadd.reinforced_combat_chains.line5").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.translatable("tooltip.sporeadd.reinforced_combat_chains.line6").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.sporeadd.reinforced_combat_chains.line7").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.sporeadd.reinforced_combat_chains.line8").withStyle(ChatFormatting.WHITE));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadd.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack currentStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (SporeAddsConfig.REINFORCED_COMBAT_CHAINS_REQUIRES_ORIGIN.get()
                    && !SporeClassUtil.hasClass(player, "berserker")) {
                if (player instanceof ServerPlayer sp) {
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadd.reinforced_combat_chains.wrong_class")
                                    .withStyle(ChatFormatting.GRAY), true);
                }
                return InteractionResultHolder.fail(currentStack);
            }

            for (Entity e : level.getEntitiesOfClass(ChainProjectileEntity.class, player.getBoundingBox().inflate(64.0D))) {
                if (e instanceof ChainProjectileEntity chain && chain.getOwner() == player) {
                    return InteractionResultHolder.fail(currentStack);
                }
            }

            CompoundTag tag = currentStack.getOrCreateTag();
            if (!tag.hasUUID(TAG_CHAIN_UUID)) {
                tag.putUUID(TAG_CHAIN_UUID, UUID.randomUUID());
            }

            UUID chainId = tag.getUUID(TAG_CHAIN_UUID);

            if (hand == InteractionHand.MAIN_HAND) {
                ItemStack offhandItem = player.getOffhandItem().copy();

                player.setItemInHand(InteractionHand.OFF_HAND, currentStack.copy());
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

                if (!offhandItem.isEmpty() && !player.getInventory().add(offhandItem)) {
                    player.drop(offhandItem, false);
                }
            }

            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CHAIN_HIT,
                    SoundSource.PLAYERS,
                    1.2F,
                    0.9F + level.random.nextFloat() * 0.2F
            );

            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_LAND,
                    SoundSource.PLAYERS,
                    0.9F,
                    1.1F + level.random.nextFloat() * 0.15F
            );

            ChainProjectileEntity projectile = new ChainProjectileEntity(level, player, chainId);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);

            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}