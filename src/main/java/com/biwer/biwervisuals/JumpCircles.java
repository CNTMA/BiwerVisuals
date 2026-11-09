package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JumpCircles — белый круг в точке прыжка, который расходится и плавно гаснет.
 *
 * Рисуется через gizmo-API (Gizmos.circle + GizmoStyle), то есть штатными
 * средствами движка: никаких своих буферов и шейдеров. Круг появляется там,
 * где игрок оторвался от земли, и живёт ~0.65 с.
 *
 * Если gizmo-коллектор недоступен в этой сборке, модуль один раз сообщает об
 * этом в чат и переключается на запасной вариант — кольцо белых частиц.
 */
public class JumpCircles {
    private static final long LIFE_MS = 650L;
    private static final int SEGMENTS = 28;

    private static final List<Circle> CIRCLES = new ArrayList<>();
    private static boolean wasOnGround = true;
    private static boolean gizmoFailed = false;

    private static final class Circle {
        final double x;
        final double y;
        final double z;
        final long start;

        Circle(double x, double y, double z, long start) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.start = start;
        }
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(JumpCircles::tick);
        LevelRenderEvents.BEFORE_GIZMOS.register(JumpCircles::renderGizmos);
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            CIRCLES.clear();
            wasOnGround = true;
            return;
        }

        if (!BiwerVisualsClient.CONFIG.jumpCirclesEnabled) {
            CIRCLES.clear();
            wasOnGround = client.player.onGround();
            return;
        }

        boolean onGround = client.player.onGround();

        // Оторвались от земли вверх — это прыжок
        if (wasOnGround && !onGround && client.player.getDeltaMovement().y > 0.05) {
            Circle circle = new Circle(
                    client.player.getX(),
                    client.player.getY(),
                    client.player.getZ(),
                    System.currentTimeMillis()
            );

            if (gizmoFailed) {
                spawnParticles(client, circle);
            } else {
                CIRCLES.add(circle);
            }
        }

        wasOnGround = onGround;

        long now = System.currentTimeMillis();
        Iterator<Circle> iterator = CIRCLES.iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().start > LIFE_MS) iterator.remove();
        }
    }

    private static void renderGizmos(LevelRenderContext context) {
        if (gizmoFailed || CIRCLES.isEmpty()) return;
        if (!BiwerVisualsClient.CONFIG.jumpCirclesEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        long now = System.currentTimeMillis();

        try {
            for (Circle circle : CIRCLES) {
                float progress = (now - circle.start) / (float) LIFE_MS;
                if (progress < 0.0f || progress > 1.0f) continue;

                int alpha = (int) ((1.0f - progress) * 235.0f);
                if (alpha < 4) continue;

                float radius = 0.35f + progress * 1.10f;

                Vec3 center = new Vec3(circle.x, circle.y + 0.03, circle.z);

                Gizmos.circle(center, radius, GizmoStyle.strokeAndFill(
                        white(alpha),
                        2.5f,
                        white(alpha / 3)
                ));
            }
        } catch (IllegalStateException exception) {
            gizmoFailed = true;
            CIRCLES.clear();
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal(
                        "§e[JumpCircles] §7gizmo-рендер недоступен, включено кольцо из частиц."
                ));
            }
        }
    }

    private static void spawnParticles(Minecraft mc, Circle circle) {
        if (mc.level == null) return;
        for (int i = 0; i < SEGMENTS; i++) {
            double angle = Math.PI * 2.0 * i / SEGMENTS;
            double x = circle.x + Math.cos(angle) * 0.9;
            double z = circle.z + Math.sin(angle) * 0.9;
            mc.level.addParticle(ParticleTypes.END_ROD, x, circle.y + 0.05, z, 0.0, 0.0, 0.0);
        }
    }

    private static int white(int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | 0x00FFFFFF;
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.jumpCirclesEnabled = !BiwerVisualsClient.CONFIG.jumpCirclesEnabled;
        CIRCLES.clear();
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.jumpCirclesEnabled ? "§aВКЛ" : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[JumpCircles] " + state));
        }
    }
}