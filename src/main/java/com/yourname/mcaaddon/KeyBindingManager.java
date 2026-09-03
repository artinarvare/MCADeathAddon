package com.yourname.mcaaddon;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

// This @ annotation automatically registers this class to load onto the game bus correctly
@Mod.EventBusSubscriber(modid = MCAAddon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindingManager {

    // Creates the literal KeyMapping config. R defaults to "Reaper".
    public static final KeyMapping TOGGLE_REAPER = new KeyMapping(
            "key.mcaaddon.toggle_reaper",
            KeyConflictContext.IN_GAME, // Means it only works while playing
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R, // Sets the default key to the 'R' key!
            "MCA Addon Category" // Category name in settings
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_REAPER);
    }
}