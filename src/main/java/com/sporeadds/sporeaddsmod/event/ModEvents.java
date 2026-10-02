package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.client.SporeKeyMapping;
import com.sporeadds.sporeaddsmod.client.gui.AbilityChargeBarGui;
import com.sporeadds.sporeaddsmod.client.gui.ArmorGui;
import com.sporeadds.sporeaddsmod.client.gui.SporeBarGui;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.Poder5UsePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEvents {

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("spore_bar", SporeBarGui.SPORE_BAR);
        event.registerAboveAll("spore_armor_bar", ArmorGui.ARMOR_BAR);
        event.registerAboveAll("ability_charge_bar", AbilityChargeBarGui.ABILITY_CHARGE_BAR);
    }

    // Colega: ¡esto es para solo keymaps y overlays!
    // El handler de clic de entidad debe ir en Bus.FORGE:
    @Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeClientEvents {
        @SubscribeEvent
        public static void onEntityInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
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

    public ModEvents() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Tu código de configuración inicial aquí
    }
}
