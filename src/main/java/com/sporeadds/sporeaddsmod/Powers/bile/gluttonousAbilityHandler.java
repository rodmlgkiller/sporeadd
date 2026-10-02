package com.sporeadds.sporeaddsmod.Powers.bile;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class gluttonousAbilityHandler {

    public static final String FRAGMENTS_TAG = "sporeadd_gluttonous_fragments";
    public static final String GENERIC_FOOD_COUNT_TAG = "sporeadd_generic_food_count";
    public static final String BONE_COUNT_TAG = "sporeadd_bone_count";
    public static final String GORE_COUNT_TAG = "sporeadd_gore_count";

    public static final String DAMAGE_TAG = "sporeadd_gluttonous_damage";
    public static final String NO_KNOCKBACK_TAG = "sporeadd_gluttonous_no_knockback";
    public static final String IGNORE_IFRAMES_TAG = "sporeadd_ignore_iframes";

    private static final int MAX_FRAGMENTS = 25;

    public static int getMaxFragments() {
        return MAX_FRAGMENTS;
    }

    private static final ResourceLocation SPORE_gluttonous_BULLET_ID = ResourceLocation.fromNamespaceAndPath("spore", "bile_bullet");
    private static final ResourceLocation VARIANT_VOMIT_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "variant_vomit");

    private static final float gluttonous_BURST_SPRAY = 6.5F;
    private static final float BONE_BURST_SPRAY = 3.75F;

    public static void addgluttonousFragment(ServerPlayer player, String fragmentType) {
        List<String> fragments = getFragments(player);
        if (fragments.size() < MAX_FRAGMENTS) {
            fragments.add(fragmentType);
            saveFragments(player, fragments);
            displayBar(player, fragments);
        }
    }

    public static void addGenericFoodConsumed(ServerPlayer player) {
        int count = player.getPersistentData().getInt(GENERIC_FOOD_COUNT_TAG) + 1;

        if (count >= 3) {
            List<String> fragments = getFragments(player);
            if (fragments.size() < MAX_FRAGMENTS) {
                fragments.add("gluttonous");
                saveFragments(player, fragments);
                player.getPersistentData().putInt(GENERIC_FOOD_COUNT_TAG, 0);
            } else {
                player.getPersistentData().putInt(GENERIC_FOOD_COUNT_TAG, count - 1);
            }
            displayBar(player, fragments);
        } else {
            player.getPersistentData().putInt(GENERIC_FOOD_COUNT_TAG, count);
        }
    }

    public static void addBoneConsumed(ServerPlayer player) {
        int count = player.getPersistentData().getInt(BONE_COUNT_TAG) + 1;

        if (count >= 5) {
            List<String> fragments = getFragments(player);
            if (fragments.size() < MAX_FRAGMENTS) {
                fragments.add("bone");
                saveFragments(player, fragments);
                player.getPersistentData().putInt(BONE_COUNT_TAG, 0);
            } else {
                player.getPersistentData().putInt(BONE_COUNT_TAG, count - 1);
            }
            displayBar(player, fragments);
        } else {
            player.getPersistentData().putInt(BONE_COUNT_TAG, count);
        }
    }

    public static void addGoreConsumed(ServerPlayer player) {
        int count = player.getPersistentData().getInt(GORE_COUNT_TAG) + 1;

        if (count >= 4) {
            List<String> fragments = getFragments(player);
            if (fragments.size() < MAX_FRAGMENTS) {
                fragments.add("gore");
                saveFragments(player, fragments);
                player.getPersistentData().putInt(GORE_COUNT_TAG, 0);
            } else {
                player.getPersistentData().putInt(GORE_COUNT_TAG, count - 1);
            }
            displayBar(player, fragments);
        } else {
            player.getPersistentData().putInt(GORE_COUNT_TAG, count);
        }
    }

    public static void addGenericFoodConsumed(ServerPlayer player, int amount) {
        for (int i = 0; i < amount; i++) addGenericFoodConsumed(player);
    }

    public static void addBoneConsumed(ServerPlayer player, int amount) {
        for (int i = 0; i < amount; i++) addBoneConsumed(player);
    }

    public static void addGoreConsumed(ServerPlayer player, int amount) {
        for (int i = 0; i < amount; i++) addGoreConsumed(player);
    }

    public static void addManualRewards(ServerPlayer player, int gore, int gluttonous, int bone) {
        if (gore > 0) addGoreConsumed(player, gore);
        if (gluttonous > 0) addGenericFoodConsumed(player, gluttonous);
        if (bone > 0) addBoneConsumed(player, bone);
    }

    private static void fireGoreShotgun(ServerPlayer player) {
        EntityType<?> projectileType = BuiltInRegistries.ENTITY_TYPE.get(VARIANT_VOMIT_ID);
        if (projectileType == null) return;

        int playerLevel = player.getCapability(PlayerLevelProvider.PLAYER_LVL)
                .map(level -> level.getLevel())
                .orElse(0);

        float damage = getGoreDamageForLevel(playerLevel);
        int pelletCount = player.getRandom().nextIntBetweenInclusive(3, 8);

        net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
        net.minecraft.world.phys.Vec3 look = player.getLookAngle();
        double forwardOffset = 0.28D;

        for (int i = 0; i < pelletCount; i++) {
            Entity entity = projectileType.create(player.level());
            if (!(entity instanceof Projectile projectile)) continue;

            projectile.getPersistentData().putFloat(DAMAGE_TAG, damage);
            projectile.getPersistentData().putBoolean(NO_KNOCKBACK_TAG, true);
            projectile.getPersistentData().putBoolean(IGNORE_IFRAMES_TAG, true);
            projectile.getPersistentData().putString("VomitVariant", "gore");

            if (projectile instanceof com.sporeadds.sporeaddsmod.entity.projectile.VariantVomitProjectile vomitProjectile) {
                vomitProjectile.setVariant("gore");
            }

            projectile.setPos(
                    eyePos.x + look.x * forwardOffset,
                    eyePos.y - 0.12D + look.y * forwardOffset,
                    eyePos.z + look.z * forwardOffset
            );

            projectile.setOwner(player);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.5F, 18.0F);

            player.level().addFreshEntity(projectile);
        }

        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.SLIME_SQUISH,
                SoundSource.PLAYERS,
                1.5F,
                0.8F
        );
        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.SLIME_ATTACK,
                SoundSource.PLAYERS,
                1.0F,
                0.5F
        );
    }

    private static List<String> getFragments(ServerPlayer player) {
        if (!player.getPersistentData().contains(FRAGMENTS_TAG)) return new ArrayList<>();
        String raw = player.getPersistentData().getString(FRAGMENTS_TAG);
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    private static void saveFragments(ServerPlayer player, List<String> fragments) {
        player.getPersistentData().putString(FRAGMENTS_TAG, String.join(",", fragments));
    }

    private static void clearBar(ServerPlayer player) {
        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.send(
                com.sporeadds.sporeaddsmod.network.PacketDistributor.PLAYER.with(() -> player),
                new com.sporeadds.sporeaddsmod.network.SyncGluttonousFragmentsPacket("")
        );
    }

    private static void displayBar(ServerPlayer player, List<String> fragments) {
        if (!isgluttonous(player)) {
            clearBar(player);
            return;
        }

        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.send(
                com.sporeadds.sporeaddsmod.network.PacketDistributor.PLAYER.with(() -> player),
                new com.sporeadds.sporeaddsmod.network.SyncGluttonousFragmentsPacket(String.join(",", fragments))
        );
    }

    public static void tryFiregluttonousBurst(ServerPlayer player) {
        if (!isgluttonous(player)) {
            clearBar(player);
            return;
        }

        List<String> fragments = getFragments(player);

        if (!fragments.isEmpty()) {
            String typeToFire = fragments.remove(fragments.size() - 1);
            saveFragments(player, fragments);

            if (typeToFire.equals("gore")) {
                fireGoreShotgun(player);
            } else {
                NeoForge.EVENT_BUS.register(new gluttonousBurstTask(player, typeToFire));
            }

            displayBar(player, fragments);
        } else {
            player.displayClientMessage(Component.translatable("message.sporeadd.out_of_charges"), true);
        }
    }

    private static boolean isgluttonous(ServerPlayer player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()) &&
                        "gluttonous".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);
    }

    private static float getDamageForLevel(int level) {
        return switch (level) {
            case 0 -> 1.0F;
            case 1 -> 1.5F;
            case 2 -> 2.0F;
            case 3 -> 2.5F;
            case 4 -> 3.0F;
            case 5 -> 3.5F;
            case 6 -> 4.0F;
            case 7 -> 4.5F;
            case 8 -> 5.0F;
            case 9 -> 5.5F;
            default -> 1.0F;
        };
    }

    private static float getBoneDamageForLevel(int level) {
        return switch (level) {
            case 0 -> 5.0F;
            case 1 -> 6.0F;
            case 2 -> 7.5F;
            case 3 -> 9.0F;
            case 4 -> 10.5F;
            case 5 -> 12.5F;
            case 6 -> 14.0F;
            case 7 -> 15.5F;
            case 8 -> 16.5F;
            case 9 -> 18.0F;
            default -> 5.0F;
        };
    }

    private static float getGoreDamageForLevel(int level) {
        return switch (level) {
            case 0 -> 0.5F;
            case 1 -> 0.75F;
            case 2 -> 1.0F;
            case 3 -> 1.25F;
            case 4 -> 1.50F;
            case 5 -> 2.0F;
            case 6 -> 2.5F;
            case 7 -> 3.0F;
            case 8 -> 3.5F;
            case 9 -> 4.0F;
            default -> 1.0F;
        };
    }

    public static class gluttonousBurstTask {
        private final ServerPlayer player;
        private final String variant;
        private final int totalBullets;
        private int ticksElapsed = 0;
        private int bulletsFired = 0;

        public gluttonousBurstTask(ServerPlayer player, String variant) {
            this.player = player;
            this.variant = variant;
            this.totalBullets = variant.equalsIgnoreCase("bone") ? 3 : 5;
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {

            if (!player.isAlive()) {
                NeoForge.EVENT_BUS.unregister(this);
                return;
            }

            if (ticksElapsed % 2 == 0) {
                fireBullet();
                bulletsFired++;
            }

            ticksElapsed++;

            if (bulletsFired >= this.totalBullets) {
                NeoForge.EVENT_BUS.unregister(this);
            }
        }

        private void fireBullet() {
            ResourceLocation projectileId = this.variant.equalsIgnoreCase("gluttonous")
                    ? SPORE_gluttonous_BULLET_ID
                    : VARIANT_VOMIT_ID;

            EntityType<?> projectileType = BuiltInRegistries.ENTITY_TYPE.get(projectileId);
            if (projectileType == null) return;

            Entity entity = projectileType.create(player.level());
            if (!(entity instanceof Projectile projectile)) return;

            int playerLevel = player.getCapability(PlayerLevelProvider.PLAYER_LVL)
                    .map(level -> level.getLevel())
                    .orElse(0);

            float damage = this.variant.equalsIgnoreCase("bone")
                    ? getBoneDamageForLevel(playerLevel)
                    : getDamageForLevel(playerLevel);

            projectile.getPersistentData().putFloat(DAMAGE_TAG, damage);
            projectile.getPersistentData().putBoolean(NO_KNOCKBACK_TAG, true);
            projectile.getPersistentData().putBoolean(IGNORE_IFRAMES_TAG, true);

            if (!this.variant.equalsIgnoreCase("gluttonous")) {
                projectile.getPersistentData().putString("VomitVariant", this.variant);

                if (projectile instanceof com.sporeadds.sporeaddsmod.entity.projectile.VariantVomitProjectile vomitProjectile) {
                    vomitProjectile.setVariant(this.variant);
                }
            }

            net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
            double forwardOffset = 0.28D;

            projectile.setPos(
                    eyePos.x + look.x * forwardOffset,
                    eyePos.y - 0.12D + look.y * forwardOffset,
                    eyePos.z + look.z * forwardOffset
            );

            projectile.setOwner(player);

            float spray = this.variant.equalsIgnoreCase("bone") ? BONE_BURST_SPRAY : gluttonous_BURST_SPRAY;
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F, spray);

            player.level().addFreshEntity(projectile);

            if (this.variant.equalsIgnoreCase("bone")) {
                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.LLAMA_SPIT,
                        SoundSource.PLAYERS,
                        1.0F,
                        0.75F
                );
            } else {
                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.SLIME_ATTACK,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.5F + (player.getRandom().nextFloat() * 0.2F)
                );
            }
        }
    }
}