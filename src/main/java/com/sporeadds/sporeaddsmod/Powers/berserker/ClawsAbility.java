package com.sporeadds.sporeaddsmod.Powers.berserker;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.client.BerserkerCooldownClientState;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncBerserkerCooldownPacket;
import com.sporeadds.sporeaddsmod.network.SyncClawCounterPacket;
import com.sporeadds.sporeaddsmod.network.SyncClawsActivePacket;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Berserker - "Claws of Brutality".
 *
 * Un contador de daño ("Brutality") sube con el daño que el berserker inflige a otras entidades
 * (sin contar el sobredaño que la vida no respalda). Tras 20 s sin subir, decae 1 cada 0.5 s.
 * Al llegar a 200 se puede activar: el contador pasa a bajar 1 cada 0.5 s y, mientras dure,
 * el jugador tiene garras en las manos y cada golpe con la MANO DESNUDA hace 8 de daño
 * (con arma, sin bonus). Al llegar el contador a 0 se desactiva y entra en cooldown de 120 s.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class ClawsAbility {

    public static final float ACTIVATION_THRESHOLD = 200.0F;
    private static final float MAX_COUNTER = 200.0F;
    private static final int DECAY_INTERVAL_TICKS = 10;        // 0.5 s
    private static final int IDLE_BEFORE_DECAY_TICKS = 400;    // 20 s
    private static final int COOLDOWN_TICKS = 2400;            // 120 s
    public static final float BARE_HAND_DAMAGE = 12.0F;

    private static final Map<UUID, Float> COUNTER = new HashMap<>();
    private static final Map<UUID, Long> LAST_GAIN_TICK = new HashMap<>();
    private static final Set<UUID> ACTIVE = new HashSet<>();
    private static final Map<UUID, Long> COOLDOWN_UNTIL = new HashMap<>();

    private static final Map<UUID, Integer> LAST_SENT_VALUE = new HashMap<>();
    private static final Set<UUID> LAST_SENT_ACTIVE = new HashSet<>();

    // Modificador temporal de ATTACK_DAMAGE para que Minecraft no cancele el golpe desarmado
    // cuando el jugador tiene Debilidad (que dejaría el daño base <= 0). Se añade al iniciar el
    // ataque y se quita al aplicarlo; el valor se descuenta en la fórmula del daño de garra.
    private static final ResourceLocation WEAKNESS_BYPASS_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "clawsability_weakness_bypass_uuid");
    private static final double WEAKNESS_BYPASS_AMOUNT = 1024.0D;

    // Crit desarmado pendiente (multiplicador) a consumir en el proximo LivingIncomingDamageEvent.
    private static final Map<UUID, Float> CRIT_PENDING = new HashMap<>();
    // Empuje extra pendiente por calcified: id del objetivo -> multiplicador / tick del golpe.
    private static final Map<UUID, Float> PENDING_KB = new HashMap<>();
    private static final Map<UUID, Long> PENDING_KB_TICK = new HashMap<>();

    private ClawsAbility() {
    }

    // ------------------------------------------------------------------ activation

    public static void tryActivate(ServerPlayer player) {
        if (!SporeClassUtil.hasClass(player, "berserker")) return;

        UUID id = player.getUUID();
        long now = player.level().getGameTime();

        // Volver a usar la habilidad mientras está activa la detiene: la mitad de la barra
        // restante se descuenta del cooldown (en segundos) y la otra mitad se conserva.
        if (ACTIVE.contains(id)) {
            float remaining = COUNTER.getOrDefault(id, 0.0F);
            float half = remaining / 2.0F;
            int cd = Math.max(0, COOLDOWN_TICKS - Math.round(half * 20.0F));
            deactivate(player, cd, remaining - half);
            player.displayClientMessage(
                    Component.translatable("message.sporeadd.berserker.claws.stopped").withStyle(ChatFormatting.GRAY), true);
            return;
        }

        Long cd = COOLDOWN_UNTIL.get(id);
        if (cd != null && now < cd) {
            player.displayClientMessage(
                    Component.translatable("message.sporeadd.berserker.claws.cooldown").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (COUNTER.getOrDefault(id, 0.0F) < ACTIVATION_THRESHOLD) {
            player.displayClientMessage(
                    Component.translatable("message.sporeadd.berserker.claws.not_ready").withStyle(ChatFormatting.GRAY), true);
            return;
        }

        ACTIVE.add(id);
        LAST_GAIN_TICK.put(id, now);
        CompoundEffects.applyBuffs(player, CompoundEffects.counts(player));
        CompoundEffects.syncToClient(player);
        broadcastActive(player, true);
        syncCounter(player);
        activateFx(player);
        player.displayClientMessage(
                Component.translatable("message.sporeadd.berserker.claws.activated").withStyle(ChatFormatting.RED), true);
    }

    private static void deactivate(ServerPlayer player, int cooldownTicks, float keepCounter) {
        UUID id = player.getUUID();
        ACTIVE.remove(id);
        COUNTER.put(id, Mth.clamp(keepCounter, 0.0F, MAX_COUNTER));
        LAST_GAIN_TICK.put(id, player.level().getGameTime());
        CompoundEffects.removeBuffs(player);
        clearWeaknessBypass(player);

        if (cooldownTicks > 0) {
            COOLDOWN_UNTIL.put(id, player.level().getGameTime() + cooldownTicks);
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncBerserkerCooldownPacket(BerserkerCooldownClientState.CLAWS, cooldownTicks));
        }

        broadcastActive(player, false);
        syncCounter(player);
        deactivateFx(player);
    }

    public static void forget(UUID id) {
        COUNTER.remove(id);
        LAST_GAIN_TICK.remove(id);
        ACTIVE.remove(id);
        COOLDOWN_UNTIL.remove(id);
        LAST_SENT_VALUE.remove(id);
        LAST_SENT_ACTIVE.remove(id);
        CRIT_PENDING.remove(id);
    }

    // ------------------------------------------------------------------ counting

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!SporeClassUtil.hasClass(player, "berserker")) return;

        LivingEntity target = event.getEntity();
        if (target == player) return;

        float counted = Math.min(event.getAmount(), Math.max(0.0F, target.getHealth()));
        if (counted <= 0.0F) return;

        UUID id = player.getUUID();

        // Riders de las syringes de Compounds (solo con el modo Claws activo).
        if (ACTIVE.contains(id)) {
            CompoundEffects.onHitEntity(player, target, CompoundEffects.counts(player));
        }

        LAST_GAIN_TICK.put(id, player.level().getGameTime());

        float current = COUNTER.getOrDefault(id, 0.0F);
        if (current >= MAX_COUNTER) return;
        COUNTER.put(id, Math.min(MAX_COUNTER, current + counted));
        maybeSyncCounter(player);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!ACTIVE.contains(player.getUUID())) return;
        if (event.getSource().getDirectEntity() != player) return;   // solo golpe directo

        EnumMap<CompoundType, Integer> counts = CompoundEffects.counts(player);

        // calcified: registra el empuje extra, aplicado luego en LivingKnockBackEvent.
        float kbMult = CompoundEffects.outgoingKnockbackMultiplier(counts);
        if (kbMult > 1.0F) {
            UUID tid = event.getEntity().getUUID();
            PENDING_KB.put(tid, kbMult);
            PENDING_KB_TICK.put(tid, player.level().getGameTime());
        }

        if (!player.getMainHandItem().isEmpty()) return;             // el resto solo con mano desnuda

        // Daño de garra = daño plano + daño base del jugador - 1, para que Fuerza/Debilidad
        // afecten. Nunca baja de 1 (Debilidad no anula el golpe). Se descuenta el modificador
        // temporal que evita que Minecraft cancele el golpe bajo Debilidad.
        float clawFlat = BARE_HAND_DAMAGE + CompoundEffects.bonusUnarmedDamage(counts);
        double attackAttr = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        AttributeInstance atkInst = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atkInst != null) {
            AttributeModifier bypass = atkInst.getModifier(WEAKNESS_BYPASS_UUID);
            if (bypass != null) {
                attackAttr -= bypass.amount();
                atkInst.removeModifier(WEAKNESS_BYPASS_UUID);
            }
        }
        float base = (float) Math.max(1.0D, clawFlat + attackAttr - 1.0D);
        Float critMult = CRIT_PENDING.remove(player.getUUID());
        if (critMult != null) {
            base *= critMult;
        }
        event.setAmount(base);

        if (player.level() instanceof ServerLevel level) {
            LivingEntity target = event.getEntity();
            level.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6D, target.getZ(),
                    10, 0.3D, 0.3D, 0.3D, 0.2D);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.7F);
        }
    }

    /**
     * Al iniciar un golpe desarmado con el modo Claws activo, añade un ATTACK_DAMAGE enorme y
     * temporal para que Minecraft no cancele el ataque cuando el jugador tiene Debilidad
     * (daño base <= 0). Se descuenta en {@link #onLivingHurt} y se limpia cada tick.
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ACTIVE.contains(player.getUUID())) return;
        if (!player.getMainHandItem().isEmpty()) return;
        AttributeInstance inst = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (inst == null) return;
        inst.removeModifier(WEAKNESS_BYPASS_UUID);
        inst.addTransientModifier(new AttributeModifier(
                WEAKNESS_BYPASS_UUID,
                WEAKNESS_BYPASS_AMOUNT, AttributeModifier.Operation.ADD_VALUE));
    }

    /** Crit desarmado con el modo Claws activo: guarda el multiplicador para el LivingIncomingDamageEvent. */
    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ACTIVE.contains(player.getUUID())) return;
        if (!player.getMainHandItem().isEmpty()) return;
        boolean crit = event.isCriticalHit();
        if (!crit) return;
        float mult = event.getDamageMultiplier();
        CRIT_PENDING.put(player.getUUID(), mult <= 1.0F ? 1.5F : mult);
    }

    /** calcified: multiplica el empuje del golpe del berserker. */
    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        if (PENDING_KB.isEmpty()) return;
        UUID id = event.getEntity().getUUID();
        Float mult = PENDING_KB.remove(id);
        Long tick = PENDING_KB_TICK.remove(id);
        if (mult == null || tick == null) return;
        if (event.getEntity().level().getGameTime() - tick > 1L) return;
        event.setStrength(event.getStrength() * mult);
    }

    /** reinforced: reduce el daño recibido mientras el modo Claws está activo. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBerserkerHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ACTIVE.contains(player.getUUID())) return;
        if (!SporeClassUtil.hasClass(player, "berserker")) return;
        float mult = CompoundEffects.incomingDamageMultiplier(CompoundEffects.counts(player));
        if (mult < 1.0F) {
            event.setAmount(event.getAmount() * mult);
        }
    }

    /**
     * Con el modo Claws activo, los objetos recogidos no se autocolocan en ranuras vacías de
     * la hotbar: se apilan en pilas ya existentes (donde sea) y sólo rellenan huecos del
     * inventario principal (9..35).
     */
    @SubscribeEvent
    public static void onItemPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!ACTIVE.contains(player.getUUID())) return;

        ItemStack live = event.getItemEntity().getItem();
        if (live.isEmpty()) return;

        int original = live.getCount();
        boolean changed = mergeThenFillMainOnly(player.getInventory(), live);
        int taken = original - live.getCount();
        if (taken <= 0 && !changed) {
            return;   // no cabía sin usar la hotbar: deja que vanilla lo maneje
        }

        if (taken > 0) {
            player.take(event.getItemEntity(), taken);
        }
        if (live.isEmpty()) {
            event.getItemEntity().discard();
        } else {
            event.getItemEntity().setItem(live);
        }
        player.inventoryMenu.broadcastChanges();
        event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
    }

    private static boolean mergeThenFillMainOnly(Inventory inv, ItemStack stack) {
        boolean changed = false;
        int max = stack.getMaxStackSize();

        // 1) apilar en pilas existentes del mismo item (incluida la hotbar)
        for (int i = 0; i < inv.items.size() && !stack.isEmpty(); i++) {
            ItemStack slot = inv.items.get(i);
            if (slot.isEmpty() || !ItemStack.isSameItemSameComponents(slot, stack)) continue;
            int room = Math.min(max, slot.getMaxStackSize()) - slot.getCount();
            if (room <= 0) continue;
            int move = Math.min(room, stack.getCount());
            slot.grow(move);
            stack.shrink(move);
            changed = true;
        }

        // 2) rellenar huecos SOLO del inventario principal (9..35), nunca la hotbar (0..8)
        for (int i = 9; i < inv.items.size() && !stack.isEmpty(); i++) {
            if (!inv.items.get(i).isEmpty()) continue;
            ItemStack placed = stack.copy();
            int move = Math.min(max, stack.getCount());
            placed.setCount(move);
            inv.items.set(i, placed);
            stack.shrink(move);
            changed = true;
        }
        return changed;
    }

    // ------------------------------------------------------------------ tick

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;

        // Estados transitorios de un solo tick.
        if (!CRIT_PENDING.isEmpty()) CRIT_PENDING.clear();
        if (!PENDING_KB.isEmpty()) {
            PENDING_KB.clear();
            PENDING_KB_TICK.clear();
        }
        // Backstop: nunca dejar el modificador de bypass de Debilidad puesto entre ticks.
        for (UUID id : ACTIVE) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p == null) continue;
            AttributeInstance inst = p.getAttribute(Attributes.ATTACK_DAMAGE);
            if (inst != null && inst.getModifier(WEAKNESS_BYPASS_UUID) != null) {
                inst.removeModifier(WEAKNESS_BYPASS_UUID);
            }
        }

        if (COUNTER.isEmpty() && ACTIVE.isEmpty() && COOLDOWN_UNTIL.isEmpty()) return;

        Set<UUID> tracked = new HashSet<>(COUNTER.keySet());
        tracked.addAll(ACTIVE);
        tracked.addAll(COOLDOWN_UNTIL.keySet());

        for (UUID id : tracked) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) continue;

            if (!SporeClassUtil.hasClass(player, "berserker")) {
                boolean wasActive = ACTIVE.remove(id);
                CompoundEffects.removeBuffs(player);
                clearWeaknessBypass(player);
                forget(id);
                if (wasActive) broadcastActive(player, false);
                NetworkHandle.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncClawCounterPacket(0.0F, false));
                continue;
            }

            long now = player.level().getGameTime();
            if (now % DECAY_INTERVAL_TICKS != 0) {
                continue;
            }

            float current = COUNTER.getOrDefault(id, 0.0F);

            if (ACTIVE.contains(id)) {
                float drain = 1.0F + CompoundEffects.bonusDrainPerHalfSecond(CompoundEffects.counts(player));
                current = Math.max(0.0F, current - drain);
                COUNTER.put(id, current);
                if (current <= 0.0F) {
                    deactivate(player, COOLDOWN_TICKS, 0.0F);
                    continue;
                }
            } else {
                long lastGain = LAST_GAIN_TICK.getOrDefault(id, now);
                if (current > 0.0F && now - lastGain > IDLE_BEFORE_DECAY_TICKS) {
                    current = Math.max(0.0F, current - 1.0F);
                    COUNTER.put(id, current);
                }
            }
            maybeSyncCounter(player);
        }
    }

    // ------------------------------------------------------------------ join / leave

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncCounter(player);
        broadcastActive(player, ACTIVE.contains(player.getUUID()));
        CompoundEffects.syncToClient(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            CompoundEffects.removeBuffs(sp);
            clearWeaknessBypass(sp);
        }
        LAST_SENT_VALUE.remove(event.getEntity().getUUID());
        LAST_SENT_ACTIVE.remove(event.getEntity().getUUID());
    }

    // ------------------------------------------------------------------ helpers

    private static void clearWeaknessBypass(ServerPlayer player) {
        AttributeInstance inst = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (inst != null && inst.getModifier(WEAKNESS_BYPASS_UUID) != null) {
            inst.removeModifier(WEAKNESS_BYPASS_UUID);
        }
    }

    private static void broadcastActive(ServerPlayer player, boolean active) {
        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncClawsActivePacket(player.getId(), active));
    }

    private static void syncCounter(ServerPlayer player) {
        UUID id = player.getUUID();
        float value = COUNTER.getOrDefault(id, 0.0F);
        boolean active = ACTIVE.contains(id);
        LAST_SENT_VALUE.put(id, Mth.floor(value));
        if (active) LAST_SENT_ACTIVE.add(id); else LAST_SENT_ACTIVE.remove(id);
        NetworkHandle.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncClawCounterPacket(value, active));
    }

    private static void maybeSyncCounter(ServerPlayer player) {
        UUID id = player.getUUID();
        float value = COUNTER.getOrDefault(id, 0.0F);
        boolean active = ACTIVE.contains(id);
        int floored = Mth.floor(value);
        boolean valueChanged = LAST_SENT_VALUE.getOrDefault(id, Integer.MIN_VALUE) != floored;
        boolean activeChanged = LAST_SENT_ACTIVE.contains(id) != active;
        if (valueChanged || activeChanged) {
            syncCounter(player);
        }
    }

    private static void activateFx(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        double x = player.getX(), y = player.getY() + 1.0D, z = player.getZ();
        level.sendParticles(ParticleTypes.CRIT, x, y, z, 60, 0.5D, 0.8D, 0.5D, 0.4D);
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 15, 0.4D, 0.5D, 0.4D, 0.1D);
        level.playSound(null, x, y, z, SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.7F, 1.4F);
        level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    private static void deactivateFx(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        double x = player.getX(), y = player.getY() + 1.0D, z = player.getZ();
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 20, 0.4D, 0.5D, 0.4D, 0.02D);
        level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 0.8F, 0.7F);
    }
}
