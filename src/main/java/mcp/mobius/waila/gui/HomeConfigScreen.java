package mcp.mobius.waila.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.impl.config.PluginConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.jade.Jade;

public class HomeConfigScreen extends Screen {

	private final Screen parent;

	public HomeConfigScreen(Screen parent) {
		super(new TranslationTextComponent("gui.waila.configuration"));

		this.parent = parent;
	}

	@Override
	protected void init() {
		addButton(new Button(width / 2 - 105, height / 2 - 10, 100, 20, new TranslationTextComponent("gui.waila.waila_settings", Jade.NAME), w -> {
			minecraft.displayGuiScreen(new WailaConfigScreen(HomeConfigScreen.this));
		}));
		addButton(new Button(width / 2 + 5, height / 2 - 10, 100, 20, new TranslationTextComponent("gui.waila.plugin_settings"), w -> {
			minecraft.displayGuiScreen(new PluginsConfigScreen(HomeConfigScreen.this));
		}));
		addButton(new Button(width / 2 - 50, height / 2 + 20, 100, 20, new TranslationTextComponent("gui.done"), w -> {
			Waila.CONFIG.save();
			PluginConfig.INSTANCE.save();
			minecraft.displayGuiScreen(parent);
		}));
	}

	@Override
	public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
		renderBackground(matrixStack);
		drawCenteredString(matrixStack, font, title.getString(), width / 2, height / 3, 16777215);
		super.render(matrixStack, x, y, partialTicks);
	}
}
