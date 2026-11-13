package com.biwer.biwervisuals.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class LoadingStyleMixin {
    @Shadow private float currentProgress;
    @Shadow private long fadeOutStart;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void biwer$loading(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float opacity = fadeOutStart < 0 ? 1f
                : 1f - Math.clamp((Util.getMillis() - fadeOutStart - 1000) / 1000f, 0f, 1f);
        if (opacity <= 0) return;
        int alpha = Math.round(opacity * 255);
        int w = g.guiWidth(), h = g.guiHeight();
        g.fillGradient(0, 0, w, h, (alpha << 24) | 0x211232, (alpha << 24) | 0x090711);
        int size = Math.max(50, Math.min(w, h) / 3);
        int x = (w - size) / 2, y = (h - size) / 2 - 15;
        g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath("biwervisuals", "textures/menu/logo.png"),
                x, y, 0f, 0f, size, size, 1, 1, 1, 1, (alpha << 24) | 0xD8B9FF);
        int barWidth = Math.min(180, w - 40), left = (w - barWidth) / 2, top = y + size + 8;
        g.fill(left, top, left + barWidth, top + 3, (alpha << 24) | 0x382547);
        g.fill(left, top, left + Math.round(barWidth * Math.clamp(currentProgress, 0f, 1f)),
                top + 3, (alpha << 24) | 0xBD8CFF);
    }
}
