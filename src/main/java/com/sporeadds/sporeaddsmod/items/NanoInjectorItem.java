package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.config.ConfigItems;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class NanoInjectorItem extends Item {
    // Max charges, range, freeze time, effect duration and reload cost: see ScientistConfig (scientist.nano_injector.*).

    public NanoInjectorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.NANO_INJECTOR_REQUIRES_ORIGIN.get(), "scientist"));

        CompoundTag tag = ItemNbt.getOrCreateTag(stack);
        int charges = tag.getInt("nano_charges");

        tooltip.add(Component.literal("Charges: " + charges + " / " + ScientistConfig.nanoInjectorMaxCharges()).withStyle(ChatFormatting.AQUA));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.nano_injector.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.nano_injector.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.nano_injector.ammo").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("tooltip.sporeadds.nano_injector.reload").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.nano_injector.range").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.sporeadds.nano_injector.duration").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.sporeadds.nano_injector.effect").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private boolean hasScientistOrigin(ServerPlayer player) {
        return SporeClassUtil.hasClass(player, "scientist");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);

        ServerPlayer sp = (player instanceof ServerPlayer) ? (ServerPlayer) player : null;
        CompoundTag tag = ItemNbt.getOrCreateTag(stack);
        int charges = tag.getInt("nano_charges");

        final int maxCharges = ScientistConfig.nanoInjectorMaxCharges();
        final double range = ScientistConfig.nanoInjectorRange();
        final int effectDurationTicks = ScientistConfig.nanoInjectorEffectDurationTicks();

        boolean canUse = (sp != null);
        if (canUse && SporeAddsConfig.NANO_INJECTOR_REQUIRES_ORIGIN.get()) {
            canUse = hasScientistOrigin(sp);
        }

        if (player.isCrouching()) {
            if (!canUse) {
                player.displayClientMessage(
                        Component.translatable("message.sporeadds.no_knowledge").withStyle(ChatFormatting.GRAY),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            if (charges < maxCharges) {
                if (tryConsumeReloadCost(player)) {
                    tag.putInt("nano_charges", charges + 1);
                    player.displayClientMessage(
                            Component.translatable("message.sporeadds.nano_injector.recharged", charges + 1, maxCharges)
                                    .withStyle(ChatFormatting.AQUA),
                            true
                    );
                    return InteractionResultHolder.success(stack);
                } else {
                    player.displayClientMessage(
                            Component.translatable("message.sporeadds.nano_injector.no_ammo").withStyle(ChatFormatting.RED),
                            true
                    );
                }
            } else {
                player.displayClientMessage(
                        Component.translatable("message.sporeadds.nano_injector.full").withStyle(ChatFormatting.YELLOW),
                        true
                );
            }

            return InteractionResultHolder.success(stack);
        }

        if (!canUse) {
            player.displayClientMessage(
                    Component.translatable("message.sporeadds.no_knowledge").withStyle(ChatFormatting.GRAY),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        if (charges <= 0) {
            player.displayClientMessage(
                    Component.translatable("message.sporeadds.nano_injector.empty").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        LivingEntity target = findEntityTarget(level, player, range);
        Vec3 start = getHandStart(player, hand);
        Vec3 end = (target != null) ? target.getBoundingBox().getCenter() : start.add(player.getLookAngle().scale(range));

        if (level instanceof ServerLevel serverLevel) {
            spawnParticleBeam(serverLevel, start, end);
        }

        boolean didShoot = false;

        if (target != null) {
            if (target.getTeam() != null && "spore".equalsIgnoreCase(target.getTeam().getName())) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, effectDurationTicks, 0));
                target.addEffect(new MobEffectInstance(effects.VULNERABLE, effectDurationTicks, 0));
                target.setTicksFrozen(Math.max(target.getTicksFrozen(), ScientistConfig.nanoInjectorFreezeTicks()));

                player.displayClientMessage(
                        Component.translatable("message.sporeadds.nano_injector.injected").withStyle(ChatFormatting.GREEN),
                        true
                );
            } else {
                player.displayClientMessage(
                        Component.translatable("message.sporeadds.nano_injector.invalid_target").withStyle(ChatFormatting.RED),
                        true
                );
            }
            didShoot = true;
        } else {
            player.displayClientMessage(
                    Component.translatable("message.sporeadds.nano_injector.miss").withStyle(ChatFormatting.GRAY),
                    true
            );
            didShoot = true;
        }

        if (didShoot) {
            tag.putInt("nano_charges", Math.max(0, charges - 1));
            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "pci_inject")),
                    SoundSource.PLAYERS,
                    2.0f,
                    0.4f
            );
        }

        return InteractionResultHolder.success(stack);
    }

    /**
     * Takes the reload cost (scientist.nano_injector.reload_cost, default 1 packed ice) from the player's inventory.
     * Returns false, taking nothing, if they cannot pay it.
     */
    private boolean tryConsumeReloadCost(Player player) {
        List<? extends String> raw = ScientistConfig.nanoInjectorReloadCostEntries();
        List<ConfigItems.Entry> cost = ConfigItems.parseAll(raw);

        // A non-empty list that resolves to nothing (typo, missing mod) must not make reloading free.
        if (cost.isEmpty() && !raw.isEmpty()) {
            return false;
        }

        if (!ConfigItems.has(player, cost)) {
            return false;
        }

        ConfigItems.take(player, cost);
        return true;
    }

    private LivingEntity findEntityTarget(Level level, Player player, double range) {
        Vec3 from = player.getEyePosition(1.0F);
        Vec3 to = from.add(player.getLookAngle().scale(range));

        LivingEntity closest = null;
        double closestDistance = range + 1;

        for (Entity entity : level.getEntities(
                player,
                player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0D),
                e -> e instanceof LivingEntity && e != player && e.isPickable()
        )) {
            Vec3 hitVec = entity.getBoundingBox().clip(from, to).orElse(null);
            if (hitVec != null) {
                double distance = from.distanceToSqr(hitVec);
                if (distance < closestDistance * closestDistance) {
                    closest = (LivingEntity) entity;
                    closestDistance = Math.sqrt(distance);
                }
            }
        }
        return closest;
    }

    private Vec3 getHandStart(Player player, InteractionHand hand) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 right = look.cross(up);

        if (right.lengthSqr() < 1.0E-4) {
            right = new Vec3(1.0, 0.0, 0.0);
        } else {
            right = right.normalize();
        }

        double sideOffset = hand == InteractionHand.MAIN_HAND ? 0.28D : -0.28D;
        return eyePos.add(right.scale(sideOffset)).add(look.scale(0.45D)).add(0.0D, -0.18D, 0.0D);
    }

    private void spawnParticleBeam(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 diff = end.subtract(start);
        double length = diff.length();
        if (length <= 0.001D) return;

        Vec3 step = diff.normalize().scale(0.25D);
        int points = Math.max(1, (int) (length / 0.25D));

        DustParticleOptions cyanDust = new DustParticleOptions(new Vector3f(0.0F, 1.0F, 1.0F), 1.0F);

        for (int i = 0; i <= points; i++) {
            Vec3 pos = start.add(step.scale(i));
            level.sendParticles(
                    cyanDust,
                    pos.x, pos.y, pos.z,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
    }
}