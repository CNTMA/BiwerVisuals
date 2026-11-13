package com.biwer.biwervisuals.mixin;

import com.biwer.biwervisuals.BiwerVisualsClient;
import com.biwer.biwervisuals.SwingAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {

    // Отмена ванильного взмаха
    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true, index = 5)
    private float biwer$cancelVanillaSwing(float swingProgress) {
        return SwingAnimation.cancelVanilla() ? 0.0f : swingProgress;
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void onRenderArmWithItem(AbstractClientPlayer player, float tickDelta, float pitch,
                                     InteractionHand hand, float swingProgress, ItemStack item,
                                     float equipProgress, PoseStack matrices,
                                     SubmitNodeCollector vertexConsumers, int light, CallbackInfo ci) {

        // 1. VIEWMODEL (смещение и поворот)
        if (BiwerVisualsClient.CONFIG.viewmodelEnabled) {
            if (hand == InteractionHand.MAIN_HAND) {
                // Смещение основной руки
                matrices.translate(
                        BiwerVisualsClient.CONFIG.vmMainX,
                        BiwerVisualsClient.CONFIG.vmMainY,
                        BiwerVisualsClient.CONFIG.vmMainZ
                );
                // Повороты основной руки (до 360 градусов)
                if (BiwerVisualsClient.CONFIG.vmRotMainX != 0) {
                    matrices.mulPose(Axis.XP.rotationDegrees(BiwerVisualsClient.CONFIG.vmRotMainX));
                }
                if (BiwerVisualsClient.CONFIG.vmRotMainY != 0) {
                    matrices.mulPose(Axis.YP.rotationDegrees(BiwerVisualsClient.CONFIG.vmRotMainY));
                }
                if (BiwerVisualsClient.CONFIG.vmRotMainZ != 0) {
                    matrices.mulPose(Axis.ZP.rotationDegrees(BiwerVisualsClient.CONFIG.vmRotMainZ));
                }
            } else {
                // Смещение второй руки (без поворотов)
                matrices.translate(
                        BiwerVisualsClient.CONFIG.vmOffX,
                        BiwerVisualsClient.CONFIG.vmOffY,
                        BiwerVisualsClient.CONFIG.vmOffZ
                );
            }
        }

        // 2. SWING ANIMATION (взмах)
        SwingAnimation.apply(matrices, player, hand, tickDelta);
    }
}