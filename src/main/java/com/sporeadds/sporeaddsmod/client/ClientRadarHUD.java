package com.sporeadds.sporeaddsmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public class ClientRadarHUD {

    public static List<Component> reportLines = new ArrayList<>();
    public static int displayTimer = 0;

    // Llama a esto cuando llega el paquete del servidor
    public static void setReport(List<Component> lines, int ticksToDisplay) {
        reportLines = lines;
        displayTimer = ticksToDisplay;
    }

    // El HUD Overlay que dibuja en pantalla
    public static final IGuiOverlay HUD_RADAR = (gui, graphics, partialTick, width, height) -> {
        if (displayTimer > 0 && !reportLines.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            int fontHeight = mc.font.lineHeight;

            // --- SISTEMA DE TAMAÑO FIJO INDEPENDIENTE ---
            double targetGuiScale = 2.0; // El tamaño fijo que queremos (GUI Scale 2)
            double currentGuiScale = mc.getWindow().getGuiScale(); // El tamaño que tiene puesto el jugador

            // Factor de compensación
            float customScale = (float) (targetGuiScale / currentGuiScale);

            graphics.pose().pushPose();
            // Compensamos la matriz para forzar nuestro tamaño
            graphics.pose().scale(customScale, customScale, customScale);

            // Al forzar el tamaño, necesitamos recalcular dónde cae "la cuarta parte superior"
            // basándonos en los píxeles reales (físicos) del monitor
            double physicalHeight = mc.getWindow().getScreenHeight();
            int scaledHeight = (int) (physicalHeight / targetGuiScale);

            int x = 10; // Margen izquierdo fijo
            int startY = scaledHeight / 4; // Empezar un poco más arriba de la mitad

            // Calcular el ancho del recuadro oscuro
            int maxWidth = 0;
            for (Component line : reportLines) {
                int lineWidth = mc.font.width(line);
                if (lineWidth > maxWidth) maxWidth = lineWidth;
            }

            // Dibujar fondo semitransparente (RGBA: Negro al 60% de opacidad)
            int totalHeight = reportLines.size() * (fontHeight + 2);
            graphics.fill(x - 5, startY - 5, x + maxWidth + 5, startY + totalHeight + 5, 0x99000000);

            // Dibujar el texto línea a línea
            int currentY = startY;
            for (Component line : reportLines) {
                graphics.drawString(mc.font, line, x, currentY, 0xFFFFFF, true);
                currentY += fontHeight + 2;
            }

            graphics.pose().popPose(); // Restauramos para no romper el resto de menús
        }
    };

    // Reducir el tiempo en cada tick del cliente
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (displayTimer > 0) {
                displayTimer--;
            }
        }
    }
}