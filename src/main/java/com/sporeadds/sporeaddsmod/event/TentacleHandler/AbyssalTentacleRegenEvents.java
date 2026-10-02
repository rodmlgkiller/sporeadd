package com.sporeadds.sporeaddsmod.event.TentacleHandler;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.network.AbyssalTentaclePacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class AbyssalTentacleRegenEvents {

    public static final int REGEN_TIME_TICKS = 15 * 20;
    public static final int RETRY_TIME_TICKS = 20;
    public static final int BIOMASS_COST = 3;

    public static final String SLOT_1_TIMER_TAG = "sporeadds_tentacle_slot_1_regen_timer";
    public static final String SLOT_2_TIMER_TAG = "sporeadds_tentacle_slot_2_regen_timer";
    public static final String SLOT_3_TIMER_TAG = "sporeadds_tentacle_slot_3_regen_timer";

    // Identificadores de sonido personalizados
    private static final ResourceLocation HOWLER_GROWL_RL = ResourceLocation.fromNamespaceAndPath("spore", "howler_growl");
    private static final ResourceLocation UMARMER_AMBIENT_RL = ResourceLocation.fromNamespaceAndPath("spore", "umarmer_ambient");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        AbyssalTentaclePacket.normalizeSlotStates(data);

        boolean hasDeadOrReturning = false;

        hasDeadOrReturning |= tickSlot(player, data, 1);
        hasDeadOrReturning |= tickSlot(player, data, 2);
        hasDeadOrReturning |= tickSlot(player, data, 3);

        // Refresco automático del action bar si hay cooldowns activos
        if (hasDeadOrReturning && player.tickCount % 20 == 0) {
            int playerLevel = AbyssalTentaclePacket.getMaxTentaclesForLevel(
                    com.sporeadds.sporeaddsmod.level.PlayerLevelProvider.PLAYER_LVL.get(player)
                            .map(cap -> cap.getLevel()).orElse(0)
            );
            AbyssalTentaclePacket.sendTentacleStatus(player, data, playerLevel);
        }
    }

    public static void startRegenTimer(CompoundTag data, int slot, ServerPlayer owner) {
        data.putInt(getTimerTag(slot), REGEN_TIME_TICKS);

        // Sonido de muerte del tentáculo (pitch 2.0F)
        SoundEvent growlSound = BuiltInRegistries.SOUND_EVENT.get(HOWLER_GROWL_RL);
        if (growlSound != null) {
            // El volumen 3.0F asegura que se escuche a unos 48 bloques (16 bloques base * 3)
            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                    growlSound, SoundSource.PLAYERS, 3.0F, 2.0F);
        }
    }

    public static int getRegenTimer(CompoundTag data, int slot) {
        return data.getInt(getTimerTag(slot));
    }

    // Devuelve true si el slot está en estado activo de tiempo (DEAD o RETURNING)
    private static boolean tickSlot(ServerPlayer player, CompoundTag data, int slot) {
        String state = AbyssalTentaclePacket.getSlotState(data, slot);
        int timer = data.getInt(getTimerTag(slot));

        if (!AbyssalTentaclePacket.STATE_DEAD.equals(state)) {
            if (timer != 0) {
                data.putInt(getTimerTag(slot), 0);
            }
            return AbyssalTentaclePacket.STATE_RETURNING.equals(state);
        }

        if (timer > 0) {
            data.putInt(getTimerTag(slot), timer - 1);
            return true;
        }

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(sporeCap -> {
            if (sporeCap.getSpore() >= BIOMASS_COST) {
                sporeCap.subSpore(BIOMASS_COST);
                AbyssalTentaclePacket.setSlotState(data, slot, AbyssalTentaclePacket.STATE_READY);
                data.putInt(getTimerTag(slot), 0);

                // Sonido de regeneración exitosa (pitch 1.0F)
                SoundEvent ambientSound = BuiltInRegistries.SOUND_EVENT.get(UMARMER_AMBIENT_RL);
                if (ambientSound != null) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ambientSound, SoundSource.PLAYERS, 3.0F, 1.0F);
                }

                // Disparo visual débil
                AbyssalTentaclePacket.shootRegenTentacle(player, slot);

            } else {
                data.putInt(getTimerTag(slot), RETRY_TIME_TICKS);
            }
        });

        return true;
    }

    private static String getTimerTag(int slot) {
        return switch (slot) {
            case 1 -> SLOT_1_TIMER_TAG;
            case 2 -> SLOT_2_TIMER_TAG;
            case 3 -> SLOT_3_TIMER_TAG;
            default -> throw new IllegalArgumentException("Invalid tentacle slot: " + slot);
        };
    }
}