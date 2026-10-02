package com.sporeadds.sporeaddsmod.util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "sporeadd")
public final class DelayedTaskScheduler {

    private static final List<ScheduledTask> TASKS = new ArrayList<>();

    private DelayedTaskScheduler() {
    }

    public static void schedule(int tickDelay, Runnable task) {
        TASKS.add(new ScheduledTask(tickDelay, task));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        List<ScheduledTask> toRun = new ArrayList<>();
        for (ScheduledTask task : TASKS) {
            task.remainingTicks--;
            if (task.remainingTicks <= 0) {
                toRun.add(task);
            }
        }

        TASKS.removeAll(toRun);
        for (ScheduledTask task : toRun) {
            task.runnable.run();
        }
    }

    private static class ScheduledTask {
        int remainingTicks;
        final Runnable runnable;

        ScheduledTask(int remainingTicks, Runnable runnable) {
            this.remainingTicks = remainingTicks;
            this.runnable = runnable;
        }
    }
}