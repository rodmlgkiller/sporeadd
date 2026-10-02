package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncMoundCountPacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.network.PacketDistributor;

public class SporeMoundSummoner {

    public static void spawnMound(ServerPlayer player) {
        var cap = player.getCapability(PlayerSporeProvider.PLAYER_CAP);

        cap.ifPresent(spore -> {

            if (spore.getPhase() >= 50) {
                spore.setPhase(spore.getPhase() - 50);

                ServerLevel level = player.serverLevel();

                double alcance = 20.0D;
                Vec3 startVec = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle();
                Vec3 endVec = startVec.add(lookVec.x * alcance, lookVec.y * alcance, lookVec.z * alcance);

                BlockHitResult hitResult = level.clip(new ClipContext(
                        startVec, endVec,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                ));

                BlockPos spawnPos;
                String debugLook;
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    spawnPos = hitResult.getBlockPos().above();
                    debugLook = "" + spawnPos;
                } else {
                    spawnPos = BlockPos.containing(endVec);
                    debugLook = "" + spawnPos;
                }

                EntityType<?> moundType = BuiltInRegistries.ENTITY_TYPE.get(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("spore", "mound")
                );

                if (moundType == null) {
                    return;
                }

                var entity = moundType.create(level);
                if (!(entity instanceof Mob mound)) {
                    return;
                }

                mound.moveTo(
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        player.getYRot(), player.getXRot()
                );
                mound.position();

                var difficulty = level.getCurrentDifficultyAt(spawnPos);
                mound.finalizeSpawn(level, difficulty, MobSpawnType.MOB_SUMMONED, null, null);

                // Configuración de datos persistentes
                mound.getPersistentData().putInt("max_age", 4);
                mound.getPersistentData().putInt("age", 1);
                mound.getPersistentData().putBoolean("core", true); // ← NUEVA TAG "core"

                mound.setGlowingTag(true);
                mound.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE, -1, 3, false, false
                ));

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

                // NEW: Force-load the chunk where the mound spawns
                ChunkPos chunkPos = new ChunkPos(mound.blockPosition());
                level.setChunkForced(chunkPos.x, chunkPos.z, true);

                // Es importante recordar chunkPos y nivel para poder liberar el chunk después

                // REGISTRO DE UUID EN EL JUGADOR
                spore.getMoundRegistry().add(mound.getUUID());
                spore.getMoundRegistry().getList().toString();

                // >>>>> Fin del método original <<<<<

                // El chunk se liberará automáticamente usando un handler global en tu mod, por ejemplo:
                // véase MoundChunkReleaseEvents.java, para liberar chunk cuando el mound muere/desaparece

            } else {
                player.sendSystemMessage(Component.literal("§4Not enough biomass"));
            }
            CompoundTag nbt = new CompoundTag();
            spore.saveNBTData(nbt);
            NetworkHandle.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncMoundCountPacket(nbt));
        });
        if (!cap.isPresent()) {
        }
    }
}
