package com.sporeadds.sporeaddsmod.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class GoreParticleType extends ParticleType<GoreParticleData> {

    public GoreParticleType(boolean overrideLimiter) {
        super(overrideLimiter, GoreParticleData.DESERIALIZER);
    }

    @Override
    public Codec<GoreParticleData> codec() {
        return Codec.INT.xmap(
                variant -> new GoreParticleData(this, variant),
                GoreParticleData::getVariant
        );
    }
}