package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.core.SConfig;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.Set;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class SeasonedDeathEvent {

    private static final ResourceLocation SEASONED_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "seasoned");
    private static final String TAG_PERFECTED_EVO = "perfected_evolution";

    private static final Set<String> EVOLVABLE_MOBS = Set.of(
            "spore:inf_human", "spore:inf_husk", "spore:inf_drowned", "spore:inf_villager",
            "spore:inf_diseased_villager", "spore:inf_wanderer", "spore:inf_witch",
            "spore:inf_pillager", "spore:inf_player", "spore:inf_hazmat", "spore:knight",
            "spore:braiomil", "spore:leaper", "spore:stalker", "spore:brute",
            "spore:inf_vindicator", "spore:inf_evoker"
    );

    private static final Set<String> HYPER_EVOLVABLE_MOBS = Set.of(
            "spore:knight", "spore:braiomil", "spore:leaper", "spore:stalker",
            "spore:brute", "spore:inf_vindicator", "spore:inf_evoker"
    );

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        MobEffect seasoned = BuiltInRegistries.MOB_EFFECT.get(SEASONED_ID);
        if (seasoned == null || !victim.hasEffect(seasoned)) return;

        Entity killer = resolveTrueKiller(event);

        if (killer instanceof Player playerKiller) {
            if (isSporePlayer(playerKiller)) {
                playerKiller.addEffect(new MobEffectInstance(
                        effects.EXQUISITE_CUISINE.get(),
                        30 * 20,
                        0,
                        false,
                        true,
                        true
                ));

                playerKiller.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        10 * 20,
                        1,
                        false,
                        true,
                        true
                ));
            }
            return;
        }

        if (!(killer instanceof LivingEntity livingKiller)) return;

        evolveEntityFromSeasonedKill(livingKiller);
        applySeasonedBuffs(livingKiller);
        checkAndApplyPerfectedEvolution(livingKiller);
    }

    private static boolean isSporePlayer(Player player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String id = data.getIdentifier();
                    return id != null && !id.isBlank() && !"human".equalsIgnoreCase(id);
                })
                .orElse(false);
    }

    private static Entity resolveTrueKiller(LivingDeathEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity != null) {
            return sourceEntity;
        }

        Entity directEntity = event.getSource().getDirectEntity();
        if (directEntity instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner != null) {
                return owner;
            }
        }

        return directEntity;
    }

    private static void evolveEntityFromSeasonedKill(LivingEntity entity) {
        if (entity instanceof Infected infected) {
            infected.setEvolution(SConfig.SERVER.evolution_age_human.get());

            if (entity instanceof Scamper scamper) {
                scamper.setAge(SConfig.SERVER.scamper_age.get());
            } else if (infected instanceof EvolvedInfected evolvedInfected) {
                evolvedInfected.setEvoPoints(SConfig.SERVER.min_kills_hyper.get());
            } else {
                infected.setEvoPoints(SConfig.SERVER.min_kills.get());
            }
        } else if (entity instanceof Mound mound) {
            mound.setAge(mound.getAge() + 1);
        } else if (entity instanceof Calamity calamity) {
            calamity.ActivateAdaptation();
        }
    }

    private static void applySeasonedBuffs(LivingEntity killer) {
        killer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0, false, true));
        killer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2, false, true));
        killer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1, false, true));
        killer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1, false, true));
    }

    private static void checkAndApplyPerfectedEvolution(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();

        if (persistentData.getBoolean(TAG_PERFECTED_EVO)) {
            return;
        }

        if (entity instanceof Infected) {
            if (entity instanceof Scamper) return;

            if (!usesEvolutionKillCost(entity)) {
                AttributeInstance maxHealthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
                if (maxHealthAttr != null) {
                    double currentMax = maxHealthAttr.getBaseValue();
                    double newMax = currentMax * 2.0D;

                    maxHealthAttr.setBaseValue(newMax);
                    entity.setHealth((float) newMax);

                    persistentData.putBoolean(TAG_PERFECTED_EVO, true);
                }
            }
        }
    }

    private static boolean usesEvolutionKillCost(Entity entity) {
        if (entity instanceof Experiment) {
            return true;
        }
        ResourceLocation entityId = entity.getType().builtInRegistryHolder().key().location();
        return EVOLVABLE_MOBS.contains(entityId.toString()) || HYPER_EVOLVABLE_MOBS.contains(entityId.toString());
    }
}