package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import com.sporeadds.sporeaddsmod.entity.Tentacle;
import com.sporeadds.sporeaddsmod.entity.projectile.TentacleProjectile;
import com.sporeadds.sporeaddsmod.event.TentacleHandler.AbyssalTentacleRegenEvents;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class AbyssalTentaclePacket {

    public static final int ABSOLUTE_MAX_TENTACLES = 3;

    public static final String SLOT_1_TAG = "sporeadds_tentacle_slot_1";
    public static final String SLOT_2_TAG = "sporeadds_tentacle_slot_2";
    public static final String SLOT_3_TAG = "sporeadds_tentacle_slot_3";

    public static final String STATE_READY = "READY";
    public static final String STATE_DEPLOYED = "DEPLOYED";
    public static final String STATE_RETURNING = "RETURNING";
    public static final String STATE_DEAD = "DEAD";

    private static final int RECALL_DAMAGE_IMMUNITY_TICKS = 40;

    private static final ResourceLocation TENTACLE_LAUNCH_SOUND_ID = new ResourceLocation("spore", "cleaver_spin");
    private static final float TENTACLE_LAUNCH_SOUND_VOLUME = 1.0F;
    private static final float TENTACLE_LAUNCH_SOUND_PITCH = 1.1F;

    public AbyssalTentaclePacket() {
    }

    public static void encode(AbyssalTentaclePacket msg, FriendlyByteBuf buf) {
    }

    public static AbyssalTentaclePacket decode(FriendlyByteBuf buf) {
        return new AbyssalTentaclePacket();
    }

    public static void handle(AbyssalTentaclePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            boolean isAbyssal = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                    .map(data -> "kommandant".equals(data.getIdentifier()) && "abyssal".equals(data.getSubclass()))
                    .orElse(false);

            if (!isAbyssal) {
                return;
            }

            int playerLevel = player.getCapability(PlayerLevelProvider.PLAYER_LVL)
                    .map(cap -> cap.getLevel())
                    .orElse(0);

            int maxTentacles = getMaxTentaclesForLevel(playerLevel);

            CompoundTag data = player.getPersistentData();
            normalizeSlotStates(data);

            int freeSlot = findFirstReadySlot(data, maxTentacles);

            if (freeSlot == -1) {
                boolean recalledAny = recallGrabbedTentacles(player, data, maxTentacles);

                if (!recalledAny) {
                    sendTentacleStatus(player, data, maxTentacles);
                }
                return;
            }

            setSlotState(data, freeSlot, STATE_DEPLOYED);

            TentacleProjectile projectile = new TentacleProjectile(
                    ModEntities.TENTACLE_PROJECTILE.get(),
                    player.level(),
                    player,
                    6.0F
            );

            projectile.setTentacleSlot(freeSlot);

            Vec3 look = player.getLookAngle();

            projectile.setPos(
                    player.getX() + look.x * 1.2D,
                    player.getEyeY() - 0.1D,
                    player.getZ() + look.z * 1.2D
            );

            projectile.shoot(look.x, look.y, look.z, 1.6F, 0.0F);

            player.level().addFreshEntity(projectile);
            playTentacleLaunchSound(player);
            player.swing(InteractionHand.MAIN_HAND, true);

            sendTentacleStatus(player, data, maxTentacles);
        });

        ctx.get().setPacketHandled(true);
    }

    private static boolean recallGrabbedTentacles(ServerPlayer player, CompoundTag data, int maxTentacles) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        AABB searchBox = player.getBoundingBox().inflate(96.0D);

        List<TentacleProjectile> tentacles = serverLevel.getEntitiesOfClass(
                TentacleProjectile.class,
                searchBox,
                tentacle -> tentacle.isAlive() && tentacle.getOwnerById() == player
        );

        boolean recalledAny = false;

        for (TentacleProjectile tentacleProjectile : tentacles) {
            int slot = tentacleProjectile.getTentacleSlot();

            if (slot < 1 || slot > maxTentacles) {
                continue;
            }

            if (!STATE_DEPLOYED.equals(getSlotState(data, slot))) {
                continue;
            }

            if (tentacleProjectile.getVictimById() == null) {
                continue;
            }

            Tentacle attachedTentacle = tentacleProjectile.getActiveTentacleEntity();
            if (attachedTentacle != null && attachedTentacle.wasRecentlyDamaged(RECALL_DAMAGE_IMMUNITY_TICKS)) {
                continue;
            }

            tentacleProjectile.releaseVictimAndReturn();
            recalledAny = true;
        }

        if (recalledAny) {
            sendTentacleStatus(player, data, maxTentacles);
        }

        return recalledAny;
    }

    public static int getMaxTentaclesForLevel(int level) {
        if (level >= 7) {
            return 3;
        }
        if (level >= 4) {
            return 2;
        }
        return 1;
    }

    public static void normalizeSlotStates(CompoundTag data) {
        for (int i = 1; i <= ABSOLUTE_MAX_TENTACLES; i++) {
            String state = getSlotState(data, i);
            if (!isValidState(state)) {
                setSlotState(data, i, STATE_READY);
            }
        }
    }

    public static int findFirstReadySlot(CompoundTag data, int maxTentacles) {
        for (int i = 1; i <= maxTentacles; i++) {
            if (STATE_READY.equals(getSlotState(data, i))) {
                return i;
            }
        }
        return -1;
    }

    public static String getSlotState(CompoundTag data, int slot) {
        return data.getString(getSlotTag(slot));
    }

    public static void setSlotState(CompoundTag data, int slot, String state) {
        data.putString(getSlotTag(slot), state);
    }

    public static int countActiveSlots(CompoundTag data, int maxTentacles) {
        int count = 0;
        for (int i = 1; i <= maxTentacles; i++) {
            String state = getSlotState(data, i);
            if (STATE_DEPLOYED.equals(state) || STATE_RETURNING.equals(state)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isValidState(String state) {
        return STATE_READY.equals(state)
                || STATE_DEPLOYED.equals(state)
                || STATE_RETURNING.equals(state)
                || STATE_DEAD.equals(state);
    }

    private static String getSlotTag(int slot) {
        return switch (slot) {
            case 1 -> SLOT_1_TAG;
            case 2 -> SLOT_2_TAG;
            case 3 -> SLOT_3_TAG;
            default -> throw new IllegalArgumentException("Invalid tentacle slot: " + slot);
        };
    }

    private static Component getDisplayState(CompoundTag data, int slot, String state) {
        return switch (state) {
            case STATE_DEPLOYED -> Component.translatable("message.sporeadds.tentacle.state.deployed")
                    .withStyle(ChatFormatting.GOLD);

            case STATE_RETURNING -> Component.translatable("message.sporeadds.tentacle.state.returning")
                    .withStyle(ChatFormatting.YELLOW);

            case STATE_DEAD -> {
                int ticks = AbyssalTentacleRegenEvents.getRegenTimer(data, slot);
                int seconds = Math.max(0, (ticks + 19) / 20);
                yield Component.translatable("message.sporeadds.tentacle.state.dead", seconds)
                        .withStyle(ChatFormatting.RED);
            }

            default -> Component.translatable("message.sporeadds.tentacle.state.ready")
                    .withStyle(ChatFormatting.GREEN);
        };
    }

    public static void sendTentacleStatus(ServerPlayer player, CompoundTag data, int maxTentacles) {
        Component[] slotComponents = new Component[maxTentacles];

        for (int i = 1; i <= maxTentacles; i++) {
            String state = getSlotState(data, i);
            slotComponents[i - 1] = Component.translatable(
                    "message.sporeadds.tentacle.slot_status",
                    i,
                    getDisplayState(data, i, state)
            );
        }

        Component message;
        int active = countActiveSlots(data, maxTentacles);

        if (maxTentacles == 1) {
            message = Component.translatable(
                    "message.sporeadds.tentacle.status.1",
                    slotComponents[0],
                    active,
                    maxTentacles
            );
        } else if (maxTentacles == 2) {
            message = Component.translatable(
                    "message.sporeadds.tentacle.status.2",
                    slotComponents[0],
                    slotComponents[1],
                    active,
                    maxTentacles
            );
        } else {
            message = Component.translatable(
                    "message.sporeadds.tentacle.status.3",
                    slotComponents[0],
                    slotComponents[1],
                    slotComponents[2],
                    active,
                    maxTentacles
            );
        }

        player.displayClientMessage(message, true);
    }

    private static void playTentacleLaunchSound(ServerPlayer player) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(TENTACLE_LAUNCH_SOUND_ID);
        if (sound != null) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    sound,
                    SoundSource.PLAYERS,
                    TENTACLE_LAUNCH_SOUND_VOLUME,
                    TENTACLE_LAUNCH_SOUND_PITCH
            );
        }
    }

    public static void shootRegenTentacle(ServerPlayer player, int slot) {
        TentacleProjectile projectile = new TentacleProjectile(
                ModEntities.TENTACLE_PROJECTILE.get(),
                player.level(),
                player,
                0.0F
        );

        projectile.setTentacleSlot(slot);
        setSlotState(player.getPersistentData(), slot, STATE_RETURNING);

        Vec3 look = player.getLookAngle();

        projectile.setPos(
                player.getX() - look.x * 0.5D,
                player.getEyeY() - 0.1D,
                player.getZ() - look.z * 0.5D
        );

        projectile.shoot(-look.x, -look.y, -look.z, 0.3F, 0.0F);

        player.level().addFreshEntity(projectile);
    }
}