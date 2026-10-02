package com.sporeadds.sporeaddsmod.Powers;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class Poder2 {

    private static final ResourceLocation MYCELIUM_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("spore", "mycelium_ef");
    private static final ResourceLocation BLOOD_PARTICLE_ID = ResourceLocation.fromNamespaceAndPath("spore", "blood_particle");

    @SubscribeEvent
    public static void applyMyceliumOnHit(LivingHurtEvent event) {
        DamageSource source = event.getSource();

        if (source.getEntity() instanceof Player player) {
            if (!source.is(DamageTypes.MAGIC) && !source.is(DamageTypes.EXPLOSION)) {
                PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
                    if (data.getSwitch().length() > 2 && data.getSwitch().charAt(2) == '1') {
                        PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelCap -> {
                            int level = levelCap.getLevel();

                            if (level >= 0) {
                                int durationSeconds = Math.max(level * 5, 5);
                                int amplifier = level / 3;

                                LivingEntity target = event.getEntity();

                                if (target != null && target != player) {
                                    var effect = BuiltInRegistries.MOB_EFFECT.get(MYCELIUM_EFFECT_ID);
                                    if (effect != null) {
                                        target.addEffect(new MobEffectInstance(effect, durationSeconds * 20, amplifier, false, true));
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    public static boolean tryExecute(Player player, Entity rawTarget) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (!(player.level() instanceof ServerLevel serverLevel)) return false;
        if (!(rawTarget instanceof LivingEntity target)) return false;
        if (target.getHealth() <= 0) return false;

        AtomicBoolean enabled = new AtomicBoolean(false);
        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            if (data.getSwitch().length() > 2 && data.getSwitch().charAt(2) == '1') {
                enabled.set(true);
            }
        });

        if (!enabled.get()) return false;

        // No se puede arrancar una Mente peligrosa a otro Kommandant.
        if (target instanceof Player targetPlayer
                && com.sporeadds.sporeaddsmod.util.SporeClassUtil.hasClass(targetPlayer, "kommandant")) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.sporeadd.power2.kommandant_target")
                            .withStyle(ChatFormatting.DARK_RED)
            );
            return true;
        }

        float currentHealth = target.getHealth();
        if (currentHealth >= 5.0f) {
            int hpRounded = (int) Math.ceil(currentHealth);
            player.sendSystemMessage(
                    Component.translatable("message.sporeadd.power2.too_much_health", hpRounded)
                            .withStyle(ChatFormatting.DARK_RED)
            );
            return false;
        }

        target.removeAllEffects();

        int roll = player.getRandom().nextInt(100) + 1;
        boolean isEnderman = target.getType() == EntityType.ENDERMAN;
        boolean isWither = target.getType() == EntityType.WITHER_SKELETON;
        boolean endermanSuccess = isEnderman && roll <= 20;
        boolean endermanFail = isEnderman && !endermanSuccess;

        if (!endermanFail) {
            SoundEvent reaveSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "reaver_reave"));
            if (reaveSound != null) {
                serverLevel.playSound(
                        null,
                        target.getX(), target.getY(), target.getZ(),
                        reaveSound,
                        SoundSource.PLAYERS,
                        1.0F, 0.5F
                );
            }

            ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
            if (entityKey != null) {
                ResourceLocation deathSoundId = ResourceLocation.fromNamespaceAndPath(
                        entityKey.getNamespace(),
                        "entity." + entityKey.getPath() + ".death"
                );
                SoundEvent deathSound = BuiltInRegistries.SOUND_EVENT.get(deathSoundId);
                if (deathSound != null) {
                    serverLevel.playSound(
                            null,
                            target.getX(), target.getY(), target.getZ(),
                            deathSound,
                            SoundSource.HOSTILE,
                            1.0F, 0.7F
                    );
                }
            }
        } else {
            serverLevel.playSound(
                    null,
                    target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE,
                    1.0F, 1.0F
            );
        }

        double centerX = target.getX();
        double centerY = target.getY() + target.getBbHeight() / 2.0D;
        double centerZ = target.getZ();
        double scale = 1.5D;
        double xSpread = Math.max(0.2D, target.getBbWidth() * scale);
        double ySpread = Math.max(0.2D, target.getBbHeight() * scale * 0.5D);
        double zSpread = Math.max(0.2D, target.getBbWidth() * scale);

        if (endermanFail) {
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    centerX, centerY + 1.0D, centerZ,
                    50,
                    0.5D, 1.0D, 0.5D,
                    0.1D
            );
        } else {
            var bloodParticleType = BuiltInRegistries.PARTICLE_TYPE.get(BLOOD_PARTICLE_ID);
            int bloodCount = Math.max(15, (int) (target.getBbWidth() * target.getBbHeight() * 35.0D));
            if (bloodParticleType instanceof ParticleOptions bloodOptions) {
                serverLevel.sendParticles(
                        bloodOptions,
                        centerX, centerY, centerZ,
                        bloodCount,
                        xSpread, ySpread, zSpread,
                        0.08D
                );
            }

            int redstoneCount = Math.max(10, (int) (target.getBbWidth() * target.getBbHeight() * 25.0D));
            if (isWither) {
                serverLevel.sendParticles(
                        ParticleTypes.LARGE_SMOKE,
                        centerX, centerY, centerZ,
                        redstoneCount,
                        xSpread, ySpread, zSpread,
                        0.05D
                );
            } else {
                DustParticleOptions darkRedDust = new DustParticleOptions(new Vector3f(0.45F, 0.0F, 0.0F), 1.0F);
                serverLevel.sendParticles(
                        darkRedDust,
                        centerX, centerY, centerZ,
                        redstoneCount,
                        xSpread, ySpread, zSpread,
                        0.02D
                );
            }
        }

        if (target instanceof Player) {
            if (roll <= 95) {
                spawnItem(serverLevel, target, "sporeadd", "dangerous_mind");
                String[] successKeys = {
                        "message.sporeadd.power2.player.success_1",
                        "message.sporeadd.power2.player.success_2",
                        "message.sporeadd.power2.player.success_3"
                };
                player.sendSystemMessage(
                        Component.translatable(successKeys[player.getRandom().nextInt(successKeys.length)])
                                .withStyle(ChatFormatting.DARK_RED)
                );
            } else {
                player.sendSystemMessage(
                        Component.translatable("message.sporeadd.power2.player.fail")
                                .withStyle(ChatFormatting.DARK_RED)
                );
            }
        } else if ("spore:inf_player".equals(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType())))) {
            boolean isPlayerGenerated = target.getPersistentData().getBoolean("is_player_generated");

            if (isPlayerGenerated) {
                if (roll <= 65) {
                    spawnItem(serverLevel, target, "sporeadd", "dangerous_mind");
                    String[] successKeys = {
                            "message.sporeadd.power2.inf_player.gen.success_1",
                            "message.sporeadd.power2.inf_player.gen.success_2"
                    };
                    player.sendSystemMessage(
                            Component.translatable(successKeys[player.getRandom().nextInt(successKeys.length)])
                                    .withStyle(ChatFormatting.DARK_RED)
                    );
                } else {
                    String[] failKeys = {
                            "message.sporeadd.power2.inf_player.gen.fail_1",
                            "message.sporeadd.power2.inf_player.gen.fail_2"
                    };
                    player.sendSystemMessage(
                            Component.translatable(failKeys[player.getRandom().nextInt(failKeys.length)])
                                    .withStyle(ChatFormatting.DARK_RED)
                    );
                }
            } else {
                int requiredRoll = serverPlayer.getServer() != null && serverPlayer.getServer().isDedicatedServer() ? 1 : 10;

                if (roll <= requiredRoll) {
                    spawnItem(serverLevel, target, "sporeadd", "dangerous_mind");
                    String[] successKeys = {
                            "message.sporeadd.power2.inf_player.nongen.success_1",
                            "message.sporeadd.power2.inf_player.nongen.success_2"
                    };
                    player.sendSystemMessage(
                            Component.translatable(successKeys[player.getRandom().nextInt(successKeys.length)])
                                    .withStyle(ChatFormatting.DARK_RED)
                    );
                } else {
                    String[] failKeys = {
                            "message.sporeadd.power2.inf_player.nongen.fail_1",
                            "message.sporeadd.power2.inf_player.nongen.fail_2"
                    };
                    player.sendSystemMessage(
                            Component.translatable(failKeys[player.getRandom().nextInt(failKeys.length)])
                                    .withStyle(ChatFormatting.DARK_RED)
                    );
                }
            }
        } else if (target.getType() == EntityType.VILLAGER || target.getType() == EntityType.ZOMBIE_VILLAGER) {
            int chance = target.getType() == EntityType.VILLAGER ? 75 : 50;
            if (roll <= chance) {
                spawnItem(serverLevel, target, "sporeadd", "individualist_mind");
                player.sendSystemMessage(
                        Component.translatable("message.sporeadd.power2.villager.success")
                                .withStyle(ChatFormatting.DARK_RED)
                );
            } else {
                String[] failKeys = {
                        "message.sporeadd.power2.villager.fail_1",
                        "message.sporeadd.power2.villager.fail_2"
                };
                player.sendSystemMessage(
                        Component.translatable(failKeys[player.getRandom().nextInt(failKeys.length)])
                                .withStyle(ChatFormatting.DARK_RED)
                );
            }
        } else if (isWither) {
            if (roll <= 75) {
                spawnItem(serverLevel, target, "sporeadd", "intact_wither_marrow");
                player.sendSystemMessage(
                        Component.translatable("message.sporeadd.power2.wither.success")
                                .withStyle(ChatFormatting.DARK_RED)
                );
            } else {
                player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 200, 1));
                String[] failKeys = {
                        "message.sporeadd.power2.wither.fail_1",
                        "message.sporeadd.power2.wither.fail_2",
                        "message.sporeadd.power2.wither.fail_3"
                };
                player.sendSystemMessage(
                        Component.translatable(failKeys[player.getRandom().nextInt(failKeys.length)])
                                .withStyle(ChatFormatting.DARK_RED)
                );
            }
        } else if (target.getType() == EntityType.WITCH) {
            if (roll <= 85) {
                spawnItem(serverLevel, target, "sporeadd", "chemist_mind");
                player.sendSystemMessage(
                        Component.translatable("message.sporeadd.power2.witch.success")
                                .withStyle(ChatFormatting.DARK_RED)
                );
            } else {
                player.sendSystemMessage(
                        Component.translatable("message.sporeadd.power2.witch.fail")
                                .withStyle(ChatFormatting.DARK_RED)
                );
            }
        } else if (isEnderman) {
            if (endermanSuccess) {
                spawnItem(serverLevel, target, "sporeadd", "weakened_ender_eye");
                player.sendSystemMessage(
                        Component.translatable("message.sporeadd.power2.enderman.success")
                                .withStyle(ChatFormatting.DARK_RED)
                );
            } else {
                String[] failKeys = {
                        "message.sporeadd.power2.enderman.fail_1",
                        "message.sporeadd.power2.enderman.fail_2",
                        "message.sporeadd.power2.enderman.fail_3",
                        "message.sporeadd.power2.enderman.fail_4"
                };
                player.sendSystemMessage(
                        Component.translatable(failKeys[player.getRandom().nextInt(failKeys.length)])
                                .withStyle(ChatFormatting.DARK_RED)
                );
            }
        } else if (target.getType() == EntityType.ZOMBIE || target.getType() == EntityType.DROWNED || target.getType() == EntityType.HUSK) {
            PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
                if (spore.getSpore() >= 1) {
                    spore.setSpore(spore.getSpore() - 1);
                    PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelCap -> {
                        int levelphase = levelCap.getLevel();
                        int probability = 20 + (levelphase * 5);
                        int zRoll = player.getRandom().nextInt(100) + 1;

                        if (zRoll <= probability) {
                            spawnItem(serverLevel, target, "sporeadd", "mutated_undead");
                            player.sendSystemMessage(
                                    Component.translatable("message.sporeadd.power2.undead.success")
                                            .withStyle(ChatFormatting.DARK_RED)
                            );
                        } else {
                            String[] failKeys = {
                                    "message.sporeadd.power2.undead.fail_1",
                                    "message.sporeadd.power2.undead.fail_2",
                                    "message.sporeadd.power2.undead.fail_3",
                                    "message.sporeadd.power2.undead.fail_4",
                                    "message.sporeadd.power2.undead.fail_5",
                                    "message.sporeadd.power2.undead.fail_6",
                                    "message.sporeadd.power2.undead.fail_7",
                                    "message.sporeadd.power2.undead.fail_8",
                                    "message.sporeadd.power2.undead.fail_9",
                                    "message.sporeadd.power2.undead.fail_10"
                            };
                            player.sendSystemMessage(
                                    Component.translatable(failKeys[player.getRandom().nextInt(failKeys.length)])
                                            .withStyle(ChatFormatting.DARK_RED)
                            );
                        }
                    });
                }
            });
        }

        if (target instanceof Player pTarget) {
            pTarget.kill();
        } else {
            target.discard();
        }

        return true;
    }

    private static void spawnItem(ServerLevel level, LivingEntity target, String modid, String path) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(modid, path));
        if (item != null && item != net.minecraft.world.item.Items.AIR) {
            ItemEntity itemEntity = new ItemEntity(level, target.getX(), target.getY(), target.getZ(), new ItemStack(item));
            level.addFreshEntity(itemEntity);
        }
    }
}