package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.sporeadds.sporeaddsmod.combat.WeakPointKillTracker;
import com.sporeadds.sporeaddsmod.research.PrestigeManager;
import com.sporeadds.sporeaddsmod.research.TrackedEntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(value = Calamity.class, remap = false)
public abstract class CalamityWeakPointLootMixin {

    @Inject(method = "getDroppedItems", at = @At("RETURN"), cancellable = true, remap = false)
    private void sporeadd$doubleDroppedItems(int val, CallbackInfoReturnable<List<ItemStack>> cir) {
        Calamity self = (Calamity)(Object)this;

        if (!sporeadd$isEligibleForDoubleLoot(self)) return;

        List<ItemStack> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return;

        for (ItemStack stack : original) {
            stack.setCount(stack.getCount() * 2);
        }

        WeakPointKillTracker.clear(self.getId());
    }

    private static boolean sporeadd$isEligibleForDoubleLoot(Calamity target) {
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