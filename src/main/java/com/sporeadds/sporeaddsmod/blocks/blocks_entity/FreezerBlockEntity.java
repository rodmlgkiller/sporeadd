package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import net.minecraft.core.Holder;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.modblocksentity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag; // Añadido para el NBT
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FreezerBlockEntity extends BlockEntity {
    private int tickCount = 0;

    // Temporizador para el atasco de nieve (en ticks)
    private int ticksUntilSnowJam;

    // Variable para saber si la máquina fue interrumpida/atascada recientemente
    private boolean wasJammed = false;

    private static final int TICKS_PER_SECOND = 20;
    private static final int CLEAN_RADIUS = 100;
    private static final int RADIUS_SQR = CLEAN_RADIUS * CLEAN_RADIUS;
    private static final int SECTOR_SIZE = 34;

    private final List<AABB> sectorsToScan = new ArrayList<>();
    private final Random random = new Random();

    // ==========================================================
    // LISTAS DE VARIEDAD DE FOLIAGE (Techo y Paredes)
    // ==========================================================
    private static final List<ResourceLocation> ROOF_FOLIAGE_OPTIONS = Arrays.asList(
            ResourceLocation.fromNamespaceAndPath("spore", "growths_big"),
            ResourceLocation.fromNamespaceAndPath("spore", "growths_small"),
            ResourceLocation.fromNamespaceAndPath("spore", "rotten_bush"),
            ResourceLocation.fromNamespaceAndPath("spore", "bloomfung")
    );

    private static final List<ResourceLocation> WALL_FOLIAGE_OPTIONS = Arrays.asList(
            ResourceLocation.fromNamespaceAndPath("spore", "wall_growths"),
            ResourceLocation.fromNamespaceAndPath("spore", "wall_growths_fleshy")
    );

    public static final TagKey<Block> REMOVABLE_FOLIAGE = TagKey.create(
            BuiltInRegistries.BLOCK.getRegistryKey(),
            ResourceLocation.fromNamespaceAndPath("spore", "removable_foliage")
    );
    public static final TagKey<Block> INFECTED_BIOMASS = TagKey.create(
            BuiltInRegistries.BLOCK.getRegistryKey(),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "infected_biomass")
    );

    private static final List<ResourceLocation> BIOMASS_BLOCKS = Arrays.asList(
            ResourceLocation.fromNamespaceAndPath("sporeadd", "biomass_block"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "sicken_biomass_block"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "calcified_biomass_block"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "membrane_block"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "rooted_biomass"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "gastric_biomass_block"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "rooted_mycelium"),
            ResourceLocation.fromNamespaceAndPath("spore", "biomass_block"),
            ResourceLocation.fromNamespaceAndPath("spore", "sicken_biomass_block"),
            ResourceLocation.fromNamespaceAndPath("spore", "calcified_biomass_block"),
            ResourceLocation.fromNamespaceAndPath("spore", "membrane_block"),
            ResourceLocation.fromNamespaceAndPath("spore", "rooted_biomass"),
            ResourceLocation.fromNamespaceAndPath("spore", "gastric_biomass_block"),
            ResourceLocation.fromNamespaceAndPath("spore", "rooted_mycelium")
    );

    public FreezerBlockEntity(BlockPos pos, BlockState state) {
        super(modblocksentity.FREEZER.get(), pos, state);
        resetSnowJamTimer();
    }

    // ==========================================================
    // SISTEMA DE GUARDADO NBT - NECESARIO PARA EL RADAR DEL KOMMANDANT
    // ==========================================================
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isJammed", this.wasJammed);
        tag.putInt("ticksUntilSnowJam", this.ticksUntilSnowJam);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("isJammed")) {
            this.wasJammed = tag.getBoolean("isJammed");
        }
        if (tag.contains("ticksUntilSnowJam")) {
            this.ticksUntilSnowJam = tag.getInt("ticksUntilSnowJam");
        }
    }

    // Método getter para el Poder11 (Radar del vigil)
    public boolean isJammed() {
        return this.wasJammed;
    }
    // ==========================================================

    private void resetSnowJamTimer() {
        // Establece el temporizador entre 70 y 120 segundos
        this.ticksUntilSnowJam = (70 + this.random.nextInt(51)) * TICKS_PER_SECOND;
    }

    private static boolean isBiomass(BlockState state) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key != null && BIOMASS_BLOCKS.contains(key);
    }

    private void refillSectors(BlockPos pos) {
        sectorsToScan.clear();
        int minX = pos.getX() - CLEAN_RADIUS;
        int minY = pos.getY() - CLEAN_RADIUS;
        int minZ = pos.getZ() - CLEAN_RADIUS;
        int maxX = pos.getX() + CLEAN_RADIUS;
        int maxY = pos.getY() + CLEAN_RADIUS;
        int maxZ = pos.getZ() + CLEAN_RADIUS;

        for (int x = minX; x <= maxX; x += SECTOR_SIZE) {
            for (int y = minY; y <= maxY; y += SECTOR_SIZE) {
                for (int z = minZ; z <= maxZ; z += SECTOR_SIZE) {
                    sectorsToScan.add(new AABB(
                            x, y, z,
                            Math.min(x + SECTOR_SIZE, maxX),
                            Math.min(y + SECTOR_SIZE, maxY),
                            Math.min(z + SECTOR_SIZE, maxZ)
                    ));
                }
            }
        }
        Collections.shuffle(sectorsToScan, random);
    }

    public static void corruptMachine(Level level, BlockPos pos) {
        if (level.isClientSide) return;

        BlockPos[] baseOffsets = {
                new BlockPos(0, 0, 0), new BlockPos(1, 0, 0),
                new BlockPos(0, 0, 1), new BlockPos(1, 0, 1)
        };

        for (BlockPos offset : baseOffsets) {
            for (int y = 0; y <= 1; y++) {
                BlockPos currentPos = pos.offset(offset.getX(), offset.getY() + y, offset.getZ());
                BlockState currentBlockState = level.getBlockState(currentPos);

                if (y == 1) {
                    BlockPos abovePos = currentPos.above();
                    if (level.isEmptyBlock(abovePos) && Block.canSupportRigidBlock(level, currentPos) && level.getRandom().nextFloat() < 0.6F) {
                        ResourceLocation randomRoofLoc = ROOF_FOLIAGE_OPTIONS.get(level.getRandom().nextInt(ROOF_FOLIAGE_OPTIONS.size()));
                        Block roofFoliage = BuiltInRegistries.BLOCK.get(randomRoofLoc);
                        if (roofFoliage != null) {
                            level.setBlock(abovePos, roofFoliage.defaultBlockState(), 3);
                        }
                    }
                }

                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos neighborPos = currentPos.relative(dir);
                    boolean isPartOfStructure = false;
                    for (BlockPos innerOffset : baseOffsets) {
                        for (int innerY = 0; innerY <= 1; innerY++) {
                            if (pos.offset(innerOffset.getX(), innerY, innerOffset.getZ()).equals(neighborPos)) {
                                isPartOfStructure = true;
                                break;
                            }
                        }
                    }

                    if (!isPartOfStructure && level.isEmptyBlock(neighborPos) && currentBlockState.isSolidRender(level, currentPos)) {
                        boolean hasNearbyFoliage = false;
                        for(Direction checkDir : Direction.values()) {
                            BlockState nearbyState = level.getBlockState(neighborPos.relative(checkDir));
                            if (nearbyState.is(REMOVABLE_FOLIAGE)) {
                                hasNearbyFoliage = true;
                                break;
                            }
                        }

                        if (!hasNearbyFoliage && level.getRandom().nextFloat() < 0.6F) {
                            ResourceLocation randomWallLoc = WALL_FOLIAGE_OPTIONS.get(level.getRandom().nextInt(WALL_FOLIAGE_OPTIONS.size()));
                            Block wallFoliage = BuiltInRegistries.BLOCK.get(randomWallLoc);
                            if (wallFoliage != null) {
                                BlockState wallState = wallFoliage.defaultBlockState();
                                Property<?> property = wallState.getBlock().getStateDefinition().getProperty("facing");
                                if (property instanceof DirectionProperty directionProperty) {
                                    wallState = wallState.setValue(directionProperty, dir);
                                }
                                level.setBlock(neighborPos, wallState, 3);
                            }
                        }
                    }
                }
            }
        }
        level.playSound(null, pos, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "inf_damage")), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FreezerBlockEntity be) {
        if (level.isClientSide) return;

        be.tickCount++;

        BlockPos[] baseOffsets = {
                new BlockPos(0, 0, 0), new BlockPos(1, 0, 0),
                new BlockPos(0, 0, 1), new BlockPos(1, 0, 1)
        };

        boolean isCurrentlyJammed = false;

        for (BlockPos offset : baseOffsets) {
            for (int y = 0; y <= 1; y++) {
                BlockPos currentPos = pos.offset(offset.getX(), offset.getY() + y, offset.getZ());
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos neighborPos = currentPos.relative(dir);

                    boolean isPartOfStructure = false;
                    for (BlockPos innerOffset : baseOffsets) {
                        for (int innerY = 0; innerY <= 1; innerY++) {
                            if (pos.offset(innerOffset.getX(), innerY, innerOffset.getZ()).equals(neighborPos)) {
                                isPartOfStructure = true;
                                break;
                            }
                        }
                    }

                    if (!isPartOfStructure && !level.isEmptyBlock(neighborPos)) {
                        isCurrentlyJammed = true;
                        break;
                    }
                }
                if (isCurrentlyJammed) break;
            }
            if (isCurrentlyJammed) break;
        }

        if (!isCurrentlyJammed) {
            for (BlockPos offset : baseOffsets) {
                BlockPos base = pos.offset(offset);
                for (int y = 2; y <= 100; y++) {
                    BlockPos checkPos = base.above(y);
                    if (!level.isEmptyBlock(checkPos)) {
                        isCurrentlyJammed = true;
                        break;
                    }
                }
                if (isCurrentlyJammed) break;
            }
        }

        // Si la máquina se atasca (ya sea por tumores, bloques del jugador o nieve)
        if (isCurrentlyJammed) {
            be.wasJammed = true; // Guardamos que su funcionamiento ha sido interrumpido
            return; // No hace nada más (tampoco avanza el temporizador de nieve)
        }

        // Si llegó hasta aquí, significa que la máquina está libre y funcionando
        if (be.wasJammed) {
            be.resetSnowJamTimer();
            be.wasJammed = false;
        }

        // ==============================================================================
        // LÓGICA DE ATASCO DE NIEVE (MANTENIMIENTO ININTERRUMPIDO)
        // ==============================================================================
        be.ticksUntilSnowJam--;
        if (be.ticksUntilSnowJam <= 0) {
            be.resetSnowJamTimer();

            int blocksToPlace = 3 + level.getRandom().nextInt(5); // 3 a 7 bloques sólidos
            List<BlockPos> potentialJamSpots = new ArrayList<>();

            for (BlockPos offset : baseOffsets) {
                for (int y = 0; y <= 1; y++) {
                    BlockPos currentPos = pos.offset(offset.getX(), offset.getY() + y, offset.getZ());

                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos neighborPos = currentPos.relative(dir);
                        boolean isPartOfStructure = false;
                        for (BlockPos innerOffset : baseOffsets) {
                            for (int innerY = 0; innerY <= 1; innerY++) {
                                if (pos.offset(innerOffset.getX(), innerY, innerOffset.getZ()).equals(neighborPos)) {
                                    isPartOfStructure = true;
                                    break;
                                }
                            }
                        }
                        if (!isPartOfStructure && level.isEmptyBlock(neighborPos)) {
                            potentialJamSpots.add(neighborPos);
                        }
                    }
                    if (y == 1) {
                        BlockPos abovePos = currentPos.above();
                        if (level.isEmptyBlock(abovePos)) {
                            potentialJamSpots.add(abovePos);
                        }
                    }
                }
            }

            Collections.shuffle(potentialJamSpots, be.random);

            BlockState snowState = Blocks.SNOW_BLOCK.defaultBlockState();
            Block snowLayerBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("minecraft", "snow"));

            int placedSolid = 0;
            int placedLayers = 0;

            for (BlockPos jamPos : potentialJamSpots) {
                if (placedSolid < blocksToPlace) {
                    level.setBlock(jamPos, snowState, 3);
                    BlockPos downPos = jamPos.below();
                    while (level.isEmptyBlock(downPos) && downPos.getY() > level.getMinBuildHeight()) {
                        level.setBlock(downPos, snowState, 3);
                        downPos = downPos.below();
                    }
                    placedSolid++;
                } else if (placedLayers < 10) {
                    BlockPos dropPos = jamPos;
                    while (level.isEmptyBlock(dropPos) && dropPos.getY() > level.getMinBuildHeight()) {
                        dropPos = dropPos.below();
                    }
                    BlockPos targetPos = dropPos.above();

                    if (level.isEmptyBlock(targetPos) && Block.canSupportRigidBlock(level, dropPos)) {
                        if (snowLayerBlock != null) {
                            var layersProp = snowLayerBlock.getStateDefinition().getProperty("layers");
                            if (layersProp instanceof net.minecraft.world.level.block.state.properties.IntegerProperty intProp) {
                                int randomLayers = 1 + level.getRandom().nextInt(7);
                                BlockState snowLayerState = snowLayerBlock.defaultBlockState().setValue(intProp, randomLayers);
                                level.setBlock(targetPos, snowLayerState, 3);
                            }
                        }
                    }
                    placedLayers++;
                } else {
                    break;
                }
            }

            level.playSound(null, pos, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("minecraft", "block.snow.place")), SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }

        // ==============================================================================
        // FUNCIONAMIENTO NORMAL (Efectos y limpieza)
        // ==============================================================================

        if (be.tickCount % 10 == 0) {
            Vec3 center = Vec3.atLowerCornerOf(pos).add(1.0, 1.0, 1.0);
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.END_ROD, center.x, center.y, center.z,
                    10, 0.5, 2.0, 0.5, 0.05
            );
        }

        if (be.tickCount % 40 == 0) {
            level.playSound(
                    null, pos,
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "cdu_ambient")),
                    SoundSource.BLOCKS, 7.0F, 0.5F
            );
        }

        if (be.tickCount % (10 * TICKS_PER_SECOND) == 0) {
            AABB fullArea = new AABB(
                    pos.getX() - CLEAN_RADIUS, pos.getY() - CLEAN_RADIUS, pos.getZ() - CLEAN_RADIUS,
                    pos.getX() + CLEAN_RADIUS, pos.getY() + CLEAN_RADIUS, pos.getZ() + CLEAN_RADIUS
            );

            Vec3 centerPos = Vec3.atCenterOf(pos);

            Holder<MobEffect> frostbite = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "frostbite")).orElse(null);

            if (frostbite != null) {
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, fullArea)) {
                    if (entity.distanceToSqr(centerPos) > RADIUS_SQR) continue;
                    if (entity.getTeam() == null || !"spore".equalsIgnoreCase(entity.getTeam().getName())) continue;

                    entity.setTicksFrozen(entity.getTicksFrozen() + 100);

                    MobEffectInstance current = entity.getEffect(frostbite);
                    int amplifier = current == null ? 0 : current.getAmplifier() + 1;

                    if (entity instanceof Player) {
                        amplifier = Math.min(4, amplifier);
                    }

                    entity.addEffect(new MobEffectInstance(frostbite, 300, amplifier, false, true));
                }
            }
        }

        if (be.sectorsToScan.isEmpty()) {
            be.refillSectors(pos);
        }

        AABB currentSector = be.sectorsToScan.remove(be.sectorsToScan.size() - 1);

        for (BlockPos bp : BlockPos.betweenClosed(
                new BlockPos((int) currentSector.minX, (int) currentSector.minY, (int) currentSector.minZ),
                new BlockPos((int) currentSector.maxX, (int) currentSector.maxY, (int) currentSector.maxZ))) {

            if (bp.distSqr(pos) > RADIUS_SQR) continue;

            BlockState bs = level.getBlockState(bp);

            if (bs.is(REMOVABLE_FOLIAGE) && level.getRandom().nextFloat() < 0.2F) {
                level.removeBlock(bp, false);
            }

            ResourceLocation remainsKey = BuiltInRegistries.BLOCK.getKey(bs.getBlock());
            if (remainsKey != null && remainsKey.getPath().equals("remains")) {
                Block frozenRemains = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(remainsKey.getNamespace(), "frozen_remains"));
                if (frozenRemains != null) {
                    level.setBlock(bp, frozenRemains.defaultBlockState(), 3);
                }
            }

            if (bs.is(INFECTED_BIOMASS) && level.getRandom().nextFloat() < 0.2F) {
                level.removeBlock(bp, false);
            }

            if (isBiomass(bs) && level.getRandom().nextFloat() < 0.1F) {
                Block freezeBurned = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("spore", "freeze_burned_biomass"));
                if (freezeBurned != null) {
                    level.setBlock(bp, freezeBurned.defaultBlockState(), 3);
                }
            }

            ResourceLocation bileKey = BuiltInRegistries.BLOCK.getKey(bs.getBlock());
            if (bileKey != null && bileKey.getPath().equals("bile") && level.getRandom().nextFloat() < 0.1F) {
                Block crustedBile = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(bileKey.getNamespace(), "crusted_bile"));
                if (crustedBile != null) {
                    level.setBlock(bp, crustedBile.defaultBlockState(), 3);
                }
            }

            ResourceLocation myceliumKey = BuiltInRegistries.BLOCK.getKey(bs.getBlock());
            if (myceliumKey != null && myceliumKey.equals(ResourceLocation.fromNamespaceAndPath("minecraft", "mycelium")) && level.getRandom().nextFloat() < 0.15F) {
                Block dirt = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("minecraft", "dirt"));
                if (dirt != null) {
                    level.setBlock(bp, dirt.defaultBlockState(), 3);
                }
            }

            if (level.getRandom().nextFloat() < 0.001F) {
                BlockState above = level.getBlockState(bp.above());
                if (bs.isSolidRender(level, bp) && above.isAir()) {
                    int layers = 1 + level.getRandom().nextInt(3);
                    Block snow = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("minecraft", "snow"));
                    if (snow != null) {
                        var layersProp = snow.getStateDefinition().getProperty("layers");
                        if (layersProp instanceof net.minecraft.world.level.block.state.properties.IntegerProperty intProp) {
                            BlockState snowStateLayers = snow.defaultBlockState().setValue(intProp, layers);
                            level.setBlock(bp.above(), snowStateLayers, 3);
                        }
                    }
                }
            }
        }

        if (be.tickCount > 1_000_000) {
            be.tickCount = 0;
        }
    }
}