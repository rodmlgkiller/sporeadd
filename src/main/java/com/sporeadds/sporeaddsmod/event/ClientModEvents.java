package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.blocks.modblocks;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.modblocksentity;
import com.sporeadds.sporeaddsmod.client.renderer.block.MoundTerrariumBlockEntityRenderer;
import com.sporeadds.sporeaddsmod.client.ClientRadarHUD;
import com.sporeadds.sporeaddsmod.client.gui.VervaCountdownOverlay;
import com.sporeadds.sporeaddsmod.client.renderer.SporeeggRenderer;
import com.sporeadds.sporeaddsmod.client.renderer.TentacleRenderer;
import com.sporeadds.sporeaddsmod.client.screen.*;
import com.sporeadds.sporeaddsmod.entity.ModEntities;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientModEvents {

    @EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {

        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.MEDIC_BLOCK_MENU.get(), MedicBlockScreen::new);
        event.register(ModMenuTypes.IMPLANT_MENU.get(), ImplantScreen::new);
        event.register(ModMenuTypes.COMPOUNDS_MENU.get(), CompoundsScreen::new);
        event.register(ModMenuTypes.MEDIC_BLOCK_CONSTRUCTOR_MENU.get(), MedicBlockContructorScreen::new);
        event.register(ModMenuTypes.SCIENTIST_MENU.get(), ScientistScreen::new);
        event.register(ModMenuTypes.CRYO_MENU.get(), cryoscreen::new);
        event.register(ModMenuTypes.MOUND_TERRARIUM_MENU.get(), MoundTerrariumScreen::new);
        event.register(ModMenuTypes.RAID_CONTROLER.get(), RaidControlerScreen::new);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                EntityRenderers.register(ModEntities.COCOON.get(), SporeeggRenderer::new);
                EntityRenderers.register(ModEntities.TENTACLE.get(), TentacleRenderer::new);

                ItemBlockRenderTypes.setRenderLayer(modblocks.MOUND_TERRARIUM.get(), RenderType.translucent());
            });
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(
                    modblocksentity.MOUND_TERRARIUM_ENTITY.get(),
                    MoundTerrariumBlockEntityRenderer::new
            );
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiLayersEvent event) {
            event.registerAboveAll(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "radar_scan_hud"), ClientRadarHUD.HUD_RADAR);
            event.registerAboveAll(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sporeadd", "verva_countdown"), VervaCountdownOverlay.OVERLAY);
        }
    }
}