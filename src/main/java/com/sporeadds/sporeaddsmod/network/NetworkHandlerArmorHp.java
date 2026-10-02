package com.sporeadds.sporeaddsmod.network;

/** The armor-hp packet now goes through the main payload registration in {@link NetworkHandle}. */
public class NetworkHandlerArmorHp {

    public static PacketChannel getChannel() {
        return NetworkHandle.INSTANCE;
    }
}
