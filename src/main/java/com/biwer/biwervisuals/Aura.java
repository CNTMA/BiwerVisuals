package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.phys.Vec3;

/**
 * Aura — автоматически атакует цели рядом (порт aethereal Aura).
 *
 * Как и в оригинале: цель удерживается, пока валидна; приоритет — прицел,
 * дистанция или ХП; наводка плавная (скорость + ускорение); удары только
 * по кулдауну; криты/адаптивные прыжки, пробитие щита топором, сброс
 * спринта, проверки «использую предмет / открыт контейнер / за стеной».
 *
 * Отличие от оригинала: у нас нет silent-rotations, поэтому наводка
 * видимая — игрок реально поворачивает камеру (как в нашем KillAura).
 */
public class Aura {
    private static LivingEntity target;
    private static float yawVelocity;
    private static float pitchVelocity;
    private static int previousSlot = -1;
    private static int restoreTicks;

    public static LivingEntity getTarget() {
        return target;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(Aura::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            reset();
            return;
        }
        if (!BiwerVisualsClient.CONFIG.auraEnabled) {
            reset();
            return;
        }
        if (BiwerVisualsClient.CONFIG.auraDontHitItemUse && player.isUsingItem()) return;
        if (BiwerVisualsClient.CONFIG.auraDontHitContainer && client.screen != null) return;

        if (restoreTicks > 0) {
            restoreTicks--;
            if (restoreTicks == 0 && previousSlot >= 0) {
                player.getInventory().setSelectedSlot(previousSlot);
                previousSlot = -1;
            }
        }

        double reach = BiwerVisualsClient.CONFIG.auraRange + BiwerVisualsClient.CONFIG.auraExtraRange;

        LivingEntity picked = pick(client, player, reach);
        if (picked == null) {
            target = null;
            yawVelocity = 0.0f;
            pitchVelocity = 0.0f;
            return;
        }
        if (target != picked) {
            yawVelocity = pitchVelocity = 0f;
        }
        target = picked;

        Vec3 aim = CombatUtil.aimPoint(player, picked);
        float[] rotations = CombatUtil.rotations(player, aim);
        float angle = CombatUtil.angleTo(player, aim);

        boolean fovMode = BiwerVisualsClient.CONFIG.auraAimMode == 1;
        if (!fovMode || angle <= BiwerVisualsClient.CONFIG.auraFov) {
            float scale = BiwerVisualsClient.CONFIG.auraAimMode == 2 ? 0.55f : 1.0f;
            aim(player, rotations[0], rotations[1], scale);
        }

        if (BiwerVisualsClient.CONFIG.auraDontHitThroughWalls && !CombatUtil.canSee(player, picked)) return;
        if (!CombatUtil.inReach(player, picked, reach)) return;
        if (CombatUtil.angleTo(player, aim) > BiwerVisualsClient.CONFIG.auraFov) return;

        if (player.getAttackStrengthScale(0.5f) < 0.9f) return;

        if (BiwerVisualsClient.CONFIG.auraOnlyCriticals && !CombatUtil.canCrit(player)) {
            if (BiwerVisualsClient.CONFIG.auraAdaptiveHits && player.onGround()) {
                player.jumpFromGround();
            }
            return;
        }

        if (BiwerVisualsClient.CONFIG.auraShieldBreaker && picked.isBlocking()) {
            int axe = findAxe(player);
            if (axe >= 0 && axe != player.getInventory().getSelectedSlot()) {
                if (previousSlot < 0) previousSlot = player.getInventory().getSelectedSlot();
                player.getInventory().setSelectedSlot(axe);
                restoreTicks = 6;
            }
        }

        if (BiwerVisualsClient.CONFIG.auraSmartSprint && player.isSprinting()) {
            player.setSprinting(false);
        }

        client.gameMode.attack(player, picked);
        player.swing(InteractionHand.MAIN_HAND);
    }

