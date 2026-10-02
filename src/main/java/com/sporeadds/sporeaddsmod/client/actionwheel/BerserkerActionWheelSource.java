package com.sporeadds.sporeaddsmod.client.actionwheel;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.client.SporeKeyMapping;
import com.sporeadds.sporeaddsmod.network.ActivateClawsPacket;
import com.sporeadds.sporeaddsmod.network.ActivateCounterPacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.OpenCompoundsPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class BerserkerActionWheelSource {

    private static final ResourceLocation COUNTER_ICON =
            new ResourceLocation("sporeadd", "textures/mob_effect/counter.png");
    private static final ResourceLocation CLAWS_ICON =
            new ResourceLocation("sporeadd", "textures/mob_effect/claws_of_brutality.png");
    private static final ResourceLocation COMPOUNDS_ICON =
            new ResourceLocation("sporeadd", "textures/mob_effect/compounds.png");

    private BerserkerActionWheelSource() {
    }

    private static boolean hasBerserkerClass(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "berserker".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    public static List<ActionWheelOption> getOptionsIfApplicable(Player player) {
        List<ActionWheelOption> options = new ArrayList<>();
        if (!hasBerserkerClass(player)) {
            return options;
        }

        ActionWheelOption.IconRenderer counterIcon = (graphics, x, y, size) ->
                graphics.blit(COUNTER_ICON, x, y, 0, 0, size, size, size, size);

        options.add(new ActionWheelOption(
                Component.translatable("ability.sporeadds.berserker.counter").withStyle(ChatFormatting.GOLD),
                counterIcon,
                () -> NetworkHandle.INSTANCE.sendToServer(new ActivateCounterPacket()),
                SporeKeyMapping.ABILITY_1
        ));

        ActionWheelOption.IconRenderer clawsIcon = (graphics, x, y, size) ->
                graphics.blit(CLAWS_ICON, x, y, 0, 0, size, size, size, size);

        options.add(new ActionWheelOption(
                Component.translatable("ability.sporeadds.berserker.claws").withStyle(ChatFormatting.RED),
                clawsIcon,
                () -> NetworkHandle.INSTANCE.sendToServer(new ActivateClawsPacket()),
                SporeKeyMapping.ABILITY_2
        ));

        ActionWheelOption.IconRenderer compoundsIcon = (graphics, x, y, size) ->
                graphics.blit(COMPOUNDS_ICON, x, y, 0, 0, size, size, size, size);

        options.add(new ActionWheelOption(
                Component.translatable("ability.sporeadds.berserker.compounds").withStyle(ChatFormatting.AQUA),
                compoundsIcon,
                () -> NetworkHandle.INSTANCE.sendToServer(new OpenCompoundsPacket())
        ));

        return options;
    }
}
