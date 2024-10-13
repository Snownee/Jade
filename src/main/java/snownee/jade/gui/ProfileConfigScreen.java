package snownee.jade.gui;

import java.util.Objects;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.config.OptionButton;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.config.WailaConfig;

public class ProfileConfigScreen extends BaseOptionsScreen {

	private OptionValue<Boolean> enabledEntry;

	public ProfileConfigScreen(Screen parent) {
		super(parent, Component.translatable("gui.jade.profile_settings"));
		saver = () -> {
			IWailaConfig.get().save();
			KeyMapping.resetMapping();
			Minecraft.getInstance().options.save();
		};
		boolean enabled = Jade.rootConfig().isEnableProfiles();
		int index = Jade.rootConfig().profileIndex;
		Runnable runnable = JadeClient.recoverKeysAction($ -> JadeClient.openConfig.getCategory().equals($.getCategory()));
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
	public OptionsList createOptions() {
		Objects.requireNonNull(minecraft);
		OptionsList options = new OptionsList(this, minecraft, width - 120, height - 32, 0, 26, IWailaConfig.get()::save);

		WailaConfig.Root root = Jade.rootConfig();
		options.title("general");
		enabledEntry = options.choices("enable_profiles", root::isEnableProfiles, value -> {
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
		for (OptionsList.Entry entry : options.children()) {
			if (entry != enabledEntry) {
				entry.setDisabled(!enabled);
				if (entry instanceof ProfileEntry profileEntry) {
					profileEntry.refresh();
				}
			}
		}
	}

	public static class ProfileEntry extends OptionButton {
		public static final Component USE = Component.translatable("gui.jade.profile.use");
		public static final Component SAVE = Component.translatable("selectWorld.edit.save");
		private final int index;
		private final Component normalTitle;

		public ProfileEntry(int index) {
			super(Component.translatable("config.jade.profile." + index), Button.builder(USE, $ -> {
				Jade.useProfile(index);
				if (Minecraft.getInstance().screen instanceof ProfileConfigScreen screen) {
					screen.refresh();
				}
			}).size(48, 20).build());
			this.index = index;
			this.normalTitle = title;
			addWidget(Button.builder(SAVE, $ -> {
				if (Screen.hasControlDown()) {
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
						Component.translatable("gui.jade.save_profile.message", normalTitle),
						Component.translatable("gui.continue"),
						Component.translatable("gui.cancel")));
			}).size(48, 20).build(), 100 - 48);
			addMessage(SAVE.getString());
		}

		public void refresh() {
			WailaConfig.Root root = Jade.rootConfig();
			boolean current = index == root.profileIndex;
			if (current) {
				title = normalTitle.copy().append(Component.translatable("gui.jade.profile.active"));
			} else {
				title = normalTitle;
			}
			if (!root.isEnableProfiles()) {
				return;
			}
			for (AbstractWidget widget : widgets) {
				widget.active = !current;
			}
		}
	}

}
