package com.sporeadds.sporeaddsmod.client.actionwheel;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.MedicConfig;
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
            new ResourceLocation("sporeadd", "textures/mob_effect/defibrilator.png");

    private static final ResourceLocation SELF_DEFIBRILLATE_ICON =
            new ResourceLocation("sporeadd", "textures/mob_effect/defibrilator2.png");

    private MedicActionWheelSource() {
    }

    private static boolean hasMedicClass(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "medic".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean canUseSelfDefibrillate(Player player) {
        // Client side: this client's own copy of medic.self_defibrillate.health_threshold (the server checks it again).
        return player.isAlive() && player.getHealth() <= player.getMaxHealth() * MedicConfig.selfDefibrillateHealthThreshold();
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