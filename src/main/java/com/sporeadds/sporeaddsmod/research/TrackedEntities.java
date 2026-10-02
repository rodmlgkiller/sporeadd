package com.sporeadds.sporeaddsmod.research;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TrackedEntities {
    public static final String KOMMANDANT_PLAYER_ID = "sporeadd:kommandant_player";
    public record Entry(String id, int rarity) {
    }

    public static final ResourceLocation GOLDEN_FRAME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/golden.png");

    public static final List<Entry> ENTRIES;

    static {
        List<Entry> raw = new ArrayList<>(List.of(
                new Entry("spore:bairn", 1),
                new Entry("spore:hevoker_arm", 1),
                new Entry("spore:inf_human", 1),
                new Entry("spore:inf_husk", 1),
                new Entry("spore:inf_player", 1),
                new Entry("spore:inf_villager", 1),
                new Entry("spore:inf_pillager", 1),
                new Entry("spore:inf_wanderer", 1),
                new Entry("spore:inf_drowned", 1),
                new Entry("spore:inf_hazmat", 1),
                new Entry("spore:saugling", 1),
                new Entry("spore:inf_diseased_villager", 1),
                new Entry("spore:scamper", 2),
                new Entry("spore:plagued", 2),
                new Entry("spore:chemist", 2),
                new Entry("spore:jagd", 2),
                new Entry("spore:scavenger", 2),
                new Entry("spore:bloater", 2),
                new Entry("spore:naiad", 2),
                new Entry("spore:inf_witch", 1),
                new Entry("spore:lacerator", 2),
                new Entry("spore:knight", 2),
                new Entry("spore:griefer", 2),
                new Entry("spore:braiomil", 2),
                new Entry("spore:busser", 2),
                new Entry("spore:leaper", 2),
                new Entry("spore:slasher", 2),
                new Entry("spore:inebriater", 2),
                new Entry("spore:brute", 2),
                new Entry("spore:stalker", 2),
                new Entry("spore:gargoyle", 2),
                new Entry("spore:inf_vindicator", 2),
                new Entry("spore:gorgon", 2),
                new Entry("spore:spitter", 2),
                new Entry("spore:howler", 2),
                new Entry("spore:thorn", 2),
                new Entry("spore:nuclea", 2),
                new Entry("spore:protector", 2),
                new Entry("spore:gastgaber", 2),
                new Entry("spore:conductor", 2),
                new Entry("spore:inf_evoker", 1),
                new Entry("spore:inquisitor", 3),
                new Entry("spore:wendigo", 3),
                new Entry("spore:biobloob", 2),
                new Entry("spore:brot", 3),
                new Entry("spore:inf_contruct", 3),
                new Entry("spore:grober", 3),
                new Entry("spore:ogre", 3),
                new Entry("spore:mephitic", 2),
                new Entry("spore:volatile", 2),
                new Entry("spore:hevoker", 3),
                new Entry("spore:hvindicator", 3),
                new Entry("spore:reaper", 3),
                new Entry("spore:specter", 3),
                new Entry("spore:vanguard", 3),
                new Entry("spore:umarmed", 3),
                new Entry("spore:usurper", 3),
                new Entry("spore:braurei", 3),
                new Entry("spore:delusioner", 3),
                new Entry("spore:proto", 4),
                new Entry("spore:mound", 1),
                new Entry("spore:vigil", 3),
                new Entry("spore:reconstructor", 3),
                new Entry("spore:hivetumor", 3),
                new Entry("spore:sieger", 4),
                new Entry("spore:howitzer", 4),
                new Entry("spore:stahl", 4),
                new Entry("spore:hohlfresser", 4),
                new Entry("spore:gazenbreacher", 4),
                new Entry("spore:kraken", 4),
                new Entry("spore:leviathan", 4),
                new Entry("spore:hindenburg", 4),
                new Entry("spore:verfall", 4)

        ));
        raw.add(new Entry(KOMMANDANT_PLAYER_ID, 4));
        raw.sort(Comparator.comparingInt(Entry::rarity));
        ENTRIES = List.copyOf(raw);
    }

    public static final List<String> ENTITY_IDS = ENTRIES.stream().map(Entry::id).toList();

    public static boolean isHalfScaleRender(String entityId) {
        return switch (entityId) {
            case "spore:sieger", "spore:howitzer", "spore:stahl", "spore:hohlfresser",
                 "spore:gazenbreacher", "spore:kraken", "spore:leviathan",
                 "spore:hindenburg", "spore:verfall" -> true;
            default -> false;
        };
    }

    public static boolean isKommandantPlayerEntry(String entityId) {
        return KOMMANDANT_PLAYER_ID.equals(entityId);
    }

    public static int getRarity(String entityId) {
        for (Entry entry : ENTRIES) {
            if (entry.id().equals(entityId)) {
                return entry.rarity();
            }
        }
        return 1;
    }

    public static ResourceLocation getPageTexture(int rarity) {
        int clamped = Math.max(1, Math.min(4, rarity));
        return ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/page" + clamped + ".png");
    }

    private TrackedEntities() {
    }
}