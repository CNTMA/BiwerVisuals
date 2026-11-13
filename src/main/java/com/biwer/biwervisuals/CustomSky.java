package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

/**
 * CustomSky / SkyChanger — перекрашивает небо, управляет звёздами и рисует северное сияние.
 *
 * Работает на готовом состоянии неба: движок сначала собирает SkyRenderState,
 * затем рисует по нему. Мы правим его в END_EXTRACTION (между этими фазами),
 * поэтому не трогаем шейдеры, не ломаем ванильные закаты и не отправляем
 * на сервер ничего лишнего.
 *
 * Северное сияние рисуется в BEFORE_GIZMOS штатным gizmo-API: несколько
 * полупрозрачных волнистых «занавесей» вокруг игрока. Снизу занавес окрашен
 * в auroraColorBottom, сверху — в auroraColorTop; оба цвета меняются в GUI
 * (Render -> CustomSky -> Северное сияние).
 *
 * Режимы неба:
 *   0 Закат    — тёплый янтарь, плавно уходящий в багровый
 *   1 Аврора   — глубокая бирюза с зелёным свечением
 *   2 Космос   — почти чёрное небо с максимально яркими звёздами
 *   3 Неон     — розово-фиолетовый перелив
 *   4 Перелив  — полный оборот по спектру
 */
public class CustomSky {
    // Геометрия сияния: 3 кольца-занавеса по 60 секторов и 5 слоёв по высоте.
    private static final int AURORA_BANDS = 3;
    private static final int AURORA_SEGMENTS = 96;
    private static final int AURORA_LAYERS = 6;

    // Если gizmo-коллектор недоступен, сияние один раз предупреждает и отключается.
    private static boolean gizmoFailed = false;

    public static void init() {
        LevelRenderEvents.END_EXTRACTION.register(context -> {
            if (context == null || context.levelState() == null) return;
            apply(context.levelState().skyRenderState);
        });

        LevelRenderEvents.BEFORE_GIZMOS.register(CustomSky::renderAurora);
    }

    private static void apply(SkyRenderState sky) {
        if (sky == null) return;
        if (!BiwerVisualsClient.CONFIG.customSkyEnabled
                && !VisualEffects.enabled("AuroraSky") && !VisualEffects.enabled("GalaxySky")
                && !VisualEffects.enabled("SkyChanger")) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (sky.skybox != DimensionType.Skybox.OVERWORLD) return;

        float speed = Math.max(0.05f, BiwerVisualsClient.CONFIG.skySpeed / 100.0f);
        float phase = ((System.currentTimeMillis() % 3_600_000L) / 1000.0f) * speed;

        int mode = VisualEffects.enabled("GalaxySky") ? 2 : VisualEffects.enabled("AuroraSky") ? 1
                : VisualEffects.enabled("SkyChanger") ? 0 : BiwerVisualsClient.CONFIG.skyMode;
        int color = switch (mode) {
            case 0 -> breathe(0xFF8A4B, 0xC2185B, phase);
            case 1 -> breathe(0x0B3D4A, 0x0FA36B, phase);
            case 2 -> breathe(0x05010F, 0x1B0B3A, phase);
            case 3 -> hsv((phase * 60.0f) % 360.0f, 0.70f, 0.55f);
            default -> hsv((phase * 40.0f) % 360.0f, 0.55f, 0.80f);
        };

        String effect = VisualEffects.enabled("GalaxySky") ? "GalaxySky" : VisualEffects.enabled("AuroraSky")
                ? "AuroraSky" : VisualEffects.enabled("SkyChanger") ? "SkyChanger" : "CustomSky";
        float strength = VisualEffects.strength(effect);
        color = VisualEffects.blend(color, VisualEffects.tint(effect, phase), 0.25f);
        sky.skyColor = VisualEffects.blend(sky.skyColor, color, strength);

        float stars = BiwerVisualsClient.CONFIG.skyStars / 100.0f;
        if (mode == 2) stars = Math.max(stars, 0.85f);
        sky.starBrightness += (Math.clamp(stars, 0f, 1f) - sky.starBrightness) * strength;

        if (mode == 0 && strength > 0.25f) {
            // Тёплый горизонт: подкрашиваем ванильный закатный «веер»
            sky.sunriseAndSunsetColor = 0xFFB36B;
        }
    }

    // ==================== СЕВЕРНОЕ СИЯНИЕ ====================

