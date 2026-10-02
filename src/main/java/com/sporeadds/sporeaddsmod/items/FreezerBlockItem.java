package com.sporeadds.sporeaddsmod.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FreezerBlockItem extends BlockItem {
    public FreezerBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.freezer_block.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.freezer_block.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.freezer_block.desc3").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.freezer_block.jam").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.sporeadds.freezer_block.no_water").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Player player = context.getPlayer();

        // Chequea si el espacio 2x2x2 está libre:
        boolean canPlace = true;
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (!level.getBlockState(checkPos).canBeReplaced()) {
                        canPlace = false;
                        break;
                    }
                }
                if (!canPlace) break;
            }
            if (!canPlace) break;
        }

        if (!canPlace) {
            if (player != null && level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("block.sporeadd.freezer_block.not_enough_space"),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        // Si hay espacio, llama al código normal de colocación de bloque
        return super.useOn(context);
    }
}