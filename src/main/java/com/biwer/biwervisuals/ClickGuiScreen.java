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

    // --- CustomSky: раскладка панели северного сияния (смещения от skyY) ---
    private static final int SKY_PANEL_BOTTOM = 112;
    private static final int SKY_AURORA_PANEL_BOTTOM = 203;
    private static final int SKY_AURORA_TOGGLE_Y = 113;
    private static final int SKY_AURORA_BOTTOM_LABEL_Y = 126;
    private static final int SKY_AURORA_BOTTOM_SLIDER_Y = 139;
    private static final int SKY_AURORA_TOP_LABEL_Y = 153;
    private static final int SKY_AURORA_TOP_SLIDER_Y = 166;
    private static final int SKY_AURORA_BRIGHT_LABEL_Y = 181;
    private static final int SKY_AURORA_BRIGHT_SLIDER_Y = 193;

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
        int colHeight = 510; // Увеличили под все блоки

        // --- КОЛОНКА COMBAT ---
        graphics.fill(startX, startY, startX + colWidth, startY + colHeight, bgColor);
        graphics.text(this.font, "Combat", startX + 10, startY + 10, textColor, false);

        int combatY = startY + 30;
        graphics.text(this.font, "KillAura", startX + 10, combatY + 5, textColor, false);
        drawToggle(graphics, startX + 90, combatY + 5, 20, 10, BiwerVisualsClient.CONFIG.killAuraEnabled);

        if (!BiwerVisualsClient.CONFIG.killAuraCollapsed) {
            graphics.fill(startX + 5, combatY + 15, startX + colWidth - 5, combatY + 302, 0xAA000000);

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

            graphics.text(this.font, "Наводка:", startX + 10, combatY + 212, 0xFFAAAAAA, false);

            graphics.text(this.font, "Плавность: " + (int) BiwerVisualsClient.CONFIG.killAuraSmooth + "%", startX + 10, combatY + 225, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, combatY + 237, 100, 4, BiwerVisualsClient.CONFIG.killAuraSmooth, 5, 100, "");

            graphics.text(this.font, "Скорость: " + (int) BiwerVisualsClient.CONFIG.killAuraSpeed + "°/т", startX + 10, combatY + 250, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, combatY + 262, 100, 4, BiwerVisualsClient.CONFIG.killAuraSpeed, 1, 30, "");

            graphics.text(this.font, "Угол: " + (int) BiwerVisualsClient.CONFIG.killAuraFov + "°", startX + 10, combatY + 275, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, combatY + 287, 100, 4, BiwerVisualsClient.CONFIG.killAuraFov, 30, 180, "");
        }

        // --- AURA (порт) ---
        int auraY = combatY + (BiwerVisualsClient.CONFIG.killAuraCollapsed ? 30 : 310);
        graphics.text(this.font, "Aura", startX + 10, auraY + 5, textColor, false);
        drawToggle(graphics, startX + 90, auraY + 5, 20, 10, BiwerVisualsClient.CONFIG.auraEnabled);

        if (!BiwerVisualsClient.CONFIG.auraCollapsed) {
            graphics.fill(startX + 5, auraY + 15, startX + colWidth - 5, auraY + 250, 0xAA000000);

            graphics.text(this.font, "Дистанция: " + String.format("%.1f", BiwerVisualsClient.CONFIG.auraRange), startX + 10, auraY + 20, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, auraY + 32, 100, 4, BiwerVisualsClient.CONFIG.auraRange, 0.1f, 6.0f, "");

            graphics.text(this.font, "Доп.: " + String.format("%.1f", BiwerVisualsClient.CONFIG.auraExtraRange), startX + 10, auraY + 45, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, auraY + 57, 100, 4, BiwerVisualsClient.CONFIG.auraExtraRange, 0.0f, 3.0f, "");

            graphics.text(this.font, "Наведение:", startX + 10, auraY + 70, 0xFFAAAAAA, false);
            drawButton(graphics, "ФанТайм", startX + 10, auraY + 82, 42, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.auraAimMode == 0, true);
            drawButton(graphics, "ФОВ", startX + 55, auraY + 82, 27, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.auraAimMode == 1, true);
            drawButton(graphics, "Легит", startX + 85, auraY + 82, 30, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.auraAimMode == 2, true);

            graphics.text(this.font, "Приоритет:", startX + 10, auraY + 98, 0xFFAAAAAA, false);
            drawButton(graphics, "Прицел", startX + 10, auraY + 110, 42, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.auraPriority == 0, true);
            drawButton(graphics, "Дист", startX + 56, auraY + 110, 28, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.auraPriority == 1, true);
            drawButton(graphics, "ХП", startX + 88, auraY + 110, 26, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.auraPriority == 2, true);

            drawToggle(graphics, startX + 10, auraY + 126, 15, 8, BiwerVisualsClient.CONFIG.auraPlayers);
            graphics.text(this.font, "Игроки", startX + 30, auraY + 126, 0xFFAAAAAA, false);
            drawToggle(graphics, startX + 70, auraY + 126, 15, 8, BiwerVisualsClient.CONFIG.auraMobs);
            graphics.text(this.font, "Мобы", startX + 90, auraY + 126, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, auraY + 139, 15, 8, BiwerVisualsClient.CONFIG.auraAnimals);
            graphics.text(this.font, "Животные", startX + 30, auraY + 139, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, auraY + 152, 15, 8, BiwerVisualsClient.CONFIG.auraOnlyCriticals);
            graphics.text(this.font, "Криты", startX + 30, auraY + 152, 0xFFAAAAAA, false);
            drawToggle(graphics, startX + 66, auraY + 152, 15, 8, BiwerVisualsClient.CONFIG.auraAdaptiveHits);
            graphics.text(this.font, "Адапт", startX + 86, auraY + 152, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, auraY + 165, 15, 8, BiwerVisualsClient.CONFIG.auraShieldBreaker);
            graphics.text(this.font, "Щит", startX + 30, auraY + 165, 0xFFAAAAAA, false);
            drawToggle(graphics, startX + 70, auraY + 165, 15, 8, BiwerVisualsClient.CONFIG.auraSmartSprint);
            graphics.text(this.font, "Бег", startX + 90, auraY + 165, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, auraY + 178, 15, 8, BiwerVisualsClient.CONFIG.auraDontHitItemUse);
            graphics.text(this.font, "Предмет", startX + 30, auraY + 178, 0xFFAAAAAA, false);
            drawToggle(graphics, startX + 70, auraY + 178, 15, 8, BiwerVisualsClient.CONFIG.auraDontHitContainer);
            graphics.text(this.font, "Инв.", startX + 90, auraY + 178, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, auraY + 191, 15, 8, BiwerVisualsClient.CONFIG.auraDontHitThroughWalls);
            graphics.text(this.font, "Стены", startX + 30, auraY + 191, 0xFFAAAAAA, false);

            graphics.text(this.font, "Скорость: " + (int) BiwerVisualsClient.CONFIG.auraSpeed + "°/т", startX + 10, auraY + 204, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, auraY + 216, 100, 4, BiwerVisualsClient.CONFIG.auraSpeed, 1.0f, 30.0f, "");

            graphics.text(this.font, "Плавность: " + (int) BiwerVisualsClient.CONFIG.auraSmooth + "%", startX + 10, auraY + 229, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, auraY + 241, 100, 4, BiwerVisualsClient.CONFIG.auraSmooth, 5.0f, 100.0f, "");
        }

        // --- TRIGGERBOT ---
        int triggerY = auraY + (BiwerVisualsClient.CONFIG.auraCollapsed ? 30 : 260);
        graphics.text(this.font, "TriggerBot", startX + 10, triggerY + 5, textColor, false);
        drawToggle(graphics, startX + 90, triggerY + 5, 20, 10, BiwerVisualsClient.CONFIG.triggerBotEnabled);

        if (!BiwerVisualsClient.CONFIG.triggerBotCollapsed) {
            graphics.fill(startX + 5, triggerY + 15, startX + colWidth - 5, triggerY + 96, 0xAA000000);

            graphics.text(this.font, "Дистанция: " + String.format("%.1f", BiwerVisualsClient.CONFIG.triggerBotRange), startX + 10, triggerY + 20, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, triggerY + 32, 100, 4, BiwerVisualsClient.CONFIG.triggerBotRange, 0.5f, 5.0f, "");

            drawToggle(graphics, startX + 10, triggerY + 45, 15, 8, BiwerVisualsClient.CONFIG.triggerBotWeaponOnly);
            graphics.text(this.font, "Только оружие", startX + 30, triggerY + 45, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, triggerY + 58, 15, 8, BiwerVisualsClient.CONFIG.triggerBotOnlyCriticals);
            graphics.text(this.font, "Криты", startX + 30, triggerY + 58, 0xFFAAAAAA, false);
            drawToggle(graphics, startX + 70, triggerY + 58, 15, 8, BiwerVisualsClient.CONFIG.triggerBotDontHitThroughWalls);
            graphics.text(this.font, "Стены", startX + 90, triggerY + 58, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, triggerY + 71, 15, 8, BiwerVisualsClient.CONFIG.triggerBotPlayers);
            graphics.text(this.font, "Игроки", startX + 30, triggerY + 71, 0xFFAAAAAA, false);
            drawToggle(graphics, startX + 70, triggerY + 71, 15, 8, BiwerVisualsClient.CONFIG.triggerBotMobs);
            graphics.text(this.font, "Мобы", startX + 90, triggerY + 71, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, triggerY + 84, 15, 8, BiwerVisualsClient.CONFIG.triggerBotAnimals);
            graphics.text(this.font, "Животные", startX + 30, triggerY + 84, 0xFFAAAAAA, false);
        }

        // --- AIMASSIST ---
        int assistY = triggerY + (BiwerVisualsClient.CONFIG.triggerBotCollapsed ? 30 : 106);
        graphics.text(this.font, "AimAssistant", startX + 10, assistY + 5, textColor, false);
        drawToggle(graphics, startX + 90, assistY + 5, 20, 10, BiwerVisualsClient.CONFIG.aimAssistEnabled);

        if (!BiwerVisualsClient.CONFIG.aimAssistCollapsed) {
            graphics.fill(startX + 5, assistY + 15, startX + colWidth - 5, assistY + 96, 0xAA000000);

            graphics.text(this.font, "Сила: " + (int) BiwerVisualsClient.CONFIG.aimAssistStrength + "%", startX + 10, assistY + 20, 0xFFAAAAAA, false);
            drawSlider(graphics, startX + 10, assistY + 32, 100, 4, BiwerVisualsClient.CONFIG.aimAssistStrength, 5.0f, 100.0f, "");

            drawToggle(graphics, startX + 10, assistY + 45, 15, 8, BiwerVisualsClient.CONFIG.aimAssistWeaponOnly);
            graphics.text(this.font, "Только оружие", startX + 30, assistY + 45, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, assistY + 58, 15, 8, BiwerVisualsClient.CONFIG.aimAssistThroughWalls);
            graphics.text(this.font, "Через стены", startX + 30, assistY + 58, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, assistY + 71, 15, 8, BiwerVisualsClient.CONFIG.aimAssistPlayers);
            graphics.text(this.font, "Игроки", startX + 30, assistY + 71, 0xFFAAAAAA, false);
            drawToggle(graphics, startX + 70, assistY + 71, 15, 8, BiwerVisualsClient.CONFIG.aimAssistMobs);
            graphics.text(this.font, "Мобы", startX + 90, assistY + 71, 0xFFAAAAAA, false);

            drawToggle(graphics, startX + 10, assistY + 84, 15, 8, BiwerVisualsClient.CONFIG.aimAssistAnimals);
            graphics.text(this.font, "Животные", startX + 30, assistY + 84, 0xFFAAAAAA, false);
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
            graphics.fill(x3 + 5, vmY + 15, x3 + colWidth - 5, vmY + 122, 0xAA000000);
            if (BiwerVisualsClient.CONFIG.viewmodelEnabled) {
                graphics.text(this.font, "Основная рука", x3 + 10, vmY + 20, 0xFFAAAAAA, false);
                drawSlider(graphics, x3 + 10, vmY + 32, 100, 4, BiwerVisualsClient.CONFIG.vmMainX, -2, 2, "");
                drawSlider(graphics, x3 + 10, vmY + 42, 100, 4, BiwerVisualsClient.CONFIG.vmMainY, -2, 2, "");
                drawSlider(graphics, x3 + 10, vmY + 52, 100, 4, BiwerVisualsClient.CONFIG.vmMainZ, -2, 2, "");

                graphics.text(this.font, "Вторая рука", x3 + 10, vmY + 65, 0xFFAAAAAA, false);
                drawSlider(graphics, x3 + 10, vmY + 77, 100, 4, BiwerVisualsClient.CONFIG.vmOffX, -2, 2, "");
                drawSlider(graphics, x3 + 10, vmY + 87, 100, 4, BiwerVisualsClient.CONFIG.vmOffY, -2, 2, "");
                drawSlider(graphics, x3 + 10, vmY + 97, 100, 4, BiwerVisualsClient.CONFIG.vmOffZ, -2, 2, "");

                drawButton(graphics, "Сбросить", x3 + 10, vmY + 108, 100, 12, mouseX, mouseY, false, true);
            }
        }

        // Entity ESP
        int espY = vmY + (BiwerVisualsClient.CONFIG.viewmodelCollapsed ? 30 : 135);
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

        // JumpCircles
        int jcY = tagsY + (BiwerVisualsClient.CONFIG.tagsCollapsed ? 30 : 80);
        graphics.text(this.font, "JumpCircles", x3 + 10, jcY + 5, textColor, false);
        drawToggle(graphics, x3 + 90, jcY + 5, 20, 10, BiwerVisualsClient.CONFIG.jumpCirclesEnabled);

        // CustomSky
        int skyY = jcY + 30;
        graphics.text(this.font, "CustomSky", x3 + 10, skyY + 5, textColor, false);
        drawToggle(graphics, x3 + 90, skyY + 5, 20, 10, BiwerVisualsClient.CONFIG.customSkyEnabled);

        if (!BiwerVisualsClient.CONFIG.customSkyCollapsed) {
            int skyPanelBottom = BiwerVisualsClient.CONFIG.auroraEnabled
                    ? skyY + SKY_AURORA_PANEL_BOTTOM
                    : skyY + SKY_PANEL_BOTTOM;
            graphics.fill(x3 + 5, skyY + 15, x3 + colWidth - 5, skyPanelBottom, 0xAA000000);

            drawButton(graphics, "Закат", x3 + 10, skyY + 20, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.skyMode == 0, true);
            drawButton(graphics, "Аврора", x3 + 65, skyY + 20, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.skyMode == 1, true);
            drawButton(graphics, "Космос", x3 + 10, skyY + 34, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.skyMode == 2, true);
            drawButton(graphics, "Неон", x3 + 65, skyY + 34, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.skyMode == 3, true);
            drawButton(graphics, "Перелив", x3 + 10, skyY + 48, 105, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.skyMode == 4, true);

            graphics.text(this.font, "Звёзды: " + (int) BiwerVisualsClient.CONFIG.skyStars + "%", x3 + 10, skyY + 64, 0xFFAAAAAA, false);
            drawSlider(graphics, x3 + 10, skyY + 76, 100, 4, BiwerVisualsClient.CONFIG.skyStars, 0, 100, "");

            graphics.text(this.font, "Скорость: " + (int) BiwerVisualsClient.CONFIG.skySpeed + "%", x3 + 10, skyY + 89, 0xFFAAAAAA, false);
            drawSlider(graphics, x3 + 10, skyY + 101, 100, 4, BiwerVisualsClient.CONFIG.skySpeed, 0, 100, "");

            // Северное сияние
            drawToggle(graphics, x3 + 10, skyY + SKY_AURORA_TOGGLE_Y, 15, 8, BiwerVisualsClient.CONFIG.auroraEnabled);
            graphics.text(this.font, "Северное сияние", x3 + 30, skyY + SKY_AURORA_TOGGLE_Y,
                    BiwerVisualsClient.CONFIG.auroraEnabled ? 0xFFFFFFFF : 0xFFAAAAAA, false);

            if (BiwerVisualsClient.CONFIG.auroraEnabled) {
                drawAuroraColorRow(graphics, "Низ", x3 + 10,
                        skyY + SKY_AURORA_BOTTOM_LABEL_Y, skyY + SKY_AURORA_BOTTOM_SLIDER_Y,
                        BiwerVisualsClient.CONFIG.auroraColorBottom);
                drawAuroraColorRow(graphics, "Верх", x3 + 10,
                        skyY + SKY_AURORA_TOP_LABEL_Y, skyY + SKY_AURORA_TOP_SLIDER_Y,
                        BiwerVisualsClient.CONFIG.auroraColorTop);

                graphics.text(this.font, "Яркость: " + (int) BiwerVisualsClient.CONFIG.auroraBrightness + "%",
                        x3 + 10, skyY + SKY_AURORA_BRIGHT_LABEL_Y, 0xFFAAAAAA, false);
                drawSlider(graphics, x3 + 10, skyY + SKY_AURORA_BRIGHT_SLIDER_Y, 100, 4,
                        BiwerVisualsClient.CONFIG.auroraBrightness, 0, 100, "");
            }
        }

        // --- Fly (Movement) ---
        int moveX = x2;
        int flyY = startY + 30;
        graphics.text(this.font, "Fly", moveX + 10, flyY + 5, textColor, false);
        drawToggle(graphics, moveX + 90, flyY + 5, 20, 10, BiwerVisualsClient.CONFIG.flyEnabled);

        // Блок Sprint
        int sprintY = flyY + 30; // Сдвигаем ниже Fly
        graphics.text(this.font, "Sprint", x2 + 10, sprintY + 5, textColor, false);
        drawToggle(graphics, x2 + 90, sprintY + 5, 20, 10, BiwerVisualsClient.CONFIG.sprintEnabled);

        // Блок Speed
        int speedY = sprintY + 30;
        graphics.text(this.font, "Speed", x2 + 10, speedY + 5, textColor, false);
        drawToggle(graphics, x2 + 90, speedY + 5, 20, 10, BiwerVisualsClient.CONFIG.speedEnabled);

        if (!BiwerVisualsClient.CONFIG.speedCollapsed) {
            graphics.fill(x2 + 5, speedY + 15, x2 + colWidth - 5, speedY + 86, 0xAA000000);
            graphics.text(this.font, "Буст: +" + (int) BiwerVisualsClient.CONFIG.speedPercent + "%", x2 + 10, speedY + 20, 0xFFAAAAAA, false);
            drawSlider(graphics, x2 + 10, speedY + 32, 100, 4, BiwerVisualsClient.CONFIG.speedPercent, 5, 60, "");
            drawToggle(graphics, x2 + 10, speedY + 45, 15, 8, BiwerVisualsClient.CONFIG.speedOnlySprint);
            graphics.text(this.font, "Только спринт", x2 + 30, speedY + 45, 0xFFAAAAAA, false);
            drawToggle(graphics, x2 + 10, speedY + 58, 15, 8, BiwerVisualsClient.CONFIG.speedSmooth);
            graphics.text(this.font, "Плавный разгон", x2 + 30, speedY + 58, 0xFFAAAAAA, false);
            drawToggle(graphics, x2 + 10, speedY + 71, 15, 8, BiwerVisualsClient.CONFIG.speedSkipIfPotion);
            graphics.text(this.font, "Без зелья", x2 + 30, speedY + 71, 0xFFAAAAAA, false);
        }

        // Блок BunnyHop
        int bhopY = speedY + (BiwerVisualsClient.CONFIG.speedCollapsed ? 30 : 100);
        graphics.text(this.font, "BunnyHop", x2 + 10, bhopY + 5, textColor, false);
        drawToggle(graphics, x2 + 90, bhopY + 5, 20, 10, BiwerVisualsClient.CONFIG.bunnyHopEnabled);

        if (!BiwerVisualsClient.CONFIG.bunnyHopCollapsed) {
            graphics.fill(x2 + 5, bhopY + 15, x2 + colWidth - 5, bhopY + 72, 0xAA000000);
            drawToggle(graphics, x2 + 10, bhopY + 20, 15, 8, BiwerVisualsClient.CONFIG.bhopAutoJump);
            graphics.text(this.font, "Авто-прыжок", x2 + 30, bhopY + 20, 0xFFAAAAAA, false);
            drawToggle(graphics, x2 + 10, bhopY + 33, 15, 8, BiwerVisualsClient.CONFIG.bhopOnlyWhileMoving);
            graphics.text(this.font, "В движении", x2 + 30, bhopY + 33, 0xFFAAAAAA, false);
            graphics.text(this.font, "Задержка: " + BiwerVisualsClient.CONFIG.bhopJumpDelay + " т.", x2 + 10, bhopY + 46, 0xFFAAAAAA, false);
            drawSlider(graphics, x2 + 10, bhopY + 58, 100, 4, BiwerVisualsClient.CONFIG.bhopJumpDelay, 0, 20, "");
        }

        // Блок TargetStrafe
        int strafeY = bhopY + (BiwerVisualsClient.CONFIG.bunnyHopCollapsed ? 30 : 90);
        graphics.text(this.font, "TargetStrafe", x2 + 10, strafeY + 5, textColor, false);
        drawToggle(graphics, x2 + 90, strafeY + 5, 20, 10, BiwerVisualsClient.CONFIG.targetStrafeEnabled);

        if (!BiwerVisualsClient.CONFIG.targetStrafeCollapsed) {
            graphics.fill(x2 + 5, strafeY + 15, x2 + colWidth - 5, strafeY + 60, 0xAA000000);
            drawToggle(graphics, x2 + 10, strafeY + 20, 15, 8, BiwerVisualsClient.CONFIG.strafeAutoJump);
            graphics.text(this.font, "Авто-прыжок", x2 + 30, strafeY + 20, 0xFFAAAAAA, false);
            graphics.text(this.font, "Дистанция: " + String.format("%.1f", BiwerVisualsClient.CONFIG.strafeRange), x2 + 10, strafeY + 33, 0xFFAAAAAA, false);
            drawSlider(graphics, x2 + 10, strafeY + 45, 100, 4, BiwerVisualsClient.CONFIG.strafeRange, 0.5f, 4.0f, "");
        }

        // --- PLAYER и MISC ---
        int x4 = x3 + colWidth + padding;
        graphics.fill(x4, startY, x4 + colWidth, startY + colHeight, bgColor);
        graphics.text(this.font, "Player", x4 + 10, startY + 10, textColor, false);

        // AutoTotem (колонка Player)
        int autoTotemY = startY + 30;
        graphics.text(this.font, "AutoTotem", x4 + 10, autoTotemY + 5, textColor, false);
        drawToggle(graphics, x4 + 90, autoTotemY + 5, 20, 10, BiwerVisualsClient.CONFIG.autoTotemEnabled);

        if (!BiwerVisualsClient.CONFIG.autoTotemCollapsed) {
            graphics.fill(x4 + 5, autoTotemY + 15, x4 + colWidth - 5, autoTotemY + 50, 0xAA000000);
            graphics.text(this.font, "Задержка: " + BiwerVisualsClient.CONFIG.autoTotemDelay + " т.", x4 + 10, autoTotemY + 20, 0xFFAAAAAA, false);
            drawSlider(graphics, x4 + 10, autoTotemY + 35, 100, 4, BiwerVisualsClient.CONFIG.autoTotemDelay, 1, 20, "");
        }

        // --- VELOCITY ---
        int velocityY = autoTotemY + (BiwerVisualsClient.CONFIG.autoTotemCollapsed ? 30 : 60);
        graphics.text(this.font, "Velocity", x4 + 10, velocityY + 5, textColor, false);
        drawToggle(graphics, x4 + 90, velocityY + 5, 20, 10, BiwerVisualsClient.CONFIG.velocityEnabled);

        if (!BiwerVisualsClient.CONFIG.velocityCollapsed) {
            graphics.fill(x4 + 5, velocityY + 15, x4 + colWidth - 5, velocityY + 72, 0xAA000000);

            graphics.text(this.font, "Режим:", x4 + 10, velocityY + 20, 0xFFAAAAAA, false);
            drawButton(graphics, "Обычный", x4 + 10, velocityY + 32, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.velocityMode == 0, true);
            drawButton(graphics, "Легит", x4 + 64, velocityY + 32, 46, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.velocityMode == 1, true);

            graphics.text(this.font, "Сила: " + (int) BiwerVisualsClient.CONFIG.velocityPercent + "%", x4 + 10, velocityY + 46, 0xFFAAAAAA, false);
            drawSlider(graphics, x4 + 10, velocityY + 58, 100, 4, BiwerVisualsClient.CONFIG.velocityPercent, 0.0f, 100.0f, "");
        }

        int x5 = x4 + colWidth + padding;
        graphics.fill(x5, startY, x5 + colWidth, startY + colHeight, bgColor);
        graphics.text(this.font, "Misc", x5 + 10, startY + 10, textColor, false);

        // HUD
        int hudY = startY + 30;
        graphics.text(this.font, "HUD", x5 + 10, hudY + 5, textColor, false);
        drawToggle(graphics, x5 + 90, hudY + 5, 20, 10, BiwerVisualsClient.CONFIG.hudEnabled);
        drawButton(graphics, "Редактор HUD", x5 + 10, hudY + 24, 100, 14, mouseX, mouseY, false, true);

        // FullBright (Misc)
        int fullBrightY = hudY + 50;
        graphics.text(this.font, "FullBright", x5 + 10, fullBrightY + 5, textColor, false);
        drawToggle(graphics, x5 + 90, fullBrightY + 5, 20, 10, BiwerVisualsClient.CONFIG.fullBrightEnabled);

        if (!BiwerVisualsClient.CONFIG.fullBrightCollapsed) {
            graphics.fill(x5 + 5, fullBrightY + 15, x5 + colWidth - 5, fullBrightY + 70, 0xAA000000);

            graphics.text(this.font, "Режим:", x5 + 10, fullBrightY + 20, 0xFFAAAAAA, false);
            drawButton(graphics, "Гамма", x5 + 10, fullBrightY + 32, 50, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.fullBrightMode == 0, true);
            drawButton(graphics, "Ночное", x5 + 64, fullBrightY + 32, 46, 12, mouseX, mouseY, BiwerVisualsClient.CONFIG.fullBrightMode == 1, true);

            graphics.text(this.font, "Гамма: " + String.format("%.1f", BiwerVisualsClient.CONFIG.fullBrightGamma), x5 + 10, fullBrightY + 46, 0xFFAAAAAA, false);
            drawSlider(graphics, x5 + 10, fullBrightY + 58, 100, 4, BiwerVisualsClient.CONFIG.fullBrightGamma, 1.0f, 8.0f, "");
        }

        // Crosshair (Misc)
        int crosshairY = fullBrightY + (BiwerVisualsClient.CONFIG.fullBrightCollapsed ? 30 : 80);
        graphics.text(this.font, "Crosshair", x5 + 10, crosshairY + 5, textColor, false);
        drawToggle(graphics, x5 + 90, crosshairY + 5, 20, 10, BiwerVisualsClient.CONFIG.crosshairEnabled);

        if (!BiwerVisualsClient.CONFIG.crosshairCollapsed) {
            graphics.fill(x5 + 5, crosshairY + 15, x5 + colWidth - 5, crosshairY + 96, 0xAA000000);

            graphics.text(this.font, "Зазор: " + String.format("%.1f", BiwerVisualsClient.CONFIG.crosshairGap), x5 + 10, crosshairY + 20, 0xFFAAAAAA, false);
            drawSlider(graphics, x5 + 10, crosshairY + 32, 100, 4, BiwerVisualsClient.CONFIG.crosshairGap, 0.0f, 6.0f, "");

            graphics.text(this.font, "Длина: " + String.format("%.1f", BiwerVisualsClient.CONFIG.crosshairLength), x5 + 10, crosshairY + 45, 0xFFAAAAAA, false);
            drawSlider(graphics, x5 + 10, crosshairY + 57, 100, 4, BiwerVisualsClient.CONFIG.crosshairLength, 2.0f, 5.0f, "");

            drawToggle(graphics, x5 + 10, crosshairY + 70, 15, 8, BiwerVisualsClient.CONFIG.crosshairOutline);
            graphics.text(this.font, "Контур", x5 + 30, crosshairY + 70, 0xFFAAAAAA, false);
            drawToggle(graphics, x5 + 70, crosshairY + 70, 15, 8, BiwerVisualsClient.CONFIG.crosshairCenterDot);
            graphics.text(this.font, "Точка", x5 + 90, crosshairY + 70, 0xFFAAAAAA, false);

            drawToggle(graphics, x5 + 10, crosshairY + 83, 15, 8, BiwerVisualsClient.CONFIG.crosshairAdaptive);
            graphics.text(this.font, "Адаптив", x5 + 30, crosshairY + 83, 0xFFAAAAAA, false);
        }

        // SwingAnimation (Misc)
        int swingY = crosshairY + (BiwerVisualsClient.CONFIG.crosshairCollapsed ? 30 : 106);
        graphics.text(this.font, "SwingAnimation", x5 + 10, swingY + 5, textColor, false);
        drawToggle(graphics, x5 + 90, swingY + 5, 20, 10, BiwerVisualsClient.CONFIG.swingAnimationEnabled);

        if (!BiwerVisualsClient.CONFIG.swingAnimationCollapsed) {
            graphics.fill(x5 + 5, swingY + 15, x5 + colWidth - 5, swingY + 120, 0xAA000000);

            graphics.text(this.font, "Мод:", x5 + 10, swingY + 20, 0xFFAAAAAA, false);
            for (int i = 0; i < 5; i++) {
                drawButton(graphics, "М" + (i + 1), x5 + 10 + i * 20, swingY + 32, 18, 12, mouseX, mouseY,
                        BiwerVisualsClient.CONFIG.swingAnimationMode == i, true);
            }

            graphics.text(this.font, "Угол: " + (int) BiwerVisualsClient.CONFIG.swingAngle, x5 + 10, swingY + 46, 0xFFAAAAAA, false);
            drawSlider(graphics, x5 + 10, swingY + 58, 100, 4, BiwerVisualsClient.CONFIG.swingAngle, 0.0f, 360.0f, "");

            graphics.text(this.font, "Наклон: " + (int) BiwerVisualsClient.CONFIG.swingTipTilt, x5 + 10, swingY + 71, 0xFFAAAAAA, false);
            drawSlider(graphics, x5 + 10, swingY + 83, 100, 4, BiwerVisualsClient.CONFIG.swingTipTilt, -90.0f, 90.0f, "");

            graphics.text(this.font, "Сила: " + (int) BiwerVisualsClient.CONFIG.swingPower, x5 + 10, swingY + 96, 0xFFAAAAAA, false);
            drawSlider(graphics, x5 + 10, swingY + 108, 100, 4, BiwerVisualsClient.CONFIG.swingPower, 1.0f, 10.0f, "");
        }

        // SeeInvisibles (Misc)
        int seeY = swingY + (BiwerVisualsClient.CONFIG.swingAnimationCollapsed ? 30 : 130);
        graphics.text(this.font, "SeeInvisibles", x5 + 10, seeY + 5, textColor, false);
        drawToggle(graphics, x5 + 90, seeY + 5, 20, 10, BiwerVisualsClient.CONFIG.seeInvisiblesEnabled);

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

    private void drawColorSlider(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int value, int channelColor) {
        graphics.fill(x, y, x + width, y + height, 0xFF333333);
        int filled = Math.round(Math.max(0, Math.min(255, value)) / 255.0f * width);
        graphics.fill(x, y, x + filled, y + height, channelColor);
        graphics.fill(x + filled - 1, y - 2, x + filled + 1, y + height + 2, 0xFFFFFFFF);
    }

    private void drawAuroraColorRow(GuiGraphicsExtractor graphics, String title, int x, int labelY, int sliderY, int color) {
        graphics.text(this.font, title, x, labelY, 0xFFAAAAAA, false);
        graphics.fill(x + 26, labelY - 1, x + 46, labelY + 7, 0xFF000000 | (color & 0xFFFFFF));
        graphics.text(this.font, hexColor(color), x + 50, labelY, 0xFFAAAAAA, false);

        drawColorSlider(graphics, x, sliderY, 30, 4, (color >> 16) & 0xFF, 0xFFFF5555);
        drawColorSlider(graphics, x + 35, sliderY, 30, 4, (color >> 8) & 0xFF, 0xFF55FF55);
        drawColorSlider(graphics, x + 70, sliderY, 30, 4, color & 0xFF, 0xFF5599FF);
    }

    private static String hexColor(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    private static int channelValue(double mouseX, int sliderX, int width) {
        double k = (mouseX - sliderX) / (double) width;
        return (int) Math.round(Math.max(0.0, Math.min(1.0, k)) * 255.0);
    }

    private static int withChannel(int rgb, int shift, int value) {
        int mask = 0xFF << shift;
        return (rgb & ~mask) | ((value & 0xFF) << shift);
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

        // --- Координаты для всех колонок ---
        int x1 = startX;
        int combatY = startY + 30;

        int x2 = startX + colWidth + padding; // Movement
        int flyY = startY + 30; // Fly
        int sprintY = flyY + 30; // Sprint (сдвиг на 30 пикселей ниже Fly)

        int x3 = startX + (colWidth + padding) * 2; // Render
        int ambY = startY + 30;
        int vmY = ambY + (BiwerVisualsClient.CONFIG.ambienceCollapsed ? 30 : 150);
        int espY = vmY + (BiwerVisualsClient.CONFIG.viewmodelCollapsed ? 30 : 135);
        int tagsY = espY + (BiwerVisualsClient.CONFIG.espCollapsed ? 30 : 80);
        int x4 = startX + (colWidth + padding) * 3; // Player
        int autoTotemY = startY + 30;

        if (button == 1) { // Сворачивание
            if (isHovered(mouseX, mouseY, x1 + 5, combatY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.killAuraCollapsed = !BiwerVisualsClient.CONFIG.killAuraCollapsed; return true; }
            int auraHitY = combatY + (BiwerVisualsClient.CONFIG.killAuraCollapsed ? 30 : 310);
            if (isHovered(mouseX, mouseY, x1 + 5, auraHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.auraCollapsed = !BiwerVisualsClient.CONFIG.auraCollapsed; return true; }
            int triggerHitY = auraHitY + (BiwerVisualsClient.CONFIG.auraCollapsed ? 30 : 260);
            if (isHovered(mouseX, mouseY, x1 + 5, triggerHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.triggerBotCollapsed = !BiwerVisualsClient.CONFIG.triggerBotCollapsed; return true; }
            int assistHitY = triggerHitY + (BiwerVisualsClient.CONFIG.triggerBotCollapsed ? 30 : 106);
            if (isHovered(mouseX, mouseY, x1 + 5, assistHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.aimAssistCollapsed = !BiwerVisualsClient.CONFIG.aimAssistCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x3 + 5, ambY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.ambienceCollapsed = !BiwerVisualsClient.CONFIG.ambienceCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x3 + 5, vmY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.viewmodelCollapsed = !BiwerVisualsClient.CONFIG.viewmodelCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x3 + 5, espY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.espCollapsed = !BiwerVisualsClient.CONFIG.espCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x3 + 5, tagsY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.tagsCollapsed = !BiwerVisualsClient.CONFIG.tagsCollapsed; return true; }
            int speedHitY = flyY + 60;
            int bhopHitY = speedHitY + (BiwerVisualsClient.CONFIG.speedCollapsed ? 30 : 100);
            int strafeHitY = bhopHitY + (BiwerVisualsClient.CONFIG.bunnyHopCollapsed ? 30 : 90);
            if (isHovered(mouseX, mouseY, x2 + 5, speedHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.speedCollapsed = !BiwerVisualsClient.CONFIG.speedCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x2 + 5, bhopHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.bunnyHopCollapsed = !BiwerVisualsClient.CONFIG.bunnyHopCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x2 + 5, strafeHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.targetStrafeCollapsed = !BiwerVisualsClient.CONFIG.targetStrafeCollapsed; return true; }
            int skyHitY = tagsY + (BiwerVisualsClient.CONFIG.tagsCollapsed ? 30 : 80) + 30;
            if (isHovered(mouseX, mouseY, x3 + 5, skyHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.customSkyCollapsed = !BiwerVisualsClient.CONFIG.customSkyCollapsed; return true; }
            if (isHovered(mouseX, mouseY, x4 + 5, autoTotemY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.autoTotemCollapsed = !BiwerVisualsClient.CONFIG.autoTotemCollapsed; return true; }
            int velocityHitY = autoTotemY + (BiwerVisualsClient.CONFIG.autoTotemCollapsed ? 30 : 60);
            if (isHovered(mouseX, mouseY, x4 + 5, velocityHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.velocityCollapsed = !BiwerVisualsClient.CONFIG.velocityCollapsed; return true; }
            int miscHitX = startX + (colWidth + padding) * 4;
            int miscHudY = startY + 30;
            int fullBrightHitY = miscHudY + 50;
            if (isHovered(mouseX, mouseY, miscHitX + 5, fullBrightHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.fullBrightCollapsed = !BiwerVisualsClient.CONFIG.fullBrightCollapsed; return true; }
            int crosshairHitY = fullBrightHitY + (BiwerVisualsClient.CONFIG.fullBrightCollapsed ? 30 : 80);
            if (isHovered(mouseX, mouseY, miscHitX + 5, crosshairHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.crosshairCollapsed = !BiwerVisualsClient.CONFIG.crosshairCollapsed; return true; }
            int swingHitY = crosshairHitY + (BiwerVisualsClient.CONFIG.crosshairCollapsed ? 30 : 106);
            if (isHovered(mouseX, mouseY, miscHitX + 5, swingHitY, colWidth - 10, 15)) { BiwerVisualsClient.CONFIG.swingAnimationCollapsed = !BiwerVisualsClient.CONFIG.swingAnimationCollapsed; return true; }
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
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 237, 100, 4)) { BiwerVisualsClient.CONFIG.killAuraSmooth = (float) (5 + (mouseX - (x1 + 10)) / 100.0 * 95); return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 262, 100, 4)) { BiwerVisualsClient.CONFIG.killAuraSpeed = (float) (1 + (mouseX - (x1 + 10)) / 100.0 * 29); return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, combatY + 287, 100, 4)) { BiwerVisualsClient.CONFIG.killAuraFov = (float) (30 + (mouseX - (x1 + 10)) / 100.0 * 150); return true; }
            }

            // Aura (порт)
            int auraY = combatY + (BiwerVisualsClient.CONFIG.killAuraCollapsed ? 30 : 310);
            if (isHovered(mouseX, mouseY, x1 + 90, auraY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.auraEnabled = !BiwerVisualsClient.CONFIG.auraEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.auraCollapsed) {
                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 32, 100, 4)) { BiwerVisualsClient.CONFIG.auraRange = (float) (0.1 + (mouseX - (x1 + 10)) / 100.0 * 5.9); return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 57, 100, 4)) { BiwerVisualsClient.CONFIG.auraExtraRange = (float) ((mouseX - (x1 + 10)) / 100.0 * 3.0); return true; }

                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 82, 42, 12)) { BiwerVisualsClient.CONFIG.auraAimMode = 0; return true; }
                if (isHovered(mouseX, mouseY, x1 + 55, auraY + 82, 27, 12)) { BiwerVisualsClient.CONFIG.auraAimMode = 1; return true; }
                if (isHovered(mouseX, mouseY, x1 + 85, auraY + 82, 30, 12)) { BiwerVisualsClient.CONFIG.auraAimMode = 2; return true; }

                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 110, 42, 12)) { BiwerVisualsClient.CONFIG.auraPriority = 0; return true; }
                if (isHovered(mouseX, mouseY, x1 + 56, auraY + 110, 28, 12)) { BiwerVisualsClient.CONFIG.auraPriority = 1; return true; }
                if (isHovered(mouseX, mouseY, x1 + 88, auraY + 110, 26, 12)) { BiwerVisualsClient.CONFIG.auraPriority = 2; return true; }

                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 126, 15, 8)) { BiwerVisualsClient.CONFIG.auraPlayers = !BiwerVisualsClient.CONFIG.auraPlayers; return true; }
                if (isHovered(mouseX, mouseY, x1 + 70, auraY + 126, 15, 8)) { BiwerVisualsClient.CONFIG.auraMobs = !BiwerVisualsClient.CONFIG.auraMobs; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 139, 15, 8)) { BiwerVisualsClient.CONFIG.auraAnimals = !BiwerVisualsClient.CONFIG.auraAnimals; return true; }

                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 152, 15, 8)) { BiwerVisualsClient.CONFIG.auraOnlyCriticals = !BiwerVisualsClient.CONFIG.auraOnlyCriticals; return true; }
                if (isHovered(mouseX, mouseY, x1 + 66, auraY + 152, 15, 8)) { BiwerVisualsClient.CONFIG.auraAdaptiveHits = !BiwerVisualsClient.CONFIG.auraAdaptiveHits; return true; }

                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 165, 15, 8)) { BiwerVisualsClient.CONFIG.auraShieldBreaker = !BiwerVisualsClient.CONFIG.auraShieldBreaker; return true; }
                if (isHovered(mouseX, mouseY, x1 + 70, auraY + 165, 15, 8)) { BiwerVisualsClient.CONFIG.auraSmartSprint = !BiwerVisualsClient.CONFIG.auraSmartSprint; return true; }

                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 178, 15, 8)) { BiwerVisualsClient.CONFIG.auraDontHitItemUse = !BiwerVisualsClient.CONFIG.auraDontHitItemUse; return true; }
                if (isHovered(mouseX, mouseY, x1 + 70, auraY + 178, 15, 8)) { BiwerVisualsClient.CONFIG.auraDontHitContainer = !BiwerVisualsClient.CONFIG.auraDontHitContainer; return true; }

                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 191, 15, 8)) { BiwerVisualsClient.CONFIG.auraDontHitThroughWalls = !BiwerVisualsClient.CONFIG.auraDontHitThroughWalls; return true; }

                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 216, 100, 4)) { BiwerVisualsClient.CONFIG.auraSpeed = (float) (1 + (mouseX - (x1 + 10)) / 100.0 * 29); return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, auraY + 241, 100, 4)) { BiwerVisualsClient.CONFIG.auraSmooth = (float) (5 + (mouseX - (x1 + 10)) / 100.0 * 95); return true; }
            }

            // TriggerBot
            int triggerY = auraY + (BiwerVisualsClient.CONFIG.auraCollapsed ? 30 : 260);
            if (isHovered(mouseX, mouseY, x1 + 90, triggerY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.triggerBotEnabled = !BiwerVisualsClient.CONFIG.triggerBotEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.triggerBotCollapsed) {
                if (isHovered(mouseX, mouseY, x1 + 10, triggerY + 32, 100, 4)) { BiwerVisualsClient.CONFIG.triggerBotRange = (float) (0.5 + (mouseX - (x1 + 10)) / 100.0 * 4.5); return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, triggerY + 45, 15, 8)) { BiwerVisualsClient.CONFIG.triggerBotWeaponOnly = !BiwerVisualsClient.CONFIG.triggerBotWeaponOnly; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, triggerY + 58, 15, 8)) { BiwerVisualsClient.CONFIG.triggerBotOnlyCriticals = !BiwerVisualsClient.CONFIG.triggerBotOnlyCriticals; return true; }
                if (isHovered(mouseX, mouseY, x1 + 70, triggerY + 58, 15, 8)) { BiwerVisualsClient.CONFIG.triggerBotDontHitThroughWalls = !BiwerVisualsClient.CONFIG.triggerBotDontHitThroughWalls; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, triggerY + 71, 15, 8)) { BiwerVisualsClient.CONFIG.triggerBotPlayers = !BiwerVisualsClient.CONFIG.triggerBotPlayers; return true; }
                if (isHovered(mouseX, mouseY, x1 + 70, triggerY + 71, 15, 8)) { BiwerVisualsClient.CONFIG.triggerBotMobs = !BiwerVisualsClient.CONFIG.triggerBotMobs; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, triggerY + 84, 15, 8)) { BiwerVisualsClient.CONFIG.triggerBotAnimals = !BiwerVisualsClient.CONFIG.triggerBotAnimals; return true; }
            }

            // AimAssistant
            int assistY = triggerY + (BiwerVisualsClient.CONFIG.triggerBotCollapsed ? 30 : 106);
            if (isHovered(mouseX, mouseY, x1 + 90, assistY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.aimAssistEnabled = !BiwerVisualsClient.CONFIG.aimAssistEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.aimAssistCollapsed) {
                if (isHovered(mouseX, mouseY, x1 + 10, assistY + 32, 100, 4)) { BiwerVisualsClient.CONFIG.aimAssistStrength = (float) (5 + (mouseX - (x1 + 10)) / 100.0 * 95); return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, assistY + 45, 15, 8)) { BiwerVisualsClient.CONFIG.aimAssistWeaponOnly = !BiwerVisualsClient.CONFIG.aimAssistWeaponOnly; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, assistY + 58, 15, 8)) { BiwerVisualsClient.CONFIG.aimAssistThroughWalls = !BiwerVisualsClient.CONFIG.aimAssistThroughWalls; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, assistY + 71, 15, 8)) { BiwerVisualsClient.CONFIG.aimAssistPlayers = !BiwerVisualsClient.CONFIG.aimAssistPlayers; return true; }
                if (isHovered(mouseX, mouseY, x1 + 70, assistY + 71, 15, 8)) { BiwerVisualsClient.CONFIG.aimAssistMobs = !BiwerVisualsClient.CONFIG.aimAssistMobs; return true; }
                if (isHovered(mouseX, mouseY, x1 + 10, assistY + 84, 15, 8)) { BiwerVisualsClient.CONFIG.aimAssistAnimals = !BiwerVisualsClient.CONFIG.aimAssistAnimals; return true; }
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
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 32, 100, 4)) { BiwerVisualsClient.CONFIG.vmMainX = (float) ((mouseX - (x3 + 10)) / 100.0 * 4 - 2); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 42, 100, 4)) { BiwerVisualsClient.CONFIG.vmMainY = (float) ((mouseX - (x3 + 10)) / 100.0 * 4 - 2); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 52, 100, 4)) { BiwerVisualsClient.CONFIG.vmMainZ = (float) ((mouseX - (x3 + 10)) / 100.0 * 4 - 2); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 77, 100, 4)) { BiwerVisualsClient.CONFIG.vmOffX = (float) ((mouseX - (x3 + 10)) / 100.0 * 4 - 2); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 87, 100, 4)) { BiwerVisualsClient.CONFIG.vmOffY = (float) ((mouseX - (x3 + 10)) / 100.0 * 4 - 2); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 97, 100, 4)) { BiwerVisualsClient.CONFIG.vmOffZ = (float) ((mouseX - (x3 + 10)) / 100.0 * 4 - 2); return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, vmY + 108, 100, 12)) {
                    BiwerVisualsClient.CONFIG.vmMainX = 0.0f;
                    BiwerVisualsClient.CONFIG.vmMainY = 0.0f;
                    BiwerVisualsClient.CONFIG.vmMainZ = 0.0f;
                    BiwerVisualsClient.CONFIG.vmOffX = 0.0f;
                    BiwerVisualsClient.CONFIG.vmOffY = 0.0f;
                    BiwerVisualsClient.CONFIG.vmOffZ = 0.0f;
                    return true;
                }
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

            // JumpCircles
            int jcY = tagsY + (BiwerVisualsClient.CONFIG.tagsCollapsed ? 30 : 80);
            if (isHovered(mouseX, mouseY, x3 + 90, jcY + 5, 20, 10)) {
                BiwerVisualsClient.CONFIG.jumpCirclesEnabled = !BiwerVisualsClient.CONFIG.jumpCirclesEnabled;
                return true;
            }

            // CustomSky
            int skyY = jcY + 30;
            if (isHovered(mouseX, mouseY, x3 + 90, skyY + 5, 20, 10)) {
                BiwerVisualsClient.CONFIG.customSkyEnabled = !BiwerVisualsClient.CONFIG.customSkyEnabled;
                return true;
            }
            if (!BiwerVisualsClient.CONFIG.customSkyCollapsed) {
                if (isHovered(mouseX, mouseY, x3 + 10, skyY + 20, 50, 12)) { BiwerVisualsClient.CONFIG.skyMode = 0; return true; }
                if (isHovered(mouseX, mouseY, x3 + 65, skyY + 20, 50, 12)) { BiwerVisualsClient.CONFIG.skyMode = 1; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, skyY + 34, 50, 12)) { BiwerVisualsClient.CONFIG.skyMode = 2; return true; }
                if (isHovered(mouseX, mouseY, x3 + 65, skyY + 34, 50, 12)) { BiwerVisualsClient.CONFIG.skyMode = 3; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, skyY + 48, 105, 12)) { BiwerVisualsClient.CONFIG.skyMode = 4; return true; }
                if (isHovered(mouseX, mouseY, x3 + 10, skyY + 76, 100, 4)) {
                    BiwerVisualsClient.CONFIG.skyStars = (float) Math.max(0, Math.min(100, (mouseX - (x3 + 10))));
                    return true;
                }
                if (isHovered(mouseX, mouseY, x3 + 10, skyY + 101, 100, 4)) {
                    BiwerVisualsClient.CONFIG.skySpeed = (float) Math.max(0, Math.min(100, (mouseX - (x3 + 10))));
                    return true;
                }

                // Северное сияние
                if (isHovered(mouseX, mouseY, x3 + 10, skyY + SKY_AURORA_TOGGLE_Y, 15, 8)) {
                    BiwerVisualsClient.CONFIG.auroraEnabled = !BiwerVisualsClient.CONFIG.auroraEnabled;
                    if (BiwerVisualsClient.CONFIG.auroraEnabled) BiwerVisualsClient.CONFIG.customSkyEnabled = true;
                    return true;
                }
                if (BiwerVisualsClient.CONFIG.auroraEnabled) {
                    if (isHovered(mouseX, mouseY, x3 + 10, skyY + SKY_AURORA_BOTTOM_SLIDER_Y, 30, 4)) {
                        BiwerVisualsClient.CONFIG.auroraColorBottom = withChannel(BiwerVisualsClient.CONFIG.auroraColorBottom, 16, channelValue(mouseX, x3 + 10, 30));
                        return true;
                    }
                    if (isHovered(mouseX, mouseY, x3 + 45, skyY + SKY_AURORA_BOTTOM_SLIDER_Y, 30, 4)) {
                        BiwerVisualsClient.CONFIG.auroraColorBottom = withChannel(BiwerVisualsClient.CONFIG.auroraColorBottom, 8, channelValue(mouseX, x3 + 45, 30));
                        return true;
                    }
                    if (isHovered(mouseX, mouseY, x3 + 80, skyY + SKY_AURORA_BOTTOM_SLIDER_Y, 30, 4)) {
                        BiwerVisualsClient.CONFIG.auroraColorBottom = withChannel(BiwerVisualsClient.CONFIG.auroraColorBottom, 0, channelValue(mouseX, x3 + 80, 30));
                        return true;
                    }
                    if (isHovered(mouseX, mouseY, x3 + 10, skyY + SKY_AURORA_TOP_SLIDER_Y, 30, 4)) {
                        BiwerVisualsClient.CONFIG.auroraColorTop = withChannel(BiwerVisualsClient.CONFIG.auroraColorTop, 16, channelValue(mouseX, x3 + 10, 30));
                        return true;
                    }
                    if (isHovered(mouseX, mouseY, x3 + 45, skyY + SKY_AURORA_TOP_SLIDER_Y, 30, 4)) {
                        BiwerVisualsClient.CONFIG.auroraColorTop = withChannel(BiwerVisualsClient.CONFIG.auroraColorTop, 8, channelValue(mouseX, x3 + 45, 30));
                        return true;
                    }
                    if (isHovered(mouseX, mouseY, x3 + 80, skyY + SKY_AURORA_TOP_SLIDER_Y, 30, 4)) {
                        BiwerVisualsClient.CONFIG.auroraColorTop = withChannel(BiwerVisualsClient.CONFIG.auroraColorTop, 0, channelValue(mouseX, x3 + 80, 30));
                        return true;
                    }
                    if (isHovered(mouseX, mouseY, x3 + 10, skyY + SKY_AURORA_BRIGHT_SLIDER_Y, 100, 4)) {
                        BiwerVisualsClient.CONFIG.auroraBrightness = (float) Math.max(0, Math.min(100, (mouseX - (x3 + 10))));
                        return true;
                    }

                }
            }

            // Fly

            if (isHovered(mouseX, mouseY, x2 + 90, flyY + 5, 20, 10)) {
                BiwerVisualsClient.CONFIG.flyEnabled = !BiwerVisualsClient.CONFIG.flyEnabled;
                return true;
            }

            // Sprint
            if (isHovered(mouseX, mouseY, x2 + 90, sprintY + 5, 20, 10)) {
                BiwerVisualsClient.CONFIG.sprintEnabled = !BiwerVisualsClient.CONFIG.sprintEnabled;
                return true;
            }

            // Speed
            int speedY = flyY + 60;
            if (isHovered(mouseX, mouseY, x2 + 90, speedY + 5, 20, 10)) {
                BiwerVisualsClient.CONFIG.speedEnabled = !BiwerVisualsClient.CONFIG.speedEnabled;
                return true;
            }
            if (!BiwerVisualsClient.CONFIG.speedCollapsed) {
                if (isHovered(mouseX, mouseY, x2 + 10, speedY + 32, 100, 4)) {
                    BiwerVisualsClient.CONFIG.speedPercent = (float) Math.max(5, Math.min(60, Math.round((mouseX - (x2 + 10)) / 100.0 * 55 + 5)));
                    return true;
                }
                if (isHovered(mouseX, mouseY, x2 + 10, speedY + 45, 15, 8)) { BiwerVisualsClient.CONFIG.speedOnlySprint = !BiwerVisualsClient.CONFIG.speedOnlySprint; return true; }
                if (isHovered(mouseX, mouseY, x2 + 10, speedY + 58, 15, 8)) { BiwerVisualsClient.CONFIG.speedSmooth = !BiwerVisualsClient.CONFIG.speedSmooth; return true; }
                if (isHovered(mouseX, mouseY, x2 + 10, speedY + 71, 15, 8)) { BiwerVisualsClient.CONFIG.speedSkipIfPotion = !BiwerVisualsClient.CONFIG.speedSkipIfPotion; return true; }
            }

            // BunnyHop
            int bhopY = speedY + (BiwerVisualsClient.CONFIG.speedCollapsed ? 30 : 100);
            if (isHovered(mouseX, mouseY, x2 + 90, bhopY + 5, 20, 10)) {
                BiwerVisualsClient.CONFIG.bunnyHopEnabled = !BiwerVisualsClient.CONFIG.bunnyHopEnabled;
                return true;
            }
            if (!BiwerVisualsClient.CONFIG.bunnyHopCollapsed) {
                if (isHovered(mouseX, mouseY, x2 + 10, bhopY + 20, 15, 8)) { BiwerVisualsClient.CONFIG.bhopAutoJump = !BiwerVisualsClient.CONFIG.bhopAutoJump; return true; }
                if (isHovered(mouseX, mouseY, x2 + 10, bhopY + 33, 15, 8)) { BiwerVisualsClient.CONFIG.bhopOnlyWhileMoving = !BiwerVisualsClient.CONFIG.bhopOnlyWhileMoving; return true; }
                if (isHovered(mouseX, mouseY, x2 + 10, bhopY + 58, 100, 4)) {
                    BiwerVisualsClient.CONFIG.bhopJumpDelay = (int) Math.round((mouseX - (x2 + 10)) / 100.0 * 20);
                    return true;
                }
            }

            // TargetStrafe
            int strafeY = bhopY + (BiwerVisualsClient.CONFIG.bunnyHopCollapsed ? 30 : 90);
            if (isHovered(mouseX, mouseY, x2 + 90, strafeY + 5, 20, 10)) {
                BiwerVisualsClient.CONFIG.targetStrafeEnabled = !BiwerVisualsClient.CONFIG.targetStrafeEnabled;
                return true;
            }
            if (!BiwerVisualsClient.CONFIG.targetStrafeCollapsed) {
                if (isHovered(mouseX, mouseY, x2 + 10, strafeY + 20, 15, 8)) { BiwerVisualsClient.CONFIG.strafeAutoJump = !BiwerVisualsClient.CONFIG.strafeAutoJump; return true; }
                if (isHovered(mouseX, mouseY, x2 + 10, strafeY + 45, 100, 4)) {
                    BiwerVisualsClient.CONFIG.strafeRange = (float) Math.max(0.5, Math.min(4.0, (mouseX - (x2 + 10)) / 100.0 * 3.5 + 0.5));
                    return true;
                }
            }

            // AutoTotem (Player)
            if (isHovered(mouseX, mouseY, x4 + 90, autoTotemY + 5, 20, 10)) {
                BiwerVisualsClient.CONFIG.autoTotemEnabled = !BiwerVisualsClient.CONFIG.autoTotemEnabled;
                return true;
            }
            if (!BiwerVisualsClient.CONFIG.autoTotemCollapsed) {
                if (isHovered(mouseX, mouseY, x4 + 10, autoTotemY + 35, 100, 4)) {
                    BiwerVisualsClient.CONFIG.autoTotemDelay = (int) Math.round((mouseX - (x4 + 10)) / 100.0 * 19 + 1);
                    return true;
                }
            }

            // Velocity
            int velocityY = autoTotemY + (BiwerVisualsClient.CONFIG.autoTotemCollapsed ? 30 : 60);
            if (isHovered(mouseX, mouseY, x4 + 90, velocityY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.velocityEnabled = !BiwerVisualsClient.CONFIG.velocityEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.velocityCollapsed) {
                if (isHovered(mouseX, mouseY, x4 + 10, velocityY + 32, 50, 12)) { BiwerVisualsClient.CONFIG.velocityMode = 0; return true; }
                if (isHovered(mouseX, mouseY, x4 + 64, velocityY + 32, 46, 12)) { BiwerVisualsClient.CONFIG.velocityMode = 1; return true; }
                if (isHovered(mouseX, mouseY, x4 + 10, velocityY + 58, 100, 4)) { BiwerVisualsClient.CONFIG.velocityPercent = (float) Math.max(0, Math.min(100, (mouseX - (x4 + 10)))); return true; }
            }

            // HUD
            int miscX = startX + (colWidth + padding) * 4;
            int hudY = startY + 30;
            if (isHovered(mouseX, mouseY, miscX + 90, hudY + 5, 20, 10)) {
                BiwerVisualsClient.CONFIG.hudEnabled = !BiwerVisualsClient.CONFIG.hudEnabled;
                return true;
            }
            if (isHovered(mouseX, mouseY, miscX + 10, hudY + 24, 100, 14)) {
                if (this.minecraft != null) this.minecraft.setScreen(new HudEditorScreen(this));
                return true;
            }

            // FullBright
            int fullBrightY = hudY + 50;
            if (isHovered(mouseX, mouseY, miscX + 90, fullBrightY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.fullBrightEnabled = !BiwerVisualsClient.CONFIG.fullBrightEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.fullBrightCollapsed) {
                if (isHovered(mouseX, mouseY, miscX + 10, fullBrightY + 32, 50, 12)) { BiwerVisualsClient.CONFIG.fullBrightMode = 0; return true; }
                if (isHovered(mouseX, mouseY, miscX + 64, fullBrightY + 32, 46, 12)) { BiwerVisualsClient.CONFIG.fullBrightMode = 1; return true; }
                if (isHovered(mouseX, mouseY, miscX + 10, fullBrightY + 58, 100, 4)) { BiwerVisualsClient.CONFIG.fullBrightGamma = (float) (1.0 + (mouseX - (miscX + 10)) / 100.0 * 7.0); return true; }
            }

            // Crosshair
            int crosshairY = fullBrightY + (BiwerVisualsClient.CONFIG.fullBrightCollapsed ? 30 : 80);
            if (isHovered(mouseX, mouseY, miscX + 90, crosshairY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.crosshairEnabled = !BiwerVisualsClient.CONFIG.crosshairEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.crosshairCollapsed) {
                if (isHovered(mouseX, mouseY, miscX + 10, crosshairY + 32, 100, 4)) { BiwerVisualsClient.CONFIG.crosshairGap = (float) ((mouseX - (miscX + 10)) / 100.0 * 6.0); return true; }
                if (isHovered(mouseX, mouseY, miscX + 10, crosshairY + 57, 100, 4)) { BiwerVisualsClient.CONFIG.crosshairLength = (float) (2.0 + (mouseX - (miscX + 10)) / 100.0 * 3.0); return true; }
                if (isHovered(mouseX, mouseY, miscX + 10, crosshairY + 70, 15, 8)) { BiwerVisualsClient.CONFIG.crosshairOutline = !BiwerVisualsClient.CONFIG.crosshairOutline; return true; }
                if (isHovered(mouseX, mouseY, miscX + 70, crosshairY + 70, 15, 8)) { BiwerVisualsClient.CONFIG.crosshairCenterDot = !BiwerVisualsClient.CONFIG.crosshairCenterDot; return true; }
                if (isHovered(mouseX, mouseY, miscX + 10, crosshairY + 83, 15, 8)) { BiwerVisualsClient.CONFIG.crosshairAdaptive = !BiwerVisualsClient.CONFIG.crosshairAdaptive; return true; }
            }

            // SwingAnimation
            int swingY = crosshairY + (BiwerVisualsClient.CONFIG.crosshairCollapsed ? 30 : 106);
            if (isHovered(mouseX, mouseY, miscX + 90, swingY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.swingAnimationEnabled = !BiwerVisualsClient.CONFIG.swingAnimationEnabled; return true; }
            if (!BiwerVisualsClient.CONFIG.swingAnimationCollapsed) {
                for (int i = 0; i < 5; i++) {
                    if (isHovered(mouseX, mouseY, miscX + 10 + i * 20, swingY + 32, 18, 12)) {
                        BiwerVisualsClient.CONFIG.swingAnimationMode = i;
                        return true;
                    }
                }
                if (isHovered(mouseX, mouseY, miscX + 10, swingY + 58, 100, 4)) { BiwerVisualsClient.CONFIG.swingAngle = (float) ((mouseX - (miscX + 10)) / 100.0 * 360.0); return true; }
                if (isHovered(mouseX, mouseY, miscX + 10, swingY + 83, 100, 4)) { BiwerVisualsClient.CONFIG.swingTipTilt = (float) (-90.0 + (mouseX - (miscX + 10)) / 100.0 * 180.0); return true; }
                if (isHovered(mouseX, mouseY, miscX + 10, swingY + 108, 100, 4)) { BiwerVisualsClient.CONFIG.swingPower = (float) (1.0 + (mouseX - (miscX + 10)) / 100.0 * 9.0); return true; }
            }

            // SeeInvisibles
            int seeY = swingY + (BiwerVisualsClient.CONFIG.swingAnimationCollapsed ? 30 : 130);
            if (isHovered(mouseX, mouseY, miscX + 90, seeY + 5, 20, 10)) { BiwerVisualsClient.CONFIG.seeInvisiblesEnabled = !BiwerVisualsClient.CONFIG.seeInvisiblesEnabled; return true; }

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