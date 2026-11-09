package com.biwer.biwervisuals.mixin;

import com.biwer.biwervisuals.BiwerVisualsClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class SeeInvisiblesMixin {

    @Inject(method = "isInvisible", at = @At("RETURN"), cancellable = true)
    private void biwer$seeInvisibles(CallbackInfoReturnable<Boolean> cir) {
        if (!BiwerVisualsClient.CONFIG.seeInvisiblesEnabled) return;
        if (!cir.getReturnValue()) return;
        if ((Object) this instanceof LocalPlayer) return;
        cir.setReturnValue(false);
    }
}