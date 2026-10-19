package org.codersoft.cleaspfabric.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModConfigTest {

    @TempDir
    Path tempDir;

    private Path configFile;

    @BeforeEach
    void setUp() {
        configFile = tempDir.resolve("invistags.json");
        ModConfig.showInvisibleSkin = true;
        ModConfig.showInvisibleNametag = true;
        ModConfig.showInvisibleMobs = false;
    }

    @AfterEach
    void reset() {
        ModConfig.showInvisibleSkin = true;
        ModConfig.showInvisibleNametag = true;
        ModConfig.showInvisibleMobs = false;
    }

    @Test
    void saveWritesCurrentValues() throws Exception {
        ModConfig.showInvisibleSkin = false;
        ModConfig.showInvisibleNametag = true;

        assertTrue(ModConfig.save(configFile));

        String json = Files.readString(configFile);
        assertTrue(json.contains("\"showInvisibleSkin\": false"));
        assertTrue(json.contains("\"showInvisibleNametag\": true"));
    }

    @Test
    void loadRestoresSavedValues() {
        ModConfig.showInvisibleSkin = false;
        ModConfig.showInvisibleNametag = false;
        ModConfig.showInvisibleMobs = true;
        assertTrue(ModConfig.save(configFile));

        ModConfig.showInvisibleSkin = true;
        ModConfig.showInvisibleNametag = true;
        ModConfig.showInvisibleMobs = false;

        assertTrue(ModConfig.load(configFile));
        assertFalse(ModConfig.showInvisibleSkin);
        assertFalse(ModConfig.showInvisibleNametag);
        assertTrue(ModConfig.showInvisibleMobs);
    }

    @Test
    void missingFileIsCreatedWithDefaults() throws Exception {
        assertFalse(Files.exists(configFile));

        assertTrue(ModConfig.load(configFile));

        assertTrue(Files.exists(configFile));
        assertTrue(ModConfig.showInvisibleSkin);
        assertTrue(ModConfig.showInvisibleNametag);
    }

    @Test
    void malformedJsonKeepsDefaults() throws Exception {
        Files.writeString(configFile, "{ this is not json");

        assertFalse(ModConfig.load(configFile));
        assertTrue(ModConfig.showInvisibleSkin);
        assertTrue(ModConfig.showInvisibleNametag);
    }

    @Test
    void partialFileKeepsUnspecifiedDefaults() throws Exception {
        Files.writeString(configFile, "{\"showInvisibleSkin\": false}");

        assertTrue(ModConfig.load(configFile));
        assertFalse(ModConfig.showInvisibleSkin);
        assertTrue(ModConfig.showInvisibleNametag);
    }

    @Test
    void unknownKeysAreIgnored() throws Exception {
        Files.writeString(configFile, "{\"showInvisibleNametag\": false, \"foo\": 123}");

        assertTrue(ModConfig.load(configFile));
        assertTrue(ModConfig.showInvisibleSkin);
        assertFalse(ModConfig.showInvisibleNametag);
    }

    @Test
    void wrongValueTypeIsIgnored() throws Exception {
        Files.writeString(configFile, "{\"showInvisibleSkin\": \"yes\"}");

        assertTrue(ModConfig.load(configFile));
        assertTrue(ModConfig.showInvisibleSkin);
        assertTrue(ModConfig.showInvisibleNametag);
    }
}
