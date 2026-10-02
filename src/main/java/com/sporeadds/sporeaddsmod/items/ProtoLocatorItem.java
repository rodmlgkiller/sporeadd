package com.sporeadds.sporeaddsmod.items;

import net.minecraft.core.registries.BuiltInRegistries;

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
    private static final long COOLDOWN_DURATION = 30 * 60 * 1000;
    private static final int GLOWING_DURATION = 5 * 60 * 20;

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
                CompoundTag nbt = stack.getOrCreateTag();
                long currentTime = System.currentTimeMillis();
                long lastUsed = nbt.getLong("lastUsed");
                long timeLeft = (lastUsed + COOLDOWN_DURATION) - currentTime;

                if (timeLeft > 0) {
                    int minutesLeft = (int) (timeLeft / (60 * 1000)) + 1;
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.proto_locator.cooldown", minutesLeft)
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
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
                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.proto_locator.ready_in_30")
                                    .withStyle(ChatFormatting.GRAY),
                            true
                    );
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
                                GLOWING_DURATION,
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

                    sp.displayClientMessage(
                            Component.translatable("message.sporeadds.proto_locator.ready_in_30")
                                    .withStyle(ChatFormatting.GRAY),
                            true
                    );
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
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("lastUsed")) {
            long currentTime = System.currentTimeMillis();
            long lastUsed = nbt.getLong("lastUsed");
            long timeLeft = (lastUsed + COOLDOWN_DURATION) - currentTime;
            return timeLeft > 0;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.PROTO_LOCATOR_REQUIRES_ORIGIN.get(), "scientist"));

        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("lastUsed")) {
            long currentTime = System.currentTimeMillis();
            long lastUsed = nbt.getLong("lastUsed");
            long timeLeft = (lastUsed + COOLDOWN_DURATION) - currentTime;

            if (timeLeft > 0) {
                int minutesLeft = (int) (timeLeft / (60 * 1000)) + 1;
                tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.cooldown", minutesLeft).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
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
            tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.duration").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.cost").withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.sporeadds.proto_locator.usage").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}