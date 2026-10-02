package com.sporeadds.sporeaddsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncVervaGuiPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.NetworkDirection;

import java.util.ArrayList;
import java.util.List;

public class VervaCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("verva")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();

                    List<String> verwa = new ArrayList<>(SporeAddsConfig.VERWA_SUMMONING_MENU.get());
                    List<String> organoid = new ArrayList<>(SporeAddsConfig.ORGANOID_SUMMONING_MENU.get());
                    List<String> bomb = new ArrayList<>(SporeAddsConfig.BOMB_SUMMONING_MENU.get());

                    NetworkHandle.INSTANCE.send(
                            com.sporeadds.sporeaddsmod.network.PacketDistributor.PLAYER.with(() -> player),
                            new SyncVervaGuiPacket(verwa, organoid, bomb)
                    );

                    return 1;
                })
        );
    }
}