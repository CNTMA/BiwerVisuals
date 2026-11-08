package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class BindManager {
    private static final Map<String, Integer> BINDS = new HashMap<>();
    private static final Map<String, Boolean> WAS_PRESSED = new HashMap<>();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Получаем handle окна (в 26.1.2 это может быть getWindow().getWindow() или getWindow().getHandle())
            long window = GLFW.glfwGetCurrentContext(); // Если не работает, попробуйте getHandle()
            if (window == 0) return;

            for (Map.Entry<String, Integer> entry : BINDS.entrySet()) {
                String functionName = entry.getKey();
                int keyCode = entry.getValue();

                boolean isPressed = GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;

                if (isPressed && !WAS_PRESSED.getOrDefault(functionName, false)) {
                    toggleFunction(functionName, client);
                }

                WAS_PRESSED.put(functionName, isPressed);
            }
        });
    }

    public static boolean addBind(String functionName, int keyCode) {
        String lower = functionName.toLowerCase();
        if (getAvailableFunctions().length > 0) {
            for (String func : getAvailableFunctions()) {
                if (func.equals(lower)) {
                    BINDS.put(lower, keyCode);
                    WAS_PRESSED.put(lower, false);
                    BiwerVisualsClient.CONFIG.binds.put(lower, keyCode);
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeBind(String functionName) {
        String lower = functionName.toLowerCase();
        BINDS.remove(lower);
        WAS_PRESSED.remove(lower);
        BiwerVisualsClient.CONFIG.binds.remove(lower);
    }

    public static void clearAll() {
        BINDS.clear();
        WAS_PRESSED.clear();
    }

    public static String[] getAvailableFunctions() {
        return new String[]{"killaura", "ambience", "viewmodel", "entityesp", "tags", "fly", "sprint"};
    }

    private static void toggleFunction(String functionName, Minecraft client) {
        boolean isEnabled = false;
        switch (functionName) {
            case "killaura" -> { BiwerVisualsClient.CONFIG.killAuraEnabled = !BiwerVisualsClient.CONFIG.killAuraEnabled; isEnabled = BiwerVisualsClient.CONFIG.killAuraEnabled; }
            case "ambience" -> { BiwerVisualsClient.CONFIG.ambienceEnabled = !BiwerVisualsClient.CONFIG.ambienceEnabled; isEnabled = BiwerVisualsClient.CONFIG.ambienceEnabled; }
            case "viewmodel" -> { BiwerVisualsClient.CONFIG.viewmodelEnabled = !BiwerVisualsClient.CONFIG.viewmodelEnabled; isEnabled = BiwerVisualsClient.CONFIG.viewmodelEnabled; }
            case "entityesp" -> { BiwerVisualsClient.CONFIG.espEnabled = !BiwerVisualsClient.CONFIG.espEnabled; isEnabled = BiwerVisualsClient.CONFIG.espEnabled; }
            case "tags" -> { BiwerVisualsClient.CONFIG.tagsEnabled = !BiwerVisualsClient.CONFIG.tagsEnabled; isEnabled = BiwerVisualsClient.CONFIG.tagsEnabled; }
            case "fly" -> { Fly.toggle(client); return; } // Fly сам выведет сообщение
            case "sprint" -> { Sprint.toggle(client); return; }
            default -> { return; }
        }

        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("§e[Bind] " + functionName + ": " + (isEnabled ? "§aВКЛ" : "§cВЫКЛ")));
        }
    }
}