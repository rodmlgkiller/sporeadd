package com.sporeadds.sporeaddsmod.events;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "sporeadd")
public class EffectsImmunity {

    // Inmunidades base de cualquier kommandant
    private static final Set<ResourceLocation> BASE_KOMMANDANT_IMMUNITIES = Set.of(
            new ResourceLocation("spore", "mycelium_ef"),
            new ResourceLocation("spore", "madness"),
            new ResourceLocation("sporeadd", "exposed"),
            new ResourceLocation("sporeadd", "termina")
    );

    // Inmunidades extra de kommandant caustic
    private static final Set<ResourceLocation> CAUSTIC_IMMUNITIES = Set.of(
            new ResourceLocation("spore", "corrosion"),
            new ResourceLocation("sporeadd", "dissolution")
    );

    // Inmunidades extra de kommandant abyssal
    private static final Set<ResourceLocation> ABYSSAL_IMMUNITIES = Set.of(
            new ResourceLocation("minecraft", "dolphins_grace")
    );

    private static final Set<ResourceLocation> gluttonous_IMMUNITIES = Set.of(
            new ResourceLocation("spore", "biled")
    );

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            if (!"kommandant".equalsIgnoreCase(data.getIdentifier())) {
                return;
            }

            String subclass = data.getSubclass() == null ? "none" : data.getSubclass().toLowerCase();

            Set<ResourceLocation> immunities = new HashSet<>(BASE_KOMMANDANT_IMMUNITIES);

            switch (subclass) {
                case "caustic" -> immunities.addAll(CAUSTIC_IMMUNITIES);
                case "abyssal" -> immunities.addAll(ABYSSAL_IMMUNITIES);
                case "gluttonous" -> immunities.addAll(gluttonous_IMMUNITIES);
            }

            MobEffect incomingEffect = event.getEffectInstance().getEffect();
            ResourceLocation incomingId = ForgeRegistries.MOB_EFFECTS.getKey(incomingEffect);

            if (incomingId != null && immunities.contains(incomingId)) {
                event.setResult(Event.Result.DENY);
            }
        });
    }
}