package com.yourname.mcaaddon;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here MUST match the modId you put in mods.toml !
@Mod("mcaaddon")
public class MCAAddon {

    public static final String MOD_ID = "mcaaddon";

    public MCAAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        NetworkSetup.registerPackets();
    }
}