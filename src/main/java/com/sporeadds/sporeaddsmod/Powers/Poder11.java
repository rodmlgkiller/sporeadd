package com.sporeadds.sporeaddsmod.Powers;

import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.FreezerBlockEntity;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.VigilRadarPacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;
import com.Harbinger.Spore.Sentities.Organoids.Proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Poder11 extends PowerBase {
    private static final int PHASE_COST = 20;
    private static final int REQUIRED_LEVEL_USE = 7;
    private static final int REQUIRED_LEVEL_VARIANT1 = 7;

    private static final int INITIAL_SCAN_RADIUS = 15;
    private static final int SCAN_INTERVAL = 200;
    private static final int SCAN_GROWTH = 15;
    private static final int MAX_SCAN_RADIUS = 150;

    private static final int JAM_TIME_TICKS = 500;

    private static final ResourceLocation FREEZER_BLOCK_ID = new ResourceLocation("sporeadd", "freezer_block");
    private static final ResourceLocation CDU_BLOCK_ID = new ResourceLocation("spore", "cdu");

    private static final String JAMMER_TAG = "SporeAdds_IsJammer";
    private static final String JAMMER_TIMER_TAG = "JammingTimer";
    private static final String MACHINE_X_TAG = "SporeAdds_MachineX";
    private static final String MACHINE_Y_TAG = "SporeAdds_MachineY";
    private static final String MACHINE_Z_TAG = "SporeAdds_MachineZ";
    private static final String SPAWN_SOURCE_TAG = "SporeAdds_SpawnSource";
    private static final String PODER11_MOUND_SOURCE = "Poder11JammingMound";

    private static final Map<UUID, BlockPos> jammingMounds = new HashMap<>();

    private static boolean isOwnedByPoder11(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.getBoolean(JAMMER_TAG) && PODER11_MOUND_SOURCE.equals(data.getString(SPAWN_SOURCE_TAG));
    }

    private static int getProtoCountAcrossDimensions(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return 0;

        int count = 0;
        for (ServerLevel serverLevel : server.getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof Proto) {
                    count++;
                }
            }
        }
        return count;
    }

    public void use(ServerPlayer player) {
        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            int currentPhase = spore.getSpore();
            if (currentPhase < PHASE_COST) {
                player.displayClientMessage(
                        Component.translatable("message.sporeadd.power11.no_biomass")
                                .withStyle(ChatFormatting.DARK_RED),
                        true
                );
                return;
            }

            player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(lvl -> {
                if (lvl.getLevel() < REQUIRED_LEVEL_USE) return;
                spore.setSpore(currentPhase - PHASE_COST);

                ServerLevel level = player.serverLevel();
                int protoCount = getProtoCountAcrossDimensions(player);

                double alcance = 150.0D;
                Vec3 startVec = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle();
                Vec3 endVec = startVec.add(lookVec.x * alcance, lookVec.y * alcance, lookVec.z * alcance);

                BlockHitResult hitResult = level.clip(new ClipContext(
                        startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
                ));

                BlockPos spawnPos;
                if (hitResult.getType() == BlockHitResult.Type.BLOCK) {
                    spawnPos = hitResult.getBlockPos().above();
                } else {
                    spawnPos = BlockPos.containing(endVec.x, endVec.y, endVec.z);
                }

                int maxDrops = 150;
                while (spawnPos.getY() > level.getMinBuildHeight() && level.isEmptyBlock(spawnPos.below()) && maxDrops > 0) {
                    spawnPos = spawnPos.below();
                    maxDrops--;
                }

                if (spawnPos.getY() <= level.getMinBuildHeight()) {
                    return;
                }

                int variant = (lvl.getLevel() >= REQUIRED_LEVEL_VARIANT1) ? 1 : 0;
                EntityType<?> vigilType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("spore", "vigil"));
                if (vigilType == null) return;

                Entity spawnedEntity = vigilType.create(level);
                if (spawnedEntity instanceof Mob vigil) {
                    vigil.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
                    vigil.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null, null);

                    CompoundTag nbt = new CompoundTag();
                    nbt.putInt("Variant", variant);
                    nbt.putInt("timer", 40);
                    vigil.load(nbt);

                    vigil.teleportTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);

                    vigil.setYRot(player.getYRot());
                    vigil.setXRot(player.getXRot());
                    vigil.setYHeadRot(player.getYRot());
                    vigil.setYBodyRot(player.getYRot());
                    vigil.setDeltaMovement(Vec3.ZERO);

                    if (vigil.getAttribute(Attributes.MAX_HEALTH) != null) {
                        vigil.getAttribute(Attributes.MAX_HEALTH).setBaseValue(120.0D);
                    }
                    if (vigil.getAttribute(Attributes.ARMOR) != null) {
                        vigil.getAttribute(Attributes.ARMOR).setBaseValue(10.0D);
                    }
                    vigil.setHealth(vigil.getMaxHealth());

                    vigil.addEffect(new MobEffectInstance(
                            MobEffects.REGENERATION,
                            Integer.MAX_VALUE,
                            1,
                            false,
                            false,
                            false
                    ));

                    double bonusHealth = protoCount * 50.0;
                    double baseHealth = vigil.getMaxHealth();
                    if (vigil.getAttribute(Attributes.MAX_HEALTH) != null) {
                        vigil.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth + bonusHealth);
                    }
                    vigil.setHealth((float) (baseHealth + bonusHealth));

                    float finalScale = 1.2F + (0.2F * protoCount);
                    ScaleData scaleData = ScaleTypes.BASE.getScaleData(vigil);
                    scaleData.setScale(finalScale);
                    scaleData.setTargetScale(finalScale);

                    vigil.setCustomName(Component.translatable("entity.sporeadd.kommandant_vigil"));
                    vigil.setCustomNameVisible(true);

                    CompoundTag persistentData = vigil.getPersistentData();
                    persistentData.putBoolean("SporeAdds_IsRadarVigil", true);
                    persistentData.putInt("SporeAdds_ScanTimer", 0);
                    persistentData.putInt("SporeAdds_ScanRadius", INITIAL_SCAN_RADIUS);
                    persistentData.putInt("SporeAdds_ParticleWave", -1);
                    persistentData.putBoolean("SporeAdds_NoHardFloorDespawn", true);
                    persistentData.putString("SporeAdds_SpawnSource", "Poder11Vigil");

                    level.addFreshEntity(vigil);

                    player.displayClientMessage(
                            Component.translatable(
                                    "message.sporeadd.power11.vigil_called",
                                    (int) (baseHealth + bonusHealth),
                                    finalScale
                            ).withStyle(ChatFormatting.DARK_RED),
                            true
                    );
                }
            });
        });
    }

    private static boolean isMachineBeingJammed(ServerLevel level, BlockPos machinePos) {
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(machinePos).inflate(8.0D),
                e -> e.isAlive() && e.getPersistentData().getBoolean(JAMMER_TAG)
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
    public static void onVigilTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        CompoundTag persistentData = entity.getPersistentData();
        if (!persistentData.getBoolean("SporeAdds_IsRadarVigil")) return;

        int timer = persistentData.getInt("SporeAdds_ScanTimer") + 1;

        if (timer >= SCAN_INTERVAL) {
            timer = 0;
            ServerLevel level = (ServerLevel) entity.level();
            int currentRadius = persistentData.getInt("SporeAdds_ScanRadius");

            SoundEvent sonarSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "sonar"));
            if (sonarSound != null) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sonarSound, SoundSource.HOSTILE, 12.5F, 1.0F);
            }

            runKommandantScan(entity, level, currentRadius);

            MobEffect markerEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "marker"));
            AABB scanBox = entity.getBoundingBox().inflate(currentRadius);

            List<LivingEntity> targets = level.getEntitiesOfClass(
                    LivingEntity.class, scanBox, e -> (e instanceof Player || e instanceof Villager)
            );

            for (LivingEntity target : targets) {
                if (target.distanceTo(entity) <= currentRadius) {
                    if (target instanceof ServerPlayer playerTarget && SporeClassUtil.hasClass(playerTarget, "kommandant")) {
                        continue;
                    }
                    if (markerEffect != null) {
                        target.addEffect(new MobEffectInstance(markerEffect, 300, 1, false, true, true));
                    }
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 1, false, false, true));
                }
            }

            int nextRadius = Math.min(currentRadius + SCAN_GROWTH, MAX_SCAN_RADIUS);
            persistentData.putInt("SporeAdds_ScanRadius", nextRadius);
            persistentData.putInt("SporeAdds_ParticleWave", 0);
        }

        persistentData.putInt("SporeAdds_ScanTimer", timer);

        int waveTick = persistentData.getInt("SporeAdds_ParticleWave");
        if (waveTick >= 0 && waveTick <= 10) {
            ServerLevel level = (ServerLevel) entity.level();
            double radius = waveTick;
            DustParticleOptions particle = new DustParticleOptions(new Vector3f(0.8F, 0.05F, 0.05F), 1.5F);
            int numParticles = Math.max(15, (int) (radius * 12));

            for (int i = 0; i < numParticles; i++) {
                double angle = 2 * Math.PI * i / numParticles;
                double px = entity.getX() + radius * Math.cos(angle);
                double pz = entity.getZ() + radius * Math.sin(angle);
                level.sendParticles(particle, px, entity.getY() + 0.2, pz, 1, 0, 0, 0, 0);
            }
            persistentData.putInt("SporeAdds_ParticleWave", waveTick + 1);
        } else if (waveTick > 10) {
            persistentData.putInt("SporeAdds_ParticleWave", -1);
        }
    }

    @SubscribeEvent
    public static void onMoundTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!isOwnedByPoder11(entity)) return;

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

        if (machinePos == null) return;

        int ticks = data.getInt(JAMMER_TIMER_TAG) + 1;

        if (ticks >= JAM_TIME_TICKS) {
            ServerLevel level = (ServerLevel) entity.level();
            jamMachine(level, machinePos);
            ticks = 0;
        }

        data.putInt(JAMMER_TIMER_TAG, ticks);

        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
    }

    @SubscribeEvent
    public static void onMoundDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!isOwnedByPoder11(entity)) return;

        if (event.getSource().is(DamageTypes.FREEZE) || event.getSource().is(DamageTypes.IN_WALL)) {
            event.setCanceled(true);

            if (event.getSource().is(DamageTypes.FREEZE) && entity.getTicksFrozen() > 0) {
                entity.setTicksFrozen(0);
            }
        }
    }

    @SubscribeEvent
    public static void onMoundDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!isOwnedByPoder11(entity)) return;

        CompoundTag data = entity.getPersistentData();
        jammingMounds.remove(entity.getUUID());
        data.remove(JAMMER_TIMER_TAG);
        data.remove(MACHINE_X_TAG);
        data.remove(MACHINE_Y_TAG);
        data.remove(MACHINE_Z_TAG);
    }

    private static void jamMachine(ServerLevel level, BlockPos machinePos) {
        BlockEntity be = level.getBlockEntity(machinePos);
        if (be == null) return;

        if (be instanceof FreezerBlockEntity freezer) {
            FreezerBlockEntity.corruptMachine(level, machinePos);
            freezer.setChanged();

            BlockState state = level.getBlockState(machinePos);
            level.sendBlockUpdated(machinePos, state, state, 3);
        } else if (be instanceof CDUBlockEntity cdu) {
            cdu.setFuel(0);
            BlockState state = level.getBlockState(machinePos);

            Property<?> litProp = state.getBlock().getStateDefinition().getProperty("lit");
            if (litProp instanceof BooleanProperty bProp) {
                level.setBlock(machinePos, state.setValue(bProp, true), 3);
            }

            cdu.setChanged();
            BlockState newState = level.getBlockState(machinePos);
            level.sendBlockUpdated(machinePos, newState, newState, 3);
        }
    }

    private static void runKommandantScan(LivingEntity vigil, ServerLevel level, int scanRadius) {
        ServerPlayer kommandant = getKommandantPlayer(level);
        if (kommandant == null) return;

        BlockPos center = vigil.blockPosition();
        AABB scanBox = new AABB(center).inflate(scanRadius);

        int villagerCount = level.getEntitiesOfClass(Villager.class, scanBox).size();
        int playerCount = (int) level.getEntitiesOfClass(ServerPlayer.class, scanBox).stream()
                .filter(p -> !SporeClassUtil.hasClass(p, "kommandant"))
                .count();

        List<BlockPos> freezerPos = new ArrayList<>();
        List<Integer> freezerStatus = new ArrayList<>();
        List<Boolean> freezerJamming = new ArrayList<>();

        List<BlockPos> cduPos = new ArrayList<>();
        List<Integer> cduStatus = new ArrayList<>();
        List<Boolean> cduJamming = new ArrayList<>();

        scanForCryoUnitsFast(level, center, scanRadius, freezerPos, freezerStatus, freezerJamming, cduPos, cduStatus, cduJamming);

        for (int i = 0; i < freezerPos.size(); i++) {
            BlockPos pos = freezerPos.get(i);
            boolean jamming = freezerJamming.get(i);

            if (!jamming && level.getRandom().nextFloat() <= 0.30f) {
                trySpawnJammingMound(level, pos, true);
                freezerJamming.set(i, true);
            }
        }

        for (int i = 0; i < cduPos.size(); i++) {
            BlockPos pos = cduPos.get(i);
            boolean jamming = cduJamming.get(i);

            if (!jamming && level.getRandom().nextFloat() <= 0.30f) {
                trySpawnJammingMound(level, pos, false);
                cduJamming.set(i, true);
            }
        }

        List<Component> reportLines = new ArrayList<>();

        reportLines.add(Component.translatable("message.sporeadd.power11.scan.title").withStyle(ChatFormatting.BOLD));
        reportLines.add(Component.translatable("message.sporeadd.power11.scan.radius", scanRadius).withStyle(ChatFormatting.GOLD));
        reportLines.add(Component.translatable("message.sporeadd.power11.scan.humanoids", villagerCount));
        reportLines.add(Component.translatable("message.sporeadd.power11.scan.humans", playerCount));
        reportLines.add(Component.translatable("message.sporeadd.power11.scan.cryo_units"));
        reportLines.add(Component.empty());

        reportLines.add(Component.translatable("message.sporeadd.power11.scan.freezers").withStyle(ChatFormatting.AQUA));
        if (freezerPos.isEmpty()) {
            reportLines.add(Component.translatable("message.sporeadd.power11.scan.none").withStyle(ChatFormatting.GRAY));
        } else {
            for (int i = 0; i < freezerPos.size(); i++) {
                BlockPos p = freezerPos.get(i);
                int status = freezerStatus.get(i);
                boolean jamming = freezerJamming.get(i);

                MutableComponent statusStr;
                ChatFormatting color;

                if (jamming && status == 0) {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.jammed_mound");
                    color = ChatFormatting.DARK_RED;
                } else if (jamming) {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.jamming");
                    color = ChatFormatting.GOLD;
                } else if (status == 0) {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.jammed");
                    color = ChatFormatting.DARK_RED;
                } else if (status == 1) {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.disabled");
                    color = ChatFormatting.RED;
                } else {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.active");
                    color = ChatFormatting.GREEN;
                }

                reportLines.add(Component.translatable("message.sporeadd.power11.scan.machine_format", p.getX(), p.getY(), p.getZ(), statusStr).withStyle(color));
            }
        }

        reportLines.add(Component.empty());
        reportLines.add(Component.translatable("message.sporeadd.power11.scan.cdus").withStyle(ChatFormatting.YELLOW));

        if (cduPos.isEmpty()) {
            reportLines.add(Component.translatable("message.sporeadd.power11.scan.none").withStyle(ChatFormatting.GRAY));
        } else {
            for (int i = 0; i < cduPos.size(); i++) {
                BlockPos p = cduPos.get(i);
                int status = cduStatus.get(i);
                boolean jamming = cduJamming.get(i);

                MutableComponent statusStr;
                ChatFormatting color;

                if (jamming && status == 0) {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.jammed_mound");
                    color = ChatFormatting.DARK_RED;
                } else if (jamming) {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.jamming");
                    color = ChatFormatting.GOLD;
                } else if (status == 0) {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.jammed");
                    color = ChatFormatting.DARK_RED;
                } else if (status == 1) {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.disabled");
                    color = ChatFormatting.RED;
                } else {
                    statusStr = Component.translatable("message.sporeadd.power11.scan.status.active");
                    color = ChatFormatting.GREEN;
                }

                reportLines.add(Component.translatable("message.sporeadd.power11.scan.machine_format", p.getX(), p.getY(), p.getZ(), statusStr).withStyle(color));
            }
        }

        NetworkHandle.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> kommandant),
                new VigilRadarPacket(reportLines)
        );
    }

    private static ServerPlayer getKommandantPlayer(ServerLevel level) {
        return level.players().stream()
                .filter(p -> SporeClassUtil.hasClass(p, "kommandant"))
                .findFirst()
                .orElse(null);
    }

    private static void scanForCryoUnitsFast(
            ServerLevel level,
            BlockPos center,
            int radius,
            List<BlockPos> freezerPos,
            List<Integer> freezerStatus,
            List<Boolean> freezerJamming,
            List<BlockPos> cduPos,
            List<Integer> cduStatus,
            List<Boolean> cduJamming
    ) {
        Block freezerBlock = ForgeRegistries.BLOCKS.getValue(FREEZER_BLOCK_ID);
        Block cduBlock = ForgeRegistries.BLOCKS.getValue(CDU_BLOCK_ID);

        double radiusSqr = (double) radius * radius;

        int chunkMinX = (center.getX() - radius) >> 4;
        int chunkMaxX = (center.getX() + radius) >> 4;
        int chunkMinZ = (center.getZ() - radius) >> 4;
        int chunkMaxZ = (center.getZ() + radius) >> 4;

        for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
            for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
                if (!level.hasChunk(cx, cz)) continue;

                LevelChunk chunk = level.getChunk(cx, cz);
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (pos.distSqr(center) > radiusSqr) continue;

                    BlockEntity be = entry.getValue();
                    Block block = be.getBlockState().getBlock();

                    if (freezerBlock != null && block == freezerBlock) {
                        freezerPos.add(pos.immutable());

                        boolean isBeingJammed = isMachineBeingJammed(level, pos);
                        freezerJamming.add(isBeingJammed);

                        int status = 2;
                        if (be instanceof FreezerBlockEntity customFreezer) {
                            if (customFreezer.isJammed()) {
                                status = 0;
                            }
                        }
                        freezerStatus.add(status);

                    } else if (cduBlock != null && block == cduBlock) {
                        cduPos.add(pos.immutable());

                        boolean isBeingJammed = isMachineBeingJammed(level, pos);
                        cduJamming.add(isBeingJammed);

                        int status = 1;

                        boolean isLit = false;
                        Property<?> litProp = block.getStateDefinition().getProperty("lit");
                        if (litProp instanceof BooleanProperty bProp) {
                            isLit = be.getBlockState().getValue(bProp);
                        }

                        if (isLit) {
                            status = 0;
                        } else if (be instanceof CDUBlockEntity cduEntity) {
                            if (cduEntity.isRunning()) {
                                status = 2;
                            }
                        }

                        cduStatus.add(status);
                    }
                }
            }
        }
    }

    private static void trySpawnJammingMound(ServerLevel level, BlockPos machinePos, boolean isFreezer) {
        EntityType<?> moundType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("spore", "mound"));
        if (moundType == null) return;

        if (isMachineBeingJammed(level, machinePos)) return;

        BlockPos spawnPos = machinePos.above();
        double spawnX = machinePos.getX() + 0.5D;
        double spawnY = spawnPos.getY();
        double spawnZ = machinePos.getZ() + 0.5D;

        if (isFreezer) {
            spawnPos = machinePos.above(2);
            spawnX = machinePos.getX() + 1.0D;
            spawnY = spawnPos.getY();
            spawnZ = machinePos.getZ() + 1.0D;

            if (!level.getBlockState(spawnPos).isAir()) return;
        } else {
            if (!level.getBlockState(spawnPos).isAir()) return;
        }

        Entity e = moundType.create(level);
        if (!(e instanceof Mob mound)) return;

        mound.moveTo(spawnX, spawnY, spawnZ, 0.0F, 0.0F);

        mound.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.MOB_SUMMONED,
                null,
                null
        );

        mound.setPersistenceRequired();
        mound.setCustomName(Component.translatable("entity.sporeadd.jamming_mound"));
        mound.setCustomNameVisible(false);

        if (mound.getAttribute(Attributes.MAX_HEALTH) != null) {
            mound.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0D);
        }
        if (mound.getAttribute(Attributes.ARMOR) != null) {
            mound.getAttribute(Attributes.ARMOR).setBaseValue(30.0D);
        }
        mound.setHealth(Math.min(mound.getMaxHealth(), 10.0F));

        CompoundTag data = mound.getPersistentData();
        data.putInt(JAMMER_TIMER_TAG, 0);
        data.putBoolean(JAMMER_TAG, true);
        data.putInt(MACHINE_X_TAG, machinePos.getX());
        data.putInt(MACHINE_Y_TAG, machinePos.getY());
        data.putInt(MACHINE_Z_TAG, machinePos.getZ());
        data.putBoolean("SporeAdds_NoHardFloorDespawn", true);
        data.putString("SporeAdds_SpawnSource", "Poder11JammingMound");

        level.addFreshEntity(mound);
        jammingMounds.put(mound.getUUID(), machinePos);
    }

    @SubscribeEvent
    public static void onVigilDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        CompoundTag persistentData = entity.getPersistentData();
        if (!persistentData.getBoolean("SporeAdds_IsRadarVigil")) return;

        if (event.getSource().is(DamageTypes.FREEZE)) {
            float originalDamage = event.getAmount();
            float reducedDamage = originalDamage * 0.30f;
            event.setAmount(reducedDamage);

            if (entity.getTicksFrozen() > 100) {
                entity.setTicksFrozen(100);
            }
        }
    }
}