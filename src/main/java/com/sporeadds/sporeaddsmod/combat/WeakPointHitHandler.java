    package com.sporeadds.sporeaddsmod.combat;

    import net.minecraft.server.level.ServerLevel;
    import net.minecraft.server.level.ServerPlayer;
    import net.minecraft.sounds.SoundEvent;
    import net.minecraft.sounds.SoundSource;
    import net.minecraft.world.entity.LivingEntity;
    import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
    import net.neoforged.bus.api.SubscribeEvent;
    import net.neoforged.fml.common.Mod;

    @EventBusSubscriber(modid = "sporeadd")
    public final class WeakPointHitHandler {

        private WeakPointHitHandler() {
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
                return;
            }

            LivingEntity target = event.getEntity();

            if (!WeakPointManager.hasWeakPoint(target.getId())) {
                return;
            }

            if (!target.hasEffect(com.sporeadds.sporeaddsmod.effects.effects.EXPOSED_WEAKNESS.get())) {
                WeakPointManager.remove(target.getId());
                syncRemoval(target);
                return;
            }

            boolean hit = WeakPointManager.tryHit(target, attacker);

            if (hit) {
                float bonus = WeakPointManager.getDamageBonus(target.getId());
                event.setAmount(event.getAmount() + bonus);

                WeakPointKillTracker.recordHit(target.getId(), attacker.getUUID(), target.level().getGameTime());

                net.minecraft.sounds.SoundEvent hitSound = WeakPointManager.getHitSound(target.getId());
                if (target.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.playSound(
                            null,
                            target.getX(), target.getY(), target.getZ(),
                            hitSound,
                            net.minecraft.sounds.SoundSource.PLAYERS,
                            WeakPointManager.getHitSoundVolume(),
                            WeakPointManager.getHitSoundPitch(target.getId())
                    );
                }

                WeakPointManager.relocate(target);
                syncMarker(target);
            }
        }

        private static void syncMarker(LivingEntity target) {
            com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.send(
                    net.neoforged.neoforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> target),
                    new com.sporeadds.sporeaddsmod.network.SyncWeakPointPacket(
                            target.getId(), true, WeakPointManager.getOffset(target.getId())
                    )
            );
        }

        private static void syncRemoval(LivingEntity target) {
            com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.send(
                    net.neoforged.neoforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> target),
                    new com.sporeadds.sporeaddsmod.network.SyncWeakPointPacket(target.getId(), false, null)
            );
        }
    }