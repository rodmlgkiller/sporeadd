package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Inebriator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = Inebriator.class, remap = false)
public class InebriatorMixin {

    @Inject(
            method = "checkForPatients",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void injectCheckForPatients(CallbackInfo ci) {
        Inebriator self = (Inebriator)(Object)this;
        Level level = self.level();
        if (!level.isClientSide()) {
            AABB aabb = self.getBoundingBox().inflate(4, 1, 4);
            List<Entity> entities = level.getEntities(self, aabb, entity -> {
                if (entity instanceof Inebriator) return false;
                if (entity instanceof Infected || entity instanceof UtilityEntity) return true;
                if (entity instanceof Player player &&
                        player.getTeam() != null &&
                        "spore".equalsIgnoreCase(player.getTeam().getName())) return true;
                return false;
            });
            if (entities.isEmpty()) {
                self.setPatient(null);
                return;
            }
            Entity entity = entities.get(level.getRandom().nextInt(entities.size())); // CAMBIO AQUÍ
            if (entity instanceof LivingEntity livingEntity && self.hasLineOfSight(livingEntity) && !livingEntity.isInvulnerable()) {
                self.setPatient(livingEntity);
            }
            ci.cancel();
        }
    }
}
