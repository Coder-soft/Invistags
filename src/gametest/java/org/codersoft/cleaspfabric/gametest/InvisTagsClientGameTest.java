package org.codersoft.cleaspfabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.phys.Vec3;
import org.codersoft.cleaspfabric.client.InvistagsConfigScreen;
import org.codersoft.cleaspfabric.client.ModConfig;

import java.io.File;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Loads a real singleplayer world so every client renderer and the mixins applied
 * to them are exercised, turns the new toggles on, opens the config screen and runs
 * the /invis command. A missing mixin target or a broken config entry fails here.
 */
public final class InvisTagsClientGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            context.waitTicks(60);

            context.runOnClient(client -> {
                ModConfig.showInvisibleSkin = true;
                ModConfig.showInvisibleNametag = true;
                ModConfig.showInvisibleGear = true;
                ModConfig.flagVanishedPlayers = true;
            });
            context.waitTicks(20);

            context.runOnClient(client -> client.setScreenAndShow(InvistagsConfigScreen.create(null)));
            context.waitTicks(20);
            if (!context.computeOnClient(client -> client.screen != null)) {
                throw new AssertionError("InvisTags config screen did not open");
            }
            context.takeScreenshot("invistags-config");
            context.runOnClient(client -> client.setScreen(null));

            context.runOnClient(client -> client.player.connection.sendCommand("invis"));
            context.waitTicks(10);
            context.takeScreenshot("invistags-status");
        }

        // Two-player pixel test: a real dedicated server with the Manchulla
        // mineflayer bot as an invisible player, observed by this modded client.
        // Opt-in because it needs the bot checkout and node on the machine.
        if ("1".equals(System.getenv("INVISTAGS_BOT_TEST"))) {
            try {
                runTwoPlayerGearTest(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void runTwoPlayerGearTest(ClientGameTestContext context) throws Exception {
        Properties props = new Properties();
        props.setProperty("online-mode", "false");
        props.setProperty("enforce-secure-profile", "false");
        props.setProperty("server-port", "25566");
        props.setProperty("max-players", "10");
        props.setProperty("view-distance", "6");
        props.setProperty("level-type", "minecraft:flat");
        props.setProperty("generator-settings",
            "{\"layers\":[{\"block\":\"minecraft:bedrock\",\"height\":1},"
                + "{\"block\":\"minecraft:dirt\",\"height\":2},"
                + "{\"block\":\"minecraft:grass_block\",\"height\":1}],"
                + "\"biome\":\"minecraft:plains\"}");
        props.setProperty("gamemode", "creative");
        props.setProperty("difficulty", "peaceful");
        props.setProperty("spawn-monsters", "false");
        props.setProperty("spawn-npcs", "false");
        props.setProperty("spawn-animals", "false");
        props.setProperty("spawn-protection", "0");
        props.setProperty("allow-flight", "true");
        props.setProperty("pause-when-empty-seconds", "0");

        try (TestDedicatedServerContext server = context.worldBuilder().createServer(props);
             TestServerConnection connection = server.connect()) {
            connection.getClientLevel().waitForChunksRender();

            int port = server.computeOnServer(s -> s.getPort());
            Process bot = new ProcessBuilder(
                System.getenv().getOrDefault("INVISTAGS_NODE", "node"),
                "src/index.js", "127.0.0.1", Integer.toString(port), "GearBot")
                .directory(new File(System.getProperty("user.home"), "manchullabot"))
                .redirectErrorStream(true)
                .redirectOutput(new File(System.getProperty("java.io.tmpdir"), "invistags-gearbot.log"))
                .start();
            try {
                boolean joined = false;
                for (int i = 0; i < 1200 && !joined; i++) {
                    context.waitTicks(1);
                    joined = context.computeOnClient(c -> c.level != null && c.level.players().stream()
                        .anyMatch(p -> "GearBot".equals(p.getName().getString())));
                }
                if (!joined) {
                    throw new AssertionError("GearBot did not join the dedicated server");
                }
                server.runCommand("op GearBot");
                server.runCommand("time set day");
                server.runCommand("weather clear");

                Vec3 pos = context.computeOnClient(c -> c.player.position());
                server.runCommand(String.format(Locale.ROOT,
                    "tp GearBot %.2f %.2f %.2f", pos.x, pos.y, pos.z + 4.0));
                context.runOnClient(c -> {
                    c.player.setYRot(0.0F);
                    c.player.setXRot(0.0F);
                });
                context.waitTicks(20);

                String[][] gear = {
                    {"armor.head", "minecraft:diamond_helmet"},
                    {"armor.chest", "minecraft:diamond_chestplate"},
                    {"armor.legs", "minecraft:diamond_leggings"},
                    {"armor.feet", "minecraft:diamond_boots"},
                    {"weapon.mainhand", "minecraft:diamond_sword"},
                    {"weapon.offhand", "minecraft:shield"}
                };
                for (String[] slot : gear) {
                    server.runCommand("item replace entity GearBot " + slot[0] + " with " + slot[1]);
                }
                context.waitTicks(40);
                context.takeScreenshot("invistags-gear-visible");

                // Invisible + equipment emptied server-side: the state InvisTags must repair.
                server.runCommand("effect give GearBot minecraft:invisibility 600 1 true");
                for (String[] slot : gear) {
                    server.runCommand("item replace entity GearBot " + slot[0] + " with minecraft:air");
                }
                context.waitTicks(40);
                context.takeScreenshot("invistags-gear-invisible-on");

                context.runOnClient(c -> ModConfig.showInvisibleGear = false);
                context.waitTicks(20);
                context.takeScreenshot("invistags-gear-invisible-off");
                context.runOnClient(c -> ModConfig.showInvisibleGear = true);
            } finally {
                bot.destroy();
                if (!bot.waitFor(5, TimeUnit.SECONDS)) {
                    bot.destroyForcibly();
                }
            }
        }
    }
}
