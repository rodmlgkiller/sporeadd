package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Organoids.Usurper;
import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import com.Harbinger.Spore.Sentities.Utility.ArenaEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(value = ArenaEntity.class, remap = false)
public abstract class ArenaEntityMixin {

    private static final String NO_DESPAWN_TAG = "SporeAdds_NoHardFloorDespawn";
    private static final String STAGED_RAID_TAG = "staged_raid";

    @Shadow
    public abstract void startWave(boolean value);

    @Inject(method = "compareEntity", at = @At("HEAD"), cancellable = true)
    private void sporeadds$bypassEvaluationForStagedRaid(List<Entity> entities, CallbackInfo ci) {
        ArenaEntity arena = (ArenaEntity) (Object) this;

        if (!arena.getPersistentData().getBoolean(STAGED_RAID_TAG)) {
            return;
        }

        boolean hasLivingTarget = entities.stream().anyMatch(entity -> entity instanceof LivingEntity);

        if (hasLivingTarget) {
            this.startWave(true);
        }

        ci.cancel();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sporeadds$tagArena(EntityType<? extends PathfinderMob> type, Level level, CallbackInfo ci) {
        ((Entity) (Object) this).addTag(NO_DESPAWN_TAG);
    }

    @Inject(method = "summonVerva", at = @At("TAIL"))
    private void sporeadds$tagSpawnedVerva(boolean special, List<? extends String> mob, CallbackInfo ci) {
        ArenaEntity arena = (ArenaEntity) (Object) this;

        arena.level()
                .getEntitiesOfClass(Verwa.class, arena.getBoundingBox().inflate(32.0D))
                .stream()
                .filter(verwa -> !verwa.getTags().contains(NO_DESPAWN_TAG))
                .filter(verwa -> verwa.tickCount <= 2)
                .min(Comparator.comparingDouble(arena::distanceToSqr))
                .ifPresent(verwa -> verwa.addTag(NO_DESPAWN_TAG));
    }

    @Inject(method = "summonUsurper", at = @At("TAIL"))
    private void sporeadds$tagSpawnedUsurper(CallbackInfo ci) {
        ArenaEntity arena = (ArenaEntity) (Object) this;

        arena.level()
                .getEntitiesOfClass(Usurper.class, arena.getBoundingBox().inflate(32.0D))
                .stream()
                .filter(usurper -> !usurper.getTags().contains(NO_DESPAWN_TAG))
                .filter(usurper -> usurper.tickCount <= 2)
                .min(Comparator.comparingDouble(arena::distanceToSqr))
                .ifPresent(usurper -> usurper.addTag(NO_DESPAWN_TAG));
    }

    @Inject(method = "summonBomb", at = @At("TAIL"))
    private void sporeadds$tagSpawnedBomb(CallbackInfo ci) {
        ArenaEntity arena = (ArenaEntity) (Object) this;

        arena.level()
                .getEntitiesOfClass(FleshBomb.class, arena.getBoundingBox().inflate(64.0D, 120.0D, 64.0D))
                .stream()
                .filter(bomb -> !bomb.getTags().contains(NO_DESPAWN_TAG))
                .filter(bomb -> bomb.tickCount <= 2)
                .min(Comparator.comparingDouble(arena::distanceToSqr))
                .ifPresent(bomb -> bomb.addTag(NO_DESPAWN_TAG));
    }

    @Inject(method = "dropLoot", at = @At("TAIL"))
    private void sporeadds$tagDroppedLoot(CallbackInfo ci) {
        ArenaEntity arena = (ArenaEntity) (Object) this;

        arena.level()
                .getEntitiesOfClass(ItemEntity.class, arena.getBoundingBox().inflate(8.0D))
                .stream()
                .filter(item -> !item.getTags().contains(NO_DESPAWN_TAG))
                .filter(item -> item.tickCount <= 2)
                .forEach(item -> item.addTag(NO_DESPAWN_TAG));
    }
}