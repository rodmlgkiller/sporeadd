package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class KommandantColdBiomeEvents {

    private static final int FROSTBITE_DURATION = 300; // 15 segundos
    private static final int REFRESH_THRESHOLD = 20;   // 1 segundo
    private static final int MAX_FROSTBITE_AMPLIFIER = 3;

    // Cuánto exhaustion añadir por tick mientras esté en bioma frío.
    // 0.005f = 0.1 por segundo.
    private static final float EXHAUSTION_ADDED_PER_TICK = 0.005f;

    private static boolean hasKommandantClass(ServerPlayer player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equals(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isColdBiome(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        Biome biome = player.serverLevel().getBiome(pos).value();
        return biome.getBaseTemperature() <= 0.15F;
    }

    private static void applyColdHungerPenalty(ServerPlayer player) {
        FoodData foodData = player.getFoodData();
        foodData.addExhaustion(EXHAUSTION_ADDED_PER_TICK);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!hasKommandantClass(player)) return;
        if (!isColdBiome(player)) return;

        applyColdHungerPenalty(player);

        MobEffect frostbiteEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "frostbite"));
        if (frostbiteEffect == null) return;

        MobEffectInstance currentEffect = player.getEffect(frostbiteEffect);

        if (currentEffect == null) {
            player.addEffect(new MobEffectInstance(
                    frostbiteEffect,
                    FROSTBITE_DURATION,
                    0,
                    false,
                    true
            ));
            return;
        }

        if (currentEffect.getDuration() <= REFRESH_THRESHOLD) {
            int newAmplifier = Math.min(MAX_FROSTBITE_AMPLIFIER, currentEffect.getAmplifier() + 1);

            player.addEffect(new MobEffectInstance(
                    frostbiteEffect,
                    FROSTBITE_DURATION,
                    newAmplifier,
                    false,
                    true
            ));
        }
    }
}