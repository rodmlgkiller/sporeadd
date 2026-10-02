package com.sporeadds.sporeaddsmod.client.actionwheel;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.client.SporeKeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.List;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class ActionWheelHandler {

    private static boolean wheelOpen = false;
    private static boolean waitingForRelease = false;

    private ActionWheelHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        boolean keyDown = SporeKeyMapping.OPEN_ACTION_WHEEL.isDown();

        if (!keyDown) {
            waitingForRelease = false;
            wheelOpen = false;
            return;
        }

        if (keyDown && !wheelOpen && !waitingForRelease) {
            wheelOpen = true;
            openWheel(player);
        }
    }

    private static void openWheel(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            waitingForRelease = true;
            return;
        }

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        List<ActionWheelOption> options;

        if (stack.getItem() instanceof ActionWheelProvider provider) {
            options = provider.getActionWheelOptions(player, stack);
        } else {
            options = GhostActionWheelSource.getOptionsIfApplicable(player);

            if (options.isEmpty()) {
                options = ScientistActionWheelSource.getOptionsIfApplicable(player);
            }

            if (options.isEmpty()) {
                options = MedicActionWheelSource.getOptionsIfApplicable(player);
            }

            if (options.isEmpty()) {
                options = BerserkerActionWheelSource.getOptionsIfApplicable(player);
            }
        }

        if (options.isEmpty()) {
            wheelOpen = false;
            waitingForRelease = true;
            return;
        }

        mc.setScreen(new ActionWheelScreen(options));
        waitingForRelease = true;
    }

    public static void notifyClosedExternally() {
        wheelOpen = false;
    }
}