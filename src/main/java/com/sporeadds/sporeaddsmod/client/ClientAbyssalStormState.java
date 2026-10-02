package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.network.SyncAbyssalStormPacket;

import java.util.ArrayList;
import java.util.List;

/** Client-only mirror of active Abyssal Power13 storm positions, used to hide vanilla rain nearby. */
public final class ClientAbyssalStormState {

    public static final double RADIUS = 200.0D;

    private static List<SyncAbyssalStormPacket.Storm> storms = new ArrayList<>();

    private ClientAbyssalStormState() {
    }

    public static void update(List<SyncAbyssalStormPacket.Storm> newStorms) {
        storms = newStorms;
    }

    public static boolean isNear(double x, double y, double z) {
        double radiusSq = RADIUS * RADIUS;
        for (SyncAbyssalStormPacket.Storm storm : storms) {
            double dx = x - storm.x;
            double dy = y - storm.y;
            double dz = z - storm.z;
            if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                return true;
            }
        }
        return false;
    }
}
