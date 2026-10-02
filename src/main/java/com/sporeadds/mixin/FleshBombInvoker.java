package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = FleshBomb.class, remap = false)
public interface FleshBombInvoker {

    @Invoker("SummonInfected")
    void sporeadds$summonInfected(ServerLevel serverLevel);
}
