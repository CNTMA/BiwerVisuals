package com.biwer.biwervisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * HUD клиента: 7 элементов, позиции которых задаются в редакторе (перетаскивание)
 * или командами в чате.
 *
 * Команды:
 *   .hud                  — открыть редактор
 *   .hud list             — показать координаты элементов
 *   .hud reset            — вернуть всё на места по умолчанию
 *   .hud <элемент> <x> <y> — поставить элемент в точку
 *
 * Позиции хранятся в config/hud.json и сохраняются сразу после перетаскивания.
 */
public class Hud implements HudElement {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config", "hud.json");

    private static final int LINE = 10;

    /** Границы элементов в экранных координатах — заполняются при отрисовке. */
    private static final Map<Element, int[]> BOUNDS = new EnumMap<>(Element.class);

    private static boolean pendingEditor = false;

    public enum Element {
        WATERMARK("WaterMark", 6, 6),
        BINDS("Binds", 6, 22),
        POTIONS("Potions", 6, 96),
        ARRAY_LIST("Array List", -6, 6),
        COORDINATES("Cordinates", 6, 220),
        RADAR("Radar", 6, 262),
        TARGET_HUD("TargetHUD", 190, 6),
        ARMOR("Armor", 190, 60),
        ITEMS("Items", 190, 100),
        ENVIRONMENT("Environment", 190, 140),
        COOLDOWNS("Cooldowns", 190, 180);

        public final String title;
        public final float defX;
        public final float defY;

