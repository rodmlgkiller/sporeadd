package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class DehydrationCureEvents {

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide()) return;

        var item = event.getItem().getItem();
        if (item == Items.POTION || item == Items.HONEY_BOTTLE || item == Items.MILK_BUCKET) {
            if (event.getEntity().hasEffect(effects.DEHYDRATION)) {
                event.getEntity().removeEffect(effects.DEHYDRATION);
            }
        }
    }

    @SubscribeEvent
    public static void onPotionSplash(ProjectileImpactEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getProjectile() instanceof ThrownPotion thrownPotion) {
            if (event.getRayTraceResult() instanceof EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof net.minecraft.world.entity.LivingEntity living) {
                    if (living.hasEffect(effects.DEHYDRATION)) {
                        living.removeEffect(effects.DEHYDRATION);
                    }
                }
            } else {
                var entities = thrownPotion.level().getEntitiesOfClass(
                        net.minecraft.world.entity.LivingEntity.class,
                        thrownPotion.getBoundingBox().inflate(4.0D)
                );

                for (var living : entities) {
                    if (living.hasEffect(effects.DEHYDRATION)) {
                        living.removeEffect(effects.DEHYDRATION);
                    }
                }
            }
        }
    }
}