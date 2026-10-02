package com.sporeadds.sporeaddsmod.combat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class WeakPointKillTracker {

    private record HitInfo(UUID attacker, long gameTime) {
    }

    private static final Map<Integer, HitInfo> lastWeakPointHit = new HashMap<>();
    private static final Map<Integer, UUID> realDeathAttacker = new HashMap<>();
    private static final long EXPIRY_TICKS = 40L;

    private WeakPointKillTracker() {
    }

    public static void recordHit(int entityId, UUID attackerId, long gameTime) {
        lastWeakPointHit.put(entityId, new HitInfo(attackerId, gameTime));
    }

    public static boolean wasKilledByWeakPoint(int entityId, UUID attackerId, long currentGameTime) {
        HitInfo info = lastWeakPointHit.get(entityId);
        if (info == null) {
            return false;
        }
        boolean sameAttacker = info.attacker().equals(attackerId);
        boolean recent = (currentGameTime - info.gameTime()) <= 1L;
        return sameAttacker && recent;
    }

    public static void recordRealKillAttacker(int entityId, UUID attackerId) {
        realDeathAttacker.put(entityId, attackerId);
    }

    public static UUID getRealKillAttacker(int entityId) {
        return realDeathAttacker.get(entityId);
    }

    public static void clear(int entityId) {
        lastWeakPointHit.remove(entityId);
        realDeathAttacker.remove(entityId);
    }

    public static void purgeStale(long currentGameTime) {
        Iterator<Map.Entry<Integer, HitInfo>> iterator = lastWeakPointHit.entrySet().iterator();
        while (iterator.hasNext()) {
            HitInfo info = iterator.next().getValue();
            if ((currentGameTime - info.gameTime()) > EXPIRY_TICKS) {
                iterator.remove();
            }
        }
    }
}