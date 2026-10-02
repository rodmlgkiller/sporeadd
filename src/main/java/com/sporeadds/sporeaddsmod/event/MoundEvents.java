package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.data.MoundSavedData;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncMoundCountPacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MoundEvents {

    private static final ResourceLocation MOUND_ID = new ResourceLocation("spore", "mound");

    @SubscribeEvent
    public static void onMoundDeath(LivingDeathEvent event) {
        LivingEntity living = event.getEntity();
        if (!(living.level() instanceof ServerLevel serverLevel)) return;

        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(living.getType());
        if (!MOUND_ID.equals(key)) return;

        ChunkPos chunkPos = new ChunkPos(living.blockPosition());
        serverLevel.setChunkForced(chunkPos.x, chunkPos.z, false);

        CompoundTag data = living.getPersistentData();
        if (!data.hasUUID("spore_owner")) return;

        UUID ownerUUID = data.getUUID("spore_owner");
        UUID moundUUID = living.getUUID();

        MoundSavedData savedData = MoundSavedData.get(serverLevel.getServer());
        savedData.removeMound(ownerUUID, moundUUID);

        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null) {
            owner.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
                boolean wasRemoved = spore.getMoundRegistry().remove(moundUUID);

                if (wasRemoved) {
                    CompoundTag nbt = new CompoundTag();
                    spore.saveNBTData(nbt);

                    NetworkHandle.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> owner),
                            new SyncMoundCountPacket(nbt)
                    );
                }
            });
        } else {
            // Dueño offline: dejar constancia para que la fusión de listas al reconectar no lo resucite.
            savedData.noteOfflineRemoval(ownerUUID, moundUUID);
        }

        savedData.persistNow(serverLevel.getServer());
    }
}