package com.sporeadds.sporeaddsmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Falling streak used to reskin the Abyssal Power13 storm's rain visuals. */
public class RainParticle extends TextureSheetParticle {

    protected RainParticle(ClientLevel level, double x, double y, double z,
                            double dx, double dy, double dz, SpriteSet spriteSet) {
        super(level, x, y, z, dx, dy, dz);

        this.gravity = 1.2F;
        this.hasPhysics = true;
        this.lifetime = 20 + this.random.nextInt(10);
        this.quadSize = 0.12F + this.random.nextFloat() * 0.06F;
        this.xd = dx;
        this.yd = -1.4D - this.random.nextDouble() * 0.4D;
        this.zd = dz;
        this.alpha = 0.85F;

        this.setSprite(spriteSet.get(0, 1));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime || this.onGround) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
        }
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
            return new RainParticle(level, x, y, z, dx, dy, dz, spriteSet);
        }
    }
}
