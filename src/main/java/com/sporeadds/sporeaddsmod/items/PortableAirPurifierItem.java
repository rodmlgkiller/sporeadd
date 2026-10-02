package com.sporeadds.sporeaddsmod.items;

import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PortableAirPurifierItem extends Item {

    public PortableAirPurifierItem(Properties properties) {
        super(properties.durability(3).setNoRepair());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.isEnchanted();
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }



    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            AABB bounds = player.getBoundingBox().inflate(8.0D);
            List<ScentEntity> scents = serverLevel.getEntitiesOfClass(ScentEntity.class, bounds);

            boolean absorbedAny = false;

            for (ScentEntity scent : scents) {
                PurifierEventHandler.startAbsorption(
                        serverLevel,
                        scent.getX(),
                        scent.getY() + scent.getBbHeight() / 2.0D,
                        scent.getZ(),
                        player.getUUID(),
                        scent.getOvercharged(),
                        15
                );

                serverLevel.playSound(
                        null,
                        scent.getX(), scent.getY(), scent.getZ(),
                        SoundEvents.BREWING_STAND_BREW,
                        SoundSource.PLAYERS,
                        0.25F,
                        1.2F + serverLevel.random.nextFloat() * 0.35F
                );

                scent.discard();
                absorbedAny = true;
            }

            if (absorbedAny) {
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
                player.getCooldowns().addCooldown(this, 300);
                level.playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.BEACON_POWER_SELECT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.5F
                );
            } else {
                level.playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.DISPENSER_FAIL,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        int charge = stack.getMaxDamage() - stack.getDamageValue();

        if (charge > 0) {
            tooltip.add(Component.translatable("tooltip.sporeadd.air_purifier.charge", charge, stack.getMaxDamage())
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadd.air_purifier.charge_depleted", 0, stack.getMaxDamage())
                    .withStyle(ChatFormatting.DARK_RED));
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadd.air_purifier.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadd.air_purifier.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadd.air_purifier.use").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.sporeadd.air_purifier.warning").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}