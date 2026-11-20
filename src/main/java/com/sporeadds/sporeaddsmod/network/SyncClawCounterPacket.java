package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.ClawCounterClientState;
import com.sporeadds.sporeaddsmod.config.BerserkerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S -> C (solo al jugador): valor del contador de Brutality, si sus garras están activas y la escala de la barra
 * (maximo y umbral de activacion segun la config del servidor, para que el HUD muestre los valores reales).
 */
public class SyncClawCounterPacket {

    private final float value;
    private final boolean active;
    private final float max;
    private final float activationThreshold;

    /** Solo para construirlo en el servidor: adjunta el maximo y el umbral configurados. */
    public SyncClawCounterPacket(float value, boolean active) {
        this(value, active, BerserkerConfig.clawsMaxPoints(), BerserkerConfig.clawsActivationThreshold());
    }

    public SyncClawCounterPacket(float value, boolean active, float max, float activationThreshold) {
        this.value = value;
        this.active = active;
        this.max = max;
        this.activationThreshold = activationThreshold;
    }

    public static void encode(SyncClawCounterPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.value);
        buf.writeBoolean(msg.active);
        buf.writeFloat(msg.max);
        buf.writeFloat(msg.activationThreshold);
    }

    public static SyncClawCounterPacket decode(FriendlyByteBuf buf) {
        return new SyncClawCounterPacket(buf.readFloat(), buf.readBoolean(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(SyncClawCounterPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClawCounterClientState.set(msg.value, msg.active, msg.max, msg.activationThreshold));
        ctx.get().setPacketHandled(true);
    }
}
