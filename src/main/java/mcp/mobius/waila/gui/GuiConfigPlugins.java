package mcp.mobius.waila.gui;

import mcp.mobius.waila.api.impl.config.ConfigEntry;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import mcp.mobius.waila.gui.config.OptionsEntryButton;
import mcp.mobius.waila.gui.config.OptionsListWidget;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Set;

public class GuiConfigPlugins extends GuiOptions {

    public GuiConfigPlugins(Screen parent) {
        super(parent, new TranslationTextComponent("gui.waila.plugin_settings"), PluginConfig.INSTANCE::save, PluginConfig.INSTANCE::reload);
    }

    @Override
    public OptionsListWidget getOptions() {
        OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30, PluginConfig.INSTANCE::save);
        PluginConfig.INSTANCE.getNamespaces().forEach(namespace -> {
            String translationKey = "config.waila.plugin_" + namespace;
            Set<ResourceLocation> keys = PluginConfig.INSTANCE.getKeys(namespace);
            options.add(new OptionsEntryButton(translationKey, new Button(0, 0, 100, 20, new StringTextComponent(""), w -> {
                minecraft.displayGuiScreen(new GuiOptions(GuiConfigPlugins.this, new TranslationTextComponent(translationKey)) {
                    @Override
                    public OptionsListWidget getOptions() {
                        OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
                        keys.stream().sorted((o1, o2) -> o1.getPath().compareToIgnoreCase(o2.getPath())).forEach(i -> {
                            ConfigEntry entry = PluginConfig.INSTANCE.getEntry(i);
                            if (!entry.isSynced() || Minecraft.getInstance().getCurrentServerData() == null)
                                options.add(new OptionsEntryValueBoolean(translationKey + "." + i.getPath(), entry.getValue(), b -> PluginConfig.INSTANCE.set(i, b)));
                        });
                        return options;
                    }
                });
            })));
        });
        return options;
    }
}
