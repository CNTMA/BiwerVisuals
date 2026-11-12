package com.biwer.biwervisuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AuraUtil {
    private AuraUtil() {}

    /** Дистанция от глаз до ближайшей точки хитбокса. */
    public static double reachDistance(LocalPlayer player, LivingEntity target) {
        return Math.sqrt(target.getBoundingBox().distanceToSqr(player.getEyePosition()));
    }

    /** Угол между взглядом и точкой в градусах. */
    public static float angleTo(LocalPlayer player, Vec3 point) {
        Vec3 eye = player.getEyePosition();
        Vec3 dir = point.subtract(eye);
        double len = dir.length();
        if (len < 1e-4) return 0f;
        double dot = player.getViewVector(1.0f).dot(dir.scale(1.0 / len));
        return (float) Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));
    }

    /** Внутри ли цель FOV по yaw/pitch. */
    public static boolean inFov(LocalPlayer player, float targetYaw, float targetPitch, float fov) {
        return Math.abs(Mth.wrapDegrees(targetYaw - player.getYRot())) <= fov
                && Math.abs(targetPitch - player.getXRot()) <= 70.0f;
    }

    /** Плавный подход к углу (аналог AuraUtil.a из Delta). */
    public static float approachAngle(float current, float target, float amount) {
        float delta = Mth.wrapDegrees(target - current);
        if (Math.abs(delta) < 0.5f) return target;
        return Mth.wrapDegrees(current + delta * amount);
    }

    /** Ограничение угловой скорости (градусы за тик). */
    public static float rotateTowards(float current, float target, float maxStep) {
        float delta = Mth.wrapDegrees(target - current);
        if (Math.abs(delta) <= maxStep) return target;
        return Mth.wrapDegrees(current + Math.signum(delta) * maxStep);
    }

    /** Точка прицеливания: центр хитбокса, по высоте — глаза игрока. */
    public static Vec3 aimPoint(LocalPlayer player, LivingEntity target) {
        AABB box = target.getBoundingBox();
        Vec3 center = box.getCenter();
        double y = Mth.clamp(player.getEyePosition().y, box.minY + 0.1, box.maxY - 0.05);
        return new Vec3(center.x, y, center.z);
    }

    /** Углы (yaw, pitch) от глаз игрока к точке. */
    public static float[] rotations(LocalPlayer player, Vec3 point) {
        Vec3 eye = player.getEyePosition();
        double dx = point.x - eye.x;
        double dy = point.y - eye.y;
        double dz = point.z - eye.z;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
        return new float[]{yaw, pitch};
    }
}