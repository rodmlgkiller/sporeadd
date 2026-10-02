package com.sporeadds.mixin;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;

@Mixin(TargetingConditions.class)
public interface TargetingConditionsAccessor {
    @Accessor("selector")
    Predicate<LivingEntity> getSelector();
}