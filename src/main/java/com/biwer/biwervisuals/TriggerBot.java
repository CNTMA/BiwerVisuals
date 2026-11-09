package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/**
 * TriggerBot — автоматический удар, когда прицел наведён на цель.
 *
 * Как в оригинале: цель берётся из-под прицела (crosshairPickEntity),
 * проверяются тип цели, дистанция, стена, оружие в руке и кулдаун удара.
 */
public class TriggerBot {
    private static LivingEntity target;

    public static LivingEntity getTarget() {
        return target;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(TriggerBot::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || !BiwerVisualsClient.CONFIG.triggerBotEnabled) {
            target = null;
            return;
        }
        if (BiwerVisualsClient.CONFIG.triggerBotDontHitItemUse && player.isUsingItem()) return;
        if (BiwerVisualsClient.CONFIG.triggerBotDontHitContainer && client.screen != null) return;

        if (!(client.crosshairPickEntity instanceof LivingEntity living)) {
            target = null;
            return;
        }
        if (!CombatUtil.isValidTarget(living,
                BiwerVisualsClient.CONFIG.triggerBotPlayers,
                BiwerVisualsClient.CONFIG.triggerBotMobs,
                BiwerVisualsClient.CONFIG.triggerBotAnimals,
                true)) {
            target = null;
            return;
        }
        if (CombatUtil.reachDistance(player, living) > BiwerVisualsClient.CONFIG.triggerBotRange) {
            target = null;
            return;
        }
        if (BiwerVisualsClient.CONFIG.triggerBotDontHitThroughWalls && !CombatUtil.canSee(player, living)) {
            target = null;
            return;
        }
        if (BiwerVisualsClient.CONFIG.triggerBotWeaponOnly && !CombatUtil.armed(player)) {
            target = null;
            return;
        }

        target = living;

        if (BiwerVisualsClient.CONFIG.triggerBotOnlyCriticals && !CombatUtil.canCrit(player)) return;
        if (player.getAttackStrengthScale(0.5f) < 0.9f) return;

        client.gameMode.attack(player, living);
        player.swing(InteractionHand.MAIN_HAND);
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.triggerBotEnabled = !BiwerVisualsClient.CONFIG.triggerBotEnabled;
        target = null;
        if (client != null && client.player != null) {
            String state = BiwerVisualsClient.CONFIG.triggerBotEnabled ? "§aВКЛ" : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[TriggerBot] " + state));
        }
    }
}