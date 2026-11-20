package com.sporeadds.sporeaddsmod.Powers.berserker;

import net.minecraft.resources.ResourceLocation;

import com.sporeadds.sporeaddsmod.capabilities.CompoundsCapability;
import com.sporeadds.sporeaddsmod.config.BerserkerConfig;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncCompoundsPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/**
 * Traduce las syringes colocadas en el inventario de Compounds en efectos sobre el modo
 * Claws of Brutality. Cada tipo es stackeable segun cuantas haya del mismo.
 */
public final class CompoundEffects {

    private static final ResourceLocation MOVE_SPEED_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "compoundeffects_move_speed_uuid");
    private static final ResourceLocation KB_RES_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "compoundeffects_kb_res_uuid");
    private static final ResourceLocation SWIM_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "compoundeffects_swim_uuid");

    private CompoundEffects() {
    }

    /**
     * Cuenta las syringes por tipo en el inventario de Compounds del jugador, respetando los limites de la
     * config (berserker.blood_samples.max_total y max_count de cada tipo).
     */
    public static EnumMap<CompoundType, Integer> counts(ServerPlayer player) {
        List<ItemStack> stacks = new ArrayList<>();
        CompoundsCapability.PLAYER_COMPOUNDS.get(player).ifPresent(store -> {
            for (int i = 0; i < store.size(); i++) {
                stacks.add(store.getStack(i));
            }
        });
        return countLimited(stacks);
    }

    /**
     * Para cada ranura, true si su syringe cuenta; false si esta vacia o supera los limites de la config. Se
     * recorre en orden de ranura: como mucho max_total syringes en total y max_count de cada tipo.
     */
    public static boolean[] activeSlots(List<ItemStack> stacks) {
        boolean[] active = new boolean[stacks.size()];
        EnumMap<CompoundType, Integer> perKind = new EnumMap<>(CompoundType.class);
        int total = 0;
        int maxTotal = BerserkerConfig.sampleMaxTotal();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack == null || stack.isEmpty()) continue;
            CompoundType type = CompoundType.fromItem(stack.getItem());
            if (type == null) continue;
            int have = perKind.getOrDefault(type, 0);
            if (total >= maxTotal || have >= type.maxCount()) continue;
            perKind.put(type, have + 1);
            total++;
            active[i] = true;
        }
        return active;
    }

    /** Syringes por tipo que realmente cuentan (ver {@link #activeSlots}). Lo usan el servidor y la pantalla. */
    public static EnumMap<CompoundType, Integer> countLimited(List<ItemStack> stacks) {
        boolean[] active = activeSlots(stacks);
        EnumMap<CompoundType, Integer> map = new EnumMap<>(CompoundType.class);
        for (int i = 0; i < active.length; i++) {
            if (!active[i]) continue;
            map.merge(CompoundType.fromItem(stacks.get(i).getItem()), 1, Integer::sum);
        }
        return map;
    }

    // ------------------------------------------------------------------ buffs persistentes

    public static void applyBuffs(ServerPlayer player, EnumMap<CompoundType, Integer> counts) {
        int skeletal = counts.getOrDefault(CompoundType.SKELETAL, 0);
        int drowned = counts.getOrDefault(CompoundType.DROWNED, 0);
        int charred = counts.getOrDefault(CompoundType.CHARRED, 0);

        setModifier(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_UUID, "sporeadd_claws_skeletal_speed",
                BerserkerConfig.SKELETAL_SPEED_BONUS.get() * skeletal, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, skeletal > 0);
        setModifier(player, Attributes.KNOCKBACK_RESISTANCE, KB_RES_UUID, "sporeadd_claws_skeletal_kbres",
                BerserkerConfig.SKELETAL_KNOCKBACK_RESISTANCE.get() * skeletal, AttributeModifier.Operation.ADD_VALUE, skeletal > 0);

        net.minecraft.core.Holder<Attribute> swim = net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED;
        setModifier(player, swim, SWIM_UUID, "sporeadd_claws_drowned_swim",
                BerserkerConfig.DROWNED_SWIM_BONUS.get() * drowned, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, drowned > 0);

        if (drowned > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,
                    MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
        }
        if (charred > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
                    MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
        }
    }

    public static void removeBuffs(ServerPlayer player) {
        removeModifier(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_UUID);
        removeModifier(player, Attributes.KNOCKBACK_RESISTANCE, KB_RES_UUID);
        removeModifier(player, net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED, SWIM_UUID);

        MobEffectInstance wb = player.getEffect(MobEffects.WATER_BREATHING);
        if (wb != null && wb.isInfiniteDuration()) {
            player.removeEffect(MobEffects.WATER_BREATHING);
        }
        MobEffectInstance fr = player.getEffect(MobEffects.FIRE_RESISTANCE);
        if (fr != null && fr.isInfiniteDuration()) {
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
        }
    }

    private static void setModifier(LivingEntity entity, net.minecraft.core.Holder<Attribute> attr, net.minecraft.resources.ResourceLocation id, String name,
                                    double amount, AttributeModifier.Operation op, boolean present) {
        if (attr == null) return;
        AttributeInstance inst = entity.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (present && amount != 0.0D) {
            inst.addTransientModifier(new AttributeModifier(id, amount, op));
        }
    }

    private static void removeModifier(LivingEntity entity, net.minecraft.core.Holder<Attribute> attr, net.minecraft.resources.ResourceLocation id) {
        if (attr == null) return;
        AttributeInstance inst = entity.getAttribute(attr);
        if (inst != null) {
            inst.removeModifier(id);
        }
    }

    // ------------------------------------------------------------------ efectos por golpe

    /** Daño recibido: reinforced reduce un 7.5% por stack (min 0). Valor por defecto; ver la config. */
    public static float incomingDamageMultiplier(EnumMap<CompoundType, Integer> counts) {
        int reinforced = counts.getOrDefault(CompoundType.REINFORCED, 0);
        if (reinforced <= 0) return 1.0F;
        float perSample = BerserkerConfig.REINFORCED_DAMAGE_REDUCTION.get().floatValue();
        return Math.max(0.0F, 1.0F - perSample * reinforced);
    }

    /** Empuje extra del ataque: calcified +35% por stack. Valor por defecto; ver la config. */
    public static float outgoingKnockbackMultiplier(EnumMap<CompoundType, Integer> counts) {
        int calcified = counts.getOrDefault(CompoundType.CALCIFIED, 0);
        if (calcified <= 0) return 1.0F;
        return 1.0F + BerserkerConfig.CALCIFIED_KNOCKBACK_BONUS.get().floatValue() * calcified;
    }

    /** Daño desarmado extra por bezerk: +2 por stack. Valor por defecto; ver la config. */
    public static float bonusUnarmedDamage(EnumMap<CompoundType, Integer> counts) {
        return BerserkerConfig.BEZERK_UNARMED_DAMAGE.get().floatValue() * counts.getOrDefault(CompoundType.BEZERK, 0);
    }

    /**
     * Drenaje extra de la barra por bezerk en cada paso de decaimiento: 0.5 por stack por defecto (paso de
     * 10 ticks = 0.5 s -> 1 por segundo por stack). Valor por defecto; ver la config.
     */
    public static float bonusDrainPerHalfSecond(EnumMap<CompoundType, Integer> counts) {
        return BerserkerConfig.BEZERK_EXTRA_DRAIN.get().floatValue() * counts.getOrDefault(CompoundType.BEZERK, 0);
    }

    /** Riders aplicados cuando el berserker con el modo activo daña a una entidad. */
    public static void onHitEntity(ServerPlayer attacker, LivingEntity target,
                                   EnumMap<CompoundType, Integer> counts) {
        int vampiric = counts.getOrDefault(CompoundType.VAMPIRIC, 0);
        if (vampiric > 0) {
            float heal = BerserkerConfig.VAMPIRIC_HEAL_PER_HIT.get().floatValue() * vampiric;
            if (heal > 0.0F) {
                attacker.heal(heal);
            }
        }
        int charred = counts.getOrDefault(CompoundType.CHARRED, 0);
        if (charred > 0 && !target.fireImmune()) {
            // Cada golpe SUMA 1 s de fuego por stack al fuego actual de la entidad (por defecto 20 ticks por stack).
            int currentTicks = Math.max(0, target.getRemainingFireTicks());
            target.setRemainingFireTicks(currentTicks + charred * BerserkerConfig.CHARRED_FIRE_TICKS.get());
        }
        int toxic = counts.getOrDefault(CompoundType.TOXIC, 0);
        int poisonTicks = toxic * BerserkerConfig.TOXIC_POISON_TICKS.get();
        if (toxic > 0 && poisonTicks > 0) {
            // Amplificador y duración escalan con el nº de syringes (10 s por stack por defecto).
            target.addEffect(new MobEffectInstance(MobEffects.POISON, poisonTicks, toxic - 1));
        }
        int rotten = counts.getOrDefault(CompoundType.ROTTEN, 0);
        int witherTicks = rotten * BerserkerConfig.ROTTEN_WITHER_TICKS.get();
        if (rotten > 0 && witherTicks > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, witherTicks, rotten - 1));
        }
    }

    // ------------------------------------------------------------------ cambio de clase

    /** Envía al cliente del jugador el contenido de su inventario de Compounds (para el HUD). */
    public static void syncToClient(ServerPlayer player) {
        ItemStack[] arr = new ItemStack[CompoundsCapability.SIZE];
        java.util.Arrays.fill(arr, ItemStack.EMPTY);
        CompoundsCapability.PLAYER_COMPOUNDS.get(player).ifPresent(store -> {
            for (int i = 0; i < store.size() && i < arr.length; i++) {
                ItemStack s = store.getStack(i);
                arr[i] = s == null ? ItemStack.EMPTY : s;
            }
        });
        NetworkHandle.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncCompoundsPacket(arr));
    }

    /** Suelta al suelo y vacía las syringes de Compounds (al dejar la clase berserker). */
    public static void dropAndClearCompounds(ServerPlayer player) {
        CompoundsCapability.PLAYER_COMPOUNDS.get(player).ifPresent(store -> {
            for (int i = 0; i < store.size(); i++) {
                ItemStack s = store.getStack(i);
                if (!s.isEmpty()) {
                    player.drop(s.copy(), false);
                    store.setStack(i, ItemStack.EMPTY);
                }
            }
        });
    }
}
