package snownee.jade.gui;

import java.util.Objects;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.gui.config.OptionsList;

public abstract class PreviewOptionsScreen extends BaseOptionsScreen {

	public PreviewOptionsScreen(Screen parent, Component title) {
		super(parent, title);
	}

	@Override
	protected void init() {
		Objects.requireNonNull(minecraft);
		super.init();
		if (minecraft.level != null) {
			CycleButton<Boolean> previewButton = CycleButton.booleanBuilder(OptionsList.OPTION_ON, OptionsList.OPTION_OFF).create(10, saveButton.getY(), 85, 20, Component.translatable("gui.jade.preview"), (button, value) -> {
				Jade.CONFIG.get().getGeneral().previewOverlay = value;
				saver.run();
			});
			previewButton.setValue(Jade.CONFIG.get().getGeneral().previewOverlay);
			addRenderableWidget(previewButton);
		}
	}

	public boolean forcePreviewOverlay() {
		Objects.requireNonNull(minecraft);
		if (!isDragging() || options == null)
			return false;
		OptionsList.Entry entry = options.getSelected();
		if (entry == null || entry.getFirstWidget() == null)
			return false;
		return options.forcePreview.contains(entry);
	}
}
