package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.util.SporeFactionHelper;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class SporeFactionEvents {

    private static final String GOLEM_SPORE_GOAL_TAG = "sporeadd_iron_golem_spore_target";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !SporeAddsConfig.SPORE_FACTION_ENABLED.get()) {
            return;
        }

        if (!(event.getEntity() instanceof IronGolem golem)) {
            return;
        }

        if (golem.getPersistentData().getBoolean(GOLEM_SPORE_GOAL_TAG)) {
            return;
        }

        golem.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                golem,
                Player.class,
                10,
                true,
                false,
                living -> living instanceof Player player && SporeFactionHelper.isSporePlayer(player)
        ));

        golem.getPersistentData().putBoolean(GOLEM_SPORE_GOAL_TAG, true);
    }
}