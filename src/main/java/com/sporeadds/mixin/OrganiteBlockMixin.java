package com.sporeadds.mixin;

import com.Harbinger.Spore.core.SConfig;
import com.Harbinger.Spore.core.Seffects;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sblocks.OrganiteBlock;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(OrganiteBlock.class)
public class OrganiteBlockMixin {

    @Inject(method = "m_213897_", at = @At("TAIL"), remap = false)
    private void sporeadds$applyMarkerToBlacklisted(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource, CallbackInfo ci) {

        if (!SporeAddsConfig.ORGANITE_MARKER_FOR_BLACKLIST.get()) {
            return;
        }

        AABB searchbox = AABB.ofSize(new Vec3(pos.getX(), pos.getY(), pos.getZ()), 35.0, 35.0, 35.0);
        List<? extends String> blacklist = SConfig.SERVER.blacklist.get();

        if (blacklist == null || blacklist.isEmpty()) {
            return;
        }

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, searchbox)) {

            String entityId = entity.getEncodeId();

            if (entityId != null && blacklist.contains(entityId)) {

                boolean isProtected = entity instanceof Infected ||
                        entity instanceof UtilityEntity ||
                        Utilities.helmetList().contains(entity.getItemBySlot(EquipmentSlot.HEAD).getItem());

                if (!isProtected) {
                    entity.addEffect(new MobEffectInstance(Seffects.MARKER, 400, 0));
                }
            }
        }
    }
}