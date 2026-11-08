package com.biwer.biwervisuals.mixin;

import com.biwer.biwervisuals.BiwerVisualsClient;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.ClientClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientClockManager.class)
public class ClientClockManagerMixin {

    @ModifyReturnValue(method = "getTotalTicks", at = @At("RETURN"))
    private long modifyTotalTicks(long original) {
        if (BiwerVisualsClient.CONFIG.ambienceEnabled && BiwerVisualsClient.CONFIG.activeTimeButton > 0) {
            return switch (BiwerVisualsClient.CONFIG.activeTimeButton) {
                case 1 -> 1000L;   // Рассвет
                case 2 -> 6000L;   // День
                case 3 -> 13000L;  // Ночь
                case 4 -> 18000L;  // Полночь
                default -> original;
            };
        }
        return original;
    }
}