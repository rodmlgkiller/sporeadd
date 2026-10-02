package com.sporeadds.sporeaddsmod.research;

import com.sporeadds.sporeaddsmod.research.ResearchPopupType;
import com.sporeadds.sporeaddsmod.research.TrackedEntities;

import java.util.ArrayList;
import java.util.List;

public final class ResearchPopupManager {

    public static class Popup {
        public final ResearchPopupType type;
        public final String entityId;
        public int amount;
        public long expiresAtMillis;
        public long spawnedAtMillis;

        Popup(ResearchPopupType type, String entityId, int amount) {
            this.type = type;
            this.entityId = entityId;
            this.amount = amount;
            this.spawnedAtMillis = System.currentTimeMillis();
            this.expiresAtMillis = spawnedAtMillis + durationForType(type);
        }
    }

    private static final long UNLOCK_DURATION_MS = 4000L;
    private static final long PRESTIGE_DURATION_MS = 4500L;
    private static final long DATA_DURATION_MS = 5000L;

    private static final List<Popup> activePopups = new ArrayList<>();

    private ResearchPopupManager() {
    }

    private static long durationForType(ResearchPopupType type) {
        return switch (type) {
            case UNLOCK -> UNLOCK_DURATION_MS;
            case PRESTIGE -> PRESTIGE_DURATION_MS;
            case DATA -> DATA_DURATION_MS;
        };
    }

    public static synchronized void push(ResearchPopupType type, String entityId, int amount) {
        if (type == ResearchPopupType.DATA) {
            for (Popup popup : activePopups) {
                if (popup.type == ResearchPopupType.DATA && popup.entityId.equals(entityId)) {
                    popup.amount += amount;
                    popup.expiresAtMillis = System.currentTimeMillis() + DATA_DURATION_MS;
                    return;
                }
            }
        }

        Popup popup = new Popup(type, entityId, amount);
        activePopups.add(popup);
        playSoundForType(type, entityId);
    }

    private static void playSoundForType(ResearchPopupType type, String entityId) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;

        switch (type) {
            case UNLOCK -> {
                int rarity = TrackedEntities.getRarity(entityId);
                net.minecraft.sounds.SoundEvent sound = unlockSoundForRarity(rarity);
                float pitch = unlockPitchForRarity(rarity);
                float volume = unlockVolumeForRarity(rarity);
                mc.player.playSound(sound, volume, pitch);
            }
            case PRESTIGE -> {
                mc.player.playSound(net.minecraft.sounds.SoundEvents.TOTEM_USE, 1.0F, 1.0F);
                mc.player.playSound(net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 1.0F);
            }
            case DATA -> {
                // Silencioso: el sonido ya se reproduce en el servidor al recoger el item físico.
            }
        }
    }

    private static net.minecraft.sounds.SoundEvent unlockSoundForRarity(int rarity) {
        return switch (rarity) {
            case 1 -> net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP;
            case 2 -> net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP;
            case 3 -> net.minecraft.sounds.SoundEvents.UI_TOAST_IN;
            case 4 -> net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;
            default -> net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP;
        };
    }

    private static float unlockPitchForRarity(int rarity) {
        return switch (rarity) {
            case 1 -> 1.4F;
            case 2 -> 1.1F;
            case 3 -> 0.9F;
            case 4 -> 0.7F;
            default -> 1.0F;
        };
    }

    private static float unlockVolumeForRarity(int rarity) {
        return switch (rarity) {
            case 1 -> 0.6F;
            case 2 -> 0.8F;
            case 3 -> 1.0F;
            case 4 -> 1.0F;
            default -> 0.7F;
        };
    }

    public static synchronized void tick() {
        long now = System.currentTimeMillis();
        activePopups.removeIf(popup -> popup.expiresAtMillis <= now);
    }

    public static synchronized List<Popup> getActivePopups() {
        return new ArrayList<>(activePopups);
    }
}