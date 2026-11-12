package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class Aura {
    private static LivingEntity target;

    public static LivingEntity getTarget() {
        return target;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(Aura::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || !BiwerVisualsClient.CONFIG.auraEnabled) {
            target = null;
            return;
        }
        if (BiwerVisualsClient.CONFIG.auraDontHitItemUse && player.isUsingItem()) return;
        if (BiwerVisualsClient.CONFIG.auraDontHitContainer && client.screen != null) return;

        double reach = BiwerVisualsClient.CONFIG.auraRange + BiwerVisualsClient.CONFIG.auraExtraRange;
        LivingEntity picked = pick(client, player, reach);
        if (picked == null) {
            target = null;
            return;
        }
        target = picked;

        Vec3 aim = AuraUtil.aimPoint(player, picked);
        float[] rot = AuraUtil.rotations(player, aim);
        float angle = AuraUtil.angleTo(player, aim);

        int mode = BiwerVisualsClient.CONFIG.auraAimMode;
        if (mode == 1 && angle > BiwerVisualsClient.CONFIG.auraFov) {
            // ФанТайм ФОВ — не доводим вне FOV
        } else if (mode == 2) {
            aimLegit(player, rot[0], rot[1]);
        } else {
            aimFunTime(player, rot[0], rot[1]);
        }

        if (BiwerVisualsClient.CONFIG.auraDontHitThroughWalls && !player.hasLineOfSight(picked)) return;
        if (AuraUtil.reachDistance(player, picked) > reach) return;
        if (angle > BiwerVisualsClient.CONFIG.auraFov) return;

        if (player.getAttackStrengthScale(0.5f) >= 0.9f) {
            client.gameMode.attack(player, picked);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static LivingEntity pick(Minecraft client, LocalPlayer player, double reach) {
        if (target != null && valid(player, target, reach)) return target;

        LivingEntity best = null;
        double bestKey = Double.MAX_VALUE;
        for (Entity e : client.level.getEntities(player, player.getBoundingBox().inflate(reach + 2.0))) {
            if (!(e instanceof LivingEntity living)) continue;
            if (!valid(player, living, reach)) continue;
            double key = switch (BiwerVisualsClient.CONFIG.auraPriority) {
                case 1 -> AuraUtil.reachDistance(player, living);
                case 2 -> living.getHealth();
                default -> AuraUtil.angleTo(player, living.getBoundingBox().getCenter());
            };
            if (key < bestKey) {
                bestKey = key;
                best = living;
            }
        }
        return best;
    }

    private static boolean valid(LocalPlayer player, LivingEntity e, double reach) {
        if (!CombatUtil.isValidTarget(e,
                BiwerVisualsClient.CONFIG.auraPlayers,
                BiwerVisualsClient.CONFIG.auraMobs,
                BiwerVisualsClient.CONFIG.auraAnimals,
                BiwerVisualsClient.CONFIG.auraNaked)) return false;
        if (AuraUtil.reachDistance(player, e) > reach + 0.75) return false;
        return !BiwerVisualsClient.CONFIG.auraDontHitThroughWalls || CombatUtil.canSee(player, e);
    }

    /** Плавная наводка "ФанТайм" (порт Delta). */
    private static void aimFunTime(LocalPlayer player, float targetYaw, float targetPitch) {
        float t = player.tickCount + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float smoothW = (float) ((Math.sin(t * 0.4) * 3.0) + (Math.sin(t * 0.95 + 1.4) * 2.0));
        float smoothH = (float) ((Math.cos(t * 0.5 + 0.7) * 0.5) + (Math.cos(t * 0.78 + 3.1) * 1.5));

        float finalPitch = AuraUtil.approachAngle(player.getXRot(), targetPitch + smoothH * 1.5f, 0.15f);
        float finalYaw = AuraUtil.approachAngle(player.getYRot(), targetYaw + smoothW, 0.25f);

        if (!AuraUtil.inFov(player, targetYaw, targetPitch, BiwerVisualsClient.CONFIG.auraFov)) {
            finalYaw = targetYaw;
            finalPitch = targetPitch;
        }

        player.setYRot(AuraUtil.rotateTowards(player.getYRot(), finalYaw, 180.0f));
        player.setXRot(CombatUtil.clamp(AuraUtil.rotateTowards(player.getXRot(), finalPitch, 180.0f), -90f, 90f));
    }

    /** Плавная наводка "Легит" (порт Delta). */
    private static void aimLegit(LocalPlayer player, float targetYaw, float targetPitch) {
        float t = player.tickCount + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float noise = (float) ((Math.sin(t * 0.31) * 0.5) + (Math.sin(t * 1.7 + 2.6) * 0.2)) * 2.0f;

        float finalYaw = AuraUtil.approachAngle(player.getYRot(), targetYaw, 0.25f);
        float finalPitch = AuraUtil.approachAngle(player.getXRot(), targetPitch, 0.2f);

        if (noise > 0) {
            finalYaw += noise;
            finalPitch += noise * 0.5f;
        }

        player.setYRot(AuraUtil.rotateTowards(player.getYRot(), finalYaw, 120.0f));
        player.setXRot(CombatUtil.clamp(AuraUtil.rotateTowards(player.getXRot(), finalPitch, 120.0f), -90f, 90f));
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.auraEnabled = !BiwerVisualsClient.CONFIG.auraEnabled;
        target = null;
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.auraEnabled ? "§aВКЛ" : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[Aura] " + state));
        }
    }
}