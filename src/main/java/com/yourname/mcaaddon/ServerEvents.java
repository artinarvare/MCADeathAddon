package com.yourname.mcaaddon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MCAAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    public static final Set<UUID> REAPER_PLAYERS = new HashSet<>();

    // Wait until the Living cycle completes
    @SubscribeEvent
    public static void onLivingTickServer(LivingEvent.LivingTickEvent event) {

        if (event.getEntity().level().isClientSide) return;

        // Exactly the same logic as the Client fix!
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (REAPER_PLAYERS.contains(serverPlayer.getUUID())) {
                serverPlayer.getAbilities().mayfly = true;

                if (serverPlayer.getAbilities().flying) {
                    serverPlayer.noPhysics = true;
                } else {
                    serverPlayer.noPhysics = false;
                }
            }
        }
    }

    // A brand new mechanic preventing Suffocation & Gravity from destroying you inside mountains!
    @SubscribeEvent
    public static void onPlayerDamage(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (REAPER_PLAYERS.contains(serverPlayer.getUUID())) {

                // When we take "Inside a solid block" wall damage, CANCEL IT immediately!
                if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) {
                    event.setCanceled(true);
                }

                // Stop annoying fall-damage splats when turning flight on and off mid-air
                if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    // Keep Server Collisions perfectly identical!
    @SubscribeEvent
    public static void onPlayerSizeServer(net.minecraftforge.event.entity.EntityEvent.Size event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {

            if (REAPER_PLAYERS.contains(serverPlayer.getUUID())) {
                event.setNewSize(net.minecraft.world.entity.EntityDimensions.fixed(0.8f, 2.8f));
                event.setNewEyeHeight(2.5f);
            }
        }
    }
}