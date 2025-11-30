package com.example.packetlogger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PacketLoggerScreen extends Screen {

    private static final int LINE_HEIGHT = 12;

    public PacketLoggerScreen() {
        super(Text.literal("Packet Logger"));
    }

    @Override
    protected void init() {
        int mid = width / 2;
        int y = height - 30;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Dump Buffer"),
                b -> PacketLogManager.dumpCurrentBuffer()
        ).dimensions(mid - 110, y, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                b -> MinecraftClient.getInstance().setScreen(null)
        ).dimensions(mid + 10, y, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);

        ctx.drawCenteredTextWithShadow(textRenderer, "Packet Logger", width / 2, 10, 0xFFFFFF);

        List<PacketLogManager.PacketLogEntry> list = PacketLogManager.getRecentEntries();
        int y = 30;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        for (PacketLogManager.PacketLogEntry e : list) {
            String line = "[" + sdf.format(new Date(e.timeMillis)) + "] "
                    + (e.direction == PacketLogManager.Direction.SEND ? "S" : "R")
                    + " " + e.packetClass + " - " + e.summary;

            ctx.drawTextWithShadow(textRenderer, line, 10, y, e.direction == PacketLogManager.Direction.SEND ? 0x55FF55 : 0x5599FF);
            y += LINE_HEIGHT;
            if (y >= height - 50) break;
        }

        super.render(ctx, mouseX, mouseY, delta);
    }
}