package com.biwer.biwervisuals;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class AuraHandler {
    private static final RenderPipeline FILLED_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("biwervisuals", "pipeline/aura_filled"))
                    .build()
    );

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(1536);
    private static BufferBuilder buffer;
    private static MappableRingBuffer vertexBuffer;

    private static final Vector3f[] CRYSTAL_VERTS = {
            new Vector3f(0, 1.5f, 0), new Vector3f(0, -1.5f, 0),
            new Vector3f(1, 0, 0), new Vector3f(-1, 0, 0),
            new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)
    };
    private static final int[][] FACES = {
            {0, 4, 2}, {0, 3, 4}, {0, 5, 3}, {0, 2, 5},
            {1, 2, 4}, {1, 4, 3}, {1, 3, 5}, {1, 5, 2}
    };
    private static final float[] FACE_BRIGHTNESS = {1f, 0.8f, 0.6f, 0.9f, 0.7f, 0.5f, 0.4f, 0.6f};

    private static LivingEntity target;
    private static long lastTick = 0;

    public static void init() {
        LevelRenderEvents.BEFORE_GIZMOS.register(AuraHandler::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LivingEntity newTarget = Aura.getTarget();
            if (newTarget != target) {
                target = newTarget;
                lastTick = System.currentTimeMillis();
            }
        });
    }

    private static void render(LevelRenderContext context) {
        if (target == null || !target.isAlive()) return;
        if (!BiwerVisualsClient.CONFIG.auraEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        float anim = Math.min(1f, (System.currentTimeMillis() - lastTick) / 200f);
        float moving = (System.currentTimeMillis() % 360000) / 2.5f + anim;

        Vec3 cam = context.levelState().cameraRenderState.pos;
        float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 pos = new Vec3(
                Mth.lerp(tickDelta, target.xo, target.getX()),
                Mth.lerp(tickDelta, target.yo, target.getY()),
                Mth.lerp(tickDelta, target.zo, target.getZ())
        );

        PoseStack stack = new PoseStack();
        stack.translate(-cam.x, -cam.y, -cam.z);

        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR, FILLED_PIPELINE.getVertexFormatMode(), FILLED_PIPELINE.getVertexFormat());
        }

        renderCrystals(stack, buffer, pos, target.getBbWidth() * 1.5f, 1.25f - 0.5f * anim, moving, 0xFFAB9FF2);

        MeshData built = buffer.buildOrThrow();
        MeshData.DrawState drawState = built.drawState();
        VertexFormat format = drawState.format();

        int vertexBufferSize = drawState.vertexCount() * format.getVertexSize();
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(
                    () -> "biwervisuals_aura",
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
                        () -> "biwervisuals_aura",
                        mc.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(),
                        mc.getMainRenderTarget().getDepthTextureView(),
                        OptionalDouble.empty()
                )) {
            pass.setPipeline(FILLED_PIPELINE);
            pass.setVertexBuffer(0, vertexBuffer.currentBuffer());
            pass.draw(0, drawState.vertexCount());
        }

        buffer = null;
        vertexBuffer.rotate();
    }

    private static void renderCrystals(PoseStack stack, VertexConsumer consumer, Vec3 pos, float ringWidth, float ringScale, float moving, int color) {
        Vec3 center = pos.add(0, target.getBbHeight() / 2f, 0);
        for (int i = 0; i < 360; i += 20) {
            float angle = (float) Math.toRadians(i + moving * 0.3f);
            float offX = (float) Math.sin(angle) * ringWidth * ringScale;
            float offZ = (float) Math.cos(angle) * ringWidth * ringScale;
            float offY = 0.1f + target.getBbHeight() * Math.abs((float) Math.sin(i));

            stack.pushPose();
            stack.translate(pos.x + offX, pos.y + offY, pos.z + offZ);
            stack.mulPose(new Quaternionf().rotationTo(
                    new Vector3f(0, 1, 0),
                    new Vector3f(
                            (float) (center.x - (pos.x + offX)),
                            (float) (center.y - (pos.y + offY)),
                            (float) (center.z - (pos.z + offZ))
                    ).normalize()
            ));
            stack.scale(0.1f, 0.1f, 0.1f);
            renderCrystalMesh(stack, consumer, color);
            stack.popPose();
        }
    }

    private static void renderCrystalMesh(PoseStack stack, VertexConsumer consumer, int color) {
        Matrix4f mat = stack.last().pose();
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        for (int i = 0; i < FACES.length; i++) {
            int[] face = FACES[i];
            float br = FACE_BRIGHTNESS[i];
            int shaded = (a << 24) | (Math.min(255, (int)(r * br)) << 16) | (Math.min(255, (int)(g * br)) << 8) | Math.min(255, (int)(b * br));
            for (int v = 0; v < 3; v++) {
                Vector3f vert = CRYSTAL_VERTS[face[v]];
                consumer.addVertex(mat, vert.x, vert.y, vert.z).setColor(shaded);
            }
        }
    }
}