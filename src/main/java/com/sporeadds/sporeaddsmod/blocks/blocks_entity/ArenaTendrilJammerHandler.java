package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.neoforge.event.tick.EntityTickEvent;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class ArenaTendrilJammerHandler {

    private static final int JAMMER_CHECK_INTERVAL = 150;
    private static final float JAMMER_CHANCE = 0.75F;
    private static final int SEARCH_RADIUS = 150;
    private static final int JAM_TIME_TICKS = 160;

    private static final float FREEZE_DAMAGE_REDUCTION = 0.95F;
    private static final double MOUND_MAX_HEALTH = 30.0D;
    private static final double MOUND_BASE_ARMOR = 30.0D;

    private static final ResourceLocation CDU_BLOCK_ID = ResourceLocation.fromNamespaceAndPath("spore", "cdu");
    private static final ResourceLocation FREEZER_BLOCK_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "freezer_block");

    private static final String JAMMER_TAG = "SporeAdds_IsJammer";
    private static final String JAMMER_TIMER_TAG = "JammingTimer";
    private static final String MACHINE_X_TAG = "SporeAdds_MachineX";
    private static final String MACHINE_Y_TAG = "SporeAdds_MachineY";
    private static final String MACHINE_Z_TAG = "SporeAdds_MachineZ";
    private static final String ARENA_TENDRIL_TAG = "SporeAdds_IsArenaTendril";
    private static final String ARENA_JAMMER_TIMER_TAG = "SporeAdds_JammerTimer";
    private static final String SPAWN_SOURCE_TAG = "SporeAdds_SpawnSource";
    private static final String NO_HARD_FLOOR_DESPAWN_TAG = "SporeAdds_NoHardFloorDespawn";
    private static final String ARENA_MOUND_SOURCE = "ArenaTendrilJammer";

    private static final Map<UUID, BlockPos> jammingMounds = new HashMap<>();

    private static boolean isOwnedByArenaTendril(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.getBoolean(JAMMER_TAG) && ARENA_MOUND_SOURCE.equals(data.getString(SPAWN_SOURCE_TAG));
    }

    @SubscribeEvent
    public static void onArenaTendrilTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (entity.level().isClientSide()) {
            return;
        }

        CompoundTag data = entity.getPersistentData();
        if (data.getBoolean(ARENA_TENDRIL_TAG)) {
            int timer = data.getInt(ARENA_JAMMER_TIMER_TAG) + 1;

            if (timer >= JAMMER_CHECK_INTERVAL) {
                timer = 0;
                ServerLevel level = (ServerLevel) entity.level();
                tryJamNearbyActiveMachine(level, entity.blockPosition());
            }

            data.putInt(ARENA_JAMMER_TIMER_TAG, timer);
        }
    }

    private static boolean isMachineBeingJammed(ServerLevel level, BlockPos machinePos) {
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(machinePos).inflate(8.0D),
                ArenaTendrilJammerHandler::isOwnedByArenaTendril
        )) {
            CompoundTag data = entity.getPersistentData();

            if (data.contains(MACHINE_X_TAG) && data.contains(MACHINE_Y_TAG) && data.contains(MACHINE_Z_TAG)) {
                BlockPos linkedPos = new BlockPos(
                        data.getInt(MACHINE_X_TAG),
                        data.getInt(MACHINE_Y_TAG),
                        data.getInt(MACHINE_Z_TAG)
                );

                if (linkedPos.equals(machinePos)) {
                    return true;
                }
            }
        }

        return false;
    }

    @SubscribeEvent
    public static void onMoundTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (entity.level().isClientSide()) {
            return;
        }

        if (!isOwnedByArenaTendril(entity)) {
            return;
        }

        CompoundTag data = entity.getPersistentData();

        BlockPos machinePos = jammingMounds.get(entity.getUUID());
        if (machinePos == null && data.contains(MACHINE_X_TAG) && data.contains(MACHINE_Y_TAG) && data.contains(MACHINE_Z_TAG)) {
            machinePos = new BlockPos(
                    data.getInt(MACHINE_X_TAG),
                    data.getInt(MACHINE_Y_TAG),
                    data.getInt(MACHINE_Z_TAG)
            );
            jammingMounds.put(entity.getUUID(), machinePos);
        }

        if (machinePos != null) {
            int ticks = data.getInt(JAMMER_TIMER_TAG) + 1;

            if (ticks >= JAM_TIME_TICKS) {
                ServerLevel level = (ServerLevel) entity.level();
                BlockEntity be = level.getBlockEntity(machinePos);

                if (be != null && isMachineActive(be, be.getBlockState().getBlock())) {
                    jamMachine(level, machinePos);
                }

                ticks = 0;
            }

            data.putInt(JAMMER_TIMER_TAG, ticks);

            if (entity.getRemainingFireTicks() > 0) {
                entity.setRemainingFireTicks(0);
            }
        }
    }

    @SubscribeEvent
    public static void onMoundDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        if (!isOwnedByArenaTendril(entity)) {
            return;
        }

        if (event.getSource().is(DamageTypes.FREEZE)) {
            float reducedDamage = event.getAmount() * (1.0F - FREEZE_DAMAGE_REDUCTION);
            event.setAmount(reducedDamage);

            if (entity.getRemainingFireTicks() > 0) {
                entity.setRemainingFireTicks(0);
            }
            return;
        }

        if (event.getSource().is(DamageTypes.IN_FIRE) || event.getSource().is(DamageTypes.ON_FIRE)) {
            return;
        }

        if (event.getSource().is(DamageTypes.MAGIC)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMoundDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        if (!isOwnedByArenaTendril(entity)) {
            return;
        }

        CompoundTag data = entity.getPersistentData();
        jammingMounds.remove(entity.getUUID());
        data.remove(JAMMER_TIMER_TAG);
        data.remove(MACHINE_X_TAG);
        data.remove(MACHINE_Y_TAG);
        data.remove(MACHINE_Z_TAG);
    }

    private static void jamMachine(ServerLevel level, BlockPos machinePos) {
        BlockEntity be = level.getBlockEntity(machinePos);
        if (be == null) {
            return;
        }

        if (be instanceof CDUBlockEntity cdu) {
            cdu.setFuel(0);

            BlockState state = level.getBlockState(machinePos);
            Property<?> litProp = state.getBlock().getStateDefinition().getProperty("lit");

            if (litProp instanceof BooleanProperty booleanProperty) {
                level.setBlock(machinePos, state.setValue(booleanProperty, true), 3);
            }

            cdu.setChanged();

            BlockState newState = level.getBlockState(machinePos);
            level.sendBlockUpdated(machinePos, newState, newState, 3);
        } else if (be instanceof FreezerBlockEntity freezer) {
            FreezerBlockEntity.corruptMachine(level, machinePos);
            freezer.setChanged();

            BlockState state = level.getBlockState(machinePos);
            level.sendBlockUpdated(machinePos, state, state, 3);
        }
    }

    private static void tryJamNearbyActiveMachine(ServerLevel level, BlockPos center) {
        Block cduBlock = BuiltInRegistries.BLOCK.get(CDU_BLOCK_ID);
        Block freezerBlock = BuiltInRegistries.BLOCK.get(FREEZER_BLOCK_ID);

        double radiusSqr = SEARCH_RADIUS * SEARCH_RADIUS;

        int chunkMinX = (center.getX() - SEARCH_RADIUS) >> 4;
        int chunkMaxX = (center.getX() + SEARCH_RADIUS) >> 4;
        int chunkMinZ = (center.getZ() - SEARCH_RADIUS) >> 4;
        int chunkMaxZ = (center.getZ() + SEARCH_RADIUS) >> 4;

        for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
            for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }

                LevelChunk chunk = level.getChunk(cx, cz);

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();

                    if (pos.distSqr(center) > radiusSqr) {
                        continue;
                    }

                    BlockEntity be = entry.getValue();
                    Block block = be.getBlockState().getBlock();

                    boolean isCdu = cduBlock != null && block == cduBlock;
                    boolean isFreezer = freezerBlock != null && block == freezerBlock;

                    if ((isCdu || isFreezer)
                            && isMachineActive(be, block)
                            && !isMachineBeingJammed(level, pos)
                            && level.random.nextFloat() <= JAMMER_CHANCE) {
                        spawnAntiReaverMound(level, pos, isFreezer);
                    }
                }
            }
        }
    }

    private static boolean isMachineActive(BlockEntity be, Block block) {
        if (be instanceof CDUBlockEntity cduEntity) {
            Property<?> litProp = block.getStateDefinition().getProperty("lit");
            boolean isLit = false;

            if (litProp instanceof BooleanProperty booleanProperty) {
                isLit = be.getBlockState().getValue(booleanProperty);
            }

            return !isLit && cduEntity.isRunning();
        } else if (be instanceof FreezerBlockEntity freezer) {
            return !freezer.isJammed();
        }

        return false;
    }

    private static void applyJammerData(Mob mound, BlockPos machinePos) {
        mound.addTag("unrecolectable");
        mound.addTag("anti_reaver");
        mound.addTag(NO_HARD_FLOOR_DESPAWN_TAG);

        CompoundTag data = mound.getPersistentData();
        data.putInt(JAMMER_TIMER_TAG, 0);
        data.putBoolean(JAMMER_TAG, true);
        data.putBoolean(NO_HARD_FLOOR_DESPAWN_TAG, true);
        data.putInt(MACHINE_X_TAG, machinePos.getX());
        data.putInt(MACHINE_Y_TAG, machinePos.getY());
        data.putInt(MACHINE_Z_TAG, machinePos.getZ());
        data.putString(SPAWN_SOURCE_TAG, ARENA_MOUND_SOURCE);
    }

    private static void spawnAntiReaverMound(ServerLevel level, BlockPos machinePos, boolean isFreezer) {
        EntityType<?> moundType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("spore", "mound"));
        if (moundType == null) {
            return;
        }

        BlockPos spawnPos = isFreezer ? machinePos.above(2) : machinePos.above();
        double spawnX = isFreezer ? machinePos.getX() + 1.0D : machinePos.getX() + 0.5D;
        double spawnZ = isFreezer ? machinePos.getZ() + 1.0D : machinePos.getZ() + 0.5D;

        if (!level.getBlockState(spawnPos).isAir()) {
            return;
        }

        Entity entity = moundType.create(level);
        if (!(entity instanceof Mob mound)) {
            return;
        }

        mound.moveTo(spawnX, spawnPos.getY(), spawnZ, 0.0F, 0.0F);
        mound.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null);
        mound.setPersistenceRequired();
        mound.setCustomName(Component.translatable("entity.sporeadd.jamming_mound"));
        mound.setCustomNameVisible(false);

        if (mound.getAttribute(Attributes.MAX_HEALTH) != null) {
            mound.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MOUND_MAX_HEALTH);
        }

        if (mound.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            mound.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(MOUND_MAX_HEALTH);
        }

        if (mound.getAttribute(Attributes.ARMOR) != null) {
            mound.getAttribute(Attributes.ARMOR).setBaseValue(MOUND_BASE_ARMOR);
        }

        mound.setHealth((float) MOUND_MAX_HEALTH);

        applyJammerData(mound, machinePos);

        level.addFreshEntity(mound);
        jammingMounds.put(mound.getUUID(), machinePos);
    }
}
