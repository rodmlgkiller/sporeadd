package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber
public class SporeDefenseHandler {

    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) return;

        // Comprobar que el que recibe el golpe es un jugador (Defensor)
        if (!(event.getEntity() instanceof ServerPlayer defender)) return;

        // Comprobar que el atacante (quien originó el daño) es otro jugador
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;

        // Verificar si el atacante tiene "spore:uneasy"
        MobEffect uneasy = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("spore", "uneasy"));
        if (uneasy == null || !attacker.hasEffect(uneasy)) return;

        // Verificar que el defensor está en el equipo "spore"
        if (defender.getTeam() == null || !defender.getTeam().getName().equals("spore")) return;

        // Verificar que el defensor es nivel 7 o mayor
        PlayerLevelProvider.PLAYER_LVL.get(defender).ifPresent(levelCap -> {
            if (levelCap.getLevel() >= 7) {

                // 30% de probabilidad de bloquear el ataque
                if (Math.random() < 0.30) {

                    // 1. Cancelar el daño (Funciona para cuerpo a cuerpo y a distancia)
                    event.setCanceled(true);

                    // 2. Reproducir el sonido del escudo bloqueando
                    SoundEvent shieldBash = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "shield_bash"));
                    if (shieldBash != null) {
                        defender.level().playSound(null, defender.blockPosition(), shieldBash, SoundSource.PLAYERS, 1.0F, 2.0F);
                    }

                    // 3. SOLO SI ES CUERPO A CUERPO: Empujar al atacante y mostrar mensaje
                    // Si el atacante directo es el mismo jugador, fue a puños o con espada.
                    if (event.getSource().getDirectEntity() == attacker) {

                        // Calcular empuje (knockback) alejándolo del defensor
                        double dX = defender.getX() - attacker.getX();
                        double dZ = defender.getZ() - attacker.getZ();

                        // Aplicar knockback con fuerza 1.5D (~3 bloques)
                        attacker.knockback(1.5D, dX, dZ);
                        attacker.hurtMarked = true;

                        // Enviar mensaje a la Action Bar (encima del inventario)
                        attacker.displayClientMessage(
                                Component.translatable("message.sporeadd.general.attack_repelled").withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                }
            }
        });
    }
}