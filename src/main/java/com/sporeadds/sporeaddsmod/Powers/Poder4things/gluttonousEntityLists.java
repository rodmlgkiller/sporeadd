package com.sporeadds.sporeaddsmod.Powers.Poder4things;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class gluttonousEntityLists {

    public static final ResourceLocation MEAT_ABOMINATION_ID = new ResourceLocation("sporeadd", "meat_abomination");
    public static final ResourceLocation SEASONED_ID = new ResourceLocation("sporeadd", "seasoned");

    public static final Set<ResourceLocation> BASIC_ENTITIES = Set.of(
            new ResourceLocation("spore", "inf_human"),
            new ResourceLocation("spore", "inf_husk"),
            new ResourceLocation("spore", "inf_player"),
            new ResourceLocation("spore", "inf_villager"),
            new ResourceLocation("spore", "inf_witch"),
            new ResourceLocation("spore", "inf_pillager"),
            new ResourceLocation("spore", "inf_vindicator"),
            new ResourceLocation("spore", "inf_evoker"),
            new ResourceLocation("spore", "inf_wanderer"),
            new ResourceLocation("spore", "inf_drowned"),
            new ResourceLocation("spore", "inf_hazmat"),
            new ResourceLocation("spore", "plagued"),
            new ResourceLocation("spore", "lacerator"),
            new ResourceLocation("spore", "biobloob"),
            new ResourceLocation("spore", "saugling"),
            new ResourceLocation("spore", "scamper"),
            new ResourceLocation("spore", "chemist"),
            new ResourceLocation("spore", "inf_diseased_villager"),
            new ResourceLocation("spore", "claw"),
            new ResourceLocation("spore", "hevoker_arm"),
            new ResourceLocation("spore", "bairn")
    );

    public static final Set<ResourceLocation> STRONG_ENTITIES = Set.of(
            new ResourceLocation("spore", "knight"),
            new ResourceLocation("spore", "griefer"),
            new ResourceLocation("spore", "braiomil"),
            new ResourceLocation("spore", "busser"),
            new ResourceLocation("spore", "leaper"),
            new ResourceLocation("spore", "slasher"),
            new ResourceLocation("spore", "spitter"),
            new ResourceLocation("spore", "howler"),
            new ResourceLocation("spore", "stalker"),
            new ResourceLocation("spore", "brute"),
            new ResourceLocation("spore", "volatile"),
            new ResourceLocation("spore", "inebriater"),
            new ResourceLocation("spore", "thorn"),
            new ResourceLocation("spore", "jagd"),
            new ResourceLocation("spore", "scavenger"),
            new ResourceLocation("spore", "bloater"),
            new ResourceLocation("spore", "nuclea"),
            new ResourceLocation("spore", "protector"),
            new ResourceLocation("spore", "gastgaber"),
            new ResourceLocation("spore", "specter"),
            new ResourceLocation("spore", "howit_arm"),
            new ResourceLocation("spore", "sieger_tail"),
            new ResourceLocation("spore", "licker"),
            new ResourceLocation("spore", "corpse_piece"),
            new ResourceLocation("spore", "conductor"),
            new ResourceLocation("spore", "gargoyle"),
            new ResourceLocation("spore", "reaper"),
            new ResourceLocation("spore", "vanguard"),
            new ResourceLocation("spore", "naiad"),
            new ResourceLocation("spore", "mephitic"),
            new ResourceLocation("spore", "gorgon")
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