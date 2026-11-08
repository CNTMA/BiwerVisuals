package com.biwer.biwervisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

public class BiwerVisualsClient implements ClientModInitializer {
    private static KeyMapping openGuiKey;

    public static ModConfig CONFIG = new ModConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File("config");

    @Override
    public void onInitializeClient() {
        // 1. Регистрация клавиши меню
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("biwervisuals", "main")
        );

        openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.biwervisuals.opengui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        // 2. Инициализация ESP
        EspRenderer.init();

        // 3. Инициализация менеджера биндов
        BindManager.init();

        // 4. Загрузка биндов из конфига при старте
        for (Map.Entry<String, Integer> entry : CONFIG.binds.entrySet()) {
            BindManager.addBind(entry.getKey(), entry.getValue());
        }

        // 5. Тик (открытие меню и KillAura)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ClickGuiScreen(null));
                }
            }

            // --- KILLAURA ---
            if (client.player == null || client.level == null) return;

            if (CONFIG.killAuraEnabled) {
                if (CONFIG.dontHitItemUse && client.player.isUsingItem()) return;
                if (CONFIG.dontHitContainer && client.screen != null) return;

                LivingEntity target = findTarget(client);
                if (target != null) {
                    double dx = target.getX() - client.player.getX();
                    double dy = (target.getY() + target.getEyeHeight()) - (client.player.getY() + client.player.getEyeHeight());
                    double dz = target.getZ() - client.player.getZ();
                    double dist = Math.sqrt(dx * dx + dz * dz);

                    float targetYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
                    float targetPitch = (float) -(Math.atan2(dy, dist) * 180.0 / Math.PI);

                    if (CONFIG.aimMode != 0) {
                        float speed = (CONFIG.aimMode == 2) ? 0.8f : 0.25f;
                        float currentYaw = client.player.getYRot();
                        float currentPitch = client.player.getXRot();

                        float deltaYaw = targetYaw - currentYaw;
                        while (deltaYaw > 180) deltaYaw -= 360;
                        while (deltaYaw < -180) deltaYaw += 360;
                        float deltaPitch = targetPitch - currentPitch;

                        client.player.setYRot(currentYaw + deltaYaw * speed);
                        client.player.setXRot(currentPitch + deltaPitch * speed);
                    }

                    if (CONFIG.onlyCriticals && client.player.onGround()) {
                        // Ждем прыжка для крита
                    } else {
                        if (client.player.getAttackStrengthScale(0.5f) >= 1.0f) {
                            client.gameMode.attack(client.player, target);
                            client.player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }
            }
        });

        // 6. Обработка команд
        ClientSendMessageEvents.ALLOW_CHAT.register((message) -> {
            if (message.startsWith(".")) {
                // Конфиги
                if (message.startsWith(".cfg save ")) {
                    String name = message.substring(".cfg save ".length()).trim();
                    if (!name.isEmpty()) saveConfig(name);
                    return false;
                }
                if (message.startsWith(".cfg load ")) {
                    String name = message.substring(".cfg load ".length()).trim();
                    if (!name.isEmpty()) loadConfig(name);
                    return false;
                }
                if (message.equalsIgnoreCase(".cfg dir")) {
                    openConfigFolder();
                    return false;
                }

                // --- БИНДЫ ---
                if (message.startsWith(".bind add ")) {
                    String[] parts = message.split(" ");
                    if (parts.length >= 4) {
                        String func = parts[2];
                        String keyName = parts[3].toUpperCase();
                        int keyCode = getKeyCode(keyName);

                        if (keyCode != -1) {
                            BindManager.addBind(func, keyCode);
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.sendSystemMessage(
                                        Component.literal("§a[Bind] Функция §e" + func + " §aуспешно привязана к клавише §e" + keyName)
                                );
                            }
                        } else {
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.sendSystemMessage(
                                        Component.literal("§c[Bind] Неизвестная клавиша: §e" + keyName)
                                );
                            }
                        }
                    } else {
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.sendSystemMessage(
                                    Component.literal("§c[Bind] Используйте: §e.bind add <функция> <клавиша>")
                            );
                        }
                    }
                    // Закрываем чат, чтобы поле ввода очистилось
                    if (Minecraft.getInstance().screen instanceof net.minecraft.client.gui.screens.ChatScreen) {
                        Minecraft.getInstance().setScreen(null);
                    }
                    return false;
                }

                if (message.startsWith(".bind remove ")) {
                    String[] parts = message.split(" ");
                    if (parts.length >= 3) {
                        String func = parts[2];
                        BindManager.removeBind(func);
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.sendSystemMessage(
                                    Component.literal("§a[Bind] Бинд для функции §e" + func + " §aуспешно удален")
                            );
                        }
                    } else {
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.sendSystemMessage(
                                    Component.literal("§c[Bind] Используйте: §e.bind remove <функция>")
                            );
                        }
                    }
                    // Закрываем чат, чтобы поле ввода очистилось
                    if (Minecraft.getInstance().screen instanceof net.minecraft.client.gui.screens.ChatScreen) {
                        Minecraft.getInstance().setScreen(null);
                    }
                    return false;
                }

                // Подсказка для остальных команд
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§c[BiwerVisuals] Доступные команды:"));
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§e.cfg save <имя> §7- сохранить конфиг"));
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§e.cfg load <имя> §7- загрузить конфиг"));
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§e.cfg dir §7- открыть папку конфигов"));
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§e.bind add <функция> <клавиша> §7- привязать клавишу"));
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§e.bind remove <функция> §7- удалить бинд"));
                }
                return false;
            }
            return true;
        });
    }

    // Поиск цели для KillAura
    private static LivingEntity findTarget(Minecraft client) {
        Player player = client.player;
        if (player == null || client.level == null) return null;

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        double range = CONFIG.killAuraRange + CONFIG.killAuraExtraRange;

        for (var entity : client.level.getEntities(player, player.getBoundingBox().inflate(range))) {
            if (entity instanceof LivingEntity living && entity != player && living.isAlive()) {
                boolean isPlayer = living instanceof Player;
                boolean isMob = living instanceof Mob;
                boolean isAnimal = living instanceof Animal || living instanceof AmbientCreature;

                if (isPlayer && !CONFIG.targetPlayers) continue;
                if (isMob && !CONFIG.targetMobs) continue;
                if (isAnimal && !CONFIG.targetAnimals) continue;

                if (isPlayer && !CONFIG.targetNaked) {
                    boolean hasArmor = false;
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        if (!living.getItemBySlot(slot).isEmpty()) {
                            hasArmor = true;
                            break;
                        }
                    }
                    if (hasArmor) continue;
                }

                if (CONFIG.dontHitThroughWalls && !player.hasLineOfSight(living)) continue;

                double dist = player.distanceTo(living);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = living;
                }
            }
        }
        return best;
    }

    // Вспомогательный метод для получения кода клавиши
    private static int getKeyCode(String keyName) {
        return switch (keyName) {
            case "A" -> GLFW.GLFW_KEY_A;
            case "B" -> GLFW.GLFW_KEY_B;
            case "C" -> GLFW.GLFW_KEY_C;
            case "D" -> GLFW.GLFW_KEY_D;
            case "E" -> GLFW.GLFW_KEY_E;
            case "F" -> GLFW.GLFW_KEY_F;
            case "G" -> GLFW.GLFW_KEY_G;
            case "H" -> GLFW.GLFW_KEY_H;
            case "I" -> GLFW.GLFW_KEY_I;
            case "J" -> GLFW.GLFW_KEY_J;
            case "K" -> GLFW.GLFW_KEY_K;
            case "L" -> GLFW.GLFW_KEY_L;
            case "M" -> GLFW.GLFW_KEY_M;
            case "N" -> GLFW.GLFW_KEY_N;
            case "O" -> GLFW.GLFW_KEY_O;
            case "P" -> GLFW.GLFW_KEY_P;
            case "Q" -> GLFW.GLFW_KEY_Q;
            case "R" -> GLFW.GLFW_KEY_R;
            case "S" -> GLFW.GLFW_KEY_S;
            case "T" -> GLFW.GLFW_KEY_T;
            case "U" -> GLFW.GLFW_KEY_U;
            case "V" -> GLFW.GLFW_KEY_V;
            case "W" -> GLFW.GLFW_KEY_W;
            case "X" -> GLFW.GLFW_KEY_X;
            case "Y" -> GLFW.GLFW_KEY_Y;
            case "Z" -> GLFW.GLFW_KEY_Z;
            case "1" -> GLFW.GLFW_KEY_1;
            case "2" -> GLFW.GLFW_KEY_2;
            case "3" -> GLFW.GLFW_KEY_3;
            case "4" -> GLFW.GLFW_KEY_4;
            case "5" -> GLFW.GLFW_KEY_5;
            case "6" -> GLFW.GLFW_KEY_6;
            case "7" -> GLFW.GLFW_KEY_7;
            case "8" -> GLFW.GLFW_KEY_8;
            case "9" -> GLFW.GLFW_KEY_9;
            case "0" -> GLFW.GLFW_KEY_0;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "SHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "CTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "ALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            default -> -1;
        };
    }

    private void saveConfig(String name) {
        try {
            if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
            File file = new File(CONFIG_DIR, name + ".json");
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(CONFIG, writer);
            }
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("§a[Config] Сохранено: " + name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadConfig(String name) {
        try {
            File file = new File(CONFIG_DIR, name + ".json");
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    CONFIG = GSON.fromJson(reader, ModConfig.class);

                    // Перезагружаем бинды из нового конфига
                    BindManager.clearAll();
                    for (Map.Entry<String, Integer> entry : CONFIG.binds.entrySet()) {
                        BindManager.addBind(entry.getKey(), entry.getValue());
                    }

                    if (Minecraft.getInstance().screen instanceof ClickGuiScreen) {
                        Minecraft.getInstance().setScreen(new ClickGuiScreen(null));
                    }
                }
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§a[Config] Загружено: " + name));
                }
            } else {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§c[Config] Файл не найден: " + name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openConfigFolder() {
        try {
            File folder = new File(CONFIG_DIR.getAbsolutePath());
            if (!folder.exists()) folder.mkdirs();

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("explorer.exe \"" + folder.getAbsolutePath() + "\"");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open \"" + folder.getAbsolutePath() + "\"");
            } else {
                Runtime.getRuntime().exec("xdg-open \"" + folder.getAbsolutePath() + "\"");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}