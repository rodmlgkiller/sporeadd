package com.sporeadds.sporeaddsmod.blocks;

import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.FreezerBlock;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MoundTerrariumBlock;
import com.sporeadds.sporeaddsmod.items.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class modblocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "sporeadd");

    public static final RegistryObject<Block> MEDIC_BLOCK = registerBlock("medic_block",
            () -> new medicblock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> MEDIC_CONTRUCTOR_BLOCK = registerBlock("medic_constructor_block",
            () -> new MedicBlockCrafter(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> SCIENTIST_BLOCK = registerBlock("scientist_block",
            () -> new scientistblock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> CRYO_BLOCK = registerBlock("cryo_block",
            () -> new cryoblock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> FREEZER_BLOCK = BLOCKS.register("freezer_block",
            () -> new FreezerBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> PELLET_BLOCK = registerBlock("pellet_block",
            () -> new PelletBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .mapColor(MapColor.NONE)
                    .strength(0.2f)
                    .noOcclusion()));

    public static final RegistryObject<Block> MOUND_TERRARIUM = registerBlock("mound_terrarium",
            () -> new MoundTerrariumBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .mapColor(MapColor.PLANT)
                    .strength(0.4f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistryObject<Block> RAID_CONTROLER = registerBlock("raid_controler",
            () -> new RaidControlerBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        if (!name.equals("freezer_block")) {
            registerBlockItem(name, toReturn);
        }
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> {
            if (name.equals("cryo_block")) {
                return new CryoBlockItem(block.get(), new Item.Properties());
            }
            if (name.equals("mound_terrarium")) {
                return new MoundTerrariumItem(block.get(), new Item.Properties());
            }
            if (name.equals("raid_controler")) {
                return new RaidControlerItem(block.get(), new Item.Properties());
            }
            if (name.equals("scientist_block")) {
                return new ScientistBlockItem(block.get(), new Item.Properties());
            }
            if (name.equals("medic_block")) {
                return new MedicBlockItem(block.get(), new Item.Properties());
            }
            if (name.equals("medic_constructor_block")) {
                return new MedicConstructorBlockItem(block.get(), new Item.Properties());
            }
            return new BlockItem(block.get(), new Item.Properties());
        });
    }
}