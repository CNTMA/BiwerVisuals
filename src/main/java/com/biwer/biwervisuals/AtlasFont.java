package com.biwer.biwervisuals;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

final class AtlasFont {
    static final AtlasFont ICONS = new AtlasFont("icons");
    static final AtlasFont REGULAR = new AtlasFont("sf_regular");
    static final AtlasFont MEDIUM = new AtlasFont("sf_medium");
    static final AtlasFont ONEST = new AtlasFont("onest_regular");
    private final Map<Integer, JsonObject> glyphs = new HashMap<>();
    private final Identifier texture;
    private final int width, height;
    private final float ascender;

    private AtlasFont(String name) {
        texture = Identifier.fromNamespaceAndPath("biwervisuals", "textures/type/" + name + ".png");
        try (var stream = AtlasFont.class.getResourceAsStream("/assets/biwervisuals/textures/type/" + name + ".json");
             var reader = new InputStreamReader(Objects.requireNonNull(stream), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            width = root.getAsJsonObject("atlas").get("width").getAsInt();
            height = root.getAsJsonObject("atlas").get("height").getAsInt();
            ascender = root.getAsJsonObject("metrics").get("ascender").getAsFloat();
            for (var entry : root.getAsJsonArray("glyphs")) {
                var glyph = entry.getAsJsonObject();
                glyphs.put(glyph.get("unicode").getAsInt(), glyph);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private JsonObject glyph(int point) {
        return glyphs.getOrDefault(point, glyphs.get(63));
    }

    float width(String text, float size) {
        float result = 0;
        for (int point : text.codePoints().toArray()) {
            var glyph = glyph(point);
            if (glyph != null) result += glyph.get("advance").getAsFloat() * size;
        }
        return result;
    }

    String fit(String text, float size, int limit) {
        StringBuilder result = new StringBuilder();
        float used = 0;
        for (int point : text.codePoints().toArray()) {
            var glyph = glyph(point);
            float advance = glyph == null ? 0 : glyph.get("advance").getAsFloat() * size;
            if (used + advance > limit) break;
            result.appendCodePoint(point);
            used += advance;
        }
        return result.toString();
    }

    void draw(GuiGraphicsExtractor g, String text, float x, float y, float size, int color) {
        for (int point : text.codePoints().toArray()) {
            var glyph = glyph(point);
            if (glyph == null) continue;
            if (glyph.has("atlasBounds")) {
                var a = glyph.getAsJsonObject("atlasBounds");
                var b = glyph.getAsJsonObject("planeBounds");
                float left = b.get("left").getAsFloat(), top = b.get("top").getAsFloat();
                float w = (b.get("right").getAsFloat() - left) * size;
                float h = (top - b.get("bottom").getAsFloat()) * size;
                int sourceW = Math.round(a.get("right").getAsFloat() - a.get("left").getAsFloat());
                int sourceH = Math.round(a.get("top").getAsFloat() - a.get("bottom").getAsFloat());
                g.pose().pushMatrix();
                g.pose().translate(x + left * size, y + (ascender - top) * size);
                g.pose().scale(w / sourceW, h / sourceH);
                g.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0,
                        a.get("left").getAsFloat(), height - a.get("top").getAsFloat(),
                        sourceW, sourceH, sourceW, sourceH, width, height, color);
                g.pose().popMatrix();
            }
            x += glyph.get("advance").getAsFloat() * size;
        }
    }
}
