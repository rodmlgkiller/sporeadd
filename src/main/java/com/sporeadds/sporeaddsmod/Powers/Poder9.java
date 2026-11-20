package com.sporeadds.sporeaddsmod.Powers;

import com.Harbinger.Spore.Sentities.FoliageSpread;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.ConfigEffects;
import com.sporeadds.sporeaddsmod.config.KommandantAdvancedConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.List;

public class Poder9 extends PowerBase {

    /** Biomass cost of every variant of this ability (configurable, read when the ability is used). */
    protected static int phaseCost() {
        return KommandantAdvancedConfig.BURST_BIOMASS_COST.get();
    }

    private static final FoliageSpread FOLIAGE_SPREAD = new FoliageSpread() {};

    // -------------------------------------------------------------------------
    // Subclass helpers
    // -------------------------------------------------------------------------

    private boolean isAbyssal(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER);
        if (cap.isPresent()) {
            return "abyssal".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    private boolean isCaustic(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER);
        if (cap.isPresent()) {
            return "caustic".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    private boolean isgluttonous(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER);
        if (cap.isPresent()) {
            return "gluttonous".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // use()
    // -------------------------------------------------------------------------

    @Override
    public void use(ServerPlayer player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            int currentPhase = spore.getSpore();
            if (currentPhase < phaseCost()) {
                player.sendSystemMessage(Component.translatable("message.sporeadd.power1.not_enough_biomass"));
                return;
            }

            boolean abyssal = isAbyssal(player);
            boolean caustic = isCaustic(player);
            boolean gluttonous    = isgluttonous(player);

            // Delegar variantes complejas a Poder9Variants
            if (gluttonous) {
                Poder9Variants.executegluttonousCall(player, serverLevel, spore);
                return;
            }

            if (abyssal) {
                Poder9Variants.executeAbyssalVortex(player, serverLevel, spore);
                return;
            }

            if (caustic) {
                spore.addSpore(-phaseCost());
                Poder9Variants.activateCausticSprayMode(player, serverLevel);
                return;
            }

            // Flujo estándar (Kommandant sin subclase)
            spore.addSpore(-phaseCost());
            player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(lvl ->
                    executeStandardLogic(player, serverLevel, lvl.getLevel())
            );
        });
    }

    // =========================================================================
    // STANDARD LOGIC (Kommandant sin subclase)
    // =========================================================================

    private void executeStandardLogic(ServerPlayer player, ServerLevel level, int playerLevel) {
        int durationTicks = KommandantAdvancedConfig.burstDurationSeconds(playerLevel) * 20;
        final double effectRadius = KommandantAdvancedConfig.BURST_RADIUS.get();

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "limb_slash")),
                SoundSource.MASTER, 1.0f, 1.0f);

        level.sendParticles(new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f),
                player.getX(), player.getY(), player.getZ(), 5000, 5, 5, 5, 0);

        AABB effectArea = new AABB(
                player.getX() - effectRadius, player.getY() - effectRadius, player.getZ() - effectRadius,
                player.getX() + effectRadius, player.getY() + effectRadius, player.getZ() + effectRadius
        );
        List<LivingEntity> entitiesInArea = level.getEntitiesOfClass(LivingEntity.class, effectArea);

        // Effects come from the config (a duration of 0 there = the level-scaled durationTicks).
        List<ConfigEffects.Entry> effectsForAll = KommandantAdvancedConfig.effectsScaled(KommandantAdvancedConfig.BURST_EFFECTS_ALL, durationTicks);
        List<ConfigEffects.Entry> effectsForEnemies = KommandantAdvancedConfig.effectsScaled(KommandantAdvancedConfig.BURST_EFFECTS_ENEMIES, durationTicks);

        for (LivingEntity target : entitiesInArea) {
            if (target == player) continue;
            ConfigEffects.apply(target, effectsForAll);
            if (target.getTeam() == null || !"spore".equalsIgnoreCase(target.getTeam().getName())) {
                ConfigEffects.apply(target, effectsForEnemies);
            }
        }

        int spreadPasses = KommandantAdvancedConfig.BURST_SPREAD_PASSES.get();
        double spreadRadius = KommandantAdvancedConfig.BURST_SPREAD_RADIUS.get();
        for (int i = 0; i < spreadPasses; i++) FOLIAGE_SPREAD.SpreadInfection(level, spreadRadius, player.blockPosition());

        Block biomassBulb = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("spore", "biomass_bulb"));
        Block fangLump    = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("spore", "fang_lump"));

        if (KommandantAdvancedConfig.BURST_CONVERT_BULBS.get()
                && biomassBulb != null && fangLump != null && biomassBulb != net.minecraft.world.level.block.Blocks.AIR) {
            int radius = (int) effectRadius;
            BlockPos centerPos = player.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(centerPos.offset(-radius, -radius, -radius), centerPos.offset(radius, radius, radius))) {
                if (pos.distSqr(centerPos) <= radius * radius && level.getBlockState(pos).is(biomassBulb)) {
                    level.setBlock(pos, fangLump.defaultBlockState(), 3);
                }
            }
        }
    }
}