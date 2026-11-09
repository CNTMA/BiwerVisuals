package com.biwer.biwervisuals;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * SeeInvisibles — показывает невидимых существ (порт aethereal).
 *
 * Работает через миксин SeeInvisiblesMixin: для всех сущностей, кроме
 * локального игрока, Entity#isInvisible возвращает false, поэтому рендер
 * рисует их как обычных.
 */
public class SeeInvisibles {
    public static void init() {
        // Вся работа в SeeInvisiblesMixin.
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.seeInvisiblesEnabled = !BiwerVisualsClient.CONFIG.seeInvisiblesEnabled;
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.seeInvisiblesEnabled ? "§aВКЛ" : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[SeeInvisibles] " + state));
        }
    }
}