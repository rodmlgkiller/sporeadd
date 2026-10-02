package com.sporeadds.sporeaddsmod.client.renderer;

import com.sporeadds.sporeaddsmod.entity.projectile.ThrowableBandagesEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class ThrowableBandagesRenderer extends ThrownItemRenderer<ThrowableBandagesEntity> {

    public ThrowableBandagesRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0F, true);
    }
}