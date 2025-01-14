package com.dannyandson.nutritionalbalance.network;

import com.dannyandson.nutritionalbalance.NutritionalBalance;
import com.dannyandson.nutritionalbalance.gui.PacketOpenGui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworkHandler {
    private static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "2.2";
    public static PacketOpenGui packetOpenGui;

    private static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        packetOpenGui = new PacketOpenGui();
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(NutritionalBalance.MODID, "playersync"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals);

        INSTANCE.messageBuilder(PlayerSync.class,nextID())
                .encoder(PlayerSync::toBytes)
                .decoder(PlayerSync::new)
                .consumer(PlayerSync::handle)
                .add();
        INSTANCE.messageBuilder(GUITrigger.class,nextID())
                .encoder(GUITrigger::toBytes)
                .decoder(GUITrigger::new)
                .consumer(GUITrigger::handle)
                .add();
        INSTANCE.registerMessage(
                nextID(),
                PacketOpenGui.class,
                (((packetOpenGui, packetBuffer) -> {})),
                (packetBuffer->packetOpenGui),
                PacketOpenGui::handle
        );

    }

    public static void sendToClient(Object packet, ServerPlayer player) {
        INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

}
