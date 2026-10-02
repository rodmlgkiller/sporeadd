package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.client.trainingbook.TrainingBookClientHooks;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrainingBookItem extends Item {

    private static final String USES_REMAINING_TAG = "uses_remaining";
    /** Marca el libro entregado en el primer join, el único afectado por readonly_when_origins_present. */
    private static final String INITIAL_TAG = "sporeadd_initial";

    public TrainingBookItem(Properties properties) {
        super(properties);
    }

    public static void markInitial(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(INITIAL_TAG, true);
    }

    public static boolean isInitial(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(INITIAL_TAG);
    }

    /** El libro inicial no tiene usos si Origins está presente y readonly_when_origins_present está activo. */
    public static boolean isReadOnly(ItemStack stack) {
        return isInitial(stack) && SporeAddsConfig.isTrainingBookReadOnly();
    }

    public static int getUsesRemaining(ItemStack stack) {
        if (isReadOnly(stack)) {
            return 0;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(USES_REMAINING_TAG)) {
            tag.putInt(USES_REMAINING_TAG, SporeAddsConfig.TRAINING_BOOK_MAX_USES.get());
        }
        return tag.getInt(USES_REMAINING_TAG);
    }

    public static void setUsesRemaining(ItemStack stack, int uses) {
        stack.getOrCreateTag().putInt(USES_REMAINING_TAG, Math.max(0, uses));
    }

    public static boolean consumeUse(ItemStack stack) {
        int remaining = getUsesRemaining(stack);
        if (remaining <= 0) {
            return false;
        }
        setUsesRemaining(stack, remaining - 1);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TrainingBookClientHooks.open(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        int usesRemaining = getUsesRemaining(stack);
        int maxUses = isReadOnly(stack) ? 0 : SporeAddsConfig.TRAINING_BOOK_MAX_USES.get();

        tooltip.add(Component.translatable("tooltip.sporeadd.training_book.uses", usesRemaining, maxUses)
                .withStyle(ChatFormatting.AQUA));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadd.training_book.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadd.training_book.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadd.training_book.use").withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
