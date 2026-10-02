package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.Damage.Damagetypes2;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DehydrationEffect extends MobEffect {

    private static final UUID SPEED_UUID = UUID.fromString("d1bc5555-1234-4321-a1b2-c3d4e5f6a7b8");
    private static final UUID DAMAGE_UUID = UUID.fromString("d2bc5555-1234-4321-a1b2-c3d4e5f6a7b8");
    private static final UUID HEALTH_UUID = UUID.fromString("d3bc5555-1234-4321-a1b2-c3d4e5f6a7b8");

    private static final ResourceLocation EXTINGUISH_SOUND_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "entity.generic.extinguish_fire");

    private static final Set<UUID> SILENT_REMOVALS = new HashSet<>();

    public DehydrationEffect() {
        super(MobEffectCategory.HARMFUL, 0xEEDC82);
    }

    public static void markNextRemovalSilent(LivingEntity entity) {
        SILENT_REMOVALS.add(entity.getUUID());
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        double speedReduction;
        double damageReduction;
        double healthReduction;

        boolean ignoreMovementPenalty = isCreativeFlying(entity);

        if (amplifier >= 2) {
            speedReduction = ignoreMovementPenalty ? 0.0D : -0.40D;
            damageReduction = -0.60D;
            healthReduction = -12.0D;
        } else if (amplifier >= 1) {
            speedReduction = ignoreMovementPenalty ? 0.0D : -0.30D;
            damageReduction = -0.50D;
            healthReduction = -8.0D;
        } else {
            speedReduction = ignoreMovementPenalty ? 0.0D : -0.10D;
            damageReduction = -0.30D;
            healthReduction = 0.0D;
        }

        var speedAttr = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_UUID);
            if (speedReduction != 0.0D) {
                speedAttr.addPermanentModifier(new AttributeModifier(
                        SPEED_UUID,
                        "Dehydration speed",
                        speedReduction,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }

        var damageAttr = attributeMap.getInstance(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.removeModifier(DAMAGE_UUID);
            damageAttr.addPermanentModifier(new AttributeModifier(
                    DAMAGE_UUID,
                    "Dehydration damage",
                    damageReduction,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }

        var healthAttr = attributeMap.getInstance(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_UUID);
            if (healthReduction < 0) {
                healthAttr.addPermanentModifier(new AttributeModifier(
                        HEALTH_UUID,
                        "Dehydration health",
                        healthReduction,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }

        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        var speedAttr = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_UUID);

        var damageAttr = attributeMap.getInstance(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) damageAttr.removeModifier(DAMAGE_UUID);

        var healthAttr = attributeMap.getInstance(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_UUID);
            if (entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }

        if (!SILENT_REMOVALS.remove(entity.getUUID())) {
            playExtinguishSound(entity);
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }

        WaterCureResult waterCure = getWaterCureResult(entity);
        if (waterCure.shouldCure()) {
            if (waterCure.consumeWaterBlock()) {
                removeWaterSource(entity.level(), waterCure.blockPos());
            }
            entity.removeEffect(this);
            return;
        }

        if (!entity.onGround() && !isCreativeFlying(entity)) {
            double airFactor = amplifier >= 2 ? 0.96D : (amplifier >= 1 ? 0.97D : 0.99D);
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x * airFactor, motion.y, motion.z * airFactor);
            entity.hurtMarked = true;
        }

        if (amplifier >= 2 && entity instanceof Player player) {
            MobEffectInstance instance = entity.getEffect(this);
            if (instance != null) {
                int duration = instance.getDuration();

                if (duration % 200 == 0) {
                    PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
                        if (spore.getSpore() >= 1) {
                            spore.addSpore(-1);
                        } else {
                            player.hurt(Damagetypes2.dehydration(player), 6.0F);
                        }
                    });
                }
            }
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of(new ItemStack(Items.MILK_BUCKET));
    }

    private static boolean isCreativeFlying(LivingEntity entity) {
        return entity instanceof Player player
                && player.getAbilities().flying
                && player.getAbilities().mayfly;
    }

    private static boolean hasActiveArmorHp(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        return PlayerDataProvider.PLAYER_DATA.get(player)
                .map(data -> data.getArmorHp() > 0)
                .orElse(false);
    }

    private static void playExtinguishSound(LivingEntity entity) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(EXTINGUISH_SOUND_ID);
        if (sound != null) {
            entity.level().playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    sound,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }

    private static WaterCureResult getWaterCureResult(LivingEntity entity) {
        if (hasActiveArmorHp(entity)) {
            return new WaterCureResult(true, false, null);
        }

        BlockPos feetPos = entity.blockPosition();
        BlockPos bodyPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().minY + 0.5D, entity.getZ());
        Level level = entity.level();

        if (isWaterSourceAt(level, bodyPos)) {
            return new WaterCureResult(true, true, bodyPos);
        }

        if (isWaterSourceAt(level, feetPos)) {
            return new WaterCureResult(true, true, feetPos);
        }

        if (isInsideWaterCauldron(entity)) {
            return new WaterCureResult(true, false, null);
        }

        if (isStandingInRain(entity)) {
            return new WaterCureResult(true, false, null);
        }

        return new WaterCureResult(false, false, null);
    }

    private static boolean isStandingInRain(LivingEntity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        if (!level.isRaining() || !level.canSeeSky(pos)) return false;
        return level.getBiome(pos).value().getPrecipitationAt(pos) == Biome.Precipitation.RAIN;
    }

    private static boolean isInsideWaterCauldron(LivingEntity entity) {
        Level level = entity.level();
        BlockPos feetPos = entity.blockPosition();
        BlockPos bodyPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().minY + 0.5D, entity.getZ());
        return isWaterCauldronAt(level, feetPos) || isWaterCauldronAt(level, bodyPos);
    }

    private static boolean isWaterCauldronAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.WATER_CAULDRON)) {
            if (state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                return state.getValue(LayeredCauldronBlock.LEVEL) > 0;
            }
            return true;
        }
        return false;
    }

    private static boolean isWaterSourceAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        return state.is(Blocks.WATER) && fluidState.is(FluidTags.WATER) && fluidState.isSource();
    }

    private static void removeWaterSource(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        if (state.is(Blocks.WATER) && fluidState.is(FluidTags.WATER) && fluidState.isSource()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private record WaterCureResult(boolean shouldCure, boolean consumeWaterBlock, BlockPos blockPos) {}
}