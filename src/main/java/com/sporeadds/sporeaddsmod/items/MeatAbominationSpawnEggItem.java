package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.entity.MeatAbomination;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ForgeSpawnEggItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class MeatAbominationSpawnEggItem extends ForgeSpawnEggItem {

    public MeatAbominationSpawnEggItem(
            Supplier<? extends EntityType<? extends net.minecraft.world.entity.Mob>> type,
            int backgroundColor,
            int highlightColor,
            Properties properties
    ) {
        super(type, backgroundColor, highlightColor, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.meat_abomination_spawn_egg.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.meat_abomination_spawn_egg.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.meat_abomination_spawn_egg.desc3").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = super.useOn(context);

        if (result.consumesAction() && context.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos clickedPos = context.getClickedPos();

            List<MeatAbomination> spawned = serverLevel.getEntitiesOfClass(
                    MeatAbomination.class,
                    new net.minecraft.world.phys.AABB(clickedPos).inflate(3.0D),
                    entity -> entity.isAlive()
            );

            MeatAbomination nearest = null;
            double bestDist = Double.MAX_VALUE;

            for (MeatAbomination meat : spawned) {
                double dist = meat.distanceToSqr(
                        clickedPos.getX() + 0.5D,
                        clickedPos.getY() + 0.5D,
                        clickedPos.getZ() + 0.5D
                );

                if (dist < bestDist) {
                    bestDist = dist;
                    nearest = meat;
                }
            }

            if (nearest != null) {
                nearest.setBiomass(serverLevel.random.nextIntBetweenInclusive(1, 5));
            }
        }

        return result;
    }
}