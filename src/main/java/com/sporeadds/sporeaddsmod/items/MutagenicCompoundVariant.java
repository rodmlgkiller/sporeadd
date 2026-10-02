package com.sporeadds.sporeaddsmod.items;

public enum MutagenicCompoundVariant {
    ORIGINAL("none", 0, "item.sporeadd.mutagenic_compound.original"),
    CAUSTIC("caustic", 1, "item.sporeadd.mutagenic_compound.caustic"),
    ABYSSAL("abyssal", 2, "item.sporeadd.mutagenic_compound.abyssal"),
    GLUTTONOUS("gluttonous", 3, "item.sporeadd.mutagenic_compound.gluttonous");

    private final String subclassId;
    private final int customModelData;
    private final String descriptionKey;

    MutagenicCompoundVariant(String subclassId, int customModelData, String descriptionKey) {
        this.subclassId = subclassId;
        this.customModelData = customModelData;
        this.descriptionKey = descriptionKey;
    }

    public String getId() {
        return subclassId;
    }

    public String getSubclassId() {
        return subclassId;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public static MutagenicCompoundVariant byId(String id) {
        for (MutagenicCompoundVariant variant : values()) {
            if (variant.subclassId.equalsIgnoreCase(id)) {
                return variant;
            }
        }
        return ORIGINAL;
    }
}