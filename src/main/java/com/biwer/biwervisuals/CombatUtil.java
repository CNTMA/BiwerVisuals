package com.biwer.biwervisuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * CombatUtil — общие проверки для Aura / TriggerBot / AimAssistant.
 *
 * Здесь только чистые вычисления: дистанция до хитбокса, видимость,
 * точки прицеливания и углы. Никаких событий и состояния модулей.
 */
public final class CombatUtil {
    private CombatUtil() {
    }

    /** Подходит ли цель под выбранные фильтры. */
    public static boolean isValidTarget(LivingEntity entity, boolean players, boolean mobs, boolean animals, boolean nakedAllowed) {
        if (entity == null) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || entity == player) return false;
        if (!entity.isAlive() || entity.isRemoved() || entity.isSpectator()) return false;

        if (entity instanceof Player target) {
            if (!players) return false;
            // "Без брони" разрешает бить игроков без брони; в броне бьём всегда.
            return nakedAllowed || !isNaked(target);
        }
        if (entity instanceof Animal || entity instanceof AbstractGolem || entity instanceof AmbientCreature) {
            return animals;
        }
        if (entity instanceof Mob) {
            return mobs;
        }
        return false;
    }

    public static boolean isNaked(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!player.getItemBySlot(slot).isEmpty()) return false;
        }
        return true;
    }

    /** Расстояние от глаз до ближайшей точки хитбокса. */
    public static double reachDistance(LocalPlayer player, LivingEntity target) {
        return Math.sqrt(target.getBoundingBox().distanceToSqr(player.getEyePosition()));
    }

    public static boolean inReach(LocalPlayer player, LivingEntity target, double reach) {
        return reachDistance(player, target) <= reach;
    }

    public static boolean canSee(LocalPlayer player, LivingEntity target) {
        return player.hasLineOfSight(target);
    }

    /** Точка прицеливания: центр хитбокса по горизонтали, по высоте — на уровне глаз. */
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

    /** Угол между взглядом и точкой в градусах. */
    public static float angleTo(LocalPlayer player, Vec3 point) {
        Vec3 eye = player.getEyePosition();
        Vec3 dir = point.subtract(eye);
        double length = dir.length();
        if (length < 0.0001) return 0.0f;
        double dot = player.getViewVector(1.0f).dot(dir.scale(1.0 / length));
        return (float) Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));
    }

    /** В руке оружие ближнего боя (меч/топор/булава/тризубец). */
    public static boolean armed(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof AxeItem) return true;
        if (stack.getItem() instanceof MaceItem) return true;
        if (stack.getItem() instanceof TridentItem) return true;
        return stack.getItem().builtInRegistryHolder().is(ItemTags.SWORDS);
    }

    /** Можно ли нанести критический удар прямо сейчас. */
    public static boolean canCrit(LocalPlayer player) {
        return !player.onGround()
                && player.getDeltaMovement().y < 0.0
                && !player.onClimbable()
                && !player.isInWater()
                && !player.isPassenger();
    }

    public static float wrap(float degrees) {
        return Mth.wrapDegrees(degrees);
    }

    public static float clamp(float value, float min, float max) {
        return Mth.clamp(value, min, max);
    }
}