package com.sporeadds.sporeaddsmod.Powers.Poder4things;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.Powers.bile.gluttonousAbilityHandler;
import com.sporeadds.sporeaddsmod.Powers.Poder4things.gluttonousPowerHelper;
import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public final class gluttonousBlockHarvestLogic {

    // Which blocks count as "remains" or "bulbs" and the biomass each gives: kommandant_abilities.power_4_devour
    // (remains_blocks / bulb_blocks, remains_biomass / bulb_biomass / bulb_biomass_chance).

    private gluttonousBlockHarvestLogic() {
    }

    public static boolean tryHarvestBlock(Player player, BlockPos pos) {
        if (player.level().isClientSide()) return false;
        if (!gluttonousPowerHelper.isPower4Enabled(player)) return false;

        Block clickedBlock = player.level().getBlockState(pos).getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(clickedBlock);
        if (blockId == null) return false;

        boolean isRemains = KommandantPowersConfig.isRemainsBlock(blockId);
        boolean isBulb = KommandantPowersConfig.isBulbBlock(blockId);

        if (!isRemains && !isBulb) return false;

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
            boolean gluttonousSubclass = gluttonousPowerHelper.isSubclassgluttonous(player);

            if (isRemains) {
                spore.addSpore(KommandantPowersConfig.DEVOUR_REMAINS_BIOMASS.get());

                if (player instanceof ServerPlayer serverPlayer) {
                    int gluttonousReward = player.getRandom().nextIntBetweenInclusive(1, 2);
                    int boneReward = player.getRandom().nextIntBetweenInclusive(1, 2);
                    int goreReward = player.getRandom().nextIntBetweenInclusive(1, 3);

                    for (int i = 0; i < gluttonousReward; i++) {
                        gluttonousAbilityHandler.addGenericFoodConsumed(serverPlayer);
                    }

                    if (gluttonousSubclass) {
                        for (int i = 0; i < boneReward; i++) {
                            gluttonousAbilityHandler.addBoneConsumed(serverPlayer);
                        }

                        for (int i = 0; i < goreReward; i++) {
                            gluttonousAbilityHandler.addGoreConsumed(serverPlayer);
                        }
                    }
                }

                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 5, 0, false, true));
            } else {
                int bulbChance = KommandantPowersConfig.DEVOUR_BULB_CHANCE.get();
                if (bulbChance >= 100 || player.getRandom().nextInt(100) < bulbChance) {
                    spore.addSpore(KommandantPowersConfig.DEVOUR_BULB_BIOMASS.get());
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    int gluttonousReward = player.getRandom().nextIntBetweenInclusive(1, 2);
                    int goreReward = player.getRandom().nextIntBetweenInclusive(1, 2);

                    for (int i = 0; i < gluttonousReward; i++) {
                        gluttonousAbilityHandler.addGenericFoodConsumed(serverPlayer);
                    }

                    if (gluttonousSubclass) {
                        for (int i = 0; i < goreReward; i++) {
                            gluttonousAbilityHandler.addGoreConsumed(serverPlayer);
                        }
                    }
                }

                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 3, 0, false, true));
            }

            player.level().destroyBlock(pos, false, player);
        });

        return true;
    }
}