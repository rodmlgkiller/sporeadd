package com.sporeadds.sporeaddsmod.client.renderer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClientgluttonousCrosshairRenderState {

    private static final Map<Integer, Long> LAST_SYNC = new HashMap<>();
    private static final Map<Integer, Long> ATTACK_START = new HashMap<>();

    private static final long TIMEOUT_MS = 1500L;
    private static final long ATTACK_TIMEOUT_MS = 3000L;

    public static final long ATTACK_DURATION_MS = 50L;
    public static final long RECOVERY_DURATION_MS = 1000L;
    public static final long TOTAL_ATTACK_COOLDOWN_MS = ATTACK_DURATION_MS + RECOVERY_DURATION_MS;

    public static void mark(int playerId) {
        LAST_SYNC.put(playerId, System.currentTimeMillis());
    }

    public static boolean shouldRender(int playerId) {
        Long last = LAST_SYNC.get(playerId);
        if (last == null) return false;
        return System.currentTimeMillis() - last <= TIMEOUT_MS;
    }

    public static void markAttack(int playerId) {
        ATTACK_START.put(playerId, System.currentTimeMillis());
    }

    public static long getAttackStart(int playerId) {
        return ATTACK_START.getOrDefault(playerId, -1L);
    }

    public static void clearAttack(int playerId) {
        ATTACK_START.remove(playerId);
    }

    public static boolean isAttackOnCooldown(int playerId) {
        long attackStart = getAttackStart(playerId);
        if (attackStart < 0L) return false;
        return System.currentTimeMillis() - attackStart < TOTAL_ATTACK_COOLDOWN_MS;
    }

    public static long getRemainingAttackCooldown(int playerId) {
        long attackStart = getAttackStart(playerId);
        if (attackStart < 0L) return 0L;

        long remaining = TOTAL_ATTACK_COOLDOWN_MS - (System.currentTimeMillis() - attackStart);
        return Math.max(0L, remaining);
    }

    public static void cleanup() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<Integer, Long>> syncIt = LAST_SYNC.entrySet().iterator();
        while (syncIt.hasNext()) {
            Map.Entry<Integer, Long> entry = syncIt.next();
            if (now - entry.getValue() > TIMEOUT_MS) {
                syncIt.remove();
            }
        }

        Iterator<Map.Entry<Integer, Long>> attackIt = ATTACK_START.entrySet().iterator();
        while (attackIt.hasNext()) {
            Map.Entry<Integer, Long> entry = attackIt.next();
            if (now - entry.getValue() > ATTACK_TIMEOUT_MS) {
                attackIt.remove();
            }
        }
    }

    public static void clear() {
        LAST_SYNC.clear();
        ATTACK_START.clear();
    }
}