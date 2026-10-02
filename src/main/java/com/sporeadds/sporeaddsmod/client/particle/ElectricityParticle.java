package com.sporeadds.sporeaddsmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class ElectricityParticle extends TextureSheetParticle {

    private final SpriteSet spriteSet;

    protected ElectricityParticle(ClientLevel level, double x, double y, double z,
                                  double dx, double dy, double dz, SpriteSet spriteSet) {
        super(level, x, y, z, dx, dy, dz);

        this.spriteSet = spriteSet;
        this.gravity = 0.0F;
        this.lifetime = 5;
        this.quadSize = 0.5F;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.hasPhysics = false;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new ElectricityParticle(level, x, y, z, dx, dy, dz, spriteSet);
        }
    }
}