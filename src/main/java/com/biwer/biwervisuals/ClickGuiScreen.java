package com.biwer.biwervisuals;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
    private final Screen parent;

    private final int bgColor = 0xCC101020;
    private final int textColor = 0xFFFFFFFF;
    private final int activeColor = 0xFFFF69B4;
    private final int inactiveColor = 0xFF2A2A40;
    private final int disabledColor = 0xFF1A1A25;

    public ClickGuiScreen(Screen parent) {
        super(Component.literal("Biwer Visuals"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int colWidth = 120;
        int padding = 5;
        int totalWidth = (colWidth * 5) + (padding * 4);
        int startX = (this.width - totalWidth) / 2;
        int startY = 20;
        int colHeight = 500; // Увеличили под все блоки

        // --- КОЛОНКА COMBAT ---
        graphics.fill(startX, startY, startX + colWidth, startY + colHeight, bgColor);
        graphics.text(this.font, "Combat", startX + 10, startY + 10, textColor, false);

        int combatY = startY + 30;
        graphics.text(this.font, "KillAura", startX + 10, combatY + 5, textColor, false);
        drawToggle(graphics, startX + 90, combatY + 5, 20, 10, BiwerVisualsClient.CONFIG.killAuraEnabled);

        if (!BiwerVisualsClient.CONFIG.killAuraCollapsed) {
            graphics.fill(startX + 5, combatY + 15, startX + colWidth - 5, combatY + 205, 0xAA000000);

            graphics.text(this.font, "Дистанция: " + String.format("%.1f", BiwerVisualsClient.CONFIG.killAuraRange), startX + 10, combatY + 20, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, combatY + 32, 100, 4, BiwerVisualsClient.CONFIG.killAuraRange, 1.0f, 6.0f, "");

            graphics.text(this.font, "Наведение:", startX + 10, combatY + 45, 0xFFAAAAAA, false);
            drawButton(graphics, "Свободно", startX + 10, combatY + 57, 35, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.aimMode == 0, true);
            drawButton(graphics, "Плавно", startX + 48, combatY + 57, 35, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.aimMode == 1, true);
            drawButton(graphics, "Резко", startX + 86, combatY + 57, 25, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.aimMode == 2, true);

            drawToggle(graphics, startX + 10, combatY + 75, 15, 8, BiwerVisualsClient.CONFIG.onlyCriticals);
            graphics.text(this.font, "Только криты", startX + 30, combatY + 75, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, combatY + 88, 15, 8, BiwerVisualsClient.CONFIG.adaptiveHits);
            graphics.text(this.font, "Адаптивные удары", startX + 30, combatY + 88, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, combatY + 101, 15, 8, BiwerVisualsClient.CONFIG.shieldBreaker);
            graphics.text(this.font, "Пробитие щита", startX + 30, combatY + 101, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, combatY + 114, 15, 8, BiwerVisualsClient.CONFIG.smartSprint);
            graphics.text(this.font, "Умный спринт", startX + 30, combatY + 114, 0xFFAAAAAA, false);

            graphics.text(this.font, "Цели:", startX + 10, combatY + 130, 0xFFAAAAAA, false);
            drawButton(graphics, "Игроки", startX + 10, combatY + 142, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.targetPlayers, true);
            drawButton(graphics, "Мобы", startX + 65, combatY + 142, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.targetMobs, true);
            drawButton(graphics, "Животные", startX + 10, combatY + 156, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.targetAnimals, true);
            drawButton(graphics, "Без брони", startX + 65, combatY + 156, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.targetNaked, true);

            graphics.text(this.font, "Не бить если:", startX + 10, combatY + 172, 0xFFAAAAAA, false);
            drawToggle(graphics, startX + 10, combatY + 184, 15, 8, BiwerVisualsClient.CONFIG.dontHitItemUse);
            graphics.text(this.font, "Юзаю предмет", startX + 30, combatY + 184, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, combatY + 197, 15, 8, BiwerVisualsClient.CONFIG.dontHitContainer);
            graphics.text(this.font, "Открыт инв.", startX + 30, combatY + 197, 0xFFAAAAAA, false);
        }

        // --- КОЛОНКА MOVEMENT ---
        int x2 = startX + colWidth + padding;
        graphics.fill(x2, startY, x2 + colWidth, startY + colHeight, bgColor);
        graphics.text(this.font, "Movement", x2 + 10, startY + 10, textColor, false);

        // --- КОЛОНКА RENDER ---
        int x3 = x2 + colWidth + padding;
        graphics.fill(x3, startY, x3 + colWidth, startY + colHeight, bgColor);
        graphics.text(this.font, "Render", x3 + 10, startY + 10, textColor, false);

        // Ambience
        int ambY = startY + 30;
        graphics.text(this.font, "Ambience", x3 + 10, ambY + 5, textColor, false);
        drawToggle(graphics, x3 + 90, ambY + 5, 20, 10, BiwerVisualsClient.CONFIG.ambienceEnabled);

        if (!BiwerVisualsClient.CONFIG.ambienceCollapsed) {
            graphics.fill(x3 + 5, ambY + 15, x3 + colWidth - 5, ambY + 140, 0xAA000000);

            int timeColor = BiwerVisualsClient.CONFIG.ambienceEnabled ? 0xFFAAAAAA : 0xFF555555;
            graphics.text(this.font, "Время", x3 + 10, ambY + 25, timeColor, false);
            drawButton(graphics, "Рассвет", x3 + 10, ambY + 40, 50, 15, mouseX, mouseY, BiwerVisualsClient.CONFIG.activeTimeButton == 1, BiwerVisualsClient.CONFIG.ambienceEnabled);
            drawButton(graphics, "День", x3 + 65, ambY + 40, 50, 15, mouseX, mouseY, BiwerVisualsClient.CONFIG.activeTimeButton == 2, BiwerVisualsClient.CONFIG.ambienceEnabled);
            drawButton(graphics, "Ночь", x3 + 10, ambY + 60, 50, 15, mouseX, mouseY, BiwerVisualsClient.CONFIG.activeTimeButton == 3, BiwerVisualsClient.CONFIG.ambienceEnabled);
            drawButton(graphics, "Полночь", x3 + 65, ambY + 60, 50, 15, mouseX, mouseY, BiwerVisualsClient.CONFIG.activeTimeButton == 4, BiwerVisualsClient.CONFIG.ambienceEnabled);

            int weatherColor = BiwerVisualsClient.CONFIG.ambienceEnabled ? 0xFFAAAAAA : 0xFF555555;
            graphics.text(this.font, "Погода", x3 + 10, ambY + 85, weatherColor, false);
            drawButton(graphics, "Ясно", x3 + 10, ambY + 100, 50, 15, mouseX, mouseY, BiwerVisualsClient.CONFIG.activeWeatherButton == 1, BiwerVisualsClient.CONFIG.ambienceEnabled);
            drawButton(graphics, "Дождь", x3 + 65, ambY + 100, 50, 15, mouseX, mouseY, BiwerVisualsClient.CONFIG.activeWeatherButton == 2, BiwerVisualsClient.CONFIG.ambienceEnabled);
            drawButton(graphics, "Гроза", x3 + 10, ambY + 120, 105, 15, mouseX, mouseY, BiwerVisualsClient.CONFIG.activeWeatherButton == 3, BiwerVisualsClient.CONFIG.ambienceEnabled);
        }

        // Viewmodel
        int vmY = ambY + (BiwerVisualsClient.CONFIG.ambienceCollapsed ? 30 : 150);
        graphics.text(this.font, "Viewmodel", x3 + 10, vmY + 5, textColor, false);
        drawToggle(graphics, x3 + 90, vmY + 5, 20, 10, BiwerVisualsClient.CONFIG.viewmodelEnabled);

        if (!BiwerVisualsClient.CONFIG.viewmodelCollapsed) {
            graphics.fill(x3 + 5, vmY + 15, x3 + colWidth - 5, vmY + 160, 0xAA000000);
            if (BiwerVisualsClient.CONFIG.viewmodelEnabled) {
                drawToggle(graphics, x3 + 10, vmY + 20, 15, 8, BiwerVisualsClient.CONFIG.vmPos);
                graphics.text(this.font, "Позиция", x3 + 30, vmY + 20, 0xFFAAAAAA, false);
                drawSlider(graphics, x3 + 10, vmY + 32, 100, 4, BiwerVisualsClient.CONFIG.vmPosX, -2, 2, "X");
                drawSlider(graphics, x3 + 10, vmY + 42, 100, 4, BiwerVisualsClient.CONFIG.vmPosY, -2, 2, "Y");
                drawSlider(graphics, x3 + 10, vmY + 52, 100, 4, BiwerVisualsClient.CONFIG.vmPosZ, -2, 2, "Z");

                drawToggle(graphics, x3 + 10, vmY + 65, 15, 8, BiwerVisualsClient.CONFIG.vmRot);
                graphics.text(this.font, "Поворот", x3 + 30, vmY + 65, 0xFFAAAAAA, false);
                drawSlider(graphics, x3 + 10, vmY + 77, 100, 4, BiwerVisualsClient.CONFIG.vmRotX, -180, 180, "X");
                drawSlider(graphics, x3 + 10, vmY + 87, 100, 4, BiwerVisualsClient.CONFIG.vmRotY, -180, 180, "Y");
                drawSlider(graphics, x3 + 10, vmY + 97, 100, 4, BiwerVisualsClient.CONFIG.vmRotZ, -180, 180, "Z");

                drawToggle(graphics, x3 + 10, vmY + 110, 15, 8, BiwerVisualsClient.CONFIG.vmScale);
                graphics.text(this.font, "Размер", x3 + 30, vmY + 110, 0xFFAAAAAA, false);
                drawSlider(graphics, x3 + 10, vmY + 122, 100, 4, BiwerVisualsClient.CONFIG.vmScaleX, 0, 3, "X");
                drawSlider(graphics, x3 + 10, vmY + 132, 100, 4, BiwerVisualsClient.CONFIG.vmScaleY, 0, 3, "Y");
                drawSlider(graphics, x3 + 10, vmY + 142, 100, 4, BiwerVisualsClient.CONFIG.vmScaleZ, 0, 3, "Z");
            }
        }

        // Entity ESP
        int espY = vmY + (BiwerVisualsClient.CONFIG.viewmodelCollapsed ? 30 : 170);
        graphics.text(this.font, "Entity ESP", x3 + 10, espY + 5, textColor, false);
        drawToggle(graphics, x3 + 90, espY + 5, 20, 10, BiwerVisualsClient.CONFIG.espEnabled);

        if (!BiwerVisualsClient.CONFIG.espCollapsed) {
            graphics.fill(x3 + 5, espY + 15, x3 + colWidth - 5, espY + 70, 0xAA000000);
            drawToggle(graphics, x3 + 10, espY + 20, 15, 8, BiwerVisualsClient.CONFIG.espPlayers);
            graphics.text(this.font, "Игроки", x3 + 30, espY + 20, 0xFFAAAAAA, false);
            drawToggle(graphics, x3 + 10, espY + 33, 15, 8, BiwerVisualsClient.CONFIG.espMobs);
            graphics.text(this.font, "Мобы", x3 + 30, espY + 33, 0xFFAAAAAA, false);
            drawToggle(graphics, x3 + 10, espY + 46, 15, 8, BiwerVisualsClient.CONFIG.espAnimals);
            graphics.text(this.font, "Животные", x3 + 30, espY + 46, 0xFFAAAAAA, false);
            drawToggle(graphics, x3 + 10, espY + 59, 15, 8, BiwerVisualsClient.CONFIG.espNaked);
            graphics.text(this.font, "Без брони", x3 + 30, espY + 59, 0xFFAAAAAA, false);
        }

        // Tags
        int tagsY = espY + (BiwerVisualsClient.CONFIG.espCollapsed ? 30 : 80);
        graphics.text(this.font, "Tags", x3 + 10, tagsY + 5, textColor, false);
        drawToggle(graphics, x3 + 90, tagsY + 5, 20, 10, BiwerVisualsClient.CONFIG.tagsEnabled);

        if (!BiwerVisualsClient.CONFIG.tagsCollapsed) {
            graphics.fill(x3 + 5, tagsY + 15, x3 + colWidth - 5, tagsY + 70, 0xAA000000);
            drawToggle(graphics, x3 + 10, tagsY + 20, 15, 8, BiwerVisualsClient.CONFIG.tagsPlayers);
            graphics.text(this.font, "Игроки", x3 + 30, tagsY + 20, 0xFFAAAAAA, false);
            drawToggle(graphics, x3 + 10, tagsY + 33, 15, 8, BiwerVisualsClient.CONFIG.tagsMobs);
            graphics.text(this.font, "Мобы", x3 + 30, tagsY + 33, 0xFFAAAAAA, false);
            drawToggle(graphics, x3 + 10, tagsY + 46, 15, 8, BiwerVisualsClient.CONFIG.tagsAnimals);
            graphics.text(this.font, "Животные", x3 + 30, tagsY + 46, 0xFFAAAAAA, false);
        }

        // --- PLAYER и MISC ---
        int x4 = x3 + colWidth + padding;
        graphics.fill(x4, startY, x4 + colWidth, startY + colHeight, bgColor);
        graphics.text(this.font, "Player", x4 + 10, startY + 10, textColor, false);

        int x5 = x4 + colWidth + padding;
        graphics.fill(x5, startY, x5 + colWidth, startY + colHeight, bgColor);
        graphics.text(this.font, "Misc", x5 + 10, startY + 10, textColor, false);

        graphics.text(this.font, "X", this.width - 20, 10, 0xFFFF5555, false);
    }

    private void drawButton(GuiGraphicsExtractor graphics, String text, int x, int y, int width, int height, int mouseX, int mouseY, boolean isActive, boolean isEnabled) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        int color = !isEnabled ? disabledColor : (isActive ? activeColor : (hovered ? 0xFF4A4A6A : inactiveColor));
        graphics.fill(x, y, x + width, y + height, color);
        graphics.text(this.font, text, x + (width / 2) - (this.font.width(text) / 2), y + 2, isEnabled ? textColor : 0xFF777777, false);
    }

    private void drawToggle(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean state) {
        int bg = state ? 0xFF5A3A8A : 0xFF2A2A40;
        graphics.fill(x, y, x + width, y + height, bg);
        int circleSize = height - 2;
        int circleX = state ? (x + width - circleSize - 1) : (x + 1);
        graphics.fill(circleX, y + 1, circleX + circleSize, y + height - 1, 0xFFFFFFFF);
    }

    private void drawSlider(GuiGraphicsExtractor graphics, int x, int y, int width, int height, float value, float min, float max, String label) {
        if (!label.isEmpty()) graphics.text(this.font, label, x - 10, y - 2, 0xFFAAAAAA, false);
        graphics.fill(x, y, x + width, y + height, 0xFF333333);
        int filled = (int) ((value - min) / (max - min) * width);
        graphics.fill(x, y, x + filled, y + height, activeColor);
        graphics.fill(x + filled - 1, y - 2, x + filled + 1, y + height + 2, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        int colWidth = 120;
        int padding = 5;
        int totalWidth = (colWidth * 5) + (padding * 4);
        int startX = (this.width - totalWidth) / 2;
        int startY = 20;

        int x1 = startX;
        int combatY = startY + 30;

        int x3 = startX + (colWidth + padding) * 2;
        int ambY = startY + 30;
        int vmY = ambY + (BiwerVisualsClient.CONFIG.ambienceCollapsed ? 30 : 150);
        int espY = vmY + (BiwerVisualsClient.CONFIG.viewmodelCollapsed ? 30 : 170);
        int tagsY = espY + (BiwerVisualsClient.CONFIG.espCollapsed ? 30 : 80);

        if (button == 1) { // Сворачивание
            if (isHovered(mouseX, mouseY, x1 + 5, combatY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.killAuraCollapsed = !BiwerVisualsClient.CONFIG.killAuraCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x3 + 5, ambY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.ambienceCollapsed = !BiwerVisualsClient.CONFIG.ambienceCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x3 + 5, vmY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.viewmodelCollapsed = !BiwerVisualsClient.CONFIG.viewmodelCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x3 + 5, espY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.espCollapsed = !BiwerVisualsClient.CONFIG.espCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x3 + 5, tagsY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.tagsCollapsed = !BiwerVisualsClient.CONFIG.tagsCollapsed; return true; }
        }

        if (button == 0) { // Левый клик
            // KillAura
            if (isHovered(mouseX, mouseY, x1 + 90, combatY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.killAuraEnabled = !BiwerVisualsClient.CONFIG.killAuraEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.killAuraCollapsed) {
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 32, 100, 4)) { BiwerVisualsClient.CONFIG.killAuraRange = (float) ((mouseX - (x1 + 10)) / 100.0 * 5 + 1); return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 57, 35, 12)) { BiwerVisualsClient.CONFIG.aimMode = 0; return true; }
                if (isHovered(mouseX, mouseY, x1 + 48, combatY + 57, 35, 12)) { BiwerVisualsClient.CONFIG.aimMode = 1; return true; }
                if (isHovered(mouseX, mouseY, x1 + 86, combatY + 57, 25, 12)) { BiwerVisualsClient.CONFIG.aimMode = 2; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 75, 15, 8)) { BiwerVisualsClient.CONFIG.onlyCriticals = !BiwerVisualsClient.CONFIG.onlyCriticals; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 88, 15, 8)) { BiwerVisualsClient.CONFIG.adaptiveHits = !BiwerVisualsClient.CONFIG.adaptiveHits; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 101, 15, 8)) { BiwerVisualsClient.CONFIG.shieldBreaker = !BiwerVisualsClient.CONFIG.shieldBreaker; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 114, 15, 8)) { BiwerVisualsClient.CONFIG.smartSprint = !BiwerVisualsClient.CONFIG.smartSprint; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 142, 50, 12)) { BiwerVisualsClient.CONFIG.targetPlayers = !BiwerVisualsClient.CONFIG.targetPlayers; return true; }
                if (isHovered(mouseX, mouseY, x1 + 65, combatY + 142, 50, 12)) { BiwerVisualsClient.CONFIG.targetMobs = !BiwerVisualsClient.CONFIG.targetMobs; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 156, 50, 12)) { BiwerVisualsClient.CONFIG.targetAnimals = !BiwerVisualsClient.CONFIG.targetAnimals; return true; }
                if (isHovered(mouseX, mouseY, x1 + 65, combatY + 156, 50, 12)) { BiwerVisualsClient.CONFIG.targetNaked = !BiwerVisualsClient.CONFIG.targetNaked; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 184, 15, 8)) { BiwerVisualsClient.CONFIG.dontHitItemUse = !BiwerVisualsClient.CONFIG.dontHitItemUse; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 197, 15, 8)) { BiwerVisualsClient.CONFIG.dontHitContainer = !BiwerVisualsClient.CONFIG.dontHitContainer; return true; }
            }

            // Ambience
            if (isHovered(mouseX, mouseY, x3 + 90, ambY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.ambienceEnabled = !BiwerVisualsClient.CONFIG.ambienceEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.ambienceCollapsed && BiwerVisualsClient.CONFIG.ambienceEnabled) {
                if (isHovered(mouseX, mouseY, x3 + 10, ambY + 40, 50, 15)) { BiwerVisualsClient.CONFIG.activeTimeButton = 1; return true; }
                if (isHovered(mouseX, mouseY, x3 + 65, ambY + 40, 50, 15)) { BiwerVisualsClient.CONFIG.activeTimeButton = 2; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, ambY + 60, 50, 15)) { BiwerVisualsClient.CONFIG.activeTimeButton = 3; return true; }
                if (isHovered(mouseX, mouseY, x3 + 65, ambY + 60, 50, 15)) { BiwerVisualsClient.CONFIG.activeTimeButton = 4; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, ambY + 100, 50, 15)) { BiwerVisualsClient.CONFIG.activeWeatherButton = 1; return true; }
                if (isHovered(mouseX, mouseY, x3 + 65, ambY + 100, 50, 15)) { BiwerVisualsClient.CONFIG.activeWeatherButton = 2; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, ambY + 120, 105, 15)) { BiwerVisualsClient.CONFIG.activeWeatherButton = 3; return true; }
            }

            // Viewmodel
            if (isHovered(mouseX, mouseY, x3 + 90, vmY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.viewmodelEnabled = !BiwerVisualsClient.CONFIG.viewmodelEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.viewmodelCollapsed && BiwerVisualsClient.CONFIG.viewmodelEnabled) {
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 20, 15, 8)) { BiwerVisualsClient.CONFIG.vmPos = !BiwerVisualsClient.CONFIG.vmPos; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 65, 15, 8)) { BiwerVisualsClient.CONFIG.vmRot = !BiwerVisualsClient.CONFIG.vmRot; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 110, 15, 8)) { BiwerVisualsClient.CONFIG.vmScale = !BiwerVisualsClient.CONFIG.vmScale; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 32, 100, 4)) { BiwerVisualsClient.CONFIG.vmPosX = (float) ((mouseX - (x3 + 10)) / 100.0 * 4 - 2); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 42, 100, 4)) { BiwerVisualsClient.CONFIG.vmPosY = (float) ((mouseX - (x3 + 10)) / 100.0 * 4 - 2); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 52, 100, 4)) { BiwerVisualsClient.CONFIG.vmPosZ = (float) ((mouseX - (x3 + 10)) / 100.0 * 4 - 2); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 77, 100, 4)) { BiwerVisualsClient.CONFIG.vmRotX = (float) ((mouseX - (x3 + 10)) / 100.0 * 360 - 180); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 87, 100, 4)) { BiwerVisualsClient.CONFIG.vmRotY = (float) ((mouseX - (x3 + 10)) / 100.0 * 360 - 180); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 97, 100, 4)) { BiwerVisualsClient.CONFIG.vmRotZ = (float) ((mouseX - (x3 + 10)) / 100.0 * 360 - 180); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 122, 100, 4)) { BiwerVisualsClient.CONFIG.vmScaleX = (float) ((mouseX - (x3 + 10)) / 100.0 * 3); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 132, 100, 4)) { BiwerVisualsClient.CONFIG.vmScaleY = (float) ((mouseX - (x3 + 10)) / 100.0 * 3); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 142, 100, 4)) { BiwerVisualsClient.CONFIG.vmScaleZ = (float) ((mouseX - (x3 + 10)) / 100.0 * 3); return true; }
            }

            // Entity ESP
            if (isHovered(mouseX, mouseY, x3 + 90, espY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.espEnabled = !BiwerVisualsClient.CONFIG.espEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.espCollapsed && BiwerVisualsClient.CONFIG.espEnabled) {
                if (isHovered(mouseX, mouseY, x3 + 10, espY + 20, 15, 8)) { BiwerVisualsClient.CONFIG.espPlayers = !BiwerVisualsClient.CONFIG.espPlayers; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, espY + 33, 15, 8)) { BiwerVisualsClient.CONFIG.espMobs = !BiwerVisualsClient.CONFIG.espMobs; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, espY + 46, 15, 8)) { BiwerVisualsClient.CONFIG.espAnimals = !BiwerVisualsClient.CONFIG.espAnimals; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, espY + 59, 15, 8)) { BiwerVisualsClient.CONFIG.espNaked = !BiwerVisualsClient.CONFIG.espNaked; return true; }
            }

            // Tags
            if (isHovered(mouseX, mouseY, x3 + 90, tagsY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.tagsEnabled = !BiwerVisualsClient.CONFIG.tagsEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.tagsCollapsed && BiwerVisualsClient.CONFIG.tagsEnabled) {
                if (isHovered(mouseX, mouseY, x3 + 10, tagsY + 20, 15, 8)) { BiwerVisualsClient.CONFIG.tagsPlayers = !BiwerVisualsClient.CONFIG.tagsPlayers; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, tagsY + 33, 15, 8)) { BiwerVisualsClient.CONFIG.tagsMobs = !BiwerVisualsClient.CONFIG.tagsMobs; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, tagsY + 46, 15, 8)) { BiwerVisualsClient.CONFIG.tagsAnimals = !BiwerVisualsClient.CONFIG.tagsAnimals; return true; }
            }

            // Закрытие
            if (mouseX >= this.width - 20 && mouseX <= this.width - 10 && mouseY >= 10 && mouseY <= 20) {
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}