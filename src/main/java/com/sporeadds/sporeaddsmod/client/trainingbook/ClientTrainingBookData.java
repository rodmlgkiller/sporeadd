package com.sporeadds.sporeaddsmod.client.trainingbook;

import com.sporeadds.sporeaddsmod.network.SyncClassCountsPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientTrainingBookData {

    private static Map<String, SyncClassCountsPacket.Entry> entries = new HashMap<>();
    private static boolean received = false;

    private ClientTrainingBookData() {
    }

    public static void update(List<SyncClassCountsPacket.Entry> newEntries) {
        Map<String, SyncClassCountsPacket.Entry> map = new HashMap<>();
        for (SyncClassCountsPacket.Entry entry : newEntries) {
            map.put(entry.classId.toLowerCase(), entry);
        }
        entries = map;
        received = true;
    }

    public static boolean hasData() {
        return received;
    }

    public static int getCount(String classId) {
        SyncClassCountsPacket.Entry entry = entries.get(classId == null ? "" : classId.toLowerCase());
        return entry == null ? 0 : entry.count;
    }

    public static int getMaxSlots(String classId) {
        SyncClassCountsPacket.Entry entry = entries.get(classId == null ? "" : classId.toLowerCase());
        return entry == null ? 0 : entry.maxSlots;
    }

    public static boolean isEnabled(String classId) {
        SyncClassCountsPacket.Entry entry = entries.get(classId == null ? "" : classId.toLowerCase());
        return entry != null && entry.enabled;
    }

    public static boolean hasFreeSlot(String classId) {
        int max = getMaxSlots(classId);
        return max < 0 || getCount(classId) < max;
    }
}
