package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.core.Holder;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.core.SdamageTypes;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class DissolutionEffect extends MobEffect {

    private static final ResourceLocation DISSOLUTION_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "dissolution");
    private static final ResourceLocation CORROSION_ID = ResourceLocation.fromNamespaceAndPath("spore", "corrosion");
    private static final ResourceLocation CHEMIST_FUSE_ID = ResourceLocation.fromNamespaceAndPath("spore", "chemist_fuse");
    private static final ResourceLocation GOAT_HORN_BREAK_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "entity.goat.horn_break");

    private static final String SPORE_TEAM_NAME = "spore";

    private static final DustParticleOptions BRIGHT_GREEN_DUST =
            new DustParticleOptions(new Vector3f(0.2F, 1.0F, 0.2F), 0.9F);

    /** Per-victim cooldown gate for the "extreme" armor-durability proc below, so it can't re-trigger more than once every 5s. */
    private static final Map<UUID, Long> LAST_ARMOR_DAMAGE_TICK = new ConcurrentHashMap<>();
    private static final long ARMOR_DAMAGE_COOLDOWN_TICKS = 5 * 20;

    public DissolutionEffect() {
        super(MobEffectCategory.HARMFUL, 0x00FF00);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        int damagePerSecond = 1 + amplifier;

        if (entity instanceof Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);

                if (!stack.isEmpty() && stack.isDamageableItem()) {
                    stack.hurtAndBreak(damagePerSecond, player, p -> {
                        EquipmentSlot slot = getEquipmentSlot(player, stack);
                        if (slot != null) {
                            p.broadcastBreakEvent(slot);
                        } else {
                            p.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                        }
                    });
                }
            }
        }

        if (!entity.level().isClientSide) {
            boolean isSporeTeam = isOnSporeTeam(entity);
            boolean sporeTeamAlwaysDamaged = SporeAddsConfig.DISSOLUTION_DAMAGE_SPORE_TEAM_ALWAYS.get();

            boolean shouldAcidDamage;
            if (isSporeTeam) {
                shouldAcidDamage = sporeTeamAlwaysDamaged && !isExemptFromAcidDamage(entity);
            } else {
                shouldAcidDamage = entity.getArmorValue() <= 0.0D;
            }

            if (shouldAcidDamage) {
                float acidDamage = (float) (entity.getMaxHealth() * (0.025F * (amplifier + 1)));
                boolean damaged = entity.hurt(SdamageTypes.acid(entity), acidDamage);

                if (damaged) {
                    entity.invulnerableTime = 0;
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        Level level = victim.level();

        if (level.isClientSide) return;

        Holder<MobEffect> dissolution = BuiltInRegistries.MOB_EFFECT.getHolder(DISSOLUTION_ID).orElse(null);
        if (dissolution == null) return;

        MobEffectInstance victimDissolution = victim.getEffect(dissolution);

        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        boolean isMelee = attacker instanceof LivingEntity && attacker == directEntity;
        boolean isProjectile = directEntity instanceof Projectile;

        if (victimDissolution != null && (isMelee || isProjectile)) {
            long now = victim.level().getGameTime();
            Long lastProc = LAST_ARMOR_DAMAGE_TICK.get(victim.getUUID());
            boolean offCooldown = lastProc == null || now - lastProc >= ARMOR_DAMAGE_COOLDOWN_TICKS;

            float chance = 0.20f + (victimDissolution.getAmplifier() * 0.10f);

            if (offCooldown && victim.getRandom().nextFloat() <= chance) {
                List<ItemStack> equippedArmor = new ArrayList<>();

                for (ItemStack stack : victim.getArmorSlots()) {
                    if (!stack.isEmpty() && stack.isDamageableItem()) {
                        equippedArmor.add(stack);
                    }
                }

                if (!equippedArmor.isEmpty()) {
                    LAST_ARMOR_DAMAGE_TICK.put(victim.getUUID(), now);

                    ItemStack chosenArmor = equippedArmor.get(victim.getRandom().nextInt(equippedArmor.size()));
                    int extremeDamage = (int) Math.ceil(chosenArmor.getMaxDamage() * 0.30f);

                    int currentDamage = chosenArmor.getDamageValue();
                    int newDamage = currentDamage + extremeDamage;

                    if (newDamage >= chosenArmor.getMaxDamage()) {
                        chosenArmor.setDamageValue(chosenArmor.getMaxDamage());
                        EquipmentSlot slot = getEquipmentSlot(victim, chosenArmor);
                        if (slot != null) {
                            victim.broadcastBreakEvent(slot);
                        }
                        chosenArmor.shrink(1);
                    } else {
                        chosenArmor.setDamageValue(newDamage);
                    }

                    ServerLevel serverLevel = (ServerLevel) level;
                    SoundEvent hornBreak = BuiltInRegistries.SOUND_EVENT.get(GOAT_HORN_BREAK_ID);

                    if (hornBreak != null) {
                        serverLevel.playSound(
                                null,
                                victim.getX(),
                                victim.getY(),
                                victim.getZ(),
                                hornBreak,
                                SoundSource.PLAYERS,
                                1.0f,
                                0.5f
                        );
                    }

                    serverLevel.sendParticles(
                            new DustParticleOptions(new Vector3f(0.0f, 1.0f, 0.0f), 1.0f),
                            victim.getX(), victim.getY() + 1.0, victim.getZ(),
                            30, 0.4, 0.5, 0.4, 0.1
                    );

                    serverLevel.sendParticles(
                            new ItemParticleOption(ParticleTypes.ITEM, chosenArmor),
                            victim.getX(), victim.getY() + 1.0, victim.getZ(),
                            20, 0.3, 0.5, 0.3, 0.1
                    );
                }
            }
        }

        if (victimDissolution != null && isOnSporeTeam(victim) && isMelee && attacker instanceof LivingEntity livingAttacker) {
            ServerLevel serverLevel = (ServerLevel) level;

            for (int i = 0; i < 80; i++) {
                double rx = (victim.getRandom().nextDouble() - 0.5D) * 1.2D;
                double ry = victim.getRandom().nextDouble() * 1.5D;
                double rz = (victim.getRandom().nextDouble() - 0.5D) * 1.2D;

                serverLevel.sendParticles(
                        BRIGHT_GREEN_DUST,
                        victim.getX() + rx,
                        victim.getY() + 1.0D + ry,
                        victim.getZ() + rz,
                        1,
                        0.0D, 0.0D, 0.0D, 0.0D
                );
            }

            SoundEvent chemistFuse = BuiltInRegistries.SOUND_EVENT.get(CHEMIST_FUSE_ID);
            if (chemistFuse != null) {
                serverLevel.playSound(
                        null,
                        victim.getX(),
                        victim.getY(),
                        victim.getZ(),
                        chemistFuse,
                        SoundSource.PLAYERS,
                        1.0F,
                        2.0F
                );
            }

            ItemStack weapon = livingAttacker.getMainHandItem();
            if (!weapon.isEmpty() && weapon.isDamageableItem()) {
                int maxDurability = weapon.getMaxDamage();
                int damageToApply = Math.max(1, (int) Math.ceil(maxDurability * 0.01D));

                weapon.hurtAndBreak(damageToApply, livingAttacker, breaker ->
                        breaker.broadcastBreakEvent(EquipmentSlot.MAINHAND)
                );
            }
        }

        if (isMelee && attacker instanceof LivingEntity livingAttacker) {
            MobEffectInstance attackerDissolution = livingAttacker.getEffect(dissolution);

            if (attackerDissolution != null && !isOnSporeTeam(victim)) {
                Holder<MobEffect> corrosion = BuiltInRegistries.MOB_EFFECT.getHolder(CORROSION_ID).orElse(null);
                if (corrosion != null) {
                    victim.addEffect(new MobEffectInstance(corrosion, 20 * 20, 2, false, true));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        Holder<MobEffect> dissolution = BuiltInRegistries.MOB_EFFECT.getHolder(DISSOLUTION_ID).orElse(null);
        if (dissolution == null) return;

        if (entity.hasEffect(dissolution) && entity.getRandom().nextFloat() < 0.05f) {
            entity.spawnAtLocation(ModItems.MUTATION_ESSENCE.get());
        }
    }

    /**
     * Kommandant players, Organoids and Calamities are part of the spore side but shouldn't be hurt
     * by their own team's acid - only the periodic damage is skipped, corrosion/durability loss stays.
     */
    private static boolean isExemptFromAcidDamage(LivingEntity entity) {
        if (entity instanceof Player player && SporeClassUtil.hasClass(player, "kommandant")) {
            return true;
        }
        return entity instanceof Organoid || entity instanceof Calamity;
    }

    private static boolean isOnSporeTeam(Entity entity) {
        Team team = entity.getTeam();
        return team != null && SPORE_TEAM_NAME.equals(team.getName());
    }

    private static EquipmentSlot getEquipmentSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.HEAD) == stack) return EquipmentSlot.HEAD;
        if (entity.getItemBySlot(EquipmentSlot.CHEST) == stack) return EquipmentSlot.CHEST;
        if (entity.getItemBySlot(EquipmentSlot.LEGS) == stack) return EquipmentSlot.LEGS;
        if (entity.getItemBySlot(EquipmentSlot.FEET) == stack) return EquipmentSlot.FEET;
        if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) return EquipmentSlot.OFFHAND;
        return null;
    }
    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}