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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.FreezerBlock;
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

    /**
     * Checks the whole 2x2x2 volume at the position the block will really go (BlockPlaceContext resolves it: the clicked
     * cell itself when it is replaceable, e.g. tall grass, otherwise the one next to the clicked face) before anything is
     * placed, so a refused placement never leaves a lone half-built freezer.
     */
    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }

        FreezerBlock.PlacementProblem problem = FreezerBlock.placementProblem(level, origin, true);
        if (problem == FreezerBlock.PlacementProblem.NONE && !level.getFluidState(origin).isEmpty()) {
            problem = FreezerBlock.PlacementProblem.WATER;
        }
        if (problem != FreezerBlock.PlacementProblem.NONE) {
            Player player = context.getPlayer();
            if (player != null) {
                player.displayClientMessage(Component.translatable(problem == FreezerBlock.PlacementProblem.WATER
                        ? "message.sporeadds.freezer_block.no_water"
                        : "block.sporeadd.freezer_block.not_enough_space"), true);
            }
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }
}