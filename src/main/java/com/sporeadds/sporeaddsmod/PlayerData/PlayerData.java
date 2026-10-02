package com.sporeadds.sporeaddsmod.PlayerData;

import com.sporeadds.sporeaddsmod.mound.MoundRegistry;
import com.sporeadds.sporeaddsmod.network.ArmorHpSyncPacket;
import com.sporeadds.sporeaddsmod.network.EyesDataSyncPacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.NetworkHandlerArmorHp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkDirection;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerData {

    private String Switches = "000000000";
    private int armorHp = 0;
    private int eyeBaseType = 1;
    private int eyeGlowType = 1;
    private int glowOffsetX = 0;
    private int glowOffsetY = 0;
    private final MoundRegistry moundRegistry = new MoundRegistry();

    public String getSwitch() {
        return Switches;
    }

    public void setSwitch(String value) {
        this.Switches = value;
    }

    public int getArmorHp() {
        return armorHp;
    }

    public void setArmorHp(int value) {
        armorHp = Math.max(0, value);
    }

    public void setArmorHpAndSync(int value, ServerPlayer player) {
        armorHp = Math.max(0, value);
        if (player != null && !player.level().isClientSide) {
            NetworkHandlerArmorHp.getChannel().sendTo(
                    new ArmorHpSyncPacket(armorHp),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }
    }

    public void addArmorHp(int value) {
        setArmorHp(this.armorHp + value);
    }

    public int getGlowOffsetX() {
        return glowOffsetX;
    }

    public int getGlowOffsetY() {
        return glowOffsetY;
    }

    public void setGlowOffset(int x, int y) {
        this.glowOffsetX = Math.max(-3, Math.min(3, x));
        this.glowOffsetY = Math.max(-3, Math.min(3, y));
    }

    public void resetGlowOffset() {
        this.glowOffsetX = 0;
        this.glowOffsetY = 0;
    }

    public int getEyeBaseType() {
        return eyeBaseType;
    }

    public void setEyeBaseType(int value) {
        eyeBaseType = Math.max(1, Math.min(4, value));
    }

    public int getEyeGlowType() {
        return eyeGlowType;
    }

    public void setEyeGlowType(int value) {
        eyeGlowType = Math.max(1, Math.min(4, value));
    }

    public void setEyesAndSync(int baseType, int glowType, ServerPlayer player) {
        this.eyeBaseType = Math.max(1, Math.min(4, baseType));
        this.eyeGlowType = Math.max(1, Math.min(4, glowType));

        if (player != null && !player.level().isClientSide) {
            syncEyes(player);
        }
    }

    public void setEyeBaseTypeAndSync(int value, ServerPlayer player) {
        this.eyeBaseType = Math.max(1, Math.min(4, value));

        if (player != null && !player.level().isClientSide) {
            syncEyes(player);
        }
    }

    public void setEyeGlowTypeAndSync(int value, ServerPlayer player) {
        this.eyeGlowType = Math.max(1, Math.min(4, value));

        if (player != null && !player.level().isClientSide) {
            syncEyes(player);
        }
    }

    public void setGlowOffsetAndSync(int x, int y, ServerPlayer player) {
        setGlowOffset(x, y);

        if (player != null && !player.level().isClientSide) {
            syncEyes(player);
        }
    }

    private void syncEyes(ServerPlayer player) {
        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new EyesDataSyncPacket(
                        player.getId(),
                        this.eyeBaseType,
                        this.eyeGlowType,
                        this.glowOffsetX,
                        this.glowOffsetY
                )
        );
    }

    public MoundRegistry getMoundRegistry() {
        return moundRegistry;
    }

    public void copyFrom(PlayerData source) {
        this.Switches = source.Switches;
        this.armorHp = Math.max(0, source.armorHp);
        this.eyeBaseType = source.eyeBaseType;
        this.eyeGlowType = source.eyeGlowType;
        this.glowOffsetX = source.glowOffsetX;
        this.glowOffsetY = source.glowOffsetY;
        this.moundRegistry.clear();
        this.moundRegistry.getList().addAll(source.getMoundRegistry().getList());
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putString("switches", Switches);
        nbt.putInt("armorhp", armorHp);
        nbt.putInt("eyeBaseType", eyeBaseType);
        nbt.putInt("eyeGlowType", eyeGlowType);
        nbt.putInt("glowOffsetX", glowOffsetX);
        nbt.putInt("glowOffsetY", glowOffsetY);
        moundRegistry.saveNBT(nbt);
    }

    public void loadNBTData(CompoundTag nbt) {
        Switches = nbt.getString("switches");
        armorHp = Math.max(0, nbt.getInt("armorhp"));
        eyeBaseType = nbt.contains("eyeBaseType") ? Math.max(1, Math.min(4, nbt.getInt("eyeBaseType"))) : 1;
        eyeGlowType = nbt.contains("eyeGlowType") ? Math.max(1, Math.min(4, nbt.getInt("eyeGlowType"))) : 1;
        glowOffsetX = nbt.contains("glowOffsetX") ? Math.max(-3, Math.min(3, nbt.getInt("glowOffsetX"))) : 0;
        glowOffsetY = nbt.contains("glowOffsetY") ? Math.max(-3, Math.min(3, nbt.getInt("glowOffsetY"))) : 0;
        moundRegistry.loadNBT(nbt);
    }
}