package com.biwer.biwervisuals.mixin;

import com.biwer.biwervisuals.BiwerVisualsClient;
import com.biwer.biwervisuals.SwingAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {

    /**
     * Перехватываем ванильную анимацию взмаха (applyItemArmAttackTransform).
     * Если SwingAnimation применил свою — отменяем ванильную.
     */
    @Inject(
            method = "applyItemArmAttackTransform",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onApplyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress, CallbackInfo ci) {
        if (SwingAnimation.applyVanillaOverride(poseStack, arm, swingProgress)) {
            ci.cancel();
        }
    }

    /**
     * Viewmodel (порт aethereal): смещения основной и второй руки.
     */
    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void onRenderArmWithItem(AbstractClientPlayer player, float tickDelta, float pitch,
                                     InteractionHand hand, float swingProgress, ItemStack item,
                                     float equipProgress, PoseStack matrices,
                                     SubmitNodeCollector vertexConsumers, int light, CallbackInfo ci) {
        if (BiwerVisualsClient.CONFIG.viewmodelEnabled) {
            if (hand == InteractionHand.MAIN_HAND) {
                matrices.translate(
                        BiwerVisualsClient.CONFIG.vmMainX,
                        BiwerVisualsClient.CONFIG.vmMainY,
                        BiwerVisualsClient.CONFIG.vmMainZ
                );
            } else {
                matrices.translate(
                        BiwerVisualsClient.CONFIG.vmOffX,
                        BiwerVisualsClient.CONFIG.vmOffY,
                        BiwerVisualsClient.CONFIG.vmOffZ
                );
            }
        }
    }
}