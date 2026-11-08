package com.biwer.biwervisuals.mixin;

import com.biwer.biwervisuals.BiwerVisualsClient;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void onRenderArmWithItem(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, CallbackInfo ci) {

        if (BiwerVisualsClient.CONFIG.viewmodelEnabled) {
            // Сдвиг
            if (BiwerVisualsClient.CONFIG.vmPos) {
                matrices.translate(
                        BiwerVisualsClient.CONFIG.vmPosX * 0.1,
                        BiwerVisualsClient.CONFIG.vmPosY * 0.1,
                        BiwerVisualsClient.CONFIG.vmPosZ * 0.1
                );
            }

            // Поворот
            if (BiwerVisualsClient.CONFIG.vmRot) {
                matrices.mulPose(Axis.YP.rotationDegrees(BiwerVisualsClient.CONFIG.vmRotY));
                matrices.mulPose(Axis.XP.rotationDegrees(BiwerVisualsClient.CONFIG.vmRotX));
                matrices.mulPose(Axis.ZP.rotationDegrees(BiwerVisualsClient.CONFIG.vmRotZ));
            }

            // Масштаб
            if (BiwerVisualsClient.CONFIG.vmScale) {
                matrices.scale(
                        1.0f - (1.0f - BiwerVisualsClient.CONFIG.vmScaleX) * 0.1f,
                        1.0f - (1.0f - BiwerVisualsClient.CONFIG.vmScaleY) * 0.1f,
                        1.0f - (1.0f - BiwerVisualsClient.CONFIG.vmScaleZ) * 0.1f
                );
            }
        }
    }
}