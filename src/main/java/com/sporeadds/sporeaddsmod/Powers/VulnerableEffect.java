package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class VulnerableEffect extends MobEffect {
    public VulnerableEffect() {
        // Cambiado el color hexadecimal a 0x00FFFF (Cian)
        super(MobEffectCategory.HARMFUL, 0x00FFFF);
    }

    // No necesitas override aquí, el efecto se aplica en tu listener de evento
    // Puedes añadir métodos extra para partículas, icono o custom behaviors si deseas
}