package com.sporeadds.sporeaddsmod.client;

import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.Poder6UsePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Poder6ClientInput {

    private static boolean wasJumpDownLastTick = false; // Solo se usará para los terrestres

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Entity vehicle = mc.player.getVehicle();

        if (vehicle instanceof LivingEntity mob && (mob instanceof Infected || mob instanceof Calamity)) {

            // === 1. CAPTURA DE MOVIMIENTO (WASD) ===
            float forward = 0.0f;
            float strafing = 0.0f;

            if (mc.player.input.up) forward += 1.0f;
            if (mc.player.input.down) forward -= 1.0f;
            if (mc.player.input.left) strafing += 1.0f;
            if (mc.player.input.right) strafing -= 1.0f;

            // === 2. CAPTURA DE SALTO / DESCENSO ===
            boolean isJumpDown = mc.player.input.jumping;
            boolean isDescendDown = mc.options.keySprint.isDown();

            // Determinamos la dirección vertical (isAscending)
            // Priorizamos el salto sobre el descenso si ambos están presionados
            boolean isAscending = isJumpDown;
            if (!isJumpDown && isDescendDown) {
                isAscending = false; // Aquí indicamos que queremos bajar
            }

            // Calculamos si es el primer frame que se presiona el salto
            boolean isFirstPress = isJumpDown && !wasJumpDownLastTick;

            // === 3. ENVÍO UNIFICADO ===
            // Enviamos el paquete si hay CUALQUIER entrada activa
            if (forward != 0.0f || strafing != 0.0f || isJumpDown || isDescendDown) {
                NetworkHandle.INSTANCE.sendToServer(new Poder6UsePacket(
                        forward,
                        strafing,
                        isAscending,
                        isFirstPress
                ));
            }

            // Actualizamos el estado para el siguiente tick
            wasJumpDownLastTick = isJumpDown;
        }
    }
}