package snownee.jade.gui;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.JadeKeys;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.gui.config.NotUglyEditBox;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.util.JsonConfig;

public class ProfileConfigScreen extends BaseOptionsScreen {

	private @Nullable OptionValue<Boolean> enabledEntry;

	public ProfileConfigScreen(Screen parent) {
		super(parent, Component.translatable("gui.jade.profile_settings"));
		saver = () -> {
			for (OptionsList.Entry entry : options().children()) {
				if (entry instanceof ProfileEntry profileEntry) {
					profileEntry.save();
				}
			}
			KeyMapping.resetMapping();
			Minecraft.getInstance().options.save();
		};
		boolean enabled = Jade.rootConfig().isEnableProfiles();
		int index = Jade.rootConfig().profileIndex;
		Runnable runnable = JadeClient.recoverKeysAction($ -> JadeKeys.openConfig().getCategory().equals($.getCategory()));
		canceller = () -> {
			if (enabled) {
				Jade.useProfile(index);
			} else {
				Jade.rootConfig().setEnableProfiles(false);
			}
			runnable.run();
		};
	}

	@Override
	public OptionsList createOptions(OptionsList options) {
		WailaConfig.Root root = Jade.rootConfig();
		options.title("profiles");
		enabledEntry = options.choices(
				"enable_profiles", root::isEnableProfiles, value -> {
					Jade.rootConfig().setEnableProfiles(value);
					refresh();
				});
		for (int i = 0; i < JadeClient.profiles.length; i++) {
			options.add(new ProfileEntry(i));
		}

		options.title("key_binds");
		for (KeyMapping keyMapping : JadeClient.profiles) {
			options.keybind(keyMapping);
		}

		return options;
	}

	@Override
	protected void init() {
		super.init();
		refresh();
	}

	public void refresh() {
		boolean enabled = Jade.rootConfig().isEnableProfiles();
		for (OptionsList.Entry entry : options().children()) {
			if (entry != enabledEntry) {
				entry.setDisabled(!enabled);
				if (entry instanceof ProfileEntry profileEntry) {
					profileEntry.refresh();
				}
			}
		}
	}

	public static class ProfileEntry extends OptionsList.Entry {
		public static final Component USE = Component.translatable("gui.jade.profile.use");
		public static final Component SAVE = Component.translatable("selectWorld.edit.save");
		private final int index;
		private final NotUglyEditBox editBox;
		private final @Nullable String originalName;

		public ProfileEntry(int index) {
			super(new StringWidget(Component.translatable("config.jade.profile." + index), Minecraft.getInstance().font));
			this.index = index;

			editBox = new NotUglyEditBox(font, 0, 0, 150, 20, title());
			editBox.fixedTextX = 4;
			editBox.fixedTextY = 7;
			editBox.fixedInnerWidth = editBox.getWidth() - 4 - 12;

			StringWidget titleWidget = (StringWidget) title;
			titleWidget.setMaxWidth(editBox.fixedInnerWidth, StringWidget.TextOverflow.CLAMPED);

			editBox.backgroundMode = NotUglyEditBox.BackgroundMode.HOVERING;
			editBox.setMaxLength(WailaConfig.MAX_NAME_LENGTH);
			editBox.setHint(title());
			editBox.setResponder(_ -> refresh());
			String name = Jade.configs().get(index).get().getName();
			if (name.startsWith("@")) {
				editBox.setValue(I18n.get(name.substring(1)));
				originalName = editBox.getValue();
			} else {
				editBox.setValue(name);
				originalName = null;
			}
			addWidget(new OptionsList.EntryWidget(editBox, 6, -editBox.getHeight() / 2, false));

			addWidget(
					Button.builder(
							USE, _ -> {
								Jade.useProfile(index);
								if (Minecraft.getInstance().screen instanceof ProfileConfigScreen screen) {
									screen.refresh();
								}
							}).size(48, 20).build(), 0);

			addWidget(
					Button.builder(
							SAVE, _ -> {
								if (JadeUI.hasControlDown()) {
									Jade.saveProfile(index);
									return;
								}
								Minecraft mc = Minecraft.getInstance();
								Screen screen = mc.screen;
								mc.setScreen(new ConfirmScreen(
										bl -> {
											if (bl) {
												Jade.saveProfile(index);
											}
											Minecraft.getInstance().setScreen(screen);
										},
										Component.translatable("gui.jade.save_profile.title"),
										Component.translatable("gui.jade.save_profile.message", normalTitle()),
										Component.translatable("gui.continue"),
										Component.translatable("gui.cancel")));
							}).size(48, 20).build(), 100 - 48);
		}

		public void refresh() {
			WailaConfig.Root root = Jade.rootConfig();
			boolean enabled = root.isEnableProfiles();
			boolean current = index == root.profileIndex;
			if (enabled && current) {
				setTitle(normalTitle().copy().withColor(0xFFFFFF55).append(Component.translatable("gui.jade.profile.active")));
			} else {
				setTitle(normalTitle());
			}
			for (AbstractWidget widget : children()) {
				if (widget instanceof AbstractStringWidget) {
					continue;
				}
				if (widget == editBox) {
					editBox.setTextColor(enabled && current ? 0xFFFFFF55 : 0xFFE0E0E0);
					editBox.setEditable(enabled);
				} else if (enabled) {
					widget.active = !current;
				}
			}
		}

		private Component normalTitle() {
			return editBox.getValue().isBlank() ?
					Component.translatable("config.jade.profile." + index) :
					Component.literal(editBox.getValue());
		}

		public void save() {
			JsonConfig<? extends WailaConfig> config = Jade.configs().get(index);
			if (originalName == null || !originalName.equals(editBox.getValue())) {
				config.get().setName(editBox.getValue());
			}
			config.save();
		}
	}

}
