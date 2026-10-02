package com.sporeadds.sporeaddsmod.items;

import net.minecraft.resources.ResourceLocation;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.research.TrackedEntities;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public class ScalpelItem extends Item {

    private static final ResourceLocation DAMAGE_MODIFIER_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "scalpelitem_damage_modifier_uuid");
    private static final ResourceLocation SPEED_MODIFIER_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "scalpelitem_speed_modifier_uuid");

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = 3.0F;

    private final Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> defaultModifiers;

    public ScalpelItem(Properties properties) {

        super(properties.stacksTo(1).durability(200));

        ImmutableMultimap.Builder<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                DAMAGE_MODIFIER_UUID, ATTACK_DAMAGE - 1.0D, AttributeModifier.Operation.ADD_VALUE
        ));

        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                SPEED_MODIFIER_UUID, ATTACK_SPEED - 4.0D, AttributeModifier.Operation.ADD_VALUE
        ));

        this.defaultModifiers = builder.build();
    }

    @Override
    public Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, net.minecraft.world.level.block.state.BlockState state) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }

    private static boolean isScientist(Player player) {
        if (!SporeAddsConfig.SCALPEL_REQUIRES_ORIGIN.get()) {
            return true;
        }

        return player instanceof ServerPlayer sp
                && SporeIdentifierProvider.SPORE_IDENTIFIER.get(sp)
                .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isInfectedEntity(LivingEntity entity) {
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        return TrackedEntities.ENTITY_IDS.contains(entityId);
    }

    private static void spawnData(Level level, LivingEntity target, int amount, String sourceEntityId) {
        if (level.isClientSide) {
            return;
        }

        ItemStack dataStack = new ItemStack(com.sporeadds.sporeaddsmod.ModItems.DATA.get(), amount);

        com.sporeadds.sporeaddsmod.entity.DataDropItemEntity dataEntity =
                com.sporeadds.sporeaddsmod.entity.DataDropItemEntity.create(
                        level,
                        target.getX(),
                        target.getY() + 0.5D,
                        target.getZ(),
                        dataStack,
                        sourceEntityId
                );

        dataEntity.setDeltaMovement(
                (level.getRandom().nextDouble() - 0.5D) * 0.1D,
                0.2D,
                (level.getRandom().nextDouble() - 0.5D) * 0.1D
        );

        level.addFreshEntity(dataEntity);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, (entity) ->
                entity.broadcastBreakEvent(net.minecraft.world.entity.EquipmentSlot.MAINHAND));

        if (!(attacker instanceof Player player) || !isScientist(player) || target.level().isClientSide) {
            return true;
        }

        if (isInfectedEntity(target)) {
            String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
            spawnData(target.level(), target, 1, entityId);
            playDataSound(target.level(), target);
            return true;
        }

        if (target instanceof ServerPlayer targetPlayer) {
            boolean targetIsKommandant = SporeIdentifierProvider.SPORE_IDENTIFIER.get(targetPlayer)
                    .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                    .orElse(false);

            if (targetIsKommandant) {
                spawnData(target.level(), target, 1, TrackedEntities.KOMMANDANT_PLAYER_ID);
                playDataSound(target.level(), target);
            }
        }

        return true;
    }

    private static void playDataSound(Level level, LivingEntity target) {
        net.minecraft.sounds.SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("spore", "reaver_reave")
        );

        if (sound == null) {
            return;
        }

        level.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                sound,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0F,
                1.2F
        );
    }

    public static void onScientistKill(ServerPlayer killer, LivingEntity victim, ItemStack weapon) {
        if (!(weapon.getItem() instanceof ScalpelItem)) {
            return;
        }

        if (!isScientist(killer) || !isInfectedEntity(victim)) {
            return;
        }

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        spawnData(victim.level(), victim, 10, entityId);
    }

    public static void onScientistKillPlayer(ServerPlayer killer, ServerPlayer victim, ItemStack weapon) {
        if (!(weapon.getItem() instanceof ScalpelItem)) {
            return;
        }

        if (!isScientist(killer)) {
            return;
        }

        boolean victimIsKommandant = SporeIdentifierProvider.SPORE_IDENTIFIER.get(victim)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);

        if (!victimIsKommandant) {
            return;
        }

        spawnData(victim.level(), victim, 10, TrackedEntities.KOMMANDANT_PLAYER_ID);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.level.Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.SCALPEL_REQUIRES_ORIGIN.get(), "scientist"));

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(
                    Component.translatable("tooltip.sporeadds.scalpel.desc1",
                            Component.translatable("item.sporeadd.data").withStyle(ChatFormatting.YELLOW)
                    ).withStyle(ChatFormatting.GRAY)
            );
            tooltip.add(Component.translatable("tooltip.sporeadds.scalpel.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.scalpel.desc3").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}