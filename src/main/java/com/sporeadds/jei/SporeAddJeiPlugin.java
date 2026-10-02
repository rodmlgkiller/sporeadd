package com.sporeadds.jei;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.minecraft.core.registries.BuiltInRegistries;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class SporeAddJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "jei_plugin");

    public static final RecipeType<ScientistJeiRecipe> SCIENTIST_TYPE =
            RecipeType.create("sporeadd", "scientist_category", ScientistJeiRecipe.class);
    public static final RecipeType<MedicConstructorJeiRecipe> MEDIC_CONSTRUCTOR_TYPE =
            RecipeType.create("sporeadd", "medic_constructor_category", MedicConstructorJeiRecipe.class);
    public static final RecipeType<MedicBlockJeiRecipe> MEDIC_BLOCK_TYPE =
            RecipeType.create("sporeadd", "medic_category", MedicBlockJeiRecipe.class);
    public static final RecipeType<MoundTerrariumJeiRecipe> MOUND_TERRARIUM_TYPE =
            RecipeType.create("sporeadd", "mound_terrarium", MoundTerrariumJeiRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ScientistRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MedicConstructorRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new com.sporeadd.jei.MedicBlockRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MoundTerrariumRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "scientist_block"))),
                SCIENTIST_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "medic_constructor_block"))),
                MEDIC_CONSTRUCTOR_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "medic_block"))),
                MEDIC_BLOCK_TYPE
        );
        registration.addRecipeCatalyst(createMoundTerrariumStack(), MOUND_TERRARIUM_TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Item biomassItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "frozen_decayed_biomass"));

        List<ItemStack> in50 = new ArrayList<>();
        in50.add(new ItemStack(biomassItem, 50));

        List<ItemStack> in100 = new ArrayList<>();
        in100.add(new ItemStack(biomassItem, 50));
        in100.add(new ItemStack(biomassItem, 50));

        List<ItemStack> in150 = new ArrayList<>();
        in150.add(new ItemStack(biomassItem, 50));
        in150.add(new ItemStack(biomassItem, 50));
        in150.add(new ItemStack(biomassItem, 50));

        ItemStack research50 = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "research_50")));
        ItemStack research100 = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "research_100")));
        ItemStack research150 = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "research_150")));

        List<ScientistJeiRecipe> scientistRecipes = new ArrayList<>();
        scientistRecipes.add(new ScientistJeiRecipe(in50, research50));
        scientistRecipes.add(new ScientistJeiRecipe(in100, research100));
        scientistRecipes.add(new ScientistJeiRecipe(in150, research150));
        registration.addRecipes(SCIENTIST_TYPE, scientistRecipes);

        ItemStack redstoneBlock = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", "redstone_block")));
        ItemStack ironBlock = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", "iron_block")));
        ItemStack medicBlockOut = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "medic_block")));

        List<MedicConstructorJeiRecipe> constructorRecipes = new ArrayList<>();
        constructorRecipes.add(new MedicConstructorJeiRecipe(redstoneBlock, ironBlock, medicBlockOut));
        registration.addRecipes(MEDIC_CONSTRUCTOR_TYPE, constructorRecipes);

        ItemStack syringeFilled = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "syringe")));
        CompoundTag syringeTag = ItemNbt.getOrCreateTag(syringeFilled);
        syringeTag.putInt("CustomModelData", 1);

        ItemStack vaccineOut = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "vaccine")));

        List<MedicBlockJeiRecipe> medicRecipes = new ArrayList<>();
        medicRecipes.add(new MedicBlockJeiRecipe(syringeFilled, vaccineOut));
        registration.addRecipes(MEDIC_BLOCK_TYPE, medicRecipes);

        registration.addRecipes(MOUND_TERRARIUM_TYPE, createMoundTerrariumRecipes());
    }

    public static ItemStack createMoundTerrariumStack() {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "mound_terrarium")));
        CompoundTag tag = ItemNbt.getOrCreateTag(stack);
        tag.putBoolean("HasMound", true);
        tag.putBoolean("Linked", false);
        tag.putInt("CustomModelData", 1);
        return stack;
    }

    private static List<MoundTerrariumJeiRecipe> createMoundTerrariumRecipes() {
        List<MoundTerrariumJeiRecipe> recipes = new ArrayList<>();

        Item reaver = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "reaver"));
        Item mutatedFiber = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "mutated_fiber"));
        Item tumor = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "tumor"));
        Item organoidMembrane = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "organoid_membrane"));

        if (reaver != null && mutatedFiber != null && tumor != null && organoidMembrane != null) {
            List<ItemStack> inputs = new ArrayList<>();
            inputs.add(new ItemStack(reaver));
            inputs.add(createMoundTerrariumStack());

            List<ItemStack> outputs = new ArrayList<>();
            outputs.add(new ItemStack(mutatedFiber));
            outputs.add(new ItemStack(tumor));
            outputs.add(new ItemStack(organoidMembrane));

            recipes.add(new MoundTerrariumJeiRecipe(
                    inputs,
                    outputs,
                    Component.translatable("jei.sporeadds.mound_terrarium.reaver"),
                    Component.translatable("jei.sporeadds.mound_terrarium.reaver.desc")
            ));
        }

        Item blomfung = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "blomfung"));
        Item bloomfung2 = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "bloomfung2"));
        Item biomassBulb = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "biomass_bulb"));
        Item growthsBig = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "growths_big"));
        Item fungalRoots = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "fungal_roots"));
        Item growthMycelium = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "growth_mycelium"));
        Item myceliumVeins = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "mycelium_veins"));
        Item growthsSmall = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "growths_small"));
        Item fungalStemSapling = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "fungal_stem_sapling"));

        List<ItemStack> shearOutputs = new ArrayList<>();
        if (blomfung != null) shearOutputs.add(new ItemStack(blomfung));
        if (bloomfung2 != null) shearOutputs.add(new ItemStack(bloomfung2));
        if (biomassBulb != null) shearOutputs.add(new ItemStack(biomassBulb));
        if (growthsBig != null) shearOutputs.add(new ItemStack(growthsBig));
        if (fungalRoots != null) shearOutputs.add(new ItemStack(fungalRoots));
        if (growthMycelium != null) shearOutputs.add(new ItemStack(growthMycelium));
        if (myceliumVeins != null) shearOutputs.add(new ItemStack(myceliumVeins));
        if (growthsSmall != null) shearOutputs.add(new ItemStack(growthsSmall));
        if (fungalStemSapling != null) shearOutputs.add(new ItemStack(fungalStemSapling));

        if (!shearOutputs.isEmpty()) {
            List<ItemStack> inputs = new ArrayList<>();
            inputs.add(new ItemStack(Items.SHEARS));
            inputs.add(createMoundTerrariumStack());

            recipes.add(new MoundTerrariumJeiRecipe(
                    inputs,
                    shearOutputs,
                    Component.translatable("jei.sporeadds.mound_terrarium.shears"),
                    Component.translatable("jei.sporeadds.mound_terrarium.shears.desc")
            ));
        }

        Item scentSpawnEgg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "scent_spawnegg"));
        if (scentSpawnEgg != null) {
            List<ItemStack> inputs = new ArrayList<>();
            inputs.add(new ItemStack(Items.GLASS_BOTTLE));
            inputs.add(createMoundTerrariumStack());

            List<ItemStack> outputs = new ArrayList<>();
            outputs.add(new ItemStack(scentSpawnEgg));

            recipes.add(new MoundTerrariumJeiRecipe(
                    inputs,
                    outputs,
                    Component.translatable("jei.sporeadds.mound_terrarium.bottle"),
                    Component.translatable("jei.sporeadds.mound_terrarium.bottle.desc")
            ));
        }

        return recipes;
    }
}