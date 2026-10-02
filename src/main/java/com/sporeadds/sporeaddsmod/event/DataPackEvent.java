package com.sporeadds.sporeaddsmod.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PathPackResources;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataPackEvent {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {

            // Creamos una lista dinámica basada en los booleans de la config
            List<String> originsToHide = new ArrayList<>();
            if (SporeAddsConfig.HIDE_GHOST.get()) originsToHide.add("ghost");
            if (SporeAddsConfig.HIDE_KOMMANDANT.get()) originsToHide.add("kommandant");
            if (SporeAddsConfig.HIDE_MEDIC.get()) originsToHide.add("medic");
            if (SporeAddsConfig.HIDE_SCIENTIST.get()) originsToHide.add("scientist");
            if (SporeAddsConfig.HIDE_BERSERKER.get()) originsToHide.add("berserker");

            // Si no hay ninguno para ocultar, no creamos el datapack
            if (originsToHide.isEmpty()) {
                return;
            }

            try {
                Path tempDir = Files.createTempDirectory("sporeadds_hidden_origins");
                Path mcmeta = tempDir.resolve("pack.mcmeta");
                Files.write(mcmeta, "{\"pack\":{\"pack_format\":15,\"description\":\"SporeAdds Hidden Origins\"}}".getBytes(StandardCharsets.UTF_8));

                // El namespace es fijo ahora
                String namespace = "sporeadd";
                Path originDir = tempDir.resolve("data").resolve(namespace).resolve("origins");
                Files.createDirectories(originDir);

                for (String name : originsToHide) {
                    // Origins (io.github.edwinmindcraft.origins.common.data.OriginLoader) NO fusiona
                    // campo a campo los distintos JSON de un mismo origin: se queda con el ÚNICO de
                    // mayor "loading_priority" y descarta el resto por completo. Si aquí solo
                    // escribiéramos {"unchoosable": true}, ESE archivo se convertiría en la definición
                    // COMPLETA del origin, perdiendo powers/icon/impact/name/description (quedaría un
                    // origin fantasma sin poderes, aunque "dar la clase" siga pareciendo funcionar).
                    // Por eso partimos del JSON real del origin y solo tocamos unchoosable encima.
                    JsonObject origin = readBaseOriginJson(name);
                    origin.addProperty("unchoosable", true);
                    origin.addProperty("loading_priority", 999);

                    Path originFile = originDir.resolve(name + ".json");
                    Files.write(originFile, GSON.toJson(origin).getBytes(StandardCharsets.UTF_8));
                }

                Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
                    @Override
                    public net.minecraft.server.packs.PackResources open(String id) {
                        return new PathPackResources(id, tempDir, true);
                    }
                };

                Pack pack = Pack.readMetaAndCreate(
                        "sporeadds_hidden",
                        Component.literal("SporeAdds Hidden Origins"),
                        true,
                        supplier,
                        PackType.SERVER_DATA,
                        Pack.Position.TOP,
                        PackSource.BUILT_IN
                );

                if (pack != null) {
                    event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
                }

            } catch (IOException e) {
                System.err.println("Failed to generate virtual datapack for hidden origins:");
                e.printStackTrace();
            }
        }
    }

    /** Lee el JSON real (empaquetado en el propio mod) del origin, para partir de su contenido completo. */
    private static JsonObject readBaseOriginJson(String name) throws IOException {
        String path = "/data/sporeadd/origins/" + name + ".json";
        try (InputStream in = DataPackEvent.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Missing bundled origin resource: " + path);
            }
            JsonElement parsed = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            if (!parsed.isJsonObject()) {
                throw new IOException("Origin resource is not a JSON object: " + path);
            }
            return parsed.getAsJsonObject();
        }
    }
}
