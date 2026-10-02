package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.client.SporeKeyMapping;
import com.sporeadds.sporeaddsmod.client.gui.AbilityChargeBarGui;
import com.sporeadds.sporeaddsmod.client.gui.ArmorGui;
import com.sporeadds.sporeaddsmod.client.gui.SporeBarGui;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.Poder5UsePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEvents {

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "spore_bar"), SporeBarGui.SPORE_BAR);
        event.registerAboveAll(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "spore_armor_bar"), ArmorGui.ARMOR_BAR);
        event.registerAboveAll(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "ability_charge_bar"), AbilityChargeBarGui.ABILITY_CHARGE_BAR);
    }

    // Colega: ¡esto es para solo keymaps y overlays!
    // El handler de clic de entidad debe ir en Bus.FORGE:
    @EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class ForgeClientEvents {
        @SubscribeEvent
        public static void onEntityInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
            // SOLO EJECUTAR EN CLIENTE (Forge 1.20+)
            if (!event.getSide().isClient()) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || event.getEntity() != mc.player) return;
            // Aquí puedes especificar más condiciones si no es para todas las entidades
            if (event.getTarget() instanceof LivingEntity target) {
                // Aquí puedes poner checks extra de tipo de entidad, switch, etc. EJEMPLO:
                // if (tuCondicionSobreElTarget && tuCondicionSobreElJugador) {
                NetworkHandle.INSTANCE.sendToServer(new Poder5UsePacket(target.getId()));
                // Puedes cancelar la acción vanilla si quieres:
                // event.setCanceled(true);
                // }
            }
        }
    }
}