        Element(String title, float defX, float defY) {
            this.title = title;
            this.defX = defX;
            this.defY = defY;
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static void init() {
        load();
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("biwervisuals", "hud"), new Hud());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingEditor && client.screen == null) {
                pendingEditor = false;
                client.setScreen(new HudEditorScreen(null));
            }
        });
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!BiwerVisualsClient.CONFIG.hudEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;
        drawAll(graphics, mc, false);
    }

    // ================= CLI =================

    public static void handleCommand(String message) {
        Minecraft mc = Minecraft.getInstance();
        String[] parts = message.trim().split("\\s+");

        if (parts.length == 1) {
            pendingEditor = true;
            reply("§a[ HUD ] §7Открываю редактор...");
            return;
        }

        String sub = parts[1].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "editor", "edit" -> {
                pendingEditor = true;
                reply("§a[ HUD ] §7Открываю редактор...");
            }
            case "list" -> {
                reply("§a[ HUD ] §7Элементы:");
                for (Element e : Element.values()) {
                    float[] p = pos(e, mc);
                    reply("§7- §e" + e.key() + " §7(" + e.title + ") §f" + (int) p[0] + " §7/ §f" + (int) p[1]);
                }
                reply("§7Переместить: §e.hud <элемент> <x> <y>");
            }
            case "reset" -> {
                BiwerVisualsClient.CONFIG.hudPositions.clear();
                BOUNDS.clear();
                save();
                reply("§a[ HUD ] §7Позиции сброшены.");
            }
            default -> {
                Element element = byKey(sub);
                if (element == null) {
                    reply("§c[ HUD ] §7Нет такого элемента. Список: §e.hud list");
                    return;
                }
                if (parts.length < 4) {
                    reply("§c[ HUD ] §7Используй: §e.hud " + element.key() + " <x> <y>");
                    return;
                }
                try {
                    float x = Float.parseFloat(parts[2]);
                    float y = Float.parseFloat(parts[3]);
                    setPos(element, x, y);
                    save();
                    reply("§a[ HUD ] §7" + element.title + " → §f" + (int) x + "§7, §f" + (int) y);
                } catch (NumberFormatException ex) {
                    reply("§c[ HUD ] §7Координаты должны быть числами.");
                }
            }
        }
    }

    private static Element byKey(String key) {
        for (Element e : Element.values()) {
            if (e.key().equals(key) || e.title.toLowerCase(Locale.ROOT).equals(key)) return e;
        }
        return null;
    }

    private static void reply(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.sendSystemMessage(Component.literal(text));
    }

    // ================= Геометрия и хранение =================

    public static float[] pos(Element e, Minecraft mc) {
        if (BiwerVisualsClient.CONFIG.hudPositions == null) {
            BiwerVisualsClient.CONFIG.hudPositions = new HashMap<>();
        }
        float[] p = BiwerVisualsClient.CONFIG.hudPositions.get(e.key());
        if (p == null || p.length < 2) {
            float defX = e.defX;
            if (e == Element.ARRAY_LIST && defX < 0) {
                defX = mc.getWindow().getGuiScaledWidth() + defX;
            }
            p = new float[]{defX, e.defY};
            BiwerVisualsClient.CONFIG.hudPositions.put(e.key(), p);
        }
        return p;
    }

    public static void setPos(Element e, float x, float y) {
        if (BiwerVisualsClient.CONFIG.hudPositions == null) {
            BiwerVisualsClient.CONFIG.hudPositions = new HashMap<>();
        }
        BiwerVisualsClient.CONFIG.hudPositions.put(e.key(), new float[]{x, y});
    }

    public static void resetElement(Element e) {
        BiwerVisualsClient.CONFIG.hudPositions.remove(e.key());
        BOUNDS.clear();
    }

    public static int[] bounds(Element e) {
        return BOUNDS.getOrDefault(e, new int[]{0, 0, 24, LINE});
    }

    public static void load() {
        try {
            if (!FILE.exists()) return;
            Type type = new TypeToken<Map<String, float[]>>() {}.getType();
            Map<String, float[]> loaded = GSON.fromJson(new FileReader(FILE), type);
            if (loaded != null) {
                BiwerVisualsClient.CONFIG.hudPositions.clear();
                BiwerVisualsClient.CONFIG.hudPositions.putAll(loaded);
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            if (!FILE.getParentFile().exists()) FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(BiwerVisualsClient.CONFIG.hudPositions, writer);
            }
        } catch (Exception ignored) {
        }
    }

    // ================= Отрисовка =================

    public static void drawAll(GuiGraphicsExtractor g, Minecraft mc, boolean editor) {
        for (Element e : Element.values()) {
            drawElement(e, g, mc, editor);
        }
    }

    private static void drawElement(Element e, GuiGraphicsExtractor g, Minecraft mc, boolean editor) {
        float[] p = pos(e, mc);
        int x = Math.round(p[0]);
        int y = Math.round(p[1]);

        int[] size = switch (e) {
            case WATERMARK -> watermark(g, mc, x, y);
            case BINDS -> binds(g, mc, x, y, editor);
            case POTIONS -> potions(g, mc, x, y, editor);
            case ARRAY_LIST -> arrayList(g, mc, x, y, editor);
            case COORDINATES -> coordinates(g, mc, x, y);
            case RADAR -> radar(g, mc, x, y);
            case TARGET_HUD -> targetHud(g, mc, x, y, editor);
            case ARMOR -> armor(g, mc, x, y);
            case ITEMS -> items(g, mc, x, y);
            case ENVIRONMENT -> environment(g, mc, x, y);
            case COOLDOWNS -> cooldowns(g, mc, x, y);
        };

        BOUNDS.put(e, new int[]{x, y, size[0], size[1]});

        if (editor) {
            int color = 0xFF00E5FF;
            g.fill(x - 2, y - 2, x + size[0] + 2, y - 1, color);
            g.fill(x - 2, y + size[1] + 1, x + size[0] + 2, y + size[1] + 2, color);
            g.fill(x - 2, y - 1, x - 1, y + size[1] + 1, color);
            g.fill(x + size[0] + 1, y - 1, x + size[0] + 2, y + size[1] + 1, color);
        }
    }

    /** Броня (порт ArmorWidget): слоты FEET..HEAD с иконками и прочностью. */
    private static int[] armor(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        if (mc.player == null) return new int[]{0, 0};

        net.minecraft.world.entity.EquipmentSlot[] slots = {
                net.minecraft.world.entity.EquipmentSlot.FEET,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.HEAD
        };

        int count = 0;
        for (net.minecraft.world.entity.EquipmentSlot slot : slots) {
            if (!mc.player.getItemBySlot(slot).isEmpty()) count++;
        }
        if (count == 0) return new int[]{0, 0};

        int width = 4 + count * 18;
        int height = 22;
        g.fill(x, y, x + width, y + height, 0x90000000);

        int index = 0;
        for (net.minecraft.world.entity.EquipmentSlot slot : slots) {
            net.minecraft.world.item.ItemStack stack = mc.player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            int ix = x + 3 + index * 18;
            int iy = y + 3;
            g.item(stack, ix, iy);
            g.itemDecorations(mc.font, stack, ix, iy);
            index++;
        }
        return new int[]{width, height};
    }

    /** Хотбар (порт ItemsWidget): 9 слотов с подсветкой выбранного. */
    private static int[] items(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        if (mc.player == null) return new int[]{0, 0};

        int width = 4 + 9 * 18;
        int height = 22;
        g.fill(x, y, x + width, y + height, 0x90000000);

        for (int i = 0; i < 9; i++) {
            net.minecraft.world.item.ItemStack stack = mc.player.getInventory().getItem(i);
            int ix = x + 3 + i * 18;
            int iy = y + 3;
            g.item(stack, ix, iy);
            if (!stack.isEmpty()) {
                g.itemDecorations(mc.font, stack, ix, iy);
            }
        }

        int selected = mc.player.getInventory().getSelectedSlot();
        g.fill(x + 2 + selected * 18, y + 2, x + 20 + selected * 18, y + 20, 0x40FFFFFF);
        return new int[]{width, height};
    }

    /** Окружение (порт EnvironmentWidget): координаты, измерение, погода, FPS. */
    private static int[] environment(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        if (mc.player == null || mc.level == null) return new int[]{0, 0};

        String dimension = String.valueOf(mc.level.dimension().identifier().getPath());
        String weather = mc.level.isThundering() ? "Гроза" : mc.level.isRaining() ? "Дождь" : "Ясно";

        String[] lines = {
                "XYZ: " + mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ(),
                "Dim: " + dimension,
                "Weather: " + weather,
                "FPS: " + mc.getFps()
        };

        int width = 0;
        for (String line : lines) {
            width = Math.max(width, mc.font.width(line));
        }
        width += 8;
        int height = lines.length * 10 + 4;

        g.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x90000000);
        int lineY = y;
        for (String line : lines) {
            g.text(mc.font, line, x + 2, lineY, 0xFFFFFFFF, false);
            lineY += 10;
        }
        return new int[]{width, height};
    }

    /** Кулдауны (порт CooldownsWidget): актуальные задержки предметов. */
    private static int[] cooldowns(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        if (mc.player == null) return new int[]{0, 0};

        net.minecraft.world.item.ItemStack[] stacks = {
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_PEARL),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CHORUS_FRUIT),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.MACE),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WIND_CHARGE),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIREWORK_ROCKET)
        };

        java.util.List<net.minecraft.world.item.ItemStack> active = new java.util.ArrayList<>();
        for (net.minecraft.world.item.ItemStack stack : stacks) {
            if (mc.player.getCooldowns().isOnCooldown(stack)) active.add(stack);
        }
        if (active.isEmpty()) return new int[]{0, 0};

        int width = 0;
        for (net.minecraft.world.item.ItemStack stack : active) {
            int percent = Math.round(mc.player.getCooldowns().getCooldownPercent(stack, 0.0f) * 100.0f);
            String label = stack.getHoverName().getString() + " " + percent + "%";
            width = Math.max(width, 22 + mc.font.width(label));
        }
        width += 5;
        int height = 4 + active.size() * 12;

        g.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x90000000);
        int rowY = y;
        for (net.minecraft.world.item.ItemStack stack : active) {
            int percent = Math.round(mc.player.getCooldowns().getCooldownPercent(stack, 0.0f) * 100.0f);
            g.item(stack, x + 2, rowY - 1);
            g.text(mc.font, stack.getHoverName().getString() + " " + percent + "%", x + 22, rowY + 1, 0xFFFFFFFF, false);
            rowY += 12;
        }
        return new int[]{width, height};
    }

    private static int[] watermark(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        Font font = mc.font;
        String a = "Biwer";
        String b = "Visuals";
        String c = " v1.0.0";
        g.text(font, a, x, y, 0xFF39D0FF, true);
        g.text(font, b, x + font.width(a), y, 0xFFFFFFFF, true);
        g.text(font, c, x + font.width(a) + font.width(b), y, 0xFF888888, true);
        return new int[]{font.width(a + b + c), font.lineHeight};
    }

    private static int[] binds(GuiGraphicsExtractor g, Minecraft mc, int x, int y, boolean editor) {
        Font font = mc.font;
        List<String> lines = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : new TreeMap<>(BiwerVisualsClient.CONFIG.binds).entrySet()) {
            String name = entry.getKey();
            lines.add(name + " [" + keyName(entry.getValue()) + "]");
            colors.add(isEnabled(name) ? 0xFF55FF55 : 0xFF9A9AA8);
        }

        if (lines.isEmpty()) {
            if (!editor) return new int[]{0, 0};
            g.text(font, "Binds (пусто)", x, y, 0xFF777777, true);
            return new int[]{font.width("Binds (пусто)"), font.lineHeight};
        }

        int width = 0;
        for (String line : lines) width = Math.max(width, font.width(line));

        for (int i = 0; i < lines.size(); i++) {
            g.text(font, lines.get(i), x, y + i * LINE, colors.get(i), true);
        }
        return new int[]{width, lines.size() * LINE};
    }

    private static int[] potions(GuiGraphicsExtractor g, Minecraft mc, int x, int y, boolean editor) {
        Font font = mc.font;
        List<MobEffectInstance> effects = new ArrayList<>(mc.player.getActiveEffects());
        effects.sort(Comparator.comparingInt(MobEffectInstance::getDuration));

        if (effects.isEmpty()) {
            if (!editor) return new int[]{0, 0};
            g.text(font, "Potions (нет)", x, y, 0xFF777777, true);
            return new int[]{font.width("Potions (нет)"), font.lineHeight};
        }

        List<String> lines = new ArrayList<>();
        for (MobEffectInstance effect : effects) {
            String name = effect.getEffect().value().getDisplayName().getString();
            String time = formatDuration(effect.getDuration());
            String roman = roman(effect.getAmplifier() + 1);
            lines.add(name + (roman.isEmpty() ? "" : " " + roman) + " " + time);
        }

        int width = 0;
        for (String line : lines) width = Math.max(width, font.width(line));

        for (int i = 0; i < lines.size(); i++) {
            g.text(font, lines.get(i), x, y + i * LINE, 0xFFB388FF, true);
        }
        return new int[]{width, lines.size() * LINE};
    }

    private static int[] arrayList(GuiGraphicsExtractor g, Minecraft mc, int x, int y, boolean editor) {
        Font font = mc.font;
        List<String> modules = enabledModules();

        if (modules.isEmpty()) {
            if (!editor) return new int[]{0, 0};
            g.text(font, "Array List (пусто)", x - font.width("Array List (пусто)"), y, 0xFF777777, true);
            return new int[]{font.width("Array List (пусто)"), font.lineHeight};
        }

        int width = 0;
        for (int i = 0; i < modules.size(); i++) {
            String name = modules.get(i);
            int lineWidth = font.width(name);
            width = Math.max(width, lineWidth);
            g.text(font, name, x - lineWidth, y + i * LINE, 0xFF6FE3FF, true);
        }
        return new int[]{width, modules.size() * LINE};
    }

    private static int[] coordinates(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        Font font = mc.font;
        var player = mc.player;
        String line1 = "X: " + (int) Math.floor(player.getX())
                + "  Y: " + (int) Math.floor(player.getY())
                + "  Z: " + (int) Math.floor(player.getZ());
        String line2 = "FPS: " + mc.getFps();

        g.text(font, line1, x, y, 0xFFFFFFFF, true);
        g.text(font, line2, x, y + LINE, 0xFFAAAAAA, true);
        return new int[]{Math.max(font.width(line1), font.width(line2)), LINE * 2};
    }

    private static int[] radar(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        int size = 64;
        int border = 0xFF39A7FF;

        g.fill(x, y, x + size, y + size, 0x80101018);
        g.fill(x, y, x + size, y + 1, border);
        g.fill(x, y + size - 1, x + size, y + size, border);
        g.fill(x, y, x + 1, y + size, border);
        g.fill(x + size - 1, y, x + size, y + size, border);

        var player = mc.player;
        double range = 48.0;
        float yaw = (float) Math.toRadians(player.getYRot());
        float sin = (float) Math.sin(yaw);
        float cos = (float) Math.cos(yaw);
        int cx = x + size / 2;
        int cy = y + size / 2;
        int radius = size / 2 - 2;

        for (Entity entity : mc.level.getEntities(player, player.getBoundingBox().inflate(range))) {
            if (entity == player) continue;
            boolean isPlayer = entity instanceof Player;
            boolean isMob = entity instanceof Mob;
            if (!isPlayer && !isMob) continue;

            double dx = entity.getX() - player.getX();
            double dz = entity.getZ() - player.getZ();

            double right = dx * cos + dz * sin;
            double forward = -dx * sin + dz * cos;

            int px = cx + (int) Math.round(right / range * radius);
            int py = cy - (int) Math.round(forward / range * radius);

            if (px <= x + 1 || px >= x + size - 2 || py <= y + 1 || py >= y + size - 2) continue;

            g.fill(px - 1, py - 1, px + 1, py + 1, isPlayer ? 0xFFFF5555 : 0xFFFFAA00);
        }

        g.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFF39FF88);
        return new int[]{size, size};
    }

    private static int[] targetHud(GuiGraphicsExtractor g, Minecraft mc, int x, int y, boolean editor) {
        Font font = mc.font;
        LivingEntity target = Aura.getTarget(); // ← ЗАМЕНЕНО KillAura на Aura

        if (target == null) {
            if (!editor) return new int[]{0, 0};
            g.text(font, "TargetHUD (нет цели)", x, y, 0xFF777777, true);
            return new int[]{font.width("TargetHUD (нет цели)"), font.lineHeight};
        }

        int width = 110;
        String name = target.getName().getString();
        float health = target.getHealth();
        float maxHealth = Math.max(1.0f, target.getMaxHealth());
        float ratio = Math.max(0.0f, Math.min(1.0f, health / maxHealth));

        g.fill(x, y, x + width, y + 22, 0xA0101018);
        g.text(font, name, x + 3, y + 3, 0xFFFFFFFF, true);

        int barTop = y + 14;
        g.fill(x + 3, barTop, x + width - 3, barTop + 5, 0xFF331111);
        int filled = (int) ((width - 6) * ratio);
        int barColor = ratio > 0.5f ? 0xFF55FF55 : (ratio > 0.25f ? 0xFFFFD24A : 0xFFFF5555);
        g.fill(x + 3, barTop, x + 3 + filled, barTop + 5, barColor);

        return new int[]{width, 22};
    }

    // ================= Вспомогательное =================

    private static List<String> enabledModules() {
        ModConfig c = BiwerVisualsClient.CONFIG;
        List<String> list = new ArrayList<>();
        if (c.auraEnabled) list.add("Aura"); // ← ЗАМЕНЕНО killAuraEnabled на auraEnabled
        if (c.speedEnabled) list.add("Speed");
        if (c.bunnyHopEnabled) list.add("BunnyHop");
        if (c.targetStrafeEnabled) list.add("TargetStrafe");
        if (c.jumpCirclesEnabled) list.add("JumpCircles");
        if (c.customSkyEnabled) list.add("CustomSky");
        if (c.flyEnabled) list.add("Fly");
        if (c.sprintEnabled) list.add("Sprint");
        if (c.autoTotemEnabled) list.add("AutoTotem");
        if (c.espEnabled) list.add("Entity ESP");
        if (c.tagsEnabled) list.add("Tags");
        if (c.ambienceEnabled) list.add("Ambience");
        if (c.viewmodelEnabled) list.add("Viewmodel");
        return list;
    }

    private static boolean isEnabled(String function) {
        ModConfig c = BiwerVisualsClient.CONFIG;
        return switch (function) {
            case "aura" -> c.auraEnabled; // ← ЗАМЕНЕНО killaura на aura
            case "fly" -> c.flyEnabled;
            case "sprint" -> c.sprintEnabled;
            case "speed" -> c.speedEnabled;
            case "bunnyhop" -> c.bunnyHopEnabled;
            case "targetstrafe" -> c.targetStrafeEnabled;
            case "jumpcircles" -> c.jumpCirclesEnabled;
            case "customsky" -> c.customSkyEnabled;
            case "autototem" -> c.autoTotemEnabled;
            case "entityesp" -> c.espEnabled;
            case "tags" -> c.tagsEnabled;
            case "ambience" -> c.ambienceEnabled;
            case "viewmodel" -> c.viewmodelEnabled;
            default -> false;
        };
    }

    private static String keyName(int code) {
        if (code >= 32 && code <= 96) return String.valueOf((char) code).toUpperCase(Locale.ROOT);
        return switch (code) {
            case 32 -> "SPACE";
            case 340, 344 -> "SHIFT";
            case 341, 345 -> "CTRL";
            case 342, 346 -> "ALT";
            case 258 -> "TAB";
            case 256 -> "ESC";
            case 257 -> "ENTER";
            case 259 -> "BACKSPACE";
            default -> "KEY" + code;
        };
    }

    private static String roman(int value) {
        return switch (value) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> value > 5 ? String.valueOf(value) : "";
        };
    }

    private static String formatDuration(int ticks) {
        if (ticks < 0) return "**";
        int total = ticks / 20;
        return String.format("%d:%02d", total / 60, total % 60);
    }
}