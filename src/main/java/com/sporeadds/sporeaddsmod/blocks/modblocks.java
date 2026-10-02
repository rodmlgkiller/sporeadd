package com.sporeadds.sporeaddsmod.blocks;

import net.minecraft.core.registries.BuiltInRegistries;

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
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class modblocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, "sporeadd");

    public static final DeferredHolder<Block, Block> MEDIC_BLOCK = registerBlock("medic_block",
            () -> new medicblock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> MEDIC_CONTRUCTOR_BLOCK = registerBlock("medic_constructor_block",
            () -> new MedicBlockCrafter(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> SCIENTIST_BLOCK = registerBlock("scientist_block",
            () -> new scientistblock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> CRYO_BLOCK = registerBlock("cryo_block",
            () -> new cryoblock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> FREEZER_BLOCK = BLOCKS.register("freezer_block",
            () -> new FreezerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> PELLET_BLOCK = registerBlock("pellet_block",
            () -> new PelletBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .mapColor(MapColor.NONE)
                    .strength(0.2f)
                    .noOcclusion()));

    public static final DeferredHolder<Block, Block> MOUND_TERRARIUM = registerBlock("mound_terrarium",
            () -> new MoundTerrariumBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .mapColor(MapColor.PLANT)
                    .strength(0.4f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final DeferredHolder<Block, Block> RAID_CONTROLER = registerBlock("raid_controler",
            () -> new RaidControlerBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()));

    private static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> block) {
        DeferredHolder<Block, T> toReturn = BLOCKS.register(name, block);
        if (!name.equals("freezer_block")) {
            registerBlockItem(name, toReturn);
        }
        return toReturn;
    }

    private static <T extends Block> DeferredHolder<Item, Item> registerBlockItem(String name, DeferredHolder<Block, T> block) {
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