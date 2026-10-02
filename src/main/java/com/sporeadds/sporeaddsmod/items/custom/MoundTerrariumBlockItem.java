package com.sporeadds.sporeaddsmod.items.custom;

import com.sporeadds.sporeaddsmod.client.renderer.item.MoundTerrariumItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class MoundTerrariumBlockItem extends BlockItem {

    private static final BlockEntityWithoutLevelRenderer RENDERER = new MoundTerrariumItemRenderer();

    public MoundTerrariumBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return RENDERER;
            }
        });
    }
}