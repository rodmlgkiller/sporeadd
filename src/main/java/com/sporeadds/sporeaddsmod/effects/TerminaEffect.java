package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.core.Holder;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.Damage.Damagetypes2;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
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

    private static final ResourceLocation TERMINA_HEALTH_MODIFIER_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "terminaeffect_".lc("TERMINA_HEALTH_MODIFIER_UUID"));
    private static final ResourceLocation SOUND_REAGENT_ID = ResourceLocation.fromNamespaceAndPath("spore", "reagent");

    public static final ResourceKey<DamageType> MYCELIUM_OVERTAKE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("spore", "mycelium_overtake"));

    public TerminaEffect() {
        // Color base del efecto en el inventario: Morado oscuro (0x8A38B3)
        super(MobEffectCategory.HARMFUL, 0x8A38B3);
        addAttributeModifier(
                net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,
                TERMINA_HEALTH_MODIFIER_UUID.toString(),
                -8.0D,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Cada 10 segundos (200 ticks) para que no sea spam excesivo
        return duration % 200 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {

            // Si la entidad es un Jugador, le mandamos los mensajes aterradores
            if (entity instanceof ServerPlayer player) {
                if (RAND.nextDouble() < 0.05) {
                    String phrase = PHRASES[RAND.nextInt(PHRASES.length)];
                    player.displayClientMessage(Component.literal(phrase), true);
                }
            }

            // Las náuseas y el daño pasivo aplican a TODOS (jugadores y mobs)
            if (RAND.nextDouble() < 0.05) {
                var nausea = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("minecraft", "nausea")).orElse(null);
                if (nausea != null) {
                    entity.addEffect(new MobEffectInstance(nausea, 20 * 5, 0));
                }
            }
            if (RAND.nextDouble() < 0.02) {
                // Asumiendo que tu método Damagetypes2.terminal acepta LivingEntity
                entity.hurt(Damagetypes2.terminal(entity), 2.0F);
            }
        }
        return true;
    }

    @Override
    public void fillEffectCures(java.util.Set<net.neoforged.neoforge.common.EffectCure> cures, net.minecraft.world.effect.MobEffectInstance effectInstance) {
        cures.remove(net.neoforged.neoforge.common.EffectCures.MILK);
        cures.remove(net.neoforged.neoforge.common.EffectCures.HONEY_BOTTLE);
    }

    @Override
    public void removeAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributemap) {
        super.removeAttributeModifiers(attributemap);

        if (!entity.level().isClientSide) {

            // EXCEPCIÓN 1: Si se le está actualizando la duración desde el daño (Overtake), NO LO MATES
            if (entity.getPersistentData().contains("TerminaUpdatingDuration")) {
                return;
            }

            // EXCEPCIÓN 2: Si tiene el flag de vacuna, no lo mates
            if (entity.getPersistentData().contains("VaccineBypassTermina")) {
                entity.getPersistentData().remove("VaccineBypassTermina");
                if (entity instanceof ServerPlayer player) {
                    player.displayClientMessage(Component.translatable("message.sporeadd.general.symptoms_disappeared").withStyle(ChatFormatting.GREEN), true);
                }
                return;
            }

            // ---- Ejecución letal ----
            var myceliumEffect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("spore", "mycelium_ef")).orElse(null);
            if (myceliumEffect != null) {
                entity.addEffect(new MobEffectInstance(myceliumEffect, 20 * 30, 0));
            }

            // Intentar quitar el Tótem de la Inmortalidad (Funciona para jugadores y mobs que lo lleven)
            var totem = net.minecraft.world.item.Items.TOTEM_OF_UNDYING;
            try {
                if (entity.getMainHandItem().getItem() == totem) {
                    entity.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                }
                if (entity.getOffhandItem().getItem() == totem) {
                    entity.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ==========================================================
            // EFECTOS VISUALES Y SONIDO ANTES DE MORIR
            // ==========================================================
            SoundEvent sound = SoundEvent.createVariableRangeEvent(SOUND_REAGENT_ID);
            if (sound != null) {
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.MASTER, 3.0f, 1.0f);
            }

            if (entity.level() instanceof ServerLevel serverLevel) {
                // Vector3f(R, G, B) -> Vector3f(1.0f, 0.0f, 0.0f) es Rojo Brillante puro
                // El último parámetro (1.5f) es el tamaño de la partícula.
                DustParticleOptions redBloodParticle = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.5f);

                // Explosión de 150 partículas rojas alrededor de la entidad
                serverLevel.sendParticles(
                        redBloodParticle,
                        entity.getX(), entity.getY() + (entity.getBbHeight() / 2.0), entity.getZ(),
                        150, // cantidad de partículas
                        0.5, 0.5, 0.5, // dispersión en X, Y, Z
                        0.1 // velocidad extra
                );
            }

            // Finalmente, aplica el daño letal
            entity.hurt(Damagetypes2.terminal(entity), 1000.0F);
        }
    }

    // =========================================================================================
    // EVENTO GLOBAL: Reduce el tiempo del efecto al recibir MYCELIUM_OVERTAKE
    // =========================================================================================
    @SubscribeEvent
    public static void onMyceliumOvertakeDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (event.getSource().is(MYCELIUM_OVERTAKE)) {

            // Asegúrate de que tu modid sea correcto ("sporeadd" o "sporeadds")
            Holder<MobEffect> termina = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("sporeadds", "termina")).orElse(null);
            if (termina == null) return;

            MobEffectInstance currentEffect = entity.getEffect(termina);

            if (currentEffect != null) {
                int ticksToReduce = 100 + (100 * currentEffect.getAmplifier());
                int newDuration = Math.max(100, currentEffect.getDuration() - ticksToReduce);

                if (newDuration != currentEffect.getDuration()) {
                    MobEffectInstance updatedEffect = new MobEffectInstance(
                            termina,
                            newDuration,
                            currentEffect.getAmplifier(),
                            currentEffect.isAmbient(),
                            currentEffect.isVisible(),
                            currentEffect.showIcon()
                    );

                    // Seguro para que la función removeAttributeModifiers no lo mate
                    entity.getPersistentData().putBoolean("TerminaUpdatingDuration", true);

                    entity.forceAddEffect(updatedEffect, null);

                    entity.getPersistentData().remove("TerminaUpdatingDuration");
                }
            }
        }
    }
}