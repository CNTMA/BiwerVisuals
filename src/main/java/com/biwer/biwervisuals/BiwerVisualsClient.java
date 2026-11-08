package com.biwer.biwervisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class BiwerVisualsClient implements ClientModInitializer {
    private static KeyMapping openGuiKey;

    public static ModConfig CONFIG = new ModConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File("config");

    @Override
    public void onInitializeClient() {
        // 1. Регистрация клавиши
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("biwervisuals", "main")
        );

        EspRenderer.init();

        openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.biwervisuals.opengui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        // 2. Тик (меню и KillAura)
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
                        // Ждем
                    } else {
                        if (client.player.getAttackStrengthScale(0.5f) >= 1.0f) {
                            client.gameMode.attack(client.player, target);
                            client.player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }
            }
        });


        // 4. Команды
        ClientSendMessageEvents.ALLOW_CHAT.register((message) -> {
            if (message.startsWith(".")) {
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
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§c[BiwerVisuals] Используйте .cfg save <имя>, .cfg load <имя> или .cfg dir"));
                }
                return false;
            }
            return true;
        });
    }

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