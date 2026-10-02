package com.sporeadds.sporeaddsmod.entity.projectile;

import net.minecraft.core.Holder;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Vector3f;

/**
 * Kommandant Caustic's skill 9 spray ammo. Snowball-like physics but heavier (falls faster). On block
 * impact it replicates spore:assassin_bullet's puddle (an acid block placed above the hit position, if
 * empty). On entity impact it deals 2 damage, floored to 1% of the target's max health, applies a
 * duration-stacking Dissolution and a non-stacking Corrosion, and chips 3 durability off every
 * damageable item in the target's inventory. A raised shield blocks the hit but takes 1% of its own
 * max durability in damage per projectile blocked.
 */
public class GasGlobProjectile extends ThrowableProjectile {

    private static final EntityDataAccessor<Byte> TEXTURE_INDEX =
            SynchedEntityData.defineId(GasGlobProjectile.class, EntityDataSerializers.BYTE);

    private static final float BASE_DAMAGE = 2.0F;
    private static final float MIN_HEALTH_PERCENT_DAMAGE = 0.02F;
    private static final int DISSOLUTION_DURATION_TICKS = 5 * 20;
    private static final int CORROSION_DURATION_TICKS = 30 * 20;
    private static final int CORROSION_AMPLIFIER = 3;
    private static final int INVENTORY_DURABILITY_DAMAGE = 3;
    private static final float SHIELD_MAX_DURABILITY_PERCENT_DAMAGE = 0.01F;

    private static final DustParticleOptions TRAIL_DUST =
            new DustParticleOptions(new Vector3f(0.2F, 1.0F, 0.2F), 1.0F);
    private static final int HIT_DUST_COUNT = 10;

    public GasGlobProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public GasGlobProjectile(EntityType<? extends ThrowableProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
        this.entityData.set(TEXTURE_INDEX, (byte) this.random.nextInt(3));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TEXTURE_INDEX, (byte) 0);
    }

    public int getTextureIndex() {
        return this.entityData.get(TEXTURE_INDEX);
    }

    @Override
    protected float getGravity() {
        return 0.09F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(TRAIL_DUST, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    private void spawnHitDust(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double width = target.getBbWidth();
        double height = target.getBbHeight();
        for (int i = 0; i < HIT_DUST_COUNT; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * width;
            double oy = this.random.nextDouble() * height;
            double oz = (this.random.nextDouble() - 0.5D) * width;
            serverLevel.sendParticles(TRAIL_DUST,
                    target.getX() + ox, target.getY() + oy, target.getZ() + oz,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) return;

        BlockPos above = result.getBlockPos().above();
        if (this.level().getBlockState(above).isAir()) {
            Block acidBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("spore", "acid"));
            if (acidBlock != null && acidBlock != Blocks.AIR) {
                this.level().setBlock(above, acidBlock.defaultBlockState(), 3);
            }
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SLIME_SQUISH, SoundSource.NEUTRAL, 1.0F, 1.0F);

        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;
        if (!(result.getEntity() instanceof LivingEntity target)) return;

        DamageSource damageSource = com.Harbinger.Spore.core.SdamageTypes.acid(target);

        if (target.isDamageSourceBlocked(damageSource)) {
            damageShield(target);
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else {
            float damage = Math.max(BASE_DAMAGE, target.getMaxHealth() * MIN_HEALTH_PERCENT_DAMAGE);

            resetIFrames(target);
            target.hurt(damageSource, damage);
            resetIFrames(target);

            applyStackingDissolution(target);
            applyCorrosion(target);

            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "chemist_fuse")),
                    SoundSource.NEUTRAL, 1.0F, 2.0F);
        }

        damageInventoryDurability(target);
        spawnHitDust(target);
        this.discard();
    }

    private void damageShield(LivingEntity target) {
        ItemStack shield = target.getUseItem();
        if (shield.isEmpty() || !shield.isDamageableItem()) return;

        int amount = Math.max(1, Math.round(shield.getMaxDamage() * SHIELD_MAX_DURABILITY_PERCENT_DAMAGE));
        shield.hurtAndBreak(amount, target, entity -> entity.broadcastBreakEvent(
                entity.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
    }

    private void applyStackingDissolution(LivingEntity target) {
        Holder<MobEffect> dissolution = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "dissolution")).orElse(null);
        if (dissolution == null) return;

        MobEffectInstance existing = target.getEffect(dissolution);
        int newDuration = DISSOLUTION_DURATION_TICKS + (existing != null ? existing.getDuration() : 0);
        target.addEffect(new MobEffectInstance(dissolution, newDuration, 0, false, true, true));
    }

    private void applyCorrosion(LivingEntity target) {
        Holder<MobEffect> corrosion = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "corrosion")).orElse(null);
        if (corrosion == null) return;

        target.addEffect(new MobEffectInstance(corrosion, CORROSION_DURATION_TICKS, CORROSION_AMPLIFIER, false, true, true));
    }

    private void resetIFrames(LivingEntity target) {
        target.invulnerableTime = 0;
        target.hurtTime = 0;
        target.hurtDuration = 0;
    }

    private void damageInventoryDurability(LivingEntity target) {
        if (!(target instanceof Player player)) return;

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                stack.hurtAndBreak(INVENTORY_DURABILITY_DAMAGE, player, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            }
        }
    }
}
