package com.sporeadds.bossbar;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.Powers.Poder12Variants;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class SporeArmorBossBarManager {

    private static final Map<UUID, SporeArmorBossBar> ACTIVE_BARS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (!SporeAddsConfig.ARMOR_BOSSBAR_ENABLED.get()) {
            if (!ACTIVE_BARS.isEmpty()) {
                List<SporeArmorBossBar> barsToClear = new ArrayList<>(ACTIVE_BARS.values());
                for (SporeArmorBossBar bar : barsToClear) {
                    try {
                        bar.removeAll();
                    } catch (Exception e) {
                    }
                }
                ACTIVE_BARS.clear();
            }
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID playerId = player.getUUID();

        AtomicInteger currentArmorHp = new AtomicInteger(0);
        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            currentArmorHp.set(data.getArmorHp());
        });

        int hp = currentArmorHp.get();
        boolean hasBar = ACTIVE_BARS.containsKey(playerId);
        int maxArmor = Poder12Variants.getMaxArmor(player);

        if (hp > 0 && !hasBar) {
            ACTIVE_BARS.put(playerId, new SporeArmorBossBar(player));
            hasBar = true;
        }

        if (hp <= 0 && hasBar) {
            SporeArmorBossBar bar = ACTIVE_BARS.get(playerId);
            try {
                bar.removeAll();
            } catch (Exception e) {
            }
            ACTIVE_BARS.remove(playerId);
            return;
        }

        if (hasBar) {
            SporeArmorBossBar bar = ACTIVE_BARS.get(playerId);

            if (!player.isAlive()) {
                try {
                    bar.removeAll();
                } catch (Exception e) {
                }
                ACTIVE_BARS.remove(playerId);
            } else {
                try {
                    double range = SporeAddsConfig.ARMOR_BOSSBAR_RANGE.get();
                    bar.tick(hp, (float) maxArmor, range);
                } catch (Exception e) {
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();
        SporeArmorBossBar bar = ACTIVE_BARS.remove(playerId);

        if (bar != null) {
            try {
                bar.removeAll();
            } catch (Exception e) {
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();
        SporeArmorBossBar oldBar = ACTIVE_BARS.remove(playerId);

        if (oldBar != null) {
            try {
                oldBar.removeAll();
            } catch (Exception e) {
            }
        }
    }
}