package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(value = Organoid.class, remap = false)
public abstract class OrganoidRegulateSpawnsMixin {

    private static final String ANTIFARM_TAG = "antifarm";
    private static final ResourceLocation COCOON_ID = new ResourceLocation("sporeadd", "cocoon");
    private static final ResourceLocation TENTACLE_ID = new ResourceLocation("sporeadd", "tentacle");
    private static final ResourceLocation MEAT_ABOMINATION_ID = new ResourceLocation("sporeadd", "meat_abomination");

    /**
     * @author sporeadds
     * @reason Excluir organoids antifarm y jammers del conteo de regulateSpawns.
     */
    @Overwrite(remap = false)
    public void regulateSpawns() {
        Organoid self = (Organoid) (Object) this;
        AABB aabb = self.getBoundingBox().inflate(6.0D);

        List<Entity> entityList = self.level().getEntities(self, aabb, entity -> {
            if (!(entity instanceof Organoid)) {
                return false;
            }

            if (entity instanceof Proto || entity instanceof Mound) {
                return false;
            }

            if (entity.getTags().contains(ANTIFARM_TAG)) {
                return false;
            }

            CompoundTag data = entity.getPersistentData();
            if (data.getBoolean("SporeAdds_IsJammer")) {
                return false;
            }

            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            if (id == null) {
                return true;
            }

            return !id.equals(COCOON_ID)
                    && !id.equals(TENTACLE_ID)
                    && !id.equals(MEAT_ABOMINATION_ID);
        });

        if (entityList.size() > 4) {
            self.tickBurrowing();
        }
    }
}