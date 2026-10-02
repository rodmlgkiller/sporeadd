package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerFrostbiteMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void sporeadds$tickFrostbiteDamage(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (player.level().isClientSide()) return;
        if (player.getTeam() == null) return;
        if (!"spore".equalsIgnoreCase(player.getTeam().getName())) return;

        MobEffect frostbite = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "frostbite"));
        if (frostbite == null) return;

        MobEffectInstance effect = player.getEffect(frostbite);
        if (effect == null) return;

        int amplifier = effect.getAmplifier();

        // Frostbite V visual = amplifier 4 interno
        if (amplifier < 4) return;

        // Cada 80 ticks = 4 segundos
        if (player.tickCount % 80 != 0) return;

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            float damage = (amplifier + 1) * 0.3F;

            if (data.getArmorHp() > 0) {
                damage *= 2.5F;
            }

            player.hurt(player.damageSources().freeze(), damage);
            player.setTicksFrozen(player.getTicksFrozen() + 100);
        });
    }
}