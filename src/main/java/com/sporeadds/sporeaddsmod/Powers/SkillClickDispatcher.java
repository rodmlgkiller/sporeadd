package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.Powers.Poder4things.gluttonousBlockHarvestLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Runs a skill whose activation input is its own key (mode {@link SkillClick#MODE_KEY}) instead of the Use click: finds what the
 * player is looking at, the way a click would, and calls the same logic the interaction events call.
 */
public final class SkillClickDispatcher {
    private SkillClickDispatcher() {
    }

    public static void handle(ServerPlayer player, SkillClick skill) {
        if (skill == null || !player.isAlive() || player.isSpectator()) {
            return;
        }
        // Only a key the client reported as its own can trigger the skill this way; a skill that follows the Use click
        // is triggered by the interaction events and must not fire twice.
        if (SkillBindings.mode(player, skill) != SkillClick.MODE_KEY) {
            return;
        }

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        double entityReach = player.getEntityReach();
        double blockReach = player.getBlockReach();

        BlockHitResult blockHit = player.level().clip(new ClipContext(eye, eye.add(look.scale(blockReach)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        double blockDistSqr = blockHit.getType() == HitResult.Type.MISS ? Double.MAX_VALUE : eye.distanceToSqr(blockHit.getLocation());

        Vec3 entityEnd = eye.add(look.scale(entityReach));
        AABB box = player.getBoundingBox().expandTowards(look.scale(entityReach)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eye, entityEnd, box,
                e -> !e.isSpectator() && e.isPickable(), entityReach * entityReach);
        double entityDistSqr = entityHit == null ? Double.MAX_VALUE : eye.distanceToSqr(entityHit.getLocation());

        Entity entity = entityHit != null && entityDistSqr < blockDistSqr ? entityHit.getEntity() : null;
        BlockPos block = entity == null && blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getBlockPos() : null;

        switch (skill) {
            case POWER2_EXECUTE -> {
                if (entity != null) Poder2.tryExecute(player, entity);
            }
            case POWER4_CONSUME -> {
                if (entity != null) {
                    if (!Poder4.tryDevourMeatAbomination(player, entity)) {
                        Poder4.tryHarvestEntity(player, entity);
                    }
                } else if (block != null) {
                    gluttonousBlockHarvestLogic.tryHarvestBlock(player, block);
                }
            }
            case POWER5_FEED -> {
                if (entity != null) {
                    Poder5.tryFeedMound(player, entity);
                } else if (block != null) {
                    Poder5.tryFeedBlock(player, block);
                }
            }
            case POWER5_HARVEST -> {
                if (block != null) Poder5.tryHarvestBlock(player, block);
            }
            case POWER6_EVOLVE -> {
                if (entity != null) Poder6.tryForceEvolve(player, entity);
            }
            case POWER7_MOUNT -> {
                if (entity != null) Poder7.tryMount(player, entity);
            }
        }
    }
}
