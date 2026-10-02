package com.sporeadds.sporeaddsmod.client.hive;

import com.sporeadds.sporeaddsmod.ModSounds;
import com.sporeadds.sporeaddsmod.network.HiveCinematicDonePacket;
import com.sporeadds.sporeaddsmod.network.HiveDialogueAdvancePacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SubmitHiveChoicePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Secuencia cinemática del modo "downed but not out" de la colmena (Call of the Hive).
 *
 * 1. Oscurecido total de pantalla + reducción de sonido al 10% en una transición de 3 s.
 * 2. Diálogo de la colmena, carácter a carácter (0.05 s/carácter). Cada bloque salvo el último:
 *    escribe -> mantiene 5 s -> se transparenta en 1 s -> siguiente.
 * 3. Bloque 7 ("What will it be?"): se queda fijo y aparecen dos "Give Up": el de la IZQUIERDA
 *    en rojo oscuro = rendición; el de la DERECHA normal = perecer. Timer blanco de 20 s en medio.
 *    Timeout = "Give Up" normal (perecer).
 * 4. Tras la decisión, el servidor manda el bloque post-decisión ("Very well..." + "Now arise..."
 *    o "Then perish"), y al terminar se revierte la transición y se cierra.
 */
public class HiveCinematicScreen extends Screen {

    private static final String[] DIALOGUE = {
            "...",
            "I can see you have come far",
            "But that's all",
            "Resistance its pointless",
            "You never stood chance, the best you can do is give up now and embrace the change.",
            "Surrender now and i will let you live and become part of the new world",
            "What will it be?"
    };

    private static final int CHAR_DELAY_TICKS = 1;        // 0.05 s por carácter
    private static final int HOLD_AFTER_PART_TICKS = 100;  // 5 s
    private static final int PART_FADE_TICKS = 20;         // 1 s
    private static final int SCREEN_FADE_TICKS = 60;       // 3 s
    private static final int CHOICE_TIMEOUT_TICKS = 400;   // 20 s
    private static final int POST_HOLD_TICKS = 30;
    private static final int POST_FIRST_LINE_EXTRA_HOLD = 40;

    private static final int TEXT_COLOR = 0x6E0000;        // rojo oscuro (sin alpha)
    private static final int GIVEUP_NORMAL_COLOR = 0xC8C8C8;
    private static final int GIVEUP_RED_COLOR = 0x6E0000;
    private static final int TIMER_COLOR = 0xFFFFFFFF;

    private static final int BUTTON_W = 130;
    private static final int BUTTON_H = 28;

    private enum Phase {
        FADE_IN,
        DIALOGUE,
        CHOICE,
        POST,
        FADE_OUT,
        DONE
    }

    private Phase phase = Phase.FADE_IN;
    private int phaseTick = 0;

    // diálogo principal
    private int partIndex = 0;
    private int shownChars = 0;
    private int typeCounter = 0;
    private int holdCounter = 0;
    private boolean partFading = false;
    private int partFadeTick = 0;
    private float partAlpha = 1.0F;

    // elección
    private boolean buttonsVisible = false;
    private int choiceTimer = CHOICE_TIMEOUT_TICKS;
    private Boolean chosenSurrender = null;

    // bloque post-decisión
    private String[] postLines = null;
    private int postIndex = 0;
    private int postShownChars = 0;
    private int postTypeCounter = 0;
    private int postHoldCounter = 0;
    private boolean postHoldForever = false;

    private float darkness = 0.0F;
    private boolean duckEnded = false;

    /** Si true, la única opción es el "give up" rojo (rendición); el timeout también rinde. */
    private final boolean forceSurrender;

    public HiveCinematicScreen(boolean forceSurrender) {
        super(Component.literal("Call of the Hive"));
        this.forceSurrender = forceSurrender;
    }

    @Override
    protected void init() {
        HiveSoundDamper.beginDuck(SCREEN_FADE_TICKS, 0.10F);
    }

    // ------------------------------------------------------------------ recibido del servidor

