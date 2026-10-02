package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.client.screen.ImplantMenu;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SurgicalImplantatorItem extends Item {

    public SurgicalImplantatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.SURGICAL_IMPLANTATOR_REQUIRES_ORIGIN.get(), "scientist"));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.implantator.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.implantator.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.implantator.use_self").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.sporeadds.implantator.use_other").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private boolean isScientist(ServerPlayer player) {
        return SporeClassUtil.hasClass(player, "scientist");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean canUse = true;
            if (SporeAddsConfig.SURGICAL_IMPLANTATOR_REQUIRES_ORIGIN.get()) {
                canUse = isScientist(serverPlayer);
            }

            if (!canUse) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.sporeadds.no_knowledge").withStyle(ChatFormatting.GRAY),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            if (player.isShiftKeyDown()) {
                openImplantGui(serverPlayer, serverPlayer);
                return InteractionResultHolder.success(stack);
            }

            serverPlayer.displayClientMessage(
                    Component.translatable("message.sporeadds.implantator.hint").withStyle(ChatFormatting.GOLD),
                    true
            );
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.FAIL;

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean canUse = true;
            if (SporeAddsConfig.SURGICAL_IMPLANTATOR_REQUIRES_ORIGIN.get()) {
                canUse = isScientist(serverPlayer);
            }

            if (!canUse) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.sporeadds.no_knowledge").withStyle(ChatFormatting.GRAY),
                        true
                );
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean canUse = true;
            if (SporeAddsConfig.SURGICAL_IMPLANTATOR_REQUIRES_ORIGIN.get()) {
                canUse = isScientist(serverPlayer);
            }

            if (!canUse) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.sporeadds.no_knowledge").withStyle(ChatFormatting.GRAY),
                        true
                );
                return InteractionResult.FAIL;
            }

            if (target instanceof ServerPlayer targetPlayer) {
                if (player.isShiftKeyDown()) {
                    openImplantGui(serverPlayer, serverPlayer);
                } else {
                    openImplantGui(serverPlayer, targetPlayer);
                }
                return InteractionResult.SUCCESS;
            } else {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.sporeadds.implantator.only_players").withStyle(ChatFormatting.RED),
                        true
                );
            }
        }

        return InteractionResult.PASS;
    }

    private void openImplantGui(ServerPlayer opener, ServerPlayer target) {
        NetworkHooks.openScreen(opener, new ImplantMenuProvider(target),
                buf -> buf.writeUUID(target.getUUID()));

        if (opener.equals(target)) {
            opener.displayClientMessage(
                    Component.translatable("message.sporeadds.implantator.open_self").withStyle(ChatFormatting.GREEN),
                    true
            );
        } else {
            opener.displayClientMessage(
                    Component.translatable("message.sporeadds.implantator.open_other", target.getName().getString())
                            .withStyle(ChatFormatting.GREEN),
                    true
            );

            target.displayClientMessage(
                    Component.translatable("message.sporeadds.implantator.accessed_by", opener.getName().getString())
                            .withStyle(ChatFormatting.GOLD),
                    false
            );
        }
    }

    public static class ImplantMenuProvider implements net.minecraft.world.MenuProvider {
        private final ServerPlayer targetPlayer;

        public ImplantMenuProvider(ServerPlayer targetPlayer) {
            this.targetPlayer = targetPlayer;
        }

        @Override
        public Component getDisplayName() {
            if (targetPlayer != null) {
                return Component.literal("Implants: " + targetPlayer.getName().getString());
            }
            return Component.translatable("item.sporeadds.surgical_implantator");
        }

        @Override
        public AbstractContainerMenu createMenu(int windowId, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
            return new ImplantMenu(windowId, playerInventory, targetPlayer);
        }
    }
}