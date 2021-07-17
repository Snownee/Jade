package mcp.mobius.waila.gui;

import java.util.stream.Collectors;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.HUDTheme;
import mcp.mobius.waila.api.config.WailaConfig.ConfigFormatting;
import mcp.mobius.waila.api.config.WailaConfig.ConfigGeneral;
import mcp.mobius.waila.api.config.WailaConfig.ConfigOverlay;
import mcp.mobius.waila.api.config.WailaConfig.ConfigOverlay.ConfigOverlayColor;
import mcp.mobius.waila.gui.config.OptionButton;
import mcp.mobius.waila.gui.config.OptionsListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.jade.Jade;

public class WailaConfigScreen extends OptionsScreen {

	public WailaConfigScreen(Screen parent) {
		super(parent, new TranslationTextComponent("gui.waila.configuration", Jade.NAME), Waila.CONFIG::save, Waila.CONFIG::invalidate);
	}

	@Override
	public OptionsListWidget getOptions() {
		ConfigGeneral general = Waila.CONFIG.get().getGeneral();
		ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
		ConfigOverlayColor color = overlay.getColor();
		ConfigFormatting formatting = Waila.CONFIG.get().getFormatting();
		OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30, Waila.CONFIG::save);
		options.add(new OptionButton("general", new Button(0, 0, 100, 20, StringTextComponent.EMPTY, w -> {
			minecraft.displayGuiScreen(new OptionsScreen(WailaConfigScreen.this, "general") {
				@Override
				public OptionsListWidget getOptions() {
					OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
					options.choices("display_tooltip", general.shouldDisplayTooltip(), general::setDisplayTooltip);
					options.choices("display_entities", general.getDisplayEntities(), general::setDisplayEntities);
					options.choices("display_blocks", general.getDisplayBlocks(), general::setDisplayBlocks);
					options.choices("display_fluids", general.getDisplayFluids(), general::setDisplayFluids);
					options.choices("display_mode", general.getDisplayMode(), general::setDisplayMode);
					options.choices("hide_from_debug", general.shouldHideFromDebug(), general::setHideFromDebug);
					options.choices("display_item", general.shouldShowIcon(), general::setShowIcon);
					options.slider("reach_distance", general.getReachDistance(), general::setReachDistance, 0, 20);
					options.choices("tts", general.shouldEnableTextToSpeech(), general::setEnableTextToSpeech);
					return options;
				}
			});
		})));
		options.add(new OptionButton("overlay", new Button(0, 0, 100, 20, StringTextComponent.EMPTY, w -> {
			minecraft.displayGuiScreen(new OptionsScreen(WailaConfigScreen.this, "overlay") {
				@Override
				public OptionsListWidget getOptions() {
					OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
					options.slider("overlay_alpha", color.getAlpha(), color::setAlpha);
					options.choices("overlay_theme", color.getTheme().getId(), color.getThemes().stream().map(HUDTheme::getId).collect(Collectors.toList()), color::applyTheme);
					options.choices("overlay_square", overlay.getSquare(), overlay::setSquare);
					options.slider("overlay_scale", overlay.getOverlayScale(), overlay::setOverlayScale, 0.2f, 2);
					options.slider("overlay_pos_x", overlay.getOverlayPosX(), overlay::setOverlayPosX);
					options.slider("overlay_pos_y", overlay.getOverlayPosY(), overlay::setOverlayPosY);
					options.slider("overlay_anchor_x", overlay.getAnchorX(), overlay::setAnchorX);
					options.slider("overlay_anchor_y", overlay.getAnchorY(), overlay::setAnchorY);
					options.choices("flip_main_hand", overlay.getFlipMainHand(), overlay::setFlipMainHand);
					return options;
				}
			});
		})));
		options.add(new OptionButton("formatting", new Button(0, 0, 100, 20, StringTextComponent.EMPTY, w -> {
			minecraft.displayGuiScreen(new OptionsScreen(WailaConfigScreen.this, "formatting") {
				@Override
				public OptionsListWidget getOptions() {
					OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
					options.input("format_mod_name", formatting.getModName(), val -> formatting.setModName(val.isEmpty() || !val.contains("%s") ? formatting.getModName() : val));
					options.input("format_block_name", formatting.getBlockName(), val -> formatting.setBlockName(val.isEmpty() || !val.contains("%s") ? formatting.getBlockName() : val));
					options.input("format_fluid_name", formatting.getFluidName(), val -> formatting.setFluidName(val.isEmpty() || !val.contains("%s") ? formatting.getFluidName() : val));
					options.input("format_entity_name", formatting.getEntityName(), val -> formatting.setEntityName(val.isEmpty() || !val.contains("%s") ? formatting.getEntityName() : val));
					options.input("format_registry_name", formatting.getRegistryName(), val -> formatting.setRegistryName(val.isEmpty() || !val.contains("%s") ? formatting.getRegistryName() : val));
					return options;
				}
			});
		})));
		return options;
	}
}
