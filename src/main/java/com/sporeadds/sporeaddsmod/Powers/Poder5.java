package com.sporeadds.sporeaddsmod.Powers;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class Poder5 {

    private static final long COOLDOWN_MS = 200;
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerRightClickBlockPower4(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        long now = System.currentTimeMillis();
        UUID playerUUID = player.getUUID();
        long lastUse = cooldowns.getOrDefault(playerUUID, 0L);

        if (now - lastUse < COOLDOWN_MS) {
            return;
        }

        BlockPos pos = event.getPos();
        Block clickedBlock = player.level().getBlockState(pos).getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(clickedBlock);

        Set<ResourceLocation> collectableBlocks = Set.of(
                ResourceLocation.fromNamespaceAndPath("spore", "remains"),
                ResourceLocation.fromNamespaceAndPath("spore", "wall_remains"),
                ResourceLocation.fromNamespaceAndPath("spore", "hive_spawn"),
                ResourceLocation.fromNamespaceAndPath("spore", "biomass_lump")
        );

        if (blockId != null && collectableBlocks.contains(blockId) && player.isCrouching()) {
            PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
                if (data.getSwitch().length() > 5 && data.getSwitch().charAt(5) == '1') {
                    ItemStack itemStack = new ItemStack(clickedBlock);
                    boolean given = player.getInventory().add(itemStack);

                    player.level().removeBlock(pos, false);

                    if (!given) {
                        player.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                                player.level(),
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                itemStack
                        ));
                    }

                    player.level().playSound(
                            null,
                            pos,
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("minecraft", "item.pickup")),
                            SoundSource.PLAYERS,
                            0.8F,
                            1.2F
                    );

                    cooldowns.put(playerUUID, now);
                    event.setCanceled(true);
                }
            });
            return;
        }

        Set<ResourceLocation> biomassBlocks = Set.of(
                ResourceLocation.fromNamespaceAndPath("spore", "hive_spawn"),
                ResourceLocation.fromNamespaceAndPath("spore", "biomass_lump")
        );

        if (blockId != null && biomassBlocks.contains(blockId)) {
            PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
                if (data.getSwitch().length() > 5 && data.getSwitch().charAt(5) == '1') {
                    PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
                        if (player.level().getBlockEntity(pos) instanceof com.Harbinger.Spore.SBlockEntities.LivingStructureBlocks be) {

                            if (spore.getSpore() >= 1) {
                                spore.addSpore(-1);
                                be.addKills();

                                player.sendSystemMessage(
                                        Component.translatable("message.sporeadd.power5.block_biomass", be.getKills())
                                                .withStyle(ChatFormatting.DARK_RED)
                                );

                                player.level().playSound(
                                        null,
                                        player.blockPosition(),
                                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "spit")),
                                        SoundSource.PLAYERS,
                                        1.0F,
                                        1.0F
                                );

                                cooldowns.put(playerUUID, now);
                            } else {
                                player.sendSystemMessage(
                                        Component.translatable("message.sporeadd.power1.not_enough_biomass")
                                                .withStyle(ChatFormatting.DARK_RED)
                                );
                            }
                        }
                    });
                }
            });
            event.setCanceled(true);
        }
    }
}