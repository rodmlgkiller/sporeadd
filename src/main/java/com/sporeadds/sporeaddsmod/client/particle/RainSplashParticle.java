package com.sporeadds.sporeaddsmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;

/** Ground splash used to reskin the Abyssal Power13 storm's rain-drop impacts (vanilla's splash particle, recolored). */
public class RainSplashParticle extends TextureSheetParticle {

    protected RainSplashParticle(ClientLevel level, double x, double y, double z,
                                  double dx, double dy, double dz, SpriteSet spriteSet) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);

        this.xd *= 0.3D;
        this.yd = Math.random() * 0.2D + 0.1D;
        this.zd *= 0.3D;
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.04F;
        this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));

        if (dy == 0.0D && (dx != 0.0D || dz != 0.0D)) {
            this.xd = dx;
            this.yd = 0.1D;
            this.zd = dz;
        }

        this.setSprite(spriteSet.get(this.random.nextInt(4), 4));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98D;
            this.yd *= 0.98D;
            this.zd *= 0.98D;

            if (this.onGround) {
                if (Math.random() < 0.5D) {
                    this.remove();
                }
                this.xd *= 0.7D;
                this.zd *= 0.7D;
            }

            BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
            double d0 = Math.max(
                    this.level.getBlockState(blockpos).getCollisionShape(this.level, blockpos)
                            .max(Direction.Axis.Y, this.x - blockpos.getX(), this.z - blockpos.getZ()),
                    this.level.getFluidState(blockpos).getHeight(this.level, blockpos));
            if (d0 > 0.0D && this.y < blockpos.getY() + d0) {
                this.remove();
            }
        }
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
            return new RainSplashParticle(level, x, y, z, dx, dy, dz, spriteSet);
        }
    }
}
