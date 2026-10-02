package com.sporeadds.sporeaddsmod.items;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.util.ClassTooltipUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BucketOfRemainsItem extends Item {
    public BucketOfRemainsItem(Properties properties) {
        super(properties);
    }

    // --- COLOR DEL NOMBRE DEL ÍTEM ---
    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.DARK_RED);
    }
    // ---------------------------------------------------

    // --- TOOLTIP MULTILÍNEA ---
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(ClassTooltipUtil.classRequirement(SporeAddsConfig.BUCKET_OF_REMAINS_REQUIRES_ORIGIN.get(), "ghost"));

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.sporeadds.remains.desc1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.sporeadds.remains.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.remains.duration").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.sporeadds.remains.usage").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    // ---------------------------------

    // --- NUEVO: COMPROBACIÓN DE CLASE GHOST ---

    private boolean hasGhostClass(ServerPlayer sp) {
        return sp.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).map(data -> {
            // COMPROBACIÓN ACTUALIZADA: solo "ghost"
            return "ghost".equals(data.getIdentifier());
        }).orElse(false);
    }

    // --- NUEVO: quita el aggro de infectados/utility entities que ya iban tras el jugador ---
    // La IA de estas entidades base (mod Spore) mantiene el target una vez adquirido aunque
    // el jugador cambie de equipo, así que hay que limpiarlo explícitamente al camuflarse.
    private static final double PURSUER_CLEAR_RADIUS = 48.0D;

    private static void clearPursuers(ServerPlayer player) {
        AABB bounds = player.getBoundingBox().inflate(PURSUER_CLEAR_RADIUS);
        List<LivingEntity> nearby = player.level().getEntitiesOfClass(
                LivingEntity.class,
                bounds,
                entity -> entity instanceof Infected || entity instanceof UtilityEntity
        );

        for (LivingEntity nearbyEntity : nearby) {
            if (nearbyEntity instanceof Mob mob && mob.getTarget() == player) {
                mob.setTarget(null);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {

            boolean canUse = true;
            // Cambiado para usar el nuevo método
            if (SporeAddsConfig.BUCKET_OF_REMAINS_REQUIRES_ORIGIN.get()) {
                canUse = hasGhostClass(sp);
            }

            if (canUse) {
                // USO NORMAL DEL ITEM (600 ticks = 30 segundos)
                sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        effects.CAMOUFLAGED.get(), 600, 0));

                clearPursuers(sp);

                if (!sp.isCreative()) {
                    ItemStack bucket = new ItemStack(Items.BUCKET);
                    if (!sp.addItem(bucket)) {
                        sp.drop(bucket, false);
                    }
                    stack.shrink(1);
                }

                level.playSound(
                        null,
                        sp.blockPosition(),
                        BuiltInRegistries.SOUND_EVENT.get(
                                ResourceLocation.fromNamespaceAndPath("spore", "limb_slash")),
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        2.0F,
                        2.0F
                );

                ((ServerPlayer) player).serverLevel().sendParticles(
                        new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F),
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        30,
                        0.5, 0.5, 0.5,
                        0.01
                );
            } else {
                sp.displayClientMessage(Component.translatable("message.sporeadds.remains.disgust").withStyle(ChatFormatting.GRAY), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}