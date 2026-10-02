package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.client.gui.SporeAbilitySelector;
import com.sporeadds.sporeaddsmod.client.gui.VervaGuiScreen;
import com.sporeadds.sporeaddsmod.client.gui.VervaVariantGuiScreen;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class AbilityKeyHandler {

    private static int causticChargeTicks = 0;
    private static boolean wasCausticShotDown = false;

    private static boolean sprayModeActive = false;
    private static int sprayFireCooldown = 0;

    private static boolean wasgluttonousShotDown = false;

    private static int gluttonousFireCooldown = 0;

    private static final DustParticleOptions BRIGHT_GREEN =
            new DustParticleOptions(new Vec3(0.1F, 1.0F, 0.1F).toVector3f(), 1.2F);

    private static final DustParticleOptions gluttonous_GREEN =
            new DustParticleOptions(new Vec3(0.45F, 0.85F, 0.12F).toVector3f(), 1.2F);

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (gluttonousFireCooldown > 0) gluttonousFireCooldown--;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        handleContinuousInputs(mc);
        handleInstantInputs(mc);
    }

    private static void handleContinuousInputs(Minecraft mc) {
        if (mc.player == null) return;

        handleCausticCharge(mc);
        handlegluttonousCharge(mc);
    }

    private static void handleCausticCharge(Minecraft mc) {
        if (!hasSubclass(mc.player, "kommandant", "caustic")) {
            causticChargeTicks = 0;
            wasCausticShotDown = false;
            sprayModeActive = false;
            return;
        }

        if (sprayModeActive) {
            handleCausticSprayMode(mc);
            return;
        }

        if (SporeKeyMapping.CAUSTIC_SHOT.isDown()) {
            if (causticChargeTicks == 0) {
                mc.player.playSound(
                        BuiltInRegistries.SOUND_EVENT.get(
                                ResourceLocation.fromNamespaceAndPath("spore", "chemist_fuse")
                        ),
                        1.0F, 1.0F
                );
            }

            causticChargeTicks++;
            wasCausticShotDown = true;

            com.sporeadds.sporeaddsmod.client.ClientAbilityChargeState.set(causticChargeTicks, true);

            spawnChargeParticles(mc, BRIGHT_GREEN);
        } else {
            if (wasCausticShotDown) {
                if (causticChargeTicks >= 3) {
                    NetworkHandle.INSTANCE.sendToServer(new CausticShotPacket(causticChargeTicks));
                }
                causticChargeTicks = 0;
                wasCausticShotDown = false;
                com.sporeadds.sporeaddsmod.client.ClientAbilityChargeState.set(0, false);
            }
        }
    }

    /**
     * Caustic's skill 9 spray mode: while active, holding the fire key drains the shared charge bar
     * (at the same 1-tick-per-tick rate it normally fills at) instead of filling it, firing a
     * GasGlobProjectile every 2 ticks with a slight spread until the ammo runs out, at which point it
     * reverts to the normal single-charged-shot mode. The bar stays visible with the remaining ammo
     * even while the key isn't held, so ammo carries over across separate hold/release cycles.
     */
    private static void handleCausticSprayMode(Minecraft mc) {
        if (SporeKeyMapping.CAUSTIC_SHOT.isDown() && causticChargeTicks > 0) {
            causticChargeTicks--;

            if (sprayFireCooldown > 0) {
                sprayFireCooldown--;
            } else {
                NetworkHandle.INSTANCE.sendToServer(new FireCausticSprayShotPacket());
                sprayFireCooldown = 1;
            }

            if (mc.player.getRandom().nextFloat() < 0.25F) {
                spawnChargeParticles(mc, BRIGHT_GREEN);
            }
        }

        if (causticChargeTicks <= 0) {
            sprayModeActive = false;
            com.sporeadds.sporeaddsmod.client.ClientAbilityChargeState.set(0, false);
        } else {
            com.sporeadds.sporeaddsmod.client.ClientAbilityChargeState.set(causticChargeTicks, true);
        }
    }

    /** Called client-side when a CausticSprayModePacket arrives from the server. */
    public static void activateCausticSprayMode() {
        sprayModeActive = true;
        wasCausticShotDown = false;
        sprayFireCooldown = 0;
        causticChargeTicks = com.sporeadds.sporeaddsmod.client.ClientAbilityChargeState.MAX_TICKS;
        com.sporeadds.sporeaddsmod.client.ClientAbilityChargeState.set(causticChargeTicks, true);
    }

    private static void handlegluttonousCharge(Minecraft mc) {
        if (hasSubclass(mc.player, "kommandant", "gluttonous")) {
            if (SporeKeyMapping.CAUSTIC_SHOT.isDown()) {
                if (gluttonousFireCooldown <= 0) {
                    NetworkHandle.INSTANCE.sendToServer(new gluttonousShotPacket());
                    gluttonousFireCooldown = 12;
                }
            }
        }
    }

    private static void spawnChargeParticles(Minecraft mc, DustParticleOptions particle) {
        if (mc.player == null || mc.level == null) return;

        double px = mc.player.getX();
        double py = mc.player.getY() + (mc.player.getBbHeight() / 2.0);
        double pz = mc.player.getZ();

        int particles = 2 + mc.player.getRandom().nextInt(2);
        for (int i = 0; i < particles; i++) {
            double radius = 0.5 + mc.player.getRandom().nextDouble() * 1.2;
            double theta = mc.player.getRandom().nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * mc.player.getRandom().nextDouble() - 1);

            double offsetX = radius * Math.sin(phi) * Math.cos(theta);
            double offsetY = radius * Math.cos(phi);
            double offsetZ = radius * Math.sin(phi) * Math.sin(theta);

            mc.level.addParticle(particle, px + offsetX, py + offsetY, pz + offsetZ, 0, 0.05, 0);
        }
    }

    private static void handleInstantInputs(Minecraft mc) {
        if (mc.player == null) return;

        if (hasSubclass(mc.player, "kommandant", "abyssal")) {
            while (SporeKeyMapping.CAUSTIC_SHOT.consumeClick()) {
                NetworkHandle.INSTANCE.sendToServer(new AbyssalTentaclePacket());
            }
        }

        if (hasBerserkerClass()) {
            if (SporeKeyMapping.ABILITY_1.consumeClick()
                    && !com.sporeadds.sporeaddsmod.client.BerserkerCooldownClientState.isCounterOnCooldown()) {
                NetworkHandle.INSTANCE.sendToServer(new ActivateCounterPacket());
            }
            if (SporeKeyMapping.ABILITY_2.consumeClick()
                    && !com.sporeadds.sporeaddsmod.client.BerserkerCooldownClientState.isClawsOnCooldown()) {
                NetworkHandle.INSTANCE.sendToServer(new ActivateClawsPacket());
            }
        }

        if (hasKommandantClass()) {
            if (SporeKeyMapping.POWER_1.consumeClick()) triggerPower(1);
            if (SporeKeyMapping.POWER_2.consumeClick()) triggerPower(2);
            if (SporeKeyMapping.POWER_3.consumeClick()) triggerPower(3);
            if (SporeKeyMapping.POWER_4.consumeClick()) triggerPower(4);
            if (SporeKeyMapping.POWER_5.consumeClick()) triggerPower(5);
            if (SporeKeyMapping.POWER_6.consumeClick()) triggerPower(6);
            if (SporeKeyMapping.POWER_7.consumeClick()) triggerPower(7);
            if (SporeKeyMapping.POWER_8.consumeClick()) triggerPower(8);
            if (SporeKeyMapping.POWER_9.consumeClick()) triggerPower(9);
            if (SporeKeyMapping.POWER_10.consumeClick()) triggerPower(10);
            if (SporeKeyMapping.POWER_11.consumeClick()) triggerPower(11);
            if (SporeKeyMapping.POWER_12.consumeClick()) triggerPower(12);
            if (SporeKeyMapping.POWER_13.consumeClick()) triggerPower(13);
        }

        if (SporeKeyMapping.OPEN_MENU.consumeClick()) {
            if (!hasKommandantClass()) {
                mc.player.displayClientMessage(
                        Component.translatable("message.sporeadd.no_kommandant"),
                        true
                );
            } else {
                NetworkHandle.INSTANCE.sendToServer(new RequestLevelSyncPacket());

                if (mc.screen instanceof SporeAbilitySelector) {
                    mc.setScreen(null);
                } else {
                    SporeAbilitySelector selector = SporeAbilitySelector.getInstance();
                    mc.setScreen(selector != null ? selector : new SporeAbilitySelector());
                }
            }
        }

        if (SporeKeyMapping.OPEN_VERVA_MENU.consumeClick()) {
            if (!hasKommandantClass()) {
                mc.player.displayClientMessage(
                        Component.translatable("message.sporeadd.no_kommandant"),
                        true
                );
            } else if (!(mc.screen instanceof VervaGuiScreen) && !(mc.screen instanceof VervaVariantGuiScreen)) {
                AtomicInteger playerLevel = new AtomicInteger(0);
                mc.player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(lvl -> {
                    playerLevel.set(lvl.getLevel());
                });

                if (playerLevel.get() >= 4) {
                    NetworkHandle.INSTANCE.sendToServer(new RequestVervaGuiPacket());
                } else {
                    mc.player.displayClientMessage(
                            Component.translatable("tooltip.sporeadd.power8.req_level"),
                            true
                    );
                }
            }
        }

    }

    private static void triggerPower(int abilityNumber) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        AtomicInteger sporeLevel = new AtomicInteger(0);
        mc.player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(lvl -> {
            sporeLevel.set(lvl.getLevel() + 4);
        });

        boolean isUnlocked = (abilityNumber == 13) ? (sporeLevel.get() >= 13) : (sporeLevel.get() >= abilityNumber);

        if (!isUnlocked) return;

        switch (abilityNumber) {
            case 2:
            case 4:
            case 5:
            case 6:
            case 7:
                mc.player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                    String switches = data.getSwitch();
                    if (switches != null && switches.length() > abilityNumber) {
                        char[] chars = switches.toCharArray();
                        chars[abilityNumber] = chars[abilityNumber] == '0' ? '1' : '0';
                        String newSwitches = new String(chars);
                        NetworkHandle.INSTANCE.sendToServer(new DataToServer(newSwitches));
                        data.setSwitch(newSwitches);
                    }
                });
                break;
            case 12:
                NetworkHandle.INSTANCE.sendToServer(new Poder12ActivatePacket());
                break;
            case 13:
                NetworkHandle.INSTANCE.sendToServer(new Poder13UsePacket());
                break;
            default:
                NetworkHandle.INSTANCE.sendToServer(new PowerUseHandler(abilityNumber));
                break;
        }
    }

    private static boolean hasKommandantClass() {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equals(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean hasBerserkerClass() {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "berserker".equals(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean hasSubclass(net.minecraft.world.entity.player.Player player, String mainClass, String subClass) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).map(data ->
                mainClass.equals(data.getIdentifier()) && subClass.equals(data.getSubclass())
        ).orElse(false);
    }
}