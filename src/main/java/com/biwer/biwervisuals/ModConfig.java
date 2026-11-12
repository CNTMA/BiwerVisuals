package com.biwer.biwervisuals;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    public boolean ambienceEnabled = false;
    public int activeTimeButton = 0;
    public int activeWeatherButton = 0;

    // --- Viewmodel ---
    public boolean viewmodelEnabled = false;
    public boolean vmPos = false;
    public float vmPosX = 0.0f;
    public float vmPosY = 0.0f;
    public float vmPosZ = 0.0f;

    public boolean vmRot = false;
    public float vmRotX = 0.0f;
    public float vmRotY = 0.0f;
    public float vmRotZ = 0.0f;

    public boolean vmScale = false;
    public float vmScaleX = 1.0f;
    public float vmScaleY = 1.0f;
    public float vmScaleZ = 1.0f;
    // Состояние свернутости блоков (false = развернут)
    public boolean ambienceCollapsed = false;
    public boolean viewmodelCollapsed = true; // По умолчанию Viewmodel свернут, чтобы не загромождать
    // --- KillAura ---
    public boolean killAuraEnabled = false;
    public boolean killAuraCollapsed = true;
    public float killAuraRange = 3.0f;
    public float killAuraExtraRange = 0.5f;

    // --- KillAura: плавность наводки ---
    public float killAuraSmooth = 35.0f;  // % остатка угла за тик
    public float killAuraSpeed = 12.0f;   // максимум градусов за тик
    public float killAuraFov = 120.0f;    // угол атаки в градусах

    // Цели
    public boolean targetPlayers = true;
    public boolean targetMobs = false;
    public boolean targetAnimals = false;
    public boolean targetFriends = false;
    public boolean targetNaked = true; // Без брони

    // Настройки
    public boolean onlyCriticals = true;
    public boolean adaptiveHits = true;
    public boolean shieldBreaker = true;
    public boolean smartSprint = false;

    // Не бить когда
    public boolean dontHitItemUse = true;
    public boolean dontHitContainer = true;
    public boolean dontHitThroughWalls = true;

    // --- KillAura ---
    public int aimMode = 1; // 0 = Свободно, 1 = Преследование, 2 = Наводка
    // --- Entity ESP ---
    public boolean espEnabled = false;
    public boolean espCollapsed = true;
    public boolean espPlayers = true;
    public boolean espMobs = true;
    public boolean espAnimals = false; // Те самые "мирные"
    public boolean espNaked = false;

    // --- Tags ---
    public boolean tagsEnabled = false;
    public boolean tagsCollapsed = true;
    public boolean tagsPlayers = true;
    public boolean tagsMobs = true;
    public boolean tagsAnimals = false;
    // Бинды: имя функции -> код клавиши (GLFW)
    public Map<String, Integer> binds = new HashMap<>();
    // --- Fly ---
    public boolean flyEnabled = false;
    // --- Sprint ---
    public boolean sprintEnabled = false;

    // --- Speed ---
    public boolean speedEnabled = false;
    public boolean speedCollapsed = true;
    public float speedPercent = 20.0f;
    public boolean speedOnlySprint = true;
    public boolean speedSmooth = true;
    public boolean speedSkipIfPotion = true;

    // --- BunnyHop ---
    public boolean bunnyHopEnabled = false;
    public boolean bunnyHopCollapsed = true;
    public boolean bhopAutoJump = true;
    public boolean bhopOnlyWhileMoving = true;
    public int bhopJumpDelay = 0;

    // --- TargetStrafe ---
    public boolean targetStrafeEnabled = false;
    public boolean targetStrafeCollapsed = true;
    public boolean strafeAutoJump = true;
    public float strafeRange = 1.0f;

    // --- CustomSky ---
    public boolean customSkyEnabled = false;
    public boolean customSkyCollapsed = true;
    public int skyMode = 0;
    public float skyStars = 50.0f;
    public float skySpeed = 50.0f;

    // --- CustomSky: северное сияние ---
    public boolean auroraEnabled = false;          // отдельный выключатель сияния
    public int auroraColorBottom = 0x3BF0A6;       // нижний слой (изумруд)
    public int auroraColorTop = 0x9A5CFF;          // верхний слой (фиолет)
    public float auroraBrightness = 70.0f;         // яркость, %

    // --- JumpCircles ---
    public boolean jumpCirclesEnabled = false;

    // --- HUD ---
    public boolean hudEnabled = false;
    public boolean hudCollapsed = true;
    public Map<String, float[]> hudPositions = new HashMap<>();

    // --- AutoTotem ---
    public boolean autoTotemEnabled = false;
    public boolean autoTotemCollapsed = true;
    public int autoTotemDelay = 4; // тиков между попытками (1 тик = 50 мс)

    // --- Aura (порт aethereal) ---
    public boolean auraEnabled = false;
    public boolean auraCollapsed = true;
    public float auraRange = 3.0f;
    public float auraExtraRange = 0.5f;
    public int auraAimMode = 0;          // 0 ФанТайм, 1 ФанТайм ФОВ, 2 Легит
    public int auraPriority = 0;         // 0 Прицел, 1 Дистанция, 2 ХП
    public boolean auraPlayers = true;
    public boolean auraMobs = false;
    public boolean auraAnimals = false;
    public boolean auraNaked = true;     // разрешить бить игроков без брони
    public boolean auraOnlyCriticals = true;
    public boolean auraAdaptiveHits = true;
    public boolean auraShieldBreaker = true;
    public boolean auraSmartSprint = false;
    public boolean auraDontHitItemUse = true;
    public boolean auraDontHitContainer = true;
    public boolean auraDontHitThroughWalls = true;
    public float auraFov = 120.0f;
    public float auraSpeed = 12.0f;
    public float auraSmooth = 35.0f;

    // --- TriggerBot ---
    public boolean triggerBotEnabled = false;
    public boolean triggerBotCollapsed = true;
    public float triggerBotRange = 3.0f;
    public boolean triggerBotWeaponOnly = true;
    public boolean triggerBotOnlyCriticals = false;
    public boolean triggerBotDontHitThroughWalls = false;
    public boolean triggerBotDontHitItemUse = true;
    public boolean triggerBotDontHitContainer = true;
    public boolean triggerBotPlayers = true;
    public boolean triggerBotMobs = false;
    public boolean triggerBotAnimals = false;

    // --- AimAssistant ---
    public boolean aimAssistEnabled = false;
    public boolean aimAssistCollapsed = true;
    public float aimAssistStrength = 35.0f;
    public boolean aimAssistThroughWalls = false;
    public boolean aimAssistWeaponOnly = true;
    public boolean aimAssistPlayers = true;
    public boolean aimAssistMobs = false;
    public boolean aimAssistAnimals = false;

    // --- Velocity ---
    public boolean velocityEnabled = false;
    public boolean velocityCollapsed = true;
    public int velocityMode = 0;          // 0 Обычный, 1 Легитный
    public float velocityPercent = 35.0f;

    // --- FullBright ---
    public boolean fullBrightEnabled = false;
    public boolean fullBrightCollapsed = true;
    public int fullBrightMode = 0;          // 0 Гамма, 1 Ночное зрение
    public float fullBrightGamma = 4.0f;

    // --- Crosshair ---
    public boolean crosshairEnabled = false;
    public boolean crosshairCollapsed = true;
    public float crosshairGap = 0.0f;
    public float crosshairLength = 2.5f;
    public boolean crosshairOutline = true;
    public boolean crosshairCenterDot = false;
    public boolean crosshairAdaptive = false;

    // --- SwingAnimation ---
    public boolean swingAnimationEnabled = false;
    public boolean swingAnimationCollapsed = true;
    public boolean swingAnimationAuraOnly = false;
    public int swingAnimationMode = 0;       // 0..4 (Мод 1..5)
    public float swingAngle = 75.0f;
    public float swingTipTilt = -20.0f;
    public float swingPower = 5.0f;

    // --- SeeInvisibles ---
    public boolean seeInvisiblesEnabled = false;

    public boolean chinaHatEnabled = false;
    public boolean chinaHatSelf = true;
    public boolean chinaHatFriends = true;
    public boolean chinaHatAll = false;
    public int chinaHatColor = 0xFFAB9FF2;
}