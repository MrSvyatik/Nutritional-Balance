package com.dannyandson.nutritionalbalance.capabilities;

import com.dannyandson.nutritionalbalance.api.INutritionalBalancePlayer;
import com.dannyandson.nutritionalbalance.api.IPlayerNutrient;
import com.dannyandson.nutritionalbalance.nutrients.Nutrient;
import com.dannyandson.nutritionalbalance.nutrients.WorldNutrients;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityNutritionalBalancePlayer {
    @CapabilityInject(INutritionalBalancePlayer.class)
    public static Capability<INutritionalBalancePlayer> HEALTHY_DIET_PLAYER_CAPABILITY = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(INutritionalBalancePlayer.class);
    }

    public static class NutritionalBalancePlayerStorage implements Capability.IStorage<INutritionalBalancePlayer>
    {
        /**
         * Serialize the capability instance to a NBTTag.
         * This allows for a central implementation of saving the data.
         * <p>
         * It is important to note that it is up to the API defining
         * the capability what requirements the 'instance' value must have.
         * <p>
         * Due to the possibility of manipulating internal data, some
         * implementations MAY require that the 'instance' be an instance
         * of the 'default' implementation.
         * <p>
         * Review the API docs for more info.
         *
         * @param capability The Capability being stored.
         * @param instance   An instance of that capabilities interface.
         * @param side       The side of the object the instance is associated with.
         * @return a NBT holding the data. Null if no data needs to be stored.
         */
        @Nullable
        @Override
        public INBT writeNBT(Capability<INutritionalBalancePlayer> capability, INutritionalBalancePlayer instance, Direction side) {
            CompoundTag nbtTags = new CompoundTag();
            for (IPlayerNutrient playerNutrient: instance.getPlayerNutrients())
            {
                nbtTags.putFloat(playerNutrient.getNutrient().name, playerNutrient.getValue());
            }
            return nbtTags;
        }

        /**
         * Read the capability instance from a NBT tag.
         * <p>
         * This allows for a central implementation of saving the data.
         * <p>
         * It is important to note that it is up to the API defining
         * the capability what requirements the 'instance' value must have.
         * <p>
         * Due to the possibility of manipulating internal data, some
         * implementations MAY require that the 'instance' be an instance
         * of the 'default' implementation.
         * <p>
         * Review the API docs for more info.         *
         *
         * @param capability The Capability being stored.
         * @param instance   An instance of that capabilities interface.
         * @param side       The side of the object the instance is associated with.
         * @param nbt        A NBT holding the data. Must not be null, as doesn't make sense to call this function with nothing to read...
         */
        @Override
        public void readNBT(Capability<INutritionalBalancePlayer> capability, INutritionalBalancePlayer instance, Direction side, INBT nbt)
        {
            //TODO (maybe): Read all player nutrient values, even if nutrient not defined in the world
            // to prevent loss of nutrient values if item tags are broken by something else.
            for (Nutrient nutrient: WorldNutrients.get())
            {
                instance.getPlayerNutrientByName(nutrient.name).setValue(((CompoundTag) nbt).getFloat(nutrient.name));
            }

        }
    }
}
