package com.example.packetlogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class PacketLogManager {

    public enum Direction {
        SEND,
        RECEIVE
    }

    public static class PacketLogEntry {
        public final long timeMillis;
        public final Direction direction;
        public final String packetClass;
        public final String summary;

        public PacketLogEntry(long timeMillis, Direction direction, String packetClass, String summary) {
            this.timeMillis = timeMillis;
            this.direction = direction;
            this.packetClass = packetClass;
            this.summary = summary;
        }
    }

    private static final int MAX_ENTRIES = 200;
    private static final Deque<PacketLogEntry> ENTRIES = new ArrayDeque<>();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static Path sessionLogFile;

    public static void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        Path baseDir = client.runDirectory.toPath().resolve("packetlogs");

        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            System.err.println("[PacketLogger] Failed to create directory: " + e);
        }

        String ts = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        sessionLogFile = baseDir.resolve("packets-" + ts + ".jsonl");
    }

    public static void logPacket(Direction direction, Packet<?> packet) {
        long now = System.currentTimeMillis();
        PacketLogEntry entry = new PacketLogEntry(
                now,
                direction,
                packet.getClass().getSimpleName(),
                packet.toString()
        );

        synchronized (ENTRIES) {
            ENTRIES.addLast(entry);
            while (ENTRIES.size() > MAX_ENTRIES) {
                ENTRIES.removeFirst();
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                sessionLogFile,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
        )) {
            writer.write(GSON.toJson(entry));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("[PacketLogger] Failed to write: " + e);
        }
    }

    public static List<PacketLogEntry> getRecentEntries() {
        synchronized (ENTRIES) {
            return new ArrayList<>(ENTRIES);
        }
    }

    public static void dumpCurrentBuffer() {
        MinecraftClient client = MinecraftClient.getInstance();
        Path baseDir = client.runDirectory.toPath().resolve("packetlogs");
        String ts = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        Path dump = baseDir.resolve("dump-" + ts + ".jsonl");

        List<PacketLogEntry> snapshot = getRecentEntries();

        try (BufferedWriter writer = Files.newBufferedWriter(dump)) {
            for (PacketLogEntry e : snapshot) {
                writer.write(GSON.toJson(e));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[PacketLogger] Failed to dump buffer: " + e);
        }
    }
}