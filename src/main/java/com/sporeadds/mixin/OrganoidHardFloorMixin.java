package com.sporeadds.mixin;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Organoid.class, remap = false)
public abstract class OrganoidHardFloorMixin {

    private static final String NO_DESPAWN_TAG = "SporeAdds_NoHardFloorDespawn";

    @Inject(method = "despawnIfHardFloor", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$ignoreHardFloorForSpecificEntities(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        if (id == null) {
            return;
        }

        if (id.equals(ResourceLocation.fromNamespaceAndPath("sporeadd", "cocoon"))
                || id.equals(ResourceLocation.fromNamespaceAndPath("sporeadd", "tentacle"))
                || id.equals(ResourceLocation.fromNamespaceAndPath("sporeadd", "meat_abomination"))) {
            ci.cancel();
            return;
        }

        if (entity.getTags().contains(NO_DESPAWN_TAG)
                || entity.getPersistentData().getBoolean(NO_DESPAWN_TAG)) {
            ci.cancel();
        }
    }
}