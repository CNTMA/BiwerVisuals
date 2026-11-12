package com.biwer.biwervisuals;

import com.google.gson.Gson;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class CustomFont {
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(1536);
    private static BufferBuilder buffer;
    private static MappableRingBuffer vertexBuffer;

    // Регистрируем пайплайн с нашими шейдерами (text.vsh и text.fsh)
    private static final RenderPipeline TEXT_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("biwervisuals", "pipeline/msdf_text"))
                    .withVertexShader("core/text")
                    .withFragmentShader("core/text")
                    .withUniform("uRange", RenderPipeline.UniformType.FLOAT)
                    .withUniform("uThickness", RenderPipeline.UniformType.FLOAT)
                    .withUniform("uSmoothness", RenderPipeline.UniformType.FLOAT)
                    .withUniform("uOutline", RenderPipeline.UniformType.INT)
                    .withUniform("uOutlineThickness", RenderPipeline.UniformType.FLOAT)
                    .withUniform("uOutlineColor", RenderPipeline.UniformType.VEC4)
                    .withUniform("uFadeEnabled", RenderPipeline.UniformType.INT)
                    .withUniform("uFadeStart", RenderPipeline.UniformType.FLOAT)
                    .withUniform("uFadeEnd", RenderPipeline.UniformType.FLOAT)
                    .build()
    );

    private final FontData data;
    private final Identifier textureId;
    private final Map<Integer, FontData.GlyphData> glyphs = new HashMap<>();
    private final Map<Long, Float> kernings = new HashMap<>();

    public CustomFont(Identifier jsonId, Identifier textureId) {
        this.textureId = textureId;
        try (var stream = CustomFont.class.getResourceAsStream(
                "/assets/" + jsonId.getNamespace() + "/" + jsonId.getPath())) {
            if (stream == null) throw new IllegalStateException("JSON not found: " + jsonId);
            this.data = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), FontData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load font " + jsonId, e);
        }

        for (FontData.GlyphData g : data.glyphs()) {
            glyphs.put(g.unicode(), g);
        }
        if (data.kernings() != null) {
            for (FontData.KerningData k : data.kernings()) {
                kernings.put(((long) k.leftChar() << 32) | (k.rightChar() & 0xFFFFFFFFL), k.advance());
            }
        }
    }

    public void drawText(GuiGraphicsExtractor graphics, String text, float x, float y, float scale, int color) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || text == null || text.isEmpty()) return;

        PoseStack pose = graphics.pose();

        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR, TEXT_PIPELINE.getVertexFormatMode(), TEXT_PIPELINE.getVertexFormat());
        }

        float cursorX = x;
        float cursorY = y;
        float atlasW = data.atlas().width();
        float atlasH = data.atlas().height();
        int prevUnicode = -1;

        for (int i = 0; i < text.length(); i++) {
            int code = text.charAt(i);
            if (code == '\n') {
                cursorX = x;
                cursorY += data.metrics().lineHeight() * scale;
                prevUnicode = -1;
                continue;
            }

            FontData.GlyphData glyph = glyphs.get(code);
            if (glyph == null) continue;

            if (prevUnicode != -1) {
                Float kern = kernings.get(((long) prevUnicode << 32) | (code & 0xFFFFFFFFL));
                if (kern != null) cursorX += kern * scale;
            }

            FontData.BoundsData plane = glyph.planeBounds();
            FontData.BoundsData atlas = glyph.atlasBounds();

            if (plane != null && atlas != null) {
                float gx = cursorX + plane.left() * scale;
                float gy = cursorY - plane.top() * scale;
                float gw = (plane.right() - plane.left()) * scale;
                float gh = (plane.top() - plane.bottom()) * scale;

                float u = atlas.left() / atlasW;
                float v = (atlasH - atlas.top()) / atlasH;
                float uw = (atlas.right() - atlas.left()) / atlasW;
                float uh = (atlas.top() - atlas.bottom()) / atlasH;

                int a = (color >> 24) & 0xFF;
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;

                buffer.addVertex(pose.last().pose(), gx, gy, 0).setColor(r, g, b, a).setUv(u, v);
                buffer.addVertex(pose.last().pose(), gx, gy + gh, 0).setColor(r, g, b, a).setUv(u, v + uh);
                buffer.addVertex(pose.last().pose(), gx + gw, gy + gh, 0).setColor(r, g, b, a).setUv(u + uw, v + uh);

                buffer.addVertex(pose.last().pose(), gx, gy, 0).setColor(r, g, b, a).setUv(u, v);
                buffer.addVertex(pose.last().pose(), gx + gw, gy + gh, 0).setColor(r, g, b, a).setUv(u + uw, v + uh);
                buffer.addVertex(pose.last().pose(), gx + gw, gy, 0).setColor(r, g, b, a).setUv(u + uw, v);
            }

            cursorX += glyph.advance() * scale;
            prevUnicode = code;
        }

        MeshData built = buffer.buildOrThrow();
        MeshData.DrawState drawState = built.drawState();
        VertexFormat format = drawState.format();

        int vertexBufferSize = drawState.vertexCount() * format.getVertexSize();
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(
                    () -> "biwervisuals_text",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                    vertexBufferSize
            );
        }

        try (var mappedView = RenderSystem.getDevice().createCommandEncoder()
                .mapBuffer(vertexBuffer.currentBuffer().slice(0, built.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(built.vertexBuffer(), mappedView.data());
        }

        try (var pass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(
                        () -> "biwervisuals_text",
                        mc.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(),
                        mc.getMainRenderTarget().getDepthTextureView(),
                        OptionalDouble.empty()
                )) {
            pass.setPipeline(TEXT_PIPELINE);
            pass.setVertexBuffer(0, vertexBuffer.currentBuffer());

            var texture = mc.getTextureManager().getTexture(textureId);
            pass.bindTexture("Sampler0", texture);

            pass.setUniform("uRange", data.atlas().range());
            pass.setUniform("uThickness", 0.0f);
            pass.setUniform("uSmoothness", 0.5f);
            pass.setUniform("uOutline", 0);
            pass.setUniform("uOutlineThickness", 0.0f);
            pass.setUniform("uOutlineColor", 0f, 0f, 0f, 0f);
            pass.setUniform("uFadeEnabled", 0);
            pass.setUniform("uFadeStart", 0f);
            pass.setUniform("uFadeEnd", 0f);

            pass.draw(0, drawState.vertexCount());
        }

        buffer = null;
        vertexBuffer.rotate();
    }
}