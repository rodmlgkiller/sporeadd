package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
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
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());

        if (id == null) {
            return;
        }

        if (id.equals(new ResourceLocation("sporeadd", "cocoon"))
                || id.equals(new ResourceLocation("sporeadd", "tentacle"))
                || id.equals(new ResourceLocation("sporeadd", "meat_abomination"))) {
            ci.cancel();
            return;
        }

        if (entity.getTags().contains(NO_DESPAWN_TAG)
                || entity.getPersistentData().getBoolean(NO_DESPAWN_TAG)) {
            ci.cancel();
        }
    }
}