    public void onServerPhase(String serverPhase) {
        if (HiveDialogueAdvancePacket.PHASE_ABORT.equals(serverPhase)) {
            beginFadeOut();
            return;
        }

        if (HiveDialogueAdvancePacket.PHASE_VERY_WELL.equals(serverPhase)) {
            postLines = new String[]{"Very well..."};
            postHoldForever = true;              // se queda en "Very well..." hasta que llegue "arise"
        } else if (HiveDialogueAdvancePacket.PHASE_ARISE.equals(serverPhase)) {
            postLines = new String[]{"Now arise..."};
            postHoldForever = false;             // tras "Now arise..." se revierte y cierra
        } else {
            postLines = new String[]{"Then perish"};
            postHoldForever = false;
        }

        postIndex = 0;
        postShownChars = 0;
        postTypeCounter = 0;
        postHoldCounter = 0;
        buttonsVisible = false;
        // Si el diálogo llega antes de terminar el fundido de entrada (p.ej. al reconectar),
        // saltamos directamente a pantalla oscura para que el texto se vea.
        if (darkness < 1.0F) {
            darkness = 1.0F;
        }
        phase = Phase.POST;
        phaseTick = 0;
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        phaseTick++;

        switch (phase) {
            case FADE_IN -> {
                darkness = Mth.clamp(phaseTick / (float) SCREEN_FADE_TICKS, 0.0F, 1.0F);
                if (phaseTick >= SCREEN_FADE_TICKS) {
                    darkness = 1.0F;
                    phase = Phase.DIALOGUE;
                    phaseTick = 0;
                    resetPartTyping();
                }
            }
            case DIALOGUE -> tickDialogue();
            case CHOICE -> tickChoice();
            case POST -> tickPost();
            case FADE_OUT -> {
                darkness = Mth.clamp(1.0F - phaseTick / (float) SCREEN_FADE_TICKS, 0.0F, 1.0F);
                if (phaseTick >= SCREEN_FADE_TICKS) {
                    phase = Phase.DONE;
                    NetworkHandle.INSTANCE.sendToServer(new HiveCinematicDonePacket());
                    onClose();
                }
            }
            default -> {
            }
        }
    }

    private void resetPartTyping() {
        shownChars = 0;
        typeCounter = 0;
        holdCounter = 0;
        partFading = false;
        partFadeTick = 0;
        partAlpha = 1.0F;
    }

    private void tickDialogue() {
        String line = DIALOGUE[partIndex];

        if (shownChars < line.length()) {
            if (++typeCounter >= CHAR_DELAY_TICKS) {
                typeCounter = 0;
                shownChars++;
                playType(line, shownChars);
            }
            return;
        }

        boolean lastPart = partIndex == DIALOGUE.length - 1;

        if (lastPart) {
            // "What will it be?" se queda; pasar a la elección.
            phase = Phase.CHOICE;
            phaseTick = 0;
            buttonsVisible = true;
            choiceTimer = CHOICE_TIMEOUT_TICKS;
            return;
        }

        if (!partFading) {
            if (++holdCounter >= HOLD_AFTER_PART_TICKS) {
                partFading = true;
                partFadeTick = 0;
            }
            return;
        }

        partFadeTick++;
        partAlpha = Mth.clamp(1.0F - partFadeTick / (float) PART_FADE_TICKS, 0.0F, 1.0F);

        if (partFadeTick >= PART_FADE_TICKS) {
            partIndex++;
            resetPartTyping();
        }
    }

    private void tickChoice() {
        if (chosenSurrender != null) return;

        if (--choiceTimer <= 0) {
            choose(forceSurrender);   // force = rendición; si no, "give up" normal (perecer)
        }
    }

    private void tickPost() {
        if (postLines == null) {
            beginFadeOut();
            return;
        }

        String line = postLines[postIndex];

        if (postShownChars < line.length()) {
            if (++postTypeCounter >= CHAR_DELAY_TICKS) {
                postTypeCounter = 0;
                postShownChars++;
                playType(line, postShownChars);
            }
            return;
        }

        int holdTarget = POST_HOLD_TICKS + (postIndex == 0 && postLines.length > 1 ? POST_FIRST_LINE_EXTRA_HOLD : 0);

        if (++postHoldCounter < holdTarget) {
            return;
        }

        if (postIndex < postLines.length - 1) {
            postIndex++;
            postShownChars = 0;
            postTypeCounter = 0;
            postHoldCounter = 0;
        } else if (!postHoldForever) {
            beginFadeOut();
        }
    }

