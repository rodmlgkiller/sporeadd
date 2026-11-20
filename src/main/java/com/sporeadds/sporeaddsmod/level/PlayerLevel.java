package com.sporeadds.sporeaddsmod.level;

import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import net.minecraft.nbt.CompoundTag;

public class PlayerLevel {
    private int SporeLevel;
    private int knowledge_level; // NUEVA VARIABLE
    private final int MIN_LEVEL = 0;
    /** Hard cap of the knowledge level and of every level table (the configurable level cap is {@link #levelCap()}). */
    private final int MAX_LEVEL = 9;

    /** Highest evolution level allowed right now (kommandant_abilities.general.max_evolution_level, default 9). */
    private static int levelCap() {
        return KommandantPowersConfig.maxEvolutionLevel();
    }

    public int getLevel() {
        return SporeLevel;
    }

    public int getKnowledgeLevel() {
        return knowledge_level;
    }

    public void addLevel(int add) {
        this.SporeLevel = Math.min(SporeLevel + add, levelCap());
        updateKnowledgeLevel();
    }

    public void subLevel(int sub) {
        // CORRECCIÓN: Antes era Math.min, debía ser Math.max para no bajar de MIN_LEVEL
        // (a level left above a lowered cap counts as the cap, so losing levels starts from the cap).
        this.SporeLevel = Math.max(Math.min(SporeLevel, levelCap()) - sub, MIN_LEVEL);
    }

    public void setLevel(int value) {
        // Mantiene el nivel dentro de los límites permitidos
        this.SporeLevel = Math.max(MIN_LEVEL, Math.min(value, levelCap()));
        updateKnowledgeLevel();
    }

    public void setKnowledgeLevel(int value) {
        this.knowledge_level = Math.max(MIN_LEVEL, Math.min(value, MAX_LEVEL));
    }

    private void updateKnowledgeLevel() {
        // Si el nivel actual es mayor que el knowledge_level, actualizarlo
        if (this.SporeLevel > this.knowledge_level) {
            this.knowledge_level = this.SporeLevel;
        }
    }

    public void copyFrom(PlayerLevel source) {
        this.SporeLevel = source.SporeLevel;
        this.knowledge_level = source.knowledge_level; // Asegurar que el conocimiento también persiste al morir/clonar
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("spore_level", SporeLevel);
        nbt.putInt("knowledge_level", knowledge_level);
    }

    public void loadNBTData(CompoundTag nbt) {
        SporeLevel = nbt.getInt("spore_level");

        if (nbt.contains("knowledge_level")) {
            knowledge_level = nbt.getInt("knowledge_level");
        } else {
            // Retrocompatibilidad: Si es un jugador viejo que no tenía la variable, su knowledge empieza igual que su nivel actual
            knowledge_level = SporeLevel;
        }
    }
}