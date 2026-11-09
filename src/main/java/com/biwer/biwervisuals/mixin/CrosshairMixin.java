package com.biwer.biwervisuals.mixin;

import com.biwer.biwervisuals.BiwerVisualsClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class CrosshairMixin {

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void biwer$hideVanillaCrosshair(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (BiwerVisualsClient.CONFIG.crosshairEnabled) {
            ci.cancel();
        }
    }
}