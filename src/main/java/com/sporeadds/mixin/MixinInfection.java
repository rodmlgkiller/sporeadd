package com.sporeadds.mixin;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.Harbinger.Spore.sEvents.Infection", remap = false)
public class MixinInfection {

    @Inject(
            method = "callProto(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("TAIL"),
            remap = false
    )
    private static void onCallProto(Entity entity, CallbackInfo ci) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        MinecraftServer server = serverLevel.getServer();
        if (server == null) return;

        boolean protoExists = false;

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
                if (key != null && key.toString().equals("spore:proto")) {
                    protoExists = true;
                    break;
                }
            }

            if (protoExists) {
                break;
            }
        }

        if (!protoExists) {
            return;
        }

        // Nombre traducido de la entidad (p.ej. "Zombi", "Proto") en lugar de su id.
        Component entidad = entity.getType().getDescription();

        BlockPos pos = entity.blockPosition();

        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                if (player.getTeam() != null && "spore".equalsIgnoreCase(player.getTeam().getName())) {
                    player.sendSystemMessage(
                            Component.translatable(
                                    "message.sporeadd.proto.entity_killed",
                                    entidad,
                                    pos.getX(),
                                    pos.getY(),
                                    pos.getZ()
                            ).withStyle(ChatFormatting.DARK_RED)
                    );
                }
            }
        }
    }
}