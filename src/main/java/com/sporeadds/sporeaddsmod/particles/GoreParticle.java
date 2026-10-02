package com.sporeadds.sporeaddsmod.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;

public class GoreParticle extends TextureSheetParticle {

    public static final String[] VARIANT_NAMES = {
            "gore",
            "gore2",
            "gore3",
            "gore4",
            "gore5",
            "gore_b",
            "gore_b2",
            "gore_b3",
            "gore_b4",
            "gore_l",
            "gore_l2",
            "gores",
            "blood",
            "blood1",
            "blood2",
            "blood3",
            "blood4",
            "blood5"
    };

    private static final int SOLID_LIFETIME = 20 * 30;
    private static final int FADE_LIFETIME = 20 * 3;
    private static final int TOTAL_LIFETIME = SOLID_LIFETIME + FADE_LIFETIME;

    private final SpriteSet sprites;
    private final int variantIndex;

    protected GoreParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites, int variantIndex) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;

        if (variantIndex < 0 || variantIndex >= VARIANT_NAMES.length) {
            this.variantIndex = this.random.nextInt(VARIANT_NAMES.length);
        } else {
            this.variantIndex = variantIndex;
        }

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.hasPhysics = true;
        this.gravity = 0.25F;
        this.friction = 0.90F;
        this.quadSize = 0.2F + this.random.nextFloat() * 0.25F;
        this.lifetime = TOTAL_LIFETIME;
        this.alpha = 1.0F;

        this.pickSpriteByVariant();
    }

    private void pickSpriteByVariant() {
        this.setSprite(this.sprites.get(this.variantIndex, VARIANT_NAMES.length));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd -= 0.04D * this.gravity;
        this.move(this.xd, this.yd, this.zd);

        if (this.onGround) {
            if (Math.abs(this.yd) > 0.03D) {
                this.yd *= -0.08D;
            } else {
                this.yd = 0.0D;
            }

            this.xd *= 0.25D;
            this.zd *= 0.25D;

            if (Math.abs(this.xd) < 0.003D) this.xd = 0.0D;
            if (Math.abs(this.zd) < 0.003D) this.zd = 0.0D;
        }

        this.xd *= this.friction;
        this.yd *= 0.98D;
        this.zd *= this.friction;

        if (this.age >= SOLID_LIFETIME) {
            float fadeProgress = (float) (this.age - SOLID_LIFETIME) / FADE_LIFETIME;
            this.alpha = 1.0F - Mth.clamp(fadeProgress, 0.0F, 1.0F);
        } else {
            this.alpha = 1.0F;
        }

        if (this.alpha <= 0.0F) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<GoreParticleData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(GoreParticleData data, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new GoreParticle(level, x, y, z, xd, yd, zd, this.sprites, data.getVariant());
        }
    }
}