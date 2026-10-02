package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.util.SporeFactionHelper;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowGolem.class)
public class SnowGolemMixin {

    /**
     * Inyectamos al final (TAIL) del método donde el Snow Golem carga sus IA básicas.
     */
    @Inject(method = "registerGoals", at = @At("TAIL"))
    protected void onRegisterGoals(CallbackInfo ci) {
        SnowGolem golem = (SnowGolem) (Object) this;

        // Añadimos un objetivo nativo para que busque Jugadores de la misma forma que busca Monstruos.
        // Al poner la clase Player.class, el juego sí los verá.
        golem.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                golem,
                Player.class,
                10,
                true,
                false,
                (entity) -> SporeAddsConfig.SPORE_FACTION_ENABLED.get() && SporeFactionHelper.isSporePlayer(entity)
        ));
    }
}