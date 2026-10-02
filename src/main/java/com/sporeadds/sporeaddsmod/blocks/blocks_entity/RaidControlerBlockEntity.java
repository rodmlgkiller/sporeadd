package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.Harbinger.Spore.Sentities.Utility.ArenaEntity;
import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.client.gui.RaidControlerMenu;
import com.sporeadds.sporeaddsmod.util.RaidSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class RaidControlerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_EGG = 0;
    public static final int SLOT_BAIT = 1;
    public static final int SLOT_BIOMASS_BAIT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int COOLDOWN_TICKS = 400;

    private static final double ALERT_RADIUS = 150.0D;
    private static final double REQUIRED_PROTO_RADIUS = 5000.0D;

    private int cooldownTicks = 0;
    private int gasAnimationTicks = 0;
    private int gasAnimationDuration = 0;
    private int gasAnimationMaxRadiusScaled = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_EGG) {
                return stack.getItem() == ModItems.OVERCHARGED_SCENT_SPAWN_EGG.get();
            } else if (slot == SLOT_BAIT) {
                return stack.getItem() == ModItems.POTENCY_BAIT_I.get()
                        || stack.getItem() == ModItems.POTENCY_BAIT_II.get()
                        || stack.getItem() == ModItems.POTENCY_BAIT_III.get();
            } else if (slot == SLOT_BIOMASS_BAIT) {
                return stack.getItem() == ModItems.BIOMASS_BAIT.get();
            }

            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public RaidControlerBlockEntity(BlockPos pos, BlockState state) {
        super(modblocksentity.RAID_CONTROLER.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public int getScentCount() {
        return itemHandler.getStackInSlot(SLOT_EGG).getCount();
    }

    public int getPotencyLevel() {
        ItemStack stack = itemHandler.getStackInSlot(SLOT_BAIT);

        if (stack.isEmpty()) {
            return 0;
        } else if (stack.getItem() == ModItems.POTENCY_BAIT_I.get()) {
            return 1;
        } else if (stack.getItem() == ModItems.POTENCY_BAIT_II.get()) {
            return 2;
        } else if (stack.getItem() == ModItems.POTENCY_BAIT_III.get()) {
            return 3;
        }

        return 0;
    }

    public int getBiomassBaitCount() {
        return itemHandler.getStackInSlot(SLOT_BIOMASS_BAIT).getCount();
    }

    public boolean canStartRaid() {
        return getScentCount() > 0 && getPotencyLevel() > 0 && cooldownTicks <= 0;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    private boolean hasProtoNearby(ServerLevel level, BlockPos centerPos) {
        AABB searchBox = new AABB(centerPos).inflate(REQUIRED_PROTO_RADIUS);
        return !level.getEntitiesOfClass(Proto.class, searchBox, Proto::isAlive).isEmpty();
    }

    private void notifyMissingProto(ServerLevel level) {
        double radiusSqr = ALERT_RADIUS * ALERT_RADIUS;

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(level.dimension())
                    && player.blockPosition().distSqr(worldPosition) <= radiusSqr) {
                player.displayClientMessage(
                        Component.translatable("message.sporeadd.raid_controler.no_proto_nearby", REQUIRED_PROTO_RADIUS),
                        true
                );
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    public boolean tryStartRaid() {
        if (level == null || level.isClientSide()) {
            return false;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        if (!canStartRaid()) {
            return false;
        }

        if (!hasProtoNearby(serverLevel, worldPosition)) {
            notifyMissingProto(serverLevel);
            return false;
        }

        int waveSize = getScentCount();
        int potency = getPotencyLevel();
        int waveLevel = potency - 1;
        int specialSpawns = getBiomassBaitCount();

        BlockPos spawnPos = findValidTendrilSpawnPos(serverLevel, worldPosition);
        if (spawnPos == null) {
            return false;
        }

        itemHandler.setStackInSlot(SLOT_EGG, ItemStack.EMPTY);
        itemHandler.setStackInSlot(SLOT_BAIT, ItemStack.EMPTY);
        itemHandler.setStackInSlot(SLOT_BIOMASS_BAIT, ItemStack.EMPTY);
        setChanged();

        startGasAnimation(waveSize);
        broadcastRaidAlert(serverLevel, spawnPos, waveSize, potency, specialSpawns);

        ArenaEntity arena = RaidSpawner.spawnCustomRaid(
                serverLevel, spawnPos, waveSize, waveLevel, specialSpawns, 0.0D, true
        );

        arena.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 0, false, false, false));
        CompoundTag arenaData = arena.getPersistentData();
        arenaData.putBoolean("SporeAdds_IsArenaTendril", true);
        arenaData.putInt("SporeAdds_JammerTimer", 0);
        arena.tickEmerging();

        cooldownTicks = COOLDOWN_TICKS;
        setChanged();
        return true;
    }

    private BlockPos findValidTendrilSpawnPos(ServerLevel level, BlockPos controllerPos) {
        for (int attempt = 0; attempt < 30; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double distance = 100.0D + level.random.nextDouble() * 50.0D;

            int offsetX = (int) Math.round(Math.cos(angle) * distance);
            int offsetZ = (int) Math.round(Math.sin(angle) * distance);

            BlockPos candidateXZ = controllerPos.offset(offsetX, 0, offsetZ);
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, candidateXZ.getX(), candidateXZ.getZ());
            BlockPos candidate = new BlockPos(candidateXZ.getX(), surfaceY, candidateXZ.getZ());

            if (isValidTendrilSpawn(level, candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private boolean isValidTendrilSpawn(ServerLevel level, BlockPos pos) {
        AABB box = new AABB(
                pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                pos.getX() + 0.5D, pos.getY() + 3.0D, pos.getZ() + 0.5D
        );

        return level.noCollision(box) && level.getBlockState(pos.below()).isSolid();
    }

    private void broadcastRaidAlert(ServerLevel level, BlockPos raidPos, int waveSize, int potency, int specialSpawns) {
        SoundEvent alertSound = ForgeRegistries.SOUND_EVENTS.getValue(
                new ResourceLocation("minecraft", "entity.warden.sonic_boom")
        );

        double radiusSqr = ALERT_RADIUS * ALERT_RADIUS;

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(level.dimension())
                    && player.blockPosition().distSqr(raidPos) <= radiusSqr) {

                if (alertSound != null) {
                    player.connection.send(new ClientboundSoundPacket(
                            BuiltInRegistries.SOUND_EVENT.wrapAsHolder(alertSound),
                            SoundSource.HOSTILE,
                            player.getX(), player.getY(), player.getZ(),
                            2.0F, 1.0F,
                            level.getRandom().nextLong()
                    ));
                }

                Component title = Component.translatable("message.sporeadd.raid_controler.raid_detected_short");
                Component subtitle = Component.translatable(
                        "message.sporeadd.raid_controler.raid_detected_subtitle",
                        raidPos.getX(), raidPos.getY(), raidPos.getZ()
                );
                Component actionbar = Component.translatable(
                        "message.sporeadd.raid_controler.raid_detected_actionbar",
                        waveSize, potency, specialSpawns
                );

                player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 150, 10));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                player.connection.send(new ClientboundSetActionBarTextPacket(actionbar));
            }
        }
    }

    private void startGasAnimation(int scentCount) {
        gasAnimationTicks = 1;
        gasAnimationDuration = 14 + Math.min(20, scentCount * 2);
        gasAnimationMaxRadiusScaled = 6 + scentCount * 2;
        setChanged();
    }

    private void tickGasAnimation() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (gasAnimationTicks <= 0) {
            return;
        }

        ParticleOptions particle = Sparticles.SPORE_PARTICLE.get();
        double progress = (double) gasAnimationTicks / (double) gasAnimationDuration;
        double radius = 0.15D + ((double) gasAnimationMaxRadiusScaled / 10.0D) * progress;
        int points = 24 + gasAnimationTicks * 2;

        double centerX = worldPosition.getX() + 0.5D;
        double centerY = worldPosition.getY() + 0.55D;
        double centerZ = worldPosition.getZ() + 0.5D;

        for (int i = 0; i < points; i++) {
            double theta = Math.PI * 2.0D * serverLevel.random.nextDouble();
            double phi = Math.acos(1.0D - 2.0D * serverLevel.random.nextDouble());

            double dirX = Math.sin(phi) * Math.cos(theta);
            double dirY = Math.cos(phi);
            double dirZ = Math.sin(phi) * Math.sin(theta);

            double spawnX = centerX + dirX * radius;
            double spawnY = centerY + dirY * radius;
            double spawnZ = centerZ + dirZ * radius;

            double velocityScale = 0.03D + progress * 0.05D;

            serverLevel.sendParticles(
                    particle,
                    spawnX, spawnY, spawnZ,
                    0,
                    dirX * velocityScale,
                    dirY * velocityScale,
                    dirZ * velocityScale,
                    1.0D
            );
        }

        gasAnimationTicks++;
        if (gasAnimationTicks > gasAnimationDuration) {
            gasAnimationTicks = 0;
            gasAnimationDuration = 0;
            gasAnimationMaxRadiusScaled = 0;
            setChanged();
        }
    }

    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
            if (cooldownTicks == 0) {
                setChanged();
            }
        }

        tickGasAnimation();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.sporeadd.raid_controler");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new RaidControlerMenu(windowId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("cooldown", cooldownTicks);
        tag.putInt("gasAnimationTicks", gasAnimationTicks);
        tag.putInt("gasAnimationDuration", gasAnimationDuration);
        tag.putInt("gasAnimationMaxRadiusScaled", gasAnimationMaxRadiusScaled);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        cooldownTicks = tag.getInt("cooldown");
        gasAnimationTicks = tag.getInt("gasAnimationTicks");
        gasAnimationDuration = tag.getInt("gasAnimationDuration");
        gasAnimationMaxRadiusScaled = tag.getInt("gasAnimationMaxRadiusScaled");
    }

    public void drops() {
        if (level == null) {
            return;
        }

        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(level, worldPosition, inventory);
    }
}