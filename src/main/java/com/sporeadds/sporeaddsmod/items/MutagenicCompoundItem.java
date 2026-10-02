package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.Powers.Levelstats;
import com.sporeadds.sporeaddsmod.util.SporeIdentifierUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MutagenicCompoundItem extends Item {
    private static final String TAG_VARIANT = "CompoundVariant";

    public MutagenicCompoundItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static MutagenicCompoundVariant getVariant(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_VARIANT)) {
            return MutagenicCompoundVariant.byId(tag.getString(TAG_VARIANT));
        }
        return MutagenicCompoundVariant.ORIGINAL;
    }

    public static void setVariant(ItemStack stack, MutagenicCompoundVariant variant) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_VARIANT, variant.getId());
        tag.putInt("CustomModelData", variant.getCustomModelData());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getVariant(stack).getDescriptionKey();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        CompoundTag tag = stack.getOrCreateTag();
        MutagenicCompoundVariant variant = getVariant(stack);
        int expected = variant.getCustomModelData();

        if (!tag.contains("CustomModelData") || tag.getInt("CustomModelData") != expected) {
            tag.putInt("CustomModelData", expected);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadd.mutagenic_compound.desc1")
                    .withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.translatable(
                    "tooltip.sporeadd.mutagenic_compound.desc2",
                    Component.translatable("tooltip.sporeadd.skill.rampant_evolution")
                            .withStyle(ChatFormatting.DARK_RED)
            ).withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.empty());

            tooltip.add(Component.translatable("tooltip.sporeadd.mutagenic_compound.creative_only")
                    .withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void playCreativeUseEffects(ServerPlayer sp) {
        ServerLevel serverLevel = sp.serverLevel();

        serverLevel.playSound(
                null,
                sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                1.0F,
                1.15F
        );

        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                sp.getX(),
                sp.getY() + 1.0D,
                sp.getZ(),
                1,
                0.0D, 0.0D, 0.0D,
                0.0D
        );

        serverLevel.sendParticles(
                ParticleTypes.POOF,
                sp.getX(),
                sp.getY() + 1.0D,
                sp.getZ(),
                24,
                0.45D, 0.65D, 0.45D,
                0.05D
        );

        serverLevel.sendParticles(
                ParticleTypes.CRIMSON_SPORE,
                sp.getX(),
                sp.getY() + 1.0D,
                sp.getZ(),
                18,
                0.35D, 0.60D, 0.35D,
                0.02D
        );
    }

    private static Component getVariantAppliedMessage(MutagenicCompoundVariant variant) {
        return switch (variant) {
            case ORIGINAL -> Component.translatable("message.sporeadd.mutagenic_compound.changed_to_original");
            case CAUSTIC -> Component.translatable("message.sporeadd.mutagenic_compound.changed_to_caustic");
            case ABYSSAL -> Component.translatable("message.sporeadd.mutagenic_compound.changed_to_abyssal");
            case GLUTTONOUS -> Component.translatable("message.sporeadd.mutagenic_compound.changed_to_gluttonous");
        };
    }

    private static Component getAlreadyVariantMessage(MutagenicCompoundVariant variant) {
        return switch (variant) {
            case ORIGINAL -> Component.translatable("message.sporeadd.mutagenic_compound.already_original");
            case CAUSTIC -> Component.translatable("message.sporeadd.mutagenic_compound.already_caustic");
            case ABYSSAL -> Component.translatable("message.sporeadd.mutagenic_compound.already_abyssal");
            case GLUTTONOUS -> Component.translatable("message.sporeadd.mutagenic_compound.already_gluttonous");
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!sp.isCreative()) {
            return InteractionResultHolder.pass(stack);
        }

        MutagenicCompoundVariant itemVariant = getVariant(stack);

        sp.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            if (!"kommandant".equalsIgnoreCase(data.getIdentifier())) {
                sp.displayClientMessage(
                        Component.translatable("message.sporeadd.mutagenic_compound.not_kommandant")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            if (itemVariant.getSubclassId().equalsIgnoreCase(data.getSubclass())) {
                sp.displayClientMessage(
                        getAlreadyVariantMessage(itemVariant).copy().withStyle(ChatFormatting.GRAY),
                        true
                );
                return;
            }

            SporeIdentifierUtil.setSubclassAndSync(sp, itemVariant.getSubclassId());
            Levelstats.forceReapplyStats(sp);

            playCreativeUseEffects(sp);

            sp.displayClientMessage(
                    getVariantAppliedMessage(itemVariant).copy().withStyle(ChatFormatting.DARK_RED),
                    true
            );
        });

        return InteractionResultHolder.success(stack);
    }
}