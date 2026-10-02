package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoundTerrariumItem extends BlockItem {

    private static final ResourceLocation MOUND_ID = ResourceLocation.fromNamespaceAndPath("spore", "mound");

    public MoundTerrariumItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        CompoundTag tag = ItemNbt.getTag(stack);
        boolean hasMound = tag != null && tag.getBoolean("HasMound");

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadd.mound_terrarium.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadd.mound_terrarium.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadd.mound_terrarium.desc3").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadd.mound_terrarium.collect_title").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.translatable("tooltip.sporeadd.mound_terrarium.collect1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadd.mound_terrarium.collect2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(
                    hasMound
                            ? Component.translatable("tooltip.sporeadd.mound_terrarium.state_full").withStyle(ChatFormatting.GREEN)
                            : Component.translatable("tooltip.sporeadd.mound_terrarium.state_empty").withStyle(ChatFormatting.YELLOW)
            );
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(
                    hasMound
                            ? Component.translatable("tooltip.sporeadd.mound_terrarium.state_full").withStyle(ChatFormatting.GREEN)
                            : Component.translatable("tooltip.sporeadd.mound_terrarium.state_empty").withStyle(ChatFormatting.YELLOW)
            );
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();

        ResourceLocation targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (targetId == null || !targetId.equals(MOUND_ID)) {
            return super.interactLivingEntity(stack, player, target, hand);
        }

        CompoundTag stackTag = ItemNbt.getOrCreateTag(stack);
        if (stackTag.getBoolean("HasMound")) {
            return InteractionResult.PASS;
        }

        CompoundTag entityTag = target.saveWithoutId(new CompoundTag());
        int age = entityTag.getInt("age");

        if (age > 1) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            target.discard();

            stackTag.putBoolean("HasMound", true);
            stackTag.putBoolean("Linked", entityTag.getBoolean("linked"));
            stackTag.putInt("CustomModelData", 1);

            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOTTLE_FILL,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}