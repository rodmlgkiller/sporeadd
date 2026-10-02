package com.sporeadds.sporeaddsmod.hive;

import com.Harbinger.Spore.Sentities.Organoids.Proto;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Utilidad para localizar el {@code spore:proto} más cercano a una posición.
 * El proto es una entidad de otro mod que siempre está renderizada / cargada,
 * así que no hace falta preocuparse de chunks ni de render por nuestra parte.
 */
public final class ProtoProximity {

    private ProtoProximity() {
    }

    public static boolean isProtoWithin(Level level, Vec3 pos, double radius) {
        return nearestProto(level, pos, radius) != null;
    }

    /** True si hay al menos un {@code spore:proto} vivo en esa dimensión (barrido completo). */
    public static boolean anyProtoAliveInDimension(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Proto proto && proto.isAlive()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Proto nearestProto(Level level, Vec3 pos, double radius) {
        AABB box = new AABB(
                pos.x - radius, pos.y - radius, pos.z - radius,
                pos.x + radius, pos.y + radius, pos.z + radius
        );

        List<Proto> protos = level.getEntitiesOfClass(Proto.class, box, p -> p.isAlive());

        Proto closest = null;
        double closestSq = radius * radius;

        for (Proto proto : protos) {
            double distSq = proto.distanceToSqr(pos.x, pos.y, pos.z);
            if (distSq <= closestSq) {
                closestSq = distSq;
                closest = proto;
            }
        }

        return closest;
    }

    public static Vec3 positionOf(Entity entity) {
        return new Vec3(entity.getX(), entity.getY(), entity.getZ());
    }
}
