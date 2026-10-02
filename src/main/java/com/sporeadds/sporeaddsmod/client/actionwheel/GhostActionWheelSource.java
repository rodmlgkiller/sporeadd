package com.sporeadds.sporeaddsmod.client.actionwheel;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.network.ActivateCamouflagePacket;
import com.sporeadds.sporeaddsmod.network.ActivateDecoyPacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class GhostActionWheelSource {

    private static final ResourceLocation CAMOUFLAGE_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/disguised.png");

    private static final ResourceLocation DECOY_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/decoy.png");

    private GhostActionWheelSource() {
    }

    private static boolean hasGhostClass(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "ghost".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    public static List<ActionWheelOption> getOptionsIfApplicable(Player player) {
        List<ActionWheelOption> options = new ArrayList<>();

        if (!hasGhostClass(player)) {
            return options;
        }

        ActionWheelOption.IconRenderer camouflageIcon = (graphics, x, y, size) -> {
            graphics.blit(
                    CAMOUFLAGE_ICON,
                    x, y,
                    0, 0,
                    size, size,
                    size, size
            );
        };

        Component camouflageLabel = Component.translatable("ability.sporeadds.ghost.camouflage")
                .withStyle(ChatFormatting.GREEN);

        options.add(new ActionWheelOption(
                camouflageLabel,
                camouflageIcon,
                () -> NetworkHandle.INSTANCE.sendToServer(new ActivateCamouflagePacket(player.getId()))
        ));

        ActionWheelOption.IconRenderer decoyIcon = (graphics, x, y, size) -> {
            graphics.blit(
                    DECOY_ICON,
                    x, y,
                    0, 0,
                    size, size,
                    size, size
            );
        };

        Component decoyLabel = Component.translatable("ability.sporeadds.ghost.decoy")
                .withStyle(ChatFormatting.GREEN);

        options.add(new ActionWheelOption(
                decoyLabel,
                decoyIcon,
                () -> NetworkHandle.INSTANCE.sendToServer(new ActivateDecoyPacket(player.getId()))
        ));

        return options;
    }
}