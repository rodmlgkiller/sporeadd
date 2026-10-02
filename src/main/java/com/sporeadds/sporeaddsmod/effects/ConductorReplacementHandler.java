package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "sporeadd")
public final class ConductorReplacementHandler {

    private static final int CONDUCTOR_DEFIBRILLATION_DURATION_TICKS = 20 * 60;
    private static final int CONDUCTOR_RESISTANCE_DURATION_TICKS = 20 * 5;
    private static final int CONDUCTOR_RESISTANCE_AMPLIFIER = 2;

    private ConductorReplacementHandler() {
    }

    @SubscribeEvent(
            priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST,
            receiveCanceled = true
    )
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean wasInMinigame = SelfDefibrillateAbility.isAttemptActive(player.getUUID());
        if (!wasInMinigame) {
            return;
        }

        boolean hitBySporeNonGhost = SporeTeamCombatTracker.wasHitBySporeNonGhost(player.getUUID());

        SelfDefibrillateAbility.forceEndAttempt(player);

        if (!hitBySporeNonGhost) {
            return;
        }

        DelayedDefibrillationEffectHandler.triggerSharedLightningImpact(serverLevel, player);
        spawnConductor(serverLevel, player);
    }

    private static void spawnConductor(ServerLevel level, ServerPlayer player) {
        ResourceLocation conductorId = new ResourceLocation("spore", "conductor");
        EntityType<?> conductorType = ForgeRegistries.ENTITY_TYPES.getValue(conductorId);

        if (conductorType == null) {
            return;
        }

        var entity = conductorType.create(level);
        if (!(entity instanceof Mob conductor)) {
            return;
        }

        conductor.moveTo(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        );

        conductor.setCustomName(Component.literal(player.getName().getString()));
        conductor.setCustomNameVisible(true);
        conductor.setPersistenceRequired();

        copyArmor(player, conductor);

        conductor.addEffect(new MobEffectInstance(
                effects.DELAYED_DEFIBRILLATION.get(),
                CONDUCTOR_DEFIBRILLATION_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));

        conductor.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                CONDUCTOR_RESISTANCE_DURATION_TICKS,
                CONDUCTOR_RESISTANCE_AMPLIFIER,
                false,
                true,
                true
        ));

        level.addFreshEntity(conductor);

        copyArmor(player, conductor);
    }

    private static void copyArmor(ServerPlayer player, Mob conductor) {
        copyArmorSlot(player, conductor, EquipmentSlot.HEAD);
        copyArmorSlot(player, conductor, EquipmentSlot.CHEST);
        copyArmorSlot(player, conductor, EquipmentSlot.LEGS);
        copyArmorSlot(player, conductor, EquipmentSlot.FEET);
    }

    private static void copyArmorSlot(ServerPlayer player, Mob conductor, EquipmentSlot slot) {
        ItemStack playerStack = player.getItemBySlot(slot);

        if (playerStack.isEmpty()) {
            conductor.setItemSlot(slot, ItemStack.EMPTY);
        } else {
            conductor.setItemSlot(slot, playerStack.copy());
        }

        conductor.setDropChance(slot, 0.0F);
    }
}