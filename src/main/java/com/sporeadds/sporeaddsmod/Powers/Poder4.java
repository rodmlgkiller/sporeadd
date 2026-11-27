package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.Powers.bile.gluttonousAbilityHandler;
import com.sporeadds.sporeaddsmod.Powers.Poder4things.gluttonousBlockHarvestLogic;
import com.sporeadds.sporeaddsmod.Powers.Poder4things.gluttonousHarvestLogic;
import com.sporeadds.sporeaddsmod.Powers.Poder4things.gluttonousPowerHelper;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.entity.MeatAbomination;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Poder4 {

    public static boolean tryHarvestEntity(Player player, net.minecraft.world.entity.Entity target) {
        return gluttonousHarvestLogic.tryHarvestEntity(player, target, true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!gluttonousPowerHelper.isPower4Enabled(player)) return;

        if (!(event.getTarget() instanceof MeatAbomination meatAbomination)) return;

        meatAbomination.setBiomass(meatAbomination.getBiomass() - 1.0F);

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> spore.addSpore(1));

        int saturationTicks = 8;
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, saturationTicks, 0));

        player.addEffect(new MobEffectInstance(
                effects.EXQUISITE_CUISINE.get(),
                60 * 20,
                0,
                false,
                true,
                true
        ));

        if (player instanceof ServerPlayer serverPlayer) {
            gluttonousAbilityHandler.addGenericFoodConsumed(serverPlayer);

            if (gluttonousPowerHelper.isSubclassgluttonous(player)) {
                int boneReward = player.getRandom().nextIntBetweenInclusive(0, 1);
                int goreReward = player.getRandom().nextIntBetweenInclusive(1, 2);
                int gluttonousReward = player.getRandom().nextIntBetweenInclusive(0, 1);

                for (int i = 0; i < boneReward; i++) {
                    gluttonousAbilityHandler.addBoneConsumed(serverPlayer);
                }

                for (int i = 0; i < goreReward; i++) {
                    gluttonousAbilityHandler.addGoreConsumed(serverPlayer);
                }

                for (int i = 0; i < gluttonousReward; i++) {
                    gluttonousAbilityHandler.addGenericFoodConsumed(serverPlayer);
                }

                gluttonousHarvestLogic.applygluttonousDietRewards(player, meatAbomination);
            }
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            double width = Math.max(0.35D, meatAbomination.getBbWidth());
            double height = Math.max(0.35D, meatAbomination.getBbHeight());

            serverLevel.sendParticles(
                    new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.2F),
                    meatAbomination.getX(),
                    meatAbomination.getY() + (height * 0.5D),
                    meatAbomination.getZ(),
                    18,
                    width * 0.45D,
                    height * 0.35D,
                    width * 0.45D,
                    0.01D
            );
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS,
                1.0F,
                0.9F
        );

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (gluttonousBlockHarvestLogic.tryHarvestBlock(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }
}