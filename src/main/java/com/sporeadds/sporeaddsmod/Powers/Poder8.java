package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.core.Sentities;
import com.Harbinger.Spore.core.Ssounds;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import com.Harbinger.Spore.Sentities.Variants.NaiadVariants;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevel;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSpore;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.NonNullConsumer;
import net.minecraft.core.particles.ParticleTypes;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Poder8 extends PowerBase {

    private static final int MIN_LEVEL = 4;
    private static final int PHASE_COST = 15;
    private static final String ANTIFARM_TAG = "antifarm";
    private static final Random random = new Random();

    public void use(ServerPlayer player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(idData -> {
            boolean isAbyssal = "kommandant".equalsIgnoreCase(idData.getIdentifier())
                    && "abyssal".equalsIgnoreCase(idData.getSubclass());

            PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
                int currentPhase = spore.getSpore();

                PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(new NonNullConsumer<PlayerLevel>() {
                    @Override
                    public void accept(PlayerLevel lvl) {
                        if (lvl == null) return;

                        int playerLevel = lvl.getLevel();
                        if (playerLevel < MIN_LEVEL) return;

                        if (isAbyssal) {
                            executeAbyssalLogic(player, serverLevel, spore, currentPhase, playerLevel);
                        } else {
                            executeNormalLogic(player, serverLevel, spore, currentPhase, playerLevel);
                        }
                    }
                });
            });
        });
    }

    private void executeAbyssalLogic(ServerPlayer player, ServerLevel level, PlayerSpore spore, int currentPhase, int playerLevel) {
        if (currentPhase < PHASE_COST) return;

        BlockPos spawnPos = findAbyssalSpawnPos(player, level);
        if (spawnPos == null) return;

        spore.setSpore(currentPhase - PHASE_COST);
        spawnSonicBoomHeadParticles(level, player);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ELDER_GUARDIAN_CURSE,
                SoundSource.PLAYERS,
                2.0f,
                0.5f
        );

        EntityType<?> naiadType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("spore", "naiad"));
        if (naiadType == null) return;

        MobEffect marker = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("spore", "marker"));

        int protoCount = getProtoCountAcrossDimensions(player);
        int totalNaiads = 2 + (protoCount * 2);

        for (int i = 0; i < totalNaiads; i++) {
            Entity entity = naiadType.create(level);
            if (entity instanceof com.Harbinger.Spore.Sentities.EvolvedInfected.Naiad naiad) {
                naiad.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0, 0);
                naiad.setVariant(NaiadVariants.TRITON.getId());
                ScaleData scaleData = ScaleTypes.BASE.getScaleData(naiad);
                scaleData.setScale(1.3F);

                AttributeInstance healthAttr = naiad.getAttribute(Attributes.MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(getAbyssalNaiadHealth(playerLevel));
                    naiad.setHealth(naiad.getMaxHealth());
                }

                naiad.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 2, false, false, true));

                if (marker != null) {
                    naiad.addEffect(new MobEffectInstance(marker, 2000, 5, false, false, true));
                }

                level.addFreshEntity(naiad);
            }
        }
    }

    private void spawnSonicBoomHeadParticles(ServerLevel level, ServerPlayer player) {
        double x = player.getX();
        double y = player.getY() + player.getEyeHeight() * 0.95D;
        double z = player.getZ();

        level.sendParticles(ParticleTypes.SONIC_BOOM, x, y, z, 8, 0.15D, 0.12D, 0.15D, 0.0D);
    }

    private BlockPos findAbyssalSpawnPos(ServerPlayer player, ServerLevel level) {
        BlockPos base = player.blockPosition();

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                BlockPos columnBase = base.offset(dx, 0, dz);
                if (!level.isLoaded(columnBase)) continue;

                int startY = Math.min(base.getY(), level.getMaxBuildHeight() - 2);
                int minY = Math.max(level.getMinBuildHeight(), base.getY() - 50);

                for (int y = startY; y >= minY; y--) {
                    BlockPos pos = new BlockPos(columnBase.getX(), y, columnBase.getZ());
                    if (!level.isLoaded(pos)) continue;

                    if (!level.getFluidState(pos).is(FluidTags.WATER)) {
                        continue;
                    }

                    if (level.getMaxLocalRawBrightness(pos) != 0) {
                        continue;
                    }

                    if (!hasClearVerticalPathFromPlayer(level, player, pos)) {
                        continue;
                    }

                    BlockPos below = pos.below();
                    if (!level.getBlockState(below).isSolidRender(level, below) || level.getFluidState(below).is(FluidTags.WATER)) {
                        continue;
                    }

                    return pos;
                }
            }
        }

        return null;
    }

    private boolean hasClearVerticalPathFromPlayer(ServerLevel level, ServerPlayer player, BlockPos target) {
        int topY = Math.min(player.blockPosition().getY(), target.getY());
        int bottomY = Math.max(player.blockPosition().getY(), target.getY());

        for (int y = topY; y <= bottomY; y++) {
            BlockPos pos = new BlockPos(target.getX(), y, target.getZ());
            if (y == target.getY()) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isSolidRender(level, pos)) {
                return false;
            }

            if (!state.getFluidState().is(FluidTags.WATER) && !state.isAir()) {
                return false;
            }
        }

        return true;
    }

    private double getAbyssalNaiadHealth(int level) {
        switch (level) {
            case 0: return 20.0;
            case 1: return 25.0;
            case 2: return 30.0;
            case 3: return 40.0;
            case 4: return 50.0;
            case 5: return 65.0;
            case 6: return 80.0;
            case 7: return 100.0;
            case 8: return 120.0;
            case 9: return 150.0;
            default:
                if (level > 9) return 200.0;
                return 20.0;
        }
    }

    private void executeNormalLogic(ServerPlayer player, ServerLevel level, PlayerSpore spore, int currentPhase, int playerLevel) {
        if (currentPhase < PHASE_COST) {
            player.sendSystemMessage(Component.translatable("message.sporeadd.power1.not_enough_biomass"));
            return;
        }

        spore.setSpore(currentPhase - PHASE_COST);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                Ssounds.HOWLER_GROWL.get(),
                SoundSource.PLAYERS,
                4.0f,
                1.0f
        );

        int protoCount = getProtoCountAcrossDimensions(player);
        spawnVerwasWithBudget(player, level, playerLevel, protoCount);
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

    private static void spawnVerwasWithBudget(Player player, Level level, int playerLevel, int protoCount) {
        int baseVerwas = 6;
        int maxVerwas = 10;

        int totalVerwas = baseVerwas + protoCount;
        if (totalVerwas > maxVerwas) {
            totalVerwas = maxVerwas;
        }

        int hordeBiomass = getHordeBiomass(playerLevel);

        for (int i = 1; i <= protoCount; i++) {
            if (baseVerwas + i <= maxVerwas) {
                hordeBiomass += 7;
            } else {
                hordeBiomass += 5;
            }
        }

        if (totalVerwas > hordeBiomass + 1) {
            totalVerwas = hordeBiomass + 1;
        }

        int[] vervaCosts = new int[totalVerwas];
        int[] vervaCaps = new int[totalVerwas];

        for (int i = 0; i < totalVerwas; i++) {
            vervaCaps[i] = 5 + random.nextInt(5);
        }

        int vanguardIndex = random.nextInt(totalVerwas);
        vervaCaps[vanguardIndex] = 10;

        int remainingPoints = hordeBiomass;

        for (int i = 0; i < totalVerwas; i++) {
            if (remainingPoints > 0) {
                vervaCosts[i] = 1;
                remainingPoints--;
            }
        }

        while (remainingPoints > 0) {
            int index = random.nextInt(totalVerwas);

            if (vervaCosts[index] < vervaCaps[index]) {
                vervaCosts[index]++;
                remainingPoints--;
            } else {
                boolean allMaxed = true;
                for (int i = 0; i < totalVerwas; i++) {
                    if (vervaCosts[i] < vervaCaps[i]) {
                        allMaxed = false;
                        break;
                    }
                }

                if (allMaxed) {
                    break;
                }
            }
        }

        Map<Integer, List<String>> mobsByCost = getConfiguredHordePools();
        List<String> fallbackPool = mobsByCost.getOrDefault(1, List.of("spore:inf_human"));

        for (int i = 0; i < totalVerwas; i++) {
            int finalCost = vervaCosts[i];

            List<String> pool = mobsByCost.get(finalCost);
            if (pool == null || pool.isEmpty()) {
                pool = fallbackPool;
            }
            if (pool.isEmpty()) {
                pool = List.of("spore:inf_human");
            }

            String selectedMob = pool.get(random.nextInt(pool.size()));

            double offsetX = (random.nextDouble() * 30) - 15;
            double offsetZ = (random.nextDouble() * 30) - 15;
            double spawnX = player.getX() + offsetX;
            double spawnZ = player.getZ() + offsetZ;
            double spawnY = getValidSpawnY(level, spawnX, player.getY(), spawnZ);

            Verwa verwa = new Verwa(Sentities.VERVA.get(), level);
            verwa.moveTo(spawnX, spawnY, spawnZ);
            verwa.setStoredMob(selectedMob);
            verwa.tickEmerging();
            verwa.addTag(ANTIFARM_TAG);

            level.addFreshEntity(verwa);
        }
    }

    private static Map<Integer, List<String>> getConfiguredHordePools() {
        Map<Integer, List<String>> byCost = new LinkedHashMap<>();

        for (String entry : SporeAddsConfig.VERWA_HORDE_POOL.get()) {
            if (entry == null || entry.isBlank()) continue;

            String[] parts = entry.split(";");
            if (parts.length < 2) continue;

            String mobId = parts[0].trim();
            String costRaw = parts[1].trim();

            if (mobId.isEmpty()) continue;

            try {
                int cost = Integer.parseInt(costRaw);
                if (cost < 0) continue;

                List<String> list = byCost.computeIfAbsent(cost, k -> new ArrayList<>());
                if (!list.contains(mobId)) {
                    list.add(mobId);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return byCost;
    }

    private static double getValidSpawnY(Level level, double x, double playerY, double z) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(x, playerY + 5, z);

        for (int i = 0; i < 20; i++) {
            if (level.getBlockState(mpos.below()).isSolidRender(level, mpos.below())
                    && level.isEmptyBlock(mpos)
                    && level.isEmptyBlock(mpos.above())) {
                return mpos.getY();
            }
            mpos.move(net.minecraft.core.Direction.DOWN);
        }

        return playerY;
    }

    private static int getHordeBiomass(int playerLevel) {
        switch (playerLevel) {
            case 4: return 11;
            case 5: return 15;
            case 6: return 18;
            case 7: return 22;
            case 8: return 26;
            case 9: return 30;
            default:
                if (playerLevel > 9) return 35;
                return 7;
        }
    }
}