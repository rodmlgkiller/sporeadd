package com.sporeadds.sporeaddsmod.client.actionwheel;

import com.sporeadds.sporeaddsmod.items.BiomassCoreItem;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.network.BiomassCoreMoldPacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side of the Biomass Core action wheel: a wheel with one entry per item the core can mold at the player's
 * level. Picking one sends {@link BiomassCoreMoldPacket}; the server validates and molds it.
 */
public final class BiomassCoreWheelClient {

    private BiomassCoreWheelClient() {
    }

    /** The server asked for the wheel (the Biomass Core was right-clicked with biomass_core.action_wheel_enabled on). */
    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }

        final int[] level = {0};
        PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(cap -> level[0] = cap.getLevel());

        List<ActionWheelOption> options = new ArrayList<>();
        for (String choice : BiomassCoreItem.wheelChoices(level[0])) {
            ResourceLocation id = BiomassCoreItem.choiceItemId(choice);
            Item item = id == null ? null : BuiltInRegistries.ITEM.get(id);
            if (item == null || item == Items.AIR) {
                continue;
            }
            ItemStack icon = new ItemStack(item);
            options.add(new ActionWheelOption(
                    icon.getHoverName().copy().withStyle(ChatFormatting.AQUA),
                    (graphics, x, y, size) -> {
                        graphics.pose().pushPose();
                        graphics.pose().translate(x, y, 0);
                        float scale = size / 16.0F;
                        graphics.pose().scale(scale, scale, 1.0F);
                        graphics.renderFakeItem(icon, 0, 0);
                        graphics.pose().popPose();
                    },
                    () -> NetworkHandle.INSTANCE.sendToServer(new BiomassCoreMoldPacket(choice))
            ));
        }

        if (!options.isEmpty()) {
            mc.setScreen(new ActionWheelScreen(options));
        }
    }
}
