package com.sporeadds.sporeaddsmod.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class GoreParticleData implements ParticleOptions {

    public static MapCodec<GoreParticleData> codec(ParticleType<GoreParticleData> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("variant").forGetter(GoreParticleData::getVariant)
        ).apply(instance, variant -> new GoreParticleData(type, variant)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, GoreParticleData> streamCodec(ParticleType<GoreParticleData> type) {
        return StreamCodec.composite(
                ByteBufCodecs.VAR_INT, GoreParticleData::getVariant,
                variant -> new GoreParticleData(type, variant)
        );
    }

    private final ParticleType<GoreParticleData> type;
    private final int variant;

    public GoreParticleData(ParticleType<GoreParticleData> type, int variant) {
        this.type = type;
        this.variant = variant;
    }

    public int getVariant() {
        return variant;
    }

    @Override
    public ParticleType<GoreParticleData> getType() {
        return type;
    }
}
