package com.yourname.mcaaddon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.Map;

@Mod.EventBusSubscriber(modid = MCAAddon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientRenderEvents {

    private static LivingEntity dummyReaper = null;
    private static ModelPart physicalScytheBone = null;
    private static boolean reparentedToRightArm = false;

    private static final String SCYTHE_KEY = "scythe_handle";

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {

        if (ClientEvents.isReaperFormActive) {

            event.setCanceled(true);

            AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
            Minecraft mc = Minecraft.getInstance();

            if (dummyReaper == null && player.level() != null) {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("mca", "grim_reaper"));
                if (type != null) {
                    dummyReaper = (LivingEntity) type.create(player.level());

                    try {
                        var renderer = mc.getEntityRenderDispatcher().getRenderer(dummyReaper);
                        if (renderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?> livingRenderer) {
                            var skeletonModel = livingRenderer.getModel();

                            Field scytheBoneField = skeletonModel.getClass().getDeclaredField("scythe");
                            scytheBoneField.setAccessible(true);
                            physicalScytheBone = (ModelPart) scytheBoneField.get(skeletonModel);

                            if (skeletonModel instanceof HumanoidModel<?> humanoidModel) {
                                ModelPart leftArm = getHumanoidArm(humanoidModel, "f_102812_"); // leftArm
                                ModelPart rightArm = getHumanoidArm(humanoidModel, "f_102811_"); // rightArm

                                if (leftArm != null && rightArm != null && !reparentedToRightArm) {
                                    reparentScythe(leftArm, rightArm, physicalScytheBone);
                                    reparentedToRightArm = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (dummyReaper != null) {

                dummyReaper.setDeltaMovement(player.getDeltaMovement());
                dummyReaper.setPos(player.getX(), player.getY(), player.getZ());
                dummyReaper.xo = player.xo;
                dummyReaper.yo = player.yo;
                dummyReaper.zo = player.zo;

                dummyReaper.tickCount = player.tickCount;
                dummyReaper.yBodyRot = player.yBodyRot;
                dummyReaper.yBodyRotO = player.yBodyRotO;
                dummyReaper.yHeadRot = player.yHeadRot;
                dummyReaper.yHeadRotO = player.yHeadRotO;

                double dx = player.getX() - player.xo;
                double dz = player.getZ() - player.zo;
                double speedXZ = Math.sqrt(dx * dx + dz * dz);
                float bossScurveBend = (float) Math.min(speedXZ * 40.0f, 18.0f);

                dummyReaper.setXRot(player.getXRot() - bossScurveBend);
                dummyReaper.xRotO = player.xRotO - bossScurveBend;
                dummyReaper.setShiftKeyDown(player.isCrouching());
                dummyReaper.setPose(player.getPose());

                var playerMainHand = player.getMainHandItem();
                boolean hasRealScythe = playerMainHand.getDescriptionId().toLowerCase().contains("scythe") ||
                        playerMainHand.toString().toLowerCase().contains("scythe");

                if (hasRealScythe && physicalScytheBone != null) {
                    dummyReaper.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                    dummyReaper.swinging = player.swinging;
                    dummyReaper.swingingArm = net.minecraft.world.InteractionHand.MAIN_HAND;
                } else {
                    dummyReaper.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, playerMainHand);
                    if (physicalScytheBone != null) {
                        physicalScytheBone.visible = false;
                    }
                    dummyReaper.swinging = player.swinging;
                    dummyReaper.swingingArm = player.swingingArm;
                }

                dummyReaper.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, player.getOffhandItem());
                dummyReaper.swingTime = player.swingTime;
                dummyReaper.attackAnim = player.attackAnim;
                dummyReaper.oAttackAnim = player.oAttackAnim;
                dummyReaper.hurtTime = player.hurtTime;
                dummyReaper.hurtDuration = player.hurtDuration;
                dummyReaper.setCustomNameVisible(false);

                float tickPulseFloat = (float) (Math.sin((player.tickCount + event.getPartialTick()) * 0.12) * 0.05f);

                com.mojang.blaze3d.vertex.PoseStack poseMaster = event.getPoseStack();
                poseMaster.pushPose();
                poseMaster.translate(0, tickPulseFloat, 0);

                float pivotAnchorHeight = 1.55f;
                poseMaster.translate(0, pivotAnchorHeight, 0);
                poseMaster.mulPose(com.mojang.math.Axis.YN.rotationDegrees(dummyReaper.yBodyRot));
                poseMaster.mulPose(com.mojang.math.Axis.XP.rotationDegrees(bossScurveBend));
                poseMaster.mulPose(com.mojang.math.Axis.YP.rotationDegrees(dummyReaper.yBodyRot));
                poseMaster.translate(0, -pivotAnchorHeight, 0);

                mc.getEntityRenderDispatcher().render(
                        dummyReaper,
                        0.0D, 0.0D, 0.0D,
                        0.0F,
                        event.getPartialTick(),
                        poseMaster,
                        event.getMultiBufferSource(),
                        event.getPackedLight()
                );

                poseMaster.popPose();
            }
        }
    }

    // Fires AFTER setupAnim() has already reset the scythe's pose, so our override sticks.
    @SubscribeEvent
    public static void onDummyReaperRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() == dummyReaper && physicalScytheBone != null) {

            var playerHand = Minecraft.getInstance().player.getMainHandItem();
            boolean hasRealScythe = playerHand.getDescriptionId().toLowerCase().contains("scythe") ||
                    playerHand.toString().toLowerCase().contains("scythe");

            physicalScytheBone.visible = hasRealScythe;

            if (hasRealScythe) {
                physicalScytheBone.x = -7.0f;
                physicalScytheBone.y = 6.0f;
                physicalScytheBone.z = 0.0f;
                physicalScytheBone.xRot = (float) Math.toRadians(90.0f);
                physicalScytheBone.yRot = (float) Math.toRadians(20.0f);
                physicalScytheBone.zRot = (float) Math.toRadians(-90.0f);

                if (Minecraft.getInstance().player.tickCount % 40 == 0) {
                }
            }
        }
    }

    private static ModelPart getHumanoidArm(HumanoidModel<?> model, String srgFieldName) {
        try {
            Field f = HumanoidModel.class.getDeclaredField(srgFieldName);
            f.setAccessible(true);
            return (ModelPart) f.get(model);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static void reparentScythe(ModelPart oldParent, ModelPart newParent, ModelPart scythe) throws Exception {
        Field childrenField = findChildrenMapField();
        childrenField.setAccessible(true);

        Map<String, ModelPart> oldChildren = (Map<String, ModelPart>) childrenField.get(oldParent);
        Map<String, ModelPart> newChildren = (Map<String, ModelPart>) childrenField.get(newParent);

        oldChildren.remove(SCYTHE_KEY);
        newChildren.put(SCYTHE_KEY, scythe);
    }

    // Finds ModelPart's internal Map<String, ModelPart> field by type rather than by
    // (mapping-dependent) name, so this survives across mapping/version differences.
    private static Field findChildrenMapField() throws NoSuchFieldException {
        for (Field f : ModelPart.class.getDeclaredFields()) {
            if (Map.class.isAssignableFrom(f.getType())) {
                return f;
            }
        }
        throw new NoSuchFieldException("Could not locate ModelPart children map field");
    }
}