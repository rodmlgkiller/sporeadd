package com.sporeadds.origins;

import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import io.github.edwinmindcraft.origins.api.registry.OriginsDynamicRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Acceso directo a la API de Origins. Esta clase (y sus imports de Origins) SOLO se carga
 * cuando se la referencia, y todas las referencias están detrás de
 * {@code ModList.get().isLoaded("origins")}, así que es seguro con Origins como dependencia
 * opcional ausente.
 *
 * Se usa la API en vez de {@code /origin set ...} porque el comando lleva un argumento de capa
 * ({@code /origin set <targets> <layer> <origin>}) y además fallaba en silencio.
 */
public final class OriginApiBridge {

    private static final ResourceLocation MAIN_LAYER_ID = ResourceLocation.fromNamespaceAndPath("origins", "origin");

    private OriginApiBridge() {
    }

    private static ResourceKey<OriginLayer> mainLayerKey() {
        return ResourceKey.create(OriginsDynamicRegistries.LAYERS_REGISTRY, MAIN_LAYER_ID);
    }

    private static ResourceKey<Origin> originKey(ResourceLocation originId) {
        return ResourceKey.create(OriginsDynamicRegistries.ORIGINS_REGISTRY, originId);
    }

    /** Fija el origin de la capa principal y sincroniza al cliente (igual que hace {@code /origin set}). */
    public static void setOrigin(ServerPlayer player, ResourceLocation originId) {
        ResourceKey<OriginLayer> layerKey = mainLayerKey();
        ResourceKey<Origin> origin = originKey(originId);

        IOriginContainer.get(player).ifPresent(container -> {
            container.setOrigin(layerKey, origin);
            container.synchronize();
        });
    }

    /** @return el id del origin actual del jugador en la capa principal, o null. */
    public static ResourceLocation currentMainOrigin(ServerPlayer player) {
        IOriginContainer container = IOriginContainer.get(player).resolve().orElse(null);
        if (container == null) {
            return null;
        }
        ResourceKey<Origin> key = container.getOrigin(mainLayerKey());
        return key == null ? null : key.location();
    }
}
