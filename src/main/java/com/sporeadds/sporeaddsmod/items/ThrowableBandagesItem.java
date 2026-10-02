package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.entity.projectile.ThrowableBandagesEntity;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ThrowableBandagesItem extends Item {

    private static final int SELF_HEAL_USE_TICKS = 20;
    private static final Map<UUID, Boolean> APPLIED_THIS_USE = new HashMap<>();

    public ThrowableBandagesItem(Properties properties) {
        super(properties.stacksTo(8));
    }

    private static boolean hasMedicClass(ServerPlayer sp) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(sp)
                .map(data -> "medic".equals(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean canUse(Player player) {
        if (!SporeAddsConfig.THROWABLE_BANDAGES_REQUIRES_ORIGIN.get()) {
            return true;
        }
        return player instanceof ServerPlayer sp && hasMedicClass(sp);
    }

    private static void playBandageEffect(Level level, LivingEntity target) {
        level.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvents.SNOW_STEP,
                SoundSource.PLAYERS,
                1.0F, 1.2F
        );

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.WHITE_CARPET.defaultBlockState()),
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    10,
                    0.3, 0.3, 0.3,
                    0.05
            );
        }
    }

    private static void throwBandage(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }

        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.PLAYERS,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        ThrowableBandagesEntity projectile = new ThrowableBandagesEntity(level, player);
        projectile.setItem(stack.copyWithCount(1));
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.5F, 1.0F);
        level.addFreshEntity(projectile);

        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        stack.shrink(1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!canUse(player)) {
            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(
                        Component.translatable("message.sporeadds.injector.unknown_usage")
                                .withStyle(ChatFormatting.GRAY),
                        true
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        if (player.isCrouching()) {
            APPLIED_THIS_USE.put(player.getUUID(), false);
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        throwBandage(level, player, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int useTicks = this.getUseDuration(stack) - remainingUseDuration;

        if (!player.isCrouching()) {
            APPLIED_THIS_USE.remove(player.getUUID());
            player.stopUsingItem();
            return;
        }

        if (useTicks >= SELF_HEAL_USE_TICKS) {
            boolean alreadyApplied = APPLIED_THIS_USE.getOrDefault(player.getUUID(), false);

            if (!alreadyApplied) {
                APPLIED_THIS_USE.put(player.getUUID(), true);

                if (!level.isClientSide) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 4));
                    playBandageEffect(level, player);
                    stack.shrink(1);
                }

                player.stopUsingItem();
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            APPLIED_THIS_USE.remove(player.getUUID());
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.THROWABLE_BANDAGES_REQUIRES_ORIGIN.get(), "medic"));

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.throwable_bandages.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.throwable_bandages.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}