import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class SporePowerHandler {

    private static final ResourceLocation MYCELIUM_EFFECT_ID = new ResourceLocation("spore", "mycelium_ef");

    @SubscribeEvent
    public void onPlayerMeleeDamage(LivingHurtEvent event) {
        DamageSource source = event.getSource();

        if (source.getEntity() instanceof Player player) {
            // Solo daño cuerpo a cuerpo: no proyectil, no mágico, no explosión
            if (!source.is(DamageTypes.ARROW) && !source.is(DamageTypes.MAGIC) && !source.is(DamageTypes.EXPLOSION)) {

                player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
                    int level = levelCap.getLevel();

                    if (level > 3) {
                        int durationSeconds = (level - 3) * 5;
                        int amplifier = level / 3;

                        LivingEntity target = event.getEntity();

                        if (target != null && target != player) {
                            var myceliumEffect = ForgeRegistries.MOB_EFFECTS.getValue(MYCELIUM_EFFECT_ID);

                            if (myceliumEffect != null) {
                                target.addEffect(new MobEffectInstance(myceliumEffect, durationSeconds * 20, amplifier, false, true));
                            }
                        }
                    }
                });
            }
        }
    }
}
