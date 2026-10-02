package com.sporeadds.sporeaddsmod.Powers;

import com.Harbinger.Spore.Sentities.FoliageSpread;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Poder13 {

    private static final FoliageSpread FOLIAGE_SPREAD = new FoliageSpread() {};
    private static final Random RANDOM = new Random();
    private static final Map<UUID, Long> executionCooldown = new ConcurrentHashMap<>();

    static boolean isCaustic(Player player) {
        LazyOptional<SporeIdentifierData> cap = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER);
        if (cap.isPresent()) {
            return "caustic".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        }
        return false;
    }

    public static void activate(ServerLevel level, Player player) {
        if (level.isClientSide()) return;

        UUID playerId = player.getUUID();
        long currentTime = level.getGameTime();

        if (executionCooldown.containsKey(playerId) && currentTime - executionCooldown.get(playerId) < 10) {
            return;
        }

        if (Poder13Variants.isgluttonous(player)) {
            var famined = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sporeadd", "famined"));
            if (famined != null && player.hasEffect(famined)) {
                return;
            }

            executionCooldown.put(playerId, currentTime);
            Poder13Variants.activategluttonous(level, player);
            return;
        }

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeData -> {
            int currentSpore = sporeData.getSpore();
            int cost = 20;

            if (currentSpore < cost) {
                player.sendSystemMessage(Component.translatable("message.sporeadd.power1.not_enough_biomass").withStyle(ChatFormatting.DARK_RED));
                executionCooldown.put(playerId, currentTime);
                return;
            }

            executionCooldown.put(playerId, currentTime);
            sporeData.setSpore(currentSpore - cost);

            if (Poder13Variants.isAbyssal(player)) {
                Poder13Variants.activateAbyssal(level, player);
                return;
            }

            if (isCaustic(player)) {
                Poder13Variants.activateCaustic(level, player);
                return;
            }

            BlockPos spawnPos = player.blockPosition().above(50);
            var nukeEntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("spore", "tumoroid_nuke"));
            if (nukeEntityType == null) return;

            Entity nukeEntity = nukeEntityType.create(level);
            if (nukeEntity == null) return;

            CompoundTag tag = new CompoundTag();
            tag.putInt("timer", 300);
            tag.putByte("overclocked", (byte) 1);
            nukeEntity.load(tag);
            nukeEntity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0f, 0f);

            boolean success = level.addFreshEntity(nukeEntity);

            if (success) {
                boolean hasCaustic = isCaustic(player);

                player.sendSystemMessage(
                        Component.translatable("message.sporeadd.power13.nuke_incoming")
                                .withStyle(hasCaustic ? ChatFormatting.GREEN : ChatFormatting.DARK_RED)
                );

                level.playSound(
                        null,
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "falling_bomb")),
                        SoundSource.MASTER,
                        7.0F, 1.0F
                );

                var resistance = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("minecraft", "resistance"));
                if (resistance != null) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(resistance, 300, 224, false, true));
                }

                var anticipation = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sporeadd", "anticipation"));
                if (anticipation != null) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            anticipation,
                            200,
                            0,
                            false,
                            true,
                            true
                    ));
                }

                new DelayedDetonation(level, nukeEntity, player, 200, hasCaustic);
            }
        });
    }

    public static class DelayedDetonation {
        private int ticks = 0;
        private final int delay;
        private final ServerLevel level;
        private final Entity nukeEntity;
        private final Player player;
        private final boolean isCaustic;

        public DelayedDetonation(ServerLevel level, Entity nukeEntity, Player player, int delay, boolean isCaustic) {
            this.level = level;
            this.nukeEntity = nukeEntity;
            this.player = player;
            this.delay = delay;
            this.isCaustic = isCaustic;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            ticks++;
            if (ticks >= delay) {
                detonate(level, nukeEntity, player, isCaustic);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    public static class NukeFalloutSpreader {
        private final ServerLevel level;
        private final BlockPos center;
        private final double x, y, z;
        private final boolean isCaustic;

        private int ticksElapsed = 0;
        private int totalTicks = 0;
        private int spreadCycles = 0;
        private final int MAX_CYCLES = 4;
        private final int TICKS_BETWEEN_SPREADS = 200;
        private final int MAX_LIFETIME = 1200;

        public NukeFalloutSpreader(ServerLevel level, BlockPos center, double x, double y, double z, boolean isCaustic) {
            this.level = level;
            this.center = center;
            this.x = x;
            this.y = y;
            this.z = z;
            this.isCaustic = isCaustic;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            ticksElapsed++;
            totalTicks++;

            float currentRadius = 60.0F - ((float) totalTicks / MAX_LIFETIME) * 12.0F;

            if (ticksElapsed >= TICKS_BETWEEN_SPREADS) {
                ticksElapsed = 0;
                spreadCycles++;

                if (spreadCycles <= MAX_CYCLES) {
                    if (isCaustic) {
                        Block acidBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("spore", "acid"));

                        if (acidBlock != null && acidBlock != net.minecraft.world.level.block.Blocks.AIR) {
                            int r = (int) currentRadius;
                            int attempts = (int) (Math.PI * r * r * 0.10);

                            for (int i = 0; i < attempts; i++) {
                                int dx = RANDOM.nextInt(r * 2 + 1) - r;
                                int dz = RANDOM.nextInt(r * 2 + 1) - r;

                                if (dx * dx + dz * dz <= r * r) {
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
                        }
                    } else {
                        FOLIAGE_SPREAD.SpreadInfection(level, currentRadius, center);
                    }
                }
            }

            if (totalTicks % 10 == 0) {
                int particleCount = (int) (currentRadius * 5);
                DustParticleOptions particleOpt = isCaustic
                        ? new DustParticleOptions(new Vector3f(0.0F, 1.0F, 0.0F), 1.5F)
                        : new DustParticleOptions(new Vector3f(0.8F, 0.07F, 0.07F), 1.5F);

                for (int i = 0; i < particleCount; i++) {
                    double px = x + (RANDOM.nextDouble() * 2 - 1) * currentRadius;
                    double py = y + (RANDOM.nextDouble() * 5);
                    double pz = z + (RANDOM.nextDouble() * 2 - 1) * currentRadius;
                    level.sendParticles(particleOpt, px, py, pz, 1, 0, 0, 0, 0);
                }

                AABB cloudArea = new AABB(x, y, z, x, y, z).inflate(currentRadius, 10.0, currentRadius);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, cloudArea, LivingEntity::isAlive);

                var exposed = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sporeadd", "exposed"));
                var dissolution = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sporeadd", "dissolution"));
                var mycelium = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "mycelium_ef"));
                var corrosion = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "corrosion"));

                int exposedLevel = SporeAddsConfig.NUKE_EXPOSED_LEVEL.get();
                int myceliumLevel = SporeAddsConfig.NUKE_MYCELIUM_LEVEL.get();

                for (LivingEntity target : targets) {
                    Team team = target.getTeam();
                    if (team == null || !team.getName().equals("spore")) {
                        double dx = target.getX() - x;
                        double dz = target.getZ() - z;
                        if (dx * dx + dz * dz <= currentRadius * currentRadius) {
                            if (isCaustic) {
                                if (dissolution != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(dissolution, 1000, 3));
                                if (mycelium != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(mycelium, 1000, myceliumLevel));
                                if (corrosion != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(corrosion, 1000, myceliumLevel));
                            } else {
                                if (exposed != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(exposed, 1000, exposedLevel));
                                if (mycelium != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(mycelium, 1000, myceliumLevel));
                            }
                        }
                    }
                }
            }

            if (totalTicks >= MAX_LIFETIME) {
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    static void detonate(ServerLevel level, Entity nukeEntity, Player player, boolean isCaustic) {
        if (!nukeEntity.isAlive()) return;

        double ex = nukeEntity.getX();
        double ey = nukeEntity.getY();
        double ez = nukeEntity.getZ();
        BlockPos detonationPos = new BlockPos((int) ex, (int) ey, (int) ez);

        level.playSound(
                null, ex, ey, ez,
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "nuke")),
                SoundSource.MASTER,
                50.0F, 1.0F
        );
        level.playSound(
                null, ex, ey, ez,
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                SoundSource.MASTER,
                22.0F, 1.0F
        );

        level.explode(
                nukeEntity,
                ex, ey, ez,
                24f,
                true,
                Level.ExplosionInteraction.TNT
        );

        player.sendSystemMessage(Component.translatable("message.sporeadd.power13.nuke_detonated").withStyle(ChatFormatting.DARK_RED));

        double damageRadius = 100.0;
        AABB effectArea = new AABB(ex, ey, ez, ex, ey, ez).inflate(damageRadius);
        List<LivingEntity> affected = level.getEntitiesOfClass(LivingEntity.class, effectArea, LivingEntity::isAlive);

        var mycelium = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "mycelium_ef"));
        var termina = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sporeadd", "termina"));
        var dissolution = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sporeadd", "dissolution"));
        var corrosion = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "corrosion"));

        int minTermina = SporeAddsConfig.TERMINA_DURATION_MIN.get();
        int maxTermina = SporeAddsConfig.TERMINA_DURATION_MAX.get();
        if (minTermina > maxTermina) {
            int temp = minTermina;
            minTermina = maxTermina;
            maxTermina = temp;
        }

        int terminaLevel = SporeAddsConfig.NUKE_TERMINA_LEVEL.get();
        int myceliumLevel = SporeAddsConfig.NUKE_MYCELIUM_LEVEL.get();

        for (LivingEntity entity : affected) {
            Team team = entity.getTeam();
            if (team == null || !team.getName().equals("spore")) {

                if (mycelium != null) {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(mycelium, 6000, myceliumLevel, false, true));
                }

                if (isCaustic) {
                    if (dissolution != null) {
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(dissolution, 6000, 1, false, true));
                    }
                    if (corrosion != null) {
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(corrosion, 6000, myceliumLevel, false, true));
                    }
                } else {
                    if (termina != null) {
                        int terminaTicks = minTermina + RANDOM.nextInt((maxTermina - minTermina) + 1);
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(termina, terminaTicks, terminaLevel, false, true));
                    }
                }

                entity.hurt(level.damageSources().explosion(null), 350.0F);

                double dx = entity.getX() - ex;
                double dy = entity.getY() - ey;
                double dz = entity.getZ() - ez;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz) + 0.1;
                entity.push(dx / dist * 4, dy / dist * 2, dz / dist * 4);
            }
        }

        new NukeFalloutSpreader(level, detonationPos, ex, ey, ez, isCaustic);
        nukeEntity.discard();

        int basePenalty = SporeAddsConfig.NUKE_LEVEL_PENALTY.get();
        int levelsToLose = Poder12Variants.isgluttonous(player) ? (basePenalty / 2) : basePenalty;

        if (levelsToLose > 0) {
            player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelData -> {
                int newLevel = Math.max(0, levelData.getLevel() - levelsToLose);
                levelData.setLevel(newLevel);
            });
            player.sendSystemMessage(Component.translatable("message.sporeadd.power13.grow_weaker").withStyle(ChatFormatting.RED));
        }
    }
}