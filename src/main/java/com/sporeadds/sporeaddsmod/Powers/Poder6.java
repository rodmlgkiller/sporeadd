package com.sporeadds.sporeaddsmod.Powers;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Poder6 {

    /** Debounce between two uses in ms (kommandant_abilities.power_6_forced_mutation.use_cooldown_ms, default 200). */
    static long cooldownMs() {
        return KommandantPowersConfig.MUTATION_COOLDOWN_MS.get();
    }

    static final java.util.Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    static final String TAG_FORCED_EVO = "forced_evolution";
    /** How many times the entity was force-mutated (the old flag {@link #TAG_FORCED_EVO} counts as one). */
    static final String TAG_FORCED_EVO_COUNT = "forced_evolution_count";
    static final int INFINITE_EFFECT_DURATION = Integer.MAX_VALUE;

    // The lists of evolvable / hyper-evolvable mobs are power_6_forced_mutation.evolvable_mobs / hyper_evolvable_mobs.

    public static boolean tryForceEvolve(Player player, Entity target) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;

        AtomicBoolean enabled = new AtomicBoolean(false);
        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            if (data.getSwitch().length() > 6 && data.getSwitch().charAt(6) == '1') {
                enabled.set(true);
            }
        });

        if (!enabled.get()) return false;
        if (!PowerUnlocks.isUnlocked(player, 6)) return false;

        return activateResolved(serverPlayer, target);
    }

    static int forcedMutationCount(CompoundTag tag) {
        int count = tag.getInt(TAG_FORCED_EVO_COUNT);
        if (count <= 0 && tag.getBoolean(TAG_FORCED_EVO)) {
            count = 1;
        }
        return count;
    }

    public static boolean activateResolved(ServerPlayer player, Entity target) {
        if (Poder6Variants.tryActivateVariant(player, target)) {
            return true;
        }

        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return false;

        long now = System.currentTimeMillis();
        UUID playerUUID = player.getUUID();
        long lastUse = cooldowns.getOrDefault(playerUUID, 0L);
        if (now - lastUse < cooldownMs()) {
            return false;
        }

        if (target == null) return false;
        if (target instanceof com.Harbinger.Spore.Sentities.BaseEntities.Calamity) return false;

        if (target instanceof Scamper) {
            cooldowns.put(playerUUID, now);
            player.sendSystemMessage(
                    Component.translatable("message.sporeadd.power6.cannot_evolve_scamper")
                            .withStyle(ChatFormatting.DARK_RED)
            );
            return true;
        }

        if (target instanceof Organoid) {
            cooldowns.put(playerUUID, now);
            return true;
        }

        if (!(target instanceof Infected) && !(target instanceof UtilityEntity)) {
            return false;
        }

        AtomicBoolean consumed = new AtomicBoolean(false);

        CompoundTag persistentData = target.getPersistentData();
        if (forcedMutationCount(persistentData) >= KommandantPowersConfig.MUTATION_MAX_PER_ENTITY.get()) {
            cooldowns.put(playerUUID, now);
            player.sendSystemMessage(
                    Component.translatable("message.sporeadd.power6.already_evolved")
                            .withStyle(ChatFormatting.DARK_RED)
            );
            return true;
        }

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
                int rawLevel = levelCap.getLevel();
                int sporeLevel = Math.max(rawLevel, 0);
                int effectDuration = KommandantPowersConfig.mutationBuffSeconds(sporeLevel);
                int effectPower = KommandantPowersConfig.mutationStrengthPower(sporeLevel);
                boolean caustic = isCaustic(player);

                if (target instanceof Infected infected) {
                    int biomassCost;
                    boolean usesEvolutionCost = usesEvolutionKillCost(infected);

                    if (usesEvolutionCost) {
                        int maxPointsRequired = getMaxPointsRequired(infected);
                        int currentPoints = getCurrentPoints(infected);
                        int baseCost = getBiomassCost(currentPoints, maxPointsRequired);
                        int kills = getEntityKillsForDiscount(infected);
                        biomassCost = applyKillDiscount(baseCost, kills);
                    } else {
                        int kills = getEntityKillsForDiscount(infected);
                        biomassCost = applyKillDiscount(KommandantPowersConfig.MUTATION_COST_MAX.get(), kills);
                    }

                    if (spore.getSpore() < biomassCost) {
                        player.sendSystemMessage(
                                Component.translatable("message.sporeadd.power6.not_enough_biomass_infected")
                                        .withStyle(ChatFormatting.DARK_RED)
                        );
                        cooldowns.put(playerUUID, now);
                        consumed.set(true);
                        return;
                    }

                    spore.addSpore(-biomassCost);

                    if (usesEvolutionCost) {
                        int maxPointsRequired = getMaxPointsRequired(infected);

                        if (infected instanceof EvolvedInfected evolvedInfected) {
                            evolvedInfected.setEvoPoints(maxPointsRequired);
                        } else if (infected instanceof Experiment experiment) {
                            experiment.setEvoPoints(maxPointsRequired);
                            if (experiment.isDormant()) {
                                experiment.setDormant(false);
                            }
                        } else {
                            infected.setEvoPoints(maxPointsRequired);
                        }

                        infected.setEvolution(SConfig.SERVER.evolution_age_human.get());
                    }

                    applyBuffsAndSave(infected, serverLevel, player, effectPower, rawLevel, effectDuration, biomassCost, caustic);
                    cooldowns.put(playerUUID, now);
                    consumed.set(true);
                    return;
                }

                if (target instanceof UtilityEntity utilityTarget && !(target instanceof Organoid)) {
                    int kills = getEntityKillsForDiscount(utilityTarget);
                    int biomassCost = applyKillDiscount(KommandantPowersConfig.MUTATION_COST_MAX.get(), kills);

                    if (spore.getSpore() < biomassCost) {
                        player.sendSystemMessage(
                                Component.translatable("message.sporeadd.power6.not_enough_biomass_enhanced")
                                        .withStyle(ChatFormatting.DARK_RED)
                        );
                        cooldowns.put(playerUUID, now);
                        consumed.set(true);
                        return;
                    }

                    spore.addSpore(-biomassCost);
                    applyBuffsAndSave(utilityTarget, serverLevel, player, effectPower, rawLevel, effectDuration, biomassCost, caustic);
                    cooldowns.put(playerUUID, now);
                    consumed.set(true);
                }
            });
        });

        return consumed.get();
    }

    static int getEntityKillsForDiscount(Entity entity) {
        if (entity instanceof Infected infected) {
            return Math.max(0, infected.getKills());
        }

        CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        if (tag.contains("kills")) {
            return Math.max(0, tag.getInt("kills"));
        }

        CompoundTag persistentData = entity.getPersistentData();
        if (persistentData.contains("kills")) {
            return Math.max(0, persistentData.getInt("kills"));
        }

        return 0;
    }

    static int applyKillDiscount(int baseCost, int kills) {
        int discount = Math.max(0, kills) * KommandantPowersConfig.MUTATION_DISCOUNT_PER_KILL.get();
        return Math.max(KommandantPowersConfig.MUTATION_COST_MIN.get(), baseCost - discount);
    }

    static boolean isCaustic(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER);
        if (cap.isPresent()) {
            return "caustic".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    static boolean isAbyssal(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER);
        if (cap.isPresent()) {
            return "abyssal".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    static MobEffect getDissolutionEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sporeadd", "dissolution"));
    }

    static boolean usesEvolutionKillCost(Entity entity) {
        if (entity instanceof Experiment) {
            return true;
        }

        ResourceLocation entityId = entity.getType().builtInRegistryHolder().key().location();
        return KommandantPowersConfig.isEvolvableMob(entityId.toString())
                || KommandantPowersConfig.isHyperEvolvableMob(entityId.toString());
    }

    static boolean usesHyperEvolutionKillCost(Entity entity) {
        ResourceLocation entityId = entity.getType().builtInRegistryHolder().key().location();
        return KommandantPowersConfig.isHyperEvolvableMob(entityId.toString());
    }

    static int getBiomassCost(int currentPoints, int maxPointsRequired) {
        int maxCost = KommandantPowersConfig.MUTATION_COST_MAX.get();
        if (!KommandantPowersConfig.MUTATION_COST_SCALES_WITH_POINTS.get()) {
            return maxCost;
        }
        int pointsNeeded = Math.max(0, maxPointsRequired - currentPoints);
        return pointsNeeded > 0 ? Math.min(maxCost, pointsNeeded) : maxCost;
    }

    static int getMaxPointsRequired(Entity entity) {
        if (entity instanceof Experiment experiment) {
            return experiment.getBreaking();
        }

        if (usesHyperEvolutionKillCost(entity)) {
            return SConfig.SERVER.min_kills_hyper.get();
        }

        if (usesEvolutionKillCost(entity)) {
            return SConfig.SERVER.min_kills.get();
        }

        return 5;
    }

    static int getCurrentPoints(Entity entity) {
        if (entity instanceof EvolvedInfected evolvedInfected) {
            return evolvedInfected.getEvoPoints();
        }
        if (entity instanceof Experiment experiment) {
            return experiment.getEvoPoints();
        }
        if (entity instanceof Infected infected) {
            return infected.getEvoPoints();
        }

        return 0;
    }

    static void applyBuffsAndSave(
            LivingEntity target,
            ServerLevel serverLevel,
            ServerPlayer player,
            int effectPower,
            int rawLevel,
            int effectDuration,
            int biomassCost,
            boolean caustic
    ) {
        int amplifier = Math.max(effectPower - 1, 0);
        double extraHealth = KommandantPowersConfig.mutationExtraHp(rawLevel);
        AttributeInstance maxHealthAttr = target.getAttribute(Attributes.MAX_HEALTH);

        if (maxHealthAttr != null) {
            double base = maxHealthAttr.getBaseValue();
            double newMax = base + extraHealth;
            maxHealthAttr.setBaseValue(newMax);
            target.setHealth((float) newMax);
        }

        if (KommandantPowersConfig.MUTATION_RESISTANCE.get()) {
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, effectDuration * 20, 0, false, true));
        }
        if (KommandantPowersConfig.MUTATION_STRENGTH.get()) {
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, effectDuration * 20, amplifier, false, true));
        }
        if (KommandantPowersConfig.MUTATION_SPEED.get()) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, effectDuration * 20, 0, false, true));
        }
        if (KommandantPowersConfig.MUTATION_GLOWING.get()) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, effectDuration * 20, 0, false, true));
        }

        if (caustic) {
            MobEffect dissolution = getDissolutionEffect();
            if (dissolution != null) {
                target.addEffect(new MobEffectInstance(dissolution, INFINITE_EFFECT_DURATION, 0, false, true));
            }
        }

        // The count must be read BEFORE the flag is set: the flag alone counts as one mutation (old saves), so reading it
        // afterwards would make every first mutation count twice and max_mutations_per_entity 2+ would never be reached.
        target.getPersistentData().putInt(TAG_FORCED_EVO_COUNT, forcedMutationCount(target.getPersistentData()) + 1);
        target.getPersistentData().putBoolean(TAG_FORCED_EVO, true);

        BlockPos pos = target.blockPosition();
        DustParticleOptions darkRedDust = new DustParticleOptions(new Vector3f(0.2F, 0.0F, 0.0F), 0.8F);

        serverLevel.sendParticles(
                darkRedDust,
                pos.getX() + 0.5,
                pos.getY() + 1.0,
                pos.getZ() + 0.5,
                20,
                0.3, 0.3, 0.3,
                0.01
        );

        var soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "reaver_reave"));
        if (soundEvent == null) {
            soundEvent = SoundEvents.PLAYER_LEVELUP;
        }

        serverLevel.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                soundEvent,
                SoundSource.PLAYERS,
                2.0f, 1.0f
        );

        player.sendSystemMessage(
                Component.translatable("message.sporeadd.power6.forcefully_evolved", biomassCost)
                        .withStyle(ChatFormatting.DARK_RED)
        );
    }
}