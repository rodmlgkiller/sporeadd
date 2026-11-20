package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.minecraft.core.Holder;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.core.Sitems;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.ConfigEffects;
import com.sporeadds.sporeaddsmod.config.KommandantAdvancedConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Poder12Variants {
    public static final DustParticleOptions DUST_ROJO =
            new DustParticleOptions(new Vec3(0.8F, 0.07F, 0.07F).toVector3f(), 0.5F);

    public static final DustParticleOptions DUST_ROJO_IMPACTO =
            new DustParticleOptions(new Vector3f(Vec3.fromRGB24(0x8B0000).toVector3f()), 2.0F);

    // The Armor HP numbers are configurable (power_12_biomass_armor in the config). They are read when used,
    // never at class load, because the config is not loaded yet at that point.
    public static int gluttonousFaminedArmorCap() {
        return KommandantAdvancedConfig.GLUTTONOUS_FAMINED_CAP.get();
    }

    public static int gluttonousHelmBreakThreshold() {
        return KommandantAdvancedConfig.GLUTTONOUS_HELM_BREAK_HP.get();
    }

    public static long gluttonousHelmRestoreDelayTicks() {
        return KommandantAdvancedConfig.GLUTTONOUS_HELM_RESTORE_TICKS.get();
    }

    public static float gluttonousBrokenHelmDamageMultiplier() {
        return KommandantAdvancedConfig.GLUTTONOUS_BROKEN_HELM_MULTIPLIER.get().floatValue();
    }

    public static final int gluttonous_HELM_BURST_DURATION_TICKS = 60;
    public static final double gluttonous_HELM_BURST_RADIUS = 12.0D;
    public static final int gluttonous_HELM_BURST_SLOW_AMPLIFIER = 100;

    public static final int gluttonous_HELM_BREAK_SPEED_DURATION_TICKS = 200;
    public static final int gluttonous_HELM_BREAK_SPEED_AMPLIFIER = 4;

    private static final Map<UUID, Long> gluttonous_HELM_BURST_END = new HashMap<>();

    private Poder12Variants() {}

    public static boolean isCaustic(Player player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String subclass = data.getSubclass();
                    return subclass != null && subclass.trim().equalsIgnoreCase("caustic");
                })
                .orElse(false);
    }

    public static boolean isAbyssal(Player player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String subclass = data.getSubclass();
                    return subclass != null && subclass.trim().equalsIgnoreCase("abyssal");
                })
                .orElse(false);
    }

    public static boolean isgluttonous(Player player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String subclass = data.getSubclass();
                    return subclass != null && subclass.trim().equalsIgnoreCase("gluttonous");
                })
                .orElse(false);
    }

    public static boolean hasFamined(Player player) {
        Holder<MobEffect> famined = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "famined")).orElse(null);
        return famined != null && player.hasEffect(famined);
    }

    /**
     * Armor HP pool of a player: 250 by default, 220 Abyssal, 500 Gluttonous, 180 Caustic (all configurable in
     * power_12_biomass_armor.hp). Also used by the armor HUD and the boss bar.
     */
    public static int getMaxArmor(Player player) {
        if (isAbyssal(player)) {
            return Math.max(1, KommandantAdvancedConfig.ARMOR_MAX_HP_ABYSSAL.get());
        }

        AtomicInteger maxArmor = new AtomicInteger(KommandantAdvancedConfig.ARMOR_MAX_HP.get());

        SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(identifier -> {
            String subclass = identifier.getSubclass();
            if (subclass != null) {
                String normalized = subclass.trim();

                if (normalized.equalsIgnoreCase("gluttonous")) {
                    maxArmor.set(KommandantAdvancedConfig.ARMOR_MAX_HP_GLUTTONOUS.get());
                } else if (normalized.equalsIgnoreCase("caustic")) {
                    maxArmor.set(KommandantAdvancedConfig.ARMOR_MAX_HP_CAUSTIC.get());
                }
            }
        });

        return Math.max(1, maxArmor.get());
    }

    public static int getEffectiveArmorCap(Player player) {
        int maxArmor = getMaxArmor(player);
        if (isgluttonous(player) && hasFamined(player)) {
            return Math.min(maxArmor, gluttonousFaminedArmorCap());
        }
        return maxArmor;
    }

    /** true when the power puts the four armor pieces on the player (config equipment.equip_armor_pieces). */
    public static boolean equipsArmorPieces() {
        return KommandantAdvancedConfig.ARMOR_EQUIP_PIECES.get();
    }

    /**
     * Gives an item the player was wearing back to them, honouring config equipment.previous_armor_mode: thrown on
     * the ground (default) or moved to the inventory (dropped only if the inventory is full).
     */
    public static void storeOrDrop(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (KommandantAdvancedConfig.ARMOR_PREVIOUS_MODE.get() == KommandantAdvancedConfig.PreviousArmorMode.INVENTORY) {
            // Armor does not stack: it needs a free slot. A creative player's full inventory would otherwise swallow it.
            ItemStack toStore = stack.copy();
            boolean stored = player.getInventory().getFreeSlot() >= 0 && player.getInventory().add(toStore);
            if (!stored || !toStore.isEmpty()) {
                player.drop(toStore, false);
            }
        } else {
            player.drop(stack, false);
        }
    }

    /** true for the four Spore armor pieces this power puts on the player. */
    public static boolean isPowerArmorPiece(ItemStack stack) {
        return stack.is(Sitems.INF_UP_HELMET.get())
                || stack.is(Sitems.INF_UP_CHESTPLATE.get())
                || stack.is(Sitems.INF_UP_PANTS.get())
                || stack.is(Sitems.INF_UP_BOOTS.get());
    }

    /**
     * The vanilla armor + toughness reduction (same formula as CombatRules.getDamageAfterAbsorb). Used only when
     * config damage.apply_vanilla_armor_reduction is on. Damage types that ignore armor are not reduced.
     */
    public static float applyVanillaArmor(Player player, net.minecraft.world.damagesource.DamageSource source, float damage) {
        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
            return damage;
        }

        float armor = (float) player.getArmorValue();
        float toughness = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        float f = 2.0F + toughness / 4.0F;
        float f1 = net.minecraft.util.Mth.clamp(armor - damage / f, armor * 0.2F, 20.0F);
        return damage * (1.0F - f1 / 25.0F);
    }

    public static boolean isgluttonousHelmetBroken(Player player, Map<UUID, Boolean> brokenMap) {
        return brokenMap.getOrDefault(player.getUUID(), false);
    }

    public static void setgluttonousHelmetBroken(Player player, Map<UUID, Boolean> brokenMap, boolean broken) {
        if (broken) {
            brokenMap.put(player.getUUID(), true);
        } else {
            brokenMap.remove(player.getUUID());
        }
    }

    public static ItemStack createArmorPiece(net.minecraft.core.HolderLookup.Provider registries, Item item, boolean abyssalMutation) {
        ItemStack stack = new ItemStack(item);

        com.sporeadds.sporeaddsmod.util.EnchantUtil.add(stack, registries, net.minecraft.world.item.enchantment.Enchantments.BINDING_CURSE, 1);

        stack.set(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true));
        stack.setDamageValue(0);

        if (abyssalMutation && stack.getItem() instanceof SporeArmorData armorData) {
            armorData.setVariant(SporeArmorMutations.DROWNED, stack);
        }

        return stack;
    }

    public static void applyAbyssalPassive(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        int lightLevel = player.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos)
                + player.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);

        if (player.isInWaterOrRain() && lightLevel <= 5) {
            // Resistance II for 2 s by default (no particles, no HUD icon); the list is configurable.
            KommandantAdvancedConfig.applyQuiet(player, KommandantAdvancedConfig.effects(KommandantAdvancedConfig.ARMOR_ABYSSAL_EFFECTS), false);
        }
    }

    public static void applygluttonousPassives(ServerPlayer player, int currentHp) {
        if (!isgluttonousHelmetBurstActive(player)) {
            KommandantAdvancedConfig.applyQuiet(player, KommandantAdvancedConfig.effects(KommandantAdvancedConfig.GLUTTONOUS_SLOW_EFFECTS), true);
        }

        int lowHp = KommandantAdvancedConfig.GLUTTONOUS_LOW_HP.get();
        int midHp = KommandantAdvancedConfig.GLUTTONOUS_MID_HP.get();

        if (currentHp >= lowHp && currentHp < midHp) {
            ConfigEffects.apply(player, KommandantAdvancedConfig.effects(KommandantAdvancedConfig.GLUTTONOUS_MID_EFFECTS));
        } else if (currentHp < lowHp) {
            ConfigEffects.apply(player, KommandantAdvancedConfig.effects(KommandantAdvancedConfig.GLUTTONOUS_LOW_EFFECTS));
            KommandantAdvancedConfig.applyQuiet(player, KommandantAdvancedConfig.effects(KommandantAdvancedConfig.GLUTTONOUS_LOW_EFFECTS_NO_PARTICLES), true);
        }
    }

    public static int getVariantHealPerSecond(Player player, int currentHp, boolean hasStarvation) {
        if (hasStarvation) return 0;

        if (isgluttonous(player)) {
            if (currentHp >= KommandantAdvancedConfig.GLUTTONOUS_MID_HP.get()) return KommandantAdvancedConfig.GLUTTONOUS_REGEN_HIGH.get();
            if (currentHp >= KommandantAdvancedConfig.GLUTTONOUS_LOW_HP.get()) return KommandantAdvancedConfig.GLUTTONOUS_REGEN_MID.get();
            return KommandantAdvancedConfig.GLUTTONOUS_REGEN_LOW.get();
        }

        int healAmount = KommandantAdvancedConfig.ARMOR_REGEN_PER_SECOND.get();
        if (isAbyssal(player) && !player.isInWaterOrRain()) {
            healAmount = Math.max(1, (int) Math.floor(healAmount * KommandantAdvancedConfig.ARMOR_ABYSSAL_DRY_MULTIPLIER.get()));
        }
        return healAmount;
    }

    public static void dropCurrentHelmet(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty()) {
            storeOrDrop(player, helmet);
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }
    }

    public static void restoregluttonousHelmet(ServerPlayer player,
                                               Map<UUID, Boolean> brokenMap,
                                               Map<UUID, Long> restoreTimers) {
        boolean abyssal = isAbyssal(player);

        // With equip_armor_pieces off the helmet is never put on (and the player's own helmet is never touched).
        if (equipsArmorPieces()) {
            dropCurrentHelmet(player);
            player.setItemSlot(EquipmentSlot.HEAD, createArmorPiece(player.level().registryAccess(), Sitems.INF_UP_HELMET.get(), abyssal));
            player.inventoryMenu.broadcastChanges();
            player.getInventory().setChanged();
        }

        setgluttonousHelmetBroken(player, brokenMap, false);
        restoreTimers.remove(player.getUUID());
    }

    public static void spawnHelmetBreakParticles(ServerPlayer player, ItemStack brokenHelmet) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        ItemStack chosenArmor = brokenHelmet.isEmpty() ? new ItemStack(Sitems.INF_UP_HELMET.get()) : brokenHelmet.copy();

        double headY = player.getEyeY() - 0.15D;
        double horizontalSpread = Math.max(0.20D, player.getBbWidth() * 0.45D);
        double verticalSpread = Math.max(0.18D, player.getBbHeight() * 0.12D);

        serverLevel.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, chosenArmor),
                player.getX(),
                headY,
                player.getZ(),
                20,
                horizontalSpread,
                verticalSpread,
                horizontalSpread,
                0.1D
        );

        serverLevel.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, chosenArmor),
                player.getX(),
                headY,
                player.getZ(),
                35,
                horizontalSpread * 0.6D,
                verticalSpread * 0.5D,
                horizontalSpread * 0.6D,
                0.04D
        );

        for (int i = 0; i < 25; i++) {
            double rx = (player.getRandom().nextDouble() - 0.5D) * (horizontalSpread * 2.0D);
            double ry = (player.getRandom().nextDouble() - 0.5D) * (verticalSpread * 2.0D);
            double rz = (player.getRandom().nextDouble() - 0.5D) * (horizontalSpread * 2.0D);

            serverLevel.sendParticles(
                    DUST_ROJO_IMPACTO,
                    player.getX() + rx,
                    headY + ry,
                    player.getZ() + rz,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
    }

    public static void triggergluttonousHelmetBurst(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        gluttonous_HELM_BURST_END.put(player.getUUID(), now + gluttonous_HELM_BURST_DURATION_TICKS);

        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                gluttonous_HELM_BURST_DURATION_TICKS,
                gluttonous_HELM_BURST_SLOW_AMPLIFIER,
                false,
                false,
                true
        ));

        SoundEvent growl = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "howler_growl"));
        if (growl != null) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    growl,
                    SoundSource.MASTER,
                    10.0F,
                    0.5F
            );
        }

        burstSmokeAndPush(player, level, true);
    }

    public static boolean isgluttonousHelmetBurstActive(ServerPlayer player) {
        Long end = gluttonous_HELM_BURST_END.get(player.getUUID());
        return end != null && player.level().getGameTime() < end;
    }

    public static void tickgluttonousHelmetBurst(ServerPlayer player) {
        Long end = gluttonous_HELM_BURST_END.get(player.getUUID());
        if (end == null) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();

        if (now >= end) {
            gluttonous_HELM_BURST_END.remove(player.getUUID());
            return;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                5,
                gluttonous_HELM_BURST_SLOW_AMPLIFIER,
                false,
                false,
                true
        ));

        burstSmokeAndPush(player, level, false);
    }

    private static void burstSmokeAndPush(ServerPlayer player, ServerLevel level, boolean firstTick) {
        double radius = gluttonous_HELM_BURST_RADIUS;
        Vec3 center = new Vec3(
                player.getX(),
                player.getY() + (player.getBbHeight() * 0.5D),
                player.getZ()
        );

        AABB area = player.getBoundingBox().inflate(radius);

        for (Entity entity : level.getEntities(player, area, e -> e.isAlive() && e != player)) {
            if (player.getTeam() != null && entity.getTeam() != null && player.getTeam() == entity.getTeam()) {
                continue;
            }

            Vec3 targetCenter = new Vec3(
                    entity.getX(),
                    entity.getY() + (entity.getBbHeight() * 0.5D),
                    entity.getZ()
            );

            Vec3 delta = targetCenter.subtract(center);
            double distance = delta.length();

            if (distance > radius) {
                continue;
            }

            if (distance < 0.001D) {
                delta = new Vec3(
                        (level.random.nextDouble() - 0.5D),
                        (level.random.nextDouble() - 0.5D) * 0.25D,
                        (level.random.nextDouble() - 0.5D)
                );
                distance = Math.max(0.001D, delta.length());
            }

            Vec3 direction = delta.normalize();

            double normalized = 1.0D - (distance / radius);
            double horizontalStrength = (firstTick ? 0.10D : 0.14D) + (normalized * (firstTick ? 0.08D : 0.12D));
            double verticalStrength = 0.02D + (normalized * 0.035D);

            Vec3 push = new Vec3(
                    direction.x * horizontalStrength,
                    verticalStrength,
                    direction.z * horizontalStrength
            );

            entity.setDeltaMovement(entity.getDeltaMovement().add(push));
            entity.hasImpulse = true;
            entity.hurtMarked = true;
        }

        int particleCount = firstTick ? 140 : 55;

        double bodyMinX = player.getBoundingBox().minX;
        double bodyMaxX = player.getBoundingBox().maxX;
        double bodyMinY = player.getBoundingBox().minY;
        double bodyMaxY = player.getBoundingBox().maxY;
        double bodyMinZ = player.getBoundingBox().minZ;
        double bodyMaxZ = player.getBoundingBox().maxZ;

        for (int i = 0; i < particleCount; i++) {
            double spawnX = bodyMinX + level.random.nextDouble() * (bodyMaxX - bodyMinX);
            double spawnY = bodyMinY + level.random.nextDouble() * (bodyMaxY - bodyMinY);
            double spawnZ = bodyMinZ + level.random.nextDouble() * (bodyMaxZ - bodyMinZ);

            double theta = level.random.nextDouble() * (Math.PI * 2.0D);
            double u = (level.random.nextDouble() * 2.0D) - 1.0D;
            double base = Math.sqrt(1.0D - (u * u));

            double dirX = base * Math.cos(theta);
            double dirY = u;
            double dirZ = base * Math.sin(theta);

            double speed = firstTick
                    ? 0.85D + level.random.nextDouble() * 0.55D
                    : 0.35D + level.random.nextDouble() * 0.30D;

            level.sendParticles(
                    ParticleTypes.SMOKE,
                    spawnX,
                    spawnY,
                    spawnZ,
                    1,
                    dirX * speed,
                    dirY * speed,
                    dirZ * speed,
                    0.0D
            );
        }
    }

    public static void breakgluttonousHelmet(ServerPlayer player,
                                             Map<UUID, Boolean> brokenMap,
                                             Map<UUID, Long> restoreTimers,
                                             boolean triggerBurst) {
        if (isgluttonousHelmetBroken(player, brokenMap)) return;

        // With equip_armor_pieces off the helmet is not part of the power, so the player's own one is left alone.
        ItemStack equippedHelmet = equipsArmorPieces() ? player.getItemBySlot(EquipmentSlot.HEAD).copy() : ItemStack.EMPTY;

        if (!equippedHelmet.isEmpty()) {
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }

        spawnHelmetBreakParticles(player, equippedHelmet);

        ServerLevel serverLevel = (ServerLevel) player.level();
        serverLevel.playSound(
                null,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("minecraft", "entity.item.break")),
                SoundSource.PLAYERS,
                1.0F,
                0.7F
        );

        setgluttonousHelmetBroken(player, brokenMap, true);
        restoreTimers.remove(player.getUUID());

        if (triggerBurst) {
            triggergluttonousHelmetBurst(player);
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                gluttonous_HELM_BREAK_SPEED_DURATION_TICKS,
                gluttonous_HELM_BREAK_SPEED_AMPLIFIER,
                false,
                false,
                true
        ));

        player.inventoryMenu.broadcastChanges();
        player.getInventory().setChanged();
    }
}