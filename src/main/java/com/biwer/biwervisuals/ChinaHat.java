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
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class ChinaHat {
    private static final RenderPipeline FILLED_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("biwervisuals", "pipeline/china_hat"))
                    .build()
    );

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(1536);
    private static BufferBuilder buffer;
    private static MappableRingBuffer vertexBuffer;

    public static void init() {
        LevelRenderEvents.BEFORE_GIZMOS.register(ChinaHat::render);
    }

    private static void render(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!BiwerVisualsClient.CONFIG.chinaHatEnabled) return;

        // Не рисуем на себе в первом лице
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();
        Vec3 cam = context.levelState().cameraRenderState.pos;
        float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        for (Player player : mc.level.players()) {
            if (player == mc.player && firstPerson) continue;
            if (!shouldRender(player, mc.player)) continue;

            Vec3 headPos = getHeadPos(player, tickDelta);

            // УВЕЛИЧИЛИ РАДИУС: теперь шляпа шире головы, как китайская
            double radius = player.getBbWidth() * 0.9;

            PoseStack stack = new PoseStack();
            stack.translate(headPos.x - cam.x, headPos.y - cam.y, headPos.z - cam.z);

            if (buffer == null) {
                buffer = new BufferBuilder(ALLOCATOR, FILLED_PIPELINE.getVertexFormatMode(), FILLED_PIPELINE.getVertexFormat());
            }

            renderCone(stack, buffer, (float) radius, BiwerVisualsClient.CONFIG.chinaHatColor);

            MeshData built = buffer.buildOrThrow();
            MeshData.DrawState drawState = built.drawState();
            VertexFormat format = drawState.format();

            int vertexBufferSize = drawState.vertexCount() * format.getVertexSize();
            if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
                if (vertexBuffer != null) vertexBuffer.close();
                vertexBuffer = new MappableRingBuffer(
                        () -> "biwervisuals_china_hat",
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
                            () -> "biwervisuals_china_hat",
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
    }

    private static boolean shouldRender(Player target, Player self) {
        if (target == self) return BiwerVisualsClient.CONFIG.chinaHatSelf;
        if (BiwerVisualsClient.CONFIG.chinaHatFriends) return false; // заглушка
        return BiwerVisualsClient.CONFIG.chinaHatAll;
    }

    private static Vec3 getHeadPos(Player player, float tickDelta) {
        // Центр головы + чуть выше (чтобы шляпа сидела сверху, а не внутри)
        double x = Mth.lerp(tickDelta, player.xo, player.getX());
        double y = Mth.lerp(tickDelta, player.yo, player.getY()) + player.getEyeHeight() + 0.15;
        double z = Mth.lerp(tickDelta, player.zo, player.getZ());
        return new Vec3(x, y, z);
    }

    private static void renderCone(PoseStack stack, VertexConsumer consumer, float radius, int color) {
        Matrix4f mat = stack.last().pose();
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Верхушка выше, чтобы шляпа была острой
        Vec3 apex = new Vec3(0, 0.35, 0);
        int segments = 360;

        for (int i = 0; i < segments; i++) {
            double a1 = Math.PI * 2 * i / segments;
            double a2 = Math.PI * 2 * (i + 1) / segments;

            Vec3 p1 = new Vec3(Math.cos(a1) * radius, 0, Math.sin(a1) * radius);
            Vec3 p2 = new Vec3(Math.cos(a2) * radius, 0, Math.sin(a2) * radius);

            // Рисуем сам конус (основание и бока)
            consumer.addVertex(mat, (float) apex.x, (float) apex.y, (float) apex.z).setColor(r, g, b, a);
            consumer.addVertex(mat, (float) p1.x, (float) p1.y, (float) p1.z).setColor(r, g, b, a);
            consumer.addVertex(mat, (float) p2.x, (float) p2.y, (float) p2.z).setColor(r, g, b, a);
        }
    }
}