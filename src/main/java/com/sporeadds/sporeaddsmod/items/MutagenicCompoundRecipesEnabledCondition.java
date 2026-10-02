package com.sporeadds.sporeaddsmod.items;

import com.mojang.serialization.MapCodec;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Recipe condition ("sporeadd:mutagenic_compound_recipes_enabled") backed by the common config toggle. */
public record MutagenicCompoundRecipesEnabledCondition() implements ICondition {

    public static final MapCodec<MutagenicCompoundRecipesEnabledCondition> CODEC =
            MapCodec.unit(new MutagenicCompoundRecipesEnabledCondition());

    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, "sporeadd");

    static {
        CONDITION_CODECS.register("mutagenic_compound_recipes_enabled", () -> CODEC);
    }

    @Override
    public boolean test(IContext context) {
        return SporeAddsConfig.ENABLE_MUTAGENIC_COMPOUND_RECIPES.get();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
