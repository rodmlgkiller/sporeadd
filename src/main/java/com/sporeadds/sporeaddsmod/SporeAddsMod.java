package com.sporeadds.sporeaddsmod;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.modblocksentity;
import com.sporeadds.sporeaddsmod.blocks.modblocks;
import com.sporeadds.sporeaddsmod.client.SporeKeyMapping;
import com.sporeadds.sporeaddsmod.client.screen.ModMenuTypes;
import com.sporeadds.sporeaddsmod.config.SporeAddsClientConfig;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import com.sporeadds.sporeaddsmod.event.ImplantBuffEvents;
import com.sporeadds.sporeaddsmod.items.MutagenicCompoundRecipesEnabledCondition;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod("sporeadd")
public class SporeAddsMod {

    public SporeAddsMod(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(
                ModConfig.Type.COMMON,
                SporeAddsConfig.SPEC,
                "sporeadds-config.toml"
        );
        container.registerConfig(
                ModConfig.Type.CLIENT,
                SporeAddsClientConfig.SPEC,
                "sporeadds-client.toml"
        );

        // Touch the provider classes so their attachment types get queued before the registry event fires.
        com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP.getClass();
        com.sporeadds.sporeaddsmod.level.PlayerLevelProvider.PLAYER_LVL.getClass();
        com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider.PLAYER_DATA.getClass();
        com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchProvider.SCIENTIST_RESEARCH.getClass();
        com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider.SPORE_IDENTIFIER.getClass();
        com.sporeadds.sporeaddsmod.capabilities.PlayerImplantsCapability.PLAYER_IMPLANTS.getClass();
        com.sporeadds.sporeaddsmod.capabilities.CompoundsCapability.PLAYER_COMPOUNDS.getClass();
        com.sporeadds.sporeaddsmod.capabilities.Capability.ATTACHMENTS.register(modEventBus);

        MutagenicCompoundRecipesEnabledCondition.CONDITION_CODECS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        effects.MOB_EFFECTS.register(modEventBus);
        modblocks.BLOCKS.register(modEventBus);
        modblocksentity.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModEntities.register(modEventBus);
        SporeaddParticleTypes.REGISTRY.register(modEventBus);

        modEventBus.addListener(NetworkHandle::register);

        NeoForge.EVENT_BUS.register(ImplantBuffEvents.class);

        modEventBus.addListener(this::commonSetup);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientInit.registerClientEvents(modEventBus));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Sincronización inversa origin -> clase. Solo si Origins está presente, para que la
            // clase handler (con imports de Origins) no se cargue cuando sea dependencia opcional ausente.
            if (ModList.get().isLoaded("origins")) {
                NeoForge.EVENT_BUS.register(com.sporeadds.origins.OriginClassSyncHandler.class);
            }
        });
    }

    public static class ClientInit {
        public static void registerClientEvents(IEventBus modEventBus) {
            modEventBus.addListener(SporeKeyMapping::register);
        }
    }
}