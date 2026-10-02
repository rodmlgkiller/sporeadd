package com.sporeadds.sporeaddsmod.effects;

import com.sporeadds.sporeaddsmod.Powers.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class effects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "sporeadd");

    public static final RegistryObject<MobEffect> EXPOSED =
            MOB_EFFECTS.register("exposed", ExposedEffect::new);

    public static final RegistryObject<MobEffect> TERMINA =
            MOB_EFFECTS.register("termina", TerminaEffect::new);

    public static final RegistryObject<MobEffect> CAMOUFLAGED =
            MOB_EFFECTS.register("camouflaged", CamouflagedEffect::new);

    public static final RegistryObject<MobEffect> VULNERABLE =
            MOB_EFFECTS.register("vulnerable", VulnerableEffect::new);

    public static final RegistryObject<MobEffect> DISSOLUTION =
            MOB_EFFECTS.register("dissolution", DissolutionEffect::new);

    public static final RegistryObject<MobEffect> CONSTRICTION =
            MOB_EFFECTS.register("constriction", ConstrictionEffect::new);

    public static final RegistryObject<MobEffect> DEHYDRATION =
            MOB_EFFECTS.register("dehydration", DehydrationEffect::new);

    public static final RegistryObject<MobEffect> SUBJUGATION =
            MOB_EFFECTS.register("subjugation", SubjugationEffect::new);

    public static final RegistryObject<MobEffect> SEASONED =
            MOB_EFFECTS.register("seasoned", SeasonedEffect::new);

    public static final RegistryObject<MobEffect> EXQUISITE_CUISINE =
            MOB_EFFECTS.register("exquisite_cuisine", ExquisiteCuisineEffect::new);

    public static final RegistryObject<MobEffect> FAMINED =
            MOB_EFFECTS.register("famined", FaminedEffect::new);

    public static final RegistryObject<MobEffect> MANGLED =
            MOB_EFFECTS.register("mangled", MangledEffect::new);

    public static final RegistryObject<MobEffect> ANTICIPATION =
            MOB_EFFECTS.register("anticipation", AnticipationEffect::new);

    public static final RegistryObject<MobEffect> CLIPPED_WINGS =
            MOB_EFFECTS.register("clipped_wings", ClippedWingsEffect::new);

    public static final RegistryObject<MobEffect> ENCHAINED =
            MOB_EFFECTS.register("enchained", EnchainedEffect::new);

    public static final RegistryObject<MobEffect> AMBUSHED =
            MOB_EFFECTS.register("ambushed", AmbushedEffect::new);

    public static final RegistryObject<MobEffect> CRITICAL_WOUND =
            MOB_EFFECTS.register("critical_wound", CriticalWoundEffect::new);

    public static final RegistryObject<MobEffect> EXPOSED_WEAKNESS =
            MOB_EFFECTS.register("exposed_weakness", ExposedWeaknessEffect::new);

    public static final RegistryObject<MobEffect> DELAYED_DEFIBRILLATION =
            MOB_EFFECTS.register("delayed_defibrillation", DelayedDefibrillationMobEffect::new);

    public static final RegistryObject<MobEffect> POSTMORTEM =
            MOB_EFFECTS.register("postmortem", PostmortemMobEffect::new);

    public static final RegistryObject<MobEffect> CALL_OF_THE_HIVE =
            MOB_EFFECTS.register("call_of_the_hive", CallOfTheHiveEffect::new);

    public static final RegistryObject<MobEffect> PUNISHMENT =
            MOB_EFFECTS.register("punishment", PunishmentEffect::new);

    public static final RegistryObject<MobEffect> DEVOTION =
            MOB_EFFECTS.register("devotion", DevotionEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

}