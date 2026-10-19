package org.codersoft.cleaspfabric.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Builds the shared InvisTags config screen. Used both by the Mod Menu
 * integration and by the in-game keybind, so the screen works even when
 * Mod Menu is not installed.
 */
public final class InvistagsConfigScreen {

    private InvistagsConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("config.invistags.title"))
            .setSavingRunnable(ModConfig::save);

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(
            Component.translatable("config.invistags.category.general"));

        general.addEntry(entries
            .startBooleanToggle(
                Component.translatable("config.invistags.showInvisibleSkin"),
                ModConfig.showInvisibleSkin)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.invistags.showInvisibleSkin.tooltip"))
            .setSaveConsumer(value -> ModConfig.showInvisibleSkin = value)
            .build());

        general.addEntry(entries
            .startBooleanToggle(
                Component.translatable("config.invistags.showInvisibleNametag"),
                ModConfig.showInvisibleNametag)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.invistags.showInvisibleNametag.tooltip"))
            .setSaveConsumer(value -> ModConfig.showInvisibleNametag = value)
            .build());

        general.addEntry(entries
            .startBooleanToggle(
                Component.translatable("config.invistags.showInvisibleMobs"),
                ModConfig.showInvisibleMobs)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("config.invistags.showInvisibleMobs.tooltip"))
            .setSaveConsumer(value -> ModConfig.showInvisibleMobs = value)
            .build());

        general.addEntry(entries
            .startTextDescription(Component.translatable("config.invistags.hint"))
            .build());

        return builder.build();
    }
}