    private void beginFadeOut() {
        if (!duckEnded) {
            HiveSoundDamper.endDuck();
            duckEnded = true;
        }
        phase = Phase.FADE_OUT;
        phaseTick = 0;
    }

    private void choose(boolean surrender) {
        if (chosenSurrender != null) return;
        chosenSurrender = surrender;
        buttonsVisible = false;
        NetworkHandle.INSTANCE.sendToServer(new SubmitHiveChoicePacket(surrender));
    }

    private void playType(String line, int idx) {
        if (idx <= 0 || idx > line.length()) return;
        if (darkness < 0.98F) return;
        if (Character.isWhitespace(line.charAt(idx - 1))) return;

        SoundEvent sound = ModSounds.HIVE_TYPE.get();
        if (sound == null) return;

        float pitch = 0.95F + (float) Math.random() * 0.12F;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, 0.35F));
    }

    // ------------------------------------------------------------------ render

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)  {
        int alpha = (int) (darkness * 255.0F) & 0xFF;
        graphics.fill(0, 0, this.width, this.height, alpha << 24);

        // Flecha discreta para saltar hasta el momento de elegir.
        if (canSkip()) {
            renderSkipArrow(graphics, mouseX, mouseY);
        }

        if (darkness < 0.98F) {
            return;
        }

        if (phase == Phase.DIALOGUE || phase == Phase.CHOICE) {
            renderTypedBlock(graphics, DIALOGUE[partIndex], shownChars,
                    phase == Phase.CHOICE ? 1.0F : partAlpha, this.height / 2 - 30);
        } else if (phase == Phase.POST && postLines != null) {
            renderTypedBlock(graphics, postLines[postIndex], postShownChars, 1.0F, this.height / 2 - 10);
        }

        if (buttonsVisible && chosenSurrender == null) {
            renderChoice(graphics, mouseX, mouseY);
        }
    }

    /** true mientras se puede saltar (aún no hemos llegado a la elección ni al post). */
    private boolean canSkip() {
        return chosenSurrender == null && (phase == Phase.FADE_IN || phase == Phase.DIALOGUE);
    }

    private boolean hitSkipArrow(double mouseX, double mouseY) {
        int w = this.font.width(">>");
        int x0 = this.width - w - 12;
        int y0 = this.height - this.font.lineHeight - 8;
        return mouseX >= x0 - 4 && mouseX <= x0 + w + 4
                && mouseY >= y0 - 4 && mouseY <= y0 + this.font.lineHeight + 4;
    }

    private void renderSkipArrow(GuiGraphics graphics, int mouseX, int mouseY) {
        int w = this.font.width(">>");
        int x0 = this.width - w - 12;
        int y0 = this.height - this.font.lineHeight - 8;
        boolean hover = hitSkipArrow(mouseX, mouseY);
        int color = hover ? 0xCCFFFFFF : 0x55FFFFFF;
        graphics.drawString(this.font, ">>", x0, y0, color, false);
    }

    private void skipToChoice() {
        darkness = 1.0F;
        partIndex = DIALOGUE.length - 1;
        shownChars = DIALOGUE[partIndex].length();
        partAlpha = 1.0F;
        partFading = false;
        phase = Phase.CHOICE;
        phaseTick = 0;
        buttonsVisible = true;
        choiceTimer = CHOICE_TIMEOUT_TICKS;
    }

    private void renderTypedBlock(GuiGraphics graphics, String fullLine, int shown, float alpha, int centerY) {
        if (alpha <= 0.01F) return;

        String visible = fullLine.substring(0, Mth.clamp(shown, 0, fullLine.length()));
        if (visible.isEmpty()) return;

        int a = Mth.clamp((int) (alpha * 255.0F), 6, 255);
        int color = (a << 24) | TEXT_COLOR;

        int maxWidth = (int) (this.width * 0.6F);
        List<FormattedCharSequence> lines = this.font.split(Component.literal(visible), maxWidth);

        int lineHeight = this.font.lineHeight + 3;
        int totalHeight = lines.size() * lineHeight;
        int y = centerY - totalHeight / 2;

        for (FormattedCharSequence seq : lines) {
            int x = (this.width - this.font.width(seq)) / 2;
            graphics.drawString(this.font, seq, x, y, color, false);
            y += lineHeight;
        }
    }

    private void renderChoice(GuiGraphics graphics, int mouseX, int mouseY) {
        int y = this.height / 2 + 40;

        if (forceSurrender) {
            // Solo el "give up" rojo, centrado.
            drawGiveUp(graphics, this.width / 2, y, mouseX, mouseY, GIVEUP_RED_COLOR);
            int secs = Math.max(0, (choiceTimer + 19) / 20);
            graphics.drawCenteredString(this.font, Component.literal(String.valueOf(secs)),
                    this.width / 2, y + BUTTON_H + 6, TIMER_COLOR);
            return;
        }

        int leftCx = this.width / 2 - 120;
        int rightCx = this.width / 2 + 120;

        // IZQUIERDA = rojo oscuro (rendición) ; DERECHA = normal (perecer)
        drawGiveUp(graphics, leftCx, y, mouseX, mouseY, GIVEUP_RED_COLOR);
        drawGiveUp(graphics, rightCx, y, mouseX, mouseY, GIVEUP_NORMAL_COLOR);

        int secondsLeft = Math.max(0, (choiceTimer + 19) / 20);
        graphics.drawCenteredString(this.font, Component.literal(String.valueOf(secondsLeft)),
                this.width / 2, y + (BUTTON_H - this.font.lineHeight) / 2, TIMER_COLOR);
    }

    private void drawGiveUp(GuiGraphics graphics, int centerX, int y, int mouseX, int mouseY, int textColor) {
        int x0 = centerX - BUTTON_W / 2;
        int x1 = centerX + BUTTON_W / 2;
        boolean hover = mouseX >= x0 && mouseX <= x1 && mouseY >= y && mouseY <= y + BUTTON_H;

        int border = hover ? 0xFFFFFFFF : 0xFF6E6E6E;
        graphics.fill(x0, y, x1, y + BUTTON_H, 0xB0000000);
        graphics.fill(x0, y, x1, y + 1, border);
        graphics.fill(x0, y + BUTTON_H - 1, x1, y + BUTTON_H, border);
        graphics.fill(x0, y, x0 + 1, y + BUTTON_H, border);
        graphics.fill(x1 - 1, y, x1, y + BUTTON_H, border);

        int color = 0xFF000000 | textColor;
        graphics.drawCenteredString(this.font, Component.literal("Give Up"),
                centerX, y + (BUTTON_H - this.font.lineHeight) / 2, color);
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && canSkip() && hitSkipArrow(mouseX, mouseY)) {
            skipToChoice();
            return true;
        }

        if (phase == Phase.CHOICE && buttonsVisible && chosenSurrender == null && button == 0) {
            int y = this.height / 2 + 40;

            if (forceSurrender) {
                if (hit(mouseX, mouseY, this.width / 2, y)) {
                    choose(true);
                }
                return true;
            }

            int leftCx = this.width / 2 - 120;
            int rightCx = this.width / 2 + 120;

            if (hit(mouseX, mouseY, leftCx, y)) {
                choose(true);   // izquierda rojo oscuro = rendición
                return true;
            }
            if (hit(mouseX, mouseY, rightCx, y)) {
                choose(false);  // derecha normal = perecer
                return true;
            }
        }
        return true;
    }

    private boolean hit(double mouseX, double mouseY, int centerX, int y) {
        return mouseX >= centerX - BUTTON_W / 2.0
                && mouseX <= centerX + BUTTON_W / 2.0
                && mouseY >= y
                && mouseY <= y + BUTTON_H;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return true;
    }

    @Override
    public void onClose() {
        if (!duckEnded) {
            HiveSoundDamper.endDuck();
            duckEnded = true;
        }
        super.onClose();
    }

    @Override
    public void removed() {
        if (!duckEnded) {
            HiveSoundDamper.endDuck();
            duckEnded = true;
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
