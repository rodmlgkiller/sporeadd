package com.sporeadds.mixin;

import com.Harbinger.Spore.sEvents.ForgeEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraft.network.chat.Component;

@Mixin(value = ForgeEvents.class, remap = false)
public class ForgeEventsMixin {

    @Inject(method = "onCameraAngles", at = @At("HEAD"))
    private static void injectChatComprobacion(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
         // <-- Línea de debug

        // Solo ejecuta si hay jugador
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {

        }
    }
}
