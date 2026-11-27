package com.sporeadds.sporeaddsmod.Powers.Poder4things;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class gluttonousEntityLists {

    public static final ResourceLocation MEAT_ABOMINATION_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "meat_abomination");
    public static final ResourceLocation SEASONED_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "seasoned");

    public static final Set<ResourceLocation> BASIC_ENTITIES = Set.of(
            ResourceLocation.fromNamespaceAndPath("spore", "inf_human"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_husk"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_player"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_villager"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_witch"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_pillager"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_vindicator"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_evoker"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_wanderer"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_drowned"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_hazmat"),
            ResourceLocation.fromNamespaceAndPath("spore", "plagued"),
            ResourceLocation.fromNamespaceAndPath("spore", "lacerator"),
            ResourceLocation.fromNamespaceAndPath("spore", "biobloob"),
            ResourceLocation.fromNamespaceAndPath("spore", "saugling"),
            ResourceLocation.fromNamespaceAndPath("spore", "scamper"),
            ResourceLocation.fromNamespaceAndPath("spore", "chemist"),
            ResourceLocation.fromNamespaceAndPath("spore", "inf_diseased_villager"),
            ResourceLocation.fromNamespaceAndPath("spore", "claw"),
            ResourceLocation.fromNamespaceAndPath("spore", "hevoker_arm"),
            ResourceLocation.fromNamespaceAndPath("spore", "bairn")
    );

    public static final Set<ResourceLocation> STRONG_ENTITIES = Set.of(
            ResourceLocation.fromNamespaceAndPath("spore", "knight"),
            ResourceLocation.fromNamespaceAndPath("spore", "griefer"),
            ResourceLocation.fromNamespaceAndPath("spore", "braiomil"),
            ResourceLocation.fromNamespaceAndPath("spore", "busser"),
            ResourceLocation.fromNamespaceAndPath("spore", "leaper"),
            ResourceLocation.fromNamespaceAndPath("spore", "slasher"),
            ResourceLocation.fromNamespaceAndPath("spore", "spitter"),
            ResourceLocation.fromNamespaceAndPath("spore", "howler"),
            ResourceLocation.fromNamespaceAndPath("spore", "stalker"),
            ResourceLocation.fromNamespaceAndPath("spore", "brute"),
            ResourceLocation.fromNamespaceAndPath("spore", "volatile"),
            ResourceLocation.fromNamespaceAndPath("spore", "inebriater"),
            ResourceLocation.fromNamespaceAndPath("spore", "thorn"),
            ResourceLocation.fromNamespaceAndPath("spore", "jagd"),
            ResourceLocation.fromNamespaceAndPath("spore", "scavenger"),
            ResourceLocation.fromNamespaceAndPath("spore", "bloater"),
            ResourceLocation.fromNamespaceAndPath("spore", "nuclea"),
            ResourceLocation.fromNamespaceAndPath("spore", "protector"),
            ResourceLocation.fromNamespaceAndPath("spore", "gastgaber"),
            ResourceLocation.fromNamespaceAndPath("spore", "specter"),
            ResourceLocation.fromNamespaceAndPath("spore", "howit_arm"),
            ResourceLocation.fromNamespaceAndPath("spore", "sieger_tail"),
            ResourceLocation.fromNamespaceAndPath("spore", "licker"),
            ResourceLocation.fromNamespaceAndPath("spore", "corpse_piece"),
            ResourceLocation.fromNamespaceAndPath("spore", "conductor"),
            ResourceLocation.fromNamespaceAndPath("spore", "gargoyle"),
            ResourceLocation.fromNamespaceAndPath("spore", "reaper"),
            ResourceLocation.fromNamespaceAndPath("spore", "vanguard"),
            ResourceLocation.fromNamespaceAndPath("spore", "naiad"),
            ResourceLocation.fromNamespaceAndPath("spore", "mephitic"),
            ResourceLocation.fromNamespaceAndPath("spore", "gorgon")
    );

    private gluttonousEntityLists() {
    }

    public static boolean isBasicEntity(ResourceLocation id) {
        return BASIC_ENTITIES.contains(id);
    }

    public static boolean isStrongEntity(ResourceLocation id) {
        return STRONG_ENTITIES.contains(id);
    }

    public static boolean isHarvestableEntity(ResourceLocation id) {
        return isBasicEntity(id) || isStrongEntity(id);
    }
}