    private static void renderAurora(LevelRenderContext context) {
        if (gizmoFailed) return;
        if (context == null || context.levelState() == null) return;
        if (!BiwerVisualsClient.CONFIG.customSkyEnabled
                && !VisualEffects.enabled("AuroraSky") && !VisualEffects.enabled("GalaxySky")
                && !VisualEffects.enabled("SkyChanger")) return;
        if (!VisualEffects.enabled("AuroraSky")
                && !(BiwerVisualsClient.CONFIG.customSkyEnabled && BiwerVisualsClient.CONFIG.auroraEnabled)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.level.dimensionType().skybox() != DimensionType.Skybox.OVERWORLD) return;

        CameraRenderState camera = context.levelState().cameraRenderState;
        if (camera == null || !camera.initialized) return;

        float intensity = clamp01(BiwerVisualsClient.CONFIG.auroraBrightness / 100.0f)
                * VisualEffects.strength(VisualEffects.enabled("AuroraSky") ? "AuroraSky" : "CustomSky");
        if (intensity < 0.02f) return;

        // Радиус подбираем по дальней плоскости, чтобы занавес не срезался фрустумом.
        float far = camera.depthFar > 8.0f ? camera.depthFar : 256.0f;
        float radius = Math.max(46.0f, Math.min(112.0f, far * 0.38f));

        float time = (System.currentTimeMillis() % 1_200_000L) / 1000.0f;
        // Волны сияния ускоряются тем же ползунком «Скорость», что и палитра неба.
        float speed = 0.25f + clamp01(BiwerVisualsClient.CONFIG.skySpeed / 100.0f) * 1.75f;

        int bottomColor = VisualEffects.enabled("AuroraSky") ? VisualEffects.tint("AuroraSky", time)
                : BiwerVisualsClient.CONFIG.auroraColorBottom & 0xFFFFFF;
        int topColor = VisualEffects.enabled("AuroraSky") ? VisualEffects.tint("AuroraSky", time + 2)
                : BiwerVisualsClient.CONFIG.auroraColorTop & 0xFFFFFF;

        try {
            for (int band = 0; band < AURORA_BANDS; band++) {
                drawAuroraBand(camera.pos, radius, band, time, speed, intensity, bottomColor, topColor);
            }
        } catch (IllegalStateException exception) {
            gizmoFailed = true;
            BiwerVisualsClient.CONFIG.auroraEnabled = false;
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal(
                        "§e[CustomSky] §7Северное сияние недоступно: gizmo-рендер выключен."
                ));
            }
        }
    }

    private static void drawAuroraBand(Vec3 camera, float radius, int band, float time, float speed,
                                       float intensity, int bottomColor, int topColor) {
        float bandRadius = radius * (1.0f + band * 0.10f);
        float baseY = radius * (0.30f + band * 0.09f);
        float bandHeight = radius * (0.72f - band * 0.09f);
        float bandPhase = band * 2.1f;
        float bandAlpha = 1.0f - band * 0.26f;

        int alpha = Math.round(205.0f * intensity * bandAlpha);
        if (alpha < 4) return;

        for (int i = 0; i < AURORA_SEGMENTS; i++) {
            double a0 = Math.PI * 2.0 * i / AURORA_SEGMENTS;
            double a1 = Math.PI * 2.0 * (i + 1) / AURORA_SEGMENTS;

            float wave0 = wave(a0, time, speed, bandPhase);
            float wave1 = wave(a1, time, speed, bandPhase);
            float topScale0 = 0.94f + 0.06f * (float) Math.sin(a0 * 2.0 + time * 0.2 + bandPhase);
            float topScale1 = 0.94f + 0.06f * (float) Math.sin(a1 * 2.0 + time * 0.2 + bandPhase);

            double x0 = camera.x + Math.cos(a0) * bandRadius;
            double z0 = camera.z + Math.sin(a0) * bandRadius;
            double x1 = camera.x + Math.cos(a1) * bandRadius;
            double z1 = camera.z + Math.sin(a1) * bandRadius;

            float bottom0 = (float) (camera.y + baseY + radius * wave0);
            float bottom1 = (float) (camera.y + baseY + radius * wave1);
            float top0 = bottom0 + bandHeight * topScale0;
            float top1 = bottom1 + bandHeight * topScale1;

            // Мягкая волна яркости вдоль занавеса — «лучи» сияния.
            float ray = 0.88f + 0.12f * (float) Math.sin(a0 * 9.0 + time * 0.6 + bandPhase);
            int segmentAlpha = Math.round(alpha * ray);
            if (segmentAlpha < 4) continue;

            for (int layer = 0; layer < AURORA_LAYERS; layer++) {
                float t0 = layer / (float) AURORA_LAYERS;
                float t1 = (layer + 1) / (float) AURORA_LAYERS;
                float tm = (t0 + t1) * 0.5f;

                int color = lerp(bottomColor, topColor, (float) Math.pow(tm, 0.7));
                int layerAlpha = Math.round(segmentAlpha * (1.0f - tm) * (1.0f - 0.2f * tm));
                if (layerAlpha < 3) continue;

                GizmoStyle style = GizmoStyle.fill((Math.min(250, layerAlpha) << 24) | color);

                // Порядок вершин даёт нормаль внутрь кольца — игрок всегда видит лицевую сторону.
                Vec3 p0 = new Vec3(x0, lerpF(bottom0, top0, t0), z0);
                Vec3 p1 = new Vec3(x1, lerpF(bottom1, top1, t0), z1);
                Vec3 p2 = new Vec3(x1, lerpF(bottom1, top1, t1), z1);
                Vec3 p3 = new Vec3(x0, lerpF(bottom0, top0, t1), z0);

                Gizmos.rect(p0, p1, p2, p3, style);
            }
        }
    }

    private static float wave(double angle, float time, float speed, float phase) {
        return (float) (Math.sin(angle * 2.0 + time * 0.5 * speed + phase) * 0.030
                + Math.sin(angle * 3.0 - time * 0.7 * speed + phase * 1.7) * 0.010);
    }

    private static int lerp(int from, int to, float k) {
        float t = clamp01(k);
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;

        int r = Math.round(fr + (tr - fr) * t);
        int g = Math.round(fg + (tg - fg) * t);
        int b = Math.round(fb + (tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static float lerpF(float from, float to, float k) {
        return from + (to - from) * clamp01(k);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static int breathe(int from, int to, float phase) {
        float k = 0.5f + 0.5f * (float) Math.sin(phase);
        return lerp(from, to, k);
    }

    private static int hsv(float hue, float saturation, float value) {
        float h = (((hue % 360.0f) + 360.0f) % 360.0f) / 60.0f;
        int sector = (int) Math.floor(h);
        float f = h - sector;

        float p = value * (1.0f - saturation);
        float q = value * (1.0f - saturation * f);
        float t = value * (1.0f - saturation * (1.0f - f));

        float r = 0.0f;
        float g = 0.0f;
        float b = 0.0f;

        switch (sector % 6) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            default -> { r = value; g = p; b = q; }
        }

        int ri = Math.round(r * 255.0f);
        int gi = Math.round(g * 255.0f);
        int bi = Math.round(b * 255.0f);
        return (ri << 16) | (gi << 8) | bi;
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.customSkyEnabled = !BiwerVisualsClient.CONFIG.customSkyEnabled;
        if (client != null && client.player != null) {
            String[] names = {"Закат", "Аврора", "Космос", "Неон", "Перелив"};
            int mode = Math.max(0, Math.min(names.length - 1, BiwerVisualsClient.CONFIG.skyMode));
            String state = BiwerVisualsClient.CONFIG.customSkyEnabled
                    ? "§aВКЛ §7(" + names[mode] + ")"
                    : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[CustomSky] " + state));
        }
    }

    public static void toggleAurora(Minecraft client) {
        BiwerVisualsClient.CONFIG.auroraEnabled = !BiwerVisualsClient.CONFIG.auroraEnabled;
        if (BiwerVisualsClient.CONFIG.auroraEnabled) {
            // Сияние — часть CustomSky, поэтому заодно включаем сам модуль неба.
            BiwerVisualsClient.CONFIG.customSkyEnabled = true;
        }
        gizmoFailed = false;
        if (client != null && client.player != null) {
            String state;
            if (BiwerVisualsClient.CONFIG.auroraEnabled) {
                state = "§aВКЛ §7(низ #" + hex(BiwerVisualsClient.CONFIG.auroraColorBottom)
                        + "§7, верх #" + hex(BiwerVisualsClient.CONFIG.auroraColorTop) + "§7)";
            } else {
                state = "§cВЫКЛ";
            }
            client.player.sendSystemMessage(Component.literal("§e[CustomSky] Северное сияние: " + state));
        }
    }

    private static String hex(int rgb) {
        return String.format("%06X", rgb & 0xFFFFFF);
    }
}