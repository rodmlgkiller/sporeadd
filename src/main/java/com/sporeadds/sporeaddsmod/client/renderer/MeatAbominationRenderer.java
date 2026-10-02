package com.sporeadds.sporeaddsmod.client.renderer;

import com.sporeadds.sporeaddsmod.client.model.MeatAbominationModel;
import com.sporeadds.sporeaddsmod.client.renderer.layer.MeatExtraLayer;
import com.sporeadds.sporeaddsmod.entity.MeatAbomination;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MeatAbominationRenderer extends MobRenderer<MeatAbomination, MeatAbominationModel<MeatAbomination>> {

    private static final ResourceLocation TEXTURE_1 = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/meat.png");
    private static final ResourceLocation TEXTURE_2 = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/meat2.png");
    private static final ResourceLocation TEXTURE_3 = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/meat3.png");

    public MeatAbominationRenderer(EntityRendererProvider.Context context) {
        super(context, new MeatAbominationModel<>(context.bakeLayer(MeatAbominationModel.LAYER_LOCATION)), 0.5f);

        // Añadimos las capas para las texturas 2 y 3
        this.addLayer(new MeatExtraLayer(this, TEXTURE_2, 2));
        this.addLayer(new MeatExtraLayer(this, TEXTURE_3, 3));
    }

    @Override
    public ResourceLocation getTextureLocation(MeatAbomination entity) {
        // La textura principal (para el grupo 1)
        return TEXTURE_1;
    }
}