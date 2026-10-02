package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.client.actionwheel.ActionWheelOption;
import com.sporeadds.sporeaddsmod.client.actionwheel.ActionWheelProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SetInjectorModePacket;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InjectorItem extends Item implements ActionWheelProvider {

    private static final Set<ResourceLocation> NEGATIVE_EFFECTS = Set.of(
            BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.POISON.value()),
            BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.BLINDNESS.value()),
            BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.WEAKNESS.value()),
            BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.WITHER.value()),
            BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.UNLUCK.value()),
            BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.BAD_OMEN.value()),
            BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.DARKNESS.value()),
            BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.LEVITATION.value()),
            ResourceLocation.fromNamespaceAndPath("spore", "mycelium_ef"),
            ResourceLocation.fromNamespaceAndPath("spore", "corrosion"),
            ResourceLocation.fromNamespaceAndPath("spore", "madness"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "exposed"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "dissolution")
    );

    private static final ResourceLocation MANGLED_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "mangled");

    private static final EffectOption[] EFFECTS = new EffectOption[] {
            new EffectOption(MobEffects.HEAL, 1, 20, "effect.sporeadds.injector.instant_health"),
            new EffectOption(MobEffects.DIG_SPEED, 2, 600, "effect.sporeadds.injector.haste_ii"),
            new EffectOption(MobEffects.MOVEMENT_SPEED, 2, 400, "effect.sporeadds.injector.speed_ii"),
            new EffectOption(MobEffects.DAMAGE_RESISTANCE, 2, 400, "effect.sporeadds.injector.resistance_ii"),
            new EffectOption(MobEffects.REGENERATION, 2, 1000, "effect.sporeadds.injector.regeneration_ii"),
            new EffectOption(MobEffects.DAMAGE_BOOST, 2, 400, "effect.sporeadds.injector.strength_ii"),
            new EffectOption(MobEffects.INVISIBILITY, 1, 600, "effect.sporeadds.injector.invisibility"),
            new EffectOption(MobEffects.WATER_BREATHING, 1, 3000, "effect.sporeadds.injector.water_breathing"),
            new RemoveNegativeEffectOption("effect.sporeadds.injector.remove_negative")
    };

    private static final String NBT_EFFECT_INDEX = "SelectedEffect";
    private static final String NBT_LAST_ATTACK_TICK = "InjectorLastAttackTick";

    public InjectorItem(Properties properties) {
        super(properties.durability(100).setNoRepair());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.world.item.enchantment.Enchantment ench) {
        return false;
    }

    private static boolean hasMedicClass(ServerPlayer sp) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(sp)
                .map(data -> "medic".equals(data.getIdentifier()))
                .orElse(false);
    }


    public static void setSelectedEffectIndex(ItemStack stack, int index) {
        if (index < 0 || index >= EFFECTS.length) {
            return;
        }
        ItemNbt.getOrCreateTag(stack).putInt(NBT_EFFECT_INDEX, index);
    }

    private static void applyInjectorEffect(ItemStack stack, LivingEntity target, ServerPlayer sp) {
        boolean canUse = !SporeAddsConfig.INJECTOR_REQUIRES_ORIGIN.get() || hasMedicClass(sp);

        if (!canUse) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.injector.unknown_usage").withStyle(ChatFormatting.GRAY), true);
            return;
        }

        int damage = stack.getDamageValue();
        if (damage >= stack.getMaxDamage()) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.injector.empty").withStyle(ChatFormatting.RED), true);
            return;
        }

        int index = ItemNbt.getOrCreateTag(stack).getInt(NBT_EFFECT_INDEX);
        if (index < 0 || index >= EFFECTS.length) {
            index = 0;
        }

        EffectOption eff = EFFECTS[index];

        if (eff instanceof RemoveNegativeEffectOption) {
            for (MobEffectInstance inst : new ArrayList<>(target.getActiveEffects())) {
                ResourceLocation effId = BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect().value());
                boolean isNegative = effId != null && (NEGATIVE_EFFECTS.contains(effId) || inst.getEffect().getCategory() == MobEffectCategory.HARMFUL);

                if (!isNegative) {
                    continue;
                }

                if (effId.equals(MANGLED_EFFECT_ID)) {
                    int amplifier = inst.getAmplifier();

                    if (amplifier > 0) {
                        int duration = inst.getDuration();
                        target.removeEffect(inst.getEffect());
                        target.addEffect(new MobEffectInstance(inst.getEffect(), duration, amplifier - 1));
                    } else {
                        target.removeEffect(inst.getEffect());
                    }
                } else {
                    target.removeEffect(inst.getEffect());
                }
            }
            sp.displayClientMessage(Component.translatable("message.sporeadds.injector.removed_negative").withStyle(ChatFormatting.GREEN), true);
        } else {
            target.addEffect(new MobEffectInstance(eff.effect, eff.duration, eff.amplifier));
        }

        stack.setDamageValue(Math.min(damage + 5, stack.getMaxDamage()));

        ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath("spore", "pci_inject");
        if (BuiltInRegistries.SOUND_EVENT.containsKey(soundId)) {
            target.level().playSound(
                    null,
                    target.getX(), target.getY(), target.getZ(),
                    BuiltInRegistries.SOUND_EVENT.get(soundId),
                    SoundSource.PLAYERS,
                    1.0f,
                    2.0f
            );
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.INJECTOR_REQUIRES_ORIGIN.get(), "medic"));

        int index = ItemNbt.getOrCreateTag(stack).getInt(NBT_EFFECT_INDEX);
        if (index < 0 || index >= EFFECTS.length) {
            index = 0;
        }

        EffectOption eff = EFFECTS[index];

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.injector.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.injector.desc2").withStyle(ChatFormatting.GRAY));

            tooltip.add(
                    Component.translatable(
                            "tooltip.sporeadds.injector.hold_to_switch",
                            com.sporeadds.sporeaddsmod.client.SporeKeyMapping.OPEN_ACTION_WHEEL.getTranslatedKeyMessage()
                    ).withStyle(ChatFormatting.GRAY)
            );
            tooltip.add(Component.literal(""));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(
                Component.translatable("tooltip.sporeadds.injector.selected", Component.translatable(eff.translationKey()))
                        .withStyle(ChatFormatting.AQUA)
        );

        int charge = stack.getMaxDamage() - stack.getDamageValue();
        if (charge > 0) {
            tooltip.add(Component.translatable("tooltip.sporeadds.injector.charge", charge, stack.getMaxDamage()).withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.injector.charge_depleted", 0, stack.getMaxDamage()).withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public List<ActionWheelOption> getActionWheelOptions(Player player, ItemStack stack) {
        List<ActionWheelOption> options = new ArrayList<>();

        for (int i = 0; i < EFFECTS.length; i++) {
            EffectOption eff = EFFECTS[i];
            int index = i;

            ActionWheelOption.IconRenderer icon;
            if (eff instanceof RemoveNegativeEffectOption) {
                icon = (graphics, x, y, size) ->
                        graphics.renderFakeItem(new ItemStack(Items.BARRIER), x, y);
            } else {
                icon = (graphics, x, y, size) -> {
                    var sprite = Minecraft.getInstance().getMobEffectTextures().get(eff.effect);
                    graphics.blit(x, y, 0, size, size, sprite);
                };
            }

            options.add(new ActionWheelOption(
                    Component.translatable(eff.translationKey()),
                    icon,
                    () -> selectEffectClientSide(stack, index)
            ));
        }

        return options;
    }

    private static void selectEffectClientSide(ItemStack stack, int index) {
        setSelectedEffectIndex(stack, index);
        NetworkHandle.INSTANCE.sendToServer(new SetInjectorModePacket(index));
    }

    private static class EffectOption {
        final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect;
        final int amplifier;
        final int duration;
        final String translationKey;

        EffectOption(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> eff, int amp, int dur, String translationKey) {
            this.effect = eff;
            this.amplifier = amp - 1;
            this.duration = dur;
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }

    private static class RemoveNegativeEffectOption extends EffectOption {
        RemoveNegativeEffectOption(String translationKey) {
            super(MobEffects.HEAL, 0, 0, translationKey);
        }
    }

    @EventBusSubscriber
    public static class InjectorHandlers {

        @SubscribeEvent
        public static void onLivingAttack(LivingIncomingDamageEvent event) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer sp)) {
                return;
            }

            LivingEntity target = event.getEntity();

            ItemStack stack = sp.getMainHandItem();
            if (!(stack.getItem() instanceof InjectorItem)) {
                return;
            }

            long gameTime = sp.level().getGameTime();
            long lastAttackTick = ItemNbt.getOrCreateTag(stack).getLong(NBT_LAST_ATTACK_TICK);

            if (lastAttackTick == gameTime) {
                return;
            }

            ItemNbt.getOrCreateTag(stack).putLong(NBT_LAST_ATTACK_TICK, gameTime);
            applyInjectorEffect(stack, target, sp);
        }

        @SubscribeEvent
        public static void onXpPickup(PlayerXpEvent.PickupXp event) {
            Player player = event.getEntity();

            ItemStack offhand = player.getOffhandItem();
            if (offhand.getItem() instanceof InjectorItem && offhand.getDamageValue() > 0) {
                offhand.setDamageValue(offhand.getDamageValue() - 1);
                return;
            }

            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof InjectorItem && stack.getDamageValue() > 0) {
                    stack.setDamageValue(stack.getDamageValue() - 1);
                    return;
                }
            }
        }
    }
}