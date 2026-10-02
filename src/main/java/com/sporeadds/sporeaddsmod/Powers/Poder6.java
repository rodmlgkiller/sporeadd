package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.core.SConfig;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
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
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import org.joml.Vector3f;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Poder6 {

    static final long COOLDOWN_MS = 200;
    static final java.util.Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    static final String TAG_FORCED_EVO = "forced_evolution";
    static final int INFINITE_EFFECT_DURATION = Integer.MAX_VALUE;

    private static final Set<String> EVOLVABLE_MOBS = Set.of(
            "spore:inf_human",
            "spore:inf_husk",
            "spore:inf_drowned",
            "spore:inf_villager",
            "spore:inf_diseased_villager",
            "spore:inf_wanderer",
            "spore:inf_witch",
            "spore:inf_pillager",
            "spore:inf_player",
            "spore:inf_hazmat",
            "spore:knight",
            "spore:braiomil",
            "spore:leaper",
            "spore:stalker",
            "spore:brute",
            "spore:inf_vindicator",
            "spore:inf_evoker"
    );

    private static final Set<String> HYPER_EVOLVABLE_MOBS = Set.of(
            "spore:knight",
            "spore:braiomil",
            "spore:leaper",
            "spore:stalker",
            "spore:brute",
            "spore:inf_vindicator",
            "spore:inf_evoker"
    );

    public static boolean tryForceEvolve(Player player, Entity target) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;

        AtomicBoolean enabled = new AtomicBoolean(false);
        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            if (data.getSwitch().length() > 6 && data.getSwitch().charAt(6) == '1') {
                enabled.set(true);
            }
        });

        if (!enabled.get()) return false;

        return activateResolved(serverPlayer, target);
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
        if (now - lastUse < COOLDOWN_MS) {
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
        if (persistentData.getBoolean(TAG_FORCED_EVO)) {
            cooldowns.put(playerUUID, now);
            player.sendSystemMessage(
                    Component.translatable("message.sporeadd.power6.already_evolved")
                            .withStyle(ChatFormatting.DARK_RED)
            );
            return true;
        }

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
            PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelCap -> {
                int rawLevel = levelCap.getLevel();
                int sporeLevel = Math.max(rawLevel, 0);
                int effectDuration = 20 + sporeLevel * 3;
                int effectPower = Math.min(5, (int) Math.floor(sporeLevel / 2.0));
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
                        biomassCost = applyKillDiscount(5, kills);
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
                    int biomassCost = applyKillDiscount(5, kills);

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
        return Math.max(1, baseCost - Math.max(0, kills));
    }

    static boolean isCaustic(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            return "caustic".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    static boolean isAbyssal(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            return "abyssal".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    static MobEffect getDissolutionEffect() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "dissolution")).orElse(null);
    }

    static boolean usesEvolutionKillCost(Entity entity) {
        if (entity instanceof Experiment) {
            return true;
        }

        ResourceLocation entityId = entity.getType().builtInRegistryHolder().key().location();
        return EVOLVABLE_MOBS.contains(entityId.toString()) || HYPER_EVOLVABLE_MOBS.contains(entityId.toString());
    }

    static boolean usesHyperEvolutionKillCost(Entity entity) {
        ResourceLocation entityId = entity.getType().builtInRegistryHolder().key().location();
        return HYPER_EVOLVABLE_MOBS.contains(entityId.toString());
    }

    static int getBiomassCost(int currentPoints, int maxPointsRequired) {
        int pointsNeeded = Math.max(0, maxPointsRequired - currentPoints);
        return pointsNeeded > 0 ? Math.min(5, pointsNeeded) : 5;
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
        double extraHealth = 17.5 * rawLevel;
        AttributeInstance maxHealthAttr = target.getAttribute(Attributes.MAX_HEALTH);

        if (maxHealthAttr != null) {
            double base = maxHealthAttr.getBaseValue();
            double newMax = base + extraHealth;
            maxHealthAttr.setBaseValue(newMax);
            target.setHealth((float) newMax);
        }

        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, effectDuration * 20, 0, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, effectDuration * 20, amplifier, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, effectDuration * 20, 0, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, effectDuration * 20, 0, false, true));

        if (caustic) {
            MobEffect dissolution = getDissolutionEffect();
            if (dissolution != null) {
                target.addEffect(new MobEffectInstance(dissolution, INFINITE_EFFECT_DURATION, 0, false, true));
            }
        }

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

        var soundEvent = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "reaver_reave"));
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