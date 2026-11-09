package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Crosshair — настраиваемый прицел (порт aethereal Crosshair).
 *
 * Рисуется как HUD-элемент, ванильный прицел скрывается миксином
 * CrosshairMixin. Поддерживает зазор, длину сегментов, контур,
 * центральную точку и адаптивный зазор по кулдауну удара.
 */
public class Crosshair implements HudElement {
    public static void init() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("biwervisuals", "crosshair"), new Crosshair());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!BiwerVisualsClient.CONFIG.crosshairEnabled) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;

        float cooldown = 1.0f - client.player.getAttackStrengthScale(0.5f);
        float gap = BiwerVisualsClient.CONFIG.crosshairGap;
        if (BiwerVisualsClient.CONFIG.crosshairAdaptive) {
            gap += 8.0f * cooldown;
        }

        int length = Math.max(1, Math.round(BiwerVisualsClient.CONFIG.crosshairLength));
        int g = Math.round(gap);
        int color = client.crosshairPickEntity != null ? 0xFFFF4040 : 0xFFFFFFFF;

        if (BiwerVisualsClient.CONFIG.crosshairOutline) {
            int outline = 0xC0000000;
            bar(graphics, cx + g - 1, cy - 2, length + 2, 4, outline);
            bar(graphics, cx - g - length - 1, cy - 2, length + 2, 4, outline);
            bar(graphics, cx - 2, cy - g - length - 1, 4, length + 2, outline);
            bar(graphics, cx - 2, cy + g - 1, 4, length + 2, outline);
        }

        bar(graphics, cx + g, cy - 1, length, 2, color);
        bar(graphics, cx - g - length, cy - 1, length, 2, color);
        bar(graphics, cx - 1, cy - g - length, 2, length, color);
        bar(graphics, cx - 1, cy + g, 2, length, color);

        if (BiwerVisualsClient.CONFIG.crosshairCenterDot) {
            bar(graphics, cx - 1, cy - 1, 2, 2, color);
        }
    }

    private static void bar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.crosshairEnabled = !BiwerVisualsClient.CONFIG.crosshairEnabled;
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.crosshairEnabled ? "§aВКЛ" : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[Crosshair] " + state));
        }
    }
}