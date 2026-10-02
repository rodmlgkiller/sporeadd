package com.sporeadds.mixin;

import net.minecraft.core.Holder;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CDUBlockEntity.class, remap = false)
public class CDUBlockEntityMixin {

    private static final int MINIMUM_FROSTBITE_DURATION = 600; // 30 segundos
    private static final int MAX_FROSTBITE_AMPLIFIER = 4;      // Frostbite V máximo visual

    @Inject(
            method = "cleanInfection(Lnet/minecraft/core/BlockPos;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void damageSporeTeamPlayers(BlockPos blockPos, CallbackInfo ci) {
        CDUBlockEntity blockEntity = (CDUBlockEntity) (Object) this;
        net.minecraft.world.level.Level level = blockEntity.getLevel();

        if (level == null || level.isClientSide()) return;

        Holder<MobEffect> frostbiteEffect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "frostbite")).orElse(null);
        if (frostbiteEffect == null) return;

        AABB aabb = AABB.ofSize(
                new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5),
                20, 20, 20
        );

        for (Player player : level.players()) {
            if (!player.isAlive()) continue;
            if (player.getTeam() == null) continue;
            if (!"spore".equalsIgnoreCase(player.getTeam().getName())) continue;
            if (!aabb.contains(player.position())) continue;

            MobEffectInstance currentEffect = player.getEffect(frostbiteEffect);

            if (currentEffect == null) {
                player.addEffect(new MobEffectInstance(
                        frostbiteEffect,
                        MINIMUM_FROSTBITE_DURATION,
                        0,
                        false,
                        true
                ));
                continue;
            }

            if (currentEffect.getDuration() < MINIMUM_FROSTBITE_DURATION) {
                int newAmplifier = Math.min(MAX_FROSTBITE_AMPLIFIER, currentEffect.getAmplifier() + 1);

                player.addEffect(new MobEffectInstance(
                        frostbiteEffect,
                        MINIMUM_FROSTBITE_DURATION,
                        newAmplifier,
                        false,
                        true
                ));
            }
        }
    }
}