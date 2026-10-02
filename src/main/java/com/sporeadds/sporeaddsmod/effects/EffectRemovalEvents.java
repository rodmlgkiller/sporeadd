package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * 1.21 removed the entity argument from MobEffect#removeAttributeModifiers, so effects that need to react to
 * their own removal (expiry, milk, commands...) implement {@link RemovalAware} and are notified from here.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class EffectRemovalEvents {

    public interface RemovalAware {
        void onRemovedFrom(LivingEntity entity);
    }

    private EffectRemovalEvents() {
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        notifyEffect(event.getEffectInstance(), event.getEntity());
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        notifyEffect(event.getEffectInstance(), event.getEntity());
    }

    private static void notifyEffect(MobEffectInstance instance, LivingEntity entity) {
        if (instance != null && instance.getEffect().value() instanceof RemovalAware aware) {
            aware.onRemovedFrom(entity);
        }
    }
}
