package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.data.MoundSavedData;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncMoundCountPacket;
import com.sporeadds.sporeaddsmod.network.SyncSporePacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.UUID;

public class Poder3 extends PowerBase {
    public static final java.util.Map<java.util.UUID, String> MOUND_CACHE = new java.util.HashMap<>();

    @Override
    public void use(ServerPlayer player) {
        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            int currentSpore = spore.getSpore();

            if (currentSpore < 15) {
                player.sendSystemMessage(Component.translatable("message.sporeadd.power1.not_enough_biomass"));
                return;
            }

            spore.setSpore(currentSpore - 15);

            ServerLevel level = player.serverLevel();
            double alcance = 20.0D;
            Vec3 startVec = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 endVec = startVec.add(lookVec.x * alcance, lookVec.y * alcance, lookVec.z * alcance);

            BlockHitResult hitResult = level.clip(new ClipContext(
                    startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
            ));

            BlockPos spawnPos = (hitResult.getType() == HitResult.Type.BLOCK)
                    ? hitResult.getBlockPos().above()
                    : BlockPos.containing(endVec);

            EntityType<?> moundType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("spore", "mound"));

            if (moundType != null) {
                var entity = moundType.create(level);
                if (entity instanceof Mob mound) {
                    mound.moveTo(
                            spawnPos.getX() + 0.5,
                            spawnPos.getY(),
                            spawnPos.getZ() + 0.5,
                            player.getYRot(),
                            player.getXRot()
                    );

                    var difficulty = level.getCurrentDifficultyAt(spawnPos);
                    mound.finalizeSpawn(level, difficulty, MobSpawnType.MOB_SUMMONED, null, null);

                    UUID ownerUUID = player.getUUID();
                    UUID moundUUID = mound.getUUID();

                    mound.getPersistentData().putInt("max_age", 4);
                    mound.getPersistentData().putInt("age", 1);
                    mound.getPersistentData().putBoolean("core", true);
                    mound.getPersistentData().putUUID("spore_owner", ownerUUID);
                    mound.getPersistentData().putBoolean("SporeAdds_NoHardFloorDespawn", true);
                    mound.getPersistentData().putString("SporeAdds_SpawnSource", "Poder3Mound");

                    mound.setGlowingTag(true);
                    mound.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 3, false, false));

                    var server = level.getServer();
                    if (server != null) {
                        var scoreboard = server.getScoreboard();
                        PlayerTeam team = scoreboard.getPlayerTeam("spore");
                        if (team == null) {
                            team = scoreboard.addPlayerTeam("spore");
                            team.setColor(ChatFormatting.RED);
                        }
                        scoreboard.addPlayerToTeam(mound.getScoreboardName(), team);
                    }

                    level.addFreshEntity(mound);

                    ChunkPos chunkPos = new ChunkPos(mound.blockPosition());
                    level.setChunkForced(chunkPos.x, chunkPos.z, true);

                    spore.getMoundRegistry().add(moundUUID);

                    MoundSavedData savedData = MoundSavedData.get(level.getServer());
                    savedData.addMound(
                            ownerUUID,
                            moundUUID,
                            level.dimension(),
                            chunkPos
                    );
                    savedData.persistNow(level.getServer());
                }
            }

            SyncSporePacket.syncManaToClient(player);

            CompoundTag nbt = new CompoundTag();
            spore.saveNBTData(nbt);
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncMoundCountPacket(nbt)
            );
        });
    }
}