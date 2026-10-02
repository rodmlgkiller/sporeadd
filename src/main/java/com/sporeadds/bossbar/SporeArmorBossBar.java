package com.sporeadds.bossbar;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

public class SporeArmorBossBar {
    private final ServerBossEvent bossEvent;
    private final Player armorOwner;

    public SporeArmorBossBar(Player owner) {
        this.armorOwner = owner;

        this.bossEvent = new ServerBossEvent(
                buildBossBarName(owner),
                BossEvent.BossBarColor.PINK,
                BossEvent.BossBarOverlay.NOTCHED_20
        );
        this.bossEvent.setVisible(true);
    }

    private static Component buildBossBarName(Player owner) {
        String subclass = owner.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> data.getSubclass() == null ? "default" : data.getSubclass().trim().toLowerCase())
                .orElse("default");

        String visibleName = owner.getDisplayName().getString();
        return Component.literal("[sporeadd|" + subclass + "]" + visibleName);
    }

    public void tick(float currentArmorHp, float maxArmorHp, double range) {
        if (armorOwner == null || !armorOwner.isAlive() || armorOwner.level().isClientSide) {
            return;
        }

        this.bossEvent.setName(buildBossBarName(armorOwner));

        float progress = Math.max(0.0f, Math.min(1.0f, currentArmorHp / maxArmorHp));
        this.bossEvent.setProgress(progress);

        List<ServerPlayer> nearbyPlayers = armorOwner.level().getEntitiesOfClass(
                ServerPlayer.class,
                armorOwner.getBoundingBox().inflate(range),
                player -> player.isAlive()
        );

        for (ServerPlayer player : nearbyPlayers) {
            if (!this.bossEvent.getPlayers().contains(player)) {
                this.bossEvent.addPlayer(player);
            }
        }

        for (ServerPlayer player : List.copyOf(this.bossEvent.getPlayers())) {
            if (!nearbyPlayers.contains(player)) {
                this.bossEvent.removePlayer(player);
            }
        }
    }

    public void removeAll() {
        this.bossEvent.removeAllPlayers();
        this.bossEvent.setVisible(false);
    }

    public UUID getOwnerUUID() {
        return armorOwner.getUUID();
    }
}