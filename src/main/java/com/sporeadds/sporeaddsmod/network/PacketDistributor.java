package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.function.Function;
import java.util.function.Supplier;

/** Compatibility shim for the old Forge PacketDistributor API on top of NeoForge's static senders. */
public final class PacketDistributor {

    private PacketDistributor() {
    }

    public interface Target {
        void send(CustomPacketPayload payload);
    }

    public static final class WithArg<T> {
        private final Function<T, Target> factory;

        WithArg(Function<T, Target> factory) {
            this.factory = factory;
        }

        public Target with(Supplier<T> argument) {
            return factory.apply(argument.get());
        }
    }

    public static final class NoArg {
        private final Target target;

        NoArg(Target target) {
            this.target = target;
        }

        public Target noArg() {
            return target;
        }
    }

    public record TargetPoint(double x, double y, double z, double radius, ResourceKey<Level> dimension) {
    }

    public static final WithArg<ServerPlayer> PLAYER =
            new WithArg<>(p -> payload -> com.sporeadds.sporeaddsmod.network.PacketDistributor.sendToPlayer(p, payload));

    public static final WithArg<Entity> TRACKING_ENTITY =
            new WithArg<>(e -> payload -> com.sporeadds.sporeaddsmod.network.PacketDistributor.sendToPlayersTrackingEntity(e, payload));

    public static final WithArg<Entity> TRACKING_ENTITY_AND_SELF =
            new WithArg<>(e -> payload -> com.sporeadds.sporeaddsmod.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(e, payload));

    public static final WithArg<TargetPoint> NEAR = new WithArg<>(tp -> payload -> {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        var level = server.getLevel(tp.dimension());
        if (level == null) return;
        com.sporeadds.sporeaddsmod.network.PacketDistributor.sendToPlayersNear(
                level, null, tp.x(), tp.y(), tp.z(), tp.radius(), payload);
    });

    public static final NoArg ALL =
            new NoArg(payload -> com.sporeadds.sporeaddsmod.network.PacketDistributor.sendToAllPlayers(payload));

    public static final NoArg SERVER =
            new NoArg(payload -> com.sporeadds.sporeaddsmod.network.PacketDistributor.sendToServer(payload));
}
