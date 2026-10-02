package com.sporeadds.mixin;

import com.Harbinger.Spore.core.SConfig;
import com.Harbinger.Spore.SBlockEntities.OvergrownSpawnerEntity;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = OvergrownSpawnerEntity.class, remap = false)
public class OvergrownSpawnerFeedMixin {

    // Nos inyectamos al final del método "feed" (sin LocalCapture)
    @Inject(method = "feed", at = @At("RETURN"))
    private void sporeadds$feedSporePlayers(Level level, BlockPos blockPos, CallbackInfo ci) {
        if (level.isClientSide) return;

        // Replicamos la lógica original del mod para obtener el radio y el área
        int range = 2 * SConfig.DATAGEN.spawner_range.get();
        AABB aabb = AABB.ofSize(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()), range, range, range);

        // Volvemos a obtener los jugadores en el área (usamos ServerPlayer en lugar de LivingEntity
        // directamente para optimizar y no pedir todos los mobs del área de nuevo).
        List<ServerPlayer> playersInArea = level.getEntitiesOfClass(ServerPlayer.class, aabb);

        for (ServerPlayer player : playersInArea) {
            // Comprobamos si el jugador está en el equipo "spore"
            PlayerTeam team = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
            if (team != null && "spore".equalsIgnoreCase(team.getName())) {

                // 1. Dar +15 de biomasa (Spore)
                player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                    sporeCap.addSpore(15);
                });

                // 2. Dar efecto de Saturación por 10 segundos
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 200, 0, false, false));

                // 3. Enviar mensaje de chat en Dark Red (Rojo Oscuro) en la barra de acción
                player.displayClientMessage(
                                Component.translatable("message.sporeadd.general.spawner_feeds")
                                .withStyle(ChatFormatting.DARK_RED),
                        true
                );
            }
        }
    }
}