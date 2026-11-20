package com.sporeadds.sporeaddsmod.entity;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DataDropItemEntity extends ItemEntity {

    // Lifetime and magnet pickup radius: see ScientistConfig (scientist.research_data.*).
    private static final String SOURCE_ENTITY_TAG = "SourceEntityId";

    private int ticksAlive = 0;

    public DataDropItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.setDefaultPickUpDelay();
        this.setUnlimitedLifetime();
    }

    public static DataDropItemEntity create(Level level, double x, double y, double z, ItemStack stack, String sourceEntityId) {
        DataDropItemEntity entity = new DataDropItemEntity(ModEntities.DATA_DROP_ITEM.get(), level);
        entity.setPos(x, y, z);

        CompoundTag tag = ItemNbt.getOrCreateTag(stack);
        tag.putString(SOURCE_ENTITY_TAG, sourceEntityId);
        ItemNbt.setTag(stack, tag);

        entity.setItem(stack);
        return entity;
    }

    private static boolean isScientist(Player player) {
        return player instanceof ServerPlayer sp
                && SporeIdentifierProvider.SPORE_IDENTIFIER.get(sp)
                .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide) {
            return;
        }

        if (!isScientist(player)) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemStack stack = this.getItem();
        int amount = stack.getCount();

        String sourceEntityId = ItemNbt.hasTag(stack) && ItemNbt.getTag(stack).contains(SOURCE_ENTITY_TAG)
                ? ItemNbt.getTag(stack).getString(SOURCE_ENTITY_TAG)
                : "unknown";

        ScientistResearchProvider.SCIENTIST_RESEARCH.get(serverPlayer).ifPresent(research -> {
            com.sporeadds.sporeaddsmod.research.ResearchEventHelper.recordData(
                    serverPlayer, research, sourceEntityId, amount
            );
        });

        serverPlayer.take(this, amount);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        ticksAlive++;

        if (this.level().isClientSide) {
            return;
        }

        if (ticksAlive >= ScientistConfig.dataLifetimeTicks()) {
            this.discard();
            return;
        }

        collectNearbyScientists();
    }

    /**
     * Magnet pickup (scientist.research_data.pickup_radius, 0 = off): a Scientist within the radius collects this Data
     * without having to touch it.
     */
    private void collectNearbyScientists() {
        double radius = ScientistConfig.dataPickupRadius();
        if (radius <= 0.0D || this.isRemoved()) {
            return;
        }

        double radiusSqr = radius * radius;

        for (Player player : this.level().players()) {
            if (player.isSpectator() || !player.isAlive() || !isScientist(player)) {
                continue;
            }

            if (player.distanceToSqr(this) > radiusSqr) {
                continue;
            }

            this.playerTouch(player);

            if (this.isRemoved()) {
                return;
            }
        }
    }
}