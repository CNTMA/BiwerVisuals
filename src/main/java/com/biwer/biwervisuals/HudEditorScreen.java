package com.biwer.biwervisuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Визуальный редактор HUD: элементы можно тащить мышью.
 * ЛКМ — перетащить, ПКМ — сбросить элемент на место по умолчанию, Esc — выход.
 */
public class HudEditorScreen extends Screen {
    private final Screen parent;

    private Hud.Element dragging;
    private double grabX;
    private double grabY;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.fill(0, 0, this.width, this.height, 0x88000000);
        Hud.drawAll(graphics, Minecraft.getInstance(), true);

        String hint = "ЛКМ — перетащить   ПКМ — сброс элемента   Esc — выход";
        graphics.text(this.font, hint, 8, this.height - 14, 0xFFFFFF00, true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        Minecraft mc = Minecraft.getInstance();
        double mouseX = click.x();
        double mouseY = click.y();

        for (Hud.Element element : Hud.Element.values()) {
            int[] b = Hud.bounds(element);
            boolean inside = mouseX >= b[0] - 2 && mouseX <= b[0] + b[2] + 2
                    && mouseY >= b[1] - 2 && mouseY <= b[1] + b[3] + 2;
            if (!inside) continue;

            if (click.button() == 0) {
                dragging = element;
                grabX = mouseX - b[0];
                grabY = mouseY - b[1];
                return true;
            }
            if (click.button() == 1) {
                Hud.resetElement(element);
                Hud.save();
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
        if (dragging == null) return super.mouseDragged(click, dragX, dragY);

        Minecraft mc = Minecraft.getInstance();
        int[] b = Hud.bounds(dragging);
        float x = (float) (click.x() - grabX);
        float y = (float) (click.y() - grabY);

        x = Math.max(0.0f, Math.min(this.width - b[2], x));
        y = Math.max(0.0f, Math.min(this.height - b[3], y));

        Hud.setPos(dragging, x, y);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (dragging != null) {
            dragging = null;
            Hud.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void onClose() {
        Hud.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}