package snownee.jade.gui;

import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton.Builder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.gui.config.OptionButton;
import snownee.jade.gui.config.WailaOptionsList;
import snownee.jade.gui.config.WailaOptionsList.Entry;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.ConfigEntry;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.util.ModIdentification;

public class PluginsConfigScreen extends BaseOptionsScreen {

	public PluginsConfigScreen(Screen parent) {
		super(parent, new TranslatableComponent("gui.jade.plugin_settings"), PluginConfig.INSTANCE::save, PluginConfig.INSTANCE::reload);
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
				minecraft.setScreen(new BaseOptionsScreen(PluginsConfigScreen.this, title, null, null) {
					@Override
					public WailaOptionsList getOptions() {
						WailaOptionsList options = new WailaOptionsList(this, minecraft, width, height, 32, height - 32, 30);
						keys.stream().sorted((o1, o2) -> WailaCommonRegistration.INSTANCE.priorities.get(o1) - WailaCommonRegistration.INSTANCE.priorities.get(o2)).forEach(i -> {
							ConfigEntry configEntry = PluginConfig.INSTANCE.getEntry(i);
							Consumer<Builder<Boolean>> tooltip = null;
							boolean synced = configEntry.isSynced() && minecraft.level != null && !minecraft.hasSingleplayerServer();
							if (synced)
								tooltip = b -> b.withTooltip(bl -> minecraft.font.split(new TranslatableComponent("gui.jade.forced_plugin_config"), 200));
							Entry entry = options.choices(translationKey + "." + i.getPath(), configEntry.getValue(), b -> PluginConfig.INSTANCE.set(i, b), tooltip);
							if (synced)
								entry.setDisabled(true);
						});
						return options;
					}
				});
			})));
		});
		return options;
	}
}
