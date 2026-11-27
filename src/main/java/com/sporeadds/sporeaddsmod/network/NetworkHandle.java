package com.sporeadds.sporeaddsmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandle {

    private static final String PROTOCOL_VERSION = "1.0";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("sporeadd", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.registerMessage(
                nextId(),
                SyncSporePacket.class,
                SyncSporePacket::encode,
                SyncSporePacket::decode,
                SyncSporePacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncLevelPacket.class,
                SyncLevelPacket::encode,
                SyncLevelPacket::decode,
                SyncLevelPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                DataToServer.class,
                DataToServer::encode,
                DataToServer::decode,
                DataToServer::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                ServerToData.class,
                ServerToData::encode,
                ServerToData::decode,
                ServerToData::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                PowerUseHandler.class,
                PowerUseHandler::encode,
                PowerUseHandler::decode,
                PowerUseHandler::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                Poder5UsePacket.class,
                Poder5UsePacket::encode,
                Poder5UsePacket::decode,
                Poder5UsePacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                Poder12ActivatePacket.class,
                Poder12ActivatePacket::encode,
                Poder12ActivatePacket::decode,
                Poder12ActivatePacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                Poder13UsePacket.class,
                Poder13UsePacket::encode,
                Poder13UsePacket::decode,
                Poder13UsePacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncMoundCountPacket.class,
                SyncMoundCountPacket::encode,
                SyncMoundCountPacket::decode,
                SyncMoundCountPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                StartCraftingPacket.class,
                StartCraftingPacket::encode,
                StartCraftingPacket::decode,
                StartCraftingPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                OpenImplantGuiPacket.class,
                OpenImplantGuiPacket::encode,
                OpenImplantGuiPacket::decode,
                OpenImplantGuiPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncImplantDataPacket.class,
                SyncImplantDataPacket::encode,
                SyncImplantDataPacket::decode,
                SyncImplantDataPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncImplantPacket.class,
                SyncImplantPacket::encode,
                SyncImplantPacket::decode,
                SyncImplantPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                ResearchRequestPacket.class,
                ResearchRequestPacket::encode,
                ResearchRequestPacket::decode,
                ResearchRequestPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                Poder6UsePacket.class,
                Poder6UsePacket::encode,
                Poder6UsePacket::decode,
                Poder6UsePacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncVervaTravelPacket.class,
                SyncVervaTravelPacket::toBytes,
                SyncVervaTravelPacket::new,
                SyncVervaTravelPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SpawnVervaPacket.class,
                SpawnVervaPacket::encode,
                SpawnVervaPacket::decode,
                SpawnVervaPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                VigilRadarPacket.class,
                VigilRadarPacket::toBytes,
                VigilRadarPacket::new,
                VigilRadarPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncSporeIdentifierPacket.class,
                SyncSporeIdentifierPacket::encode,
                SyncSporeIdentifierPacket::decode,
                SyncSporeIdentifierPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                RequestVervaGuiPacket.class,
                RequestVervaGuiPacket::encode,
                RequestVervaGuiPacket::decode,
                RequestVervaGuiPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncEvolutionCostsPacket.class,
                SyncEvolutionCostsPacket::encode,
                SyncEvolutionCostsPacket::decode,
                SyncEvolutionCostsPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncVervaGuiPacket.class,
                SyncVervaGuiPacket::encode,
                SyncVervaGuiPacket::decode,
                SyncVervaGuiPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                CausticShotPacket.class,
                CausticShotPacket::encode,
                CausticShotPacket::decode,
                CausticShotPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncVervaCountdownPacket.class,
                SyncVervaCountdownPacket::encode,
                SyncVervaCountdownPacket::decode,
                SyncVervaCountdownPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                UpdateMoundLocationPacket.class,
                UpdateMoundLocationPacket::encode,
                UpdateMoundLocationPacket::decode,
                UpdateMoundLocationPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncKommandantEspPacket.class,
                SyncKommandantEspPacket::encode,
                SyncKommandantEspPacket::decode,
                SyncKommandantEspPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                EyesDataSyncPacket.class,
                EyesDataSyncPacket::toBytes,
                EyesDataSyncPacket::new,
                EyesDataSyncPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncBlomfungTweakPacket.class,
                SyncBlomfungTweakPacket::encode,
                SyncBlomfungTweakPacket::decode,
                SyncBlomfungTweakPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncPelletTweakPacket.class,
                SyncPelletTweakPacket::encode,
                SyncPelletTweakPacket::decode,
                SyncPelletTweakPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                nextId(),
                gluttonousShotPacket.class,
                gluttonousShotPacket::encode,
                gluttonousShotPacket::decode,
                gluttonousShotPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );
        INSTANCE.registerMessage(
                nextId(),
                AbyssalTentaclePacket.class,
                AbyssalTentaclePacket::encode,
                AbyssalTentaclePacket::decode,
                AbyssalTentaclePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );
        INSTANCE.registerMessage(
                nextId(),
                SyncgluttonousCrosshairRenderPacket.class,
                SyncgluttonousCrosshairRenderPacket::encode,
                SyncgluttonousCrosshairRenderPacket::decode,
                SyncgluttonousCrosshairRenderPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncgluttonousCrosshairAttackPacket.class,
                SyncgluttonousCrosshairAttackPacket::encode,
                SyncgluttonousCrosshairAttackPacket::decode,
                SyncgluttonousCrosshairAttackPacket::handle
        );
        INSTANCE.registerMessage(
                nextId(),
                RequestLevelSyncPacket.class,
                RequestLevelSyncPacket::encode,
                RequestLevelSyncPacket::decode,
                RequestLevelSyncPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                StartRaidPacket.class,
                StartRaidPacket::toBytes,
                StartRaidPacket::new,
                StartRaidPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                SetInjectorModePacket.class,
                SetInjectorModePacket::encode,
                SetInjectorModePacket::decode,
                SetInjectorModePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                ActivateCamouflagePacket.class,
                ActivateCamouflagePacket::encode,
                ActivateCamouflagePacket::decode,
                ActivateCamouflagePacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncCamouflagePacket.class,
                SyncCamouflagePacket::encode,
                SyncCamouflagePacket::decode,
                SyncCamouflagePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncCamouflageCooldownPacket.class,
                SyncCamouflageCooldownPacket::encode,
                SyncCamouflageCooldownPacket::decode,
                SyncCamouflageCooldownPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                ActivateDecoyPacket.class,
                ActivateDecoyPacket::toBytes,
                ActivateDecoyPacket::new,
                ActivateDecoyPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncDecoyCooldownPacket.class,
                SyncDecoyCooldownPacket::toBytes,
                SyncDecoyCooldownPacket::new,
                SyncDecoyCooldownPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                ActivateFieldResearchPacket.class,
                ActivateFieldResearchPacket::toBytes,
                ActivateFieldResearchPacket::new,
                ActivateFieldResearchPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncScientistResearchPacket.class,
                SyncScientistResearchPacket::toBytes,
                SyncScientistResearchPacket::new,
                SyncScientistResearchPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                ExposeWeaknessPacket.class,
                ExposeWeaknessPacket::toBytes,
                ExposeWeaknessPacket::new,
                ExposeWeaknessPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncExposeWeaknessCooldownPacket.class,
                SyncExposeWeaknessCooldownPacket::toBytes,
                SyncExposeWeaknessCooldownPacket::new,
                SyncExposeWeaknessCooldownPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncWeakPointPacket.class,
                SyncWeakPointPacket::toBytes,
                SyncWeakPointPacket::new,
                SyncWeakPointPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),// el siguiente entero disponible en tu secuencia
                ResearchPopupPacket.class,
                ResearchPopupPacket::toBytes,
                ResearchPopupPacket::new,
                ResearchPopupPacket::handle
        );

        INSTANCE.registerMessage(
                nextId(),
                RequestDelayedDefibrillationPacket.class,
                RequestDelayedDefibrillationPacket::toBytes,
                RequestDelayedDefibrillationPacket::new,
                RequestDelayedDefibrillationPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncDelayedDefibrillationCooldownPacket.class,
                SyncDelayedDefibrillationCooldownPacket::toBytes,
                SyncDelayedDefibrillationCooldownPacket::new,
                SyncDelayedDefibrillationCooldownPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                OpenSelfDefibrillateScreenPacket.class,
                OpenSelfDefibrillateScreenPacket::encode,
                OpenSelfDefibrillateScreenPacket::decode,
                OpenSelfDefibrillateScreenPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                RequestSelfDefibrillateStartPacket.class,
                RequestSelfDefibrillateStartPacket::encode,
                RequestSelfDefibrillateStartPacket::decode,
                RequestSelfDefibrillateStartPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                SubmitSelfDefibrillateChoicePacket.class,
                SubmitSelfDefibrillateChoicePacket::encode,
                SubmitSelfDefibrillateChoicePacket::decode,
                SubmitSelfDefibrillateChoicePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncSelfDefibrillateCooldownPacket.class,
                SyncSelfDefibrillateCooldownPacket::toBytes,
                SyncSelfDefibrillateCooldownPacket::new,
                SyncSelfDefibrillateCooldownPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                CloseSelfDefibrillateScreenPacket.class,
                CloseSelfDefibrillateScreenPacket::encode,
                CloseSelfDefibrillateScreenPacket::decode,
                CloseSelfDefibrillateScreenPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        NetworkHandle.INSTANCE.registerMessage(
                nextId(),
                SelectClassPacket.class,
                SelectClassPacket::encode,
                SelectClassPacket::decode,
                SelectClassPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                RequestClassCountsPacket.class,
                RequestClassCountsPacket::encode,
                RequestClassCountsPacket::decode,
                RequestClassCountsPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncClassCountsPacket.class,
                SyncClassCountsPacket::encode,
                SyncClassCountsPacket::decode,
                SyncClassCountsPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncHiveDownedPacket.class,
                SyncHiveDownedPacket::encode,
                SyncHiveDownedPacket::decode,
                SyncHiveDownedPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                StartHiveCinematicPacket.class,
                StartHiveCinematicPacket::encode,
                StartHiveCinematicPacket::decode,
                StartHiveCinematicPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                HiveDialogueAdvancePacket.class,
                HiveDialogueAdvancePacket::encode,
                HiveDialogueAdvancePacket::decode,
                HiveDialogueAdvancePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                SubmitHiveChoicePacket.class,
                SubmitHiveChoicePacket::encode,
                SubmitHiveChoicePacket::decode,
                SubmitHiveChoicePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                HiveCinematicDonePacket.class,
                HiveCinematicDonePacket::encode,
                HiveCinematicDonePacket::decode,
                HiveCinematicDonePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncHiveChancePacket.class,
                SyncHiveChancePacket::encode,
                SyncHiveChancePacket::decode,
                SyncHiveChancePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                ActivateCounterPacket.class,
                ActivateCounterPacket::encode,
                ActivateCounterPacket::decode,
                ActivateCounterPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                ActivateClawsPacket.class,
                ActivateClawsPacket::encode,
                ActivateClawsPacket::decode,
                ActivateClawsPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncBerserkerCooldownPacket.class,
                SyncBerserkerCooldownPacket::encode,
                SyncBerserkerCooldownPacket::decode,
                SyncBerserkerCooldownPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncClawCounterPacket.class,
                SyncClawCounterPacket::encode,
                SyncClawCounterPacket::decode,
                SyncClawCounterPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncClawsActivePacket.class,
                SyncClawsActivePacket::encode,
                SyncClawsActivePacket::decode,
                SyncClawsActivePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                OpenCompoundsPacket.class,
                OpenCompoundsPacket::encode,
                OpenCompoundsPacket::decode,
                OpenCompoundsPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncCompoundsPacket.class,
                SyncCompoundsPacket::encode,
                SyncCompoundsPacket::decode,
                SyncCompoundsPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncGasSpheresPacket.class,
                SyncGasSpheresPacket::encode,
                SyncGasSpheresPacket::decode,
                SyncGasSpheresPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                TriggerCameraShakePacket.class,
                TriggerCameraShakePacket::encode,
                TriggerCameraShakePacket::decode,
                TriggerCameraShakePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncGluttonousFragmentsPacket.class,
                SyncGluttonousFragmentsPacket::encode,
                SyncGluttonousFragmentsPacket::decode,
                SyncGluttonousFragmentsPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                SyncAbyssalStormPacket.class,
                SyncAbyssalStormPacket::encode,
                SyncAbyssalStormPacket::decode,
                SyncAbyssalStormPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                CausticShotScalePacket.class,
                CausticShotScalePacket::encode,
                CausticShotScalePacket::decode,
                CausticShotScalePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                CausticSprayModePacket.class,
                CausticSprayModePacket::encode,
                CausticSprayModePacket::decode,
                CausticSprayModePacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextId(),
                FireCausticSprayShotPacket.class,
                FireCausticSprayShotPacket::encode,
                FireCausticSprayShotPacket::decode,
                FireCausticSprayShotPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
        );

    }
}