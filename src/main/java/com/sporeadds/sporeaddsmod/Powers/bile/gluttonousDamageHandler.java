package com.sporeadds.sporeaddsmod.Powers.bile;

import net.minecraft.core.Holder;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.joml.Vector3f;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class gluttonousDamageHandler {

    private static final ResourceLocation gluttonous_BULLET_ID = ResourceLocation.fromNamespaceAndPath("spore", "bile_bullet");
    private static final ResourceLocation VARIANT_VOMIT_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "variant_vomit");
    private static final ResourceLocation gluttonousD_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("spore", "gluttonousd");
    private static final ResourceLocation SEASONED_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "seasoned");
    public static final String RECENT_gluttonous_HIT_TAG = "sporeadd_recent_gluttonous_hit";
    public static final String RECENT_gluttonous_BONE_HIT_TAG = "sporeadd_recent_gluttonous_bone_hit";

    private static int getgluttonousdAmplifierForLevel(int level) {
        return switch (level) {
            case 0 -> 0;
            case 1 -> 0;
            case 2 -> 0;
            case 3 -> 1;
            case 4 -> 1;
            case 5 -> 1;
            case 6 -> 1;
            case 7 -> 2;
            case 8 -> 2;
            case 9 -> 2;
            default -> 0;
        };
    }

    private static int getShieldDamageFromProjectileDamage(float damage) {
        return Math.max(1, Mth.ceil(damage));
    }

    private static void damageActiveShield(LivingEntity target, float projectileDamage) {
        if (!(target instanceof Player player)) return;

        ItemStack shield = player.getUseItem();
        if (shield.isEmpty() || !(shield.getItem() instanceof ShieldItem)) return;

        int durabilityDamage = getShieldDamageFromProjectileDamage(projectileDamage);
        if (durabilityDamage <= 0) return;

        InteractionHand hand = player.getUsedItemHand();
        shield.hurtAndBreak(durabilityDamage, player, brokenPlayer ->
                brokenPlayer.broadcastBreakEvent(hand));
    }

    private static void clearDamageCooldown(LivingEntity target) {
        target.invulnerableTime = 0;
        target.hurtTime = 0;
        target.hurtDuration = 0;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        if (!(direct instanceof Projectile projectile)) return;

        ResourceLocation projectileId = BuiltInRegistries.ENTITY_TYPE.getKey(direct.getType());
        if (!gluttonous_BULLET_ID.equals(projectileId) && !VARIANT_VOMIT_ID.equals(projectileId)) return;

        LivingEntity target = event.getEntity();

        float projectileDamage = event.getAmount();
        if (projectile.getPersistentData().contains(gluttonousAbilityHandler.DAMAGE_TAG)) {
            projectileDamage = projectile.getPersistentData().getFloat(gluttonousAbilityHandler.DAMAGE_TAG);
            event.setAmount(projectileDamage);
        }

        if (projectile.getPersistentData().getBoolean(gluttonousAbilityHandler.IGNORE_IFRAMES_TAG)) {
            clearDamageCooldown(target);
        }

        if (target.isBlocking()) {
            Vec3 viewVector = target.getViewVector(1.0F);
            Vec3 toProjectile = direct.position().subtract(target.position()).normalize();

            if (toProjectile.dot(viewVector) > 0.0D) {
                damageActiveShield(target, projectileDamage);

                event.setCanceled(true);
                target.level().playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.SHIELD_BLOCK,
                        SoundSource.PLAYERS,
                        1.0F,
                        0.8F + target.level().random.nextFloat() * 0.4F
                );
                return;
            }
        }

        target.getPersistentData().putInt(RECENT_gluttonous_HIT_TAG, 1);

        boolean isBoneProjectile = false;
        boolean isGoreProjectile = false;

        if (VARIANT_VOMIT_ID.equals(projectileId)) {
            String vomitVariant = projectile.getPersistentData().getString("VomitVariant");
            isBoneProjectile = "bone".equalsIgnoreCase(vomitVariant);
            isGoreProjectile = "gore".equalsIgnoreCase(vomitVariant);
        }

        target.getPersistentData().putBoolean(RECENT_gluttonous_BONE_HIT_TAG, isBoneProjectile);

        boolean shouldApplygluttonousd = true;

        if (isBoneProjectile || isGoreProjectile) {
            shouldApplygluttonousd = false;
        }

        if (shouldApplygluttonousd) {
            Holder<MobEffect> gluttonousdEffect = BuiltInRegistries.MOB_EFFECT.getHolder(gluttonousD_EFFECT_ID).orElse(null);
            if (gluttonousdEffect != null) {
                int playerLevel = 0;

                if (projectile.getOwner() instanceof Player owner) {
                    playerLevel = PlayerLevelProvider.PLAYER_LVL.get(owner)
                            .map(level -> level.getLevel())
                            .orElse(0);
                }

                int amplifier = getgluttonousdAmplifierForLevel(playerLevel);
                target.addEffect(new MobEffectInstance(gluttonousdEffect, 200, amplifier));
            }
        }

        // Efecto y partículas únicas de Gore
        if (isGoreProjectile) {
            Holder<MobEffect> seasonedEffect = BuiltInRegistries.MOB_EFFECT.getHolder(SEASONED_EFFECT_ID).orElse(null);
            if (seasonedEffect != null) {
                // 300 ticks = 15 segundos. Ambient = false, ShowParticles = true.
                target.addEffect(new MobEffectInstance(seasonedEffect, 300, 0, false, true));
            }

            if (target.level() instanceof ServerLevel serverLevel) {
                // Genera el polvo de impacto rojo oscuro (0.7, 0.0, 0.0)
                serverLevel.sendParticles(
                        new DustParticleOptions(new Vector3f(0.7F, 0.0F, 0.0F), 1.0F),
                        target.getX(),
                        target.getY() + (target.getBbHeight() / 2.0D),
                        target.getZ(),
                        20, // Cantidad de partículas
                        target.getBbWidth() / 2.0D,
                        target.getBbHeight() / 2.0D,
                        target.getBbWidth() / 2.0D,
                        0.0D
                );
            }
        }

        target.level().playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.SLIME_SQUISH_SMALL,
                SoundSource.MASTER,
                1.0F,
                0.5F
        );

        if (gluttonous_BULLET_ID.equals(projectileId) && target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new DustParticleOptions(new Vector3f(1.0F, 0.5F, 0.0F), 1.0F),
                    target.getX(),
                    target.getY() + (target.getBbHeight() / 2.0D),
                    target.getZ(),
                    30,
                    target.getBbWidth() / 2.0D,
                    target.getBbHeight() / 2.0D,
                    target.getBbWidth() / 2.0D,
                    0.0D
            );
        }

        if (projectile.getPersistentData().getBoolean(gluttonousAbilityHandler.NO_KNOCKBACK_TAG)) {
            target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
            target.hurtMarked = true;
        }

        if (projectile.getPersistentData().getBoolean(gluttonousAbilityHandler.IGNORE_IFRAMES_TAG)) {
            clearDamageCooldown(target);
        }
    }
}