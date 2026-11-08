package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class Fly {
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;

            if (BiwerVisualsClient.CONFIG.flyEnabled) {
                // Разрешаем полет (как в креативе)
                client.player.getAbilities().mayfly = true;
                client.player.getAbilities().flying = true;

                // Если игрок не в креативе, принудительно двигаем его вверх/вниз
                if (!client.player.isCreative() && !client.player.isSpectator()) {
                    Vec3 motion = client.player.getDeltaMovement();
                    double yMotion = 0;

                    // Управление: Пробел — вверх, Shift — вниз
                    if (client.options.keyJump.isDown()) {
                        yMotion = 0.5; // Скорость вверх
                    } else if (client.options.keyShift.isDown()) {
                        yMotion = -0.5; // Скорость вниз
                    }

                    // Применяем движение (только по Y)
                    client.player.setDeltaMovement(motion.x, yMotion, motion.z);
                    client.player.fallDistance = 0; // Сбрасываем урон от падения

                    // Обманываем сервер: говорим, что игрок на земле
                    if (client.getConnection() != null) {
                        client.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(true, false));
                    }
                }
            } else {
                // Если Fly выключен, возвращаем все как было (если не креатив)
                if (!client.player.isCreative() && !client.player.isSpectator()) {
                    client.player.getAbilities().mayfly = false;
                    client.player.getAbilities().flying = false;
                }
            }
        });
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.flyEnabled = !BiwerVisualsClient.CONFIG.flyEnabled;
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("§e[Fly] " + (BiwerVisualsClient.CONFIG.flyEnabled ? "§aВКЛ" : "§cВЫКЛ")));
        }
    }
}