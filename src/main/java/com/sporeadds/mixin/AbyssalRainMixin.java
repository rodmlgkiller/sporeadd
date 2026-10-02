package com.sporeadds.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.client.ClientAbyssalStormState;
import com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Whenever the local player is within 200 blocks of an active Abyssal Power13 storm:
 * - redirects the texture vanilla binds for the falling-rain streak overlay in
 *   {@code renderSnowAndRain} from {@code minecraft:textures/environment/rain.png} to our own
 *   "sporeadd:rain" texture, reusing vanilla's exact mesh/animation logic with our texture instead;
 * - redirects the discrete ground-splash particles vanilla spawns each tick in {@code tickRain} from
 *   {@code ParticleTypes.RAIN} to our own "sporeadd:rain" particle.
 */
@Mixin(LevelRenderer.class)
public class AbyssalRainMixin {

    private static final ResourceLocation VANILLA_RAIN_STREAK = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation CUSTOM_RAIN_STREAK = new ResourceLocation("sporeadd", "textures/particle/rain.png");

    @Redirect(
            method = "renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"
            )
    )
    private void sporeadds$swapRainStreakTextureNearAbyssalStorm(int textureUnit, ResourceLocation location) {
        ResourceLocation toBind = location;
        if (location.equals(VANILLA_RAIN_STREAK)) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && ClientAbyssalStormState.isNear(player.getX(), player.getY(), player.getZ())) {
                toBind = CUSTOM_RAIN_STREAK;
            }
        }
        RenderSystem.setShaderTexture(textureUnit, toBind);
    }

    @Redirect(
            method = "tickRain(Lnet/minecraft/client/Camera;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void sporeadds$replaceRainSplashNearAbyssalStorm(
            ClientLevel level, ParticleOptions options,
            double x, double y, double z, double dx, double dy, double dz
    ) {
        ParticleOptions toSpawn = options;
        if (options == ParticleTypes.RAIN && ClientAbyssalStormState.isNear(x, y, z)) {
            toSpawn = SporeaddParticleTypes.RAIN_SPLASH.get();
        }
        level.addParticle(toSpawn, x, y, z, dx, dy, dz);
    }
}
