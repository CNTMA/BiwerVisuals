package com.biwer.biwervisuals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class BindManager {
    private static final Map<String, KeyMapping> KEY_MAPPINGS = new HashMap<>();

    public static void init() {
        // Регистрируем событие тика для проверки нажатий
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Map.Entry<String, KeyMapping> entry : KEY_MAPPINGS.entrySet()) {
                String functionName = entry.getKey();
                KeyMapping keyMapping = entry.getValue();
                while (keyMapping.consumeClick()) {
                    toggleFunction(functionName, client);
                }
            }
        });
    }

    public static void addBind(String functionName, int keyCode) {
        // Удаляем старый бинд, если есть
        removeBind(functionName);

        // Создаем KeyMapping для этой функции
        KeyMapping.Category category = KeyMapping.Category.register(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("biwervisuals", "binds")
        );

        KeyMapping keyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.biwervisuals.bind." + functionName.toLowerCase(),
                InputConstants.Type.KEYSYM,
                keyCode,
                category
        ));

        KEY_MAPPINGS.put(functionName, keyMapping);
        BiwerVisualsClient.CONFIG.binds.put(functionName, keyCode);
    }

    public static void removeBind(String functionName) {
        if (KEY_MAPPINGS.containsKey(functionName)) {
            // В Minecraft 26.1 нет простого способа удалить KeyMapping из реестра,
            // поэтому мы просто убираем его из нашего списка и конфига.
            KEY_MAPPINGS.remove(functionName);
            BiwerVisualsClient.CONFIG.binds.remove(functionName);
        }
    }

    private static void toggleFunction(String functionName, net.minecraft.client.Minecraft client) {
        switch (functionName) {
            case "KillAura" -> BiwerVisualsClient.CONFIG.killAuraEnabled = !BiwerVisualsClient.CONFIG.killAuraEnabled;
            case "Ambience" -> BiwerVisualsClient.CONFIG.ambienceEnabled = !BiwerVisualsClient.CONFIG.ambienceEnabled;
            case "Viewmodel" -> BiwerVisualsClient.CONFIG.viewmodelEnabled = !BiwerVisualsClient.CONFIG.viewmodelEnabled;
            case "ESP" -> BiwerVisualsClient.CONFIG.espEnabled = !BiwerVisualsClient.CONFIG.espEnabled;
            case "Tags" -> BiwerVisualsClient.CONFIG.tagsEnabled = !BiwerVisualsClient.CONFIG.tagsEnabled;
            default -> {
                // Если функции нет в списке, ничего не делаем
                return;
            }
        }

        // Выводим сообщение в чат (для обратной связи)
        if (client.player != null) {
            boolean isEnabled = switch (functionName) {
                case "KillAura" -> BiwerVisualsClient.CONFIG.killAuraEnabled;
                case "Ambience" -> BiwerVisualsClient.CONFIG.ambienceEnabled;
                case "Viewmodel" -> BiwerVisualsClient.CONFIG.viewmodelEnabled;
                case "ESP" -> BiwerVisualsClient.CONFIG.espEnabled;
                case "Tags" -> BiwerVisualsClient.CONFIG.tagsEnabled;
                default -> false;
            };
            client.player.sendSystemMessage(Component.literal("§e[Bind] " + functionName + ": " + (isEnabled ? "§aВКЛ" : "§cВЫКЛ")));
        }
    }

    public static void clearAll() {
        KEY_MAPPINGS.clear();
    }
}