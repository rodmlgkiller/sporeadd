package com.sporeadds.jei;

import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.IModPlugin;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class SporeAddsJeiPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_UID = new ResourceLocation("sporeadd", "jei_plugin");



    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Las recetas en data/[modid]/recipes/ se detectan automáticamente si usas "minecraft:crafting_shaped" o "minecraft:crafting_shapeless"
        // No necesitas registrarlas manualmente salvo que sean recetas custom.
    }
}
