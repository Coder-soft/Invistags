package org.codersoft.cleaspfabric.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleaspfabricClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("invisnametag");

    @Override
    public void onInitializeClient() {
        LOGGER.info("InvisNametag loaded. Invisible players can no longer hide.");

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("invis")
                .then(ClientCommands.literal("skin")
                    .executes(context -> {
                        boolean current = ModConfig.showInvisibleSkin;
                        context.getSource().sendFeedback(Component.literal(
                            "Skin visibility: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommands.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.showInvisibleSkin = value;
                            context.getSource().sendFeedback(Component.literal(
                                "Skin visibility set to " + (value ? "§aON" : "§cOFF")));
                            return 1;
                        })
                    )
                )
                .then(ClientCommands.literal("nametag")
                    .executes(context -> {
                        boolean current = ModConfig.showInvisibleNametag;
                        context.getSource().sendFeedback(Component.literal(
                            "Nametag visibility: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommands.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.showInvisibleNametag = value;
                            context.getSource().sendFeedback(Component.literal(
                                "Nametag visibility set to " + (value ? "§aON" : "§cOFF")));
                            return 1;
                        })
                    )
                )
                .executes(context -> {
                    context.getSource().sendFeedback(Component.literal(
                        "§6=== CleaspFabric Config ==="));
                    context.getSource().sendFeedback(Component.literal(
                        "Skin: " + (ModConfig.showInvisibleSkin ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Component.literal(
                        "Nametag: " + (ModConfig.showInvisibleNametag ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Component.literal(
                        "§7Use /cleaspfabric <skin|nametag> [true|false]"));
                    return 1;
                })
            );
        });
    }
}
