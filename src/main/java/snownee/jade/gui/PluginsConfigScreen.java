package snownee.jade.gui;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.config.IWailaConfig;
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

	//TODO jump
	public static Screen createPluginConfigScreen(@Nullable Screen parent, String namespace, boolean dontSave) {
		Screen screen = new PluginsConfigScreen(parent);
		return screen;
	}

	@Override
	public WailaOptionsList createOptions() {
		WailaOptionsList options = new WailaOptionsList(this, minecraft, width - 120, height, 0, height - 32, 26, PluginConfig.INSTANCE::save);
		if (Minecraft.getInstance().level == null || IWailaConfig.get().getGeneral().isDebug() || !ObjectDataCenter.serverConnected) {
			options.serverFeatures = (int) PluginConfig.INSTANCE.getKeys().stream().filter(Predicate.not(WailaClientRegistration.INSTANCE::isClientFeature)).count();
		}
		PluginConfig.INSTANCE.getNamespaces().forEach(namespace -> {
			MutableComponent title;
			String translationKey = "plugin_" + namespace;
			if (ModIdentification.NAMES.containsKey(namespace)) {
				title = Component.literal(ModIdentification.getModName(namespace));
			} else {
				title = Component.translatable(translationKey);
			}
			options.add(new WailaOptionsList.Title(title));
			Set<ResourceLocation> keys = PluginConfig.INSTANCE.getKeys(namespace);
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
					entry.indent(1);
				}
			});
		});
		return options;
	}

}
