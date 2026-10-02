package com.sporeadds.sporeaddsmod.Powers;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.entity.StaticEntity;
import com.sporeadds.sporeaddsmod.items.MutagenicCompoundItem;
import com.sporeadds.sporeaddsmod.items.MutagenicCompoundVariant;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import com.sporeadds.sporeaddsmod.util.SporeIdentifierUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class Poder1 extends PowerBase {

    public static final Map<UUID, Integer> timers = new HashMap<>();
    public static final Map<UUID, Entity> mountingMap = new HashMap<>();
    private static final Map<UUID, MutagenicCompoundVariant> pendingVariantMap = new HashMap<>();

    /**
     * Cocoons "pelados": usan la misma maquinaria de tick que el cocoon de evolución
     * (montura, timer, apertura, interrupción) pero al completar el timer NO suben de
     * nivel ni cambian subclase. Usado por Call of the Hive (rendición ante la colmena).
     */
    public static final java.util.Set<UUID> PLAIN_COCOONS = new java.util.HashSet<>();

    public void use(ServerPlayer player) {
        if (timers.containsKey(player.getUUID()) || mountingMap.containsKey(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.sporeadd.power1.already_cocoon"));
            return;
        }

        if (player.isPassenger()) {
            player.sendSystemMessage(Component.translatable("message.sporeadd.power1.cannot_ride_cocoon"));
            return;
        }

        player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
            int curLevel = levelCap.getLevel();
            int curKnowledge = levelCap.getKnowledgeLevel();

            if (curLevel > 9) {
                player.sendSystemMessage(Component.translatable("message.sporeadd.power1.max_evolution"));
                return;
            }

            player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
                int phaseCost;

                if (curLevel >= 9) {
                    phaseCost = 10;
                } else {
                    int nextLevel = curLevel + 1;
                    String configString = getEvolutionCostString(nextLevel);
                    phaseCost = parsePhaseCost(configString, 50);

                    boolean requiresItems = nextLevel > curKnowledge;

                    if (requiresItems) {
                        if (!checkAndConsumeItems(player, configString, true)) {
                            return;
                        }

                        if (!checkAndConsumeItems(player, configString, false)) {
                            return;
                        }
                    }
                }

                int curPhase = spore.getSpore();
                if (curPhase < phaseCost) {
                    player.sendSystemMessage(Component.translatable("message.sporeadd.power1.not_enough_biomass", phaseCost));
                    return;
                }

                MutagenicCompoundVariant consumedVariant = findValidMutagenicCompound(player);

                if (consumedVariant != null) {
                    if (!consumeValidMutagenicCompound(player, consumedVariant)) {
                        consumedVariant = null;
                    }
                }

                spore.addSpore(-phaseCost);

                if (consumedVariant != null) {
                    pendingVariantMap.put(player.getUUID(), consumedVariant);
                }

                if (consumedVariant != null) {
                    player.sendSystemMessage(Component.translatable("message.sporeadd.power1.evolving")
                            .withStyle(getEvolutionColor(consumedVariant)));
                } else {
                    player.sendSystemMessage(Component.translatable(
                            curLevel >= 9
                                    ? "message.sporeadd.power1.entering_cocoon"
                                    : "message.sporeadd.power1.starting_evolution"
                    ));
                }

                spawnMoundAndStartTimer(player, levelCap, curLevel);
            });
        });
    }

    public static String getEvolutionCostString(int levelTarget) {
        return switch (levelTarget) {
            case 1 -> com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_1.get();
            case 2 -> com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_2.get();
            case 3 -> com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_3.get();
            case 4 -> com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_4.get();
            case 5 -> com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_5.get();
            case 6 -> com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_6.get();
            case 7 -> com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_7.get();
            case 8 -> com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_8.get();
            case 9 -> com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_9.get();
            default -> "() cost:50";
        };
    }

    public static String[] getServerEvolutionCostsSnapshot() {
        String[] costs = new String[10];
        for (int i = 1; i <= 9; i++) {
            costs[i] = getEvolutionCostString(i);
        }
        return costs;
    }

    private static int parsePhaseCost(String configString, int fallback) {
        if (configString == null || configString.isBlank()) {
            return fallback;
        }

        int costIndex = configString.indexOf("cost:");
        if (costIndex < 0) {
            return fallback;
        }

        try {
            String costStr = configString.substring(costIndex + 5).trim();
            int parsed = Integer.parseInt(costStr);
            return Math.max(0, parsed);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean checkAndConsumeItems(ServerPlayer player, String configStr, boolean simulate) {
        String[] split = configStr.split("cost:", 2);
        String itemsPart = split[0].trim();

        if (itemsPart.equals("()") || itemsPart.isEmpty()) {
            return true;
        }

        String[] andGroups = itemsPart.split(",");
        boolean hasAll = true;

        for (String group : andGroups) {
            String[] orOptions = group.split("/");
            boolean groupSatisfied = false;
            String missingItemsMsg = "";

            for (String option : orOptions) {
                String cleanOption = option.replaceAll("[()]", "").trim();
                if (cleanOption.isEmpty()) {
                    continue;
                }

                String[] parts = cleanOption.split("amount:", 2);
                if (parts.length != 2) {
                    continue;
                }

                String itemIdStr = parts[0].trim();
                int requiredAmount;

                try {
                    requiredAmount = Integer.parseInt(parts[1].trim());
                } catch (Exception ignored) {
                    continue;
                }

                if (requiredAmount <= 0) {
                    continue;
                }

                ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);
                if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                    continue;
                }

                int countInInventory = 0;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()) != null
                            && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId)) {
                        countInInventory += stack.getCount();
                    }
                }

                if (countInInventory >= requiredAmount) {
                    groupSatisfied = true;

                    if (!simulate) {
                        int remainingToConsume = requiredAmount;

                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()) != null
                                    && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId)) {
                                int shrink = Math.min(stack.getCount(), remainingToConsume);
                                stack.shrink(shrink);
                                remainingToConsume -= shrink;
                                if (remainingToConsume <= 0) {
                                    break;
                                }
                            }
                        }
                    }

                    break;
                } else {
                    if (!missingItemsMsg.isEmpty()) {
                        missingItemsMsg += " OR ";
                    }
                    missingItemsMsg += requiredAmount + "x " + itemIdStr;
                }
            }

            if (!groupSatisfied) {
                if (simulate && !missingItemsMsg.isEmpty()) {
                    player.sendSystemMessage(Component.translatable("message.sporeadd.power1.missing_items", missingItemsMsg));
                }
                hasAll = false;
            }
        }

        return hasAll;
    }

    private static void playSoundForAllPlayers(ServerLevel sourceLevel, ResourceLocation soundId, SoundSource source, float volume, float pitch) {
        if (sourceLevel.getServer() == null) return;

        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundId);
        if (soundEvent == null) return;

        for (ServerPlayer target : sourceLevel.getServer().getPlayerList().getPlayers()) {
            target.level().playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    soundEvent,
                    source,
                    volume,
                    pitch
            );
        }
    }

    private static void spawnMoundAndStartTimer(ServerPlayer player, com.sporeadds.sporeaddsmod.level.PlayerLevel level, int curLevel) {
        ServerLevel serverLevel = (ServerLevel) player.level();

        boolean maxLevelNoVariant = curLevel >= 9 && !pendingVariantMap.containsKey(player.getUUID());

        EntityType<?> moundType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "cocoon"));
        if (moundType != null) {
            Entity moundEntity = moundType.create(serverLevel);
            if (moundEntity != null) {
                moundEntity.setPos(player.getX(), player.getY(), player.getZ());

                if (moundEntity instanceof Mob mob) {
                    mob.finalizeSpawn(
                            serverLevel,
                            serverLevel.getCurrentDifficultyAt(mob.blockPosition()),
                            MobSpawnType.EVENT,
                            null,
                            null
                    );
                }

                if (moundEntity instanceof LivingEntity livingCocoon) {
                    double nuevaVida = maxLevelNoVariant ? 300.0 : (curLevel + 1) * 100.0;
                    if (livingCocoon.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) != null) {
                        livingCocoon.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(nuevaVida);
                        livingCocoon.setHealth((float) nuevaVida);
                    }
                }

                ScaleData playerScaleData = ScaleTypes.BASE.getScaleData(player);
                float sizeScale = playerScaleData.getScale();

                ScaleData cocoonScaleData = ScaleTypes.BASE.getScaleData(moundEntity);
                cocoonScaleData.setScale(sizeScale);
                cocoonScaleData.setTargetScale(sizeScale);

                serverLevel.addFreshEntity(moundEntity);

                player.startRiding(moundEntity, true);
                // El cocoon (StaticEntity) se une al equipo "spore" y brilla por su cuenta.
                if (moundEntity instanceof StaticEntity cocoon) {
                    cocoon.beginClosing();
                }

                int durationSeconds = maxLevelNoVariant ? 30 : 30 + (30 * (curLevel / 3));
                int duration = durationSeconds * 20;

                timers.put(player.getUUID(), duration);
                mountingMap.put(player.getUUID(), moundEntity);

                if (curLevel < 9) {
                    for (ServerPlayer target : serverLevel.getServer().getPlayerList().getPlayers()) {
                        if (!target.getUUID().equals(player.getUUID())) {
                            target.sendSystemMessage(Component.translatable("message.sporeadd.power1.evolving_broadcast"));
                        }
                    }

                    playSoundForAllPlayers(
                            serverLevel,
                            ResourceLocation.fromNamespaceAndPath("spore", "calamity_incoming"),
                            SoundSource.MASTER,
                            1.0F,
                            1.7F
                    );
                }
            }
        }
    }

    /**
     * Cocoon "pelado" para Call of the Hive: mismas mecánicas visuales/tick que el cocoon
     * de evolución, pero al terminar el timer solo se abre y descarta (sin subir de nivel).
     */
    public static void startPlainCocoon(ServerPlayer player, int durationTicks) {
        UUID uuid = player.getUUID();
        if (timers.containsKey(uuid) || mountingMap.containsKey(uuid)) return;
        if (player.isPassenger()) player.stopRiding();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        EntityType<?> moundType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "cocoon"));
        if (moundType == null) return;

        Entity moundEntity = moundType.create(serverLevel);
        if (moundEntity == null) return;

        moundEntity.setPos(player.getX(), player.getY(), player.getZ());

        if (moundEntity instanceof Mob mob) {
            mob.finalizeSpawn(
                    serverLevel,
                    serverLevel.getCurrentDifficultyAt(mob.blockPosition()),
                    MobSpawnType.EVENT,
                    null,
                    null
            );
        }

        if (moundEntity instanceof LivingEntity livingCocoon) {
            var maxHpAttr = livingCocoon.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (maxHpAttr != null) {
                maxHpAttr.setBaseValue(300.0D);
                livingCocoon.setHealth(300.0F);
            }
        }

        ScaleData playerScaleData = ScaleTypes.BASE.getScaleData(player);
        float sizeScale = playerScaleData.getScale();

        ScaleData cocoonScaleData = ScaleTypes.BASE.getScaleData(moundEntity);
        cocoonScaleData.setScale(sizeScale);
        cocoonScaleData.setTargetScale(sizeScale);

        serverLevel.addFreshEntity(moundEntity);
        player.startRiding(moundEntity, true);
        // El cocoon (StaticEntity) se une al equipo "spore" y brilla por su cuenta.
        if (moundEntity instanceof StaticEntity cocoon) {
            cocoon.beginClosing();
        }

        timers.put(uuid, durationTicks);
        mountingMap.put(uuid, moundEntity);
        PLAIN_COCOONS.add(uuid);
    }

    /**
     * Fin de la sesión del cocoon: en vez de descartarlo de golpe, si es un {@link StaticEntity}
     * se le pide que se entierre en el suelo (desmonta al jugador y desaparece al terminar).
     */
    private static void endCocoon(net.minecraft.world.entity.Entity ridden) {
        if (ridden instanceof StaticEntity cocoon && cocoon.isAlive()) {
            cocoon.retreatIntoGround();
        } else if (ridden != null && ridden.isAlive()) {
            ridden.discard();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;
        if (timers.isEmpty()) return;

        Iterator<UUID> it = timers.keySet().iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(uuid);
            Entity ridden = mountingMap.get(uuid);

            if (player == null || ridden == null) {
                pendingVariantMap.remove(uuid);
                PLAIN_COCOONS.remove(uuid);
                it.remove();
                mountingMap.remove(uuid);
                continue;
            }

            boolean plainCocoon = PLAIN_COCOONS.contains(uuid);

            int timeLeft = timers.get(uuid);

            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 255, false, false, false));

            if (player.getRandom().nextInt(120) == 0 && player.level() instanceof ServerLevel lvl) {
                lvl.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "womb_ambient")),
                        SoundSource.MASTER,
                        3.15F, 1.0F
                );
            }

            player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
                if (levelCap.getLevel() >= 9 && timeLeft % 40 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, false, true));
                }
            });

            boolean cancel = !player.isPassenger() || player.getVehicle() != ridden || !ridden.isAlive();

            if (cancel) {
                PLAIN_COCOONS.remove(uuid);
                player.sendSystemMessage(Component.translatable("message.sporeadd.power1.cocoon_interrupted"));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 20, 1));

                player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
                    if (levelCap.getLevel() >= 9) {
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 20, 2));
                    }
                });

                if (player.level() instanceof ServerLevel lvl) {
                    lvl.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "evolve_hurt")),
                            SoundSource.MASTER,
                            1.0F, 0.7F
                    );
                    lvl.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "fungal_boom")),
                            SoundSource.MASTER,
                            3.15F, 1.0F
                    );
                    lvl.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "broken_screams")),
                            SoundSource.MASTER,
                            3.15F, 0.5F
                    );

                    StaticEntity.spawnDustBurst(lvl, player.getX(), player.getY(), player.getZ());
                }

                pendingVariantMap.remove(uuid);
                PLAIN_COCOONS.remove(uuid);

                endCocoon(ridden);

                it.remove();
                mountingMap.remove(uuid);
                continue;
            }

            if (timeLeft <= 100) {
                if (player.getVehicle() instanceof StaticEntity staticEntity) {
                    staticEntity.setOpen(true);
                }
            }

            if (!plainCocoon && timeLeft % 20 == 0) {
                int currentHp = 0;
                int maxHp = 0;
                if (ridden instanceof LivingEntity livingCocoon) {
                    currentHp = (int) Math.ceil(livingCocoon.getHealth());
                    maxHp = (int) livingCocoon.getMaxHealth();
                }
                player.sendSystemMessage(Component.translatable("message.sporeadd.power1.evolving_progress", (timeLeft / 20), currentHp, maxHp));
            }

            if (timeLeft <= 0) {
                if (plainCocoon) {
                    PLAIN_COCOONS.remove(uuid);
                    pendingVariantMap.remove(uuid);

                    if (player.level() instanceof ServerLevel lvl) {
                        lvl.playSound(
                                null,
                                player.getX(), player.getY(), player.getZ(),
                                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "organoid_damage")),
                                SoundSource.MASTER,
                                3.15F, 1.0F
                        );
                    }

                    // Se abre y, en vez de convertirse en partículas, desmonta y se entierra.
                    endCocoon(ridden);

                    // Fija el punto de reaparición del jugador en la posición del cocoon.
                    if (player.level() instanceof ServerLevel spawnLevel) {
                        player.setRespawnPosition(spawnLevel.dimension(), player.blockPosition(), player.getYRot(), true, false);
                    }

                    // Al emerger convertido en Kommandant (rendición ante la colmena): Regeneración V, 10 s.
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 4, false, true, true));

                    it.remove();
                    mountingMap.remove(uuid);
                    continue;
                }

                player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
                    int currentLevel = levelCap.getLevel();

                    if (currentLevel < 9) {
                        levelCap.addLevel(1);
                        player.sendSystemMessage(Component.translatable("message.sporeadd.power1.evolution_stage_completed", levelCap.getLevel()));

                        if (levelCap.getLevel() == 9 && player.level() instanceof ServerLevel lvl) {
                            for (ServerPlayer pl : lvl.getServer().getPlayerList().getPlayers()) {
                                pl.sendSystemMessage(Component.translatable("message.sporeadd.power1.evolution_fully_completed"));
                                lvl.playSound(
                                        null,
                                        player.getX(), player.getY(), player.getZ(),
                                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "calamity_incoming")),
                                        SoundSource.MASTER,
                                        3.0F, 0.5F
                                );
                            }
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("message.sporeadd.power1.cocoon_completed"));
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60 * 20, 1, false, true, true));
                    }
                });

                MutagenicCompoundVariant pending = pendingVariantMap.remove(uuid);
                if (pending != null) {
                    player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
                        int penalty = com.sporeadds.sporeaddsmod.config.SporeAddsConfig
                                .KOMMANDANT_VARIANT_CHANGE_LEVEL_PENALTY.get();

                        if (penalty > 0) {
                            int currentLevelAfterGain = levelCap.getLevel();
                            int newLevel = Math.max(0, currentLevelAfterGain - penalty);

                            if (newLevel != currentLevelAfterGain) {
                                levelCap.setLevel(newLevel);
                                player.sendSystemMessage(
                                        Component.translatable("message.sporeadd.power1.variant_change_penalty", penalty, newLevel)
                                                .withStyle(ChatFormatting.RED)
                                );
                            }
                        }
                    });

                    SporeIdentifierUtil.setSubclassAndSync(player, pending.getSubclassId());
                }

                if (player.level() instanceof ServerLevel lvl) {
                    lvl.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "organoid_damage")),
                            SoundSource.MASTER,
                            3.15F, 1.0F
                    );
                }

                // Se abre y, en vez de convertirse en partículas, desmonta y se entierra.
                endCocoon(ridden);

                it.remove();
                mountingMap.remove(uuid);
            } else {
                timers.put(uuid, timeLeft - 1);
            }
        }
    }

    @Nullable
    private static MutagenicCompoundVariant findValidMutagenicCompound(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        MutagenicCompoundVariant found = getValidCompoundVariantFromStack(player, main);
        if (found != null) {
            return found;
        }

        ItemStack off = player.getOffhandItem();
        return getValidCompoundVariantFromStack(player, off);
    }

    @Nullable
    private static MutagenicCompoundVariant getValidCompoundVariantFromStack(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof MutagenicCompoundItem)) {
            return null;
        }

        MutagenicCompoundVariant variant = MutagenicCompoundItem.getVariant(stack);

        final MutagenicCompoundVariant[] result = {null};

        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            if (!"kommandant".equalsIgnoreCase(data.getIdentifier())) {
                return;
            }

            if (variant.getSubclassId().equalsIgnoreCase(data.getSubclass())) {
                return;
            }

            result[0] = variant;
        });

        return result[0];
    }

    private static boolean consumeValidMutagenicCompound(ServerPlayer player, MutagenicCompoundVariant expectedVariant) {
        ItemStack main = player.getMainHandItem();
        if (consumeCompoundFromStack(main, expectedVariant)) {
            return true;
        }

        ItemStack off = player.getOffhandItem();
        return consumeCompoundFromStack(off, expectedVariant);
    }

    private static boolean consumeCompoundFromStack(ItemStack stack, MutagenicCompoundVariant expectedVariant) {
        if (stack.isEmpty() || !(stack.getItem() instanceof MutagenicCompoundItem)) {
            return false;
        }

        MutagenicCompoundVariant stackVariant = MutagenicCompoundItem.getVariant(stack);
        if (stackVariant != expectedVariant) {
            return false;
        }

        stack.shrink(1);
        return true;
    }

    private static ChatFormatting getEvolutionColor(MutagenicCompoundVariant variant) {
        return switch (variant) {
            case ABYSSAL -> ChatFormatting.AQUA;
            case CAUSTIC -> ChatFormatting.GREEN;
            case ORIGINAL -> ChatFormatting.DARK_RED;
            case GLUTTONOUS -> ChatFormatting.GOLD;
        };
    }
}