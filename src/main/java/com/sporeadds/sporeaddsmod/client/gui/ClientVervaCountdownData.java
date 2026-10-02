package com.sporeadds.sporeaddsmod.client.gui;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ClientVervaCountdownData {

    public static class CountdownEntry {
        public final UUID transportId;
        public int seconds;
        public int priority;
        public final long createdOrder;

        public CountdownEntry(UUID transportId, int seconds, int priority, long createdOrder) {
            this.transportId = transportId;
            this.seconds = seconds;
            this.priority = priority;
            this.createdOrder = createdOrder;
        }
    }

    private static final Map<UUID, CountdownEntry> ACTIVE = new LinkedHashMap<>();
    private static long orderCounter = 0L;

    public static void upsert(UUID transportId, int seconds, int priority) {
        CountdownEntry existing = ACTIVE.get(transportId);
        if (existing != null) {
            existing.seconds = seconds;
            existing.priority = priority;
            return;
        }

        ACTIVE.put(transportId, new CountdownEntry(transportId, seconds, priority, orderCounter++));
    }

    public static void remove(UUID transportId) {
        ACTIVE.remove(transportId);
    }

    public static CountdownEntry getHighestPriority() {
        return ACTIVE.values().stream()
                .filter(e -> e.seconds > 0)
                .min(Comparator
                        .comparingInt((CountdownEntry e) -> e.priority)
                        .thenComparingLong(e -> e.createdOrder))
                .orElse(null);
    }

    public static boolean hasActive() {
        return getHighestPriority() != null;
    }

    public static void clear() {
        ACTIVE.clear();
    }
}