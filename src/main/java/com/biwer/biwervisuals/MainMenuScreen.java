package com.biwer.biwervisuals;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class MainMenuScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
            "biwervisuals", "textures/menu/background.png");
    private static final Style INTER = Style.EMPTY.withFont(new FontDescription.Resource(
            Identifier.fromNamespaceAndPath("biwervisuals", "title")));
    private final long opened = System.nanoTime();
    private boolean accountOpen;
    private int unit;

    public MainMenuScreen() {
        super(Component.literal("BiwerVisuals"));
    }

    @Override
    protected void init() {
        unit = Math.max(14, Math.round(height * 0.068f));
        int x = Math.max(8, Math.round(width * 0.03125f));
        int y = Math.round(height * 0.466f);
        int menuWidth = Math.min(width - x - 6, Math.max(153, Math.round(width * 0.236f)));
        int gap = Math.max(3, Math.round(unit * 0.18f));
        addRenderableWidget(new MenuButton(x, y, menuWidth, "Singleplayer", "globe", false,
                () -> minecraft.setScreen(new SelectWorldScreen(this))));
        MenuButton multiplayer = new MenuButton(x, y + unit + gap, menuWidth, "Multiplayer", "users", false,
                () -> minecraft.setScreen(minecraft.options.skipMultiplayerWarning
                        ? new JoinMultiplayerScreen(this) : new SafetyScreen(this)));
        multiplayer.active = minecraft.allowsMultiplayer();
        addRenderableWidget(multiplayer);
        addRenderableWidget(new MenuButton(x, y + 2 * (unit + gap), menuWidth, "Settings", "settings", false,
                () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options, false))));
        int quitWidth = (menuWidth - gap / 2) / 2;
        addRenderableWidget(new MenuButton(x, y + 3 * (unit + gap), quitWidth, "Quit", "quit", true, minecraft::stop));
        addRenderableWidget(new MenuButton(x + quitWidth + gap / 2, y + 3 * (unit + gap),
                menuWidth - quitWidth - gap / 2, "Account Manager", "", false, () -> accountOpen = !accountOpen));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        float ratio = (float) width / Math.max(1, height);
        float imageRatio = 1654f / 951f;
        float u = Math.min(1f, ratio / imageRatio);
        float v = Math.min(1f, imageRatio / ratio);
        g.blit(BACKGROUND, 0, 0, width, height, (1 - u) / 2, (1 + u) / 2, (1 - v) / 2, (1 + v) / 2);
        g.fillGradient(0, 0, width, height, 0x08051929, 0x4607101B);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        float entrance = 1f - (float) Math.exp(-(System.nanoTime() - opened) / 180_000_000.0);
        g.pose().pushMatrix();
        g.pose().translate(0, (1 - entrance) * 18);
        int headingX = Math.round(width * 0.025f);
        int headingY = Math.round(height * 0.366f);
        float scale = Math.max(1.45f, height / 175f);
        Component intro = label("Welcome to ");
        Component brand = Component.literal("BiwerVisuals").withStyle(INTER.withItalic(true));
        draw(g, intro, headingX, headingY, scale, 0xFFFFFFFF);
        draw(g, brand, headingX + Math.round(textWidth(intro) * scale), headingY, scale, 0xFFFFF3E5);
        draw(g, label("Long time no see."), Math.round(width * 0.03125f),
                headingY + Math.round(10 * scale), Math.max(0.8f, height / 530f), 0xFFD4D9DF);

        String name = minecraft.getUser().getName();
        int margin = Math.max(5, Math.round(height * 0.019f));
        int cardHeight = Math.max(27, Math.round(height * 0.089f));
        int cardWidth = Math.max(86, Math.round(width * 0.114f));
        int cardX = width - margin - cardWidth;
        pill(g, cardX, margin, cardWidth, cardHeight, 0xAA304F75);
        int avatar = cardHeight * 2 / 3;
        pill(g, cardX + 4, margin + (cardHeight - avatar) / 2, avatar, avatar, 0xD5405A77);
        icon(g, "logo", cardX + 4 + avatar / 4, margin + (cardHeight - avatar) / 2 + avatar / 4, avatar / 2);
        draw(g, label(font.substrByWidth(label(name), (cardWidth - avatar - 12) * 3).getString()),
                cardX + avatar + 8, margin + cardHeight / 2 - 8, 1f, 0xFFFFFFFF);
        draw(g, label("Minecraft"), cardX + avatar + 8, margin + cardHeight / 2 + 2, 0.8f, 0xFFCBD7E5);
        if (accountOpen) {
            int detailWidth = Math.max(150, cardWidth);
            int detailX = width - margin - detailWidth;
            pill(g, detailX, margin + cardHeight + 4, detailWidth, 34, 0xE524344A);
            draw(g, label("Signed in as " + name), detailX + 8, margin + cardHeight + 11, 1f, 0xFFFFFFFF);
            draw(g, label("Switch accounts in the launcher"), detailX + 8,
                    margin + cardHeight + 23, 0.85f, 0xFFCBD7E5);
        }
        Component footer = label("© 2026 BiwerVisuals");
        draw(g, footer, (width - textWidth(footer)) / 2, height - 10, 1f, 0xFFCCD4DE);
        g.pose().popMatrix();
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private static Component label(String text) {
        return Component.literal(text).withStyle(INTER);
    }

    private void draw(GuiGraphicsExtractor g, Component text, int x, int y, float scale, int color) {
        AtlasFont.REGULAR.draw(g, text.getString(), x, y, 10 * scale, color);
    }

    private int textWidth(Component text) {
        return Math.round(AtlasFont.REGULAR.width(text.getString(), 10));
    }
    private static void icon(GuiGraphicsExtractor g, String name, int x, int y, int size) {
        String glyph = switch (name) {
            case "settings" -> "%";
            case "globe" -> "y";
            case "quit" -> "N";
            default -> null;
        };
        if (glyph != null) {
            AtlasFont.ICONS.draw(g, glyph, x, y, size, 0xFFFFFFFF);
            return;
        }
        Identifier texture = Identifier.fromNamespaceAndPath("biwervisuals", "textures/menu/" + name + ".png");
        g.blit(texture, x, y, x + size, y + size, 0f, 1f, 0f, 1f);
    }

    private static void pill(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        float r = Math.min(w, h) * 0.43f;
        for (int row = 0; row < h; row++) {
            double dy = Math.max(0, r - Math.min(row + 0.5, h - row - 0.5));
            double edge = r - Math.sqrt(r * r - dy * dy);
            int inset = (int) Math.ceil(edge);
            if (inset > 0) {
                int alpha = Math.round((color >>> 24) * (float) (inset - edge));
                int rim = (alpha << 24) | (color & 0xFFFFFF);
                g.fill(x + inset - 1, y + row, x + inset, y + row + 1, rim);
                g.fill(x + w - inset, y + row, x + w - inset + 1, y + row + 1, rim);
            }
            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
        }
    }

    private final class MenuButton extends AbstractWidget {
        private final Runnable action;
        private final String symbol;
        private final boolean quit;
        private float hover;
        private long lastFrame = System.nanoTime();
        private long pressed;

        MenuButton(int x, int y, int width, String text, String symbol, boolean quit, Runnable action) {
            super(x, y, width, unit, label(text));
            this.action = action;
            this.symbol = symbol;
            this.quit = quit;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            boolean highlighted = isHoveredOrFocused() && active;
            long now = System.nanoTime();
            float seconds = Math.min(0.05f, (now - lastFrame) / 1_000_000_000f);
            lastFrame = now;
            hover += ((highlighted ? 1f : 0f) - hover) * (1 - (float) Math.exp(-seconds * 18));
            int color = quit ? (highlighted ? 0xFFE498AD : 0xEBCA7F97)
                    : (highlighted ? 0xF043504C : 0xEB1B2525);
            float reveal = 1f - (float) Math.exp(-Math.max(0, (System.nanoTime() - opened) / 1_000_000_000.0
                    - (getY() / (double) Math.max(1, MainMenuScreen.this.height)) * 0.14) * 9);
            g.pose().pushMatrix();
            g.pose().translate((1 - reveal) * -14, 0);
            pill(g, getX(), getY(), getWidth(), getHeight(), color);
            int highlightAlpha = Math.round(hover * 24);
            pill(g, getX(), getY(), getWidth(), getHeight(), (highlightAlpha << 24) | 0xD9C1FF);
            float pulse = (now - pressed) / 300_000_000f;
            if (pulse < 1) pill(g, getX(), getY(), getWidth(), getHeight(),
                    (Math.round((1 - pulse) * 50) << 24) | 0xFFFFFF);
            float scale = Math.min(Math.max(0.85f, getHeight() / 28f),
                    (getWidth() - 14f - (symbol.isEmpty() ? 0 : 11)) / textWidth(getMessage()));
            int textY = getY() + (getHeight() - Math.round(8 * scale)) / 2;
            int padding = Math.max(6, Math.round(getHeight() * 0.44f));
            if (symbol.isEmpty()) padding = (getWidth() - Math.round(textWidth(getMessage()) * scale)) / 2;
            draw(g, getMessage(), getX() + padding, textY, scale, active ? 0xFFFFFFFF : 0xFFA3ACAC);
            if (!symbol.isEmpty()) {
                int size = Math.max(9, Math.round(getHeight() * 0.33f));
                icon(g, symbol, getX() + getWidth() - padding - size,
                        getY() + (getHeight() - size) / 2, size);
            }
            g.pose().popMatrix();
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubled) {
            pressed = System.nanoTime();
            action.run();
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (active && (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_SPACE
                    || event.key() == GLFW.GLFW_KEY_KP_ENTER)) {
                pressed = System.nanoTime();
            action.run();
                return true;
            }
            return super.keyPressed(event);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
