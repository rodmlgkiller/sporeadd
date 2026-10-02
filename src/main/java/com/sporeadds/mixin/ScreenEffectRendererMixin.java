package com.sporeadds.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.Powers.AdaptedPhysiologyPower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Redirect(
            method = "renderScreenEffect(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderTex(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
            )
    )
    private static void sporeadds$skipBlockOverlay(TextureAtlasSprite sprite, PoseStack poseStack, Minecraft mc, PoseStack originalPoseStack) {
        Player player = mc.player;
        if (player == null) {
            ScreenEffectRendererInvoker.sporeadds$invokeRenderTex(sprite, poseStack);
            return;
        }

        BlockPos eyePos = BlockPos.containing(player.getEyePosition());
        BlockState state = player.level().getBlockState(eyePos);
        String blockId = String.valueOf(ForgeRegistries.BLOCKS.getKey(state.getBlock()));

        if (!(AdaptedPhysiologyPower.canPhase(player) && AdaptedPhysiologyPower.isPhaseableBlock(blockId))) {
            ScreenEffectRendererInvoker.sporeadds$invokeRenderTex(sprite, poseStack);
        }
    }
}