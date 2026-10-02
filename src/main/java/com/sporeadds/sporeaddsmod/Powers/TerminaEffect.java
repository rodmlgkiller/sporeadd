package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.Damage.Damagetypes2;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TerminaEffect extends MobEffect {

    private static final Random RAND = new Random();
    private static final String[] PHRASES = {
            "You feel sick...",
            "Something is very wrong...",
            "Your head is spinning...",
            "You sense something is very wrong...",
            "Why can't you breathe?",
            "Your heart skips a beat...",
            "Shadows twist at the edge of your vision...",
            "You hear someone whispering...",
            "It's getting harder to think...",
            "You want to puke...",
            "Something is crawling beneath your skin...",
            "Its over?",
            "It hurts",
            "You coughed blood...",
            "Your hands are trembling...",
            "You feel watched...",
            "Color drains from the world...",
            "You feel fuzzy...",
            "The air tastes metallic...",
            "You fight to breathe..."
    };

    // UUID único para el modificador de vida máximo
    private static final UUID TERMINA_HEALTH_MODIFIER_UUID = UUID.fromString("2b7b8e09-4e09-4c8c-967f-7b2dbe0c53ab");

    public TerminaEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A38B3);
        addAttributeModifier(
                net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,
                TERMINA_HEALTH_MODIFIER_UUID.toString(),
                -8.0D,
                AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Cada 10 segundos (200 ticks)
        return duration % 200 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof ServerPlayer player) {
            // 5% chance: mensaje actionbar
            if (RAND.nextDouble() < 0.05) {
                String phrase = PHRASES[RAND.nextInt(PHRASES.length)];
                player.displayClientMessage(Component.literal(phrase), true);
            }
            // 5% chance: náusea
            if (RAND.nextDouble() < 0.05) {
                var nausea = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("minecraft", "nausea")).orElse(null);
                if (nausea != null) {
                    player.addEffect(new MobEffectInstance(nausea, 20 * 5, 0));
                }
            }
            // 2% chance: daño tipo 'terminal' custom
            if (RAND.nextDouble() < 0.02) {
                player.hurt(Damagetypes2.terminal(player), 2.0F);
            }
        }
        return true;
    }

    @Override
    public List<net.minecraft.world.item.ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributemap, int amplifier) {
        super.removeAttributeModifiers(entity, attributemap, amplifier);

        if (!entity.level().isClientSide && entity instanceof ServerPlayer player) {
            // EXCEPCIÓN: Si tiene el flag de vacuna, no matar ni poner efectos
            if (player.getPersistentData().contains("VaccineBypassTermina")) {
                player.getPersistentData().remove("VaccineBypassTermina");
                player.displayClientMessage(Component.literal("Your symptoms have disappeared!").withStyle(ChatFormatting.GREEN), true);
                return;
            }
            // ---- Resto del código mortal habitual ----
            var myceliumEffect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "mycelium_ef")).orElse(null);
            if (myceliumEffect != null) {
                player.addEffect(new MobEffectInstance(myceliumEffect, 20 * 30, 0));
            }
            // Elimina Totem antes del daño
            var totem = net.minecraft.world.item.Items.TOTEM_OF_UNDYING;
            try {
                if (player.getMainHandItem().getItem() == totem) {
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                }
                if (player.getOffhandItem().getItem() == totem) {
                    player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                }
            } catch (Exception e) { e.printStackTrace(); }
            player.hurt(Damagetypes2.terminal(player), 1000.0F);
        }
    }

}
