package com.sporeadds.sporeaddsmod.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class GoreParticleType extends ParticleType<GoreParticleData> {

    private final MapCodec<GoreParticleData> codec = GoreParticleData.codec(this);
    private final StreamCodec<RegistryFriendlyByteBuf, GoreParticleData> streamCodec = GoreParticleData.streamCodec(this);

    public GoreParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public MapCodec<GoreParticleData> codec() {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, GoreParticleData> streamCodec() {
        return streamCodec;
    }
}
