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


        ModuleEntry aura = new ModuleEntry("Aura", BiwerVisualsClient.CONFIG.auraEnabled);

// --- Дистанции ---
        aura.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.auraRange - 0.1f) / 5.9f,
                v -> BiwerVisualsClient.CONFIG.auraRange = 0.1f + v * 5.9f));
        aura.settings.add(new SettingEntry("Доп. дистанция", BiwerVisualsClient.CONFIG.auraExtraRange / 3f,
                v -> BiwerVisualsClient.CONFIG.auraExtraRange = v * 3f));

// --- Наводка и приоритет ---
        aura.modes.add(new ModeSetting("Наводка",
                new String[]{"ФанТайм", "ФанТайм ФОВ", "Легит"},
                BiwerVisualsClient.CONFIG.auraAimMode, v -> BiwerVisualsClient.CONFIG.auraAimMode = v));
        aura.modes.add(new ModeSetting("Приоритет",
                new String[]{"Прицел", "Дистанция", "ХП"},
                BiwerVisualsClient.CONFIG.auraPriority, v -> BiwerVisualsClient.CONFIG.auraPriority = v));

// --- Плавность и скорость (используются в Aura.aimFunTime / aimLegit) ---
        aura.settings.add(new SettingEntry("Плавность", BiwerVisualsClient.CONFIG.auraSmooth / 100f,
                v -> BiwerVisualsClient.CONFIG.auraSmooth = v * 100f));
        aura.settings.add(new SettingEntry("Скорость", BiwerVisualsClient.CONFIG.auraSpeed / 30f,
                v -> BiwerVisualsClient.CONFIG.auraSpeed = v * 30f));
        aura.settings.add(new SettingEntry("FOV", (BiwerVisualsClient.CONFIG.auraFov - 10f) / 170f,
                v -> BiwerVisualsClient.CONFIG.auraFov = 10f + v * 170f));

// --- Цели ---
        aura.booleans.add(new BooleanSetting("Игроки", BiwerVisualsClient.CONFIG.auraPlayers,
                v -> BiwerVisualsClient.CONFIG.auraPlayers = v));
        aura.booleans.add(new BooleanSetting("Мобы", BiwerVisualsClient.CONFIG.auraMobs,
                v -> BiwerVisualsClient.CONFIG.auraMobs = v));
        aura.booleans.add(new BooleanSetting("Животные", BiwerVisualsClient.CONFIG.auraAnimals,
                v -> BiwerVisualsClient.CONFIG.auraAnimals = v));
        aura.booleans.add(new BooleanSetting("Без брони", BiwerVisualsClient.CONFIG.auraNaked,
                v -> BiwerVisualsClient.CONFIG.auraNaked = v));

// --- Настройки боя ---
        aura.booleans.add(new BooleanSetting("Криты", BiwerVisualsClient.CONFIG.auraOnlyCriticals,
                v -> BiwerVisualsClient.CONFIG.auraOnlyCriticals = v));
        aura.booleans.add(new BooleanSetting("Адаптивные прыжки", BiwerVisualsClient.CONFIG.auraAdaptiveHits,
                v -> BiwerVisualsClient.CONFIG.auraAdaptiveHits = v));
        aura.booleans.add(new BooleanSetting("Щитолом", BiwerVisualsClient.CONFIG.auraShieldBreaker,
                v -> BiwerVisualsClient.CONFIG.auraShieldBreaker = v));
        aura.booleans.add(new BooleanSetting("Смарт-спринт", BiwerVisualsClient.CONFIG.auraSmartSprint,
                v -> BiwerVisualsClient.CONFIG.auraSmartSprint = v));

