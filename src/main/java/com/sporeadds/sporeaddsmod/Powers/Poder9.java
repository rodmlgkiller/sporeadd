package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.Holder;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.FoliageSpread;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
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
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import org.joml.Vector3f;

import java.util.List;

public class Poder9 extends PowerBase {

    protected static final int PHASE_COST = 5;
    private static final double EFFECT_RADIUS_NORMAL = 17.0;
    private static final FoliageSpread FOLIAGE_SPREAD = new FoliageSpread() {};

    // -------------------------------------------------------------------------
    // Subclass helpers
    // -------------------------------------------------------------------------

    private boolean isAbyssal(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            return "abyssal".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    private boolean isCaustic(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            return "caustic".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    private boolean isgluttonous(ServerPlayer player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
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

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
            int currentPhase = spore.getSpore();
            if (currentPhase < PHASE_COST) {
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
                spore.addSpore(-PHASE_COST);
                Poder9Variants.activateCausticSprayMode(player, serverLevel);
                return;
            }

            // Flujo estándar (Kommandant sin subclase)
            spore.addSpore(-PHASE_COST);
            PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(lvl ->
                    executeStandardLogic(player, serverLevel, lvl.getLevel())
            );
        });
    }

    // =========================================================================
    // STANDARD LOGIC (Kommandant sin subclase)
    // =========================================================================

    private void executeStandardLogic(ServerPlayer player, ServerLevel level, int playerLevel) {
        int durationTicks = (30 + Math.max(playerLevel, 0) * 5) * 20;

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "limb_slash")),
                SoundSource.MASTER, 1.0f, 1.0f);

        level.sendParticles(new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f),
                player.getX(), player.getY(), player.getZ(), 5000, 5, 5, 5, 0);

        AABB effectArea = new AABB(
                player.getX() - EFFECT_RADIUS_NORMAL, player.getY() - EFFECT_RADIUS_NORMAL, player.getZ() - EFFECT_RADIUS_NORMAL,
                player.getX() + EFFECT_RADIUS_NORMAL, player.getY() + EFFECT_RADIUS_NORMAL, player.getZ() + EFFECT_RADIUS_NORMAL
        );
        List<LivingEntity> entitiesInArea = level.getEntitiesOfClass(LivingEntity.class, effectArea);

        Holder<MobEffect> myceliumEffect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore",    "mycelium_ef")).orElse(null);
        Holder<MobEffect> exposedEffect  = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "exposed")).orElse(null);

        for (LivingEntity target : entitiesInArea) {
            if (target == player) continue;
            if (myceliumEffect != null) target.addEffect(new MobEffectInstance(myceliumEffect, durationTicks, 2, false, true));
            if (target.getTeam() == null || !"spore".equalsIgnoreCase(target.getTeam().getName())) {
                if (exposedEffect != null) target.addEffect(new MobEffectInstance(exposedEffect, durationTicks, 1, false, true));
            }
        }

        for (int i = 0; i < 20; i++) FOLIAGE_SPREAD.SpreadInfection(level, 15.0, player.blockPosition());

        Block biomassBulb = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("spore", "biomass_bulb"));
        Block fangLump    = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("spore", "fang_lump"));

        if (biomassBulb != null && fangLump != null && biomassBulb != net.minecraft.world.level.block.Blocks.AIR) {
            int radius = (int) EFFECT_RADIUS_NORMAL;
            BlockPos centerPos = player.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(centerPos.offset(-radius, -radius, -radius), centerPos.offset(radius, radius, radius))) {
                if (pos.distSqr(centerPos) <= radius * radius && level.getBlockState(pos).is(biomassBulb)) {
                    level.setBlock(pos, fangLump.defaultBlockState(), 3);
                }
            }
        }
    }
}