package com.sporeadds.sporeaddsmod.client.screen;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MedicBlockCrafterEntity;
import com.sporeadds.sporeaddsmod.client.gui.MoundTerrariumMenu;
import com.sporeadds.sporeaddsmod.client.gui.RaidControlerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, "sporeadd");

    public static final DeferredHolder<MenuType<?>, MenuType<MedicBlockMenu>> MEDIC_BLOCK_MENU =
            MENUS.register("medic_block_menu", () ->
                    IMenuTypeExtension.create((windowId, inv, buf) -> new MedicBlockMenu(windowId, inv, buf))
            );

    public static final DeferredHolder<MenuType<?>, MenuType<ScientistMenu>> SCIENTIST_MENU =
            MENUS.register("scientist_menu", () -> IMenuTypeExtension.create(ScientistMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MedicBlockContructorMenu>> MEDIC_BLOCK_CONSTRUCTOR_MENU =
            MENUS.register("medic_block_constructor_menu",
                    () -> IMenuTypeExtension.create((id, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        BlockEntity be = inv.player.level().getBlockEntity(pos);
                        if (!(be instanceof MedicBlockCrafterEntity entity)) {
                            throw new IllegalStateException("BlockEntity no es MedicBlockCrafterEntity");
                        }
                        return new MedicBlockContructorMenu(id, inv, entity, entity.data);
                    }));

    public static final DeferredHolder<MenuType<?>, MenuType<cryomenu>> CRYO_MENU =
            MENUS.register("cryo", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return new cryomenu(windowId, inv, inv.player.level().getBlockEntity(pos));
            }));

    public static final DeferredHolder<MenuType<?>, MenuType<MoundTerrariumMenu>> MOUND_TERRARIUM_MENU =
            MENUS.register("mound_terrarium_menu", () -> IMenuTypeExtension.create(MoundTerrariumMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ImplantMenu>> IMPLANT_MENU =
            MENUS.register("implant_menu",
                    () -> IMenuTypeExtension.create((windowId, inv, data) -> new ImplantMenu(windowId, inv, data)));

    public static final DeferredHolder<MenuType<?>, MenuType<CompoundsMenu>> COMPOUNDS_MENU =
            MENUS.register("compounds_menu",
                    () -> IMenuTypeExtension.create((windowId, inv, data) -> new CompoundsMenu(windowId, inv, data)));

    public static final DeferredHolder<MenuType<?>, MenuType<RaidControlerMenu>> RAID_CONTROLER =
            MENUS.register("raid_controler",
                    () -> IMenuTypeExtension.create(RaidControlerMenu::new));

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(
            String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}