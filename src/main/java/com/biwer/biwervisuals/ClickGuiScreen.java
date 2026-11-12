package com.biwer.biwervisuals;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ClickGuiScreen extends Screen {
    private final Screen parent;

    private final int moduleBg = 0xCC14141F;
    private final int activeColor = 0xFFAB9FF2;
    private final int textColor = 0xFFFFFFFF;
    private final int textMuted = 0xFF8888AA;
    private final int sliderBg = 0xFF333333;

    private String searchQuery = "";
    private final List<Category> categories = new ArrayList<>();
    private SettingEntry draggingSetting = null;

    private static class Category {
        String name;
        List<ModuleEntry> modules = new ArrayList<>();
        int x, y, width;
        Category(String name) { this.name = name; }
    }

    private static class ModuleEntry {
        String name;
        boolean enabled;
        boolean expanded = false;
        int x, y, width, height = 16;
        float animState = 0.0f;
        float expandAnim = 0.0f;
        List<SettingEntry> settings = new ArrayList<>();
        List<BooleanSetting> booleans = new ArrayList<>();
        List<ModeSetting> modes = new ArrayList<>();
        List<ColorSetting> colors = new ArrayList<>();

        ModuleEntry(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }
    }

    private static class SettingEntry {
        String name;
        float value;
        int x, y, width, height = 12;
        Consumer<Float> onChange;
        BooleanSupplier visible;

        SettingEntry(String name, float value, Consumer<Float> onChange) {
            this(name, value, onChange, () -> true);
        }

        SettingEntry(String name, float value, Consumer<Float> onChange, BooleanSupplier visible) {
            this.name = name;
            this.value = value;
            this.onChange = onChange;
            this.visible = visible;
        }
    }

    private static class BooleanSetting {
        String name;
        boolean value;
        int x, y, width, height = 12;
        Consumer<Boolean> onChange;
        BooleanSupplier visible;

        BooleanSetting(String name, boolean value, Consumer<Boolean> onChange) {
            this(name, value, onChange, () -> true);
        }

        BooleanSetting(String name, boolean value, Consumer<Boolean> onChange, BooleanSupplier visible) {
            this.name = name;
            this.value = value;
            this.onChange = onChange;
            this.visible = visible;
        }
    }

    private static class ModeSetting {
        String name;
        String[] options;
        int index;
        int x, y, width, height = 12;
        IntConsumer onChange;
        BooleanSupplier visible;

        ModeSetting(String name, String[] options, int index, IntConsumer onChange) {
            this(name, options, index, onChange, () -> true);
        }

        ModeSetting(String name, String[] options, int index, IntConsumer onChange, BooleanSupplier visible) {
            this.name = name;
            this.options = options;
            this.index = index;
            this.onChange = onChange;
            this.visible = visible;
        }

        String current() {
            return options[Math.max(0, Math.min(options.length - 1, index))];
        }
    }

    private static class ColorSetting {
        String name;
        int color;
        int x, y, width, height = 12;
        IntConsumer onChange;
        BooleanSupplier visible;
        boolean open = false;

        ColorSetting(String name, int color, IntConsumer onChange) {
            this(name, color, onChange, () -> true);
        }

        ColorSetting(String name, int color, IntConsumer onChange, BooleanSupplier visible) {
            this.name = name;
            this.color = color & 0xFFFFFF;
            this.onChange = onChange;
            this.visible = visible;
        }
    }

    public ClickGuiScreen(Screen parent) {
        super(Component.literal("Biwer Visuals"));
        this.parent = parent;
        initModules();
    }

    private void initModules() {
        // ================= COMBAT =================
        Category combat = new Category("Combat");

        ModuleEntry killAura = new ModuleEntry("KillAura", BiwerVisualsClient.CONFIG.killAuraEnabled);
        killAura.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.killAuraRange - 1f) / 5f, v -> BiwerVisualsClient.CONFIG.killAuraRange = 1f + v * 5f));
        killAura.settings.add(new SettingEntry("Плавность", BiwerVisualsClient.CONFIG.killAuraSmooth / 100f, v -> BiwerVisualsClient.CONFIG.killAuraSmooth = v * 100f));
        killAura.modes.add(new ModeSetting("Наводка", new String[]{"Свободно", "Преследование", "Наводка"}, BiwerVisualsClient.CONFIG.aimMode, v -> BiwerVisualsClient.CONFIG.aimMode = v));
        killAura.booleans.add(new BooleanSetting("Криты", BiwerVisualsClient.CONFIG.onlyCriticals, v -> BiwerVisualsClient.CONFIG.onlyCriticals = v));
        combat.modules.add(killAura);

        ModuleEntry aura = new ModuleEntry("Aura", BiwerVisualsClient.CONFIG.auraEnabled);
        aura.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.auraRange - 0.1f) / 5.9f, v -> BiwerVisualsClient.CONFIG.auraRange = 0.1f + v * 5.9f));
        aura.modes.add(new ModeSetting("Наводка", new String[]{"ФанТайм", "ФанТайм ФОВ", "Легит"}, BiwerVisualsClient.CONFIG.auraAimMode, v -> BiwerVisualsClient.CONFIG.auraAimMode = v));
        aura.booleans.add(new BooleanSetting("Криты", BiwerVisualsClient.CONFIG.auraOnlyCriticals, v -> BiwerVisualsClient.CONFIG.auraOnlyCriticals = v));
        combat.modules.add(aura);

        ModuleEntry triggerBot = new ModuleEntry("TriggerBot", BiwerVisualsClient.CONFIG.triggerBotEnabled);
        triggerBot.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.triggerBotRange - 0.5f) / 4.5f, v -> BiwerVisualsClient.CONFIG.triggerBotRange = 0.5f + v * 4.5f));
        combat.modules.add(triggerBot);

        ModuleEntry aimAssist = new ModuleEntry("AimAssistant", BiwerVisualsClient.CONFIG.aimAssistEnabled);
        aimAssist.settings.add(new SettingEntry("Сила", BiwerVisualsClient.CONFIG.aimAssistStrength / 100f, v -> BiwerVisualsClient.CONFIG.aimAssistStrength = v * 100f));
        combat.modules.add(aimAssist);

        ModuleEntry velocity = new ModuleEntry("Velocity", BiwerVisualsClient.CONFIG.velocityEnabled);
        velocity.settings.add(new SettingEntry("Сила", BiwerVisualsClient.CONFIG.velocityPercent / 100f, v -> BiwerVisualsClient.CONFIG.velocityPercent = v * 100f));
        combat.modules.add(velocity);

        categories.add(combat);

        // ================= MOVEMENT =================
        Category movement = new Category("Movement");
        movement.modules.add(new ModuleEntry("Fly", BiwerVisualsClient.CONFIG.flyEnabled));
        movement.modules.add(new ModuleEntry("Sprint", BiwerVisualsClient.CONFIG.sprintEnabled));

        ModuleEntry speed = new ModuleEntry("Speed", BiwerVisualsClient.CONFIG.speedEnabled);
        speed.settings.add(new SettingEntry("Буст", (BiwerVisualsClient.CONFIG.speedPercent - 5f) / 55f, v -> BiwerVisualsClient.CONFIG.speedPercent = 5f + v * 55f));
        movement.modules.add(speed);

        ModuleEntry bunnyHop = new ModuleEntry("BunnyHop", BiwerVisualsClient.CONFIG.bunnyHopEnabled);
        bunnyHop.settings.add(new SettingEntry("Задержка", BiwerVisualsClient.CONFIG.bhopJumpDelay / 20f, v -> BiwerVisualsClient.CONFIG.bhopJumpDelay = (int)(v * 20f)));
        movement.modules.add(bunnyHop);

        ModuleEntry targetStrafe = new ModuleEntry("TargetStrafe", BiwerVisualsClient.CONFIG.targetStrafeEnabled);
        targetStrafe.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.strafeRange - 0.5f) / 3.5f, v -> BiwerVisualsClient.CONFIG.strafeRange = 0.5f + v * 3.5f));
        movement.modules.add(targetStrafe);

        categories.add(movement);

        // ================= RENDER =================
        Category render = new Category("Render");

        ModuleEntry ambience = new ModuleEntry("Ambience", BiwerVisualsClient.CONFIG.ambienceEnabled);
        ambience.modes.add(new ModeSetting("Время", new String[]{"Реальное", "Рассвет", "День", "Ночь", "Полночь"}, BiwerVisualsClient.CONFIG.activeTimeButton, v -> BiwerVisualsClient.CONFIG.activeTimeButton = v));
        ambience.modes.add(new ModeSetting("Погода", new String[]{"Реальная", "Ясно", "Дождь", "Гроза"}, BiwerVisualsClient.CONFIG.activeWeatherButton, v -> BiwerVisualsClient.CONFIG.activeWeatherButton = v));
        render.modules.add(ambience);

        ModuleEntry viewmodel = new ModuleEntry("Viewmodel", BiwerVisualsClient.CONFIG.viewmodelEnabled);
        viewmodel.settings.add(new SettingEntry("Main X", (BiwerVisualsClient.CONFIG.vmMainX + 2f) / 4f, v -> BiwerVisualsClient.CONFIG.vmMainX = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Main Y", (BiwerVisualsClient.CONFIG.vmMainY + 2f) / 4f, v -> BiwerVisualsClient.CONFIG.vmMainY = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Main Z", (BiwerVisualsClient.CONFIG.vmMainZ + 2f) / 4f, v -> BiwerVisualsClient.CONFIG.vmMainZ = v * 4f - 2f));
        render.modules.add(viewmodel);

        ModuleEntry esp = new ModuleEntry("EntityESP", BiwerVisualsClient.CONFIG.espEnabled);
        esp.booleans.add(new BooleanSetting("Игроки", BiwerVisualsClient.CONFIG.espPlayers, v -> BiwerVisualsClient.CONFIG.espPlayers = v));
        esp.booleans.add(new BooleanSetting("Мобы", BiwerVisualsClient.CONFIG.espMobs, v -> BiwerVisualsClient.CONFIG.espMobs = v));
        render.modules.add(esp);

        ModuleEntry tags = new ModuleEntry("Tags", BiwerVisualsClient.CONFIG.tagsEnabled);
        tags.booleans.add(new BooleanSetting("Игроки", BiwerVisualsClient.CONFIG.tagsPlayers, v -> BiwerVisualsClient.CONFIG.tagsPlayers = v));
        tags.booleans.add(new BooleanSetting("Мобы", BiwerVisualsClient.CONFIG.tagsMobs, v -> BiwerVisualsClient.CONFIG.tagsMobs = v));
        render.modules.add(tags);

        render.modules.add(new ModuleEntry("JumpCircles", BiwerVisualsClient.CONFIG.jumpCirclesEnabled));

        ModuleEntry customSky = new ModuleEntry("CustomSky", BiwerVisualsClient.CONFIG.customSkyEnabled);
        customSky.modes.add(new ModeSetting("Режим", new String[]{"Закат", "Аврора", "Космос", "Неон", "Перелив"}, BiwerVisualsClient.CONFIG.skyMode, v -> BiwerVisualsClient.CONFIG.skyMode = v));
        customSky.settings.add(new SettingEntry("Звёзды", BiwerVisualsClient.CONFIG.skyStars / 100f, v -> BiwerVisualsClient.CONFIG.skyStars = v * 100f));
        customSky.booleans.add(new BooleanSetting("Сияние", BiwerVisualsClient.CONFIG.auroraEnabled, v -> BiwerVisualsClient.CONFIG.auroraEnabled = v));
        customSky.colors.add(new ColorSetting("Низ сияния", BiwerVisualsClient.CONFIG.auroraColorBottom, v -> BiwerVisualsClient.CONFIG.auroraColorBottom = v));
        customSky.colors.add(new ColorSetting("Верх сияния", BiwerVisualsClient.CONFIG.auroraColorTop, v -> BiwerVisualsClient.CONFIG.auroraColorTop = v));
        render.modules.add(customSky);

        ModuleEntry chinaHat = new ModuleEntry("ChinaHat", BiwerVisualsClient.CONFIG.chinaHatEnabled);
        chinaHat.booleans.add(new BooleanSetting("На себя", BiwerVisualsClient.CONFIG.chinaHatSelf, v -> BiwerVisualsClient.CONFIG.chinaHatSelf = v));
        chinaHat.booleans.add(new BooleanSetting("На друзей", BiwerVisualsClient.CONFIG.chinaHatFriends, v -> BiwerVisualsClient.CONFIG.chinaHatFriends = v));
        chinaHat.booleans.add(new BooleanSetting("На всех", BiwerVisualsClient.CONFIG.chinaHatAll, v -> BiwerVisualsClient.CONFIG.chinaHatAll = v));
        chinaHat.colors.add(new ColorSetting("Цвет шляпы", BiwerVisualsClient.CONFIG.chinaHatColor, v -> BiwerVisualsClient.CONFIG.chinaHatColor = v));
        render.modules.add(chinaHat);

        categories.add(render);

        // ================= PLAYER =================
        Category player = new Category("Player");
        ModuleEntry autoTotem = new ModuleEntry("AutoTotem", BiwerVisualsClient.CONFIG.autoTotemEnabled);
        autoTotem.settings.add(new SettingEntry("Задержка", (BiwerVisualsClient.CONFIG.autoTotemDelay - 1f) / 19f, v -> BiwerVisualsClient.CONFIG.autoTotemDelay = 1 + (int)(v * 19f)));
        player.modules.add(autoTotem);
        player.modules.add(new ModuleEntry("HUD", BiwerVisualsClient.CONFIG.hudEnabled));
        categories.add(player);

        // ================= MISC =================
        Category misc = new Category("Misc");
        ModuleEntry fullBright = new ModuleEntry("FullBright", BiwerVisualsClient.CONFIG.fullBrightEnabled);
        fullBright.modes.add(new ModeSetting("Режим", new String[]{"Гамма", "Ночное зрение"}, BiwerVisualsClient.CONFIG.fullBrightMode, v -> BiwerVisualsClient.CONFIG.fullBrightMode = v));
        misc.modules.add(fullBright);

        ModuleEntry crosshair = new ModuleEntry("Crosshair", BiwerVisualsClient.CONFIG.crosshairEnabled);
        crosshair.settings.add(new SettingEntry("Зазор", BiwerVisualsClient.CONFIG.crosshairGap / 6f, v -> BiwerVisualsClient.CONFIG.crosshairGap = v * 6f));
        misc.modules.add(crosshair);

        ModuleEntry swing = new ModuleEntry("SwingAnimation", BiwerVisualsClient.CONFIG.swingAnimationEnabled);
        swing.modes.add(new ModeSetting("Режим", new String[]{"Мод 1", "Мод 2", "Мод 3", "Мод 4", "Мод 5"}, BiwerVisualsClient.CONFIG.swingAnimationMode, v -> BiwerVisualsClient.CONFIG.swingAnimationMode = v));
        swing.settings.add(new SettingEntry("Угол", BiwerVisualsClient.CONFIG.swingAngle / 360f, v -> BiwerVisualsClient.CONFIG.swingAngle = v * 360f, () -> BiwerVisualsClient.CONFIG.swingAnimationMode == 0));
        swing.settings.add(new SettingEntry("Наклон", (BiwerVisualsClient.CONFIG.swingTipTilt + 90f) / 180f, v -> BiwerVisualsClient.CONFIG.swingTipTilt = v * 180f - 90f, () -> BiwerVisualsClient.CONFIG.swingAnimationMode != 4));
        swing.settings.add(new SettingEntry("Интенсивность", (BiwerVisualsClient.CONFIG.swingPower - 1f) / 9f, v -> BiwerVisualsClient.CONFIG.swingPower = 1f + v * 9f));
        misc.modules.add(swing);

        misc.modules.add(new ModuleEntry("SeeInvisibles", BiwerVisualsClient.CONFIG.seeInvisiblesEnabled));
        categories.add(misc);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int colWidth = 120, gap = 6;
        int totalWidth = (colWidth * categories.size()) + (gap * (categories.size() - 1));
        int startX = (this.width - totalWidth) / 2, startY = 40;

        graphics.fill(0, 0, this.width, this.height, 0x88000000);
        drawRoundedRect(graphics, (this.width - 220) / 2, 10, 220, 20, 4, moduleBg);

        graphics.text(this.font, "Поиск: " + searchQuery, (this.width - 220) / 2 + 6, 16, textColor, false);

        int currentX = startX;
        for (Category cat : categories) {
            cat.x = currentX; cat.y = startY; cat.width = colWidth;
            renderCategory(graphics, cat, mouseX, mouseY, delta);
            currentX += colWidth + gap;
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void renderCategory(GuiGraphicsExtractor graphics, Category cat, int mouseX, int mouseY, float delta) {
        drawRoundedRect(graphics, cat.x, cat.y, cat.width, 18, 4, moduleBg);
        graphics.text(this.font, cat.name, cat.x + 6, cat.y + 5, activeColor, false);

        int moduleY = cat.y + 24;
        for (ModuleEntry module : cat.modules) {
            if (!searchQuery.isEmpty() && !module.name.toLowerCase().contains(searchQuery.toLowerCase())) continue;

            module.x = cat.x + 2; module.y = moduleY; module.width = cat.width - 4;
            module.animState += ((module.enabled ? 1f : 0f) - module.animState) * 0.2f;
            module.expandAnim += ((module.expanded ? 1f : 0f) - module.expandAnim) * 0.2f;

            drawRoundedRect(graphics, module.x, module.y, module.width, module.height, 3, module.enabled ? 0x33AB9FF2 : 0x22000000);
            graphics.text(this.font, module.name, module.x + 6, module.y + 4, module.enabled ? activeColor : textMuted, false);

            int toggleX = module.x + module.width - 16, toggleY = module.y + 4;
            drawRoundedRect(graphics, toggleX, toggleY, 12, 8, 4, sliderBg);
            drawRoundedRect(graphics, (int)(toggleX + 1 + (module.animState * 6)), toggleY + 1, 6, 6, 3, module.enabled ? activeColor : 0xFFFFFFFF);
            graphics.text(this.font, module.expanded ? "v" : ">", toggleX - 10, module.y + 4, textMuted, false);

            int settingsY = module.y + module.height + 2;
            if (module.expandAnim > 0.01f) {
                for (SettingEntry s : module.settings) {
                    if (!s.visible.getAsBoolean()) continue;
                    s.x = module.x + 2; s.y = settingsY; s.width = module.width - 4;
                    graphics.text(this.font, s.name, s.x + 2, s.y, textMuted, false);
                    graphics.fill(s.x + 2, s.y + 8, s.x + s.width - 2, s.y + 11, sliderBg);
                    graphics.fill(s.x + 2, s.y + 8, s.x + 2 + (int)((s.width - 4) * s.value), s.y + 11, activeColor);
                    settingsY += 16;
                }
                for (BooleanSetting b : module.booleans) {
                    if (!b.visible.getAsBoolean()) continue;
                    b.x = module.x + 2; b.y = settingsY; b.width = module.width - 4;
                    graphics.text(this.font, b.name, b.x + 2, b.y, textMuted, false);
                    drawRoundedRect(graphics, b.x + b.width - 14, b.y - 1, 10, 10, 3, sliderBg);
                    if (b.value) drawRoundedRect(graphics, b.x + b.width - 12, b.y + 1, 6, 6, 2, activeColor);
                    settingsY += 14;
                }
                for (ModeSetting m : module.modes) {
                    if (!m.visible.getAsBoolean()) continue;
                    m.x = module.x + 2; m.y = settingsY; m.width = module.width - 4;
                    graphics.text(this.font, m.name + ": " + m.current(), m.x + 2, m.y, activeColor, false);
                    settingsY += 14;
                }
                for (ColorSetting c : module.colors) {
                    if (!c.visible.getAsBoolean()) continue;
                    c.x = module.x + 2; c.y = settingsY; c.width = module.width - 4;
                    graphics.text(this.font, c.name, c.x + 2, c.y, textMuted, false);
                    graphics.fill(c.x + c.width - 14, c.y - 1, c.x + c.width - 4, c.y + 9, 0xFF000000 | c.color);
                    settingsY += 14;
                }
            }
            moduleY = settingsY;
            if (module.expandAnim <= 0.01f) moduleY += module.height + 2;
        }
    }

    private void drawRoundedRect(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int r, int color) {
        graphics.fill(x + r, y, x + w - r, y + h, color);
        graphics.fill(x, y + r, x + w, y + h - r, color);
        graphics.fill(x + 1, y + 1, x + r, y + r, color);
        graphics.fill(x + w - r, y + 1, x + w - 1, y + r, color);
        graphics.fill(x + 1, y + h - r, x + r, y + h - 1, color);
        graphics.fill(x + w - r, y + h - r, x + w - 1, y + h - 1, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        int button = click.button();

        for (Category cat : categories) {
            for (ModuleEntry module : cat.modules) {
                if (mouseX >= module.x && mouseX <= module.x + module.width && mouseY >= module.y && mouseY <= module.y + module.height) {
                    if (button == 0) { module.enabled = !module.enabled; updateModuleConfig(module.name, module.enabled); return true; }
                    if (button == 1) { module.expanded = !module.expanded; return true; }
                }
                if (module.expandAnim > 0.01f) {
                    for (SettingEntry s : module.settings) {
                        if (!s.visible.getAsBoolean()) continue;
                        if (mouseX >= s.x && mouseX <= s.x + s.width && mouseY >= s.y && mouseY <= s.y + 14) {
                            s.value = Math.max(0f, Math.min(1f, (float)((mouseX - (s.x + 2)) / (s.width - 4))));
                            s.onChange.accept(s.value);
                            draggingSetting = s;
                            return true;
                        }
                    }
                    for (BooleanSetting b : module.booleans) {
                        if (!b.visible.getAsBoolean()) continue;
                        if (mouseX >= b.x && mouseX <= b.x + b.width && mouseY >= b.y && mouseY <= b.y + b.height) {
                            if (button == 0) { b.value = !b.value; b.onChange.accept(b.value); return true; }
                        }
                    }
                    for (ModeSetting m : module.modes) {
                        if (!m.visible.getAsBoolean()) continue;
                        if (mouseX >= m.x && mouseX <= m.x + m.width && mouseY >= m.y && mouseY <= m.y + m.height) {
                            if (button == 0) { m.index = (m.index + 1) % m.options.length; m.onChange.accept(m.index); return true; }
                        }
                    }
                    for (ColorSetting c : module.colors) {
                        if (!c.visible.getAsBoolean()) continue;
                        if (mouseX >= c.x + c.width - 14 && mouseX <= c.x + c.width - 4 && mouseY >= c.y - 1 && mouseY <= c.y + 9) {
                            if (button == 0) { c.open = !c.open; return true; }
                        }
                    }
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
        if (draggingSetting != null) {
            draggingSetting.value = Math.max(0f, Math.min(1f, (float)((click.x() - (draggingSetting.x + 2)) / (draggingSetting.width - 4))));
            draggingSetting.onChange.accept(draggingSetting.value);
            return true;
        }
        return super.mouseDragged(click, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (draggingSetting != null) { draggingSetting = null; return true; }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE) { if (!searchQuery.isEmpty()) searchQuery = searchQuery.substring(0, searchQuery.length() - 1); return true; }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char c = (char) event.codepoint();
        if (c >= 32 && c < 127) { searchQuery += c; return true; }
        return super.charTyped(event);
    }

    private void updateModuleConfig(String name, boolean state) {
        switch (name) {
            case "KillAura" -> BiwerVisualsClient.CONFIG.killAuraEnabled = state;
            case "Aura" -> BiwerVisualsClient.CONFIG.auraEnabled = state;
            case "TriggerBot" -> BiwerVisualsClient.CONFIG.triggerBotEnabled = state;
            case "AimAssistant" -> BiwerVisualsClient.CONFIG.aimAssistEnabled = state;
            case "Velocity" -> BiwerVisualsClient.CONFIG.velocityEnabled = state;
            case "Fly" -> BiwerVisualsClient.CONFIG.flyEnabled = state;
            case "Sprint" -> BiwerVisualsClient.CONFIG.sprintEnabled = state;
            case "Speed" -> BiwerVisualsClient.CONFIG.speedEnabled = state;
            case "BunnyHop" -> BiwerVisualsClient.CONFIG.bunnyHopEnabled = state;
            case "TargetStrafe" -> BiwerVisualsClient.CONFIG.targetStrafeEnabled = state;
            case "Ambience" -> BiwerVisualsClient.CONFIG.ambienceEnabled = state;
            case "Viewmodel" -> BiwerVisualsClient.CONFIG.viewmodelEnabled = state;
            case "EntityESP" -> BiwerVisualsClient.CONFIG.espEnabled = state;
            case "Tags" -> BiwerVisualsClient.CONFIG.tagsEnabled = state;
            case "JumpCircles" -> BiwerVisualsClient.CONFIG.jumpCirclesEnabled = state;
            case "CustomSky" -> BiwerVisualsClient.CONFIG.customSkyEnabled = state;
            case "ChinaHat" -> BiwerVisualsClient.CONFIG.chinaHatEnabled = state;
            case "AutoTotem" -> BiwerVisualsClient.CONFIG.autoTotemEnabled = state;
            case "HUD" -> BiwerVisualsClient.CONFIG.hudEnabled = state;
            case "FullBright" -> BiwerVisualsClient.CONFIG.fullBrightEnabled = state;
            case "Crosshair" -> BiwerVisualsClient.CONFIG.crosshairEnabled = state;
            case "SwingAnimation" -> BiwerVisualsClient.CONFIG.swingAnimationEnabled = state;
            case "SeeInvisibles" -> BiwerVisualsClient.CONFIG.seeInvisiblesEnabled = state;
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}