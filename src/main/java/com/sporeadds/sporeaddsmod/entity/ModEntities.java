package com.sporeadds.sporeaddsmod.entity;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.entity.projectile.ChainProjectileEntity;
import com.sporeadds.sporeaddsmod.entity.projectile.TentacleProjectile;
import com.sporeadds.sporeaddsmod.entity.projectile.ThrowableBandagesEntity;
import com.sporeadds.sporeaddsmod.entity.projectile.GasGlobProjectile;
import com.sporeadds.sporeaddsmod.entity.projectile.VariantVomitProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, "sporeadd");

    public static final DeferredHolder<EntityType<?>, EntityType<StaticEntity>> COCOON = ENTITY_TYPES.register(
            "cocoon",
            () -> EntityType.Builder.<StaticEntity>of(StaticEntity::new, MobCategory.MISC)
                    // Hitbox 20% más ancha a los lados (0.6 -> 0.72); altura sin cambios.
                    .sized(0.72f, 1.8f)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .fireImmune()
                    .build("cocoon")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<TentacleProjectile>> TENTACLE_PROJECTILE = ENTITY_TYPES.register(
            "tentacle_projectile",
            () -> EntityType.Builder.<TentacleProjectile>of(TentacleProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("tentacle_projectile")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<Tentacle>> TENTACLE = ENTITY_TYPES.register(
            "tentacle",
            () -> EntityType.Builder.<Tentacle>of(Tentacle::new, MobCategory.MONSTER)
                    .sized(0.7f, 0.9f)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .fireImmune()
                    .build("tentacle")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<VariantVomitProjectile>> VARIANT_VOMIT = ENTITY_TYPES.register(
            "variant_vomit",
            () -> EntityType.Builder.<VariantVomitProjectile>of(VariantVomitProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("variant_vomit")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<GasGlobProjectile>> GAS_GLOB_PROJECTILE = ENTITY_TYPES.register(
            "gas_glob_projectile",
            () -> EntityType.Builder.<GasGlobProjectile>of(GasGlobProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("gas_glob_projectile")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<MeatAbomination>> MEAT_ABOMINATION = ENTITY_TYPES.register(
            "meat_abomination",
            () -> EntityType.Builder.<MeatAbomination>of(MeatAbomination::new, MobCategory.CREATURE)
                    .sized(1.2f, 1.8f)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .fireImmune()
                    .build("meat_abomination")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ChainProjectileEntity>> CHAIN_PROJECTILE = ENTITY_TYPES.register(
            "chain_projectile",
            () -> EntityType.Builder.<ChainProjectileEntity>of(ChainProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("chain_projectile")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableBandagesEntity>> THROWABLE_BANDAGES =
            ENTITY_TYPES.register("throwable_bandages", () ->
                    EntityType.Builder.<ThrowableBandagesEntity>of(ThrowableBandagesEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("throwable_bandages")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<DecoyEntity>> DECOY = ENTITY_TYPES.register("decoy",
            () -> EntityType.Builder.<DecoyEntity>of(DecoyEntity::new, MobCategory.MISC)
                    .sized(0.5F, 1.975F)
                    .clientTrackingRange(10)
                    .build("decoy")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<DataDropItemEntity>> DATA_DROP_ITEM = ENTITY_TYPES.register(
            "data_drop_item",
            () -> EntityType.Builder.<DataDropItemEntity>of(DataDropItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .build("data_drop_item")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}