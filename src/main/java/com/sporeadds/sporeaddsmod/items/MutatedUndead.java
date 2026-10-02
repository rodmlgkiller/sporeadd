package com.sporeadds.sporeaddsmod.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class MutatedUndead extends Item {

    public MutatedUndead(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack itemstack = context.getItemInHand();
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos spawnPos = blockpos.relative(direction);

        EntityType<?> biobloobType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("spore", "biobloob"));

        if (biobloobType != null) {
            Entity entity = biobloobType.create(serverLevel);

            if (entity instanceof Mob mob) {
                mob.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, context.getRotation(), 0.0F);
                mob.setPersistenceRequired();

                serverLevel.addFreshEntityWithPassengers(mob);

                // Invisibilidad muy breve para ocultar el daño inicial
                mob.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 0, false, false, false));

                float currentMaxHp = mob.getMaxHealth();
                float targetHp = 10.0f;

                if (currentMaxHp > targetHp) {
                    float damageNeeded = currentMaxHp - targetHp;
                    mob.hurt(serverLevel.damageSources().generic(), damageNeeded);
                    mob.setHealth(targetHp);
                }

                // Efecto permanente que tú querías
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, -1, 2, false, false, false));

                if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                    itemstack.shrink(1);
                }

                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }
}