package com.sporeadds.sporeaddsmod.client.screen;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MedicBlockCrafterEntity;
import com.sporeadds.sporeaddsmod.client.gui.MoundTerrariumMenu;
import com.sporeadds.sporeaddsmod.client.gui.RaidControlerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, "sporeadd");

    public static final RegistryObject<MenuType<MedicBlockMenu>> MEDIC_BLOCK_MENU =
            MENUS.register("medic_block_menu", () ->
                    IForgeMenuType.create((windowId, inv, buf) -> new MedicBlockMenu(windowId, inv, buf))
            );

    public static final RegistryObject<MenuType<ScientistMenu>> SCIENTIST_MENU =
            MENUS.register("scientist_menu", () -> IForgeMenuType.create(ScientistMenu::new));

    public static final RegistryObject<MenuType<MedicBlockContructorMenu>> MEDIC_BLOCK_CONSTRUCTOR_MENU =
            MENUS.register("medic_block_constructor_menu",
                    () -> IForgeMenuType.create((id, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        BlockEntity be = inv.player.level().getBlockEntity(pos);
                        if (!(be instanceof MedicBlockCrafterEntity entity)) {
                            throw new IllegalStateException("BlockEntity no es MedicBlockCrafterEntity");
                        }
                        return new MedicBlockContructorMenu(id, inv, entity, entity.data);
                    }));

    public static final RegistryObject<MenuType<cryomenu>> CRYO_MENU =
            MENUS.register("cryo", () -> IForgeMenuType.create((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return new cryomenu(windowId, inv, inv.player.level().getBlockEntity(pos));
            }));

    public static final RegistryObject<MenuType<MoundTerrariumMenu>> MOUND_TERRARIUM_MENU =
            MENUS.register("mound_terrarium_menu", () -> IForgeMenuType.create(MoundTerrariumMenu::new));

    public static final RegistryObject<MenuType<ImplantMenu>> IMPLANT_MENU =
            MENUS.register("implant_menu",
                    () -> IForgeMenuType.create((windowId, inv, data) -> new ImplantMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<CompoundsMenu>> COMPOUNDS_MENU =
            MENUS.register("compounds_menu",
                    () -> IForgeMenuType.create((windowId, inv, data) -> new CompoundsMenu(windowId, inv, data)));

    public static final RegistryObject<MenuType<RaidControlerMenu>> RAID_CONTROLER =
            MENUS.register("raid_controler",
                    () -> IForgeMenuType.create(RaidControlerMenu::new));

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(
            String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}