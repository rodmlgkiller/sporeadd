package com.sporeadds.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenEffectRenderer.class)
public interface ScreenEffectRendererInvoker {

    @Invoker("renderTex")
    static void sporeadds$invokeRenderTex(TextureAtlasSprite sprite, PoseStack poseStack) {
        throw new AssertionError();
    }
}