package com.sporeadds.sporeaddsmod.Powers;

import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Poder5 {

    // Debounce between two uses: kommandant_abilities.power_5_manipulation.use_cooldown_ms (default 200).
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerRightClickBlockPower4(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        long now = System.currentTimeMillis();
        UUID playerUUID = player.getUUID();
        long lastUse = cooldowns.getOrDefault(playerUUID, 0L);

        if (now - lastUse < (long) KommandantPowersConfig.MANIPULATION_COOLDOWN_MS.get()) {
            return;
        }

        BlockPos pos = event.getPos();
        Block clickedBlock = player.level().getBlockState(pos).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(clickedBlock);

        if (blockId != null && KommandantPowersConfig.isHarvestableBlock(blockId) && player.isCrouching()) {
            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                if (data.getSwitch().length() > 5 && data.getSwitch().charAt(5) == '1'
                        && PowerUnlocks.isUnlocked(player, 5)) {
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
                            ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft", "item.pickup")),
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

        // Right-clicking a block of this list is cancelled for every player (as it always was), so only list living blocks.
        if (blockId != null && KommandantPowersConfig.isFeedableBlock(blockId)) {
            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                if (data.getSwitch().length() > 5 && data.getSwitch().charAt(5) == '1'
                        && PowerUnlocks.isUnlocked(player, 5)) {
                    player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
                        if (player.level().getBlockEntity(pos) instanceof com.Harbinger.Spore.SBlockEntities.LivingStructureBlocks be) {

                            int feedCost = KommandantPowersConfig.FEED_BIOMASS_COST.get();
                            if (spore.getSpore() >= feedCost) {
                                spore.addSpore(-feedCost);
                                be.addKills();

                                player.sendSystemMessage(
                                        Component.translatable("message.sporeadd.power5.block_biomass", be.getKills())
                                                .withStyle(ChatFormatting.DARK_RED)
                                );

                                player.level().playSound(
                                        null,
                                        player.blockPosition(),
                                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "spit")),
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

    /**
     * Ability 5 on a Spore mound entity (power_5_manipulation.feed_mounds, default off): costs feed_biomass_cost and the mound grows one
     * age stage. Called by {@link PowerEntityInteractResolver}; returns true when the click was used.
     */
    public static boolean tryFeedMound(Player player, Entity target) {
        if (!KommandantPowersConfig.FEED_MOUNDS.get()) return false;
        if (player.level().isClientSide()) return false;
        if (!(target instanceof Mound mound)) return false;

        AtomicBoolean active = new AtomicBoolean(false);
        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            if (data.getSwitch().length() > 5 && data.getSwitch().charAt(5) == '1' && PowerUnlocks.isUnlocked(player, 5)) {
                active.set(true);
            }
        });
        if (!active.get()) return false;

        UUID playerUUID = player.getUUID();
        long now = System.currentTimeMillis();
        if (now - cooldowns.getOrDefault(playerUUID, 0L) < (long) KommandantPowersConfig.MANIPULATION_COOLDOWN_MS.get()) {
            return true;
        }

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            int age = mound.getAge();
            int maxAge = mound.getMaxAge();
            if (age >= maxAge) {
                player.sendSystemMessage(Component.translatableWithFallback(
                        "message.sporeadd.power5.mound_full",
                        "This mound cannot grow any further.").withStyle(ChatFormatting.DARK_RED));
                cooldowns.put(playerUUID, now);
                return;
            }

            int feedCost = KommandantPowersConfig.FEED_BIOMASS_COST.get();
            if (spore.getSpore() < feedCost) {
                player.sendSystemMessage(
                        Component.translatable("message.sporeadd.power1.not_enough_biomass")
                                .withStyle(ChatFormatting.DARK_RED)
                );
                cooldowns.put(playerUUID, now);
                return;
            }

            spore.addSpore(-feedCost);
            mound.setAge(age + 1);
            player.sendSystemMessage(Component.translatableWithFallback(
                    "message.sporeadd.power5.mound_fed",
                    "The mound feeds on your biomass and grows (stage %s of %s).",
                    age + 1, maxAge).withStyle(ChatFormatting.DARK_RED));

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "spit")),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
            cooldowns.put(playerUUID, now);
        });

        return true;
    }
}