package com.yourname.mcaaddon;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkSetup {

    private static final String PROTOCOL = "1";

    // Create a special radio channel strictly for our Addon messages to the server
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MCAAddon.MOD_ID, "main_channel"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    // This method will boot up when the game opens
    public static void registerPackets() {
        int id = 0;

        // This fully instructs Forge's internal Post Office precisely on how to seal and mail the letter!
        CHANNEL.messageBuilder(ToggleReaperPacket.class, id++)
                .encoder(ToggleReaperPacket::toBytes)
                .decoder(ToggleReaperPacket::new)
                .consumerNetworkThread(ToggleReaperPacket::handle) // Direct route straight to handle!
                .add();
    }
}