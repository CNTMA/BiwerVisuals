package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/**
 * KillAura с очень плавной наводкой.
 *
 * Чем отличается от прежнего «процента за тик»:
 *  - угловая скорость ограничена (градусы за тик), поэтому нет рывка на старте;
 *  - ограничено угловое ускорение, из-за чего разгон и торможение идут
 *    по S-образной кривой — камера «плывёт», а не прыгает;
 *  - шаг всегда урезается до оставшегося угла: перелёта и дрожи у цели нет;
 *  - цель удерживается до потери валидности (нет дёрганья между двумя врагами);
 *  - удар наносится только когда цель реально в прицеле (FOV), а не на 360°.
 *
 * Питч движется отдельно и чуть медленнее — как у живой руки.
 */
public class KillAura {
    /** Цель, которую держим между тиками. */
    private static LivingEntity currentTarget;

    /** Текущая угловая скорость (град/тик) — для ограничения ускорения. */
    private static float yawVelocity = 0.0f;
    private static float pitchVelocity = 0.0f;

    /** Текущая цель — используется TargetHUD. */
    public static LivingEntity getTarget() {
        return currentTarget;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(KillAura::onTick);
    }

    private static void onTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            reset();
            return;
        }

        if (!BiwerVisualsClient.CONFIG.killAuraEnabled) {
            reset();
            return;
        }

        if (BiwerVisualsClient.CONFIG.dontHitItemUse && player.isUsingItem()) return;
        if (BiwerVisualsClient.CONFIG.dontHitContainer && client.screen != null) return;

        double range = BiwerVisualsClient.CONFIG.killAuraRange + BiwerVisualsClient.CONFIG.killAuraExtraRange;

        LivingEntity target = pickTarget(client, player, range);
        if (target == null) {
            // Цели нет — скорость гасим, камеру не дёргаем
            yawVelocity = 0.0f;
            pitchVelocity = 0.0f;
            return;
        }
        currentTarget = target;

        float targetYaw = yawTo(player, target);
        float targetPitch = pitchTo(player, target);

        if (BiwerVisualsClient.CONFIG.aimMode != 0) {
            aim(player, targetYaw, targetPitch);
        }

        // Криты бьём только в воздухе
        if (BiwerVisualsClient.CONFIG.onlyCriticals && player.onGround()) return;

        // Бьём только то, что реально в прицеле
        if (!inFov(player, targetYaw, targetPitch)) return;

        if (player.getAttackStrengthScale(0.5f) >= 1.0f) {
            client.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    /** Держим прежнюю цель, пока она валидна — иначе ищем ближайшую. */
    private static LivingEntity pickTarget(Minecraft client, LocalPlayer player, double range) {
        if (currentTarget != null
                && currentTarget.isAlive()
                && player.distanceTo(currentTarget) <= range
                && (!BiwerVisualsClient.CONFIG.dontHitThroughWalls || player.hasLineOfSight(currentTarget))) {
            return currentTarget;
        }
        return BiwerVisualsClient.findTarget(client, range);
    }

    /** Сглаженный доворот: ограничение скорости + ограничение ускорения. */
    private static void aim(LocalPlayer player, float targetYaw, float targetPitch) {
        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();

        float deltaYaw = wrapDegrees(targetYaw - currentYaw);
        float deltaPitch = targetPitch - currentPitch;

        boolean sharp = BiwerVisualsClient.CONFIG.aimMode == 2;

        float fraction = sharp
                ? 1.0f
                : clamp(BiwerVisualsClient.CONFIG.killAuraSmooth / 100.0f, 0.05f, 1.0f);
        float maxStep = sharp ? 180.0f : Math.max(0.5f, BiwerVisualsClient.CONFIG.killAuraSpeed);

        // Желаемая угловая скорость на этот тик
        float wantedYaw = limit(deltaYaw * fraction, maxStep);
        float wantedPitch = limit(deltaPitch * fraction, maxStep * 0.7f);

        // Ограничение ускорения: скорость нарастает и гаснет постепенно
        float accel = Math.max(0.35f, maxStep * 0.25f);
        yawVelocity = approach(yawVelocity, wantedYaw, accel);
        pitchVelocity = approach(pitchVelocity, wantedPitch, accel * 0.7f);

        // Не перелетаем цель
        float stepYaw = limit(yawVelocity, Math.abs(deltaYaw));
        float stepPitch = limit(pitchVelocity, Math.abs(deltaPitch));

        player.setYRot(currentYaw + stepYaw);
        player.setXRot(clampPitch(currentPitch + stepPitch));
    }

    private static boolean inFov(LocalPlayer player, float targetYaw, float targetPitch) {
        float fov = clamp(BiwerVisualsClient.CONFIG.killAuraFov, 10.0f, 180.0f);
        if (Math.abs(wrapDegrees(targetYaw - player.getYRot())) > fov) return false;
        return Math.abs(targetPitch - player.getXRot()) <= 70.0f;
    }

    private static float yawTo(LocalPlayer player, LivingEntity target) {
        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        return (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
    }

    private static float pitchTo(LocalPlayer player, LivingEntity target) {
        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double dy = (target.getY() + target.getEyeHeight()) - (player.getY() + player.getEyeHeight());
        double dist = Math.sqrt(dx * dx + dz * dz);
        return (float) -Math.toDegrees(Math.atan2(dy, dist));
    }

    private static void reset() {
        currentTarget = null;
        yawVelocity = 0.0f;
        pitchVelocity = 0.0f;
    }

    private static float limit(float value, float max) {
        if (value > max) return max;
        if (value < -max) return -max;
        return value;
    }

    private static float approach(float current, float target, float maxDelta) {
        if (current < target) return Math.min(target, current + maxDelta);
        if (current > target) return Math.max(target, current - maxDelta);
        return target;
    }

    private static float wrapDegrees(float degrees) {
        float d = degrees % 360.0f;
        if (d >= 180.0f) d -= 360.0f;
        if (d < -180.0f) d += 360.0f;
        return d;
    }

    private static float clampPitch(float pitch) {
        return Math.max(-90.0f, Math.min(90.0f, pitch));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.killAuraEnabled = !BiwerVisualsClient.CONFIG.killAuraEnabled;
        reset();
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.killAuraEnabled
                    ? "§aВКЛ §7(плавность " + (int) BiwerVisualsClient.CONFIG.killAuraSmooth + "%)"
                    : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[KillAura] " + state));
        }
    }
}