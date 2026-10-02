package com.sporeadds.sporeaddsmod.items;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SyringeItem extends Item {
    public SyringeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    private static String generateCode() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random r = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static boolean hasMedicOrigin(ServerPlayer player) {
        return SporeClassUtil.hasClass(player, "medic");
    }

    private static void playInjectionSound(Level level, double x, double y, double z) {
        if (!level.isClientSide) {
            level.playSound(
                    null,
                    x, y, z,
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "pci_inject")),
                    SoundSource.PLAYERS,
                    1.0f,
                    2.0f
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.SYRINGE_REQUIRES_ORIGIN.get(), "medic"));

        CompoundTag tag = stack.getTag();
        boolean isFilled = tag != null && tag.contains("BloodCode", Tag.TAG_STRING);

        if (isFilled) {
            String subject = tag.getString("BloodSubject");
            String code = tag.getString("BloodCode");
            tooltip.add(Component.translatable("tooltip.sporeadds.syringe.subject", subject).withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.sporeadds.syringe.dna_code", code).withStyle(ChatFormatting.DARK_RED));
        } else {
            if (Screen.hasShiftDown()) {
                tooltip.add(Component.translatable("tooltip.sporeadds.syringe.desc1").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.sporeadds.syringe.desc2").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadds.syringe.use_other").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("tooltip.sporeadds.syringe.use_self").withStyle(ChatFormatting.AQUA));
            } else {
                tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer sp) || !(target instanceof ServerPlayer other)) return InteractionResult.PASS;

        boolean canUse = true;
        if (SporeAddsConfig.SYRINGE_REQUIRES_ORIGIN.get()) {
            canUse = hasMedicOrigin(sp);
        }

        if (!canUse) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.syringe.unknown_usage").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BloodCode", Tag.TAG_STRING)) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.syringe.already_filled").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        ResourceLocation termina = ResourceLocation.fromNamespaceAndPath("sporeadd", "termina");
        boolean found = false;
        for (MobEffectInstance eff : other.getActiveEffects()) {
            ResourceLocation effId = BuiltInRegistries.MOB_EFFECT.getKey(eff.getEffect());
            if (effId != null && effId.equals(termina)) {
                found = true;
                break;
            }
        }

        if (found) {
            String code = generateCode();
            other.getPersistentData().putString("SporeSyringeCode", code);
            tag.putString("BloodCode", code);
            tag.putString("BloodSubject", other.getGameProfile().getName());
            tag.putString("BloodUUID", other.getUUID().toString());
            stack.setHoverName(
                    Component.translatable("item.sporeadds.syringe.filled_name", other.getName())
                            .withStyle(ChatFormatting.DARK_RED)
            );
            tag.putBoolean("Filled", true);
            tag.putInt("CustomModelData", 1);

            sp.displayClientMessage(
                    Component.translatable("message.sporeadds.syringe.extracted_other", other.getName())
                            .withStyle(ChatFormatting.GREEN),
                    true
            );

            playInjectionSound(sp.level(), sp.getX(), sp.getY(), sp.getZ());
            return InteractionResult.SUCCESS;
        } else {
            sp.displayClientMessage(
                    Component.translatable("message.sporeadds.syringe.not_needed_target", other.getName())
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.FAIL;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.pass(stack);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.pass(stack);

        boolean canUse = true;
        if (SporeAddsConfig.SYRINGE_REQUIRES_ORIGIN.get()) {
            canUse = hasMedicOrigin(sp);
        }

        if (!canUse) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.syringe.unknown_usage").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BloodCode", Tag.TAG_STRING)) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.syringe.already_filled").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        if (player.isCrouching()) {
            ResourceLocation termina = ResourceLocation.fromNamespaceAndPath("sporeadd", "termina");
            boolean found = false;

            for (MobEffectInstance eff : player.getActiveEffects()) {
                ResourceLocation effId = BuiltInRegistries.MOB_EFFECT.getKey(eff.getEffect());
                if (effId != null && effId.equals(termina)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                String code = generateCode();
                player.getPersistentData().putString("SporeSyringeCode", code);
                tag.putString("BloodCode", code);
                tag.putString("BloodSubject", player.getGameProfile().getName());
                tag.putString("BloodUUID", player.getUUID().toString());
                stack.setHoverName(
                        Component.translatable("item.sporeadds.syringe.filled_name", player.getName())
                                .withStyle(ChatFormatting.DARK_RED)
                );
                tag.putBoolean("Filled", true);
                tag.putInt("CustomModelData", 1);

                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.syringe.extracted_self").withStyle(ChatFormatting.GREEN),
                        true
                );

                playInjectionSound(level, player.getX(), player.getY(), player.getZ());
                return InteractionResultHolder.success(stack);
            } else {
                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.syringe.not_needed_self").withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.pass(stack);
    }
}