package com.sporeadds.sporeaddsmod.network;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SpawnVervaPacket {
    private final String mobId;
    private final int cost;
    private final int variant;

    private final float bombDamage;
    private final int bombRadius;
    private final boolean bombCarrier;
    private final float bombScale;
    private final String bombDisplayName;

    private static final ConcurrentHashMap<UUID, Integer> PENDING_VARIANTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> PENDING_LOADOUTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, PendingVervaSpawn> PENDING_VERVA_SPAWNS = new ConcurrentHashMap<>();

    private static final Set<String> DIRECT_ORGANOIDS = Set.of(
            "spore:umarmed", "spore:usurper", "spore:braurei", "spore:delusioner"
    );

    private static final String TRACKER_TAG = "SporeAdds_Tracker";
    private static final String STORED_MOB_TAG = "SporeAdds_StoredMob";

    public static class PlayerLoadout {
        public String head, chest, legs, boots, mainHand, offHand;

        public PlayerLoadout(String head, String chest, String legs, String boots, String mainHand, String offHand) {
            this.head = head;
            this.chest = chest;
            this.legs = legs;
            this.boots = boots;
            this.mainHand = mainHand;
            this.offHand = offHand;
        }
    }

    private static class PendingVervaSpawn {
        final UUID trackingId;
        final UUID vervaUuid;
        final String mobId;
        final BlockPos spawnPos;
        final long createdAtGameTime;

        PendingVervaSpawn(UUID trackingId, UUID vervaUuid, String mobId, BlockPos spawnPos, long createdAtGameTime) {
            this.trackingId = trackingId;
            this.vervaUuid = vervaUuid;
            this.mobId = mobId;
            this.spawnPos = spawnPos;
            this.createdAtGameTime = createdAtGameTime;
        }
    }

    private static final PlayerLoadout ARCHER_LOADOUT = new PlayerLoadout(
            "minecraft:leather_helmet",
            "minecraft:leather_chestplate",
            "minecraft:leather_leggings",
            "minecraft:leather_boots",
            "minecraft:bow",
            null
    );

    private static final PlayerLoadout SWORDSMAN_LOADOUT = new PlayerLoadout(
            "minecraft:iron_helmet",
            "minecraft:iron_chestplate",
            "minecraft:chainmail_leggings",
            "minecraft:iron_boots",
            "minecraft:iron_sword",
            "minecraft:shield"
    );

    static {
        NeoForge.EVENT_BUS.register(SpawnVervaPacket.class);
    }

    public SpawnVervaPacket(String mobId, int cost, int variant, float bombDamage, int bombRadius, boolean bombCarrier, float bombScale, String bombDisplayName) {
        this.mobId = mobId;
        this.cost = cost;
        this.variant = variant;
        this.bombDamage = bombDamage;
        this.bombRadius = bombRadius;
        this.bombCarrier = bombCarrier;
        this.bombScale = bombScale;
        this.bombDisplayName = bombDisplayName;
    }

    public SpawnVervaPacket(String mobId, int cost, int variant) {
        this(mobId, cost, variant, 10.0F, 5, false, 1.0F, "");
    }

    public static void encode(SpawnVervaPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.mobId);
        buf.writeInt(msg.cost);
        buf.writeInt(msg.variant);
        buf.writeFloat(msg.bombDamage);
        buf.writeInt(msg.bombRadius);
        buf.writeBoolean(msg.bombCarrier);
        buf.writeFloat(msg.bombScale);
        buf.writeUtf(msg.bombDisplayName);
    }

    public static SpawnVervaPacket decode(FriendlyByteBuf buf) {
        return new SpawnVervaPacket(
                buf.readUtf(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readFloat(),
                buf.readUtf()
        );
    }

    private static int getServerSideCost(String mobId, int variant, String bombDisplayName) {
        for (String entry : SporeAddsConfig.VERWA_SUMMONING_MENU.get()) {
            String[] parts = entry.split(";");
            if (parts.length >= 2 && parts[0].trim().equals(mobId)) {
                try {
                    int baseCost = Integer.parseInt(parts[1].trim());
                    if (variant >= 0 && parts.length >= 3 && !parts[2].trim().isEmpty()) {
                        String[] varStrs = parts[2].split(",");
                        if (variant < varStrs.length) {
                            return Integer.parseInt(varStrs[variant].trim());
                        }
                    }
                    return baseCost;
                } catch (Exception ignored) {
                }
            }
        }

        for (String entry : SporeAddsConfig.ORGANOID_SUMMONING_MENU.get()) {
            String[] parts = entry.split(";");
            if (parts.length >= 2 && parts[0].trim().equals(mobId)) {
                try {
                    return Integer.parseInt(parts[1].trim());
                } catch (Exception ignored) {
                }
            }
        }

        for (String entry : SporeAddsConfig.BOMB_SUMMONING_MENU.get()) {
            // Multiple bomb rows share the same bomb_type (parts[3]) - e.g. "Basic Bomb", "Heavy Basic"
            // and "Carrier Basic" are all bomb_type 0 - so the display name (parts[1]) is what actually
            // identifies which specific menu entry the player picked.
            String[] parts = entry.split(";");
            if (parts.length >= 4 && parts[0].trim().equals(mobId) && parts[1].trim().equals(bombDisplayName)) {
                try {
                    int bombVariant = Integer.parseInt(parts[3].trim());
                    if (variant == bombVariant) {
                        return Integer.parseInt(parts[2].trim());
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return -1;
    }

    public static void handle(SpawnVervaPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            int realCost = getServerSideCost(msg.mobId, msg.variant, msg.bombDisplayName);

            if (realCost == -1) {
                player.displayClientMessage(
                        Component.translatable("message.sporeadd.spawn_verva.entity_not_found_config"),
                        true
                );
                return;
            }

            PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(sporeCap -> {
                if (sporeCap.getSpore() < realCost) {
                    player.displayClientMessage(
                            Component.translatable("message.sporeadd.spawn_verva.not_enough_biomass"),
                            true
                    );
                    return;
                }

                if (msg.mobId.equals("spore:flesh_bomb")) {
                    LivingEntity target = findBombTarget(level, player);
                    if (target == null) {
                        player.displayClientMessage(
                                Component.translatable("message.sporeadd.spawn_verva.no_valid_bomb_targets"),
                                true
                        );
                        return;
                    }

                    EntityType<?> bombType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(msg.mobId));
                    if (bombType == null) {
                        player.displayClientMessage(
                                Component.translatable("message.sporeadd.spawn_verva.entity_not_found", msg.mobId),
                                true
                        );
                        return;
                    }

                    Entity rawEntity = bombType.create(level);
                    if (!(rawEntity instanceof FleshBomb bomb)) {
                        player.displayClientMessage(
                                Component.translatable("message.sporeadd.spawn_verva.flesh_bomb_create_failed"),
                                true
                        );
                        return;
                    }

                    float actualDamage = msg.bombDamage;
                    int actualRadius = msg.bombRadius;
                    boolean actualCarrier = msg.bombCarrier;
                    float actualScale = msg.bombScale;

                    // Match on display name too, not just bomb_type (parts[3]) - several rows share the
                    // same bomb_type ("Basic Bomb", "Heavy Basic", "Carrier Basic", "V2Carr Basic" are
                    // all type 0), so matching by bomb_type alone silently picked whichever of those
                    // happened to be listed first, ignoring the carrier/damage/radius/scale the player
                    // actually selected.
                    for (String entry : SporeAddsConfig.BOMB_SUMMONING_MENU.get()) {
                        String[] parts = entry.split(";");
                        if (parts.length >= 8 && parts[0].trim().equals(msg.mobId) && parts[1].trim().equals(msg.bombDisplayName)) {
                            try {
                                int confVariant = Integer.parseInt(parts[3].trim());
                                if (confVariant == msg.variant) {
                                    actualDamage = Float.parseFloat(parts[4].trim());
                                    actualRadius = Integer.parseInt(parts[5].trim());
                                    actualCarrier = Boolean.parseBoolean(parts[6].trim());
                                    actualScale = Float.parseFloat(parts[7].trim());
                                    break;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    double bombX = player.getX();
                    double bombZ = player.getZ();
                    double bombY = level.dimension() == Level.NETHER ? player.getY() + 50.0D : player.getY() + 100.0D;

                    bomb.moveTo(bombX, bombY, bombZ, 0.0F, 0.0F);
                    bomb.setOwner(player);
                    bomb.setBombType(Math.max(0, Math.min(msg.variant, 4)));
                    bomb.setDamage(actualDamage);
                    bomb.setExplosion(actualRadius);
                    bomb.setCarrier(actualCarrier);
                    bomb.setTarget(target);
                    bomb.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(bomb);

                    try {
                        ScaleData scaleData = ScaleTypes.BASE.getScaleData(bomb);
                        scaleData.setTargetScale(actualScale);
                        scaleData.setScale(actualScale);
                    } catch (Exception e) {
                        System.err.println("[SporeAdds] Failed to apply Pehkui scale: " + e.getMessage());
                    }

                    SoundEvent fallingBombSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "falling_bomb"));
                    if (fallingBombSound != null) {
                        level.playSound(null, bomb.getX(), bomb.getY(), bomb.getZ(), fallingBombSound, SoundSource.HOSTILE, 10.25F, 1.0F);
                    }

                    sporeCap.addSpore(-realCost);
                    player.displayClientMessage(
                            Component.translatable("message.sporeadd.spawn_verva.bomb_deployed_target", target.getName()),
                            true
                    );
                    return;
                }

                BlockPos spawnPos = findValidSpawnPosNearPlayer(level, player, 12, 5.0D);
                if (spawnPos == null) {
                    player.displayClientMessage(
                            Component.translatable("message.sporeadd.spawn_verva.no_valid_spawn"),
                            true
                    );
                    return;
                }

                double spawnX = spawnPos.getX() + 0.5D;
                double spawnY = spawnPos.getY();
                double spawnZ = spawnPos.getZ() + 0.5D;

                if (DIRECT_ORGANOIDS.contains(msg.mobId)) {
                    EntityType<?> directType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(msg.mobId));
                    if (directType == null) return;

                    Entity rawEntity = directType.create(level);
                    if (rawEntity == null) return;

                    rawEntity.moveTo(spawnX, spawnY, spawnZ, level.getRandom().nextFloat() * 360.0F, 0.0F);

                    if (rawEntity instanceof Mob mob) {
                        DifficultyInstance difficulty = level.getCurrentDifficultyAt(spawnPos);
                        mob.finalizeSpawn(level, difficulty, MobSpawnType.COMMAND, null);
                    }

                    if (msg.variant >= 0 && rawEntity instanceof VariantKeeper keeper) {
                        keeper.setVariant(msg.variant);
                    }

                    level.addFreshEntity(rawEntity);
                    sporeCap.addSpore(-realCost);
                    player.displayClientMessage(
                            Component.translatable("message.sporeadd.spawn_verva.organoid_deployed", msg.mobId),
                            true
                    );
                    return;
                }

                EntityType<?> vervaType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("spore", "verva"));
                if (vervaType == null || vervaType == EntityType.PIG) {
                    return;
                }

                Entity rawEntity = vervaType.spawn(level, null, player, spawnPos, MobSpawnType.COMMAND, true, false);
                if (!(rawEntity instanceof Verwa verva)) {
                    return;
                }

                verva.setStoredMob(msg.mobId);

                if (msg.variant != -1 || msg.mobId.equals("spore:inf_player")) {
                    UUID trackingId = UUID.randomUUID();

                    verva.getPersistentData().putUUID(TRACKER_TAG, trackingId);
                    verva.getPersistentData().putString(STORED_MOB_TAG, msg.mobId);

                    if (msg.mobId.equals("spore:inf_player")) {
                        PENDING_LOADOUTS.put(trackingId, msg.variant);
                    } else {
                        PENDING_VARIANTS.put(trackingId, msg.variant);
                    }

                    PENDING_VERVA_SPAWNS.put(trackingId, new PendingVervaSpawn(
                            trackingId,
                            verva.getUUID(),
                            msg.mobId,
                            verva.blockPosition(),
                            level.getGameTime()
                    ));
                }

                verva.tickEmerging();
                sporeCap.addSpore(-realCost);
                player.displayClientMessage(
                        Component.translatable("message.sporeadd.spawn_verva.verwa_deployed", msg.mobId),
                        true
                );
            });
        });
        context.setPacketHandled(true);
    }

    private static BlockPos findValidSpawnPosNearPlayer(ServerLevel level, ServerPlayer player, int attempts, double radius) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            double offsetX = (level.getRandom().nextDouble() * radius * 2.0D) - radius;
            double offsetZ = (level.getRandom().nextDouble() * radius * 2.0D) - radius;

            double x = player.getX() + offsetX;
            double z = player.getZ() + offsetZ;

            double y = getValidSpawnY(level, x, player.getY(), z);
            BlockPos pos = BlockPos.containing(x, y, z);

            if (isValidSpawnPos(level, pos)) {
                return pos;
            }
        }

        BlockPos fallback = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        return isValidSpawnPos(level, fallback) ? fallback : null;
    }

    private static double getValidSpawnY(ServerLevel level, double x, double playerY, double z) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos((int) Math.floor(x), (int) Math.floor(playerY) + 5, (int) Math.floor(z));

        for (int i = 0; i < 20; i++) {
            if (level.getBlockState(mpos.below()).isSolidRender(level, mpos.below())
                    && level.isEmptyBlock(mpos)
                    && level.isEmptyBlock(mpos.above())) {
                return mpos.getY();
            }
            mpos.move(Direction.DOWN);
        }

        return playerY;
    }

    private static boolean isValidSpawnPos(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below())
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above());
    }

    private static LivingEntity findBombTarget(ServerLevel level, ServerPlayer owner) {
        double range = 100.0D;
        AABB searchBox = owner.getBoundingBox().inflate(range);

        List<Player> enemyPlayers = level.getEntitiesOfClass(
                Player.class,
                searchBox,
                p -> p != owner && p.isAlive() && !isSporeTeammate(owner, p)
        );
        if (!enemyPlayers.isEmpty()) {
            return enemyPlayers.stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(owner))).orElse(null);
        }

        List<Villager> villagers = level.getEntitiesOfClass(
                Villager.class,
                searchBox,
                Villager::isAlive
        );
        if (!villagers.isEmpty()) {
            return villagers.stream().min(Comparator.comparingDouble(v -> v.distanceToSqr(owner))).orElse(null);
        }

        List<LivingEntity> others = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e != owner && e.isAlive() && !(e instanceof Player) && !(e instanceof Villager) && !isSporeTeammate(owner, e)
        );
        return others.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(owner))).orElse(null);
    }

    private static boolean isSporeTeammate(Entity source, Entity target) {
        return source.getTeam() != null
                && target.getTeam() != null
                && source.getTeam().isAlliedTo(target.getTeam());
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        cleanupExpired(serverLevel);

        Entity entity = event.getEntity();

        if (entity instanceof Verwa) {
            return;
        }

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        String entityId = entity.getEncodeId();
        if (entityId == null) {
            return;
        }

        for (PendingVervaSpawn pending : PENDING_VERVA_SPAWNS.values()) {
            if (!entityId.equals(pending.mobId)) {
                continue;
            }

            if (!entity.level().dimension().equals(serverLevel.dimension())) {
                continue;
            }

            double distSq = entity.distanceToSqr(
                    pending.spawnPos.getX() + 0.5D,
                    pending.spawnPos.getY() + 0.5D,
                    pending.spawnPos.getZ() + 0.5D
            );

            if (distSq > 16.0D) {
                continue;
            }

            boolean applied = false;

            Integer loadout = PENDING_LOADOUTS.get(pending.trackingId);
            if (loadout != null && "spore:inf_player".equals(entityId)) {
                applyLoadout(living, loadout);
                PENDING_LOADOUTS.remove(pending.trackingId);
                applied = true;
            }

            Integer variant = PENDING_VARIANTS.get(pending.trackingId);
            if (variant != null && living instanceof VariantKeeper keeper) {
                keeper.setVariant(variant);
                PENDING_VARIANTS.remove(pending.trackingId);
                applied = true;
            }

            if (applied) {
                PENDING_VERVA_SPAWNS.remove(pending.trackingId);
            }

            break;
        }
    }

    private static void cleanupExpired(ServerLevel level) {
        long now = level.getGameTime();

        PENDING_VERVA_SPAWNS.entrySet().removeIf(entry -> {
            PendingVervaSpawn pending = entry.getValue();
            boolean expired = now - pending.createdAtGameTime > 200L;

            if (expired) {
                PENDING_VARIANTS.remove(pending.trackingId);
                PENDING_LOADOUTS.remove(pending.trackingId);
            }

            return expired;
        });
    }

    private static void applyLoadout(LivingEntity entity, int loadoutId) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            entity.setItemSlot(slot, ItemStack.EMPTY);
        }

        switch (loadoutId) {
            case 1 -> applyLoadout(entity, ARCHER_LOADOUT);
            case 2 -> applyLoadout(entity, SWORDSMAN_LOADOUT);
            default -> {
            }
        }
    }

    private static void applyLoadout(LivingEntity entity, PlayerLoadout loadout) {
        equipSlot(entity, EquipmentSlot.HEAD, loadout.head);
        equipSlot(entity, EquipmentSlot.CHEST, loadout.chest);
        equipSlot(entity, EquipmentSlot.LEGS, loadout.legs);
        equipSlot(entity, EquipmentSlot.FEET, loadout.boots);
        equipSlot(entity, EquipmentSlot.MAINHAND, loadout.mainHand);
        equipSlot(entity, EquipmentSlot.OFFHAND, loadout.offHand);
    }

    private static void equipSlot(LivingEntity entity, EquipmentSlot slot, String itemId) {
        if (itemId == null || itemId.isBlank()) {
            entity.setItemSlot(slot, ItemStack.EMPTY);
            return;
        }

        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        if (item != null && item != Items.AIR) {
            entity.setItemSlot(slot, new ItemStack(item));
        }
    }
}