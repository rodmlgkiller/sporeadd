package com.sporeadds.sporeaddsmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Same gas/gas2/gas3 look as GasParticle, at 50% the quad size - used for the Caustic spray's muzzle puffs. */
public class GasSmallParticle extends TextureSheetParticle {

    protected GasSmallParticle(ClientLevel level, double x, double y, double z,
                                double dx, double dy, double dz, SpriteSet spriteSet) {
        super(level, x, y, z, dx, dy, dz);

        this.gravity = 0.0F;
        this.lifetime = 60 + this.random.nextInt(40);
        this.quadSize = 0.3F + this.random.nextFloat() * 0.2F;
        this.xd = dx * 0.2D;
        this.yd = 0.02D + this.random.nextDouble() * 0.03D;
        this.zd = dz * 0.2D;
        this.hasPhysics = false;
        this.alpha = 0.0F;

        this.setSprite(spriteSet.get(this.random.nextInt(3), 3));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.yd += 0.001D;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98D;
            this.zd *= 0.98D;

            float lifeRatio = (float) this.age / (float) this.lifetime;
            if (lifeRatio < 0.15F) {
                this.alpha = lifeRatio / 0.15F * 0.6F;
            } else if (lifeRatio > 0.7F) {
                this.alpha = (1.0F - lifeRatio) / 0.3F * 0.6F;
            } else {
                this.alpha = 0.6F;
            }
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
            return new GasSmallParticle(level, x, y, z, dx, dy, dz, spriteSet);
        }
    }
}
