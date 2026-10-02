package com.sporeadds.sporeaddsmod.items;

import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OverchargedScentSpawnEggItem extends Item {

    private static final Component TYPE_TOOLTIP =
            Component.translatable("spore.name.unknown").withStyle(ChatFormatting.GOLD);

    public OverchargedScentSpawnEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Player player = context.getPlayer();

        EntityType<?> type = Sentities.SCENT.get();
        Entity created = type.create(serverLevel);

        if (!(created instanceof ScentEntity scent)) {
            return InteractionResult.FAIL;
        }

        scent.moveTo(
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                0.0F,
                0.0F
        );

        scent.setOvercharged(true);
        scent.setSummon(0);
        scent.setDissipate(0);

        serverLevel.addFreshEntity(scent);

        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TYPE_TOOLTIP);
        super.appendHoverText(stack, level, tooltip, flag);
    }
}