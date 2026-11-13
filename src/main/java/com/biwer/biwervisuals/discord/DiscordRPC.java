package com.biwer.biwervisuals.discord;

import com.biwer.biwervisuals.discord.Activity;
import com.biwer.biwervisuals.discord.ActivityType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordRPC {
    private static final long APP_ID = 1570466372218261606L;
    private static final Gson GSON = new Gson();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Discord-RPC");
        t.setDaemon(true);
        return t;
    });

    private static Connection connection;
    private static volatile boolean connected = false;
    private static long startTime = System.currentTimeMillis() / 1000;

    public static void init() {
        SCHEDULER.scheduleAtFixedRate(DiscordRPC::tick, 0, 10, TimeUnit.SECONDS);
    }

    private static synchronized void tick() {
        if (!connected) {
            connect();
        }
        if (connected) {
            try {
                updateActivity();
            } catch (Exception e) {
                System.err.println("[DiscordRPC] Update failed: " + e.getMessage());
                disconnect();
            }
        }
    }

    private static void connect() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        for (int i = 0; i < 10; i++) {
            String path = isWindows
                    ? "\\\\.\\pipe\\discord-ipc-" + i
                    : Path.of(System.getenv().getOrDefault("XDG_RUNTIME_DIR", "/tmp"), "discord-ipc-" + i).toString();
            try {
                if (isWindows) {
                    connection = new WindowsConnection(path);
                } else {
                    connection = new UnixConnection(path);
                }
                // Handshake
                JsonObject handshake = new JsonObject();
                handshake.addProperty("v", 1);
                handshake.addProperty("client_id", String.valueOf(APP_ID));
                sendFrame(0, handshake);

                // Читаем ответ
                Frame response = connection.read();
                if (response.opCode != 1) {
                    throw new IOException("Unexpected handshake response: " + response.opCode);
                }
                JsonObject data = response.data;
                if (data.has("evt") && "READY".equals(data.get("evt").getAsString())) {
                    connected = true;
                    System.out.println("[DiscordRPC] Connected to Discord!");
                    return;
                }
                throw new IOException("Handshake failed: " + data);
            } catch (Exception e) {
                try { if (connection != null) connection.close(); } catch (Exception ignored) {}
                connection = null;
            }
        }
        System.err.println("[DiscordRPC] Discord not found (pipes 0-9)");
    }

    private static void updateActivity() throws IOException {
        Minecraft mc = Minecraft.getInstance();
        boolean inGame = mc.player != null && mc.level != null;

        Activity activity = new Activity.a()
                .a(ActivityType.PLAYING)
                .a(inGame ? "В игре" : "В меню")
                .b(inGame ? "Biwer Visuals" : "Ожидание")
                .a(startTime)
                .d("logo")
                .a();

        JsonObject args = new JsonObject();
        args.addProperty("pid", ProcessHandle.current().pid());
        args.add("activity", activity.j());

        JsonObject payload = new JsonObject();
        payload.addProperty("cmd", "SET_ACTIVITY");
        payload.add("args", args);
        payload.addProperty("nonce", String.valueOf(System.nanoTime()));

        sendFrame(1, payload);
    }

    private static void sendFrame(int opcode, JsonObject data) throws IOException {
        byte[] json = GSON.toJson(data).getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(8 + json.length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(opcode);
        buffer.putInt(json.length);
        buffer.put(json);
        connection.write(buffer.array());
    }

    private static void disconnect() {
        connected = false;
        try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        connection = null;
    }

    // ==================== Внутренние классы ====================

    private interface Connection extends Closeable {
        void write(byte[] data) throws IOException;
        Frame read() throws IOException;
    }

    private static class Frame {
        int opCode;
        JsonObject data;
        Frame(int opCode, JsonObject data) { this.opCode = opCode; this.data = data; }
    }

    private static class WindowsConnection implements Connection {
        private final RandomAccessFile file;

        WindowsConnection(String path) throws IOException {
            this.file = new RandomAccessFile(path, "rw");
        }

        @Override
        public void write(byte[] data) throws IOException {
            file.write(data);
        }

        @Override
        public Frame read() throws IOException {
            byte[] header = new byte[8];
            file.readFully(header);
            ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
            int opCode = buffer.getInt();
            int length = buffer.getInt();
            byte[] payload = new byte[length];
            if (length > 0) file.readFully(payload);
            JsonObject data = length > 0 ? GSON.fromJson(new String(payload, StandardCharsets.UTF_8), JsonObject.class) : null;
            return new Frame(opCode, data);
        }

        @Override
        public void close() throws IOException {
            file.close();
        }
    }

    private static class UnixConnection implements Connection {
        private final SocketChannel channel;

        UnixConnection(String path) throws IOException {
            this.channel = SocketChannel.open(StandardProtocolFamily.UNIX);
            this.channel.connect(UnixDomainSocketAddress.of(Path.of(path)));
            this.channel.configureBlocking(true);
        }

        @Override
        public void write(byte[] data) throws IOException {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.hasRemaining()) channel.write(buffer);
        }

        @Override
        public Frame read() throws IOException {
            ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            while (header.hasRemaining()) channel.read(header);
            header.flip();
            int opCode = header.getInt();
            int length = header.getInt();
            JsonObject data = null;
            if (length > 0) {
                ByteBuffer payload = ByteBuffer.allocate(length);
                while (payload.hasRemaining()) channel.read(payload);
                payload.flip();
                data = GSON.fromJson(new String(payload.array(), StandardCharsets.UTF_8), JsonObject.class);
            }
            return new Frame(opCode, data);
        }

        @Override
        public void close() throws IOException {
            channel.close();
        }
    }
}