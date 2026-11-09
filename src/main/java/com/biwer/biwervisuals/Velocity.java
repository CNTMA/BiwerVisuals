package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Velocity — анти-отбрасывание (порт aethereal Velocity).
 *
 * Миксин VelocityMixin отменяет ClientboundSetEntityMotionPacket для игрока.
 * Режим «Обычный» просто гасит отбрасывание, «Легитный» возвращает часть
 * импульса через push() на следующем тике — выглядит естественнее.
 */
public class Velocity {
    private static Vec3 pending;
    private static int damageTicks;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(Velocity::tick);
    }

    /** Игрок получил урон — включаем окно подавления отбрасывания. */
    public static void onDamage() {
        damageTicks = 12;
    }

    /**
     * Подавляем пакет скорости только в окне после урона. Иначе отменяются
     * и обычные серверные коррекции движения — из-за этого игрок «зависает»
     * на краю блока (no-fall).
     */
    public static boolean shouldSuppress(Vec3 movement) {
        if (damageTicks <= 0) return false;
        // Только заметный горизонтальный импульс (нокбек). Вертикальные
        // коррекции и падение не трогаем — иначе игрок «зависает» на блоке.
        double horizontal = movement.x * movement.x + movement.z * movement.z;
        return horizontal > 0.01;
    }

    /** Вызывается из миксина (сетевой поток) — запоминаем импульс. */
    public static void onKnockback(Vec3 movement) {
        pending = movement;
    }

    private static void tick(Minecraft client) {
        if (damageTicks > 0) damageTicks--;

        if (pending == null) return;
        Vec3 movement = pending;
        pending = null;

        if (client.player == null || !BiwerVisualsClient.CONFIG.velocityEnabled) return;
        if (BiwerVisualsClient.CONFIG.velocityMode != 1) return;

        float percent = Mth.clamp(BiwerVisualsClient.CONFIG.velocityPercent / 100.0f, 0.0f, 1.0f);
        if (percent <= 0.0f) return;

        client.player.push(movement.x * percent, movement.y * percent, movement.z * percent);
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.velocityEnabled = !BiwerVisualsClient.CONFIG.velocityEnabled;
        pending = null;
        damageTicks = 0;
        if (client != null && client.player != null) {
            String[] modes = {"Обычный", "Легитный"};
            int mode = Math.max(0, Math.min(1, BiwerVisualsClient.CONFIG.velocityMode));
            String state = BiwerVisualsClient.CONFIG.velocityEnabled
                    ? "§aВКЛ §7(" + modes[mode] + ")"
                    : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[Velocity] " + state));
        }
    }
}