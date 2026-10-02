package com.sporeadds.sporeaddsmod.entity;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.core.Seffects;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = "sporeadd")
public class StaticEntity extends Organoid {

    /** Explosión de polvo rojo: FX del cocoon al aparecer / interrumpirse / completarse. */
    private static final DustParticleOptions DUST_BURST =
            new DustParticleOptions(new Vec3(0.8F, 0.07F, 0.07F).toVector3f(), 1.0F);
    public AnimationState animationState0 = new AnimationState();
    /** Idle en bucle mientras el cocoon está cerrado. */
    public AnimationState idleAnimationState = new AnimationState();
    /** Cierre (la apertura al revés) al entrar un jugador; ver {@link #beginClosing()}. */
    public AnimationState closingAnimationState = new AnimationState();

    // Entidad añadida a SynchedEntityData para las animaciones
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(StaticEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CLOSING = SynchedEntityData.defineId(StaticEntity.class, EntityDataSerializers.BOOLEAN);

    /** Duración de la animación de cierre, en ticks (igual que la de apertura: 3.5 s). */
    private static final int CLOSING_DURATION_TICKS = 70;

    /** true tras {@link #retreatIntoGround()}: el cocoon se está enterrando y desaparecerá al terminar. */
    private boolean retreating = false;
    /** Ticks restantes de la animación de cierre en curso; ver {@link #beginClosing()}. */
    private int closingTicksLeft = 0;

    public StaticEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean addEffect(MobEffectInstance effectInstance, Entity source) {
        // Inmunidad al micelio, propio de Spore
        if (effectInstance.getEffect() == Seffects.MYCELIUM.get()) {
            return false;
        }
        return super.addEffect(effectInstance, source);
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() - 1.0D;
    }

    @Override
    protected void registerGoals() {
        // Llamamos al original por si Organoid tiene IA base que deba registrarse
        super.registerGoals();
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(0, this.getDeltaMovement().y, 0);

        if (!this.onGround()) {
            this.setDeltaMovement(0, -0.5, 0);
        } else {
            this.setDeltaMovement(0, 0, 0);
        }

        startAnimation();

        if (!this.level().isClientSide()) {
            // El cocoon siempre brilla.
            this.setGlowingTag(true);

            // Forma segura de asegurar que siempre esté en el equipo (1 vez por segundo).
            if (this.tickCount % 20 == 0) {
                joinSporeTeam();
            }

            // Enterrándose para desaparecer: Organoid.tick() ya adelanta la animación de
            // "borrow"; cuando termina (modelo totalmente bajo tierra) se descarta.
            if (retreating && getBorrow() >= getBorrow_tick()) {
                this.discard();
            }

            // Cierre en curso: al agotarse su duración se vuelve al idle normal.
            if (isClosing()) {
                if (closingTicksLeft > 0) {
                    closingTicksLeft--;
                } else {
                    setClosing(false);
                }
            }
        }
    }

    /**
     * Reproduce la animación de apertura al revés, como si el cocoon se cerrase alrededor de
     * quien acaba de montarlo. Pensado para llamarse justo después de {@code player.startRiding}
     * (Poder1, o cualquier otra situación en la que un jugador entre en el cocoon).
     */
    public void beginClosing() {
        if (this.level().isClientSide()) {
            return;
        }
        setClosing(true);
        closingTicksLeft = CLOSING_DURATION_TICKS;
    }

    /**
     * Sustituye a "desaparecer con partículas": desmonta a los pasajeros y se entierra de nuevo
     * en el suelo con la animación de organoide; al terminar se descarta. Poder1 ya ha limpiado
     * sus mapas cuando llama aquí, así que no rompe su lógica.
     */
    public void retreatIntoGround() {
        if (this.level().isClientSide() || retreating) {
            return;
        }
        retreating = true;
        this.ejectPassengers();
        this.setInvulnerable(true);
        this.setNoAi(true);
        if (getBorrow() <= 0) {
            tickBurrowing();   // arranca la animación de enterrado
        }
    }

    /** Explosión de polvo rojo alrededor de (x,y,z). Usado por Poder1 en el ciclo del cocoon. */
    public static void spawnDustBurst(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 150; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 4.5;
            double dy = (level.random.nextDouble() - 0.5) * 4.5;
            double dz = (level.random.nextDouble() - 0.5) * 4.5;
            level.sendParticles(DUST_BURST, x + dx, y + 1 + dy, z + dz, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Un pasajero del cocoon no recibe daño directo: el golpe lo absorbe el propio cocoon.
     * (Antes vivía en Poder1 acoplado a sus mapas; es comportamiento intrínseco de la entidad.)
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPassengerHurt(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        if (!(victim.getVehicle() instanceof StaticEntity cocoon) || !cocoon.isAlive()) return;

        DamageSource source = event.getSource();
        float amount = event.getAmount();
        event.setCanceled(true);
        cocoon.hurt(source, amount);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OPEN, false);
        builder.define(CLOSING, false);
    }

    public boolean isOpen() {
        return this.entityData.get(OPEN);
    }

    public void setOpen(boolean open) {
        this.entityData.set(OPEN, open);
    }

    public boolean isClosing() {
        return this.entityData.get(CLOSING);
    }

    public void setClosing(boolean closing) {
        this.entityData.set(CLOSING, closing);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("open", this.isOpen());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setOpen(pCompound.getBoolean("open"));
    }

    public void startAnimation() {
        animationState0.animateWhen(isOpen(), tickCount);
        closingAnimationState.animateWhen(isClosing(), tickCount);
        idleAnimationState.animateWhen(!isOpen() && !isClosing(), tickCount);
    }

    // Unirse al equipo "spore" apenas aparece en el mundo
    @Override
    public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, MobSpawnType reason, @org.jetbrains.annotations.Nullable SpawnGroupData spawnData) {
        joinSporeTeam();
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    /**
     * Busca el equipo "spore" en el Scoreboard del mundo.
     * Si existe, añade a este mob al equipo automáticamente para evitar Friendly Fire.
     */
    private void joinSporeTeam() {
        if (this.level() instanceof ServerLevel serverLevel) {
            Scoreboard scoreboard = serverLevel.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam("spore");

            if (team != null) {
                scoreboard.addPlayerToTeam(this.getStringUUID(), team);
            }
        }
    }

    /**
     * Lógica de dropeo personalizada al morir.
     * Evita usar LootTables en JSON garantizando los drops exactos.
     */
    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int lootingMultiplier, boolean hitByPlayer) {
        super.dropCustomDeathLoot(damageSource, lootingMultiplier, hitByPlayer);

        // 1. Dropear de 2 a 6 Organoid Membrane
        Item membrane = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "organoid_membrane"));
        if (membrane != null && membrane != net.minecraft.world.item.Items.AIR) {
            int membraneCount = this.getRandom().nextInt(5) + 2;
            membraneCount += this.getRandom().nextInt(lootingMultiplier + 1); // Bonus de Saqueo
            this.spawnAtLocation(new ItemStack(membrane, membraneCount));
        }

        // 2. Dropear de 3 a 9 Armor Fragment
        Item armorFragment = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "armor_fragment"));
        if (armorFragment != null && armorFragment != net.minecraft.world.item.Items.AIR) {
            int fragmentCount = this.getRandom().nextInt(7) + 3;
            fragmentCount += this.getRandom().nextInt(lootingMultiplier + 1); // Bonus de Saqueo
            this.spawnAtLocation(new ItemStack(armorFragment, fragmentCount));
        }
    }
}