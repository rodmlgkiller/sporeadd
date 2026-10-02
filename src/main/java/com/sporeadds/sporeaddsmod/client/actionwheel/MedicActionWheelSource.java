package com.sporeadds.sporeaddsmod.client.actionwheel;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.RequestDelayedDefibrillationPacket;
import com.sporeadds.sporeaddsmod.network.RequestSelfDefibrillateStartPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class MedicActionWheelSource {

    private static final ResourceLocation DEFIBRILLATOR_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/defibrilator.png");

    private static final ResourceLocation SELF_DEFIBRILLATE_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/defibrilator2.png");

    private static final float SELF_DEFIBRILLATE_HEALTH_THRESHOLD = 0.30F;

    private MedicActionWheelSource() {
    }

    private static boolean hasMedicClass(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "medic".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean canUseSelfDefibrillate(Player player) {
        return player.isAlive() && player.getHealth() <= player.getMaxHealth() * SELF_DEFIBRILLATE_HEALTH_THRESHOLD;
    }

    public static List<ActionWheelOption> getOptionsIfApplicable(Player player) {
        List<ActionWheelOption> options = new ArrayList<>();

        if (!hasMedicClass(player)) {
            return options;
        }

        ActionWheelOption.IconRenderer defibrillatorIcon = (graphics, x, y, size) -> {
            graphics.blit(
                    DEFIBRILLATOR_ICON,
                    x, y,
                    0, 0,
                    size, size,
                    size, size
            );
        };

        Component defibrillatorLabel = Component.translatable("ability.sporeadds.medic.delayed_defibrillation")
                .withStyle(ChatFormatting.RED);

        options.add(new ActionWheelOption(
                defibrillatorLabel,
                defibrillatorIcon,
                () -> NetworkHandle.INSTANCE.sendToServer(new RequestDelayedDefibrillationPacket())
        ));

        if (canUseSelfDefibrillate(player)) {
            ActionWheelOption.IconRenderer selfDefibrillateIcon = (graphics, x, y, size) -> {
                graphics.blit(
                        SELF_DEFIBRILLATE_ICON,
                        x, y,
                        0, 0,
                        size, size,
                        size, size
                );
            };

            Component selfDefibrillateLabel = Component.translatable("ability.sporeadds.medic.self_defibrillate")
                    .withStyle(ChatFormatting.RED);

            options.add(new ActionWheelOption(
                    selfDefibrillateLabel,
                    selfDefibrillateIcon,
                    () -> NetworkHandle.INSTANCE.sendToServer(new RequestSelfDefibrillateStartPacket())
            ));
        }

        return options;
    }
}