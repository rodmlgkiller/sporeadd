package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FleshBomb.onHitBlock (m_8060_) spawns a creature when getCarrier() is true, but onHitEntity (onHitEntity)
 * never checks getCarrier() at all - so a carrier bomb that lands on a living target (as ours always do,
 * since SpawnVervaPacket homes them onto one via setTarget/aimForTarget) explodes without ever spawning
 * its creature. Mirrors onHitBlock's carrier check here too.
 */
@Mixin(value = FleshBomb.class, remap = false)
public abstract class FleshBombCarrierMixin {

    @Inject(
            method = "onHitEntity",
            at = @At("TAIL")
    )
    private void sporeadds$summonCarrierOnEntityHit(EntityHitResult result, CallbackInfo ci) {
        FleshBomb self = (FleshBomb) (Object) this;
        if (!self.getCarrier()) return;

        Level level = self.level();
        if (level instanceof ServerLevel serverLevel) {
            ((FleshBombInvoker) self).sporeadds$summonInfected(serverLevel);
        }
    }
}
