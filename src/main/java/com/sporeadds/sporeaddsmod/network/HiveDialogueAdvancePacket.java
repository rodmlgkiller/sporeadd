package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S -> C. Tras la decisión del jugador, indica al cliente qué bloque de diálogo reproducir:
 *  - "very_well" -> "Very well..." y quedarse ahí (rendición, mientras dura el transporte de la verwa)
 *  - "arise"     -> "Now arise..." y luego revertir/cerrar (rendición, al despertar al jugador)
 *  - "perish"    -> "Then perish" y luego revertir/cerrar (rechazo)
 *  - "abort"     -> cerrar la cinemática inmediatamente (caso de emergencia del servidor)
 */
public class HiveDialogueAdvancePacket {

    public static final String PHASE_VERY_WELL = "very_well";
    public static final String PHASE_ARISE = "arise";
    public static final String PHASE_PERISH = "perish";
    public static final String PHASE_ABORT = "abort";

    private final String phase;

    public HiveDialogueAdvancePacket(String phase) {
        this.phase = phase;
    }

    public static void encode(HiveDialogueAdvancePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.phase);
    }

    public static HiveDialogueAdvancePacket decode(FriendlyByteBuf buf) {
        return new HiveDialogueAdvancePacket(buf.readUtf());
    }

    public static void handle(HiveDialogueAdvancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        String phase = msg.phase;
        ctx.get().enqueueWork(() ->
                com.sporeadds.sporeaddsmod.client.hive.HiveClientHooks.advancePhase(phase)
        );
        ctx.get().setPacketHandled(true);
    }
}
