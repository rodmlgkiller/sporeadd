package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KommandantGammaController {

    private static final double NORMAL_GAMMA = 1.0D;
    private static final double MAX_CAVE_GAMMA = 3.0D;
    private static final double MAX_WATER_GAMMA = 3.0D;

    private static boolean isKommandant(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isAbyssal(Player player) {
        LazyOptional<SporeIdentifierData> cap = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER);
        if (cap.isPresent()) {
            return "abyssal".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.level == null) {
            KommandantBrightnessState.disable();
            return;
        }

        Player player = mc.player;
        if (!isKommandant(player)) {
            KommandantBrightnessState.disable();
            return;
        }

        BlockPos pos = player.blockPosition();
        int sky = mc.level.getBrightness(LightLayer.SKY, pos);
        int block = mc.level.getBrightness(LightLayer.BLOCK, pos);

        double gamma = NORMAL_GAMMA;

        if (sky == 0 && block <= 4) {
            double t = 1.0D - (block / 4.0D);
            gamma = NORMAL_GAMMA + t * (MAX_CAVE_GAMMA - NORMAL_GAMMA);
        }

        boolean underwater = player.isUnderWater() || player.isEyeInFluid(FluidTags.WATER);
        if (underwater && isAbyssal(player)) {
            gamma = Math.max(gamma, MAX_WATER_GAMMA);
        }

        if (gamma > NORMAL_GAMMA) {
            KommandantBrightnessState.enable(gamma);
        } else {
            KommandantBrightnessState.disable();
        }
    }
}