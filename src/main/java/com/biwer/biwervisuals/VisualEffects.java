package com.biwer.biwervisuals;

import java.util.ArrayDeque;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;


import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class VisualEffects {
    enum Effect {
        SkyChanger, AuroraSky, GalaxySky, WeatherChanger, Vignette,
        JumpParticles, LandingCircle, MotionTrail, HitParticles, DamageIndicator, HitMarker,
        TargetESP, PlayerESP, CrystalESP, ItemESP, AmbientParticles, Fireflies, SnowParticles
    }

    private record Mote(Effect effect, Vec3 start, Vec3 velocity, int born, int life) {}
    private static final ArrayDeque<Mote> motes = new ArrayDeque<>();
    private static final int[] PRIMARY = {0x58F0D4, 0xF66CFF, 0xFF9469, 0x80DFFF};
    private static final int[] SECONDARY = {0x776AFF, 0x795EFF, 0xE94D99, 0xEDF8FF};

    public static float strength(String name) {
        float value = BiwerVisualsClient.CONFIG.visualStrength.getOrDefault(name, 0.65f);
        return Math.max(0f, Math.min(1f, value));
    }

    public static int tint(String name, double phase) {
        int index = Math.clamp(BiwerVisualsClient.CONFIG.visualPalette.getOrDefault(name, 0), 0, PRIMARY.length - 1);
        return blend(PRIMARY[index], SECONDARY[index], (float) (0.5 + 0.5 * Math.sin(phase)));
    }

    public static int blend(int start, int end, float amount) {
        int color = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            int a = (start >> shift) & 255;
            int b = (end >> shift) & 255;
            color |= Math.round(a + (b - a) * amount) << shift;
        }
        return color;
    }

    private static int glow(Effect effect, float alpha, double phase) {
        return (Math.round(Math.clamp(alpha * strength(effect.name()), 0f, 1f) * 255) << 24)
                | tint(effect.name(), phase);
    }

    private record Sample(Vec3 position, int tick) {}

    private static final ArrayDeque<Sample> trail = new ArrayDeque<>();
    private static final ArrayDeque<Sample> landings = new ArrayDeque<>();
    private static ClientLevel world;
    private static boolean grounded;
    private static int tick;
    private static int hitTick = -100;
    private static LivingEntity pendingHit;
    private static float previousHealth;
    private static float damage;
    private static Vec3 damagePosition;
    private static int damageTick = -100;

    public static boolean enabled(String name) {
        return BiwerVisualsClient.CONFIG.visualEffects.contains(name);
    }

    private static boolean enabled(Effect effect) {
        return enabled(effect.name());
    }

    static void toggle(String name, boolean state) {
        if (state) BiwerVisualsClient.CONFIG.visualEffects.add(name);
        else BiwerVisualsClient.CONFIG.visualEffects.remove(name);
    }

    static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(VisualEffects::tick);
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> renderWorld(Minecraft.getInstance()));
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level == world && player == Minecraft.getInstance().player && !player.isSpectator()
                    && entity instanceof LivingEntity target
                    && (enabled(Effect.HitParticles) || enabled(Effect.HitMarker) || enabled(Effect.DamageIndicator))) {
                pendingHit = target;
                previousHealth = target.getHealth() + target.getAbsorptionAmount();
                hitTick = tick;
                if (enabled(Effect.HitParticles)) burst(Effect.HitParticles, target.position().add(0, target.getBbHeight() * 0.65, 0), 16);
            }
            return InteractionResult.PASS;
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("biwervisuals", "visual_effects"), (graphics, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.options.hideGui || client.screen != null) return;
            renderTarget(graphics, client);
            int w = graphics.guiWidth(), h = graphics.guiHeight();
            if (enabled(Effect.Vignette)) {
                int base = blend(0, tint("Vignette", tick * 0.04), 0.23f);
                int shade = (Math.round(180 * strength("Vignette")) << 24) | base;
                int edge = Math.min(w, h) / 4;
                graphics.fillGradient(0, 0, w, edge, shade, base);
                graphics.fillGradient(0, h - edge, w, h, base, shade);
                for (int i = 0; i < edge; i++) {
                    float weight = 1f - (float) i / edge;
                    int color = (Math.round(150 * strength("Vignette") * weight * weight) << 24) | base;
                    graphics.fill(i, 0, i + 1, h, color);
                    graphics.fill(w - i - 1, 0, w - i, h, color);
                }
            }
            int age = tick - hitTick;
            if (enabled(Effect.HitMarker) && age < 8) {
                int color = glow(Effect.HitMarker, 1f - age / 9f, age * 0.4);
                for (int dx : new int[]{-1, 1}) {
                    for (int dy : new int[]{-1, 1}) {
                        for (int i = 4; i < 8; i++) {
                            int x = w / 2 + dx * i, y = h / 2 + dy * i;
                            graphics.fill(x, y, x + 1, y + 1, color);
                        }
                    }
                }
            }
        });
    }

    private static void tick(Minecraft client) {
        if (world != client.level || client.player == null) {
            world = client.level;
            trail.clear();
            motes.clear();
            landings.clear();
            pendingHit = null;
            hitTick = damageTick = -100;
            grounded = client.player != null && client.player.onGround();
        }
        if (world == null || client.player == null || client.isPaused()) return;
        tick++;
        var player = client.player;
        Vec3 feet = player.position();
        boolean onGround = player.onGround();
        if (grounded && !onGround && player.getDeltaMovement().y > 0 && enabled(Effect.JumpParticles)) {
            burst(Effect.JumpParticles, feet, 12);
        }
        if (!grounded && onGround && enabled(Effect.LandingCircle)) landings.addLast(new Sample(feet, tick));
        grounded = onGround;
        while (!landings.isEmpty() && tick - landings.getFirst().tick() >= 14) landings.removeFirst();
        if (enabled(Effect.MotionTrail)) {
            if (!trail.isEmpty() && trail.getLast().position().distanceToSqr(feet) > 64) trail.clear();
            if (trail.isEmpty() || trail.getLast().position().distanceToSqr(feet) > 0.02) trail.addLast(new Sample(feet, tick));
            while (!trail.isEmpty() && tick - trail.getFirst().tick() >= 24) trail.removeFirst();
        } else trail.clear();

        if (pendingHit != null) {
            float health = pendingHit.getHealth() + pendingHit.getAbsorptionAmount();
            if (tick - hitTick > 12) pendingHit = null;
            else if (health < previousHealth) {
                damage = previousHealth - health;
                damagePosition = pendingHit.position().add(0, pendingHit.getBbHeight(), 0);
                damageTick = tick;
                pendingHit = null;
            }
        }
        motes.removeIf(mote -> tick - mote.born() >= mote.life() || !enabled(mote.effect()));
        if (tick % 4 == 0 && enabled(Effect.AmbientParticles)) ambient(Effect.AmbientParticles, feet, 1, 0.015);
        if (tick % 6 == 0 && enabled(Effect.Fireflies)) ambient(Effect.Fireflies, feet, 0.4, 0.01);
        if (enabled(Effect.SnowParticles)) ambient(Effect.SnowParticles, feet, 4, -0.09);
    }

    private static void burst(Effect effect, Vec3 center, int count) {
        count = Math.round(count * strength(effect.name()) * 1.5f);
        for (int i = 0; i < count; i++) {
            double angle = i * Math.TAU / count;
            addMote(new Mote(effect, center.add(0, 0.08, 0),
                    new Vec3(Math.cos(angle) * 0.12, 0.08, Math.sin(angle) * 0.12), tick, 15));
        }
    }

    private static void ambient(Effect effect, Vec3 center, double height, double speed) {
        var random = ThreadLocalRandom.current();
        if (random.nextFloat() >= strength(effect.name())) return;
        Vec3 start = center.add(random.nextDouble(-7, 7), height + random.nextDouble(3), random.nextDouble(-7, 7));
        addMote(new Mote(effect, start, new Vec3(0, speed, 0), tick, 42));
    }

    private static void addMote(Mote mote) {
        if (motes.size() >= 180) motes.removeFirst();
        motes.addLast(mote);
    }

    private static void renderMotes() {
        for (Mote mote : motes) {
            if (!enabled(mote.effect())) continue;
            int age = tick - mote.born();
            float progress = age / (float) mote.life();
            float alpha = Math.min(1f, (age + 1) / 4f) * (1 - progress);
            double phase = mote.born() * 0.43 + age * 0.16;
            Vec3 position = mote.start().add(mote.velocity().scale(age))
                    .add(Math.sin(phase) * 0.08, 0, Math.cos(phase) * 0.08);
            Vec3 end = position.add(mote.velocity().scale(1.2)).add(0, 0.06, 0);
            Gizmos.line(position, end, glow(mote.effect(), alpha * 0.30f, phase), 5f);
            Gizmos.line(position, end, glow(mote.effect(), alpha, phase), 1.6f);
        }
    }

    private static final String[] TARGET_TEXTURES = {"targetesp_2", "targetesp_3", "cube", "capture", "diamond", "delta_marker"};

    private static void renderTarget(net.minecraft.client.gui.GuiGraphicsExtractor graphics, Minecraft client) {
        if (!enabled(Effect.TargetESP)) return;
        LivingEntity target = Aura.getTarget();
        if (target == null && client.crosshairPickEntity instanceof LivingEntity pointed) target = pointed;
        if (target == null || !target.isAlive() || target.level() != client.level
                || target == client.player || !client.player.hasLineOfSight(target)) return;
        Vec3 center = target.position().add(0, target.getBbHeight() * 0.55, 0);
        Vec3 screen = client.gameRenderer.projectPointToScreen(center);
        if (!Double.isFinite(screen.x) || !Double.isFinite(screen.y) || screen.z < -1 || screen.z > 1
                || Math.abs(screen.x) > 1.2 || Math.abs(screen.y) > 1.2) return;
        var config = BiwerVisualsClient.CONFIG;
        double phase = (System.nanoTime() / 1_000_000_000.0) % 360;
        int color = config.targetAnimatedColor ? tint("TargetESP", phase * 1.6) : config.targetColor & 0xFFFFFF;
        color |= Math.round(strength("TargetESP") * 255) << 24;
        int size = Math.round((24 + Math.clamp(config.targetSize, 0f, 1f) * 60)
                * (1f + 0.035f * (float) Math.sin(phase * 3)));
        int index = Math.clamp(config.targetTexture, 0, TARGET_TEXTURES.length - 1);
        var texture = Identifier.fromNamespaceAndPath("biwervisuals", "textures/target/" + TARGET_TEXTURES[index] + ".png");
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) ((screen.x + 1) * graphics.guiWidth() / 2),
                (float) ((1 - screen.y) * graphics.guiHeight() / 2));
        graphics.pose().rotate((float) Math.sin(phase) * 0.12f);
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture,
                -size / 2, -size / 2, 0f, 0f, size, size, 1, 1, 1, 1, color);
        graphics.pose().popMatrix();
    }
    private static void renderWorld(Minecraft client) {
        if (client.player == null || client.level != world) return;
        renderMotes();
        if (enabled(Effect.LandingCircle)) {
            for (Sample landing : landings) {
                float age = (tick - landing.tick()) / 14f;
                int color = glow(Effect.LandingCircle, 1 - age, age * 4);
                Gizmos.circle(landing.position().add(0, 0.02, 0), 0.24f + age * 1.2f,
                        GizmoStyle.stroke(glow(Effect.LandingCircle, (1 - age) * 0.55f, age * 4 + 2)));
                Gizmos.circle(landing.position().add(0, 0.03, 0), 0.3f + age * 1.5f, GizmoStyle.stroke(color));
            }
        }
        if (enabled(Effect.MotionTrail) && !client.options.getCameraType().isFirstPerson()) {
            Sample previous = null;
            for (Sample sample : trail) {
                if (previous != null) {
                    float alpha = Math.max(0, 1 - (tick - sample.tick()) / 24f);
                    Gizmos.line(previous.position().add(0, 0.08, 0), sample.position().add(0, 0.08, 0),
                            glow(Effect.MotionTrail, alpha * 0.3f, sample.tick() * 0.24), 5f);
                    Gizmos.line(previous.position().add(0, 0.08, 0), sample.position().add(0, 0.08, 0),
                            glow(Effect.MotionTrail, alpha, sample.tick() * 0.24), 2f);
                }
                previous = sample;
            }
        }
        if (enabled(Effect.DamageIndicator) && tick - damageTick < 20) {
            Gizmos.billboardText(String.format(java.util.Locale.ROOT, "-%.1f", damage),
                    damagePosition.add(0, 0.25 + (tick - damageTick) * 0.045, 0),
                    TextGizmo.Style.forColorAndCentered(0xFF000000 | tint("DamageIndicator", (tick - damageTick) * 0.17))
                            .withScale(0.38f + 0.45f * strength("DamageIndicator")));
        }

        boolean players = enabled(Effect.PlayerESP);
        boolean crystals = enabled(Effect.CrystalESP);
        boolean items = enabled(Effect.ItemESP);
        if (!players && !crystals && !items) return;
        for (var entity : world.entitiesForRendering()) {
            if (entity == client.player || entity.distanceToSqr(client.player) > 64 * 64) continue;
            int color;
            if (players && entity instanceof Player) color = glow(Effect.PlayerESP, 1, tick * 0.08);
            else if (crystals && entity instanceof EndCrystal) color = glow(Effect.CrystalESP, 1, tick * 0.08);
            else if (items && entity instanceof ItemEntity) color = glow(Effect.ItemESP, 1, tick * 0.08);
            else continue;
            Gizmos.cuboid(entity.getBoundingBox().inflate(0.025), GizmoStyle.stroke(color));
        }
    }
}