// --- Не бить когда ---
        aura.booleans.add(new BooleanSetting("Не бить при использовании", BiwerVisualsClient.CONFIG.auraDontHitItemUse,
                v -> BiwerVisualsClient.CONFIG.auraDontHitItemUse = v));
        aura.booleans.add(new BooleanSetting("Не бить в контейнере", BiwerVisualsClient.CONFIG.auraDontHitContainer,
                v -> BiwerVisualsClient.CONFIG.auraDontHitContainer = v));
        aura.booleans.add(new BooleanSetting("Не бить через стены", BiwerVisualsClient.CONFIG.auraDontHitThroughWalls,
                v -> BiwerVisualsClient.CONFIG.auraDontHitThroughWalls = v));

        combat.modules.add(aura);

        ModuleEntry triggerBot = new ModuleEntry("TriggerBot", BiwerVisualsClient.CONFIG.triggerBotEnabled);
        triggerBot.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.triggerBotRange - 0.5f) / 4.5f,
                v -> BiwerVisualsClient.CONFIG.triggerBotRange = 0.5f + v * 4.5f));
        triggerBot.booleans.add(new BooleanSetting("Криты", BiwerVisualsClient.CONFIG.triggerBotOnlyCriticals,
                v -> BiwerVisualsClient.CONFIG.triggerBotOnlyCriticals = v));
        triggerBot.booleans.add(new BooleanSetting("Только оружие", BiwerVisualsClient.CONFIG.triggerBotWeaponOnly,
                v -> BiwerVisualsClient.CONFIG.triggerBotWeaponOnly = v));
        combat.modules.add(triggerBot);

        ModuleEntry aimAssist = new ModuleEntry("AimAssistant", BiwerVisualsClient.CONFIG.aimAssistEnabled);
        aimAssist.settings.add(new SettingEntry("Сила", BiwerVisualsClient.CONFIG.aimAssistStrength / 100f,
                v -> BiwerVisualsClient.CONFIG.aimAssistStrength = v * 100f));
        aimAssist.booleans.add(new BooleanSetting("Сквозь стены", BiwerVisualsClient.CONFIG.aimAssistThroughWalls,
                v -> BiwerVisualsClient.CONFIG.aimAssistThroughWalls = v));
        combat.modules.add(aimAssist);

        ModuleEntry velocity = new ModuleEntry("Velocity", BiwerVisualsClient.CONFIG.velocityEnabled);
        velocity.settings.add(new SettingEntry("Сила", BiwerVisualsClient.CONFIG.velocityPercent / 100f,
                v -> BiwerVisualsClient.CONFIG.velocityPercent = v * 100f));
        velocity.modes.add(new ModeSetting("Режим",
                new String[]{"Обычный", "Легитный"},
                BiwerVisualsClient.CONFIG.velocityMode, v -> BiwerVisualsClient.CONFIG.velocityMode = v));
        combat.modules.add(velocity);

        categories.add(combat);

        // ================= MOVEMENT =================
        Category movement = new Category("Movement");
        movement.modules.add(new ModuleEntry("Fly", BiwerVisualsClient.CONFIG.flyEnabled));
        movement.modules.add(new ModuleEntry("Sprint", BiwerVisualsClient.CONFIG.sprintEnabled));

        ModuleEntry speed = new ModuleEntry("Speed", BiwerVisualsClient.CONFIG.speedEnabled);
        speed.settings.add(new SettingEntry("Буст", (BiwerVisualsClient.CONFIG.speedPercent - 5f) / 55f,
                v -> BiwerVisualsClient.CONFIG.speedPercent = 5f + v * 55f));
        speed.booleans.add(new BooleanSetting("Только спринт", BiwerVisualsClient.CONFIG.speedOnlySprint,
                v -> BiwerVisualsClient.CONFIG.speedOnlySprint = v));
        speed.booleans.add(new BooleanSetting("Плавно", BiwerVisualsClient.CONFIG.speedSmooth,
                v -> BiwerVisualsClient.CONFIG.speedSmooth = v));
        speed.booleans.add(new BooleanSetting("Пропускать с зельем", BiwerVisualsClient.CONFIG.speedSkipIfPotion,
                v -> BiwerVisualsClient.CONFIG.speedSkipIfPotion = v));
        movement.modules.add(speed);

        ModuleEntry bunnyHop = new ModuleEntry("BunnyHop", BiwerVisualsClient.CONFIG.bunnyHopEnabled);
        bunnyHop.settings.add(new SettingEntry("Задержка", BiwerVisualsClient.CONFIG.bhopJumpDelay / 20f,
                v -> BiwerVisualsClient.CONFIG.bhopJumpDelay = (int)(v * 20f)));
        bunnyHop.booleans.add(new BooleanSetting("Только в движении", BiwerVisualsClient.CONFIG.bhopOnlyWhileMoving,
                v -> BiwerVisualsClient.CONFIG.bhopOnlyWhileMoving = v));
        bunnyHop.booleans.add(new BooleanSetting("Авто-прыжок", BiwerVisualsClient.CONFIG.bhopAutoJump,
                v -> BiwerVisualsClient.CONFIG.bhopAutoJump = v));
        movement.modules.add(bunnyHop);

        ModuleEntry targetStrafe = new ModuleEntry("TargetStrafe", BiwerVisualsClient.CONFIG.targetStrafeEnabled);
        targetStrafe.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.strafeRange - 0.5f) / 3.5f,
                v -> BiwerVisualsClient.CONFIG.strafeRange = 0.5f + v * 3.5f));
        targetStrafe.booleans.add(new BooleanSetting("Авто-прыжок", BiwerVisualsClient.CONFIG.strafeAutoJump,
                v -> BiwerVisualsClient.CONFIG.strafeAutoJump = v));
        movement.modules.add(targetStrafe);

        categories.add(movement);

        // ================= RENDER =================
        Category render = new Category("Render");

        ModuleEntry ambience = new ModuleEntry("Ambience", BiwerVisualsClient.CONFIG.ambienceEnabled);
        ambience.modes.add(new ModeSetting("Время",
                new String[]{"Реальное", "Рассвет", "День", "Ночь", "Полночь"},
                BiwerVisualsClient.CONFIG.activeTimeButton,
                v -> BiwerVisualsClient.CONFIG.activeTimeButton = v));
        ambience.modes.add(new ModeSetting("Погода",
                new String[]{"Реальная", "Ясно", "Дождь", "Гроза"},
                BiwerVisualsClient.CONFIG.activeWeatherButton,
                v -> BiwerVisualsClient.CONFIG.activeWeatherButton = v));
        render.modules.add(ambience);

        ModuleEntry viewmodel = new ModuleEntry("Viewmodel", BiwerVisualsClient.CONFIG.viewmodelEnabled);
        viewmodel.settings.add(new SettingEntry("Main X", (BiwerVisualsClient.CONFIG.vmMainX + 2f) / 4f,
                v -> BiwerVisualsClient.CONFIG.vmMainX = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Main Y", (BiwerVisualsClient.CONFIG.vmMainY + 2f) / 4f,
                v -> BiwerVisualsClient.CONFIG.vmMainY = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Main Z", (BiwerVisualsClient.CONFIG.vmMainZ + 2f) / 4f,
                v -> BiwerVisualsClient.CONFIG.vmMainZ = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Off X", (BiwerVisualsClient.CONFIG.vmOffX + 2f) / 4f,
                v -> BiwerVisualsClient.CONFIG.vmOffX = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Off Y", (BiwerVisualsClient.CONFIG.vmOffY + 2f) / 4f,
                v -> BiwerVisualsClient.CONFIG.vmOffY = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Off Z", (BiwerVisualsClient.CONFIG.vmOffZ + 2f) / 4f,
                v -> BiwerVisualsClient.CONFIG.vmOffZ = v * 4f - 2f));
        render.modules.add(viewmodel);

        ModuleEntry esp = new ModuleEntry("EntityESP", BiwerVisualsClient.CONFIG.espEnabled);
        esp.booleans.add(new BooleanSetting("Игроки", BiwerVisualsClient.CONFIG.espPlayers,
                v -> BiwerVisualsClient.CONFIG.espPlayers = v));
        esp.booleans.add(new BooleanSetting("Мобы", BiwerVisualsClient.CONFIG.espMobs,
                v -> BiwerVisualsClient.CONFIG.espMobs = v));
        esp.booleans.add(new BooleanSetting("Животные", BiwerVisualsClient.CONFIG.espAnimals,
                v -> BiwerVisualsClient.CONFIG.espAnimals = v));
        esp.booleans.add(new BooleanSetting("Без брони", BiwerVisualsClient.CONFIG.espNaked,
                v -> BiwerVisualsClient.CONFIG.espNaked = v));
        render.modules.add(esp);

        ModuleEntry tags = new ModuleEntry("Tags", BiwerVisualsClient.CONFIG.tagsEnabled);
        tags.booleans.add(new BooleanSetting("Игроки", BiwerVisualsClient.CONFIG.tagsPlayers,
                v -> BiwerVisualsClient.CONFIG.tagsPlayers = v));
        tags.booleans.add(new BooleanSetting("Мобы", BiwerVisualsClient.CONFIG.tagsMobs,
                v -> BiwerVisualsClient.CONFIG.tagsMobs = v));
        tags.booleans.add(new BooleanSetting("Животные", BiwerVisualsClient.CONFIG.tagsAnimals,
                v -> BiwerVisualsClient.CONFIG.tagsAnimals = v));
        render.modules.add(tags);

        render.modules.add(new ModuleEntry("JumpCircles", BiwerVisualsClient.CONFIG.jumpCirclesEnabled));

        ModuleEntry customSky = new ModuleEntry("CustomSky", BiwerVisualsClient.CONFIG.customSkyEnabled);
        customSky.modes.add(new ModeSetting("Режим",
                new String[]{"Закат", "Аврора", "Космос", "Неон", "Перелив"},
                BiwerVisualsClient.CONFIG.skyMode, v -> BiwerVisualsClient.CONFIG.skyMode = v));
        customSky.settings.add(new SettingEntry("Звёзды", BiwerVisualsClient.CONFIG.skyStars / 100f,
                v -> BiwerVisualsClient.CONFIG.skyStars = v * 100f));
        customSky.settings.add(new SettingEntry("Скорость", BiwerVisualsClient.CONFIG.skySpeed / 100f,
                v -> BiwerVisualsClient.CONFIG.skySpeed = v * 100f));
        customSky.settings.add(new SettingEntry("Яркость сияния", BiwerVisualsClient.CONFIG.auroraBrightness / 100f,
                v -> BiwerVisualsClient.CONFIG.auroraBrightness = v * 100f));
        customSky.booleans.add(new BooleanSetting("Сияние", BiwerVisualsClient.CONFIG.auroraEnabled,
                v -> BiwerVisualsClient.CONFIG.auroraEnabled = v));
        customSky.colors.add(new ColorSetting("Низ сияния", BiwerVisualsClient.CONFIG.auroraColorBottom,
                v -> BiwerVisualsClient.CONFIG.auroraColorBottom = v));
        customSky.colors.add(new ColorSetting("Верх сияния", BiwerVisualsClient.CONFIG.auroraColorTop,
                v -> BiwerVisualsClient.CONFIG.auroraColorTop = v));
        render.modules.add(customSky);

        ModuleEntry chinaHat = new ModuleEntry("ChinaHat", BiwerVisualsClient.CONFIG.chinaHatEnabled);
        chinaHat.colors.add(new ColorSetting("Цвет шляпы", BiwerVisualsClient.CONFIG.chinaHatColor,
                v -> BiwerVisualsClient.CONFIG.chinaHatColor = v));
        render.modules.add(chinaHat);

        categories.add(render);

        // ================= PLAYER =================
        Category player = new Category("Player");

        ModuleEntry autoTotem = new ModuleEntry("AutoTotem", BiwerVisualsClient.CONFIG.autoTotemEnabled);
        autoTotem.settings.add(new SettingEntry("Задержка", (BiwerVisualsClient.CONFIG.autoTotemDelay - 1f) / 19f,
                v -> BiwerVisualsClient.CONFIG.autoTotemDelay = 1 + (int)(v * 19f)));
        player.modules.add(autoTotem);

        player.modules.add(new ModuleEntry("HUD", BiwerVisualsClient.CONFIG.hudEnabled));

        categories.add(player);

        // ================= MISC =================
        Category misc = new Category("Misc");

        ModuleEntry fullBright = new ModuleEntry("FullBright", BiwerVisualsClient.CONFIG.fullBrightEnabled);
        fullBright.modes.add(new ModeSetting("Режим",
                new String[]{"Гамма", "Ночное зрение"},
                BiwerVisualsClient.CONFIG.fullBrightMode,
                v -> BiwerVisualsClient.CONFIG.fullBrightMode = v));
        fullBright.settings.add(new SettingEntry("Гамма", (BiwerVisualsClient.CONFIG.fullBrightGamma - 1f) / 7f,
                v -> BiwerVisualsClient.CONFIG.fullBrightGamma = 1f + v * 7f,
                () -> BiwerVisualsClient.CONFIG.fullBrightMode == 0));
        misc.modules.add(fullBright);

        ModuleEntry crosshair = new ModuleEntry("Crosshair", BiwerVisualsClient.CONFIG.crosshairEnabled);
        crosshair.settings.add(new SettingEntry("Зазор", BiwerVisualsClient.CONFIG.crosshairGap / 6f,
                v -> BiwerVisualsClient.CONFIG.crosshairGap = v * 6f));
        crosshair.settings.add(new SettingEntry("Длина", (BiwerVisualsClient.CONFIG.crosshairLength - 2f) / 3f,
                v -> BiwerVisualsClient.CONFIG.crosshairLength = 2f + v * 3f));
        crosshair.booleans.add(new BooleanSetting("Контур", BiwerVisualsClient.CONFIG.crosshairOutline,
                v -> BiwerVisualsClient.CONFIG.crosshairOutline = v));
        crosshair.booleans.add(new BooleanSetting("Точка", BiwerVisualsClient.CONFIG.crosshairCenterDot,
                v -> BiwerVisualsClient.CONFIG.crosshairCenterDot = v));
        crosshair.booleans.add(new BooleanSetting("Адаптивный зазор", BiwerVisualsClient.CONFIG.crosshairAdaptive,
                v -> BiwerVisualsClient.CONFIG.crosshairAdaptive = v));
        misc.modules.add(crosshair);

        ModuleEntry swing = new ModuleEntry("SwingAnimation", BiwerVisualsClient.CONFIG.swingAnimationEnabled);
        swing.booleans.add(new BooleanSetting("Учитывать Aura", BiwerVisualsClient.CONFIG.swingAnimationAuraOnly,
                v -> BiwerVisualsClient.CONFIG.swingAnimationAuraOnly = v));
        swing.modes.add(new ModeSetting("Режим анимации",
                new String[]{"Мод 1", "Мод 2", "Мод 3", "Мод 4", "Мод 5"},
                BiwerVisualsClient.CONFIG.swingAnimationMode,
                v -> BiwerVisualsClient.CONFIG.swingAnimationMode = v));
        swing.settings.add(new SettingEntry("Угол поворота", BiwerVisualsClient.CONFIG.swingAngle / 360f,
                v -> BiwerVisualsClient.CONFIG.swingAngle = v * 360f,
                () -> BiwerVisualsClient.CONFIG.swingAnimationMode == 0));
        swing.settings.add(new SettingEntry("Наклон кончика", (BiwerVisualsClient.CONFIG.swingTipTilt + 90f) / 180f,
                v -> BiwerVisualsClient.CONFIG.swingTipTilt = v * 180f - 90f,
                () -> BiwerVisualsClient.CONFIG.swingAnimationMode != 4));
        swing.settings.add(new SettingEntry("Интенсивность", (BiwerVisualsClient.CONFIG.swingPower - 1f) / 9f,
                v -> BiwerVisualsClient.CONFIG.swingPower = 1f + v * 9f));
        misc.modules.add(swing);

        misc.modules.add(new ModuleEntry("SeeInvisibles", BiwerVisualsClient.CONFIG.seeInvisiblesEnabled));

        categories.add(misc);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int colWidth = 120;
        int gap = 6;
        int totalWidth = (colWidth * categories.size()) + (gap * (categories.size() - 1));
        int startX = (this.width - totalWidth) / 2;
        int startY = 40;

        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        int searchWidth = 220;
        int searchX = (this.width - searchWidth) / 2;
        drawRoundedRect(graphics, searchX, 10, searchWidth, 20, 4, moduleBg);
        graphics.text(this.font, "Поиск: " + searchQuery, searchX + 6, 16, textColor, false);

        int currentX = startX;
        for (Category cat : categories) {
            cat.x = currentX;
            cat.y = startY;
            cat.width = colWidth;
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

            module.x = cat.x + 2;
            module.y = moduleY;
            module.width = cat.width - 4;

            float target = module.enabled ? 1.0f : 0.0f;
            module.animState += (target - module.animState) * 0.2f;
            float expandTarget = module.expanded ? 1.0f : 0.0f;
            module.expandAnim += (expandTarget - module.expandAnim) * 0.2f;

            int color = module.enabled ? 0x33AB9FF2 : 0x22000000;
            drawRoundedRect(graphics, module.x, module.y, module.width, module.height, 3, color);

            graphics.text(this.font, module.name, module.x + 6, module.y + 4,
                    module.enabled ? activeColor : textMuted, false);

            int toggleX = module.x + module.width - 16;
            int toggleY = module.y + 4;
            drawRoundedRect(graphics, toggleX, toggleY, 12, 8, 4, sliderBg);
            int circleX = (int) (toggleX + 1 + (module.animState * 6));
            drawRoundedRect(graphics, circleX, toggleY + 1, 6, 6, 3,
                    module.enabled ? activeColor : 0xFFFFFFFF);

            graphics.text(this.font, module.expanded ? "v" : ">", toggleX - 10, module.y + 4, textMuted, false);

            int settingsY = module.y + module.height + 2;

            if (module.expandAnim > 0.01f) {
                for (SettingEntry setting : module.settings) {
                    if (!setting.visible.getAsBoolean()) continue;
                    setting.x = module.x + 2;
                    setting.y = settingsY;
                    setting.width = module.width - 4;

                    graphics.text(this.font, setting.name, setting.x + 2, setting.y, textMuted, false);

                    int sliderY = setting.y + 8;
                    graphics.fill(setting.x + 2, sliderY, setting.x + setting.width - 2, sliderY + 3, sliderBg);
                    int filled = (int)((setting.width - 4) * setting.value);
                    graphics.fill(setting.x + 2, sliderY, setting.x + 2 + filled, sliderY + 3, activeColor);

                    int handleX = setting.x + 2 + filled;
                    graphics.fill(handleX - 1, sliderY - 1, handleX + 1, sliderY + 4, 0xFFFFFFFF);

                    settingsY += 16;
                }

                for (BooleanSetting bool : module.booleans) {
                    if (!bool.visible.getAsBoolean()) continue;
                    bool.x = module.x + 2;
                    bool.y = settingsY;
                    bool.width = module.width - 4;

                    graphics.text(this.font, bool.name, bool.x + 2, bool.y, textMuted, false);

                    int cbX = bool.x + bool.width - 14;
                    int cbY = bool.y - 1;
                    drawRoundedRect(graphics, cbX, cbY, 10, 10, 3, sliderBg);
                    if (bool.value) {
                        drawRoundedRect(graphics, cbX + 2, cbY + 2, 6, 6, 2, activeColor);
                    }

                    settingsY += 14;
                }

                for (ModeSetting mode : module.modes) {
                    if (!mode.visible.getAsBoolean()) continue;
                    mode.x = module.x + 2;
                    mode.y = settingsY;
                    mode.width = module.width - 4;

                    graphics.text(this.font, mode.name + ": " + mode.current(),
                            mode.x + 2, mode.y, activeColor, false);

                    settingsY += 14;
                }

                for (ColorSetting colorSetting : module.colors) {
                    if (!colorSetting.visible.getAsBoolean()) continue;
                    colorSetting.x = module.x + 2;
                    colorSetting.y = settingsY;
                    colorSetting.width = module.width - 4;

                    graphics.text(this.font, colorSetting.name, colorSetting.x + 2, colorSetting.y, textMuted, false);

                    int swatchX = colorSetting.x + colorSetting.width - 14;
                    int swatchY = colorSetting.y - 1;
                    graphics.fill(swatchX, swatchY, swatchX + 10, swatchY + 10, 0xFF000000 | colorSetting.color);
                    graphics.fill(swatchX, swatchY, swatchX + 10, swatchY + 1, 0xFFFFFFFF);

                    if (colorSetting.open) {
                        int gridSize = 6;
                        int cell = 10;
                        int gridX = swatchX - (gridSize * cell) + 10;
                        int gridY = swatchY + 12;
                        graphics.fill(gridX - 1, gridY - 1,
                                gridX + gridSize * cell + 1, gridY + gridSize * cell + 1, 0xFF000000);
                        for (int gy = 0; gy < gridSize; gy++) {
                            for (int gx = 0; gx < gridSize; gx++) {
                                int rgb = hsvToRgb((gx / (float) gridSize) * 360f, 1.0f,
                                        1.0f - (gy / (float) gridSize) * 0.8f);
                                int px = gridX + gx * cell;
                                int py = gridY + gy * cell;
                                graphics.fill(px, py, px + cell - 1, py + cell - 1, 0xFF000000 | rgb);
                            }
                        }
                    }

                    settingsY += colorSetting.open ? 14 + 6 * 10 + 4 : 14;
                }
            }

// Сдвигаем следующий модуль на высоту настроек (с учётом анимации)
            int settingsHeight = settingsY - (module.y + module.height + 2);
            moduleY += module.height + 2 + (int)(settingsHeight * module.expandAnim);

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
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        for (Category cat : categories) {
            for (ModuleEntry module : cat.modules) {
                if (mouseX >= module.x && mouseX <= module.x + module.width
                        && mouseY >= module.y && mouseY <= module.y + module.height) {
                    if (button == 0) {
                        module.enabled = !module.enabled;
                        updateModuleConfig(module.name, module.enabled);
                        return true;
                    }
                    if (button == 1) {
                        module.expanded = !module.expanded;
                        return true;
                    }
                }

                if (module.expandAnim > 0.01f) {
                    for (SettingEntry setting : module.settings) {
                        if (!setting.visible.getAsBoolean()) continue;
                        if (mouseX >= setting.x && mouseX <= setting.x + setting.width
                                && mouseY >= setting.y && mouseY <= setting.y + 14) {
                            float newVal = (float)((mouseX - (setting.x + 2)) / (setting.width - 4));
                            newVal = Math.max(0f, Math.min(1f, newVal));
                            setting.value = newVal;
                            setting.onChange.accept(newVal);
                            draggingSetting = setting;
                            return true;
                        }
                    }

                    for (BooleanSetting bool : module.booleans) {
                        if (!bool.visible.getAsBoolean()) continue;
                        if (mouseX >= bool.x && mouseX <= bool.x + bool.width
                                && mouseY >= bool.y && mouseY <= bool.y + bool.height) {
                            if (button == 0) {
                                bool.value = !bool.value;
                                bool.onChange.accept(bool.value);
                                return true;
                            }
                        }
                    }

                    for (ModeSetting mode : module.modes) {
                        if (!mode.visible.getAsBoolean()) continue;
                        if (mouseX >= mode.x && mouseX <= mode.x + mode.width
                                && mouseY >= mode.y && mouseY <= mode.y + mode.height) {
                            if (button == 0) {
                                mode.index = (mode.index + 1) % mode.options.length;
                                mode.onChange.accept(mode.index);
                                return true;
                            }
                        }
                    }

                    for (ColorSetting colorSetting : module.colors) {
                        if (!colorSetting.visible.getAsBoolean()) continue;
                        int swatchX = colorSetting.x + colorSetting.width - 14;
                        int swatchY = colorSetting.y - 1;

                        if (mouseX >= swatchX && mouseX <= swatchX + 10
                                && mouseY >= swatchY && mouseY <= swatchY + 10) {
                            if (button == 0) {
                                colorSetting.open = !colorSetting.open;
                                return true;
                            }
                        }

                        if (colorSetting.open) {
                            int gridSize = 6;
                            int cell = 10;
                            int gridX = swatchX - (gridSize * cell) + 10;
                            int gridY = swatchY + 12;
                            if (mouseX >= gridX && mouseX <= gridX + gridSize * cell
                                    && mouseY >= gridY && mouseY <= gridY + gridSize * cell) {
                                int gx = (int)((mouseX - gridX) / cell);
                                int gy = (int)((mouseY - gridY) / cell);
                                int rgb = hsvToRgb((gx / (float) gridSize) * 360f, 1.0f,
                                        1.0f - (gy / (float) gridSize) * 0.8f);
                                colorSetting.color = rgb;
                                colorSetting.onChange.accept(rgb);
                                colorSetting.open = false;
                                return true;
                            }
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
            float newVal = (float)((click.x() - (draggingSetting.x + 2)) / (draggingSetting.width - 4));
            newVal = Math.max(0f, Math.min(1f, newVal));
            draggingSetting.value = newVal;
            draggingSetting.onChange.accept(newVal);
            return true;
        }
        return super.mouseDragged(click, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (draggingSetting != null) {
            draggingSetting = null;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            }
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ENTER) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char codePoint = (char) event.codepoint();
        if (codePoint >= 32 && codePoint < 127) {
            searchQuery += codePoint;
            return true;
        }
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
            case "AutoTotem" -> BiwerVisualsClient.CONFIG.autoTotemEnabled = state;
            case "HUD" -> BiwerVisualsClient.CONFIG.hudEnabled = state;
            case "FullBright" -> BiwerVisualsClient.CONFIG.fullBrightEnabled = state;
            case "Crosshair" -> BiwerVisualsClient.CONFIG.crosshairEnabled = state;
            case "SwingAnimation" -> BiwerVisualsClient.CONFIG.swingAnimationEnabled = state;
            case "SeeInvisibles" -> BiwerVisualsClient.CONFIG.seeInvisiblesEnabled = state;
            case "ChinaHat" -> BiwerVisualsClient.CONFIG.chinaHatEnabled = state;
        }
    }

    private static int hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = v - c;
        float r, g, b;
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        int ri = Math.round((r + m) * 255);
        int gi = Math.round((g + m) * 255);
        int bi = Math.round((b + m) * 255);
        return (ri << 16) | (gi << 8) | bi;
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