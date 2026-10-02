package com.sporeadds.sporeaddsmod.client.model.animations;

import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationChannel;

// Save this class in your mod and generate all required imports
/**
 * Made with Blockbench 4.12.6 Exported for Minecraft version 1.19 or later with
 * Mojang mappings
 * 
 * @author Author
 */
public class Medic_blockAnimation {
	public static final AnimationDefinition close = AnimationDefinition.Builder.withLength(1.0F)
			.addAnimation("Puerta",
					new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Puerta",
					new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Puerta", new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Centrifugadora",
					new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0833F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, -357.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("Centrifugadora", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Centrifugadora", new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe(0.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Boton", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.2F), AnimationChannel.Interpolations.LINEAR), new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.1F), AnimationChannel.Interpolations.LINEAR)))
			.build();
	public static final AnimationDefinition open = AnimationDefinition.Builder.withLength(1.0F)
			.addAnimation("Puerta",
					new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Puerta",
					new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Puerta", new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Centrifugadora",
					new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("Centrifugadora", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Centrifugadora", new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Boton", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.1F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
			.build();
	public static final AnimationDefinition woriking = AnimationDefinition.Builder.withLength(6.7917F)
			.addAnimation("Puerta", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Puerta", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("Boton",
					new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.1F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(0.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("TODO",
					new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-2.5F, -2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.1667F, KeyframeAnimations.degreeVec(2.5192F, -7.4952F, -0.2197F), AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0192F, -7.4952F, -0.2197F), AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.3479F, -7.4872F, -2.7412F), AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-2.8097F, -7.4889F, 2.3018F), AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-2.143F, -7.705F, -2.7378F), AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-2.7102F, -7.5209F, 1.5498F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.125F, KeyframeAnimations.degreeVec(-2.7102F, -7.5209F, 1.5498F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.25F, KeyframeAnimations.degreeVec(-3.6724F, -7.1028F, 9.0996F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.2917F, KeyframeAnimations.degreeVec(-2.2126F, -7.4168F, 2.3521F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.4583F, KeyframeAnimations.degreeVec(0.7537F, -7.5414F, -1.9796F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.5417F, KeyframeAnimations.degreeVec(0.4221F, -7.5671F, 0.5421F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.4221F, -7.5671F, 0.5421F), AnimationChannel.Interpolations.LINEAR)))
			.addAnimation("TODO",
					new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.2F, 0.0F), AnimationChannel.Interpolations.LINEAR), new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.1F, 0.0F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(0.3333F, KeyframeAnimations.posVec(-0.4F, 0.1F, 0.2F), AnimationChannel.Interpolations.LINEAR), new Keyframe(0.4583F, KeyframeAnimations.posVec(-0.4F, 0.7F, 0.2F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(0.6667F, KeyframeAnimations.posVec(-0.4F, -0.1F, 0.2F), AnimationChannel.Interpolations.LINEAR), new Keyframe(0.8333F, KeyframeAnimations.posVec(-0.4F, -0.2F, 0.2F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.125F, KeyframeAnimations.posVec(-0.4F, -0.2F, 0.2F), AnimationChannel.Interpolations.LINEAR), new Keyframe(1.25F, KeyframeAnimations.posVec(-0.4F, 1.1F, 0.2F), AnimationChannel.Interpolations.LINEAR),
							new Keyframe(1.4583F, KeyframeAnimations.posVec(-0.4F, 0.0F, 0.2F), AnimationChannel.Interpolations.LINEAR), new Keyframe(1.75F, KeyframeAnimations.posVec(-0.4F, 0.0F, 0.2F), AnimationChannel.Interpolations.LINEAR)))
			.build();
}