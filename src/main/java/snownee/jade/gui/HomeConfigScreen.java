package snownee.jade.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.impl.config.PluginConfig;

public class HomeConfigScreen extends Screen {

	private final Screen parent;

	public HomeConfigScreen(Screen parent) {
		super(Component.translatable("gui.jade.configuration"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		addRenderableWidget(new Button(width / 2 - 105, height / 2 - 10, 100, 20, Component.translatable("gui.jade.jade_settings", Jade.NAME), w -> {
			minecraft.setScreen(new WailaConfigScreen(HomeConfigScreen.this));
		}));
		addRenderableWidget(new Button(width / 2 + 5, height / 2 - 10, 100, 20, Component.translatable("gui.jade.plugin_settings"), w -> {
			minecraft.setScreen(new PluginsConfigScreen(HomeConfigScreen.this));
		}));
		addRenderableWidget(new Button(width / 2 - 50, height / 2 + 20, 100, 20, Component.translatable("gui.done"), w -> {
			Jade.CONFIG.save();
			PluginConfig.INSTANCE.save();
			minecraft.setScreen(parent);
		}));
	}

	@Override
	public void render(PoseStack matrixStack, int x, int y, float partialTicks) {
		renderBackground(matrixStack);
		drawCenteredString(matrixStack, font, title, (int) (width * .5F), height / 3, 16777215);
		super.render(matrixStack, x, y, partialTicks);
		drawCenteredString(matrixStack, font, "§b❄§r Made with §c❤§r by Snownee §b❄", (int) (width * .5F), (int) (height * .75F), 0x55FFFFFF);
	}
}
