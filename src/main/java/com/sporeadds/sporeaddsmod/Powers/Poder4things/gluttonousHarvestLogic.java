package com.sporeadds.sporeaddsmod.Powers.Poder4things;

import com.sporeadds.sporeaddsmod.Powers.bile.gluttonousAbilityHandler;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class gluttonousHarvestLogic {

    private static final ResourceLocation EXQUISITE_CUISINE_ID =
            new ResourceLocation("sporeadd", "exquisite_cuisine");
    private static final ResourceLocation MANGLED_ID =
            new ResourceLocation("sporeadd", "mangled");
    private static final int HARVEST_RESISTANCE_DURATION = 20 * 20;
    private static final int HARVEST_RESISTANCE_AMPLIFIER = 0;

    private static final String ANTIFARM_TAG = "antifarm";

    /** Per-player cooldown between successful harvests (0.2s), so rapid-fire eating of stacked corpses can't chain-trigger biomass rewards in the same instant. */
    private static final Map<UUID, Long> LAST_HARVEST_TICK = new ConcurrentHashMap<>();
    private static final long HARVEST_COOLDOWN_TICKS = 4L;

    private gluttonousHarvestLogic() {
    }

    public static boolean tryHarvestEntity(Player player, Entity target, boolean requirePower4Enabled) {
        if (player.level().isClientSide()) return false;
        if (target == null) return false;

        long now = player.level().getGameTime();
        Long lastHarvest = LAST_HARVEST_TICK.get(player.getUUID());
        if (lastHarvest != null && now - lastHarvest < HARVEST_COOLDOWN_TICKS) {
            return false;
        }

        ResourceLocation targetId = EntityType.getKey(target.getType());
        if (targetId == null) return false;
        if (gluttonousEntityLists.MEAT_ABOMINATION_ID.equals(targetId)) return false;
        if (!gluttonousEntityLists.isHarvestableEntity(targetId)) return false;

        if (requirePower4Enabled && !gluttonousPowerHelper.isPower4Enabled(player)) {
            return false;
        }

        if (target instanceof LivingEntity living && gluttonousPowerHelper.isBlockedByPower2(player, living)) {
            return false;
        }

        boolean isBasic = gluttonousEntityLists.isBasicEntity(targetId);
        boolean isAntifarm = target.getTags().contains(ANTIFARM_TAG);

        int naturalBiomassReward = isBasic
                ? player.getRandom().nextIntBetweenInclusive(2, 3)
                : player.getRandom().nextIntBetweenInclusive(3, 4);

        final int biomassReward = isAntifarm ? 1 : naturalBiomassReward;
        final int genericFoodReward = naturalBiomassReward;

        int boneReward = isBasic
                ? player.getRandom().nextIntBetweenInclusive(1, 2)
                : player.getRandom().nextIntBetweenInclusive(2, 4);

        int goreReward = isBasic
                ? player.getRandom().nextIntBetweenInclusive(1, 2)
                : player.getRandom().nextIntBetweenInclusive(3, 5);

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> spore.addSpore(biomassReward));

        if (player instanceof ServerPlayer serverPlayer) {
            for (int i = 0; i < genericFoodReward; i++) {
                gluttonousAbilityHandler.addGenericFoodConsumed(serverPlayer);
            }
        }

        if (gluttonousPowerHelper.isSubclassgluttonous(player) && player instanceof ServerPlayer serverPlayer) {
            for (int i = 0; i < boneReward; i++) {
                gluttonousAbilityHandler.addBoneConsumed(serverPlayer);
            }

            for (int i = 0; i < goreReward; i++) {
                gluttonousAbilityHandler.addGoreConsumed(serverPlayer);
            }

            player.addEffect(new MobEffectInstance(
                    MobEffects.HEAL,
                    1,
                    0,
                    false,
                    true,
                    true
            ));

            if (target instanceof LivingEntity living) {
                applygluttonousDietRewards(player, living);
            }
        }

        int saturationTicks = isBasic ? 2 : 4;
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, saturationTicks, 0));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F),
                    target.getX(),
                    target.getY() + target.getBbHeight() / 2.0D,
                    target.getZ(),
                    20,
                    0.3D, 0.3D, 0.3D,
                    0.0D
            );
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        LAST_HARVEST_TICK.put(player.getUUID(), now);
        target.discard();
        return true;
    }

    public static void applygluttonousDietRewards(Player player, LivingEntity target) {
        if (!gluttonousPowerHelper.isSubclassgluttonous(player)) return;

        Collection<MobEffectInstance> activeEffects = target.getActiveEffects();
        for (MobEffectInstance effect : activeEffects) {
            ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect());
            if (effectId != null && (
                    effectId.equals(gluttonousEntityLists.SEASONED_ID)
                            || effectId.equals(EXQUISITE_CUISINE_ID)
                            || effectId.equals(MANGLED_ID)
            )) {
                continue;
            }

            if (effect.getEffect() == MobEffects.DAMAGE_RESISTANCE) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        HARVEST_RESISTANCE_DURATION,
                        HARVEST_RESISTANCE_AMPLIFIER,
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                ));
                continue;
            }

            int amplifier = effect.getAmplifier();
            if (effect.getEffect() == MobEffects.DAMAGE_BOOST) {
                amplifier = Math.min(amplifier, 1);
            }

            player.addEffect(new MobEffectInstance(
                    effect.getEffect(),
                    effect.getDuration(),
                    amplifier,
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()
            ));
        }

        ResourceLocation targetId = EntityType.getKey(target.getType());
        if (targetId == null) return;
        String idString = targetId.toString();

        if (idString.equals("spore:naiad") || idString.equals("spore:bloater") || idString.equals("spore:inf_drowned")) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60 * 20, 0));
        }

        if (idString.equals("spore:inf_witch")) {
            if (player.getRandom().nextFloat() <= 0.35F) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, player.getRandom().nextIntBetweenInclusive(15, 45) * 20, 0));
            if (player.getRandom().nextFloat() <= 0.35F) player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, player.getRandom().nextIntBetweenInclusive(15, 45) * 20, 0));
            if (player.getRandom().nextFloat() <= 0.35F) player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, player.getRandom().nextIntBetweenInclusive(15, 45) * 20, 0));
            if (player.getRandom().nextFloat() <= 0.35F) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, player.getRandom().nextIntBetweenInclusive(15, 45) * 20, 0));
        }

        if (idString.equals("spore:volatile") || idString.equals("spore:mephitic")) {
            if (player.getRandom().nextFloat() <= 0.45F) {
                int amp = player.getRandom().nextIntBetweenInclusive(0, 1);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, player.getRandom().nextIntBetweenInclusive(5, 55) * 20, amp));
            }
            if (player.getRandom().nextFloat() <= 0.45F) {
                int amp = player.getRandom().nextIntBetweenInclusive(0, 1);
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, player.getRandom().nextIntBetweenInclusive(5, 55) * 20, amp));
            }
            if (player.getRandom().nextFloat() <= 0.45F) {
                int amp = player.getRandom().nextIntBetweenInclusive(0, 1);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, player.getRandom().nextIntBetweenInclusive(5, 55) * 20, amp));
            }
            if (player.getRandom().nextFloat() <= 0.45F) {
                int amp = player.getRandom().nextIntBetweenInclusive(0, 1);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, player.getRandom().nextIntBetweenInclusive(5, 55) * 20, amp));
            }
        }

        if (idString.equals("spore:protector")) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    HARVEST_RESISTANCE_DURATION,
                    HARVEST_RESISTANCE_AMPLIFIER
            ));
        }

        if (idString.equals("spore:inebriater")) {
            int regenAmp = player.getRandom().nextIntBetweenInclusive(0, 2);
            int speedAmp = player.getRandom().nextIntBetweenInclusive(0, 2);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, player.getRandom().nextIntBetweenInclusive(5, 20) * 20, regenAmp));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, player.getRandom().nextIntBetweenInclusive(10, 20) * 20, speedAmp));
        }

        if (idString.equals("spore:stalker") || idString.equals("spore:inf_wanderer")) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, player.getRandom().nextIntBetweenInclusive(10, 45) * 20, 0));
        }

        if (idString.equals("spore:knight")) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, player.getRandom().nextIntBetweenInclusive(10, 25) * 20, 0));
        }
    }
}