package com.dannyandson.nutritionalbalance.events;

import com.dannyandson.nutritionalbalance.nutrients.Nutrient;
import com.dannyandson.nutritionalbalance.nutrients.WorldNutrients;
import com.dannyandson.nutritionalbalance.NutritionalBalance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.StringJoiner;

public class EventTooltip {
    @SubscribeEvent
    public void onItemToolTipEvent(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();
        Level world;
        if (event.getEntity()!=null)
            world = event.getEntity().level;
        else
        {
            //tool tip event being called by a non-entity such as JEI
            try {
                world = Minecraft.getInstance().level;
            }catch (Exception e)
            {
                //this shouldn't happen unless some mod calls the tool tip event on the server side for some reason.
                NutritionalBalance.LOGGER.error("Exception during attempt to access tooltip by non-entity." + e.getLocalizedMessage());
                return;
            }
        }

        if(item.getFoodProperties() != null || item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof CakeBlock) {
            // Create readable list of nutrients
            StringJoiner stringJoiner = new StringJoiner(", ");

            try {

                List<Nutrient> nutrients = WorldNutrients.getNutrients(item, world);
                for (Nutrient nutrient : nutrients) {
                    stringJoiner.add(nutrient.getLocalizedName());
                }

                if (stringJoiner.length() > 0) {

                    String NUvalue = "";
                    if (item.getFoodProperties() != null) {
                        NUvalue = " (" + ((float) Math.round((WorldNutrients.getEffectiveFoodQuality(item.getFoodProperties(), WorldNutrients.getNutrients(item,world).size())) * 10)) / 10 + "NU)";
                    }

                    event.getToolTip().add(Component.nullToEmpty(
                           "§7" +I18n.get("nutritionalbalance.nutrients") + ": §2" + stringJoiner.toString() + "§7" + NUvalue + "§r"
                    ));

                }

                if (event.getPlayer()!=null && event.getPlayer().getDisplayName().getString().equals("Dev") && Minecraft.getInstance().options.advancedItemTooltips)
                    if (Screen.hasShiftDown()) {
                        for (TagKey<Item> tagKey: item.getDefaultInstance().getTags().toList()) {
                            ResourceLocation tag = tagKey.location();
                            event.getToolTip().add(Component.nullToEmpty("#" + tag.toString()));
                        }

                        if (itemStack.getTag() != null) {
                            //event.getEntity().sendMessage(itemStack.getTag().toFormattedComponent(),event.getEntity().getUniqueID());
                            event.getToolTip().add(Component.nullToEmpty(itemStack.getTag().toString()));
                        }
                    } else {
                        event.getToolTip().add(Component.nullToEmpty("§8--Hold shift for tag info--§r"));
                    }

            }catch (Exception e)
            {
                //catch and log any exceptions thrown so JEI doesn't break if something goes wrong.
                NutritionalBalance.LOGGER.error("Exception thrown while adding nutrient info  for '" + item.getDefaultInstance().getDisplayName().getString() + "' to tooltips: " + e.getMessage());
            }
        }

    }
}
