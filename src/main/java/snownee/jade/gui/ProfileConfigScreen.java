package snownee.jade.gui;

import java.util.Objects;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.impl.config.WailaConfig;

public class ProfileConfigScreen extends BaseOptionsScreen {

	public ProfileConfigScreen(Screen parent) {
		super(parent, Component.translatable("gui.jade.jade_settings"));
		saver = IWailaConfig.get()::save;
		Runnable runnable = JadeClient.recoverKeysAction($ -> JadeClient.openConfig.getCategory().equals($.getCategory()));
		canceller = () -> {
			IWailaConfig.get().invalidate();
			runnable.run();
		};
	}

	@Override
	public OptionsList createOptions() {
		Objects.requireNonNull(minecraft);
		OptionsList options = new OptionsList(this, minecraft, width - 120, height - 32, 0, 26, IWailaConfig.get()::save);

		WailaConfig.Root root = Jade.rootConfig();
		options.title("general");
		options.choices("enable_profiles", root::isEnableProfiles, root::setEnableProfiles);

		options.title("key_binds");
		for (KeyMapping keyMapping : JadeClient.profiles) {
			options.keybind(keyMapping);
		}

		return options;
	}

}
