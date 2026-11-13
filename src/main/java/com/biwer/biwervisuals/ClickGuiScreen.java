package com.biwer.biwervisuals;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ClickGuiScreen extends Screen {
    private final Screen parent;

    private static final int CARD_HEIGHT = 42;
    private static final List<String> VISUAL_CATALOG = List.of(
            "CustomSky", "SkyChanger", "Fog", "CustomFog", "VolumetricFog",
            "CloudChanger", "AuroraSky", "GalaxySky", "WeatherChanger", "WorldColor",
            "CustomShaders", "Bloom", "MotionBlur", "DepthOfField", "LensFlare",
            "GodRays", "Vignette", "ChromaticAberration", "JumpCircles", "JumpParticles",
            "LandingCircle", "MotionTrail", "HitParticles", "DamageIndicator", "HitMarker",
            "TargetESP", "PlayerESP", "CrystalESP", "ItemESP", "Tracers",
            "ChinaHat", "PlayerGlow", "PlayerChams", "CustomCape", "AmbientParticles",
            "Fireflies", "SnowParticles", "DynamicLights", "ItemPhysics", "TotemAnimation");
    private static final Style MENU_FONT = Style.EMPTY.withFont(
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("biwervisuals", "title")));
    private final int activeColor = 0xFFC5A1FF;
    private final int textColor = 0xFFFFFFFF;
    private final int textMuted = 0xFFB8ACC9;
    private final int sliderBg = 0xFF30233F;

    private String searchQuery = "";
    private Category selected;
    private int contentLeft, contentTop, contentRight, contentBottom;
    private int scroll, maxScroll;
    private boolean searchFocused;
    private final List<Category> categories = new ArrayList<>();
    private SettingEntry draggingSetting = null;

    private float openAnim;
    private float tabAnim = 1f;
    private float displayedScroll;
    private long lastFrame = System.nanoTime();
    private boolean closing;
    private record ClickRipple(float x, float y, long start) {}
    private final java.util.ArrayDeque<ClickRipple> ripples = new java.util.ArrayDeque<>();

    private static class Category {
        String name;
        List<ModuleEntry> modules = new ArrayList<>();
        int x, y, width;
        Category(String name) { this.name = name; }
    }

    private static class ModuleEntry {
        String name;
        boolean enabled;
        boolean available = true;
        boolean expanded = false;
        int x, y, width, height = CARD_HEIGHT;
        float animState = 0.0f;
        int settingsBottom;
        float expansion;
        List<SettingEntry> settings = new ArrayList<>();
        List<BooleanSetting> booleans = new ArrayList<>();
        List<ModeSetting> modes = new ArrayList<>();
        List<ColorSetting> colors = new ArrayList<>();

        ModuleEntry(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
            this.animState = enabled ? 1f : 0f;
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
        MenuSounds.play("ClickGuiOpen");
        selected = categories.getFirst();
    }

    private void initModules() {
        // ================= COMBAT =================
        Category combat = new Category("Combat");

        ModuleEntry aura = new ModuleEntry("Aura", BiwerVisualsClient.CONFIG.auraEnabled);
        aura.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.auraRange - 0.1f) / 5.9f, v -> BiwerVisualsClient.CONFIG.auraRange = 0.1f + v * 5.9f));
        aura.settings.add(new SettingEntry("Доп. дистанция", BiwerVisualsClient.CONFIG.auraExtraRange / 3f, v -> BiwerVisualsClient.CONFIG.auraExtraRange = v * 3f));
        aura.modes.add(new ModeSetting("Наводка", new String[]{"ФанТайм", "ФанТайм ФОВ", "Легит"}, BiwerVisualsClient.CONFIG.auraAimMode, v -> BiwerVisualsClient.CONFIG.auraAimMode = v));
        aura.modes.add(new ModeSetting("Приоритет", new String[]{"Прицел", "Дистанция", "ХП"}, BiwerVisualsClient.CONFIG.auraPriority, v -> BiwerVisualsClient.CONFIG.auraPriority = v));
        aura.settings.add(new SettingEntry("Плавность", BiwerVisualsClient.CONFIG.auraSmooth / 100f, v -> BiwerVisualsClient.CONFIG.auraSmooth = v * 100f));
        aura.settings.add(new SettingEntry("Скорость", BiwerVisualsClient.CONFIG.auraSpeed / 30f, v -> BiwerVisualsClient.CONFIG.auraSpeed = v * 30f));
        aura.settings.add(new SettingEntry("FOV", (BiwerVisualsClient.CONFIG.auraFov - 10f) / 170f, v -> BiwerVisualsClient.CONFIG.auraFov = 10f + v * 170f));
        aura.booleans.add(new BooleanSetting("Игроки", BiwerVisualsClient.CONFIG.auraPlayers, v -> BiwerVisualsClient.CONFIG.auraPlayers = v));
        aura.booleans.add(new BooleanSetting("Мобы", BiwerVisualsClient.CONFIG.auraMobs, v -> BiwerVisualsClient.CONFIG.auraMobs = v));
        aura.booleans.add(new BooleanSetting("Животные", BiwerVisualsClient.CONFIG.auraAnimals, v -> BiwerVisualsClient.CONFIG.auraAnimals = v));
        aura.booleans.add(new BooleanSetting("Без брони", BiwerVisualsClient.CONFIG.auraNaked, v -> BiwerVisualsClient.CONFIG.auraNaked = v));
        aura.booleans.add(new BooleanSetting("Криты", BiwerVisualsClient.CONFIG.auraOnlyCriticals, v -> BiwerVisualsClient.CONFIG.auraOnlyCriticals = v));
        aura.booleans.add(new BooleanSetting("Адаптивные прыжки", BiwerVisualsClient.CONFIG.auraAdaptiveHits, v -> BiwerVisualsClient.CONFIG.auraAdaptiveHits = v));
        aura.booleans.add(new BooleanSetting("Щитолом", BiwerVisualsClient.CONFIG.auraShieldBreaker, v -> BiwerVisualsClient.CONFIG.auraShieldBreaker = v));
        aura.booleans.add(new BooleanSetting("Смарт-спринт", BiwerVisualsClient.CONFIG.auraSmartSprint, v -> BiwerVisualsClient.CONFIG.auraSmartSprint = v));
        aura.booleans.add(new BooleanSetting("Не бить при использовании", BiwerVisualsClient.CONFIG.auraDontHitItemUse, v -> BiwerVisualsClient.CONFIG.auraDontHitItemUse = v));
        aura.booleans.add(new BooleanSetting("Не бить в контейнере", BiwerVisualsClient.CONFIG.auraDontHitContainer, v -> BiwerVisualsClient.CONFIG.auraDontHitContainer = v));
        aura.booleans.add(new BooleanSetting("Не бить через стены", BiwerVisualsClient.CONFIG.auraDontHitThroughWalls, v -> BiwerVisualsClient.CONFIG.auraDontHitThroughWalls = v));
        combat.modules.add(aura);

        ModuleEntry triggerBot = new ModuleEntry("TriggerBot", BiwerVisualsClient.CONFIG.triggerBotEnabled);
        triggerBot.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.triggerBotRange - 0.5f) / 4.5f, v -> BiwerVisualsClient.CONFIG.triggerBotRange = 0.5f + v * 4.5f));
        combat.modules.add(triggerBot);

        ModuleEntry aimAssist = new ModuleEntry("AimAssistant", BiwerVisualsClient.CONFIG.aimAssistEnabled);
        aimAssist.settings.add(new SettingEntry("Сила", BiwerVisualsClient.CONFIG.aimAssistStrength / 100f, v -> BiwerVisualsClient.CONFIG.aimAssistStrength = v * 100f));
        combat.modules.add(aimAssist);

        ModuleEntry velocity = new ModuleEntry("Velocity", BiwerVisualsClient.CONFIG.velocityEnabled);
        velocity.settings.add(new SettingEntry("Сила", BiwerVisualsClient.CONFIG.velocityPercent / 100f, v -> BiwerVisualsClient.CONFIG.velocityPercent = v * 100f));
        velocity.modes.add(new ModeSetting("Режим", new String[]{"Обычный", "Легитный"}, BiwerVisualsClient.CONFIG.velocityMode, v -> BiwerVisualsClient.CONFIG.velocityMode = v));
        combat.modules.add(velocity);

        categories.add(combat);

        // ================= MOVEMENT =================
        Category movement = new Category("Movement");
        movement.modules.add(new ModuleEntry("Fly", BiwerVisualsClient.CONFIG.flyEnabled));
        movement.modules.add(new ModuleEntry("Sprint", BiwerVisualsClient.CONFIG.sprintEnabled));

        ModuleEntry speed = new ModuleEntry("Speed", BiwerVisualsClient.CONFIG.speedEnabled);
        speed.settings.add(new SettingEntry("Буст", (BiwerVisualsClient.CONFIG.speedPercent - 5f) / 55f, v -> BiwerVisualsClient.CONFIG.speedPercent = 5f + v * 55f));
        speed.booleans.add(new BooleanSetting("Только спринт", BiwerVisualsClient.CONFIG.speedOnlySprint, v -> BiwerVisualsClient.CONFIG.speedOnlySprint = v));
        speed.booleans.add(new BooleanSetting("Плавно", BiwerVisualsClient.CONFIG.speedSmooth, v -> BiwerVisualsClient.CONFIG.speedSmooth = v));
        movement.modules.add(speed);

        ModuleEntry bunnyHop = new ModuleEntry("BunnyHop", BiwerVisualsClient.CONFIG.bunnyHopEnabled);
        bunnyHop.settings.add(new SettingEntry("Задержка", BiwerVisualsClient.CONFIG.bhopJumpDelay / 20f, v -> BiwerVisualsClient.CONFIG.bhopJumpDelay = (int)(v * 20f)));
        bunnyHop.booleans.add(new BooleanSetting("Только в движении", BiwerVisualsClient.CONFIG.bhopOnlyWhileMoving, v -> BiwerVisualsClient.CONFIG.bhopOnlyWhileMoving = v));
        movement.modules.add(bunnyHop);

        ModuleEntry targetStrafe = new ModuleEntry("TargetStrafe", BiwerVisualsClient.CONFIG.targetStrafeEnabled);
        targetStrafe.settings.add(new SettingEntry("Дистанция", (BiwerVisualsClient.CONFIG.strafeRange - 0.5f) / 3.5f, v -> BiwerVisualsClient.CONFIG.strafeRange = 0.5f + v * 3.5f));
        targetStrafe.booleans.add(new BooleanSetting("Авто-прыжок", BiwerVisualsClient.CONFIG.strafeAutoJump, v -> BiwerVisualsClient.CONFIG.strafeAutoJump = v));
        movement.modules.add(targetStrafe);



        // ================= RENDER =================
        Category render = new Category("Visuals");

        ModuleEntry ambience = new ModuleEntry("Ambience", BiwerVisualsClient.CONFIG.ambienceEnabled);
        ambience.modes.add(new ModeSetting("Время", new String[]{"Реальное", "Рассвет", "День", "Ночь", "Полночь"}, BiwerVisualsClient.CONFIG.activeTimeButton, v -> BiwerVisualsClient.CONFIG.activeTimeButton = v));
        ambience.modes.add(new ModeSetting("Погода", new String[]{"Реальная", "Ясно", "Дождь", "Гроза"}, BiwerVisualsClient.CONFIG.activeWeatherButton, v -> BiwerVisualsClient.CONFIG.activeWeatherButton = v));
        render.modules.add(ambience);

        ModuleEntry viewmodel = new ModuleEntry("Viewmodel", BiwerVisualsClient.CONFIG.viewmodelEnabled);

        // Смещения основной руки
        viewmodel.settings.add(new SettingEntry("Main X", (BiwerVisualsClient.CONFIG.vmMainX + 2f) / 4f, v -> BiwerVisualsClient.CONFIG.vmMainX = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Main Y", (BiwerVisualsClient.CONFIG.vmMainY + 2f) / 4f, v -> BiwerVisualsClient.CONFIG.vmMainY = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Main Z", (BiwerVisualsClient.CONFIG.vmMainZ + 2f) / 4f, v -> BiwerVisualsClient.CONFIG.vmMainZ = v * 4f - 2f));

        // Повороты основной руки (до 360 градусов)
        viewmodel.settings.add(new SettingEntry("Rot Main X", (BiwerVisualsClient.CONFIG.vmRotMainX + 180f) / 360f, v -> BiwerVisualsClient.CONFIG.vmRotMainX = (v * 360f) - 180f));
        viewmodel.settings.add(new SettingEntry("Rot Main Y", (BiwerVisualsClient.CONFIG.vmRotMainY + 180f) / 360f, v -> BiwerVisualsClient.CONFIG.vmRotMainY = (v * 360f) - 180f));
        viewmodel.settings.add(new SettingEntry("Rot Main Z", (BiwerVisualsClient.CONFIG.vmRotMainZ + 180f) / 360f, v -> BiwerVisualsClient.CONFIG.vmRotMainZ = (v * 360f) - 180f));

        // Смещения второй руки (без поворотов)
        viewmodel.settings.add(new SettingEntry("Off X", (BiwerVisualsClient.CONFIG.vmOffX + 2f) / 4f, v -> BiwerVisualsClient.CONFIG.vmOffX = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Off Y", (BiwerVisualsClient.CONFIG.vmOffY + 2f) / 4f, v -> BiwerVisualsClient.CONFIG.vmOffY = v * 4f - 2f));
        viewmodel.settings.add(new SettingEntry("Off Z", (BiwerVisualsClient.CONFIG.vmOffZ + 2f) / 4f, v -> BiwerVisualsClient.CONFIG.vmOffZ = v * 4f - 2f));

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
        for (VisualEffects.Effect effect : VisualEffects.Effect.values()) {
            ModuleEntry entry = new ModuleEntry(effect.name(), VisualEffects.enabled(effect.name()));
            render.modules.add(entry);
        }

        chinaHat.available = false;
        render.modules.add(chinaHat);

        List<ModuleEntry> existingVisuals = List.copyOf(render.modules);
        render.modules.clear();
        for (String name : VISUAL_CATALOG) {
            ModuleEntry entry = existingVisuals.stream().filter(module -> module.name.equals(name))
                    .findFirst().orElseGet(() -> new ModuleEntry(name, false));
            if (!existingVisuals.contains(entry)) entry.available = false;
            if (entry.available) addVisualSettings(entry);
            render.modules.add(entry);
        }
        for (ModuleEntry module : existingVisuals) {
            if (!VISUAL_CATALOG.contains(module.name)) render.modules.add(module);
        }

        categories.add(render);

        // ================= PLAYER =================
        Category player = new Category("Player");
        ModuleEntry autoTotem = new ModuleEntry("AutoTotem", BiwerVisualsClient.CONFIG.autoTotemEnabled);
        autoTotem.settings.add(new SettingEntry("Задержка", (BiwerVisualsClient.CONFIG.autoTotemDelay - 1f) / 19f, v -> BiwerVisualsClient.CONFIG.autoTotemDelay = 1 + (int)(v * 19f)));
        player.modules.add(autoTotem);
        player.modules.add(new ModuleEntry("HUD", BiwerVisualsClient.CONFIG.hudEnabled));


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

        // НОВАЯ НАСТРОЙКА: Смещение по Z
        // От 0.0f (рука у лица) до -1.5f (рука далеко)
        swing.settings.add(new SettingEntry("Смещение Z", (BiwerVisualsClient.CONFIG.swingZOffset + 1.5f) / 1.5f,
                v -> BiwerVisualsClient.CONFIG.swingZOffset = (v * 1.5f) - 1.5f));

        swing.settings.add(new SettingEntry("Угол", BiwerVisualsClient.CONFIG.swingAngle / 360f, v -> BiwerVisualsClient.CONFIG.swingAngle = v * 360f, () -> BiwerVisualsClient.CONFIG.swingAnimationMode == 0));
        swing.settings.add(new SettingEntry("Наклон", (BiwerVisualsClient.CONFIG.swingTipTilt + 90f) / 180f, v -> BiwerVisualsClient.CONFIG.swingTipTilt = v * 180f - 90f, () -> BiwerVisualsClient.CONFIG.swingAnimationMode != 4));
        swing.settings.add(new SettingEntry("Интенсивность", (BiwerVisualsClient.CONFIG.swingPower - 1f) / 9f, v -> BiwerVisualsClient.CONFIG.swingPower = 1f + v * 9f));
        misc.modules.add(swing);

        misc.modules.add(new ModuleEntry("SeeInvisibles", BiwerVisualsClient.CONFIG.seeInvisiblesEnabled));
        categories.add(misc);
        categories.add(player);
        categories.add(movement);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float seconds = Math.min(0.05f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        openAnim = approach(openAnim, closing ? 0f : 1f, seconds, 20f);
        tabAnim = approach(tabAnim, 1f, seconds, 20f);
        if (closing && openAnim < 0.05f) {
            this.minecraft.setScreen(parent);
            return;
        }
        graphics.fillGradient(0, 0, width, height, (Math.round(74 * openAnim) << 24) | 0x170B29, (Math.round(117 * openAnim) << 24) | 0x10091D);
        int panelWidth = Math.min(width - 12, Math.max(340, width * 64 / 100));
        int panelHeight = Math.min(height - 12, Math.max(170, height * 64 / 100));
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2 + Math.round((1f - openAnim) * 14);
        int sidebar = Math.min(150, Math.max(94, panelWidth * 28 / 100));
        contentLeft = left + sidebar + 10;
        contentRight = left + panelWidth - 10;
        contentTop = top + 30;
        contentBottom = top + panelHeight - 10;

        glass(graphics, left, top, panelWidth, panelHeight, 20, 0xC2181026);
        glass(graphics, left, top, sidebar, panelHeight, 20, 0xCF231735);
        drawRoundedRect(graphics, left + 8, top + 8, 24, 24, 12, 0xFF7345AE);
        graphics.blit(Identifier.fromNamespaceAndPath("biwervisuals", "textures/menu/logo.png"),
                left + 13, top + 13, left + 27, top + 27, 0f, 1f, 0f, 1f);
        label(graphics, "BiwerVisuals", left + 37, top + 16, 1f, textColor);

        int rowHeight = Math.min(30, (panelHeight - 48) / categories.size());
        String[] symbols = {"⚔", "◉", "△", "♙", "➜"};
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            cat.x = left + 7;
            cat.y = top + 43 + i * rowHeight;
            cat.width = sidebar - 14;
            if (cat == selected || inside(mouseX, mouseY, cat.x, cat.y, cat.width, rowHeight - 2)) {
                drawRoundedRect(graphics, cat.x, cat.y, cat.width, rowHeight - 2, 6,
                        cat == selected ? 0xC65E3B88 : 0x70402B59);
            }
            int textY = cat.y + Math.max(2, (rowHeight - 11) / 2);
            label(graphics, symbols[i], cat.x + 5, textY, 1.2f, activeColor);
            label(graphics, cat.name, cat.x + 23, textY, 1.2f, textColor);
        }

        int searchWidth = contentRight - contentLeft;
        drawRoundedRect(graphics, contentLeft, top + 8, searchWidth, 15, 5,
                searchFocused ? 0xFF43305F : 0xFF1D142C);
        String query = searchQuery.isEmpty() ? "Поиск модулей" : searchQuery;
        text(graphics, fit(query, searchWidth - 12), contentLeft + 6, top + 12, textMuted);

        List<ModuleEntry> modules = selected.modules.stream().filter(this::matchesSearch).toList();
        int columns = searchWidth >= 200 ? 2 : 1;
        int cardWidth = (searchWidth - 8 * (columns - 1)) / columns;
        for (ModuleEntry module : modules) {
            module.expansion = approach(module.expansion, module.expanded ? 1f : 0f, seconds, 22f);
        }
        int totalHeight = 0;
        for (int i = 0; i < modules.size(); i += columns) {
            int row = CARD_HEIGHT;
            for (int j = i; j < Math.min(i + columns, modules.size()); j++) {
                row = Math.max(row, CARD_HEIGHT + settingsHeight(modules.get(j)));
            }
            totalHeight += row + 8;
        }
        maxScroll = Math.max(0, totalHeight - 8 - (contentBottom - contentTop));
        scroll = Math.min(scroll, maxScroll);
        graphics.enableScissor(contentLeft, contentTop, contentRight, contentBottom);
        displayedScroll = approach(displayedScroll, scroll, seconds, 24f);
        displayedScroll = Math.min(displayedScroll, maxScroll);
        int y = contentTop - Math.round(displayedScroll) + Math.round((1f - tabAnim) * 10);
        for (int i = 0; i < modules.size(); i += columns) {
            int row = CARD_HEIGHT;
            for (int j = i; j < Math.min(i + columns, modules.size()); j++) {
                ModuleEntry module = modules.get(j);
                module.x = contentLeft + (j - i) * (cardWidth + 8);
                module.y = y;
                module.width = cardWidth;
                int height = CARD_HEIGHT + settingsHeight(module);
                module.settingsBottom = y + height;
                row = Math.max(row, height);
                if (y + height > contentTop && y < contentBottom) renderModule(graphics, module, seconds);
            }
            y += row + 8;
        }
        graphics.disableScissor();
        long rippleTime = System.nanoTime();
        ripples.removeIf(ripple -> rippleTime - ripple.start() > 400_000_000L);
        for (ClickRipple ripple : ripples) {
            float progress = (rippleTime - ripple.start()) / 400_000_000f;
            int radius = Math.round(3 + 15 * (1 - (1 - progress) * (1 - progress)));
            int color = (Math.round(55 * (1 - progress)) << 24) | 0xD4B5FF;
            drawRoundedRect(graphics, Math.round(ripple.x()) - radius, Math.round(ripple.y()) - radius,
                    radius * 2, radius * 2, radius, color);
        }
        if (modules.isEmpty()) text(graphics, "Модули не найдены", contentLeft + 5, contentTop + 6, textMuted);
    }

    private void addVisualSettings(ModuleEntry entry) {
        String name = entry.name;
        if (name.equals("TargetESP")) {
            entry.modes.add(new ModeSetting("Текстура", new String[]{"Soft", "Cross", "Cube", "Capture", "Diamond", "Delta"},
                    Math.clamp(BiwerVisualsClient.CONFIG.targetTexture, 0, 5),
                    v -> BiwerVisualsClient.CONFIG.targetTexture = v));
            entry.settings.add(new SettingEntry("Размер", BiwerVisualsClient.CONFIG.targetSize,
                    v -> BiwerVisualsClient.CONFIG.targetSize = v));
            entry.booleans.add(new BooleanSetting("Перелив цвета", BiwerVisualsClient.CONFIG.targetAnimatedColor,
                    v -> BiwerVisualsClient.CONFIG.targetAnimatedColor = v));
            entry.colors.add(new ColorSetting("Цвет", BiwerVisualsClient.CONFIG.targetColor,
                    v -> BiwerVisualsClient.CONFIG.targetColor = v));
        }
        if (name.equals("WeatherChanger")) {
            entry.modes.add(new ModeSetting("Погода", new String[]{"Ясно", "Дождь", "Гроза"},
                    BiwerVisualsClient.CONFIG.visualPalette.getOrDefault(name, 1),
                    value -> BiwerVisualsClient.CONFIG.visualPalette.put(name, value)));
            entry.settings.add(new SettingEntry("Сила осадков", VisualEffects.strength(name),
                    value -> BiwerVisualsClient.CONFIG.visualStrength.put(name, value)));
            return;
        }
        entry.settings.add(new SettingEntry("Интенсивность", VisualEffects.strength(name),
                value -> BiwerVisualsClient.CONFIG.visualStrength.put(name, value)));
        entry.modes.add(new ModeSetting("Палитра", new String[]{"Северное сияние", "Неон", "Закат", "Лёд"},
                BiwerVisualsClient.CONFIG.visualPalette.getOrDefault(name, 0),
                value -> BiwerVisualsClient.CONFIG.visualPalette.put(name, value)));
    }

    private boolean matchesSearch(ModuleEntry module) {
        return module.name.toLowerCase(java.util.Locale.ROOT).contains(searchQuery.toLowerCase(java.util.Locale.ROOT));
    }

    private int settingsHeight(ModuleEntry module) {
        if (module.expansion == 0f) return 0;
        int height = 4;
        for (SettingEntry s : module.settings) if (s.visible.getAsBoolean()) height += 16;
        for (BooleanSetting b : module.booleans) if (b.visible.getAsBoolean()) height += 14;
        for (ModeSetting m : module.modes) if (m.visible.getAsBoolean()) height += 14;
        for (ColorSetting c : module.colors) if (c.visible.getAsBoolean()) height += 14;
        return Math.round(height * module.expansion);
    }

    private void renderModule(GuiGraphicsExtractor graphics, ModuleEntry module, float seconds) {
        glass(graphics, module.x, module.y, module.width, module.settingsBottom - module.y, 8, 0xAE302245);
        module.animState = approach(module.animState, module.enabled ? 1f : 0f, seconds, 24f);
        if (!module.available) {
            label(graphics, fit(module.name, (module.width - 16) * 5 / 7),
                    module.x + 8, module.y + 9, 1.4f, textMuted);
            text(graphics, "Не реализовано", module.x + 8, module.y + 26, textMuted);
            return;
        }
        int toggleX = module.x + module.width - 36;
        int toggleY = module.y + 8;
        drawRoundedRect(graphics, toggleX, toggleY, 29, 12, 6, module.enabled ? 0xFF9B6CE2 : 0xFF4B3A60);
        drawRoundedRect(graphics, toggleX + 1 + Math.round(12 * module.animState), toggleY + 1, 15, 10, 5, textColor);
        label(graphics, fit(module.name, (module.width - 48) * 5 / 7),
                module.x + 8, module.y + 9, 1.4f, textColor);
        if (!module.settings.isEmpty() || !module.booleans.isEmpty() || !module.modes.isEmpty() || !module.colors.isEmpty()) {
            text(graphics, module.expanded ? "ПКМ · свернуть" : "ПКМ · настройки", module.x + 8, module.y + 26, textMuted);
        }

        if (module.settingsBottom > module.y + CARD_HEIGHT) {
            graphics.enableScissor(module.x, module.y + CARD_HEIGHT,
                    module.x + module.width, module.settingsBottom);
            int settingsY = module.y + module.height + 2;

            for (SettingEntry s : module.settings) {
                if (!s.visible.getAsBoolean()) continue;
                s.x = module.x + 2; s.y = settingsY; s.width = module.width - 4;
                text(graphics, s.name, s.x + 2, s.y, textMuted);
                graphics.fill(s.x + 2, s.y + 8, s.x + s.width - 2, s.y + 11, fade(sliderBg));
                graphics.fill(s.x + 2, s.y + 8, s.x + 2 + (int)((s.width - 4) * s.value), s.y + 11, fade(activeColor));
                settingsY += 16;
            }
            for (BooleanSetting b : module.booleans) {
                if (!b.visible.getAsBoolean()) continue;
                b.x = module.x + 2; b.y = settingsY; b.width = module.width - 4;
                text(graphics, b.name, b.x + 2, b.y, textMuted);
                drawRoundedRect(graphics, b.x + b.width - 14, b.y - 1, 10, 10, 3, sliderBg);
                if (b.value) drawRoundedRect(graphics, b.x + b.width - 12, b.y + 1, 6, 6, 2, activeColor);
                settingsY += 14;
            }
            for (ModeSetting m : module.modes) {
                if (!m.visible.getAsBoolean()) continue;
                m.x = module.x + 2; m.y = settingsY; m.width = module.width - 4;
                text(graphics, m.name + ": " + m.current(), m.x + 2, m.y, activeColor);
                settingsY += 14;
            }
            for (ColorSetting c : module.colors) {
                if (!c.visible.getAsBoolean()) continue;
                c.x = module.x + 2; c.y = settingsY; c.width = module.width - 4;
                text(graphics, c.name, c.x + 2, c.y, textMuted);
                graphics.fill(c.x + c.width - 14, c.y - 1, c.x + c.width - 4, c.y + 9, fade(0xFF000000 | c.color));
                settingsY += 14;
            }
            graphics.disableScissor();
        }
    }

    private static float approach(float current, float target, float seconds, float rate) {
        float next = target + (current - target) * (float) Math.exp(-rate * seconds);
        return Math.abs(next - target) < 0.002f ? target : next;
    }

    private String fit(String value, int width) {
        return AtlasFont.MEDIUM.fit(value, 10, width);
    }

    private void text(GuiGraphicsExtractor graphics, String value, int x, int y, int color) {
        AtlasFont.MEDIUM.draw(graphics, value, x, y - 1, 10, fade(color));
    }
    private void label(GuiGraphicsExtractor graphics, String text, int x, int y, float scale, int color) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale, scale);
        text(graphics, text, 0, 0, color);
        pose.popMatrix();
    }

    private void glass(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int radius, int color) {
        drawRoundedRect(graphics, x, y, w, h, radius, 0x805F457E);
        drawRoundedRect(graphics, x + 1, y + 1, w - 2, h - 2, radius - 1, color);
        // A narrow highlight and a broad translucent reflection follow the panel curvature.
        int band = Math.min(9, h / 3);
        for (int row = 1; row < band; row++) {
            int inset = Math.max(2, radius - row);
            int alpha = Math.round((1f - row / (float) band) * 22 * openAnim);
            graphics.fill(x + inset, y + row, x + w - inset, y + row + 1,
                    (alpha << 24) | 0xE3D5FF);
        }
    }

    private void drawRoundedRect(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int r, int color) {
        color = fade(color);
        r = Math.min(r, Math.min(w, h) / 2);
        graphics.fill(x, y + r, x + w, y + h - r, color);
        for (int row = 0; row < r; row++) {
            double dy = r - row - 0.5;
            double edge = r - Math.sqrt(r * r - dy * dy);
            int inset = (int) Math.ceil(edge);
            if (inset > 0) {
                int rim = (Math.round((color >>> 24) * (float) (inset - edge)) << 24) | (color & 0xFFFFFF);
                graphics.fill(x + inset - 1, y + row, x + inset, y + row + 1, rim);
                graphics.fill(x + w - inset, y + row, x + w - inset + 1, y + row + 1, rim);
                graphics.fill(x + inset - 1, y + h - row - 1, x + inset, y + h - row, rim);
                graphics.fill(x + w - inset, y + h - row - 1, x + w - inset + 1, y + h - row, rim);
            }
            graphics.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            graphics.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
    }

    private int fade(int color) {
        return (color & 0xFFFFFF) | (Math.round((color >>> 24) * openAnim) << 24);
    }

    private static boolean inside(double x, double y, int left, int top, int w, int h) {
        return x >= left && x < left + w && y >= top && y < top + h;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (inside(x, y, contentLeft, contentTop, contentRight - contentLeft, contentBottom - contentTop)) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.round(vertical * 24)));
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (ripples.size() >= 8) ripples.removeFirst();
        ripples.addLast(new ClickRipple((float) mouseX, (float) mouseY, System.nanoTime()));
        int button = click.button();

        if (closing) return true;
        searchFocused = button == 0 && inside(mouseX, mouseY, contentLeft, contentTop - 22,
                contentRight - contentLeft, 15);
        if (searchFocused) return true;
        int rowHeight = categories.get(1).y - categories.getFirst().y;
        for (Category cat : categories) {
            if (button == 0 && inside(mouseX, mouseY, cat.x, cat.y, cat.width, rowHeight - 2)) {
                if (selected == cat) return true;
                selected = cat;
                tabAnim = 0f;
                displayedScroll = 0f;
                scroll = 0;
                return true;
            }
        }
        if (!inside(mouseX, mouseY, contentLeft, contentTop, contentRight - contentLeft, contentBottom - contentTop)) {
            return super.mouseClicked(click, doubled);
        }
        for (ModuleEntry module : selected.modules) {
            if (!module.available || !matchesSearch(module)) continue;
            if (mouseX >= module.x && mouseX <= module.x + module.width && mouseY >= module.y && mouseY <= module.y + module.height) {
                if (button == 0) { module.enabled = !module.enabled; MenuSounds.play(module.enabled ? "FunctionOn" : "FunctionOff"); updateModuleConfig(module.name, module.enabled); return true; }
                if (button == 1) { module.expanded = !module.expanded; return true; }
            }
            if (module.expanded && mouseY < module.settingsBottom && mouseY >= module.y + module.height) {
                for (SettingEntry s : module.settings) {
                    if (!s.visible.getAsBoolean()) continue;
                    if (button == 0 && mouseX >= s.x && mouseX <= s.x + s.width && mouseY >= s.y && mouseY <= s.y + 14) {
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
                        if (button == 0 || button == 1) {
                            float[] hsv = java.awt.Color.RGBtoHSB(c.color >> 16, (c.color >> 8) & 255, c.color & 255, null);
                            float hue = (hsv[0] + (button == 0 ? 1f / 12 : 11f / 12)) % 1f;
                            c.color = java.awt.Color.HSBtoRGB(hue, Math.max(0.55f, hsv[1]), Math.max(0.65f, hsv[2])) & 0xFFFFFF;
                            c.onChange.accept(c.color);
                            return true;
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
        if (event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            onClose();
            return true;
        }
        if (searchFocused && event.key() == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.offsetByCodePoints(searchQuery.length(), -1));
                scroll = 0;
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchFocused && !Character.isISOControl(event.codepoint()) && searchQuery.length() < 48) {
            searchQuery += Character.toString(event.codepoint());
            scroll = 0;
            return true;
        }
        return super.charTyped(event);
    }

    private void updateModuleConfig(String name, boolean state) {
        switch (name) {
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
            default -> VisualEffects.toggle(name, state);
            case "SeeInvisibles" -> BiwerVisualsClient.CONFIG.seeInvisiblesEnabled = state;
        }
    }

    @Override
    public void onClose() {
        if (!closing) MenuSounds.play("ClickGuiClose");
        closing = true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}