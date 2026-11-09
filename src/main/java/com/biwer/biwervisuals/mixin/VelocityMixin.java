package com.biwer.biwervisuals.mixin;

import com.biwer.biwervisuals.BiwerVisualsClient;
import com.biwer.biwervisuals.Velocity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class VelocityMixin {

    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    private void biwer$onDamage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && packet.entityId() == client.player.getId()) {
            Velocity.onDamage();
        }
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void biwer$onSetEntityMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        if (!BiwerVisualsClient.CONFIG.velocityEnabled) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || packet.id() != client.player.getId()) return;

        Vec3 movement = packet.movement();
        if (!Velocity.shouldSuppress(movement)) return;

        ci.cancel();

        if (BiwerVisualsClient.CONFIG.velocityMode == 1) {
            Velocity.onKnockback(movement);
        }
    }
}