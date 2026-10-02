package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Organoids.Vigil;
import com.sporeadds.sporeaddsmod.commands.VervaTransportTask;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(targets = "com.Harbinger.Spore.Sentities.Organoids.Vigil$WatchTargetGoat")
public abstract class VigilMixin {

    @Unique
    private static final Set<UUID> SPOREADD_PROCESSED_VIGILS = new HashSet<>();

    @Shadow(remap = false)
    private Vigil vigil;

    @Inject(method = "m_8037_()V", at = @At("TAIL"), remap = false)
    private void onWatchTargetGoatStart(CallbackInfo ci) {
        try {
            if (this.vigil == null || this.vigil.level().isClientSide) return;

            Entity target = this.vigil.getTarget();
            if (!(target instanceof ServerPlayer survivor)) return;

            if (SPOREADD_PROCESSED_VIGILS.contains(this.vigil.getUUID())) return;

            net.minecraft.server.MinecraftServer server = this.vigil.getServer();
            if (server == null) return;

            boolean foundKommandant = false;

            for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
                if (!SporeClassUtil.hasClass(onlinePlayer, "kommandant")) {
                    continue;
                }

                final boolean[] isLevel7 = {false};
                PlayerLevelProvider.PLAYER_LVL.get(onlinePlayer).ifPresent(levelCap -> {
                    if (levelCap.getLevel() >= 7) {
                        isLevel7[0] = true;
                    }
                });

                if (isLevel7[0]) {
                    foundKommandant = true;

                    String coords = String.format("[%.0f, %.0f, %.0f]", survivor.getX(), survivor.getY(), survivor.getZ());

                    MutableComponent msg = Component.translatable(
                            "message.sporeadd.vigil.survivor_located",
                            survivor.getName().getString(),
                            coords
                    );

                    onlinePlayer.sendSystemMessage(msg.withStyle(ChatFormatting.DARK_RED));

                    VervaTransportTask.startTask(
                            onlinePlayer,
                            onlinePlayer.position(),
                            this.vigil.position(),
                            (ServerLevel) onlinePlayer.level()
                    );
                }
            }

            if (foundKommandant) {
                SPOREADD_PROCESSED_VIGILS.add(this.vigil.getUUID());
            }
        } catch (Exception e) {
            // Ignoramos silenciosamente para no interrumpir el código del Vigil
        }
    }
}