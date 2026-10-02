package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Organoids.Vigil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Arregla un bug de la dependencia Spore: {@code Vigil.finalizeSpawn} (finalizeSpawn) SIEMPRE
 * re-sortea la variante del Vigil al azar entre las 4 ({@code DEFAULT, STALKER, TROLL, RINGER}),
 * ignorando la que ya tuviera la entidad o su NBT. Resultado: ~25% de los Vigils invocados por
 * huevo de spawn / {@code /summon} sin NBT / spawn natural / herramientas de copia acaban siendo
 * RINGER, y un RINGER no arranca oleadas (no usa {@code WatcherMobSummon}, solo reubica
 * infectados ya existentes con {@code WatcherMobCall}) — se queda mirando al objetivo y dándole
 * {@code uneasy}.
 *
 * <p>Capturamos la variante antes de {@code finalizeSpawn} (del NBT de spawn o de la entity data
 * ya aplicada) y, si era una variante intencionada (!= 0 = DEFAULT), la restauramos después de
 * que Spore la haya pisado.
 */
@Mixin(value = Vigil.class, remap = false)
public abstract class VigilVariantFixMixin {

    @Unique
    private int sporeadds$intendedVariant;

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), remap = false)
    private void sporeadds$captureVariant(ServerLevelAccessor level, DifficultyInstance difficulty,
                                          MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag spawnNbt,
                                          CallbackInfoReturnable<SpawnGroupData> cir) {
        Vigil self = (Vigil) (Object) this;
        sporeadds$intendedVariant = (spawnNbt != null && spawnNbt.contains("Variant"))
                ? spawnNbt.getInt("Variant")
                : self.getTypeVariant();
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"), remap = false)
    private void sporeadds$restoreVariant(ServerLevelAccessor level, DifficultyInstance difficulty,
                                          MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag spawnNbt,
                                          CallbackInfoReturnable<SpawnGroupData> cir) {
        if (sporeadds$intendedVariant != 0) {
            ((Vigil) (Object) this).setVariant(sporeadds$intendedVariant);
        }
    }
}
