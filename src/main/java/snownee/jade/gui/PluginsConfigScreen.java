package snownee.jade.gui;

import java.util.Comparator;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.entry.ConfigEntry;
import snownee.jade.util.ModIdentification;

public class PluginsConfigScreen extends PreviewOptionsScreen {

	private final MutableObject<OptionsList.Entry> jumpToEntry = new MutableObject<>();
	private String jumpTo;

	public PluginsConfigScreen(Screen parent) {
		super(parent, Component.translatable("gui.jade.plugin_settings"));
		saver = PluginConfig.INSTANCE::save;
		canceller = PluginConfig.INSTANCE::reload;
	}

	public static Screen createPluginConfigScreen(@Nullable Screen parent, @Nullable String namespace, boolean dontSave) {
		PluginsConfigScreen screen = new PluginsConfigScreen(parent);
		screen.jumpTo = namespace;
		return screen;
	}

	@Override
	public OptionsList createOptions() {
		OptionsList options = new OptionsList(this, minecraft, width - 120, height, 0, height - 32, 26, PluginConfig.INSTANCE::save);
		boolean noteServerFeature = Minecraft.getInstance().level == null || IWailaConfig.get().getGeneral().isDebug() || !ObjectDataCenter.serverConnected;
		PluginConfig.INSTANCE.getNamespaces().forEach(namespace -> {
			MutableComponent title;
			String translationKey = "plugin_" + namespace;
			if (!Jade.MODID.equals(namespace) && ModIdentification.NAMES.containsKey(namespace)) {
				title = Component.literal(ModIdentification.getModName(namespace));
			} else {
				title = Component.translatable(OptionsList.Entry.makeKey(translationKey));
			}
			if (namespace.equals(jumpTo)) {
				jumpToEntry.setValue(options.add(new OptionsList.Title(title)));
			} else {
				options.add(new OptionsList.Title(title));
			}
			Set<ResourceLocation> keys = PluginConfig.INSTANCE.getKeys(namespace);
			MutableObject<OptionValue<?>> lastPrimary = new MutableObject<>();
			keys.stream().sorted(Comparator.comparingInt(WailaCommonRegistration.instance().priorities.getSortedList()::indexOf)).forEach(i -> {
				ConfigEntry<?> configEntry = PluginConfig.INSTANCE.getEntry(i);
				OptionValue<?> entry = configEntry.createUI(options, translationKey + "." + i.getPath());
				if (configEntry.isSynced()) {
					entry.setDisabled(true);
					entry.appendDescription(ChatFormatting.DARK_RED + I18n.get("gui.jade.forced_plugin_config"));
				} else if (noteServerFeature && !WailaClientRegistration.instance().isClientFeature(i)) {
					entry.serverFeature = true;
				}
				if (i.getPath().contains(".")) {
					if (lastPrimary.getValue() != null) {
						entry.parent(lastPrimary.getValue());
					}
				} else {
					lastPrimary.setValue(entry);
				}
			});
		});
		jumpTo = null;
		return options;
	}

	@Override
	protected void init() {
		super.init();
		if (jumpToEntry.getValue() != null) {
			options.showOnTop(jumpToEntry.getValue());
			jumpToEntry.setValue(null);
		}
	}
}
