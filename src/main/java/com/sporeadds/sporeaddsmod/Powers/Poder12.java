package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.Holder;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.core.Sitems;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import com.Harbinger.Spore.Sitems.PCI;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class Poder12 {
    private static final int SPORE_COST = 30;
    private static final int PCI_DAMAGE_PER_CHARGE = 6;
    private static final int PCI_FREEZE_TICKS = 600;

    private static final ResourceLocation ARMOR_HP_KBRES_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "poder12_armor_hp_kbres_uuid");
    private static final Map<UUID, Integer> PREVIOUS_ARMOR_HP = new HashMap<>();
    private static final Map<UUID, Long> SCENT_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Boolean> gluttonous_HELM_BROKEN = new HashMap<>();
    private static final Map<UUID, Long> gluttonous_HELM_RESTORE_TIMERS = new HashMap<>();
    private static final Map<UUID, Boolean> gluttonous_CRITICAL_TRIGGERED = new HashMap<>();

    private static ItemStack getDamageableWeapon(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        Entity directEntity = event.getSource().getDirectEntity();

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return ItemStack.EMPTY;
        }

        if (attacker != directEntity) {
            return ItemStack.EMPTY;
        }

        ItemStack weapon = livingAttacker.getMainHandItem();
        if (weapon.isEmpty() || !weapon.isDamageableItem()) {
            return ItemStack.EMPTY;
        }

        return weapon;
    }

    private static boolean shouldSuppressRedParticles(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!Poder12Variants.isCaustic(player)) return false;
        return !getDamageableWeapon(event).isEmpty();
    }

    private static void spawnImpactParticles(ServerPlayer player) {
        if (player.level().isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) player.level();

        for (int i = 0; i < 80; i++) {
            double rx = (player.getRandom().nextDouble() - 0.5) * 1.2;
            double ry = player.getRandom().nextDouble() * 1.2;
            double rz = (player.getRandom().nextDouble() - 0.5) * 1.2;

            serverLevel.sendParticles(
                    Poder12Variants.DUST_ROJO_IMPACTO,
                    player.getX() + rx,
                    player.getY() + 1 + ry,
                    player.getZ() + rz,
                    1, 0, 0, 0, 0
            );
        }
    }

    private static void dropArmorSlots(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty()) {
            player.drop(helmet, false);
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.isEmpty()) {
            player.drop(chest, false);
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        }

        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!legs.isEmpty()) {
            player.drop(legs, false);
            player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.isEmpty()) {
            player.drop(boots, false);
            player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        }
    }

    private static void equipPowerArmor(ServerPlayer player) {
        boolean abyssal = Poder12Variants.isAbyssal(player);

        ItemStack boots = Poder12Variants.createArmorPiece(player.level().registryAccess(), Sitems.INF_UP_BOOTS.get(), abyssal);
        ItemStack chest = Poder12Variants.createArmorPiece(player.level().registryAccess(), Sitems.INF_UP_CHESTPLATE.get(), abyssal);
        ItemStack helmet = Poder12Variants.createArmorPiece(player.level().registryAccess(), Sitems.INF_UP_HELMET.get(), abyssal);
        ItemStack pants = Poder12Variants.createArmorPiece(player.level().registryAccess(), Sitems.INF_UP_PANTS.get(), abyssal);

        player.setItemSlot(EquipmentSlot.FEET, boots);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        player.setItemSlot(EquipmentSlot.HEAD, helmet);
        player.setItemSlot(EquipmentSlot.LEGS, pants);

        player.inventoryMenu.broadcastChanges();
        player.getInventory().setChanged();
    }

    public static void activate(ServerPlayer player) {
        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
            int currentSpore = spore.getSpore();
            if (currentSpore < SPORE_COST) {
                player.sendSystemMessage(Component.literal("§4Not enough biomass"));
                return;
            }

            spore.subSpore(SPORE_COST);

            PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
                if (data.getArmorHp() == 0) {
                    dropArmorSlots(player);
                }

                int maxArmor = Poder12Variants.getEffectiveArmorCap(player);
                data.setArmorHp(maxArmor);
                data.setArmorHpAndSync(maxArmor, player);
                PREVIOUS_ARMOR_HP.put(player.getUUID(), maxArmor);

                var attr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                if (attr != null) {
                    attr.removeModifier(ARMOR_HP_KBRES_UUID);
                    if (data.getArmorHp() > 0) {
                        attr.addPermanentModifier(
                                new AttributeModifier(
                                        ARMOR_HP_KBRES_UUID,
                                        1.0D,
                                        AttributeModifier.Operation.ADD_VALUE
                                )
                        );
                    }
                }

                equipPowerArmor(player);

                if (Poder12Variants.isgluttonous(player) && maxArmor <= Poder12Variants.gluttonous_HELM_BREAK_THRESHOLD) {
                    Poder12Variants.breakgluttonousHelmet(player, gluttonous_HELM_BROKEN, gluttonous_HELM_RESTORE_TIMERS, true);
                } else {
                    Poder12Variants.setgluttonousHelmetBroken(player, gluttonous_HELM_BROKEN, false);
                    gluttonous_HELM_RESTORE_TIMERS.remove(player.getUUID());
                }

                ServerLevel serverLevel = (ServerLevel) player.level();
                for (int i = 0; i < 60; i++) {
                    double rx = (player.getRandom().nextDouble() - 0.5) * 0.8 * 2.0;
                    double ry = player.getRandom().nextDouble() * 1.5 * 2.0;
                    double rz = (player.getRandom().nextDouble() - 0.5) * 0.8 * 2.0;
                    serverLevel.sendParticles(
                            Poder12Variants.DUST_ROJO,
                            player.getX() + rx,
                            player.getY() + 0.2 + ry,
                            player.getZ() + rz,
                            1, 0, 0, 0, 0
                    );
                }

                serverLevel.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "calamity_damage")),
                        SoundSource.PLAYERS,
                        1.0F, 0.01F
                );
            });
        });
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) return;
        if (effectInstance.getEffect() != MobEffects.HEAL) return;

        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            if (data.getArmorHp() <= 0) return;

            int amplifier = Math.max(0, effectInstance.getAmplifier());
            int durationTicks = Math.max(1, effectInstance.getDuration());

            int instantHealBase = 4 * (amplifier + 1);
            int armorRepair = instantHealBase * 5;
            armorRepair += Math.max(0, durationTicks / 20);

            int currentArmor = data.getArmorHp();
            int maxArmor = Poder12Variants.getEffectiveArmorCap(player);
            int newArmor = Math.min(maxArmor, currentArmor + armorRepair);

            if (newArmor != currentArmor) {
                data.setArmorHpAndSync(newArmor, player);
                PREVIOUS_ARMOR_HP.put(player.getUUID(), newArmor);
            }

            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        });
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().equals(player.level().damageSources().starve())) return;
        // El daño que ignora invulnerabilidad (/kill, vacío, castigo del Proto...) no lo absorbe la armadura.
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            int armorHp = data.getArmorHp();
            if (armorHp <= 0) return;

            if (event.getSource().getEntity() instanceof Player attacker) {
                ItemStack weapon = attacker.getMainHandItem();

                if (weapon.getItem() instanceof PCI pci
                        && pci.getCharge(weapon) > 0
                        && !attacker.getCooldowns().isOnCooldown(pci)) {

                    int charge = pci.getCharge(weapon);
                    int maxAllowedArmorDamage = Math.max(1, Poder12Variants.getMaxArmor(player) / 10);

                    int chargesToUse = (int) Math.floor((float) maxAllowedArmorDamage / PCI_DAMAGE_PER_CHARGE);
                    if (chargesToUse <= 0) {
                        chargesToUse = 1;
                    }

                    chargesToUse = Math.min(chargesToUse, charge);

                    int armorDamage = chargesToUse * PCI_DAMAGE_PER_CHARGE;
                    armorDamage = Math.min(armorDamage, maxAllowedArmorDamage);
                    armorDamage = Math.min(armorDamage, armorHp);

                    if (armorDamage > 0) {
                        int newArmorHp = Math.max(0, armorHp - armorDamage);
                        data.setArmorHpAndSync(newArmorHp, player);

                        pci.setCharge(weapon, charge - chargesToUse);
                        player.setTicksFrozen(PCI_FREEZE_TICKS);
                        attacker.getCooldowns().addCooldown(pci, Math.max(20, chargesToUse * 10));
                        pci.playSound(attacker);

                        event.setAmount(0.0F);
                        event.setCanceled(true);

                        boolean suppressRed = shouldSuppressRedParticles(player, event);
                        if (!suppressRed) {
                            spawnImpactParticles(player);
                        }
                        return;
                    }
                }
            }

            float dmg = event.getAmount();

            if (player.hasEffect(effects.EXQUISITE_CUISINE)) {
                dmg *= 0.8F;
            }

            if (Poder12Variants.isgluttonous(player)
                    && Poder12Variants.isgluttonousHelmetBroken(player, gluttonous_HELM_BROKEN)) {
                dmg *= Poder12Variants.gluttonous_BROKEN_HELM_DAMAGE_MULTIPLIER;
            }

            MobEffectInstance resistanceEffect = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (resistanceEffect != null) {
                float reductionPercentage = Math.min((resistanceEffect.getAmplifier() + 1) * 0.20F, 1.0F);
                dmg = dmg * (1.0F - reductionPercentage);
            }

            int dmgInt = Math.max(1, Math.round(dmg));

            if (resistanceEffect != null && (resistanceEffect.getAmplifier() + 1) * 0.20F >= 1.0F) {
                dmgInt = 0;
            }

            int newArmorHp = Math.max(0, armorHp - dmgInt);
            data.setArmorHpAndSync(newArmorHp, player);

            event.setAmount(0);
            event.setCanceled(true);

            if (dmgInt > 0) {
                ServerLevel serverLevel = (ServerLevel) player.level();

                boolean isSelfGenericActivation =
                        event.getSource().equals(player.level().damageSources().generic()) && dmgInt == 1;
                boolean suppressRed = shouldSuppressRedParticles(player, event);

                if (!isSelfGenericActivation && !suppressRed) {
                    for (int i = 0; i < 80; i++) {
                        double rx = (player.getRandom().nextDouble() - 0.5) * 0.8 * 1.5;
                        double ry = player.getRandom().nextDouble() * 0.8 * 1.5;
                        double rz = (player.getRandom().nextDouble() - 0.5) * 0.8 * 1.5;
                        serverLevel.sendParticles(
                                Poder12Variants.DUST_ROJO,
                                player.getX() + rx,
                                player.getY() + 1 + ry,
                                player.getZ() + rz,
                                1, 0, 0, 0, 0
                        );
                    }

                    serverLevel.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "calamity_damage")),
                            SoundSource.PLAYERS,
                            1.0F, 0.01F
                    );
                }

                if (dmgInt > 20
                        && Poder12Variants.isgluttonous(player)
                        && !event.getSource().equals(player.level().damageSources().fall())) {
                    boolean isPhase3 = newArmorHp < 200;
                    long currentTime = serverLevel.getGameTime();
                    long lastTrigger = SCENT_COOLDOWNS.getOrDefault(player.getUUID(), 0L);
                    long cooldownTicks = isPhase3 ? 125L : 225L;

                    if (currentTime - lastTrigger >= cooldownTicks) {
                        SCENT_COOLDOWNS.put(player.getUUID(), currentTime);
                        NeoForge.EVENT_BUS.register(new ScentSpawnTask(player, serverLevel, isPhase3));

                        serverLevel.playSound(
                                null,
                                player.getX(), player.getY(), player.getZ(),
                                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "evolve_hurt")),
                                SoundSource.PLAYERS,
                                1.0F, 1.0F
                        );
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        PlayerDataProvider.PLAYER_DATA.get(serverPlayer).ifPresent(data -> {
            int currentHp = data.getArmorHp();
            UUID playerId = serverPlayer.getUUID();
            int previousHp = PREVIOUS_ARMOR_HP.getOrDefault(playerId, 0);

            if (previousHp > 0 && currentHp <= 0) {
                ServerLevel serverLevel = (ServerLevel) serverPlayer.level();

                serverLevel.playSound(
                        null,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "hyper_damage")),
                        SoundSource.PLAYERS,
                        10.0F, 0.1F
                );

                serverLevel.playSound(
                        null,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("minecraft", "entity.item.break")),
                        SoundSource.PLAYERS,
                        1.0F, 0.7F
                );

                for (int i = 0; i < 150; i++) {
                    double rx = (serverPlayer.getRandom().nextDouble() - 0.5) * 2.2;
                    double ry = serverPlayer.getRandom().nextDouble() * 2.2;
                    double rz = (serverPlayer.getRandom().nextDouble() - 0.5) * 2.2;
                    serverLevel.sendParticles(ParticleTypes.SMOKE, serverPlayer.getX() + rx, serverPlayer.getY() + 0.3 + ry, serverPlayer.getZ() + rz, 1, 0, 0, 0, 0);
                    serverLevel.sendParticles(Poder12Variants.DUST_ROJO, serverPlayer.getX() + rx, serverPlayer.getY() + 0.3 + ry, serverPlayer.getZ() + rz, 1, 0, 0, 0, 0);
                }

                serverPlayer.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                serverPlayer.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                serverPlayer.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                serverPlayer.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);

                serverPlayer.inventoryMenu.broadcastChanges();
                serverPlayer.getInventory().setChanged();

                var attr = serverPlayer.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                if (attr != null) {
                    attr.removeModifier(ARMOR_HP_KBRES_UUID);
                }

                gluttonous_HELM_BROKEN.remove(playerId);
                gluttonous_HELM_RESTORE_TIMERS.remove(playerId);
                gluttonous_CRITICAL_TRIGGERED.remove(playerId);

                if (Poder12Variants.isgluttonous(serverPlayer)) {
                    PlayerLevelProvider.PLAYER_LVL.get(serverPlayer).ifPresent(levelData -> {
                        int currentLevel = levelData.getLevel();
                        if (currentLevel > 0) {
                            levelData.setLevel(currentLevel - 1);
                            serverPlayer.sendSystemMessage(
                                    Component.translatable("message.sporeadd.power13.grow_weaker")
                                            .withStyle(ChatFormatting.RED)
                            );
                        }
                    });
                }

                serverPlayer.sendSystemMessage(
                        Component.translatable("message.sporeadd.general.armor_collapsed")
                                .withStyle(ChatFormatting.DARK_RED)
                );
            }

            PREVIOUS_ARMOR_HP.put(playerId, currentHp);

            if (currentHp > 0) {
                if (Poder12Variants.isAbyssal(serverPlayer)) {
                    Poder12Variants.applyAbyssalPassive(serverPlayer);
                }

                boolean isgluttonous = Poder12Variants.isgluttonous(serverPlayer);
                if (isgluttonous) {
                    Poder12Variants.tickgluttonousHelmetBurst(serverPlayer);

                    boolean atCritical = currentHp <= Poder12Variants.gluttonous_HELM_BREAK_THRESHOLD;
                    boolean triggered = gluttonous_CRITICAL_TRIGGERED.getOrDefault(playerId, false);

                    if (atCritical && !triggered) {
                        gluttonous_CRITICAL_TRIGGERED.put(playerId, true);

                        Poder12Variants.breakgluttonousHelmet(serverPlayer, gluttonous_HELM_BROKEN, gluttonous_HELM_RESTORE_TIMERS, true);

                        Holder<MobEffect> famined = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "famined")).orElse(null);
                        boolean canTriggerPower13 = famined == null || !serverPlayer.hasEffect(famined);

                        if (canTriggerPower13) {
                            PlayerLevelProvider.PLAYER_LVL.get(serverPlayer).ifPresent(levelData -> {
                                if (levelData.getLevel() >= 9) {
                                    Poder13Variants.activategluttonous((ServerLevel) serverPlayer.level(), serverPlayer);
                                }
                            });
                        }
                    } else if (!atCritical) {
                        gluttonous_CRITICAL_TRIGGERED.remove(playerId);
                    }

                    if (currentHp >= Poder12Variants.gluttonous_HELM_BREAK_THRESHOLD + 1) {
                        if (Poder12Variants.isgluttonousHelmetBroken(serverPlayer, gluttonous_HELM_BROKEN)) {
                            gluttonous_HELM_RESTORE_TIMERS.putIfAbsent(
                                    playerId,
                                    serverPlayer.level().getGameTime() + Poder12Variants.gluttonous_HELM_RESTORE_DELAY_TICKS
                            );

                            Long restoreAt = gluttonous_HELM_RESTORE_TIMERS.get(playerId);
                            if (restoreAt != null && serverPlayer.level().getGameTime() >= restoreAt) {
                                Poder12Variants.restoregluttonousHelmet(serverPlayer, gluttonous_HELM_BROKEN, gluttonous_HELM_RESTORE_TIMERS);
                            }
                        }
                    } else {
                        gluttonous_HELM_RESTORE_TIMERS.remove(playerId);
                    }

                    Poder12Variants.applygluttonousPassives(serverPlayer, currentHp);
                } else {
                    gluttonous_HELM_BROKEN.remove(playerId);
                    gluttonous_HELM_RESTORE_TIMERS.remove(playerId);
                    gluttonous_CRITICAL_TRIGGERED.remove(playerId);
                }

                if (serverPlayer.tickCount % 20 == 0) {
                    Holder<MobEffect> starvation = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "starvation")).orElse(null);
                    boolean hasStarvation = starvation != null && serverPlayer.hasEffect(starvation);

                    int healAmount = Poder12Variants.getVariantHealPerSecond(serverPlayer, currentHp, hasStarvation);

                    MobEffectInstance regenEffect = serverPlayer.getEffect(MobEffects.REGENERATION);
                    if (regenEffect != null) {
                        healAmount += (regenEffect.getAmplifier() + 1) * 2;
                    }

                    if (healAmount > 0) {
                        int maxArmor = Poder12Variants.getEffectiveArmorCap(serverPlayer);
                        int newArmor = Math.min(maxArmor, currentHp + healAmount);
                        if (newArmor != currentHp) {
                            data.setArmorHpAndSync(newArmor, serverPlayer);
                            PREVIOUS_ARMOR_HP.put(playerId, newArmor);
                        }
                    }
                }
            }
        });
    }

    private static class ScentSpawnTask {
        private final ServerPlayer player;
        private final ServerLevel level;
        private final boolean isOvercharged;
        private int ticksElapsed = 0;
        private static final int MAX_TICKS = 60;

        public ScentSpawnTask(ServerPlayer player, ServerLevel level, boolean isOvercharged) {
            this.player = player;
            this.level = level;
            this.isOvercharged = isOvercharged;
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {
            if (!player.isAlive()) {
                NeoForge.EVENT_BUS.unregister(this);
                return;
            }

            ticksElapsed++;

            ResourceLocation particleId = isOvercharged
                    ? ResourceLocation.fromNamespaceAndPath("spore", "blood_particle")
                    : ResourceLocation.fromNamespaceAndPath("spore", "spore_particle");
            ParticleOptions particle = (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(particleId);

            if (particle != null) {
                AABB box = player.getBoundingBox();
                for (int i = 0; i < 5; i++) {
                    double px = box.minX + level.random.nextDouble() * (box.maxX - box.minX);
                    double py = box.minY + level.random.nextDouble() * (box.maxY - box.minY);
                    double pz = box.minZ + level.random.nextDouble() * (box.maxZ - box.minZ);
                    level.sendParticles(particle, px, py, pz, 1, 0, 0.05D, 0, 0.0D);
                }
            }

            if (ticksElapsed >= MAX_TICKS) {
                EntityType<?> scentType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("spore", "scent"));
                if (scentType != null) {
                    Entity scent = scentType.create(level);
                    if (scent instanceof ScentEntity scentEntity) {
                        scentEntity.setPos(player.getX(), player.getY(), player.getZ());
                        if (isOvercharged) {
                            scentEntity.setOvercharged(true);
                        }
                        level.addFreshEntity(scentEntity);
                    }
                }
                NeoForge.EVENT_BUS.unregister(this);
            }
        }
    }
}