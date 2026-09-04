package com.yourname.mcaaddon.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.Mth;

@Mixin(HumanoidModel.class)
public class ReaperScytheAnimationMixin<T extends LivingEntity> {

    // By injecting at the TAIL of setupAnim, we are guaranteeing we instantly overwrite the default "Punch"!
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void animateReaperScythe(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {

        // Ensure this only affects actual Players
        if (entity instanceof Player player) {

            // Cast to model to modify limbs
            @SuppressWarnings("unchecked")
            HumanoidModel<T> model = (HumanoidModel<T>) (Object) this;

            // !! CHANGE THIS TO MATCH YOUR ACTUAL SCYTHE ITEM CLASS/ID !!
            boolean holdingScythe = true; // player.getMainHandItem().getItem() == ModItems.REAPER_SCYTHE.get();

            // attackTime natively tracks swing from 0.0F (click) to 1.0F (finish)
            float swing = model.attackTime;

            // Only override if the scythe is held AND the player clicked
            if (swing > 0.0F && holdingScythe) {

                // PI variables help keep math organized
                // In radians: PI (~3.14) = 180 Degrees | PI / 2 (~1.57) = 90 Degrees
                float PI = (float) Math.PI;

                // ====== 3 PHASE SCYTHE SWING MATHEMATICS ======
                float bodyTwist;
                float armFrontBack; // xRot
                float armLeftRight; // zRot

                // --- PHASE 1: THE WIND UP (0.0 to 0.3 of swing time) ---
                if (swing < 0.3F) {
                    float p = swing / 0.3F; // Gets scale of just this phase (0 to 1)

                    // You asked to twist slightly right and put scythe behind back
                    bodyTwist = -(p * 0.4F); // Slight Rightwards tilt on torso

                    // Lifts arms high and back
                    armFrontBack = -(p * 2.5F); // Yank up
                    armLeftRight = +(p * 0.8F); // Out to the right side of the body

                    // --- PHASE 2: HORIZONTAL SWEEP TO LEFT (0.3 to 0.6) ---
                } else if (swing < 0.6F) {
                    float p = (swing - 0.3F) / 0.3F;
                    // Creates an accelerated swooshing curve
                    float swoopArc = Mth.sin(p * (PI / 2.0F));

                    // Turns torso sharply back left while swinging!
                    bodyTwist = -0.4F + (swoopArc * 1.5F);

                    // Drop arm from the sky horizontally slicing down and left
                    armFrontBack = -2.5F + (swoopArc * 1.6F);
                    armLeftRight = 0.8F - (swoopArc * 2.2F); // Swings heavily inward across chest

                    // --- PHASE 3: RECOVERY (0.6 to 1.0) ---
                } else {
                    float p = (swing - 0.6F) / 0.4F;

                    // Ease the player gradually back into their standard standing state
                    bodyTwist = 1.1F - (p * 1.1F);
                    armFrontBack = -0.9F + (p * 0.9F);
                    armLeftRight = -1.4F + (p * 1.4F);
                }

                // APPLY OUR MATH MAGIC OVER MODEL TO PREVENT WEIRD DISTORTIONS
                model.body.yRot = bodyTwist;
                model.rightArm.yRot = bodyTwist;
                model.head.yRot = bodyTwist * 0.5F; // Have the head glance where you slice
                model.rightArm.xRot = armFrontBack;
                model.rightArm.zRot = armLeftRight;

                // Stop the default swinging bob that ruins cool custom weapons
                model.rightArm.x = -5.0F;
            }
        }
    }
}