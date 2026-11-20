package com.sporeadds.sporeaddsmod.Powers;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.FoliageSpread;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.ConfigEffects;
import com.sporeadds.sporeaddsmod.config.KommandantAdvancedConfig;
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
import net.neoforged.neoforge.common.NeoForge;
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import net.neoforged.bus.api.SubscribeEvent;
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
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
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
            var famined = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "famined")).orElse(null);
            if (famined != null && player.hasEffect(famined)) {
                return;
            }

            executionCooldown.put(playerId, currentTime);
            Poder13Variants.activategluttonous(level, player);
            return;
        }

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(sporeData -> {
            int currentSpore = sporeData.getSpore();
            int cost = KommandantAdvancedConfig.NUKE_BIOMASS_COST.get();

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

            BlockPos spawnPos = player.blockPosition().above(KommandantAdvancedConfig.NUKE_SPAWN_HEIGHT.get());
            var nukeEntityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("spore", "tumoroid_nuke"));
            if (nukeEntityType == null) return;

            Entity nukeEntity = nukeEntityType.create(level);
            if (nukeEntity == null) return;

            // The detonation is driven by DelayedDetonation (below); the entity's own timer is only a safety net that
            // must outlast it, otherwise Spore's own nuke blast would fire first (300 ticks = the original value).
            int detonationDelay = KommandantAdvancedConfig.NUKE_DETONATION_DELAY_TICKS.get();

            CompoundTag tag = new CompoundTag();
            tag.putInt("timer", Math.max(300, detonationDelay + 100));
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
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "falling_bomb")),
                        SoundSource.MASTER,
                        7.0F, 1.0F
                );

                // Resistance (to survive the blast) and Anticipation by default; the list is configurable.
                ConfigEffects.apply(player, KommandantAdvancedConfig.effects(KommandantAdvancedConfig.NUKE_PLAYER_EFFECTS));

                new DelayedDetonation(level, nukeEntity, player, detonationDelay, hasCaustic);
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
            NeoForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {
            ticks++;
            if (ticks >= delay) {
                detonate(level, nukeEntity, player, isCaustic);
                NeoForge.EVENT_BUS.unregister(this);
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
        // Configurable (power_13_nuke.cloud); read when the cloud is created, i.e. at runtime.
        private final int MAX_CYCLES = KommandantAdvancedConfig.NUKE_CLOUD_SPREAD_CYCLES.get();
        private final int TICKS_BETWEEN_SPREADS = KommandantAdvancedConfig.NUKE_CLOUD_SPREAD_INTERVAL_TICKS.get();
        private final int MAX_LIFETIME = KommandantAdvancedConfig.NUKE_CLOUD_LIFETIME_TICKS.get();
        private final float START_RADIUS = KommandantAdvancedConfig.NUKE_CLOUD_START_RADIUS.get().floatValue();
        private final float SHRINK_BLOCKS = KommandantAdvancedConfig.NUKE_CLOUD_SHRINK.get().floatValue();

        public NukeFalloutSpreader(ServerLevel level, BlockPos center, double x, double y, double z, boolean isCaustic) {
            this.level = level;
            this.center = center;
            this.x = x;
            this.y = y;
            this.z = z;
            this.isCaustic = isCaustic;
            NeoForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {
            ticksElapsed++;
            totalTicks++;

            float currentRadius = START_RADIUS - ((float) totalTicks / MAX_LIFETIME) * SHRINK_BLOCKS;

            if (ticksElapsed >= TICKS_BETWEEN_SPREADS) {
                ticksElapsed = 0;
                spreadCycles++;

                if (spreadCycles <= MAX_CYCLES) {
                    if (isCaustic) {
                        Block acidBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("spore", "acid"));

                        if (acidBlock != null && acidBlock != net.minecraft.world.level.block.Blocks.AIR) {
                            int r = (int) currentRadius;
                            int attempts = (int) (Math.PI * r * r * KommandantAdvancedConfig.NUKE_CLOUD_ACID_DENSITY.get());

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

                AABB cloudArea = new AABB(x, y, z, x, y, z).inflate(currentRadius, KommandantAdvancedConfig.NUKE_CLOUD_HEIGHT.get(), currentRadius);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, cloudArea, LivingEntity::isAlive);
                int cloudEffectTicks = KommandantAdvancedConfig.NUKE_CLOUD_EFFECT_TICKS.get();
                List<ConfigEffects.Entry> cloudExtraEffects = KommandantAdvancedConfig.effects(KommandantAdvancedConfig.NUKE_CLOUD_EXTRA_EFFECTS);

                var exposed = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "exposed")).orElse(null);
                var dissolution = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "dissolution")).orElse(null);
                var mycelium = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "mycelium_ef")).orElse(null);
                var corrosion = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "corrosion")).orElse(null);

                int exposedLevel = SporeAddsConfig.NUKE_EXPOSED_LEVEL.get();
                int myceliumLevel = SporeAddsConfig.NUKE_MYCELIUM_LEVEL.get();

                for (LivingEntity target : targets) {
                    Team team = target.getTeam();
                    if (team == null || !team.getName().equals("spore")) {
                        double dx = target.getX() - x;
                        double dz = target.getZ() - z;
                        if (dx * dx + dz * dz <= currentRadius * currentRadius) {
                            if (isCaustic) {
                                if (dissolution != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(dissolution, cloudEffectTicks, 3));
                                if (mycelium != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(mycelium, cloudEffectTicks, myceliumLevel));
                                if (corrosion != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(corrosion, cloudEffectTicks, myceliumLevel));
                            } else {
                                if (exposed != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(exposed, cloudEffectTicks, exposedLevel));
                                if (mycelium != null) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(mycelium, cloudEffectTicks, myceliumLevel));
                            }
                            ConfigEffects.apply(target, cloudExtraEffects);
                        }
                    }
                }
            }

            if (totalTicks >= MAX_LIFETIME) {
                NeoForge.EVENT_BUS.unregister(this);
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
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "nuke")),
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
                KommandantAdvancedConfig.NUKE_EXPLOSION_RADIUS.get().floatValue(),
                KommandantAdvancedConfig.NUKE_EXPLOSION_FIRE.get(),
                KommandantAdvancedConfig.NUKE_EXPLOSION_BREAKS_BLOCKS.get()
                        ? Level.ExplosionInteraction.TNT
                        : Level.ExplosionInteraction.NONE
        );

        player.sendSystemMessage(Component.translatable("message.sporeadd.power13.nuke_detonated").withStyle(ChatFormatting.DARK_RED));

        double damageRadius = KommandantAdvancedConfig.NUKE_DAMAGE_RADIUS.get();
        AABB effectArea = new AABB(ex, ey, ez, ex, ey, ez).inflate(damageRadius);
        List<LivingEntity> affected = level.getEntitiesOfClass(LivingEntity.class, effectArea, LivingEntity::isAlive);

        var mycelium = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "mycelium_ef")).orElse(null);
        var termina = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "termina")).orElse(null);
        var dissolution = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadd", "dissolution")).orElse(null);
        var corrosion = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "corrosion")).orElse(null);

        int minTermina = SporeAddsConfig.TERMINA_DURATION_MIN.get();
        int maxTermina = SporeAddsConfig.TERMINA_DURATION_MAX.get();
        if (minTermina > maxTermina) {
            int temp = minTermina;
            minTermina = maxTermina;
            maxTermina = temp;
        }

        int terminaLevel = SporeAddsConfig.NUKE_TERMINA_LEVEL.get();
        int myceliumLevel = SporeAddsConfig.NUKE_MYCELIUM_LEVEL.get();

        // power_13_nuke.blast: effect duration, damage, knockback and extra effects (defaults = the old hard-coded values)
        int blastEffectTicks = KommandantAdvancedConfig.NUKE_BLAST_EFFECT_TICKS.get();
        float blastDamage = KommandantAdvancedConfig.NUKE_DAMAGE.get().floatValue();
        double knockbackHorizontal = KommandantAdvancedConfig.NUKE_KNOCKBACK_HORIZONTAL.get();
        double knockbackVertical = KommandantAdvancedConfig.NUKE_KNOCKBACK_VERTICAL.get();
        List<ConfigEffects.Entry> blastExtraEffects = KommandantAdvancedConfig.effects(KommandantAdvancedConfig.NUKE_BLAST_EXTRA_EFFECTS);

        for (LivingEntity entity : affected) {
            Team team = entity.getTeam();
            if (team == null || !team.getName().equals("spore")) {

                if (mycelium != null) {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(mycelium, blastEffectTicks, myceliumLevel, false, true));
                }

                if (isCaustic) {
                    if (dissolution != null) {
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(dissolution, blastEffectTicks, 1, false, true));
                    }
                    if (corrosion != null) {
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(corrosion, blastEffectTicks, myceliumLevel, false, true));
                    }
                } else {
                    if (termina != null) {
                        int terminaTicks = minTermina + RANDOM.nextInt((maxTermina - minTermina) + 1);
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(termina, terminaTicks, terminaLevel, false, true));
                    }
                }

                ConfigEffects.apply(entity, blastExtraEffects);

                entity.hurt(level.damageSources().explosion(null), blastDamage);

                double dx = entity.getX() - ex;
                double dy = entity.getY() - ey;
                double dz = entity.getZ() - ez;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz) + 0.1;
                entity.push(dx / dist * knockbackHorizontal, dy / dist * knockbackVertical, dz / dist * knockbackHorizontal);
            }
        }

        new NukeFalloutSpreader(level, detonationPos, ex, ey, ez, isCaustic);
        nukeEntity.discard();

        int basePenalty = SporeAddsConfig.NUKE_LEVEL_PENALTY.get();
        int levelsToLose = Poder12Variants.isgluttonous(player) ? (basePenalty / 2) : basePenalty;

        if (levelsToLose > 0) {
            PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelData -> {
                int newLevel = Math.max(0, levelData.getLevel() - levelsToLose);
                levelData.setLevel(newLevel);
            });
            player.sendSystemMessage(Component.translatable("message.sporeadd.power13.grow_weaker").withStyle(ChatFormatting.RED));
        }
    }
}