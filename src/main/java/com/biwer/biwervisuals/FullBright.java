package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * FullBright — полное освещение мира (порт aethereal Full Bright).
 *
 * Режим «Гамма» выкручивает опцию gamma (запоминаем исходное значение),
 * режим «Ночное зрение» держит на игроке эффект ночного зрения.
 */
public class FullBright {
    private static double savedGamma = -1.0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(FullBright::tick);
    }

    private static void tick(Minecraft client) {
        if (client.player == null) return;

        if (!BiwerVisualsClient.CONFIG.fullBrightEnabled) {
            restore(client);
            return;
        }

        if (BiwerVisualsClient.CONFIG.fullBrightMode == 1) {
            client.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, false, false, false));
        } else {
            if (savedGamma < 0.0) savedGamma = client.options.gamma().get();
            client.options.gamma().set((double) BiwerVisualsClient.CONFIG.fullBrightGamma);
        }
    }

    private static void restore(Minecraft client) {
        if (savedGamma >= 0.0) {
            client.options.gamma().set(savedGamma);
            savedGamma = -1.0;
        }
        if (client.player != null) {
            client.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.fullBrightEnabled = !BiwerVisualsClient.CONFIG.fullBrightEnabled;
        if (client != null && client.player != null) {
            String[] modes = {"Гамма", "Ночное зрение"};
            int mode = Math.max(0, Math.min(1, BiwerVisualsClient.CONFIG.fullBrightMode));
            String state = BiwerVisualsClient.CONFIG.fullBrightEnabled
                    ? "§aВКЛ §7(" + modes[mode] + ")"
                    : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[FullBright] " + state));
        }
    }
}