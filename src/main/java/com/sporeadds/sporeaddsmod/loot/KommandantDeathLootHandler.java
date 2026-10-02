package com.sporeadds.sporeaddsmod.loot;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.combat.WeakPointKillTracker;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.research.PrestigeManager;
import com.sporeadds.sporeaddsmod.research.TrackedEntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "sporeadd")
public class KommandantDeathLootHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(identifierData -> {
            if (!"kommandant".equalsIgnoreCase(identifierData.getIdentifier())) return;

            String subclass = identifierData.getSubclass();
            boolean prestigedBonus = isPrestigedKill(player, serverLevel);

            player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelData -> {
                int level = levelData.getLevel();

                List<String> entries = new ArrayList<>(KommandantLootTables.getBaseForLevel(level));
                entries.addAll(KommandantLootTables.getSubclassForLevel(subclass, level));

                for (String entry : entries) {
                    dropEntry(serverLevel, player, entry, prestigedBonus);
                }
            });

            if (prestigedBonus) {
                WeakPointKillTracker.clear(player.getId());
            }
        });
    }

    private static boolean isPrestigedKill(ServerPlayer target, ServerLevel serverLevel) {
        UUID attackerId = WeakPointKillTracker.getRealKillAttacker(target.getId());
        if (attackerId == null) return false;

        boolean killedByWeakPoint = WeakPointKillTracker.wasKilledByWeakPoint(
                target.getId(), attackerId, serverLevel.getGameTime());
        if (!killedByWeakPoint) return false;

        return PrestigeManager.isPrestigedByAnyScientist(serverLevel.getServer(), TrackedEntities.KOMMANDANT_PLAYER_ID);
    }

    private static void dropEntry(ServerLevel level, ServerPlayer player, String entry, boolean doubled) {
        String[] parts = entry.split("\\|");
        if (parts.length != 3) return;

        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(parts[0]));
        if (item == null) return;

        int min = Integer.parseUnsignedInt(parts[1]);
        int max = Integer.parseUnsignedInt(parts[2]);
        int amount = min == max ? min : player.getRandom().nextIntBetweenInclusive(min, max);

        if (doubled) {
            amount *= 2;
        }

        if (amount <= 0) return;

        ItemStack stack = new ItemStack(item, amount);
        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }
}