package snownee.jade.gui;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.config.OptionButton;
import snownee.jade.gui.config.WailaOptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.entry.ConfigEntry;
import snownee.jade.util.ModIdentification;

public class PluginsConfigScreen extends BaseOptionsScreen {

	public PluginsConfigScreen(Screen parent) {
		super(parent, Component.translatable("gui.jade.plugin_settings"), PluginConfig.INSTANCE::save, PluginConfig.INSTANCE::reload);
	}

	@Override
	public WailaOptionsList createOptions() {
		WailaOptionsList options = new WailaOptionsList(this, minecraft, width, height, 32, height - 32, 30, PluginConfig.INSTANCE::save);
		PluginConfig.INSTANCE.getNamespaces().forEach(namespace -> {
			Component title;
			String translationKey = "plugin_" + namespace;
			if (ModIdentification.NAMES.containsKey(namespace)) {
				title = Component.literal(ModIdentification.getModName(namespace));
			} else {
				title = Component.translatable(translationKey);
			}
			options.add(new OptionButton(title, new Button(0, 0, 100, 20, Component.empty(), w -> {
				minecraft.setScreen(createPluginConfigScreen(this, namespace, true));
			})));
		});
		return options;
	}

	public static Screen createPluginConfigScreen(@Nullable Screen parent, String namespace, boolean dontSave) {
		Component title;
		String translationKey = "plugin_" + namespace;
		if (ModIdentification.NAMES.containsKey(namespace)) {
			title = Component.literal(ModIdentification.getModName(namespace));
		} else {
			title = Component.translatable(translationKey);
		}
		return new BaseOptionsScreen(parent, title, dontSave ? null : PluginConfig.INSTANCE::save, dontSave ? null : PluginConfig.INSTANCE::reload) {
			@Override
			public WailaOptionsList createOptions() {
				Set<ResourceLocation> keys = PluginConfig.INSTANCE.getKeys(namespace);
				WailaOptionsList options = new WailaOptionsList(this, minecraft, width, height, 32, height - 32, 30);
				if (Minecraft.getInstance().level == null || IWailaConfig.get().getGeneral().isDebug() || !ObjectDataCenter.serverConnected) {
					options.serverFeatures = (int) keys.stream().filter(Predicate.not(WailaClientRegistration.INSTANCE::isClientFeature)).count();
				}
				keys.stream().sorted(Comparator.comparingInt(WailaCommonRegistration.INSTANCE.priorities.getSortedList()::indexOf)).forEach(i -> {
					ConfigEntry<?> configEntry = PluginConfig.INSTANCE.getEntry(i);
					OptionValue<?> entry = configEntry.createUI(options, translationKey + "." + i.getPath());
					if (configEntry.isSynced()) {
						entry.setDisabled(true);
						entry.appendDescription(ChatFormatting.DARK_RED + I18n.get("gui.jade.forced_plugin_config"));
					} else if (options.serverFeatures > 0 && !WailaClientRegistration.INSTANCE.isClientFeature(i)) {
						entry.getTitle().append(Component.literal("*").withStyle(ChatFormatting.GRAY));
					}
					if (i.getPath().contains(".")) {
						entry.indent = 12;
					}
				});
				return options;
			}
		};
	}

}
