package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * BunnyHop — автоматический прыжок на земле, чтобы не терять скорость.
 *
 * Прыжок делается штатным LivingEntity#jumpFromGround(), а спринт-бонус
 * (+0.2 по направлению взгляда) повторяет ванильный, поэтому профиль прыжка
 * не отличается от обычного спринт-прыжка. Никаких пакетов не отправляется.
 *
 * Настройки:
 *  - AutoJump        — прыгать автоматически;
 *  - OnlyWhileMoving — только когда игрок реально двигается;
 *  - JumpDelay       — пауза между прыжками в тиках (0 = сразу по приземлении).
 */
public class BunnyHop {
    /** Тиков до следующего прыжка (JumpDelay). */
    private static int cooldown = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(BunnyHop::onTick);
    }

    private static void onTick(Minecraft client) {
        var player = client.player;
        if (player == null || client.level == null) {
            cooldown = 0;
            return;
        }

        if (!BiwerVisualsClient.CONFIG.bunnyHopEnabled || !BiwerVisualsClient.CONFIG.bhopAutoJump) {
            cooldown = 0;
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (!canJump(client, player)) return;

        performJump(player);
        cooldown = Math.max(0, BiwerVisualsClient.CONFIG.bhopJumpDelay);
    }

    private static boolean canJump(Minecraft client, LocalPlayer player) {
        if (!player.onGround()) return false;
        if (player.isInWater() || player.isInLava()) return false;
        if (player.getAbilities().flying) return false;
        if (player.isPassenger()) return false;
        if (player.isShiftKeyDown()) return false;
        if (BiwerVisualsClient.CONFIG.bhopOnlyWhileMoving && !isMoving(client, player)) return false;
        return true;
    }

    private static boolean isMoving(Minecraft client, LocalPlayer player) {
        var motion = player.getDeltaMovement();
        if (Math.abs(motion.x) + Math.abs(motion.z) > 0.02) return true;
        return client.options.keyUp.isDown()
                || client.options.keyDown.isDown()
                || client.options.keyLeft.isDown()
                || client.options.keyRight.isDown();
    }

    /** Ванильный прыжок + спринт-бонус, как при ручном спринт-прыжке. */
    public static void performJump(LocalPlayer player) {
        player.jumpFromGround();
        if (player.isSprinting()) {
            float yaw = player.getYRot() * ((float) Math.PI / 180.0F);
            player.push(-Math.sin(yaw) * 0.2, 0.0, Math.cos(yaw) * 0.2);
        }
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.bunnyHopEnabled = !BiwerVisualsClient.CONFIG.bunnyHopEnabled;
        if (client.player != null) {
            String state = BiwerVisualsClient.CONFIG.bunnyHopEnabled
                    ? "§aВКЛ §7(задержка " + BiwerVisualsClient.CONFIG.bhopJumpDelay + " т.)"
                    : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[BunnyHop] " + state));
        }
    }
}