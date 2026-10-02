package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.ClientCausticShotScaleState;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/** S->C: tells the client to render a just-fired Caustic assassin bullet (by entity id) at an extra
 * visual-only scale (1.0-1.5) based on how charged the shot was. Never touches the entity's hitbox. */
public class CausticShotScalePacket {
    private final int entityId;
    private final float scale;

    public CausticShotScalePacket(int entityId, float scale) {
        this.entityId = entityId;
        this.scale = scale;
    }

    public CausticShotScalePacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.scale = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(scale);
    }

    public static CausticShotScalePacket decode(FriendlyByteBuf buf) {
        return new CausticShotScalePacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientCausticShotScaleState.set(entityId, scale)));
        ctx.setPacketHandled(true);
    }
}
