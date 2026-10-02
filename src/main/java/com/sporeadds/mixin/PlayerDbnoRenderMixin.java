package com.sporeadds.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sporeadds.sporeaddsmod.client.hive.HiveDbnoClientState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dibuja tumbados en el suelo a los jugadores marcados como "downed but not out" de la colmena
 * (Call of the Hive). Se inyecta al final de {@code PlayerRenderer#setupRotations}, en el espacio
 * "jugador de pie, origen en los pies, +Y hacia arriba" (antes del flip/translate del modelo).
 *
 * El jugador NO se pone en pose de nado en el servidor (eso pelearía con la rotación de nado
 * vanilla), así que aquí hacemos toda la rotación: 90 grados alrededor del eje X para dejarlo
 * horizontal, pivotando en los pies.
 *
 * NOTA: si en juego queda boca abajo en vez de boca arriba, invierte el signo de
 * {@link #SPOREADDS_LIE_DEGREES}. {@link #SPOREADDS_GROUND_LIFT} ajusta el hundimiento en el suelo.
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerDbnoRenderMixin {

    private static final float SPOREADDS_LIE_DEGREES = -90.0F;
    private static final float SPOREADDS_GROUND_LIFT = 0.1F;

    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void sporeadds$layDownIfDbno(AbstractClientPlayer player, PoseStack poseStack,
                                        float ageInTicks, float rotationYaw, float partialTicks, float scale,
                                        CallbackInfo ci) {
        if (player == null) return;
        if (player.getPose() == Pose.SLEEPING) return;
        if (!HiveDbnoClientState.isDowned(player.getId())) return;

        poseStack.translate(0.0F, SPOREADDS_GROUND_LIFT, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(SPOREADDS_LIE_DEGREES));
    }
}
