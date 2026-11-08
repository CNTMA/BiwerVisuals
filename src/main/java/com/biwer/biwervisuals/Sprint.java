package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Sprint {
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (BiwerVisualsClient.CONFIG.sprintEnabled) {
                // Если игрок движется вперёд (нажата W), но не спринтует — включаем спринт
                if (client.options.keyUp.isDown() && !client.player.isSprinting()) {
                    client.player.setSprinting(true);
                }
            }
        });
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.sprintEnabled = !BiwerVisualsClient.CONFIG.sprintEnabled;
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("§e[Sprint] " + (BiwerVisualsClient.CONFIG.sprintEnabled ? "§aВКЛ" : "§cВЫКЛ")));
        }
    }
}