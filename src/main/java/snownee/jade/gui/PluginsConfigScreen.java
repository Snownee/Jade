package snownee.jade.gui;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;

public class PluginsConfigScreen extends PreviewOptionsScreen {

	@Nullable
	private Function<OptionsList, OptionsList.Entry> jumpTo;

	public PluginsConfigScreen(Screen parent) {
		super(parent, Component.translatable("gui.jade.plugin_settings"));
		saver = IWailaConfig.get()::save;
		canceller = IWailaConfig.get()::invalidate;
	}

	public static Screen createPluginConfigScreen(
			@Nullable Screen parent,
			@Nullable Function<OptionsList, OptionsList.Entry> jumpTo,
			boolean dontSave) {
		PluginsConfigScreen screen = new PluginsConfigScreen(parent);
		screen.jumpTo = jumpTo;
		return screen;
	}

	@Override
	public OptionsList createOptions() {
		OptionsList options = new OptionsList(
				this,
				Objects.requireNonNull(minecraft),
				width - 120,
				height - 32,
				0,
				26,
				IWailaConfig.get()::save);
		boolean noteServerFeature = minecraft.level == null || IWailaConfig.get().general().isDebug() || !ObjectDataCenter.serverConnected;
		BiConsumer<ResourceLocation, Object> setter = (key, value) -> {
			IWailaConfig.get().plugin().set(key, value);
			options.updateOptionValue(key);
		};
		WailaClientRegistration.instance().getConfigListView(IWailaConfig.get().accessibility().getEnableAccessibilityPlugin()).forEach(
				category -> {
					options.add(new OptionsList.Title(category.title()));
					MutableObject<OptionValue<?>> lastPrimary = new MutableObject<>();
					category.entries().forEach(entry -> {
						OptionValue<?> option = entry.createUI(
								options,
								"plugin_" + entry.id().toLanguageKey(),
								IWailaConfig.get().plugin(),
								setter);
						option.setId(entry.id());
						if (entry.isSynced()) {
							option.setDisabled(true);
							option.appendDescription(Component.translatable("gui.jade.forced_plugin_config")
									.withStyle(ChatFormatting.DARK_RED));
						} else if (noteServerFeature && !WailaClientRegistration.instance().isClientFeature(entry.id())) {
							option.serverFeature = true;
						}
						if (!IPluginConfig.isPrimaryKey(entry.id())) {
							if (lastPrimary.getValue() != null) {
								option.parent(lastPrimary.getValue());
							}
						} else {
							lastPrimary.setValue(option);
						}
					});
				});
		return options;
	}

	@Override
	protected void init() {
		super.init();
		if (jumpTo != null) {
			OptionsList.Entry entry = jumpTo.apply(options);
			if (entry != null) {
				options.showOnTop(entry);
			}
			jumpTo = null;
		}
	}
}
