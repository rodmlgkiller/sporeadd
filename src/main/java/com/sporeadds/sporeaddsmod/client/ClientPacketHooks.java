package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerData;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.capabilities.PlayerImplantsCapability;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.research.ResearchPopupManager;
import com.sporeadds.sporeaddsmod.research.ResearchPopupType;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Client-side halves of the network packets. Keeping them out of the packet classes means the packets can be
 * linked on a dedicated server without ever touching net.minecraft.client classes. Only call these through
 * {@code DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHooks.x(...))}.
 */
public final class ClientPacketHooks {

    private ClientPacketHooks() {
    }

    public static void syncSpore(int spore) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(cap -> cap.setSpore(spore));
        }
    }

    public static void syncLevel(int level, int knowledgeLevel) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(cap -> {
                cap.setLevel(level);
                cap.setKnowledgeLevel(knowledgeLevel);
            });
        }
    }

    public static void syncMoundCount(CompoundTag nbt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            PlayerSporeProvider.PLAYER_CAP.get(mc.player).ifPresent(cap -> cap.loadNBTData(nbt));
        }
    }

    public static void serverToData(String value) {
        PlayerDataProvider.PLAYER_DATA.get(Minecraft.getInstance().player).ifPresent(cap -> cap.setSwitch(value));
    }

    public static void eyesData(int playerId, int eyeBaseType, int eyeGlowType, int glowOffsetX, int glowOffsetY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(playerId);
        if (!(entity instanceof Player player)) return;

        PlayerData data = PlayerDataProvider.get(player);
        data.setEyeBaseType(eyeBaseType);
        data.setEyeGlowType(eyeGlowType);
        data.setGlowOffset(glowOffsetX, glowOffsetY);
    }

    public static void researchPopup(ResearchPopupType type, String entityId, int amount) {
        if (Minecraft.getInstance().level != null) {
            ResearchPopupManager.push(type, entityId, amount);
        }
    }

    public static void defibrillationCooldown(int cooldownTicks) {
        if (Minecraft.getInstance().level != null) {
            DelayedDefibrillationCooldownClientState.setCooldown(cooldownTicks);
        }
    }

    public static void implantSync(UUID playerUuid, CompoundTag implantData) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Player player = mc.level.getPlayerByUUID(playerUuid);
        if (player != null) {
            PlayerImplantsCapability.PLAYER_IMPLANTS.get(player).ifPresent(implants ->
                    implants.deserializeNBT(player.level().registryAccess(), implantData));
        }
    }

    public static void scientistResearch(Map<String, Integer> kills, Map<String, Integer> data) {
        playFirstKillSoundsIfApplicable(kills);

        FieldResearchClientData.setKills(kills);
        FieldResearchClientData.setDataAmounts(data);
        FieldResearchClientData.openBookWhenReady();
    }

    private static void playFirstKillSoundsIfApplicable(Map<String, Integer> newKills) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        boolean isScientist = SporeIdentifierProvider.SPORE_IDENTIFIER.get(mc.player)
                .map(d -> "scientist".equalsIgnoreCase(d.getIdentifier())).orElse(false);
        if (!isScientist) {
            return;
        }

        for (Map.Entry<String, Integer> entry : newKills.entrySet()) {
            int newCount = entry.getValue();
            int oldCount = FieldResearchClientData.getKillCount(entry.getKey());

            if (oldCount == 0 && newCount > 0) {
                mc.level.playLocalSound(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        SoundEvents.BOOK_PUT,
                        SoundSource.PLAYERS,
                        1.0F, 1.0F, false
                );
            }
        }
    }
}
