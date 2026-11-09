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

    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true, index = 5)
    private float biwer$cancelVanillaSwing(float swingProgress) {
        return SwingAnimation.cancelVanilla() ? 0.0f : swingProgress;
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void onRenderArmWithItem(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, CallbackInfo ci) {

        if (BiwerVisualsClient.CONFIG.viewmodelEnabled) {
            // Порт aethereal View Model: отдельные смещения для основной и второй руки.
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

        SwingAnimation.apply(matrices, player, hand, tickDelta);
    }
}