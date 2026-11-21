package com.sporeadds.mixin;

import com.Harbinger.Spore.Sitems.Reaver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Reaver.class)
public abstract class ReaverAntiTagMixin {

    @Inject(method = "hurtEnemy(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void sporeadds$preventReaverHarvestOnTaggedEntities(ItemStack stack, LivingEntity livingEntity, LivingEntity victim, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity != null && livingEntity.getTags().contains("anti_reaver")) {
            cir.setReturnValue(false);
        }
    }
}