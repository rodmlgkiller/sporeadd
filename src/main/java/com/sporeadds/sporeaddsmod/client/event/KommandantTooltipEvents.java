package com.sporeadds.sporeaddsmod.client.event;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KommandantTooltipEvents {

    private static final ResourceLocation BIOMASS_ID = new ResourceLocation("spore", "biomass");

    private static boolean isKommandant(Player player) {
        if (player == null) return false;
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isgluttonous(Player player) {
        if (player == null) return false;
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "gluttonous".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);
    }

    private static boolean isBiomass(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return BIOMASS_ID.equals(key);
    }

    private static boolean isNormallyForbiddenFood(ItemStack stack) {
        if (!SporeAddsConfig.KOMMANDANT_DIET_RESTRICTION.get()) {
            return false;
        }

        if (!stack.isEdible()) {
            return false;
        }

        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) return false;

        String itemId = key.toString();

        if (itemId.contains("remains")) {
            return false;
        }

        List<? extends String> whitelist = SporeAddsConfig.KOMMANDANT_EDIBLE_ITEMS.get();
        return !whitelist.contains(itemId);
    }

    private static boolean isgluttonousOverrideEdible(ItemStack stack) {
        if (stack.getItem() == Items.BONE) return true;
        if (isBiomass(stack)) return true;

        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) return false;

        String id = key.toString();

        return id.equals("spore:claw_fragment")
                || id.equals("spore:claw")
                || id.equals("spore:armor_fragment")
                || id.equals("spore:mutated_heart")
                || id.equals("spore:wing_membrane")
                || id.equals("spore:fleshy_bone")
                || id.equals("spore:hardened_bind")
                || id.equals("spore:fleshy_claw")
                || id.equals("spore:living_core")
                || id.equals("spore:spine_fragment")
                || id.equals("spore:nerves")
                || id.equals("spore:cerebrum")
                || id.equals("spore:spine")
                || id.equals("spore:armor_plate")
                || id.equals("spore:plated_muscle")
                || id.equals("spore:alveolic_sack")
                || id.equals("spore:altered_spleen")
                || id.equals("spore:corrosive_sack")
                || id.equals("spore:organoid_membrane")
                || id.equals("spore:tendons")
                || id.equals("spore:innards")
                || id.equals("spore:sickle_fragment")
                || id.equals("spore:fang")
                || id.equals("spore:spike")
                || id.equals("spore:shield_fragment")
                || id.equals("spore:wing")
                || id.equals("spore:tumor")
                || id.equals("spore:sicken_tumor")
                || id.equals("spore:calcified_tumor")
                || id.equals("spore:gluttonous_tumor")
                || id.equals("spore:reforged_biomass_t")
                || id.equals("spore:reforged_biomass_w")
                || id.equals("spore:acidic_gland")
                || id.equals("spore:amalgamated_heart")
                || id.equals("spore:ligaments")
                || id.equals("spore:fins")
                || id.equals("spore:hyperbolized_liver")
                || id.equals("spore:respirator")
                || id.equals("spore:mutated_fiber");
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            player = Minecraft.getInstance().player;
        }

        if (player == null || !isKommandant(player)) return;

        ItemStack stack = event.getItemStack();

        if (isgluttonous(player) && (isNormallyForbiddenFood(stack) || isgluttonousOverrideEdible(stack))) {
            event.getToolTip().add(
                    Component.translatable("tooltip.sporeadd.edible")
                            .withStyle(ChatFormatting.GOLD)
            );
            return;
        }

        if (isNormallyForbiddenFood(stack)) {
            event.getToolTip().add(
                    Component.translatable("tooltip.sporeadd.inedible")
                            .withStyle(ChatFormatting.RED)
            );
        }
    }
}