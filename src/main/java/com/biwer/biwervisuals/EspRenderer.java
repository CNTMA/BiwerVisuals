package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EspRenderer {
    public static void init() {
        LevelRenderEvents.BEFORE_GIZMOS.register(EspRenderer::onRenderGizmos);
    }

    private static void onRenderGizmos(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        // --- ESP (Боксы) ---
        if (BiwerVisualsClient.CONFIG.espEnabled) {
            for (var entity : client.level.entitiesForRendering()) {
                if (!(entity instanceof net.minecraft.world.entity.LivingEntity living) || entity == client.player) continue;

                boolean isPlayer = living instanceof net.minecraft.world.entity.player.Player;
                boolean isMob = living instanceof net.minecraft.world.entity.Mob;
                boolean isAnimal = living instanceof net.minecraft.world.entity.animal.Animal || living instanceof net.minecraft.world.entity.ambient.AmbientCreature;

                if (isPlayer && !BiwerVisualsClient.CONFIG.espPlayers) continue;
                if (isMob && !BiwerVisualsClient.CONFIG.espMobs) continue;
                if (isAnimal && !BiwerVisualsClient.CONFIG.espAnimals) continue;

                if (isPlayer && BiwerVisualsClient.CONFIG.espNaked) {
                    boolean hasArmor = false;
                    for (var slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                        if (!living.getItemBySlot(slot).isEmpty()) { hasArmor = true; break; }
                    }
                    if (hasArmor) continue;
                }

                AABB box = living.getBoundingBox().inflate(0.1);
                int color = ARGB.color(128, 255, 0, 255);
                Gizmos.cuboid(box, GizmoStyle.stroke(color)).setAlwaysOnTop();
            }
        }

        // --- TAGS (Текст по центру над головой) ---
        if (BiwerVisualsClient.CONFIG.tagsEnabled) {
            for (var entity : client.level.entitiesForRendering()) {
                if (!(entity instanceof net.minecraft.world.entity.LivingEntity living) || entity == client.player) continue;

                boolean isPlayer = living instanceof net.minecraft.world.entity.player.Player;
                boolean isMob = living instanceof net.minecraft.world.entity.Mob;
                boolean isAnimal = living instanceof net.minecraft.world.entity.animal.Animal || living instanceof net.minecraft.world.entity.ambient.AmbientCreature;

                if (isPlayer && !BiwerVisualsClient.CONFIG.tagsPlayers) continue;
                if (isMob && !BiwerVisualsClient.CONFIG.tagsMobs) continue;
                if (isAnimal && !BiwerVisualsClient.CONFIG.tagsAnimals) continue;

                String text = isPlayer ? living.getName().getString() : living.getType().getDescription().getString();

                // Вычисляем точный центр над головой
                double x = living.getX();
                double y = living.getY() + living.getBbHeight() + 0.5; // 0.5 блока над макушкой
                double z = living.getZ();
                Vec3 pos = new Vec3(x, y, z);

                // Используем billboardText с точной позицией и стилем по центру
                Gizmos.billboardText(
                        text,
                        pos,
                        TextGizmo.Style.whiteAndCentered().withScale(0.5f)
                ).setAlwaysOnTop();
            }
        }
    }
}