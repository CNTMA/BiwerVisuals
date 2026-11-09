package com.biwer.biwervisuals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/**
 * TargetStrafe — обход цели по кругу на заданной дистанции.
 *
 * Движение не подменяется напрямую: модуль нажимает те же клавиши, что и игрок
 * (W/A/S/D через KeyMapping), поэтому вся физика, анимации и предсказание
 * остаются ванильными. Клавиши, которые зажал сам игрок, модуль не отпускает.
 *
 * Настройки:
 *  - AutoJump — подпрыгивать, если на пути препятствие (horizontalCollision);
 *  - Range    — дистанция, которую держим до цели.
 */
public class TargetStrafe {
    /** Радиус поиска цели (KillAura ищет в своём, у страйфа он шире). */
    private static final double SEARCH_RANGE = 6.0;

    /** 0 — вперёд, 1 — назад, 2 — влево, 3 — вправо. */
    private static final boolean[] FORCED = new boolean[4];

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(TargetStrafe::onTick);
    }

    private static void onTick(Minecraft client) {
        if (client.player == null || client.level == null || client.screen != null) {
            releaseAll(client);
            return;
        }

        if (!BiwerVisualsClient.CONFIG.targetStrafeEnabled) {
            releaseAll(client);
            return;
        }

        var player = client.player;
        if (player.getAbilities().flying || player.isPassenger()) {
            releaseAll(client);
            return;
        }

        LivingEntity target = BiwerVisualsClient.findTarget(client, SEARCH_RANGE);
        if (target == null) {
            releaseAll(client);
            return;
        }

        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0e-3) {
            releaseAll(client);
            return;
        }

        double ux = dx / dist;
        double uz = dz / dist;

        double range = clamp(BiwerVisualsClient.CONFIG.strafeRange, 0.5f, 6.0f);
        // Радиальная поправка: далеко — подходим, слишком близко — отходим.
        double radial = Math.max(-1.2, Math.min(1.2, dist - range));

        // Тангенциальное направление (обход по кругу) + радиальная поправка
        double wx = -uz + ux * radial;
        double wz = ux + uz * radial;

        double len = Math.sqrt(wx * wx + wz * wz);
        if (len < 1.0e-4) {
            releaseAll(client);
            return;
        }
        wx /= len;
        wz /= len;

        // Мировое направление -> локальные "вперёд"/"в сторону" относительно взгляда
        double yaw = Math.toRadians(player.getYRot());
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double forward = wz * cos - wx * sin;
        double strafe = wx * cos + wz * sin;

        setKeys(client, forward > 0.35, forward < -0.35, strafe > 0.35, strafe < -0.35);

        if (BiwerVisualsClient.CONFIG.strafeAutoJump
                && player.onGround()
                && player.horizontalCollision
                && !player.isInWater()
                && !player.isInLava()) {
            BunnyHop.performJump(player);
        }
    }

    private static KeyMapping[] keys(Minecraft client) {
        return new KeyMapping[]{
                client.options.keyUp,
                client.options.keyDown,
                client.options.keyLeft,
                client.options.keyRight
        };
    }

    private static void setKeys(Minecraft client, boolean up, boolean down, boolean left, boolean right) {
        boolean[] want = {up, down, left, right};
        KeyMapping[] mappings = keys(client);
        for (int i = 0; i < mappings.length; i++) {
            if (want[i]) {
                if (!mappings[i].isDown()) mappings[i].setDown(true);
                FORCED[i] = true;
            } else if (FORCED[i]) {
                release(client, mappings[i]);
                FORCED[i] = false;
            }
        }
    }

    private static void releaseAll(Minecraft client) {
        if (client == null) return;
        KeyMapping[] mappings = keys(client);
        for (int i = 0; i < mappings.length; i++) {
            if (FORCED[i]) {
                release(client, mappings[i]);
                FORCED[i] = false;
            }
        }
    }

    /** Отпускаем клавишу, только если её не держит сам игрок. */
    private static void release(Minecraft client, KeyMapping mapping) {
        try {
            int code = InputConstants.getKey(mapping.saveString()).getValue();
            if (!InputConstants.isKeyDown(client.getWindow(), code)) {
                mapping.setDown(false);
            }
        } catch (Exception ignored) {
            mapping.setDown(false);
        }
    }

    private static double clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.targetStrafeEnabled = !BiwerVisualsClient.CONFIG.targetStrafeEnabled;
        if (!BiwerVisualsClient.CONFIG.targetStrafeEnabled && client != null) {
            releaseAll(client);
        }
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.targetStrafeEnabled
                    ? "§aВКЛ §7(дистанция " + String.format("%.1f", BiwerVisualsClient.CONFIG.strafeRange) + ")"
                    : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[TargetStrafe] " + state));
        }
    }
}