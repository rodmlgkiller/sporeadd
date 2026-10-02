package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandle {

    private static final String PROTOCOL_VERSION = "1";

    public static final PacketChannel INSTANCE = new PacketChannel();

    private enum Dir { BOTH, TO_CLIENT, TO_SERVER }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar(PROTOCOL_VERSION);

        reg(r, SyncSporePacket.class, SyncSporePacket::encode, SyncSporePacket::decode, SyncSporePacket::handle, Dir.BOTH);
        reg(r, SyncLevelPacket.class, SyncLevelPacket::encode, SyncLevelPacket::decode, SyncLevelPacket::handle, Dir.BOTH);
        reg(r, ArmorHpSyncPacket.class, ArmorHpSyncPacket::encode, ArmorHpSyncPacket::decode, ArmorHpSyncPacket::handle, Dir.BOTH);
        reg(r, DataToServer.class, DataToServer::encode, DataToServer::decode, DataToServer::handle, Dir.BOTH);
        reg(r, ServerToData.class, ServerToData::encode, ServerToData::decode, ServerToData::handle, Dir.BOTH);
        reg(r, PowerUseHandler.class, PowerUseHandler::encode, PowerUseHandler::decode, PowerUseHandler::handle, Dir.BOTH);
        reg(r, Poder5UsePacket.class, Poder5UsePacket::encode, Poder5UsePacket::decode, Poder5UsePacket::handle, Dir.BOTH);
        reg(r, Poder12ActivatePacket.class, Poder12ActivatePacket::encode, Poder12ActivatePacket::decode, Poder12ActivatePacket::handle, Dir.BOTH);
        reg(r, Poder13UsePacket.class, Poder13UsePacket::encode, Poder13UsePacket::decode, Poder13UsePacket::handle, Dir.BOTH);
        reg(r, SyncMoundCountPacket.class, SyncMoundCountPacket::encode, SyncMoundCountPacket::decode, SyncMoundCountPacket::handle, Dir.BOTH);
        reg(r, StartCraftingPacket.class, StartCraftingPacket::encode, StartCraftingPacket::decode, StartCraftingPacket::handle, Dir.BOTH);
        reg(r, OpenImplantGuiPacket.class, OpenImplantGuiPacket::encode, OpenImplantGuiPacket::decode, OpenImplantGuiPacket::handle, Dir.BOTH);
        reg(r, SyncImplantDataPacket.class, SyncImplantDataPacket::encode, SyncImplantDataPacket::decode, SyncImplantDataPacket::handle, Dir.BOTH);
        reg(r, SyncImplantPacket.class, SyncImplantPacket::encode, SyncImplantPacket::decode, SyncImplantPacket::handle, Dir.BOTH);
        reg(r, ResearchRequestPacket.class, ResearchRequestPacket::encode, ResearchRequestPacket::decode, ResearchRequestPacket::handle, Dir.BOTH);
        reg(r, Poder6UsePacket.class, Poder6UsePacket::encode, Poder6UsePacket::decode, Poder6UsePacket::handle, Dir.BOTH);
        reg(r, SyncVervaTravelPacket.class, SyncVervaTravelPacket::toBytes, SyncVervaTravelPacket::new, SyncVervaTravelPacket::handle, Dir.BOTH);
        reg(r, SpawnVervaPacket.class, SpawnVervaPacket::encode, SpawnVervaPacket::decode, SpawnVervaPacket::handle, Dir.BOTH);
        reg(r, VigilRadarPacket.class, VigilRadarPacket::toBytes, VigilRadarPacket::new, VigilRadarPacket::handle, Dir.BOTH);
        reg(r, SyncSporeIdentifierPacket.class, SyncSporeIdentifierPacket::encode, SyncSporeIdentifierPacket::decode, SyncSporeIdentifierPacket::handle, Dir.BOTH);
        reg(r, RequestVervaGuiPacket.class, RequestVervaGuiPacket::encode, RequestVervaGuiPacket::decode, RequestVervaGuiPacket::handle, Dir.BOTH);
        reg(r, SyncEvolutionCostsPacket.class, SyncEvolutionCostsPacket::encode, SyncEvolutionCostsPacket::decode, SyncEvolutionCostsPacket::handle, Dir.BOTH);
        reg(r, SyncVervaGuiPacket.class, SyncVervaGuiPacket::encode, SyncVervaGuiPacket::decode, SyncVervaGuiPacket::handle, Dir.BOTH);
        reg(r, CausticShotPacket.class, CausticShotPacket::encode, CausticShotPacket::decode, CausticShotPacket::handle, Dir.BOTH);
        reg(r, SyncVervaCountdownPacket.class, SyncVervaCountdownPacket::encode, SyncVervaCountdownPacket::decode, SyncVervaCountdownPacket::handle, Dir.BOTH);
        reg(r, UpdateMoundLocationPacket.class, UpdateMoundLocationPacket::encode, UpdateMoundLocationPacket::decode, UpdateMoundLocationPacket::handle, Dir.BOTH);
        reg(r, SyncKommandantEspPacket.class, SyncKommandantEspPacket::encode, SyncKommandantEspPacket::decode, SyncKommandantEspPacket::handle, Dir.BOTH);
        reg(r, EyesDataSyncPacket.class, EyesDataSyncPacket::toBytes, EyesDataSyncPacket::new, EyesDataSyncPacket::handle, Dir.TO_CLIENT);
        reg(r, SyncBlomfungTweakPacket.class, SyncBlomfungTweakPacket::encode, SyncBlomfungTweakPacket::decode, SyncBlomfungTweakPacket::handle, Dir.TO_CLIENT);
        reg(r, SyncPelletTweakPacket.class, SyncPelletTweakPacket::encode, SyncPelletTweakPacket::decode, SyncPelletTweakPacket::handle, Dir.TO_CLIENT);
        reg(r, gluttonousShotPacket.class, gluttonousShotPacket::encode, gluttonousShotPacket::decode, gluttonousShotPacket::handle, Dir.TO_SERVER);
        reg(r, AbyssalTentaclePacket.class, AbyssalTentaclePacket::encode, AbyssalTentaclePacket::decode, AbyssalTentaclePacket::handle, Dir.TO_SERVER);
        reg(r, SyncgluttonousCrosshairRenderPacket.class, SyncgluttonousCrosshairRenderPacket::encode, SyncgluttonousCrosshairRenderPacket::decode, SyncgluttonousCrosshairRenderPacket::handle, Dir.BOTH);
        reg(r, SyncgluttonousCrosshairAttackPacket.class, SyncgluttonousCrosshairAttackPacket::encode, SyncgluttonousCrosshairAttackPacket::decode, SyncgluttonousCrosshairAttackPacket::handle, Dir.BOTH);
        reg(r, RequestLevelSyncPacket.class, RequestLevelSyncPacket::encode, RequestLevelSyncPacket::decode, RequestLevelSyncPacket::handle, Dir.BOTH);
        reg(r, StartRaidPacket.class, StartRaidPacket::toBytes, StartRaidPacket::new, StartRaidPacket::handle, Dir.BOTH);
        reg(r, SetInjectorModePacket.class, SetInjectorModePacket::encode, SetInjectorModePacket::decode, SetInjectorModePacket::handle, Dir.TO_SERVER);
        reg(r, ActivateCamouflagePacket.class, ActivateCamouflagePacket::encode, ActivateCamouflagePacket::decode, ActivateCamouflagePacket::handle, Dir.BOTH);
        reg(r, SyncCamouflagePacket.class, SyncCamouflagePacket::encode, SyncCamouflagePacket::decode, SyncCamouflagePacket::handle, Dir.TO_CLIENT);
        reg(r, SyncCamouflageCooldownPacket.class, SyncCamouflageCooldownPacket::encode, SyncCamouflageCooldownPacket::decode, SyncCamouflageCooldownPacket::handle, Dir.TO_CLIENT);
        reg(r, ActivateDecoyPacket.class, ActivateDecoyPacket::toBytes, ActivateDecoyPacket::new, ActivateDecoyPacket::handle, Dir.BOTH);
        reg(r, SyncDecoyCooldownPacket.class, SyncDecoyCooldownPacket::toBytes, SyncDecoyCooldownPacket::new, SyncDecoyCooldownPacket::handle, Dir.BOTH);
        reg(r, ActivateFieldResearchPacket.class, ActivateFieldResearchPacket::toBytes, ActivateFieldResearchPacket::new, ActivateFieldResearchPacket::handle, Dir.BOTH);
        reg(r, SyncScientistResearchPacket.class, SyncScientistResearchPacket::toBytes, SyncScientistResearchPacket::new, SyncScientistResearchPacket::handle, Dir.BOTH);
        reg(r, ExposeWeaknessPacket.class, ExposeWeaknessPacket::toBytes, ExposeWeaknessPacket::new, ExposeWeaknessPacket::handle, Dir.BOTH);
        reg(r, SyncExposeWeaknessCooldownPacket.class, SyncExposeWeaknessCooldownPacket::toBytes, SyncExposeWeaknessCooldownPacket::new, SyncExposeWeaknessCooldownPacket::handle, Dir.BOTH);
        reg(r, SyncWeakPointPacket.class, SyncWeakPointPacket::toBytes, SyncWeakPointPacket::new, SyncWeakPointPacket::handle, Dir.BOTH);
        reg(r, ResearchPopupPacket.class, ResearchPopupPacket::toBytes, ResearchPopupPacket::new, ResearchPopupPacket::handle, Dir.BOTH);
        reg(r, RequestDelayedDefibrillationPacket.class, RequestDelayedDefibrillationPacket::toBytes, RequestDelayedDefibrillationPacket::new, RequestDelayedDefibrillationPacket::handle, Dir.TO_SERVER);
        reg(r, SyncDelayedDefibrillationCooldownPacket.class, SyncDelayedDefibrillationCooldownPacket::toBytes, SyncDelayedDefibrillationCooldownPacket::new, SyncDelayedDefibrillationCooldownPacket::handle, Dir.TO_CLIENT);
        reg(r, OpenSelfDefibrillateScreenPacket.class, OpenSelfDefibrillateScreenPacket::encode, OpenSelfDefibrillateScreenPacket::decode, OpenSelfDefibrillateScreenPacket::handle, Dir.TO_CLIENT);
        reg(r, RequestSelfDefibrillateStartPacket.class, RequestSelfDefibrillateStartPacket::encode, RequestSelfDefibrillateStartPacket::decode, RequestSelfDefibrillateStartPacket::handle, Dir.TO_SERVER);
        reg(r, SubmitSelfDefibrillateChoicePacket.class, SubmitSelfDefibrillateChoicePacket::encode, SubmitSelfDefibrillateChoicePacket::decode, SubmitSelfDefibrillateChoicePacket::handle, Dir.TO_SERVER);
        reg(r, SyncSelfDefibrillateCooldownPacket.class, SyncSelfDefibrillateCooldownPacket::toBytes, SyncSelfDefibrillateCooldownPacket::new, SyncSelfDefibrillateCooldownPacket::handle, Dir.TO_CLIENT);
        reg(r, CloseSelfDefibrillateScreenPacket.class, CloseSelfDefibrillateScreenPacket::encode, CloseSelfDefibrillateScreenPacket::decode, CloseSelfDefibrillateScreenPacket::handle, Dir.TO_CLIENT);
        reg(r, SelectClassPacket.class, SelectClassPacket::encode, SelectClassPacket::decode, SelectClassPacket::handle, Dir.TO_SERVER);
        reg(r, RequestClassCountsPacket.class, RequestClassCountsPacket::encode, RequestClassCountsPacket::decode, RequestClassCountsPacket::handle, Dir.TO_SERVER);
        reg(r, SyncClassCountsPacket.class, SyncClassCountsPacket::encode, SyncClassCountsPacket::decode, SyncClassCountsPacket::handle, Dir.TO_CLIENT);
        reg(r, SyncHiveDownedPacket.class, SyncHiveDownedPacket::encode, SyncHiveDownedPacket::decode, SyncHiveDownedPacket::handle, Dir.TO_CLIENT);
        reg(r, StartHiveCinematicPacket.class, StartHiveCinematicPacket::encode, StartHiveCinematicPacket::decode, StartHiveCinematicPacket::handle, Dir.TO_CLIENT);
        reg(r, HiveDialogueAdvancePacket.class, HiveDialogueAdvancePacket::encode, HiveDialogueAdvancePacket::decode, HiveDialogueAdvancePacket::handle, Dir.TO_CLIENT);
        reg(r, SubmitHiveChoicePacket.class, SubmitHiveChoicePacket::encode, SubmitHiveChoicePacket::decode, SubmitHiveChoicePacket::handle, Dir.TO_SERVER);
        reg(r, HiveCinematicDonePacket.class, HiveCinematicDonePacket::encode, HiveCinematicDonePacket::decode, HiveCinematicDonePacket::handle, Dir.TO_SERVER);
        reg(r, SyncHiveChancePacket.class, SyncHiveChancePacket::encode, SyncHiveChancePacket::decode, SyncHiveChancePacket::handle, Dir.TO_CLIENT);
        reg(r, ActivateCounterPacket.class, ActivateCounterPacket::encode, ActivateCounterPacket::decode, ActivateCounterPacket::handle, Dir.TO_SERVER);
        reg(r, ActivateClawsPacket.class, ActivateClawsPacket::encode, ActivateClawsPacket::decode, ActivateClawsPacket::handle, Dir.TO_SERVER);
        reg(r, SyncBerserkerCooldownPacket.class, SyncBerserkerCooldownPacket::encode, SyncBerserkerCooldownPacket::decode, SyncBerserkerCooldownPacket::handle, Dir.TO_CLIENT);
        reg(r, SyncClawCounterPacket.class, SyncClawCounterPacket::encode, SyncClawCounterPacket::decode, SyncClawCounterPacket::handle, Dir.TO_CLIENT);
        reg(r, SyncClawsActivePacket.class, SyncClawsActivePacket::encode, SyncClawsActivePacket::decode, SyncClawsActivePacket::handle, Dir.TO_CLIENT);
        reg(r, OpenCompoundsPacket.class, OpenCompoundsPacket::encode, OpenCompoundsPacket::decode, OpenCompoundsPacket::handle, Dir.TO_SERVER);
        reg(r, SyncCompoundsPacket.class, SyncCompoundsPacket::encode, SyncCompoundsPacket::decode, SyncCompoundsPacket::handle, Dir.TO_CLIENT);
        reg(r, SyncGasSpheresPacket.class, SyncGasSpheresPacket::encode, SyncGasSpheresPacket::decode, SyncGasSpheresPacket::handle, Dir.TO_CLIENT);
        reg(r, TriggerCameraShakePacket.class, TriggerCameraShakePacket::encode, TriggerCameraShakePacket::decode, TriggerCameraShakePacket::handle, Dir.TO_CLIENT);
        reg(r, SyncGluttonousFragmentsPacket.class, SyncGluttonousFragmentsPacket::encode, SyncGluttonousFragmentsPacket::decode, SyncGluttonousFragmentsPacket::handle, Dir.TO_CLIENT);
        reg(r, SyncAbyssalStormPacket.class, SyncAbyssalStormPacket::encode, SyncAbyssalStormPacket::decode, SyncAbyssalStormPacket::handle, Dir.TO_CLIENT);
        reg(r, CausticShotScalePacket.class, CausticShotScalePacket::encode, CausticShotScalePacket::decode, CausticShotScalePacket::handle, Dir.TO_CLIENT);
        reg(r, CausticSprayModePacket.class, CausticSprayModePacket::encode, CausticSprayModePacket::decode, CausticSprayModePacket::handle, Dir.TO_CLIENT);
        reg(r, FireCausticSprayShotPacket.class, FireCausticSprayShotPacket::encode, FireCausticSprayShotPacket::decode, FireCausticSprayShotPacket::handle, Dir.TO_SERVER);
    }

    private static String idFor(Class<?> cls) {
        String n = cls.getSimpleName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT);
        return n;
    }

    private static <T> void reg(PayloadRegistrar r, Class<T> cls,
                                BiConsumer<T, FriendlyByteBuf> encoder,
                                Function<FriendlyByteBuf, T> decoder,
                                BiConsumer<T, Supplier<NetworkEvent.Context>> handler,
                                Dir dir) {
        CustomPacketPayload.Type<PacketWrapper> type =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("sporeadd", idFor(cls)));
        INSTANCE.bind(cls, type);

        StreamCodec<RegistryFriendlyByteBuf, PacketWrapper> codec = StreamCodec.of(
                (buf, wrapper) -> encoder.accept(cls.cast(wrapper.message()), buf),
                buf -> new PacketWrapper(type, decoder.apply(buf)));

        net.neoforged.neoforge.network.handling.IPayloadHandler<PacketWrapper> payloadHandler =
                (wrapper, context) -> {
                    NetworkEvent.Context ctx = new NetworkEvent.Context(context);
                    handler.accept(cls.cast(wrapper.message()), () -> ctx);
                };

        switch (dir) {
            case TO_CLIENT -> r.playToClient(type, codec, payloadHandler);
            case TO_SERVER -> r.playToServer(type, codec, payloadHandler);
            default -> r.playBidirectional(type, codec, payloadHandler);
        }
    }
}
