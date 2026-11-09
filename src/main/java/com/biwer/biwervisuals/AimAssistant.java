package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * AimAssistant — плавно доводит прицел до цели (порт Aim Assistant).
 *
 * Цель берётся у TriggerBot, если он включён и что-то видит, иначе
 * выбирается ближайшая по углу в радиусе 4.5 блока. Доворот ограничен
 * углом за тик, чтобы не выглядело как телепорт камеры.
 */
public class AimAssistant {
    private static LivingEntity target;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(AimAssistant::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || !BiwerVisualsClient.CONFIG.aimAssistEnabled) {
            target = null;
            return;
        }
        if (player.isUsingItem()) return;
        if (BiwerVisualsClient.CONFIG.aimAssistWeaponOnly && !CombatUtil.armed(player)) return;

        LivingEntity wanted = null;
        if (BiwerVisualsClient.CONFIG.triggerBotEnabled) {
            wanted = TriggerBot.getTarget();
        }
        if (wanted == null || !valid(player, wanted)) {
            wanted = nearest(client, player);
        }
        if (wanted == null) {
            target = null;
            return;
        }
        target = wanted;

        if (!BiwerVisualsClient.CONFIG.aimAssistThroughWalls && !CombatUtil.canSee(player, wanted)) return;

        Vec3 point = CombatUtil.aimPoint(player, wanted);
        float[] rotations = CombatUtil.rotations(player, point);

        float deltaYaw = Mth.wrapDegrees(rotations[0] - player.getYRot());
        float deltaPitch = rotations[1] - player.getXRot();

        boolean onTarget = Math.abs(deltaYaw) < 4.0f && Math.abs(deltaPitch) < 8.0f
                && CombatUtil.canSee(player, wanted);
        if (onTarget) return;
        if (Math.abs(deltaYaw) > 80.0f) return;

        float strength = Mth.clamp(BiwerVisualsClient.CONFIG.aimAssistStrength / 100.0f, 0.02f, 1.0f);
        float maxStep = 20.0f * strength;

        float stepYaw = Mth.clamp(deltaYaw * strength, -maxStep, maxStep);
        float stepPitch = Mth.clamp(deltaPitch * strength, -maxStep, maxStep);

        player.setYRot(player.getYRot() + stepYaw);
        player.setXRot(Mth.clamp(player.getXRot() + stepPitch, -90.0f, 90.0f));
    }

    private static boolean valid(LocalPlayer player, LivingEntity entity) {
        if (!CombatUtil.isValidTarget(entity,
                BiwerVisualsClient.CONFIG.aimAssistPlayers,
                BiwerVisualsClient.CONFIG.aimAssistMobs,
                BiwerVisualsClient.CONFIG.aimAssistAnimals,
                true)) {
            return false;
        }
        return CombatUtil.inReach(player, entity, 5.0);
    }

    private static LivingEntity nearest(Minecraft client, LocalPlayer player) {
        LivingEntity best = null;
        float bestAngle = 55.0f;

        for (Entity entity : client.level.getEntities(player, player.getBoundingBox().inflate(5.0))) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!valid(player, living)) continue;
            if (!BiwerVisualsClient.CONFIG.aimAssistThroughWalls && !CombatUtil.canSee(player, living)) continue;

            float angle = CombatUtil.angleTo(player, CombatUtil.aimPoint(player, living));
            if (angle < bestAngle) {
                bestAngle = angle;
                best = living;
            }
        }
        return best;
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.aimAssistEnabled = !BiwerVisualsClient.CONFIG.aimAssistEnabled;
        target = null;
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.aimAssistEnabled ? "§aВКЛ" : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[AimAssistant] " + state));
        }
    }
}