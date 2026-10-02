package com.sporeadds.sporeaddsmod.client.trainingbook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;

/** Client-only code kept out of TrainingBookItem so a dedicated server never loads these classes. */
public final class TrainingBookClientHooks {

    private TrainingBookClientHooks() {
    }

    public static void open(InteractionHand hand) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        minecraft.setScreen(new TrainingBookScreen(hand));
    }
}
