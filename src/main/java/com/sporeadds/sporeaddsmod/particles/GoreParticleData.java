package com.sporeadds.sporeaddsmod.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public class GoreParticleData implements ParticleOptions {

    public static final Deserializer<GoreParticleData> DESERIALIZER = new Deserializer<>() {
        @Override
        public GoreParticleData fromCommand(ParticleType<GoreParticleData> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int variant = reader.readInt();
            return new GoreParticleData(type, variant);
        }

        @Override
        public GoreParticleData fromNetwork(ParticleType<GoreParticleData> type, FriendlyByteBuf buf) {
            return new GoreParticleData(type, buf.readInt());
        }
    };

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

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(variant);
    }

    @Override
    public String writeToString() {
        return type.toString() + " " + variant;
    }
}