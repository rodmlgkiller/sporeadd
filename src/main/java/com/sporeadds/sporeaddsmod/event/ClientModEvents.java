package com.sporeadds.sporeaddsmod.event;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientModEvents {

    @Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenuTypes.MEDIC_BLOCK_MENU.get(), MedicBlockScreen::new);
                MenuScreens.register(ModMenuTypes.IMPLANT_MENU.get(), ImplantScreen::new);
                MenuScreens.register(ModMenuTypes.COMPOUNDS_MENU.get(), CompoundsScreen::new);
                EntityRenderers.register(ModEntities.COCOON.get(), SporeeggRenderer::new);
                EntityRenderers.register(ModEntities.TENTACLE.get(), TentacleRenderer::new);
                MenuScreens.register(ModMenuTypes.MEDIC_BLOCK_CONSTRUCTOR_MENU.get(), MedicBlockContructorScreen::new);
                MenuScreens.register(ModMenuTypes.SCIENTIST_MENU.get(), ScientistScreen::new);
                MenuScreens.register(ModMenuTypes.CRYO_MENU.get(), cryoscreen::new);
                MenuScreens.register(ModMenuTypes.MOUND_TERRARIUM_MENU.get(), MoundTerrariumScreen::new);
                MenuScreens.register(ModMenuTypes.RAID_CONTROLER.get(), RaidControlerScreen::new);

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
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("radar_scan_hud", ClientRadarHUD.HUD_RADAR);
            event.registerAboveAll("verva_countdown", VervaCountdownOverlay.OVERLAY);
        }
    }
}