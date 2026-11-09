package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Speed — мягкое ускорение, замаскированное под ванильный эффект скорости.
 *
 * Принцип: скорость не трогается напрямую (никаких setDeltaMovement и фейковых
 * пакетов позиции). Вместо этого на атрибут MOVEMENT_SPEED вешается transient
 * модификатор ADD_MULTIPLIED_BASE — ровно так, как это делает зелье Speed I
 * (+20% за уровень). Профиль движения остаётся обычным, без рывков.
 *
 * Почему не палевно:
 *  - ускорение только на земле: вода, лава, пассажир, полёт — выкл.;
 *  - не работает в приседе;
 *  - плавный разгон и затухание (~0.5 с), поэтому дельта скорости без скачка;
 *  - не накладывается поверх настоящего зелья скорости (можно отключить в меню);
 *  - ничего не отправляется на сервер.
 */
public class Speed {
    private static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath("biwervisuals", "speed_boost");

    /** Текущий сглаженный процент ускорения. */
    private static float currentPercent = 0.0f;

    /** Шаг разгона/затухания за тик, в процентах. */
    private static final float RAMP_STEP = 2.0f;

    /** Жёсткий предел, чтобы модуль не превращался в блат. */
    private static final float MAX_PERCENT = 60.0f;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(Speed::onTick);
    }

    private static void onTick(Minecraft client) {
        if (client.player == null) {
            // Мир не загружен: модификатор живёт вместе с игроком, чистить нечего.
            currentPercent = 0.0f;
            return;
        }

        if (!BiwerVisualsClient.CONFIG.speedEnabled || !isAllowed(client)) {
            fadeOut(client);
            return;
        }

        float target = clampPercent(BiwerVisualsClient.CONFIG.speedPercent);

        if (BiwerVisualsClient.CONFIG.speedSmooth) {
            if (currentPercent < target) currentPercent = Math.min(target, currentPercent + RAMP_STEP);
            else if (currentPercent > target) currentPercent = Math.max(target, currentPercent - RAMP_STEP);
        } else {
            currentPercent = target;
        }

        apply(client, currentPercent);
    }

    /** Условия, при которых ускорение выглядит и ощущается естественно. */
    private static boolean isAllowed(Minecraft client) {
        var player = client.player;
        if (player == null || client.level == null) return false;

        // Полёт (креатив/спектатор/abilities) — не наш случай
        if (player.getAbilities().flying) return false;
        if (player.isSpectator()) return false;

        // Только твёрдая земля под ногами
        if (!player.onGround()) return false;
        if (player.isInWater() || player.isInLava()) return false;

        // Транспорт и присед не ускоряем
        if (player.isPassenger()) return false;
        if (player.isShiftKeyDown()) return false;

        if (BiwerVisualsClient.CONFIG.speedOnlySprint && !player.isSprinting()) return false;

        // Зелье скорости уже даёт буст — поверх делать нечего
        if (BiwerVisualsClient.CONFIG.speedSkipIfPotion && player.hasEffect(MobEffects.SPEED)) return false;

        return true;
    }

    /** Плавно (или сразу) снимаем буст; в воздухе просто обнуляем. */
    private static void fadeOut(Minecraft client) {
        if (currentPercent <= 0.0f) {
            clear(client);
            return;
        }

        boolean grounded = client.player != null
                && client.player.onGround()
                && !client.player.isInWater()
                && !client.player.isInLava();

        if (BiwerVisualsClient.CONFIG.speedSmooth && grounded) {
            currentPercent = Math.max(0.0f, currentPercent - RAMP_STEP * 2.0f);
        } else {
            currentPercent = 0.0f;
        }

        if (currentPercent > 0.0f) {
            apply(client, currentPercent);
        } else {
            currentPercent = 0.0f;
            clear(client);
        }
    }

    private static void apply(Minecraft client, float percent) {
        AttributeInstance attribute = client.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;

        double amount = percent / 100.0;

        AttributeModifier existing = attribute.getModifier(MODIFIER_ID);
        if (existing != null && Math.abs(existing.amount() - amount) < 1.0e-4) {
            return; // нужное значение уже стоит — лишний раз не дёргаем
        }

        attribute.addOrUpdateTransientModifier(new AttributeModifier(
                MODIFIER_ID,
                amount,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
    }

    private static void clear(Minecraft client) {
        if (client.player == null) return;
        AttributeInstance attribute = client.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && attribute.getModifier(MODIFIER_ID) != null) {
            attribute.removeModifier(MODIFIER_ID);
        }
    }

    private static float clampPercent(float percent) {
        if (percent < 0.0f) return 0.0f;
        return Math.min(percent, MAX_PERCENT);
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.speedEnabled = !BiwerVisualsClient.CONFIG.speedEnabled;
        if (client.player != null) {
            String state = BiwerVisualsClient.CONFIG.speedEnabled
                    ? "§aВКЛ §7(+" + (int) clampPercent(BiwerVisualsClient.CONFIG.speedPercent) + "%)"
                    : "§cВЫКЛ";
            client.player.sendSystemMessage(Component.literal("§e[Speed] " + state));
        }
    }
}