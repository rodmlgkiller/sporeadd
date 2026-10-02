package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.*;
import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PowerRegistry {

    public PowerRegistry() {}

    private static final Map<Integer, PowerBase> POWERS = new HashMap<>();

    static {
        POWERS.put(1, new Poder1());
        POWERS.put(3, new Poder3());
        POWERS.put(8, new Poder8());
        POWERS.put(9, new Poder9());
        POWERS.put(10, new Poder10());
        POWERS.put(11, new Poder11());
    }

    public static PowerBase getPower(int id) {
        return POWERS.get(id);
    }
}
