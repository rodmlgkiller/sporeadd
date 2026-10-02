package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.Powers.bile.gluttonousAbilityHandler;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class KommandantDietEvents {

    private static final ResourceLocation BIOMASS_ID = ResourceLocation.fromNamespaceAndPath("spore", "biomass");

    private static final int DEFAULT_FAKE_FOOD = 2;
    private static final float DEFAULT_FAKE_SATURATION = 1.0F;

    private static final Map<ResourceLocation, ManualgluttonousFoodData> MANUAL_gluttonous_FOODS = new HashMap<>();

    static {
        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "claw_fragment"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 0, 0, 1));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "claw"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 0, 0, 1));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "armor_fragment"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 0, 0, 1));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "mutated_heart"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 1, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "wing_membrane"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "fleshy_bone"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 1, 0, 0, 1, 2));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "hardened_bind"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 4, 0, 0, 0, 4));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "fleshy_claw"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 2, 0, 0, 0, 5));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "living_core"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 6, 0, 1, 0, 4));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "spine_fragment"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 0, 1, 3));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "nerves"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 1, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "cerebrum"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 1, 3, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "spine"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 6, 3, 9));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "armor_plate"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 3, 0, 2, 0, 4));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "plated_muscle"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 11, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "alveolic_sack"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 2, 6, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "altered_spleen"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 6, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "corrosive_sack"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 2, 6, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "organoid_membrane"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 3, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "tendons"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "innards"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 1, 3, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "sickle_fragment"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 0, 0, 1));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "fang"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 0, 0, 1));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "spike"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 0, 0, 1));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "shield_fragment"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 0, 2, 5));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "wing"),
                new ManualgluttonousFoodData(2, 1.0F, 7, 14, 0, 0, 3, 11));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "tumor"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "sicken_tumor"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 1, 2, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "calcified_tumor"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 1, 0, 0, 1, 2));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "gluttonous_tumor"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 2, 4, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "reforged_biomass_t"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 1, 2));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "reforged_biomass_w"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 1, 2));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "acidic_gland"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 10, 20, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "amalgamated_heart"),
                new ManualgluttonousFoodData(2, 1.0F, 15, 30, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "ligaments"),
                new ManualgluttonousFoodData(2, 1.0F, 10, 20, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "fins"),
                new ManualgluttonousFoodData(2, 1.0F, 10, 20, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "hyperbolized_liver"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 15, 30, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "respirator"),
                new ManualgluttonousFoodData(2, 1.0F, 3, 6, 2, 4, 0, 2));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "mutated_fiber"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        // Comidas reales
        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "sausage"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 3, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "fiber_stew"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 3, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "heart_kebab"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 1, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "roasted_heart_kebab"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 3, 2, 3, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "roasted_tumor"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 3, 1, 2, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "vigil_eye_soup"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 1, 1, 2, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "milky_sack"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 3, 7, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "brain_noodles"),
                new ManualgluttonousFoodData(2, 1.0F, 0, 0, 0, 4, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "fried_wing_membrane"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 3, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "biomass_bacon"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "tendon_gum"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 1, 1, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "organoid_soup"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 4, 0, 2, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "fungal_sauce"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 1, 1, 2, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "slice_of_heartpie"),
                new ManualgluttonousFoodData(2, 1.0F, 3, 5, 1, 2, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "fungal_burger"),
                new ManualgluttonousFoodData(2, 1.0F, 7, 13, 2, 5, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "fleshy_ribs"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 2, 3));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "amalgamated_roast"),
                new ManualgluttonousFoodData(2, 1.0F, 3, 5, 2, 4, 1, 3));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "eldritch_sushi"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 4, 3, 5, 1, 3));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "spore_stuffed_abomination"),
                new ManualgluttonousFoodData(2, 1.0F, 3, 5, 2, 4, 1, 3));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "decayed_torso"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 4, 0, 0, 2, 5));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "stuffed_torso"),
                new ManualgluttonousFoodData(2, 1.0F, 3, 5, 3, 6, 2, 5));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "decayed_limbs"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 3, 0, 0, 1, 2));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "skull_soup"),
                new ManualgluttonousFoodData(2, 1.0F, 2, 3, 2, 3, 2, 3));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("spore", "cooked_torso"),
                new ManualgluttonousFoodData(2, 1.0F, 5, 7, 5, 7, 4, 6));
        // Vanilla meats / flesh foods -> 1-2 gore
        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "beef"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "steak"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "porkchop"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_porkchop"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "mutton"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_mutton"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "chicken"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_chicken"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "rabbit"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_rabbit"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "rabbit_stew"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "cod"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_cod"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "salmon"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_salmon"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "rotten_flesh"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "spider_eye"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_beef"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));

        registerManualFood(ResourceLocation.fromNamespaceAndPath("minecraft", "tropical_fish"),
                new ManualgluttonousFoodData(2, 1.0F, 1, 2, 0, 0, 0, 0));
    }

    private static void registerManualFood(ResourceLocation id, ManualgluttonousFoodData data) {
        MANUAL_gluttonous_FOODS.put(id, data);
    }

    private record ManualgluttonousFoodData(
            int food,
            float saturation,
            int goreMin,
            int goreMax,
            int gluttonousMin,
            int gluttonousMax,
            int boneMin,
            int boneMax
    ) {}

    private static boolean isKommandant(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isSubclassgluttonous(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "gluttonous".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);
    }

    private static boolean isBiomass(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return BIOMASS_ID.equals(key);
    }

    private static ResourceLocation getItemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static ManualgluttonousFoodData getManualFood(ItemStack stack) {
        ResourceLocation key = getItemId(stack);
        return key == null ? null : MANUAL_gluttonous_FOODS.get(key);
    }

    private static boolean isManualgluttonousFood(ItemStack stack) {
        return getManualFood(stack) != null;
    }

    private static boolean isInstantgluttonousConsumable(Player player, ItemStack stack) {
        if (!isKommandant(player) || !isSubclassgluttonous(player)) return false;
        return stack.getItem() == Items.BONE || stack.isEdible() || isBiomass(stack) || isManualgluttonousFood(stack);
    }

    private static boolean isForbiddenFood(Player player, ItemStack stack) {
        if (!SporeAddsConfig.KOMMANDANT_DIET_RESTRICTION.get()) return false;

        if (isSubclassgluttonous(player)) return false;

        if (stack.getItem() == Items.BONE) return false;
        if (isBiomass(stack)) return false;

        if (!stack.isEdible()) return false;

        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) return false;

        List<? extends String> whitelist = SporeAddsConfig.KOMMANDANT_EDIBLE_ITEMS.get();
        return !whitelist.contains(key.toString());
    }

    private static void spawnFakeEatParticles(ServerPlayer player, ItemStack particleStack) {
        double px = player.getX();
        double py = player.getY() + player.getBbHeight() * 0.78D;
        double pz = player.getZ();

        player.serverLevel().sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                px, py, pz,
                14,
                0.32D, 0.20D, 0.32D,
                0.08D
        );
    }

    private static void playEatSound(ServerPlayer player) {
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS,
                1.0F,
                0.95F + (player.getRandom().nextFloat() * 0.15F)
        );
    }

    private static void feedLikeBone(ServerPlayer player, int food, float saturation) {
        player.getFoodData().eat(food, saturation);
    }

    private static int rollRange(ServerPlayer player, int min, int max) {
        if (max < min) return min;
        return player.getRandom().nextIntBetweenInclusive(min, max);
    }

    private static void grantManualRewards(ServerPlayer player, ManualgluttonousFoodData data) {
        int gore = rollRange(player, data.goreMin(), data.goreMax());
        int gluttonous = rollRange(player, data.gluttonousMin(), data.gluttonousMax());
        int bone = rollRange(player, data.boneMin(), data.boneMax());

        for (int i = 0; i < gore; i++) {
            gluttonousAbilityHandler.addGoreConsumed(player);
        }

        for (int i = 0; i < gluttonous; i++) {
            gluttonousAbilityHandler.addGenericFoodConsumed(player);
        }

        for (int i = 0; i < bone; i++) {
            gluttonousAbilityHandler.addBoneConsumed(player);
        }
    }

    private static void consumeManualNonEdible(ServerPlayer player, InteractionHand hand, ItemStack stack, ManualgluttonousFoodData data) {
        feedLikeBone(player, data.food(), data.saturation());
        playEatSound(player);
        spawnFakeEatParticles(player, stack.copyWithCount(1));
        grantManualRewards(player, data);

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        player.swing(hand, true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        InteractionHand hand = event.getHand();

        if (!isKommandant(player)) return;

        if (isForbiddenFood(player, stack)) {
            event.setCanceled(true);
            return;
        }

        if (!isSubclassgluttonous(player)) return;

        ManualgluttonousFoodData manualFood = getManualFood(stack);

        if (stack.getItem() == Items.BONE) {
            if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                feedLikeBone(serverPlayer, DEFAULT_FAKE_FOOD, DEFAULT_FAKE_SATURATION);
                playEatSound(serverPlayer);
                spawnFakeEatParticles(serverPlayer, new ItemStack(Items.BONE));

                gluttonousAbilityHandler.addBoneConsumed(serverPlayer);
                gluttonousAbilityHandler.addBoneConsumed(serverPlayer);

                if (!serverPlayer.isCreative()) {
                    stack.shrink(1);
                }
            }

            player.swing(hand, true);
            event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
            event.setCanceled(true);
            return;
        }

        if (manualFood != null && !stack.isEdible()) {
            if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                consumeManualNonEdible(serverPlayer, hand, stack, manualFood);
            }

            event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
            event.setCanceled(true);
            return;
        }

        if (stack.isEdible() || isBiomass(stack)) {
            player.startUsingItem(hand);
            event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack stack = event.getItem();

        if (!isKommandant(player)) return;

        if (isForbiddenFood(player, stack)) {
            event.setCanceled(true);
            event.setDuration(-1);
            return;
        }

        if (isInstantgluttonousConsumable(player, stack)) {
            event.setDuration(1);
        }
    }

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItem();

        if (isKommandant(player) && isSubclassgluttonous(player)) {
            if (stack.isEdible() || isBiomass(stack)) {
                ManualgluttonousFoodData manualFood = getManualFood(stack);

                if (manualFood != null) {
                    grantManualRewards(player, manualFood);
                } else {
                    gluttonousAbilityHandler.addGenericFoodConsumed(player);
                }
            }
        }
    }
}