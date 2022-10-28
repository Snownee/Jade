package snownee.jade.gui;

import java.util.Locale;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.IConfigGeneral;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.gui.config.WailaOptionsList;
import snownee.jade.gui.config.WailaOptionsList.Entry;
import snownee.jade.util.PlatformProxy;

public class WailaConfigScreen extends BaseOptionsScreen {

	public WailaConfigScreen(Screen parent) {
		super(parent, Component.translatable("gui.jade.configuration", Jade.NAME), Jade.CONFIG::save, Jade.CONFIG::invalidate);
	}

	@Override
	public WailaOptionsList getOptions() {
		WailaOptionsList options = new WailaOptionsList(this, minecraft, width, height, 32, height - 32, 30, Jade.CONFIG::save);

		IConfigGeneral general = Jade.CONFIG.get().getGeneral();
		options.title("general");
		options.choices("display_tooltip", general.shouldDisplayTooltip(), general::setDisplayTooltip);
		options.choices("display_entities", general.getDisplayEntities(), general::setDisplayEntities);
		options.choices("display_blocks", general.getDisplayBlocks(), general::setDisplayBlocks);
		options.choices("display_fluids", general.getDisplayFluids(), general::setDisplayFluids);
		options.choices("display_mode", general.getDisplayMode(), general::setDisplayMode, builder -> {
			builder.withTooltip(mode -> {
				String key = "display_mode_" + mode.name().toLowerCase(Locale.ENGLISH) + "_desc";
				if (mode == IWailaConfig.DisplayMode.LITE && "fabric".equals(PlatformProxy.getPlatformIdentifier()))
					key += ".fabric";
				return minecraft.font.split(Entry.makeTitle(key), 200);
			});
		});
		options.choices("item_mod_name", general.showItemModNameTooltip(), general::setItemModNameTooltip);
		options.choices("hide_from_debug", general.shouldHideFromDebug(), general::setHideFromDebug);
		options.choices("hide_from_tab_list", general.shouldHideFromTabList(), general::setHideFromTabList);
		options.choices("boss_bar_overlap", general.getBossBarOverlapMode(), general::setBossBarOverlapMode);
		options.slider("reach_distance", general.getReachDistance(), general::setReachDistance, 0, 20, FloatUnaryOperator.identity());

		IConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		options.title("overlay");
		options.slider("overlay_alpha", overlay.getAlpha(), overlay::setAlpha);
		options.choices("overlay_theme", overlay.getTheme().id, overlay.getThemes().stream().map($ -> $.id).collect(Collectors.toList()), overlay::applyTheme);
		options.choices("overlay_square", overlay.getSquare(), overlay::setSquare);
		options.slider("overlay_scale", overlay.getOverlayScale(), overlay::setOverlayScale, 0.2f, 2, FloatUnaryOperator.identity());
		options.slider("overlay_pos_x", overlay.getOverlayPosX(), overlay::setOverlayPosX);
		options.slider("overlay_pos_y", overlay.getOverlayPosY(), overlay::setOverlayPosY);
		options.slider("overlay_anchor_x", overlay.getAnchorX(), overlay::setAnchorX);
		options.slider("overlay_anchor_y", overlay.getAnchorY(), overlay::setAnchorY);
		options.choices("display_item", overlay.getIconMode(), overlay::setIconMode);

		//		IConfigFormatting formatting = JadeClient.CONFIG.get().getFormatting();
		//		options.title("formatting");
		//		options.input("format_mod_name", formatting.getModName(), val -> formatting.setModName(val.isEmpty() || !val.contains("%s") ? formatting.getModName() : val));
		//		options.input("format_title_name", formatting.getTitleName(), val -> formatting.setTitleName(val.isEmpty() || !val.contains("%s") ? formatting.getTitleName() : val));
		//		options.input("format_registry_name", formatting.getRegistryName(), val -> formatting.setRegistryName(val.isEmpty() || !val.contains("%s") ? formatting.getRegistryName() : val));

		options.title("accessibility");
		options.choices("flip_main_hand", overlay.getFlipMainHand(), overlay::setFlipMainHand);
		options.choices("tts_mode", general.getTTSMode(), general::setTTSMode);

		return options;
	}
}
