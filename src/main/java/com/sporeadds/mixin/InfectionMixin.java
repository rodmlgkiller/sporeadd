package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.BasicInfected.InfectedPlayer;
import com.Harbinger.Spore.Sevents.Infection;
import com.sporeadds.sporeaddsmod.hive.HiveDownedManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Infection.class)
public class InfectionMixin {

    /**
     * Si el jugador que "muere" va a entrar en el modo downed de la colmena (Call of the Hive),
     * en realidad NO muere: se aborta el manejador de muerte de Spore para que no genere un
     * {@code InfectedPlayer} clon del jugador. En la muerte real posterior (perecer) este
     * jugador ya no cumple {@link HiveDownedManager#wouldEnterDowned}, así que Spore actúa normal.
     */
    @Inject(method = "onEntityDeath", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sporeadds$skipForHiveDowned(LivingDeathEvent event, CallbackInfo ci) {
        if (event.getEntity() instanceof ServerPlayer player
                && HiveDownedManager.wouldEnterDowned(player)) {
            ci.cancel();
        }
    }

    /**
     * Interceptamos el momento exacto en el que el mod original le aplica la skin
     * al InfectedPlayer recién creado tras la muerte de un jugador real.
     */
    @Redirect(
            method = "onEntityDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Harbinger/Spore/Sentities/BasicInfected/InfectedPlayer;setSkin()V",
                    remap = false // Es un método propio del mod, no de Minecraft, por lo que no se remapea
            ),
            remap = false // onEntityDeath es el nombre que le puso el autor, no Mojang
    )
    private static void tagPlayerGeneratedAdventurer(InfectedPlayer instance) {
        // 1. Llamamos al método original para que el mod siga funcionando normal
        instance.setSkin();

        // 2. Le añadimos un booleano persistente al NBT (PersistentData de Forge)
        instance.getPersistentData().putBoolean("is_player_generated", true);

        // 3. (Opcional) Le añadimos un Tag estándar de Minecraft (como los del comando /tag)
        // Esto te permite buscarlo con comandos: /kill @e[type=spore:inf_player,tag=player_generated]
        instance.addTag("player_generated");
    }
}