    /** Держим прежнюю цель, пока она валидна, иначе выбираем по приоритету. */
    private static LivingEntity pick(Minecraft client, LocalPlayer player, double reach) {
        if (target != null && valid(player, target, reach)) {
            return target;
        }

        LivingEntity best = null;
        double bestKey = Double.MAX_VALUE;

        for (Entity entity : client.level.getEntities(player, player.getBoundingBox().inflate(reach + 2.0))) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!valid(player, living, reach)) continue;

            double key = switch (BiwerVisualsClient.CONFIG.auraPriority) {
                case 1 -> CombatUtil.reachDistance(player, living);
                case 2 -> living.getHealth();
                default -> CombatUtil.angleTo(player, living.getBoundingBox().getCenter());
            };

            if (key < bestKey) {
                bestKey = key;
                best = living;
            }
        }
        return best;
    }

    private static boolean valid(LocalPlayer player, LivingEntity entity, double reach) {
        if (!CombatUtil.isValidTarget(entity,
                BiwerVisualsClient.CONFIG.auraPlayers,
                BiwerVisualsClient.CONFIG.auraMobs,
                BiwerVisualsClient.CONFIG.auraAnimals,
                BiwerVisualsClient.CONFIG.auraNaked)) {
            return false;
        }
        if (!CombatUtil.inReach(player, entity, reach + 0.75)) return false;
        return !BiwerVisualsClient.CONFIG.auraDontHitThroughWalls || CombatUtil.canSee(player, entity);
    }

    /** Плавный доворот с ограничением скорости и ускорения. */
    private static void aim(LocalPlayer player, float targetYaw, float targetPitch, float scale) {
        float deltaYaw = CombatUtil.wrap(targetYaw - player.getYRot());
        float deltaPitch = targetPitch - player.getXRot();

        float smooth = CombatUtil.clamp(BiwerVisualsClient.CONFIG.auraSmooth / 100.0f, 0.05f, 1.0f);
        float maxStep = Math.max(0.5f, BiwerVisualsClient.CONFIG.auraSpeed * scale);

        float response = 0.08f + (1f - smooth) * 0.32f;
        float wantedYaw = limit(deltaYaw * response, maxStep);
        float wantedPitch = limit(deltaPitch * response, maxStep * 0.7f);

        float accel = Math.max(0.35f, maxStep * 0.25f);
        yawVelocity = approach(yawVelocity, wantedYaw, accel);
        pitchVelocity = approach(pitchVelocity, wantedPitch, accel * 0.7f);

        float stepYaw = Math.signum(yawVelocity) == Math.signum(deltaYaw)
                ? limit(yawVelocity, Math.abs(deltaYaw)) : 0f;
        float stepPitch = Math.signum(pitchVelocity) == Math.signum(deltaPitch)
                ? limit(pitchVelocity, Math.abs(deltaPitch)) : 0f;

        player.setYRot(player.getYRot() + stepYaw);
        player.setXRot(CombatUtil.clamp(player.getXRot() + stepPitch, -90.0f, 90.0f));
    }

    private static int findAxe(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof AxeItem) {
                return slot;
            }
        }
        return -1;
    }

    private static void reset() {
        target = null;
        yawVelocity = 0.0f;
        pitchVelocity = 0.0f;
        previousSlot = -1;
        restoreTicks = 0;
    }

    private static float limit(float value, float max) {
        if (value > max) return max;
        if (value < -max) return -max;
        return value;
    }

    private static float approach(float current, float wanted, float maxDelta) {
        if (current < wanted) return Math.min(wanted, current + maxDelta);
        if (current > wanted) return Math.max(wanted, current - maxDelta);
        return wanted;
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.auraEnabled = !BiwerVisualsClient.CONFIG.auraEnabled;
        reset();
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.auraEnabled
                    ? "§aВКЛ §7(дистанция " + String.format("%.1f", BiwerVisualsClient.CONFIG.auraRange) + ")"
                    : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[Aura] " + state));
        }
    }
}