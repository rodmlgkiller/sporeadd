package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.event.LockedItemHandler;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncAbyssalStormPacket;
import com.sporeadds.sporeaddsmod.network.SyncGasSpheresPacket;
import com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.common.NeoForge;
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import net.neoforged.bus.api.SubscribeEvent;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Poder13Variants {

    private static final Random RANDOM = new Random();
    private static final int ABYSSAL_DURATION = 3600;

    // Pure pacing delay before the gas cloud starts, independent of the mound's own (unmodified,
    // vanilla) emerge animation - lets the shake/dig sound play out for a bit first.
    private static final int CAUSTIC_EMERGE_TICKS = 100;
    private static final float CAUSTIC_EMERGE_SHAKE_RADIUS = 32.0F;
    private static final float CAUSTIC_EMERGE_SHAKE_INTENSITY = 3.0F;

    public static boolean isAbyssal(Player player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            return "abyssal".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    public static void activateAbyssal(ServerLevel level, Player player) {
        level.setWeatherParameters(0, ABYSSAL_DURATION, true, true);

        ItemStack trident = new ItemStack(Items.TRIDENT);
        Enchantment riptide = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.fromNamespaceAndPath("minecraft", "riptide"));
        Enchantment vanishing = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.fromNamespaceAndPath("minecraft", "vanishing_curse"));

        if (riptide != null) trident.enchant(riptide, 5);
        if (vanishing != null) trident.enchant(vanishing, 1);

        ItemNbt.getOrCreateTag(trident).putBoolean("Unbreakable", true);
        ItemNbt.getOrCreateTag(trident).putBoolean("AbyssalTempTrident", true);

        LockedItemHandler.giveTemporaryLockedItem(player, trident, ABYSSAL_DURATION);

        var subjugation = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "subjugation")).orElse(null);
        if (subjugation != null) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    subjugation,
                    ABYSSAL_DURATION,
                    0,
                    false,
                    false,
                    true
            ));
        }

        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("minecraft", "entity.lightning_bolt.thunder")),
                SoundSource.PLAYERS,
                1.0F, 1.0F
        );

        player.sendSystemMessage(Component.literal("§bThe ocean resonates..."));

        new AbyssalStormTracker(level, player, ABYSSAL_DURATION);
    }

    public static class AbyssalStormTracker {
        private static final List<AbyssalStormTracker> ACTIVE = new ArrayList<>();

        private final ServerLevel level;
        private final Player player;
        private final int maxTicks;
        private int ticksElapsed = 0;
        private int nextLightningStrike = 0;

        public AbyssalStormTracker(ServerLevel level, Player player, int duration) {
            this.level = level;
            this.player = player;
            this.maxTicks = duration;
            this.nextLightningStrike = getRandomLightningDelay();
            ACTIVE.add(this);
            NeoForge.EVENT_BUS.register(this);
        }

        private int getRandomLightningDelay() {
            return 40 + RANDOM.nextInt(121);
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {

            if (!player.isAlive() || player.level() != level) {
                applyPenalty();
                finish();
                return;
            }

            ticksElapsed++;

            if (ticksElapsed >= nextLightningStrike) {
                strikeLightningOnTargets();
                nextLightningStrike = ticksElapsed + getRandomLightningDelay();
            }

            if (ticksElapsed % 60 == 0) {
                spawnAbyssalAllies();
            }

            if (ticksElapsed % 2 == 0) {
                spawnRainParticles();
            }

            if (ticksElapsed % 20 == 0) {
                applyRainExposureEffect();
                broadcastStorms();
            }

            if (ticksElapsed >= maxTicks) {
                applyPenalty();
                finish();
            }
        }

        private void finish() {
            ACTIVE.remove(this);
            NeoForge.EVENT_BUS.unregister(this);
            broadcastStorms();
        }

        /** Broadcasts every active Abyssal storm's current anchor position, used client-side to hide vanilla rain nearby. */
        private static void broadcastStorms() {
            List<SyncAbyssalStormPacket.Storm> storms = new ArrayList<>();
            for (AbyssalStormTracker tracker : ACTIVE) {
                storms.add(new SyncAbyssalStormPacket.Storm(tracker.player.getX(), tracker.player.getY(), tracker.player.getZ()));
            }
            NetworkHandle.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncAbyssalStormPacket(storms));
        }

        /**
         * Reskins the storm's rain with the custom "rain" particle around the player, since vanilla's
         * own rain rendering can't be selectively retextured per-ability without a client render Mixin.
         */
        private void spawnRainParticles() {
            var rain = SporeaddParticleTypes.RAIN.get();
            double centerX = player.getX();
            double centerZ = player.getZ();

            for (int i = 0; i < 12; i++) {
                double px = centerX + (RANDOM.nextDouble() - 0.5) * 80.0;
                double pz = centerZ + (RANDOM.nextDouble() - 0.5) * 80.0;
                int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) px, (int) pz);
                double py = Math.max(topY, player.getY()) + 15.0 + RANDOM.nextDouble() * 10.0;

                if (!level.canSeeSky(new BlockPos((int) px, (int) py, (int) pz))) continue;

                level.sendParticles(rain, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        /**
         * Entities exposed to the open sky (not sheltered under a roof) while the storm rages get
         * inflicted with mycelium infection, mirroring the mycelium spores carried by the abyssal rain.
         */
        private void applyRainExposureEffect() {
            AABB searchArea = player.getBoundingBox().inflate(200.0);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, searchArea, LivingEntity::isAlive);

            var mycelium = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "mycelium_ef")).orElse(null);
            if (mycelium == null) return;

            for (LivingEntity entity : nearby) {
                Team team = entity.getTeam();
                if (team != null && team.getName().equals("spore")) continue;

                if (!level.canSeeSky(entity.blockPosition())) continue;

                entity.addEffect(new MobEffectInstance(mycelium, 600, 5, false, true, true));
            }
        }

        private void strikeLightningOnTargets() {
            AABB searchArea = player.getBoundingBox().inflate(15.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchArea, e -> {
                if (e == player || !e.isAlive()) return false;
                Team team = e.getTeam();
                return team == null || !team.getName().equals("spore");
            });

            if (targets.isEmpty()) return;

            targets.sort((e1, e2) -> {
                boolean e1Prio = (e1 instanceof Player) || e1.getType() == EntityType.VILLAGER;
                boolean e2Prio = (e2 instanceof Player) || e2.getType() == EntityType.VILLAGER;
                return Boolean.compare(e2Prio, e1Prio);
            });

            LivingEntity target = targets.get(0);

            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            if (lightning != null) {
                lightning.moveTo(target.getX(), target.getY(), target.getZ());
                lightning.setVisualOnly(false);
                lightning.setDamage(50.0F);
                level.addFreshEntity(lightning);
            }
        }

        private void spawnAbyssalAllies() {
            BlockPos playerPos = player.blockPosition();
            int r = 50;

            BlockPos spawnPos = null;
            for (int i = 0; i < 30; i++) {
                int dx = RANDOM.nextInt(r * 2 + 1) - r;
                int dy = RANDOM.nextInt(31) - 15;
                int dz = RANDOM.nextInt(r * 2 + 1) - r;

                BlockPos checkPos = playerPos.offset(dx, dy, dz);
                if (level.getBlockState(checkPos).is(Blocks.WATER) && level.getBlockState(checkPos.above()).is(Blocks.WATER)) {
                    if (level.getMaxLocalRawBrightness(checkPos) != 0) {
                        spawnPos = checkPos;
                        break;
                    }
                }
            }

            if (spawnPos != null) {
                String[] mobs = {"spore:inf_drowned", "spore:naiad", "spore:bloater"};
                String selectedMob = mobs[RANDOM.nextInt(mobs.length)];

                var resistance = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("minecraft", "resistance")).orElse(null);
                var speed = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("minecraft", "speed")).orElse(null);
                var strength = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("minecraft", "strength")).orElse(null);
                var marker = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "marker")).orElse(null);

                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(selectedMob));
                if (type != null) {
                    Entity entity = type.spawn(level, spawnPos, MobSpawnType.COMMAND);
                    if (entity instanceof LivingEntity living) {
                        int buffDuration = 600;
                        if (resistance != null) living.addEffect(new net.minecraft.world.effect.MobEffectInstance(resistance, buffDuration, 3));
                        if (speed != null) living.addEffect(new net.minecraft.world.effect.MobEffectInstance(speed, buffDuration, 0));
                        if (strength != null) living.addEffect(new net.minecraft.world.effect.MobEffectInstance(strength, buffDuration, 0));
                        if (marker != null) living.addEffect(new net.minecraft.world.effect.MobEffectInstance(marker, buffDuration, 4));
                    }
                }
            }
        }

        private void applyPenalty() {
            int levelsToLose = SporeAddsConfig.NUKE_LEVEL_PENALTY.get() / 2;

            if (levelsToLose > 0 && player.isAlive()) {
                PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelData -> {
                    int newLevel = Math.max(0, levelData.getLevel() - levelsToLose);
                    levelData.setLevel(newLevel);
                });
                player.sendSystemMessage(Component.translatable("message.sporeadd.power13.grow_weaker").withStyle(ChatFormatting.RED));
            }
        }
    }

    public static boolean isgluttonous(Player player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            return "gluttonous".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    public static void activategluttonous(ServerLevel level, Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            var playerDataCap = com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider.PLAYER_DATA.get(serverPlayer);
            if (playerDataCap.isPresent() && playerDataCap.orElseThrow(IllegalStateException::new).getArmorHp() < 1) {
                return;
            }
        }

        var famined = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "famined")).orElse(null);
        if (famined != null && player.hasEffect(famined)) {
            return;
        }

        if (famined != null) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    famined,
                    2000,
                    0,
                    false,
                    true,
                    true
            ));
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider.PLAYER_DATA.get(serverPlayer).ifPresent(data -> {
                int currentArmor = data.getArmorHp();
                int maxArmor = Poder12Variants.getEffectiveArmorCap(serverPlayer);
                int newArmor = Math.min(maxArmor, currentArmor + 1);

                if (newArmor != currentArmor) {
                    data.setArmorHpAndSync(newArmor, serverPlayer);
                }
            });
        }

        var weakness = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("minecraft", "weakness")).orElse(null);
        var calamityIncoming = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "calamity_incoming"));

        AABB area = player.getBoundingBox().inflate(100.0D);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, area, p -> p.isAlive());

        for (Player nearby : nearbyPlayers) {
            if (weakness != null) {
                nearby.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        weakness,
                        200,
                        0,
                        false,
                        true,
                        true
                ));
            }

            if (calamityIncoming != null) {
                level.playSound(
                        null,
                        nearby.getX(),
                        nearby.getY(),
                        nearby.getZ(),
                        calamityIncoming,
                        SoundSource.MASTER,
                        8.0F,
                        1.0F
                );
            }

            nearby.sendSystemMessage(
                    Component.translatable("message.sporeadd.power13.gluttonous_incoming")
                            .withStyle(ChatFormatting.GOLD)
            );
        }
    }

    public static void activateCaustic(ServerLevel level, Player player) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer)) return;

        EntityType<?> moundType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("spore", "mound"));
        if (moundType == null) return;

        Entity raw = moundType.create(level);
        if (!(raw instanceof net.minecraft.world.entity.Mob mound)) return;

        BlockPos spawnPos = player.blockPosition();
        mound.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), player.getXRot());

        var difficulty = level.getCurrentDifficultyAt(spawnPos);
        mound.finalizeSpawn(level, difficulty, MobSpawnType.MOB_SUMMONED, null, null);

        java.util.UUID ownerUUID = player.getUUID();
        java.util.UUID moundUUID = mound.getUUID();

        // "spore_owner" / nohardfloor are our own persistent-data flags. Deliberately NOT tagged "core":
        // that flag (and the respawn-point registration below it) is exclusive to actual core mounds
        // (Poder3) - this mound is just the anchor for the gas dome, never a respawn point.
        mound.getPersistentData().putUUID("spore_owner", ownerUUID);
        mound.getPersistentData().putBoolean("SporeAdds_NoHardFloorDespawn", true);
        mound.getPersistentData().putString("SporeAdds_SpawnSource", "CausticGasMound");

        mound.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 3, false, false));

        // "linked"/"age"/"max_age" are the mound entity's own native save fields, not persistent data
        // (getPersistentData() is serialized separately under its own "ForgeData" tag and never read back
        // by the mound's own class) - same technique MoundTerrariumBlock uses to make a mound spawn linked.
        net.minecraft.nbt.CompoundTag moundTag = mound.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        moundTag.putBoolean("linked", true);
        moundTag.putInt("age", 4);
        moundTag.putInt("max_age", 4);
        mound.load(moundTag);

        // Must happen AFTER the age/linked reload above: Mound.onSyncedDataUpdated recalculates and
        // overwrites max health whenever its native "age" field changes, so setting health first would
        // just get clobbered by that reload.
        net.minecraft.world.entity.ai.attributes.AttributeInstance maxHealthAttribute =
                mound.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(200.0D);
        }
        mound.setHealth((float) mound.getMaxHealth());

        ScaleData moundScale = ScaleTypes.BASE.getScaleData(mound);
        moundScale.setScale(1.2F);
        moundScale.setTargetScale(1.2F);

        var server = level.getServer();
        if (server != null) {
            var scoreboard = server.getScoreboard();
            net.minecraft.world.scores.PlayerTeam team = scoreboard.getPlayerTeam("spore");
            if (team == null) {
                team = scoreboard.addPlayerTeam("spore");
                team.setColor(ChatFormatting.RED);
            }
            scoreboard.addPlayerToTeam(mound.getScoreboardName(), team);
        }

        level.addFreshEntity(mound);
        broadcastEmergeShake(level, mound, CAUSTIC_EMERGE_TICKS);

        level.playSound(
                null,
                mound.getX(), mound.getY(), mound.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "worm_digging")),
                net.minecraft.sounds.SoundSource.HOSTILE,
                CAUSTIC_EMERGE_SHAKE_RADIUS / 16.0F,
                1.0F
        );

        net.minecraft.world.level.ChunkPos chunkPos = new net.minecraft.world.level.ChunkPos(mound.blockPosition());
        level.setChunkForced(chunkPos.x, chunkPos.z, true);

        player.sendSystemMessage(
                Component.literal("The air starts to smell like sulfur").withStyle(ChatFormatting.GREEN)
        );

        int levelsToLose = SporeAddsConfig.NUKE_LEVEL_PENALTY.get();
        if (levelsToLose > 0) {
            PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelData ->
                    levelData.setLevel(Math.max(0, levelData.getLevel() - levelsToLose)));
            player.sendSystemMessage(Component.translatable("message.sporeadd.power13.grow_weaker").withStyle(ChatFormatting.RED));
        }

        new CausticGasBombTracker(level, mound);
    }

    /**
     * Screen shake for nearby players while the caustic mound unearths itself, matching the rumble
     * spore:hohlfresser causes when it burrows.
     */
    private static void broadcastEmergeShake(ServerLevel level, Entity source, int durationTicks) {
        AABB area = source.getBoundingBox().inflate(CAUSTIC_EMERGE_SHAKE_RADIUS);
        List<net.minecraft.server.level.ServerPlayer> nearby =
                level.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class, area, p -> true);

        for (net.minecraft.server.level.ServerPlayer nearbyPlayer : nearby) {
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> nearbyPlayer),
                    new com.sporeadds.sporeaddsmod.network.TriggerCameraShakePacket(durationTicks, CAUSTIC_EMERGE_SHAKE_INTENSITY)
            );
        }
    }

    /**
     * Drives the gas cloud around the caustic mound: grows a gas sphere to a 50 block radius over
     * 10 seconds, then shrinks it 1 block every 5 seconds until it fades out. This mound is NOT a
     * respawn point (unlike Poder3's core mound) - it's purely the anchor for the gas dome, and is
     * never removed by this tracker; only the temporary gas effect around it ends.
     */
    public static class CausticGasBombTracker {
        private static final List<CausticGasBombTracker> ACTIVE = new ArrayList<>();

        private static final int GROW_DURATION_TICKS = 400;
        private static final float MAX_RADIUS = 50.0F;
        private static final int SHRINK_TICKS_PER_BLOCK = 100;
        private static final int ACID_POOL_INTERVAL_TICKS = 200;

        // If the mound dies before the dome fades out on its own, the dome is kept alive (anchored to
        // the mound's last known position) but shrinks this many times faster than its normal rate.
        private static final float DEATH_SHRINK_MULTIPLIER = 5.0F;

        private static final float GROWTH_PER_TICK = MAX_RADIUS / GROW_DURATION_TICKS;
        private static final float SHRINK_PER_TICK = 1.0F / SHRINK_TICKS_PER_BLOCK;

        private final ServerLevel level;
        private final net.minecraft.world.entity.Mob mound;
        private int ticksElapsed = 0;
        private float radius = 0.0F;
        private boolean growthDone = false;
        private boolean moundDead = false;
        private double lastX;
        private double lastY;
        private double lastZ;

        public CausticGasBombTracker(ServerLevel level, net.minecraft.world.entity.Mob mound) {
            this.level = level;
            this.mound = mound;
            this.lastX = mound.getX();
            this.lastY = mound.getY();
            this.lastZ = mound.getZ();
            ACTIVE.add(this);
            NeoForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {

            if (mound.isAlive() && mound.level() == level) {
                lastX = mound.getX();
                lastY = mound.getY();
                lastZ = mound.getZ();
            } else if (!moundDead) {
                // The mound is gone - keep the dome alive at its last known position, but force it
                // straight into (a faster) shrink phase instead of continuing to grow/hold.
                moundDead = true;
                growthDone = true;
            }

            if (!moundDead && mound.tickCount < CAUSTIC_EMERGE_TICKS) {
                // Still unearthing - don't start the gas cloud until the mound has fully surfaced.
                return;
            }

            ticksElapsed++;

            if (!growthDone) {
                radius = Math.min(MAX_RADIUS, radius + GROWTH_PER_TICK);
                if (radius >= MAX_RADIUS) {
                    growthDone = true;
                }
            } else {
                float shrinkPerTick = SHRINK_PER_TICK * (moundDead ? DEATH_SHRINK_MULTIPLIER : 1.0F);
                radius = Math.max(0.0F, radius - shrinkPerTick);

                if (radius <= 0.0F) {
                    finish();
                    return;
                }
            }

            spawnGeyserParticles();
            spawnEdgeParticles();

            if (ticksElapsed % 4 == 0) {
                fillGasSphere();
            }

            if (ticksElapsed % 10 == 0) {
                affectNearbyEntities();
                broadcastSpheres();
            }

            if (ticksElapsed % ACID_POOL_INTERVAL_TICKS == 0) {
                spreadAcidPools();
            }
        }

        private void finish() {
            ACTIVE.remove(this);
            NeoForge.EVENT_BUS.unregister(this);
            broadcastSpheres();
        }

        private void spawnGeyserParticles() {
            float intensity = MAX_RADIUS > 0.0F ? Math.min(1.0F, radius / MAX_RADIUS) : 0.0F;
            int count = Math.round(6 * intensity);
            if (count <= 0) return;

            var gas = SporeaddParticleTypes.GAS.get();
            for (int i = 0; i < count; i++) {
                double angle = RANDOM.nextDouble() * Math.PI * 2;
                double horizontalSpeed = RANDOM.nextDouble() * 0.18 * intensity;
                double dx = Math.cos(angle) * horizontalSpeed;
                double dz = Math.sin(angle) * horizontalSpeed;
                double dy = (0.12 + RANDOM.nextDouble() * 0.35) * (0.4F + 0.6F * intensity);

                double px = lastX + (RANDOM.nextDouble() - 0.5) * 1.2;
                double pz = lastZ + (RANDOM.nextDouble() - 0.5) * 1.2;

                level.sendParticles(gas, px, lastY + 0.3, pz, 1, dx, dy, dz, 0.03);
            }
        }

        private void spawnEdgeParticles() {
            if (radius < 2.0F) return;

            var gas = SporeaddParticleTypes.GAS.get();
            int count = (int) (radius * 1.2F);

            for (int i = 0; i < count; i++) {
                double theta = Math.acos(2 * RANDOM.nextDouble() - 1);
                double phi = RANDOM.nextDouble() * Math.PI * 2;
                float r = Math.max(0.0F, radius + (RANDOM.nextFloat() - 0.5F) * 3.0F);

                double px = lastX + r * Math.sin(theta) * Math.cos(phi);
                double py = lastY + r * Math.cos(theta) * 0.5 + 1.0;
                double pz = lastZ + r * Math.sin(theta) * Math.sin(phi);

                level.sendParticles(gas, px, py, pz, 1, 0.0, 0.01, 0.0, 0.01);
            }
        }

        /**
         * Same acid-pool technique the old caustic Tumoroid Nuke used (see Poder13.NukeFalloutSpreader):
         * pick random points inside the circle, scan each column top-down for the first solid surface
         * with a replaceable block above it, and place spore:acid there.
         */
        private void spreadAcidPools() {
            if (radius < 3.0F) return;

            Block acidBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("spore", "acid"));
            if (acidBlock == null || acidBlock == net.minecraft.world.level.block.Blocks.AIR) return;

            BlockPos center = BlockPos.containing(lastX, lastY, lastZ);
            int r = (int) radius;
            int attempts = (int) (Math.PI * r * r * 0.10);

            for (int i = 0; i < attempts; i++) {
                int dx = RANDOM.nextInt(r * 2 + 1) - r;
                int dz = RANDOM.nextInt(r * 2 + 1) - r;

                if (dx * dx + dz * dz > r * r) continue;

                for (int dy = 15; dy >= -15; dy--) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState currentState = level.getBlockState(pos);
                    BlockPos posAbove = pos.above();
                    BlockState stateAbove = level.getBlockState(posAbove);

                    if (currentState.isSolidRender(level, pos) && stateAbove.canBeReplaced()) {
                        level.setBlock(posAbove, acidBlock.defaultBlockState(), 3);
                        break;
                    }
                }
            }
        }

        private void fillGasSphere() {
            if (radius < 1.0F) return;

            var gas = SporeaddParticleTypes.GAS.get();
            int count = (int) (radius * 1.5F);

            for (int i = 0; i < count; i++) {
                double theta = RANDOM.nextDouble() * Math.PI * 2;
                double phi = Math.acos(2 * RANDOM.nextDouble() - 1);
                double r = radius * Math.cbrt(RANDOM.nextDouble());

                double px = lastX + r * Math.sin(phi) * Math.cos(theta);
                double py = lastY + r * Math.cos(phi) * 0.5 + 1.0;
                double pz = lastZ + r * Math.sin(phi) * Math.sin(theta);

                level.sendParticles(gas, px, py, pz, 1, 0.0, 0.0, 0.0, 0.01);
            }
        }

        private void affectNearbyEntities() {
            AABB area = new AABB(lastX, lastY, lastZ, lastX, lastY, lastZ)
                    .inflate(radius, 10.0, radius);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive);

            var dissolution = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "dissolution")).orElse(null);
            var corrosion = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "corrosion")).orElse(null);

            for (LivingEntity entity : nearby) {
                double dx = entity.getX() - lastX;
                double dz = entity.getZ() - lastZ;
                if (dx * dx + dz * dz > (double) radius * radius) continue;

                if (shouldBlind(entity)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, true, true));
                }

                if (dissolution != null) {
                    entity.addEffect(new MobEffectInstance(dissolution, 600, 1, false, true, true));
                }
                if (corrosion != null) {
                    entity.addEffect(new MobEffectInstance(corrosion, 600, 5, false, true, true));
                }
            }
        }

        private boolean shouldBlind(LivingEntity entity) {
            Team team = entity.getTeam();
            boolean onSporeTeam = team != null && "spore".equals(team.getName());
            boolean isGhostPlayer = entity instanceof Player p && SporeClassUtil.hasClass(p, "ghost");
            return !onSporeTeam || isGhostPlayer;
        }

        private void broadcastSpheres() {
            List<SyncGasSpheresPacket.Sphere> spheres = new ArrayList<>();
            for (CausticGasBombTracker tracker : ACTIVE) {
                spheres.add(new SyncGasSpheresPacket.Sphere(
                        tracker.mound.getId(), tracker.lastX, tracker.lastY, tracker.lastZ, tracker.radius
                ));
            }
            NetworkHandle.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncGasSpheresPacket(spheres));
        }
    }
}