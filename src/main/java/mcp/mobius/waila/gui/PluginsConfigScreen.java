package mcp.mobius.waila.gui;

import java.util.Set;

import mcp.mobius.waila.gui.config.OptionButton;
import mcp.mobius.waila.gui.config.WailaOptionsList;
import mcp.mobius.waila.impl.config.ConfigEntry;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class PluginsConfigScreen extends OptionsScreen {

	public PluginsConfigScreen(Screen parent) {
		super(parent, new TranslatableComponent("gui.waila.plugin_settings"), PluginConfig.INSTANCE::save, PluginConfig.INSTANCE::reload);
	}

	@Override
	public WailaOptionsList getOptions() {
		WailaOptionsList options = new WailaOptionsList(this, minecraft, width, height, 32, height - 32, 30, PluginConfig.INSTANCE::save);
		PluginConfig.INSTANCE.getNamespaces().forEach(namespace -> {
			Component title;
			String translationKey = "plugin_" + namespace;
			if (ModIdentification.NAMES.containsKey(namespace)) {
				title = new TextComponent(ModIdentification.getModName(namespace));
			} else {
				title = new TranslatableComponent(translationKey);
			}
			Set<ResourceLocation> keys = PluginConfig.INSTANCE.getKeys(namespace);
			options.add(new OptionButton(title, new Button(0, 0, 100, 20, TextComponent.EMPTY, w -> {
				minecraft.setScreen(new OptionsScreen(PluginsConfigScreen.this, title, null, null) {
					@Override
					public WailaOptionsList getOptions() {
						WailaOptionsList options = new WailaOptionsList(this, minecraft, width, height, 32, height - 32, 30);
						keys.stream().sorted((o1, o2) -> o1.getPath().compareToIgnoreCase(o2.getPath())).forEach(i -> {
							ConfigEntry entry = PluginConfig.INSTANCE.getEntry(i);
							if (!entry.isSynced() || Minecraft.getInstance().getCurrentServer() == null)
								options.choices(translationKey + "." + i.getPath(), entry.getValue(), b -> PluginConfig.INSTANCE.set(i, b));
						});
						return options;
					}
				});
			})));
		});
		return options;
	}
}
