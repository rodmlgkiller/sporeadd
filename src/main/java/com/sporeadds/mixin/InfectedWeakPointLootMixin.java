package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.sporeadds.sporeaddsmod.combat.WeakPointKillTracker;
import com.sporeadds.sporeaddsmod.research.PrestigeManager;
import com.sporeadds.sporeaddsmod.research.TrackedEntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mixin(Infected.class)
public abstract class InfectedWeakPointLootMixin {

    @Shadow
    public abstract List<? extends String> getDropList();

    @Inject(method = "m_7472_", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadd$injectDoubleLoot(DamageSource source, int val, boolean bool, CallbackInfo ci) {
        Infected self = (Infected)(Object)this;
        List<? extends String> original = self.getDropList();

        if (original == null || original.isEmpty()) return;
        if (!sporeadd$isEligibleForDoubleLoot(self)) return;

        List<String> doubled = new ArrayList<>(original.size());
        for (String entry : original) {
            doubled.add(sporeadd$doubleQuantity(entry));
        }

        for (String str : doubled) {
            String[] parts = str.split("\\|");
            ItemStack itemStack = new ItemStack((ItemLike) Objects.requireNonNull(
                    BuiltInRegistries.ITEM.get(ResourceLocation.parse(parts[0]))));

            int min = Integer.parseUnsignedInt(parts[2]);
            int max = Integer.parseUnsignedInt(parts[3]);
            int amount = min == max ? (val > 0 ? self.getRandom().nextIntBetweenInclusive(min, min + val) : min)
                    : self.getRandom().nextIntBetweenInclusive(min, max + Math.max(val, (int)(min * 0.15F * val)));

            int chance = Integer.parseUnsignedInt(parts[1]) + val * 10;
            if (Math.random() < (chance / 100.0)) {
                itemStack.setCount(amount);
                ItemEntity item = new ItemEntity(self.level(), self.getX(), self.getY(), self.getZ(), itemStack);
                item.setDefaultPickUpDelay();
                self.level().addFreshEntity(item);
            }
        }

        WeakPointKillTracker.clear(self.getId());
        ci.cancel();
    }

    private static String sporeadd$doubleQuantity(String entry) {
        String[] parts = entry.split("\\|");
        if (parts.length != 4) {
            return entry;
        }
        try {
            int min = Integer.parseUnsignedInt(parts[2]) * 2;
            int max = Integer.parseUnsignedInt(parts[3]) * 2;
            return parts[0] + "|" + parts[1] + "|" + min + "|" + max;
        } catch (NumberFormatException e) {
            return entry;
        }
    }

    private static boolean sporeadd$isEligibleForDoubleLoot(Infected target) {
        if (!(target.level() instanceof ServerLevel serverLevel)) return false;

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
        if (!TrackedEntities.ENTITY_IDS.contains(entityId)) return false;

        UUID attackerId = WeakPointKillTracker.getRealKillAttacker(target.getId());
        if (attackerId == null) return false;

        boolean killedByWeakPoint = WeakPointKillTracker.wasKilledByWeakPoint(
                target.getId(), attackerId, serverLevel.getGameTime());
        if (!killedByWeakPoint) return false;

        return PrestigeManager.isPrestigedByAnyScientist(serverLevel.getServer(), entityId);
    }
}