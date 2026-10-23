package org.codersoft.cleaspfabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import org.codersoft.cleaspfabric.client.InvistagsConfigScreen;
import org.codersoft.cleaspfabric.client.ModConfig;

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

            context.runOnClient(client -> client.gui.setScreen(InvistagsConfigScreen.create(null)));
            context.waitTicks(20);
            if (!context.computeOnClient(client -> client.gui.screen() != null)) {
                throw new AssertionError("InvisTags config screen did not open");
            }
            context.takeScreenshot("invistags-config");
            context.runOnClient(client -> client.gui.setScreen(null));

            context.runOnClient(client -> client.player.connection.sendCommand("invis"));
            context.waitTicks(10);
            context.takeScreenshot("invistags-status");
        }
    }
}
