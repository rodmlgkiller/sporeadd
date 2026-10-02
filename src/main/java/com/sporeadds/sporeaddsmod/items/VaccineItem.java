package com.sporeadds.sporeaddsmod.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class VaccineItem extends Item {
    public VaccineItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.VACCINE_REQUIRES_ORIGIN.get(), "medic"));
        tooltip.add(Component.translatable("tooltip.sporeadds.vaccine").withStyle(ChatFormatting.RED));
    }

    private static boolean hasMedicClass(ServerPlayer player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "medic".equals(data.getIdentifier()))
                .orElse(false);
    }

    private static void playInjectionSound(Level level, double x, double y, double z) {
        if (!level.isClientSide) {
            level.playSound(
                    null,
                    x, y, z,
                    ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("spore", "pci_inject")),
                    SoundSource.PLAYERS,
                    1.0f,
                    2.0f
            );
        }
    }

    private static String generateNewBloodCode() {
        return "VACCINE-" + java.util.UUID.randomUUID().toString().substring(0, 6);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer sp) || !(target instanceof ServerPlayer other)) return InteractionResult.PASS;

        boolean canUse = true;
        if (SporeAddsConfig.VACCINE_REQUIRES_ORIGIN.get()) {
            canUse = hasMedicClass(sp);
        }

        if (!canUse) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.vaccine.no_knowledge").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }

        if (!SporeAddsConfig.VACCINE_CURES_TERMINA.get()) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.vaccine.disabled").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }

        CompoundTag tag = stack.getOrCreateTag();
        String bloodCode = tag.contains("BloodCode", Tag.TAG_STRING) ? tag.getString("BloodCode") : "";
        String playerCode = other.getPersistentData().getString("SporeSyringeCode");

        boolean isUniversal = bloodCode.isEmpty();

        if (isUniversal || (!playerCode.isEmpty() && bloodCode.equals(playerCode))) {
            other.getPersistentData().putBoolean("VaccineBypassTermina", true);

            ResourceLocation termina = new ResourceLocation("sporeadd", "termina");
            MobEffect terminaEffect = ForgeRegistries.MOB_EFFECTS.getValue(termina);
            if (terminaEffect != null) {
                other.removeEffect(terminaEffect);
            }

            String newCode = generateNewBloodCode();
            other.getPersistentData().putString("SporeSyringeCode", newCode);

            Component msg = isUniversal
                    ? Component.translatable("message.sporeadds.vaccine.universal_success")
                    : Component.translatable("message.sporeadds.vaccine.success");
            sp.displayClientMessage(msg.copy().withStyle(ChatFormatting.GREEN), true);

            playInjectionSound(sp.level(), sp.getX(), sp.getY(), sp.getZ());

            if (!sp.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;

        } else {
            sp.displayClientMessage(Component.translatable("message.sporeadds.vaccine.dna_mismatch").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.pass(stack);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.pass(stack);

        boolean canUse = true;
        if (SporeAddsConfig.VACCINE_REQUIRES_ORIGIN.get()) {
            canUse = hasMedicClass(sp);
        }

        if (!canUse) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.vaccine.no_knowledge").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!SporeAddsConfig.VACCINE_CURES_TERMINA.get()) {
            sp.displayClientMessage(Component.translatable("message.sporeadds.vaccine.disabled").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        if (player.isCrouching()) {
            CompoundTag tag = stack.getOrCreateTag();
            String bloodCode = tag.contains("BloodCode", Tag.TAG_STRING) ? tag.getString("BloodCode") : "";
            String playerCode = sp.getPersistentData().getString("SporeSyringeCode");

            boolean isUniversal = bloodCode.isEmpty();

            if (isUniversal || (!playerCode.isEmpty() && bloodCode.equals(playerCode))) {
                sp.getPersistentData().putBoolean("VaccineBypassTermina", true);

                ResourceLocation termina = new ResourceLocation("sporeadd", "termina");
                MobEffect terminaEffect = ForgeRegistries.MOB_EFFECTS.getValue(termina);
                if (terminaEffect != null) {
                    sp.removeEffect(terminaEffect);
                }

                String newCode = generateNewBloodCode();
                sp.getPersistentData().putString("SporeSyringeCode", newCode);

                Component msg = isUniversal
                        ? Component.translatable("message.sporeadds.vaccine.self_universal_success")
                        : Component.translatable("message.sporeadds.vaccine.self_success");
                sp.displayClientMessage(msg.copy().withStyle(ChatFormatting.GREEN), true);

                playInjectionSound(level, player.getX(), player.getY(), player.getZ());

                if (!sp.isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResultHolder.success(stack);

            } else {
                sp.displayClientMessage(Component.translatable("message.sporeadds.vaccine.dna_mismatch").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.pass(stack);
    }
}