package com.yourname.mcaaddon;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MCAAddon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    // Our Master state! True = You are Reaper, False = You are normal player
    public static boolean isReaperFormActive = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindingManager.TOGGLE_REAPER.consumeClick() && Minecraft.getInstance().player != null) {

            isReaperFormActive = !isReaperFormActive;

            Minecraft.getInstance().options.bobView().set(!isReaperFormActive);

            NetworkSetup.CHANNEL.sendToServer(new ToggleReaperPacket(isReaperFormActive));
            Minecraft.getInstance().player.refreshDimensions();

            var player = Minecraft.getInstance().player;
            var level = player.level();

            // --- THE TRANSFORMATION BURST EFFECTS ---
            if (isReaperFormActive) {
                // Creates a terrifying bass sound globally matching the Boss scale!
                level.playSound(player, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        0.5f, 0.8f);

                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("The Soul harvest begins...").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE), true
                );
            } else {
                // Sucking wind ghostly whisper as you return to normal human form
                level.playSound(player, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.PHANTOM_SWOOP,
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        0.5f, 0.5f);

                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("You take on a mortal guise..."), true
                );
            }

            // Pop out 40 explosion smoke blasts in an instant radius to cover the model morph!
            for (int i = 0; i < 40; i++) {
                // Mathematical trick scattering speeds 360-degrees outward
                double spreadX = (Math.random() - 0.5) * 0.5;
                double spreadY = Math.random() * 0.3;
                double spreadZ = (Math.random() - 0.5) * 0.5;

                level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                        player.getX(), player.getY() + 1.0, player.getZ(), // Burst straight from the center
                        spreadX, spreadY, spreadZ
                );
            }
        }
    }

    // LivingTick executes explicitly AFTER vanilla forces the standard player reset!
    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {

        if (event.getEntity() != Minecraft.getInstance().player) {
            return;
        }

        var player = Minecraft.getInstance().player;

        // ... gravity abilities above it inside the LivingTick ...

        if (isReaperFormActive) {
            player.getAbilities().mayfly = true;
            if (player.getAbilities().flying) {
                player.noPhysics = true;
            } else {
                player.noPhysics = false;
            }

            // --- AMBIENT VISUAL EFFECTS (Upgraded Code!) ---
// Always ensure this visual effect ONLY triggers while transformed
            if (isReaperFormActive) {
                // A loop of 3 guaranteed particles per tick for a truly dense, shadowy aura
                for (int i = 0; i < 2; i++) {

                    // Keeps the effects localized around your taller Reaper Hitbox body
                    double pX = player.getX() + (Math.random() - 0.5) * 1.5;
                    double pY = player.getY() + (Math.random() * 2.8);
                    double pZ = player.getZ() + (Math.random() - 0.5) * 1.5;

                    // Creates a 50/50 randomized mix of Spooky Blue Soul Magic & Black Ash Smoke
                    if (Math.random() > 0.5) {
                        player.level().addParticle(net.minecraft.core.particles.ParticleTypes.SOUL,
                                pX, pY, pZ,
                                0, 0.05, 0); // Light speed pushing it slowly upward
                    } else {
                        player.level().addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                                pX, pY, pZ,
                                0, 0.02, 0);
                    }
                }
            }
        }
    }

    // Hijacks standard Hitbox & Eye Measurements!
    @SubscribeEvent
    public static void onPlayerSize(net.minecraftforge.event.entity.EntityEvent.Size event) {

        // Ensure this applies to us
        if (event.getEntity() == Minecraft.getInstance().player) {

            if (isReaperFormActive) {
                // Creates a massive Hit-Box roughly matching the Reaper (2.8 Blocks Tall)
                event.setNewSize(net.minecraft.world.entity.EntityDimensions.fixed(0.8f, 2.8f));

                // Places our 1st-Person Camera exactly 2.5 Blocks into the air as requested!
                event.setNewEyeHeight(2.5f);
            }
            // (When not morphed, we simply do nothing, letting normal human proportions take back over.)
        }
    }
}