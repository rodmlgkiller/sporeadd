package com.sporeadds.sporeaddsmod.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Client-only mirror of the local player's gluttonous ammo fragments, synced from the server.
 * Just like the old action-bar text it replaced, the bar only stays visible for a few seconds after
 * the last change (a fragment gained or fired) and then hides itself again, even if fragments remain.
 */
public final class ClientGluttonousFragmentsState {

    private static final long VISIBLE_DURATION_MS = 3000L;

    private static List<String> fragments = new ArrayList<>();
    private static long lastChangeMs = 0L;

    private ClientGluttonousFragmentsState() {
    }

    public static void set(String fragmentsCsv) {
        if (fragmentsCsv == null || fragmentsCsv.isEmpty()) {
            fragments = new ArrayList<>();
        } else {
            fragments = new ArrayList<>(Arrays.asList(fragmentsCsv.split(",")));
        }
        lastChangeMs = System.currentTimeMillis();
    }

    public static List<String> get() {
        return fragments;
    }

    public static boolean shouldShow() {
        return !fragments.isEmpty() && (System.currentTimeMillis() - lastChangeMs) < VISIBLE_DURATION_MS;
    }
}
