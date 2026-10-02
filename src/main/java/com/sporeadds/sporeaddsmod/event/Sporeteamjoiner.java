package com.sporeadds.sporeaddsmod.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd")
public class Sporeteamjoiner {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        ResourceLocation entityId = EntityType.getKey(entity.getType());
        if (entityId == null) return;

        String namespace = entityId.getNamespace();

        if (!"spore".equals(namespace)) return;

        if (entity.level() == null) return;

        Scoreboard scoreboard = entity.level().getScoreboard();
        PlayerTeam sporeTeam = scoreboard.getPlayerTeam("spore");

        if (sporeTeam == null) {
            sporeTeam = scoreboard.addPlayerTeam("spore");
        }

        scoreboard.addPlayerToTeam(entity.getScoreboardName(), sporeTeam);
    }
}
