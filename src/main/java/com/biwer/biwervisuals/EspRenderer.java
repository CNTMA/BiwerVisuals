package com.biwer.biwervisuals;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class EspRenderer {
    // Используем DEBUG_FILLED_SNIPPET для заполненных боксов (официальный путь для 26.1.2)
    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("biwervisuals", "pipeline/filled_through_walls"))
                    .withDepthStencilState(Optional.empty()) // Отключаем depth-тест
                    .build()
    );

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static BufferBuilder buffer;
    private static MappableRingBuffer vertexBuffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    public static void init() {
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(EspRenderer::renderAndDraw);
    }

    private static void renderAndDraw(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (!BiwerVisualsClient.CONFIG.espEnabled) return;

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
        }

        for (var entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof net.minecraft.world.entity.LivingEntity living) || entity == client.player) continue;

            boolean isPlayer = living instanceof net.minecraft.world.entity.player.Player;
            boolean isMob = living instanceof net.minecraft.world.entity.Mob;
            boolean isAnimal = living instanceof net.minecraft.world.entity.animal.Animal || living instanceof net.minecraft.world.entity.ambient.AmbientCreature;

            if (isPlayer && !BiwerVisualsClient.CONFIG.espPlayers) continue;
            if (isMob && !BiwerVisualsClient.CONFIG.espMobs) continue;
            if (isAnimal && !BiwerVisualsClient.CONFIG.espAnimals) continue;

            AABB box = living.getBoundingBox().inflate(0.1);
            drawFilledBox(matrices.last().pose(), buffer, box, 1.0f, 0.0f, 1.0f, 0.3f); // Розовый, полупрозрачный
        }

        matrices.popPose();

        drawThroughWalls(client);
    }

    // Рисуем заполненный куб (6 граней)
    private static void drawFilledBox(Matrix4fc pose, BufferBuilder buffer, AABB box, float r, float g, float b, float a) {
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        // Нижняя грань
        addQuad(buffer, pose, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        // Верхняя грань
        addQuad(buffer, pose, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, r, g, b, a);
        // Передняя грань
        addQuad(buffer, pose, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a);
        // Задняя грань
        addQuad(buffer, pose, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        // Левая грань
        addQuad(buffer, pose, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
        // Правая грань
        addQuad(buffer, pose, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a);
    }

    private static void addQuad(BufferBuilder buffer, Matrix4fc pose,
                                float x1, float y1, float z1, float x2, float y2, float z2,
                                float x3, float y3, float z3, float x4, float y4, float z4,
                                float r, float g, float b, float a) {
        buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a);
        buffer.addVertex(pose, x4, y4, z4).setColor(r, g, b, a);
    }

    private static void drawThroughWalls(Minecraft client) {
        if (buffer == null) return;

        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        int vertexSize = format.getVertexSize();
        int vertexBufferSize = drawParameters.vertexCount() * vertexSize;

        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(() -> "biwervisuals esp", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        // ИСПРАВЛЕНО: используем mapBuffer с правильными флагами для записи
        var commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (var mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        GpuBuffer vertices = vertexBuffer.currentBuffer();

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        var mainTarget = client.getMainRenderTarget();
        var colorTexture = mainTarget.getColorTextureView();

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "biwervisuals esp", colorTexture, OptionalInt.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(FILLED_THROUGH_WALLS);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.draw(drawParameters.indexCount(), 0);
        }

        vertexBuffer.rotate();
        buffer = null;
    }
}