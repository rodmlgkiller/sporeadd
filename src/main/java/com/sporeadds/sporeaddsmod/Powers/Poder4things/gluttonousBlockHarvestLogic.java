package com.sporeadds.sporeaddsmod.Powers.Poder4things;

import com.sporeadds.sporeaddsmod.Powers.bile.gluttonousAbilityHandler;
import com.sporeadds.sporeaddsmod.Powers.Poder4things.gluttonousPowerHelper;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public final class gluttonousBlockHarvestLogic {

    private static final ResourceLocation REMAINS_ID = new ResourceLocation("spore", "remains");
    private static final ResourceLocation WALL_REMAINS_ID = new ResourceLocation("spore", "wall_remains");
    private static final ResourceLocation BIOMASS_BULB_ID = new ResourceLocation("spore", "biomass_bulb");
    private static final ResourceLocation DROWNED_LUMP_ID = new ResourceLocation("spore", "drowned_lump");

    private gluttonousBlockHarvestLogic() {
    }

    public static boolean tryHarvestBlock(Player player, BlockPos pos) {
        if (player.level().isClientSide()) return false;
        if (!gluttonousPowerHelper.isPower4Enabled(player)) return false;

        Block clickedBlock = player.level().getBlockState(pos).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(clickedBlock);
        if (blockId == null) return false;

        boolean isRemains = blockId.equals(REMAINS_ID) || blockId.equals(WALL_REMAINS_ID);
        boolean isBulb = blockId.equals(BIOMASS_BULB_ID) || blockId.equals(DROWNED_LUMP_ID);

        if (!isRemains && !isBulb) return false;

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            boolean gluttonousSubclass = gluttonousPowerHelper.isSubclassgluttonous(player);

            if (isRemains) {
                spore.addSpore(3);

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
                spore.addSpore(1);

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