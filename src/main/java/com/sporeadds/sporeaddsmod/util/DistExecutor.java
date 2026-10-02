package com.sporeadds.sporeaddsmod.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

/** Compatibility replacement for Forge's DistExecutor (removed in NeoForge). */
public final class DistExecutor {

    private DistExecutor() {
    }

    public interface SafeRunnable extends Runnable {
    }

    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        if (FMLEnvironment.dist == dist) {
            toRun.get().run();
        }
    }

    public static void safeRunWhenOn(Dist dist, Supplier<SafeRunnable> toRun) {
        if (FMLEnvironment.dist == dist) {
            toRun.get().run();
        }
    }
}
