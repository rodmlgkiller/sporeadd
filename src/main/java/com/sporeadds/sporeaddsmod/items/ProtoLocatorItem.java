package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.config.ConfigItems;
import com.sporeadds.sporeaddsmod.config.LocatorConfig;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProtoLocatorItem extends Item {
    // The cooldown (default 30 minutes, real time) and the glowing time (default 5 minutes) are read from
    // LocatorConfig at use time; the cooldown timer itself ("lastUsed") is stored per item, as it always was.

    public ProtoLocatorItem(Properties properties) {
        super(properties);
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
            if (SporeAddsConfig.PROTO_LOCATOR_REQUIRES_ORIGIN.get()) {
                canUse = hasScientistClass(sp);
            }

            if (canUse) {
                CompoundTag nbt = ItemNbt.getOrCreateTag(stack);
                long cooldownMillis = LocatorConfig.protoCooldownMillis();
                long currentTime = System.currentTimeMillis();
                long lastUsed = nbt.getLong("lastUsed");
                long timeLeft = (lastUsed + cooldownMillis) - currentTime;

                if (cooldownMillis > 0 && timeLeft > 0) {
                    sp.displayClientMessage(LocatorSupport.protoCooldownMessage(timeLeft, cooldownMillis), true);
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }

                // Optional item cost (default: none). Checked before the cooldown starts so a failed payment costs nothing.
                if (!LocatorSupport.tryPay(sp, LocatorConfig.protoCost(), LocatorConfig.PROTO_CONSUME_COST.get(), null)) {
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }

                nbt.putLong("lastUsed", currentTime);

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
                List<Mob> protoEntities = new ArrayList<>();

                AABB searchArea = new AABB(
                        player.getX() - 10000, player.getY() - 256, player.getZ() - 10000,
                        player.getX() + 10000, player.getY() + 256, player.getZ() + 10000
                );

                List<Mob> allMobs = serverLevel.getEntitiesOfClass(Mob.class, searchArea);
                for (Mob mob : allMobs) {
                    ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
                    if (entityType != null &&
                            "spore".equals(entityType.getNamespace()) &&
                            "proto".equals(entityType.getPath())) {
                        protoEntities.add(mob);
                    }
                }

                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.proto_locator.report_header")
                                .withStyle(ChatFormatting.GOLD),
                        false
                );
                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.proto_locator.report_total", protoEntities.size())
                                .withStyle(ChatFormatting.GRAY),
                        false
                );

                if (protoEntities.isEmpty()) {
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.proto_locator.report_none")
                                    .withStyle(ChatFormatting.RED),
                            false
                    );
                    Component readyAgain = LocatorSupport.protoReadyAgain(LocatorConfig.protoCooldownTicks());
                    if (readyAgain != null) {
                        sp.displayClientMessage(readyAgain, true);
                    }
                } else {
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.proto_locator.report_marked")
                                    .withStyle(ChatFormatting.GREEN),
                            false
                    );

                    for (int i = 0; i < protoEntities.size(); i++) {
                        Mob proto = protoEntities.get(i);
                        var pos = proto.position();

                        proto.addEffect(new MobEffectInstance(
                                MobEffects.GLOWING,
                                LocatorConfig.protoMarkedDurationTicks(),
                                0,
                                false,
                                true
                        ));

                        sp.displayClientMessage(
                                Component.translatable(
                                        "message.sporeadds.proto_locator.entry",
                                        i + 1,
                                        String.format("%.1f", pos.x),
                                        String.format("%.1f", pos.y),
                                        String.format("%.1f", pos.z)
                                ).withStyle(ChatFormatting.GRAY),
                                false
                        );
                    }

                    Component readyAgain = LocatorSupport.protoReadyAgain(LocatorConfig.protoCooldownTicks());
                    if (readyAgain != null) {
                        sp.displayClientMessage(readyAgain, true);
                    }
                }

            } else {
                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.proto_locator.unknown_usage")
                                .withStyle(ChatFormatting.GRAY),
                        true
                );
            }

            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag nbt = ItemNbt.getTag(stack);
        if (nbt != null && nbt.contains("lastUsed")) {
            long cooldownMillis = LocatorConfig.protoCooldownMillis();
            long currentTime = System.currentTimeMillis();
            long lastUsed = nbt.getLong("lastUsed");
            long timeLeft = (lastUsed + cooldownMillis) - currentTime;
            return cooldownMillis > 0 && timeLeft > 0;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.PROTO_LOCATOR_REQUIRES_ORIGIN.get(), "scientist"));

        CompoundTag nbt = ItemNbt.getTag(stack);
        if (nbt != null && nbt.contains("lastUsed")) {
            long cooldownMillis = LocatorConfig.protoCooldownMillis();
            long currentTime = System.currentTimeMillis();
            long lastUsed = nbt.getLong("lastUsed");
            long timeLeft = (lastUsed + cooldownMillis) - currentTime;

            if (cooldownMillis > 0 && timeLeft > 0) {
                tooltip.add(LocatorSupport.protoCooldownTooltip(timeLeft, cooldownMillis));
                tooltip.add(Component.literal(""));
            } else {
                tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.ready").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                tooltip.add(Component.literal(""));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.ready").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            tooltip.add(Component.literal(""));
        }

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.range").withStyle(ChatFormatting.AQUA));
            tooltip.add(LocatorSupport.protoMarkedLine(LocatorConfig.protoMarkedDurationTicks()));
            Component drawback = LocatorSupport.protoDrawbackLine(LocatorConfig.protoCooldownTicks());
            if (drawback != null) {
                tooltip.add(drawback);
            }
            List<ConfigItems.Entry> protoCost = LocatorConfig.protoCost();
            if (!protoCost.isEmpty()) {
                tooltip.add(LocatorSupport.customCostLine(protoCost, LocatorConfig.PROTO_CONSUME_COST.get()));
            }
            tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.usage").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}