package com.sporeadds.sporeaddsmod.PlayerData;

import net.minecraft.nbt.CompoundTag;
import java.util.List;

public class SporeIdentifierData {
    public static final List<String> VALID_IDS = List.of(
            "kommandant",
            "ghost",
            "medic",
            "scientist",
            "berserker",
            "none"
    );

    /** Nombre antiguo de la clase berserker; se migra al cargar NBT. */
    private static final String LEGACY_BERSERKER_ID = "slasher";

    private String currentIdentifier = "none";
    private String currentSubclass = "none";

    private boolean camouflaged = false;
    private int camouflageCooldown = 0;

    private boolean receivedTrainingBook = false;

    public String getIdentifier() {
        return currentIdentifier;
    }

    public String getSubclass() {
        return currentSubclass;
    }

    public boolean setIdentifier(String newIdentifier, boolean resetSubclass) {
        if (newIdentifier == null || !VALID_IDS.contains(newIdentifier)) {
            return false;
        }
        if (newIdentifier.equals(this.currentIdentifier)) {
            return false;
        }

        this.currentIdentifier = newIdentifier;
        if (resetSubclass) {
            this.currentSubclass = "none";
        }
        return true;
    }

    public boolean setIdentifier(String newIdentifier) {
        return setIdentifier(newIdentifier, true);
    }

    public void setSubclass(String newSubclass) {
        this.currentSubclass = newSubclass != null ? newSubclass : "none";
    }

    public boolean isCamouflaged() {
        return camouflaged;
    }

    public void setCamouflaged(boolean camouflaged) {
        this.camouflaged = camouflaged;
    }

    public boolean isCamouflageOnCooldown() {
        return camouflageCooldown > 0;
    }

    public int getCamouflageCooldown() {
        return camouflageCooldown;
    }

    public void setCamouflageCooldown(int ticks) {
        this.camouflageCooldown = Math.max(0, ticks);
    }

    public void tickCamouflageCooldown() {
        if (camouflageCooldown > 0) {
            camouflageCooldown--;
        }
    }

    public boolean hasReceivedTrainingBook() {
        return receivedTrainingBook;
    }

    public void setReceivedTrainingBook(boolean receivedTrainingBook) {
        this.receivedTrainingBook = receivedTrainingBook;
    }

    public void copyFrom(SporeIdentifierData source) {
        if (source != null) {
            this.currentIdentifier = source.currentIdentifier;
            this.currentSubclass = source.currentSubclass;
            this.camouflaged = source.camouflaged;
            this.camouflageCooldown = source.camouflageCooldown;
            this.receivedTrainingBook = source.receivedTrainingBook;
        }
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putString("SporeRoleId", currentIdentifier);
        nbt.putString("SporeSubclassId", currentSubclass);
        nbt.putBoolean("SporeCamouflaged", camouflaged);
        nbt.putInt("SporeCamouflageCooldown", camouflageCooldown);
        nbt.putBoolean("SporeReceivedTrainingBook", receivedTrainingBook);
    }

    public void loadNBTData(CompoundTag nbt) {
        if (!nbt.contains("SporeRoleId")) {
            return;
        }

        String loadedId = nbt.getString("SporeRoleId");

        // Migración: la clase "slasher" pasó a llamarse "berserker".
        if (LEGACY_BERSERKER_ID.equals(loadedId)) {
            loadedId = "berserker";
        }

        if (loadedId.equals("none") && !this.currentIdentifier.equals("none")) {
            return;
        }

        if (!setIdentifier(loadedId, false)) {
            if (VALID_IDS.contains(loadedId)) {
                this.currentIdentifier = loadedId;
            }
        }

        if (nbt.contains("SporeSubclassId")) {
            this.currentSubclass = nbt.getString("SporeSubclassId");
        }

        if (nbt.contains("SporeCamouflaged")) {
            this.camouflaged = nbt.getBoolean("SporeCamouflaged");
        }
        if (nbt.contains("SporeCamouflageCooldown")) {
            this.camouflageCooldown = nbt.getInt("SporeCamouflageCooldown");
        }
        if (nbt.contains("SporeReceivedTrainingBook")) {
            this.receivedTrainingBook = nbt.getBoolean("SporeReceivedTrainingBook");
        }
    }
}