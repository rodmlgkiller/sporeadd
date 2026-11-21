package com.sporeadds.sporeaddsmod.Powers;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

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
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class Poder5 {

    // Debounce between two uses: kommandant_abilities.power_5_manipulation.use_cooldown_ms (default 200).
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerRightClickBlockPower4(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        long now = System.currentTimeMillis();
        if (now - cooldowns.getOrDefault(player.getUUID(), 0L) < (long) KommandantPowersConfig.MANIPULATION_COOLDOWN_MS.get()) {
            return;
        }

        BlockPos pos = event.getPos();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(pos).getBlock());

        // Harvesting is Shift + Use by default; feeding is plain Use. Either can be rebound to its own key (Controls >
        // SporeAdds), then the Use click no longer triggers it (SkillBindings).
        if (blockId != null && KommandantPowersConfig.isHarvestableBlock(blockId)
                && SkillBindings.useClickTriggers(player, SkillClick.POWER5_HARVEST)) {
            if (harvestBlock(player, pos, now)) {
                event.setCanceled(true);
            }
            return;
        }

        // Right-clicking a block of this list is cancelled for every player (as it always was), so only list living blocks.
        if (blockId != null && KommandantPowersConfig.isFeedableBlock(blockId)
                && SkillBindings.useClickTriggers(player, SkillClick.POWER5_FEED)) {
            feedBlock(player, pos, now);
            event.setCanceled(true);
        }
    }

    /** The harvest click on a block, from the skill's own key (SkillClickDispatcher). */
    public static boolean tryHarvestBlock(Player player, BlockPos pos) {
        long now = System.currentTimeMillis();
        if (now - cooldowns.getOrDefault(player.getUUID(), 0L) < (long) KommandantPowersConfig.MANIPULATION_COOLDOWN_MS.get()) {
            return false;
        }
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(pos).getBlock());
        return blockId != null && KommandantPowersConfig.isHarvestableBlock(blockId) && harvestBlock(player, pos, now);
    }

    /** The feeding click on a block, from the skill's own key (SkillClickDispatcher). */
    public static boolean tryFeedBlock(Player player, BlockPos pos) {
        long now = System.currentTimeMillis();
        if (now - cooldowns.getOrDefault(player.getUUID(), 0L) < (long) KommandantPowersConfig.MANIPULATION_COOLDOWN_MS.get()) {
            return false;
        }
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(pos).getBlock());
        if (blockId == null || !KommandantPowersConfig.isFeedableBlock(blockId)) {
            return false;
        }
        feedBlock(player, pos, now);
        return true;
    }

    /** Collects the clicked block into the inventory when power 5 is on. Returns true when it did. */
    private static boolean harvestBlock(Player player, BlockPos pos, long now) {
        Block clickedBlock = player.level().getBlockState(pos).getBlock();
        boolean[] harvested = {false};
        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
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
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("minecraft", "item.pickup")),
                        SoundSource.PLAYERS,
                        0.8F,
                        1.2F
                );

                cooldowns.put(player.getUUID(), now);
                harvested[0] = true;
            }
        });
        return harvested[0];
    }

    /** Feeds biomass to the living block under the click when power 5 is on. */
    private static void feedBlock(Player player, BlockPos pos, long now) {
        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            if (data.getSwitch().length() > 5 && data.getSwitch().charAt(5) == '1'
                    && PowerUnlocks.isUnlocked(player, 5)) {
                PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
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
                                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "spit")),
                                    SoundSource.PLAYERS,
                                    1.0F,
                                    1.0F
                            );

                            cooldowns.put(player.getUUID(), now);
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
        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
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

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
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
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "spit")),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
            cooldowns.put(playerUUID, now);
        });

        return true;
    }
}