package com.sporeadds.sporeaddsmod.items;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoreMoundLocatorItem extends Item {
    public CoreMoundLocatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.CORE_MOUND_LOCATOR_REQUIRES_ORIGIN.get(), "scientist"));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.core_locator.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.core_locator.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.core_locator.range").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.sporeadds.core_locator.cost").withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.sporeadds.core_locator.usage").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private boolean hasScientistClass(ServerPlayer sp) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(sp)
                .map(data -> "scientist".equals(data.getIdentifier()))
                .orElse(false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {

            boolean canUse = true;
            if (SporeAddsConfig.CORE_MOUND_LOCATOR_REQUIRES_ORIGIN.get()) {
                canUse = hasScientistClass(sp);
            }

            if (canUse) {
                int totalRedstone = 0;
                for (ItemStack invStack : player.getInventory().items) {
                    if (invStack.getItem() == Items.REDSTONE_BLOCK) {
                        totalRedstone += invStack.getCount();
                    }
                }

                if (totalRedstone < 30) {
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.core_locator.not_enough_redstone", 30)
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }

                int redstoneToRemove = 30;
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

                ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath("spore", "signal");
                net.minecraft.sounds.SoundEvent signalSound = BuiltInRegistries.SOUND_EVENT.get(soundId);
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

                ServerLevel serverLevel = (ServerLevel) level;
                List<Mob> coreMounds = new ArrayList<>();

                AABB searchArea = new AABB(
                        player.getX() - 10000, player.getY() - 256, player.getZ() - 10000,
                        player.getX() + 10000, player.getY() + 256, player.getZ() + 10000
                );

                List<Mob> allMobs = serverLevel.getEntitiesOfClass(Mob.class, searchArea);
                for (Mob mob : allMobs) {
                    if (mob.getPersistentData().getBoolean("core")) {
                        coreMounds.add(mob);
                    }
                }

                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.core_locator.report_header")
                                .withStyle(ChatFormatting.GOLD),
                        false
                );
                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.core_locator.report_total", coreMounds.size())
                                .withStyle(ChatFormatting.GRAY),
                        false
                );

                if (coreMounds.isEmpty()) {
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.core_locator.report_none")
                                    .withStyle(ChatFormatting.RED),
                            false
                    );
                } else {
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.core_locator.report_coordinates")
                                    .withStyle(ChatFormatting.GREEN),
                            false
                    );

                    for (int i = 0; i < coreMounds.size(); i++) {
                        Mob mound = coreMounds.get(i);
                        var pos = mound.position();

                        sp.displayClientMessage(
                                Component.translatable(
                                        "message.sporeadds.core_locator.entry",
                                        i + 1,
                                        String.format("%.1f", pos.x),
                                        String.format("%.1f", pos.y),
                                        String.format("%.1f", pos.z)
                                ).withStyle(ChatFormatting.GRAY),
                                false
                        );
                    }
                }

            } else {
                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.core_locator.unknown_usage")
                                .withStyle(ChatFormatting.GRAY),
                        true
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}