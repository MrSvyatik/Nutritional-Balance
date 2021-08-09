package com.dannyandson.nutritionalbalance.events;

import com.dannyandson.nutritionalbalance.NutritionalBalance;
import com.dannyandson.nutritionalbalance.capabilities.CapabilityNutritionalBalancePlayer;
import com.dannyandson.nutritionalbalance.api.IPlayerNutrient;
import com.dannyandson.nutritionalbalance.network.ModNetworkHandler;
import com.dannyandson.nutritionalbalance.network.PlayerSync;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


/*
If saturation has decreased, decrease nutrients relative to that decrease.
Call on finish eating event and reset the saturation value after eating.
*/

public class EventPlayerTick {

    private int i = 0;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase==TickEvent.Phase.END) {
            PlayerEntity playerEntity = event.player;

            float playerSaturation = playerEntity.getFoodData().getSaturationLevel();
            int playerFoodLevel = playerEntity.getFoodData().getFoodLevel();
            float foodpoints = playerSaturation + playerFoodLevel;
            playerEntity.getCapability(CapabilityNutritionalBalancePlayer.HEALTHY_DIET_PLAYER_CAPABILITY).ifPresent(inutritionalbalancePlayer -> {
                inutritionalbalancePlayer.processSaturationChange(foodpoints);
            });

            if (i >= 200) {
                playerEntity.getCapability(CapabilityNutritionalBalancePlayer.HEALTHY_DIET_PLAYER_CAPABILITY).ifPresent(inutritionalbalancePlayer -> {
                    IPlayerNutrient.NutrientStatus cachedStatus = inutritionalbalancePlayer.getCachedStatus();
                    IPlayerNutrient.NutrientStatus currentStatus = inutritionalbalancePlayer.getStatus();
                    if (currentStatus== IPlayerNutrient.NutrientStatus.ENGORGED) {
                        //slowness
                        playerEntity.addEffect(new EffectInstance(Effect.byId(2),200, 0, true, true));
                        //mining fatigue
                        playerEntity.addEffect(new EffectInstance(Effect.byId(4),200, 0, true, true));
                    }
                    else if (currentStatus== IPlayerNutrient.NutrientStatus.MALNOURISHED)
                    {
                        //mining fatigue
                        playerEntity.addEffect(new EffectInstance(Effect.byId(4),200, 0, true, true));
                        //weakness
                        playerEntity.addEffect(new EffectInstance(Effect.byId(18),200, 0, true, true));
                    }
                    else if (currentStatus== IPlayerNutrient.NutrientStatus.ON_TARGET)
                    {
                        //speed
                        playerEntity.addEffect(new EffectInstance(Effect.byId(1),200, 0, true, true));
                        //haste
                        playerEntity.addEffect(new EffectInstance(Effect.byId(3),200, 0, true, true));
                        //strength
                        playerEntity.addEffect(new EffectInstance(Effect.byId(5),200, 0, true, true));
                    }

                    if (cachedStatus != currentStatus)
                    {
                        if (playerEntity.level.isClientSide()) {
                            /*
                            playerEntity.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(I18n.format("nutritionalbalance.nutrientstatus.msg." + currentStatus.name())), true);
                            Minecraft.getInstance().getToastGui().add(
                                    new SystemToast(
                                            SystemToast.Type.TUTORIAL_HINT,
                                            ITextComponent.getTextComponentOrEmpty(I18n.format("nutritionalbalance.nutrientstatus." + currentStatus.name())),
                                            ITextComponent.getTextComponentOrEmpty(I18n.format("nutritionalbalance.nutrientstatus.msg." + currentStatus.name()))
                                    )
                            );

                             */
                        }
                        else {

                            PlayerSync playerSync = new PlayerSync(new ResourceLocation(NutritionalBalance.MODID, "playersync"),inutritionalbalancePlayer);
                            ModNetworkHandler.sendToClient(playerSync, (ServerPlayerEntity) playerEntity);

                        }

                    }
                });

                i = 0;
            }
            i++;
        }
    }
}
