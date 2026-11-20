package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.config.MedicConfig;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Opens the Self Defibrillate minigame on the client. It carries the minigame rules (time limit, icons) because the
 * screen is drawn on the client but they are configured on the server.
 */
public class OpenSelfDefibrillateScreenPacket {

    private final int timeLimitTicks;
    private final int iconCount;
    private final int iconSize;
    private final boolean iconsMove;
    private final float iconSpeed;

    /** Uses the minigame settings of this side's config. */
    public OpenSelfDefibrillateScreenPacket() {
        this(MedicConfig.selfDefibrillateTimeLimitTicks(),
                MedicConfig.selfDefibrillateIconCount(),
                MedicConfig.selfDefibrillateIconSize(),
                MedicConfig.selfDefibrillateIconsMove(),
                MedicConfig.selfDefibrillateIconSpeed());
    }

    public OpenSelfDefibrillateScreenPacket(int timeLimitTicks, int iconCount, int iconSize, boolean iconsMove, float iconSpeed) {
        this.timeLimitTicks = timeLimitTicks;
        this.iconCount = iconCount;
        this.iconSize = iconSize;
        this.iconsMove = iconsMove;
        this.iconSpeed = iconSpeed;
    }

    public static void encode(OpenSelfDefibrillateScreenPacket msg, net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeVarInt(msg.timeLimitTicks);
        buf.writeVarInt(msg.iconCount);
        buf.writeVarInt(msg.iconSize);
        buf.writeBoolean(msg.iconsMove);
        buf.writeFloat(msg.iconSpeed);
    }

    public static OpenSelfDefibrillateScreenPacket decode(net.minecraft.network.FriendlyByteBuf buf) {
        int timeLimitTicks = buf.readVarInt();
        int iconCount = buf.readVarInt();
        int iconSize = buf.readVarInt();
        boolean iconsMove = buf.readBoolean();
        float iconSpeed = buf.readFloat();
        return new OpenSelfDefibrillateScreenPacket(timeLimitTicks, iconCount, iconSize, iconsMove, iconSpeed);
    }

    public static void handle(OpenSelfDefibrillateScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.sporeadds.sporeaddsmod.client.actionwheel.SelfDefibrillateClientHandler.openScreen(
                    msg.timeLimitTicks, msg.iconCount, msg.iconSize, msg.iconsMove, msg.iconSpeed);
        });
        ctx.get().setPacketHandled(true);
    }
}