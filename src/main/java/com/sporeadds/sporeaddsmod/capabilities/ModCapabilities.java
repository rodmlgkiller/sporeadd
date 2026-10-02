package com.sporeadds.sporeaddsmod.capabilities;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerData;
import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.level.PlayerLevel;
import com.sporeadds.sporeaddsmod.spore.PlayerSpore;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registro explícito de todas las capabilities del mod. Forge tolera capabilities sin
 * registrar, pero deja su serialización en un estado no garantizado; registrarlas evita
 * fallos sutiles de persistencia (p. ej. implantes que "desaparecen" al reconectar).
 */
@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {

    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerImplantsCapability.IPlayerImplants.class);
        event.register(CompoundsCapability.ICompounds.class);
        event.register(PlayerData.class);
        event.register(PlayerLevel.class);
        event.register(SporeIdentifierData.class);
        event.register(PlayerSpore.class);
        event.register(ScientistResearchData.class);
    }
}
