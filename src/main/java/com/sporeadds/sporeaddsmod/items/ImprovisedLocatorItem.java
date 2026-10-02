package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class ImprovisedLocatorItem extends Item {
    public ImprovisedLocatorItem(Properties properties) {
        super(properties);
    }

    private static class SporeClassUtil {
        public static boolean hasClass(Player player, String classId) {
            return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                    .map(data -> classId.equals(data.getIdentifier()))
                    .orElse(false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.IMPROVISED_LOCATOR_REQUIRES_ORIGIN.get(), "scientist"));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.imp_locator.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.imp_locator.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.imp_locator.cost").withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.sporeadds.imp_locator.usage").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {

            boolean canUse = true;
            if (SporeAddsConfig.IMPROVISED_LOCATOR_REQUIRES_ORIGIN.get()) {
                canUse = SporeClassUtil.hasClass(sp, "scientist");
            }

            if (canUse) {
                int totalRedstone = 0;
                for (ItemStack invStack : player.getInventory().items) {
                    if (invStack.getItem() == Items.REDSTONE_BLOCK) {
                        totalRedstone += invStack.getCount();
                    }
                }

                if (totalRedstone < 10) {
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.imp_locator.not_enough_redstone", 10)
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }

                int redstoneToRemove = 10;
                for (ItemStack invStack : player.getInventory().items) {
                    if (invStack.getItem() == Items.REDSTONE_BLOCK && redstoneToRemove > 0) {
                        int count = invStack.getCount();
                        if (count > redstoneToRemove) {
                            invStack.shrink(redstoneToRemove);
                            redstoneToRemove = 0;
                        } else {
                            redstoneToRemove -= count;
                            invStack.setCount(0);
                        }
                    }
                }

                ResourceLocation soundId = new ResourceLocation("spore", "signal");
                net.minecraft.sounds.SoundEvent signalSound =
                        net.minecraftforge.registries.ForgeRegistries.SOUND_EVENTS.getValue(soundId);

                if (signalSound != null) {
                    level.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            signalSound,
                            net.minecraft.sounds.SoundSource.PLAYERS,
                            3.0F,
                            1.0F
                    );
                }

                double minDist = Double.MAX_VALUE;
                ServerPlayer nearest = null;

                for (ServerPlayer other : ((ServerLevel) level).players()) {
                    if (other == sp) continue;

                    if (SporeClassUtil.hasClass(other, "kommandant")) {
                        double dist = sp.distanceTo(other);
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = other;
                        }
                    }
                }

                if (nearest != null) {
                    var pos = nearest.position();
                    sp.displayClientMessage(
                            Component.translatable(
                                    "message.sporeadds.imp_locator.found_kommandant",
                                    String.format("%.1f", pos.x),
                                    String.format("%.1f", pos.y),
                                    String.format("%.1f", pos.z)
                            ).withStyle(ChatFormatting.AQUA),
                            false
                    );
                } else {
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.imp_locator.not_found")
                                    .withStyle(ChatFormatting.RED),
                            false
                    );
                }

            } else {
                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.imp_locator.unknown_usage")
                                .withStyle(ChatFormatting.GRAY),
                        true
                );
            }

            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}