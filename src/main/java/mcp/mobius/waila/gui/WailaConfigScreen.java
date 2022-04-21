package mcp.mobius.waila.gui;

import java.util.Locale;
import java.util.stream.Collectors;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.WailaConfig.ConfigFormatting;
import mcp.mobius.waila.api.config.WailaConfig.ConfigGeneral;
import mcp.mobius.waila.api.config.WailaConfig.ConfigOverlay;
import mcp.mobius.waila.api.config.WailaConfig.ConfigOverlay.ConfigOverlayColor;
import mcp.mobius.waila.gui.config.WailaOptionsList;
import mcp.mobius.waila.gui.config.WailaOptionsList.Entry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import snownee.jade.Jade;

public class WailaConfigScreen extends OptionsScreen {

	public WailaConfigScreen(Screen parent) {
		super(parent, new TranslatableComponent("gui.waila.configuration", Jade.NAME), Waila.CONFIG::save, Waila.CONFIG::invalidate);
	}

	@Override
	public WailaOptionsList getOptions() {
		WailaOptionsList options = new WailaOptionsList(this, minecraft, width, height, 32, height - 32, 30, Waila.CONFIG::save);

		ConfigGeneral general = Waila.CONFIG.get().getGeneral();
		options.title("general");
		options.choices("display_tooltip", general.shouldDisplayTooltip(), general::setDisplayTooltip);
		options.choices("display_entities", general.getDisplayEntities(), general::setDisplayEntities);
		options.choices("display_blocks", general.getDisplayBlocks(), general::setDisplayBlocks);
		options.choices("display_fluids", general.getDisplayFluids(), general::setDisplayFluids);
		options.choices("display_mode", general.getDisplayMode(), general::setDisplayMode, builder -> {
			builder.withTooltip(mode -> minecraft.font.split(Entry.makeTitle("display_mode_" + mode.name().toLowerCase(Locale.ENGLISH) + "_desc"), 200));
		});
		options.choices("hide_from_debug", general.shouldHideFromDebug(), general::setHideFromDebug);
		options.choices("display_item", general.getIconMode(), general::setIconMode);
		options.slider("reach_distance", general.getReachDistance(), general::setReachDistance, 0, 20);
		options.choices("tts_mode", general.getTTSMode(), general::setTTSMode);

		ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
		ConfigOverlayColor color = overlay.getColor();
		options.title("overlay");
		options.slider("overlay_alpha", color.getAlpha(), color::setAlpha);
		options.choices("overlay_theme", color.getTheme().id, color.getThemes().stream().map($ -> $.id).collect(Collectors.toList()), color::applyTheme);
		options.choices("overlay_square", overlay.getSquare(), overlay::setSquare);
		options.slider("overlay_scale", overlay.getOverlayScale(), overlay::setOverlayScale, 0.2f, 2);
		options.slider("overlay_pos_x", overlay.getOverlayPosX(), overlay::setOverlayPosX);
		options.slider("overlay_pos_y", overlay.getOverlayPosY(), overlay::setOverlayPosY);
		options.slider("overlay_anchor_x", overlay.getAnchorX(), overlay::setAnchorX);
		options.slider("overlay_anchor_y", overlay.getAnchorY(), overlay::setAnchorY);
		options.choices("flip_main_hand", overlay.getFlipMainHand(), overlay::setFlipMainHand);

		ConfigFormatting formatting = Waila.CONFIG.get().getFormatting();
		options.title("formatting");
		options.input("format_mod_name", formatting.getModName(), val -> formatting.setModName(val.isEmpty() || !val.contains("%s") ? formatting.getModName() : val));
		options.input("format_block_name", formatting.getBlockName(), val -> formatting.setBlockName(val.isEmpty() || !val.contains("%s") ? formatting.getBlockName() : val));
		options.input("format_entity_name", formatting.getEntityName(), val -> formatting.setEntityName(val.isEmpty() || !val.contains("%s") ? formatting.getEntityName() : val));
		options.input("format_registry_name", formatting.getRegistryName(), val -> formatting.setRegistryName(val.isEmpty() || !val.contains("%s") ? formatting.getRegistryName() : val));

		return options;
	}
}
