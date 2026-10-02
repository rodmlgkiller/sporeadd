package com.sporeadds.sporeaddsmod.items;

import com.google.gson.JsonObject;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class MutagenicCompoundRecipesEnabledCondition implements ICondition {

    public static final ResourceLocation ID =
            new ResourceLocation("sporeadd", "mutagenic_compound_recipes_enabled");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return SporeAddsConfig.ENABLE_MUTAGENIC_COMPOUND_RECIPES.get();
    }

    public static class Serializer implements IConditionSerializer<MutagenicCompoundRecipesEnabledCondition> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, MutagenicCompoundRecipesEnabledCondition value) {
        }

        @Override
        public MutagenicCompoundRecipesEnabledCondition read(JsonObject json) {
            return new MutagenicCompoundRecipesEnabledCondition();
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}