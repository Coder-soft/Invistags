package org.codersoft.cleaspfabric.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleaspfabricClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("invisnametag");

    @Override
    public void onInitializeClient() {
        LOGGER.info("InvisNametag loaded. Invisible players can no longer hide.");
    }
}
