package org.codersoft.cleaspfabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CleaspfabricClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("invisnametag");

    private static KeyMapping toggleKey;
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("InvisNametag loaded. Invisible players can no longer hide.");
        ModConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("invistags", "keybinds"));

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.invistags.toggle",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_I,
            category
        ));

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.invistags.config",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_O,
            category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GearReveal.tick(client);
            VanishedPlayers.tick(client);
            while (toggleKey.consumeClick()) {
                toggleEverything(client);
            }
            while (configKey.consumeClick()) {
                client.setScreenAndShow(InvistagsConfigScreen.create(null));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("invis")
                .then(ClientCommands.literal("config")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        client.setScreenAndShow(InvistagsConfigScreen.create(null));
                        return 1;
                    })
                )
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
                            ModConfig.save();
                            context.getSource().sendFeedback(Component.literal(
                                "Skin visibility set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
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
                            ModConfig.save();
                            context.getSource().sendFeedback(Component.literal(
                                "Nametag visibility set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommands.literal("mobs")
                    .executes(context -> {
                        boolean current = ModConfig.showInvisibleMobs;
                        context.getSource().sendFeedback(Component.literal(
                            "Mob reveal: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommands.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.showInvisibleMobs = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Component.literal(
                                "Mob reveal set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommands.literal("gear")
                    .executes(context -> {
                        boolean current = ModConfig.showInvisibleGear;
                        context.getSource().sendFeedback(Component.literal(
                            "Gear reveal: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommands.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.showInvisibleGear = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Component.literal(
                                "Gear reveal set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommands.literal("vanished")
                    .executes(context -> {
                        boolean current = ModConfig.flagVanishedPlayers;
                        List<String> names = VanishedPlayers.flaggedNames(Minecraft.getInstance());
                        String flagged = names.isEmpty()
                            ? " §7(none currently flagged)"
                            : " §7(flagged: " + String.join(", ", names) + ")";
                        context.getSource().sendFeedback(Component.literal(
                            "Vanished flag: " + (current ? "§aON" : "§cOFF") + flagged));
                        return 1;
                    })
                    .then(ClientCommands.literal("list")
                        .executes(context -> {
                            List<String> names = VanishedPlayers.flaggedNames(Minecraft.getInstance());
                            context.getSource().sendFeedback(Component.literal(names.isEmpty()
                                ? "§7No vanished players currently flagged."
                                : "§cVanished players: §f" + String.join(", ", names)));
                            return 1;
                        })
                    )
                    .then(ClientCommands.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.flagVanishedPlayers = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Component.literal(
                                "Vanished flag set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommands.literal("remember")
                    .executes(context -> {
                        boolean current = ModConfig.rememberState;
                        context.getSource().sendFeedback(Component.literal(
                            "Remember state: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommands.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.rememberState = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Component.literal(
                                "Remember state set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .executes(context -> {
                    context.getSource().sendFeedback(Component.literal(
                        "§6=== InvisTags Config ==="));
                    context.getSource().sendFeedback(Component.literal(
                        "Skin: " + (ModConfig.showInvisibleSkin ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Component.literal(
                        "Nametag: " + (ModConfig.showInvisibleNametag ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Component.literal(
                        "Mobs: " + (ModConfig.showInvisibleMobs ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Component.literal(
                        "Gear: " + (ModConfig.showInvisibleGear ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Component.literal(
                        "Vanished flag: " + (ModConfig.flagVanishedPlayers ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Component.literal(
                        "Remember state: " + (ModConfig.rememberState ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Component.literal(
                        "§7Use /invis skin|nametag|mobs|gear|vanished [true|false], /invis vanished list, /invis remember [true|false], or /invis config"));
                    return 1;
                })
            );
        });
    }

    private static void toggleEverything(Minecraft client) {
        boolean enabled = !(ModConfig.showInvisibleSkin
            && ModConfig.showInvisibleNametag
            && ModConfig.showInvisibleGear);
        ModConfig.showInvisibleSkin = enabled;
        ModConfig.showInvisibleNametag = enabled;
        ModConfig.showInvisibleGear = enabled;
        ModConfig.save();

        if (client.player != null) {
            client.player.sendOverlayMessage(Component.translatable(
                "message.invistags.toggled",
                Component.translatable(enabled ? "message.invistags.on" : "message.invistags.off")
            ));
        }
    }
}
