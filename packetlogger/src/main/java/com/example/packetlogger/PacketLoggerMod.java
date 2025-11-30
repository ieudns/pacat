package com.example.packetlogger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PacketLoggerMod implements ClientModInitializer {

    public static boolean ENABLE_LOG = true;
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        System.out.println("[PacketLogger] Initialized");
        PacketLogManager.init();

        openGuiKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.packetlogger.open",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_P,
                        "category.packetlogger"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new PacketLoggerScreen());
                }
            }
        });
    }
}