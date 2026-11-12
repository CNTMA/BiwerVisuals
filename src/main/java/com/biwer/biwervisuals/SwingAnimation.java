package com.biwer.biwervisuals;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

/**
 * SwingAnimation — настраиваемая анимация взмаха руки (порт aethereal).
 *
 * В 26.1.2 ванильная анимация применяется в applyItemArmAttackTransform.
 * Миксин перехватывает его и вызывает applyVanillaOverride. Если метод
 * вернул true — ванильная анимация отменяется, и рука двигается только
 * по нашим трансформациям (Мод 1..5).
 */
public class SwingAnimation {
    public static void init() {
        // Вызов из HeldItemRendererMixin.
    }

    /**
     * @return true, если ванильный applyItemArmAttackTransform нужно отменить.
     */
    public static boolean applyVanillaOverride(PoseStack matrices, HumanoidArm arm, float swingProgress) {
        if (!BiwerVisualsClient.CONFIG.swingAnimationEnabled) return false;
        if (BiwerVisualsClient.CONFIG.swingAnimationAuraOnly && Aura.getTarget() == null) return false;

        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        int mode = Math.max(0, Math.min(4, BiwerVisualsClient.CONFIG.swingAnimationMode));
        float anim = (float) Math.sin(swingProgress * Math.PI);
        float power = BiwerVisualsClient.CONFIG.swingPower * 10.0f;

        matrices.translate(side * (mode == 4 ? 0.5f : 0.72f), -0.5f, mode == 4 ? -0.72f : -1.0f);
        if (mode != 4) {
            matrices.mulPose(Axis.XP.rotationDegrees(-BiwerVisualsClient.CONFIG.swingTipTilt));
        }

        switch (mode) {
            case 0 -> { // Мод 1
                matrices.mulPose(Axis.YP.rotationDegrees(side * 90));
                matrices.mulPose(Axis.ZP.rotationDegrees(side * -70));
                matrices.mulPose(Axis.XP.rotationDegrees(-BiwerVisualsClient.CONFIG.swingAngle - power * anim));
            }
            case 1 -> { // Мод 2
                matrices.mulPose(Axis.YP.rotationDegrees(side * 90));
                matrices.mulPose(Axis.ZP.rotationDegrees(side * -65));
                matrices.mulPose(Axis.XP.rotationDegrees(-65.0f + power * anim));
            }
            case 2 -> { // Мод 3
                matrices.mulPose(Axis.YP.rotationDegrees(side * -90));
                matrices.mulPose(Axis.ZP.rotationDegrees(side * 60));
                matrices.mulPose(Axis.XP.rotationDegrees(30.0f));
                matrices.mulPose(Axis.ZP.rotationDegrees(side * power * anim));
            }
            case 3 -> { // Мод 4
                matrices.mulPose(Axis.YP.rotationDegrees(side * 90));
                matrices.mulPose(Axis.ZP.rotationDegrees(side * -75));
                matrices.mulPose(Axis.XP.rotationDegrees(-45.0f - power * anim));
                matrices.mulPose(Axis.YP.rotationDegrees(side * power * anim * 0.5f));
            }
            default -> { // Мод 5
                float strength = power / 80.0f;
                float swing = anim * anim;
                float twist = (float) Math.sin((swingProgress * swingProgress) * Math.PI);
                matrices.mulPose(Axis.YP.rotationDegrees(side * (45.0f + twist * -20.0f * strength)));
                matrices.mulPose(Axis.ZP.rotationDegrees(side * swing * -22.0f * strength));
                matrices.mulPose(Axis.XP.rotationDegrees(swing * -85.0f * strength));
                matrices.mulPose(Axis.YP.rotationDegrees(side * -45.0f));
            }
        }

        return true;
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.swingAnimationEnabled = !BiwerVisualsClient.CONFIG.swingAnimationEnabled;
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.swingAnimationEnabled
                    ? "§aВКЛ §7(мод " + (BiwerVisualsClient.CONFIG.swingAnimationMode + 1) + ")"
                    : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[SwingAnimation] " + state));
        }
    }
}