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
}