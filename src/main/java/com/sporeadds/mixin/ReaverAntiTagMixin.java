package com.sporeadds.mixin;

import com.Harbinger.Spore.Sitems.Reaver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Reaver.class, remap = false)
public abstract class ReaverAntiTagMixin {

    @Inject(method = "m_7579_", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$preventReaverHarvestOnTaggedEntities(ItemStack stack, LivingEntity livingEntity, LivingEntity victim, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity != null && livingEntity.getTags().contains("anti_reaver")) {
            cir.setReturnValue(false);
        }
    }
}