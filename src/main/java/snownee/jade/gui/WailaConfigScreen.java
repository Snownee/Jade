package snownee.jade.gui;

import java.util.Locale;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.config.OptionButton;
import snownee.jade.gui.config.WailaOptionsList;
import snownee.jade.gui.config.WailaOptionsList.Entry;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.impl.config.WailaConfig.ConfigOverlay;
import snownee.jade.util.PlatformProxy;

public class WailaConfigScreen extends BaseOptionsScreen {

	public WailaConfigScreen(Screen parent) {
		super(parent, Component.translatable("gui.jade.configuration", Jade.NAME), Jade.CONFIG::save, Jade.CONFIG::invalidate);
	}

	@Override
	public WailaOptionsList createOptions() {
		WailaOptionsList options = new WailaOptionsList(this, minecraft, width, height, 32, height - 32, 30, Jade.CONFIG::save);

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (PlatformProxy.isDevEnv())
			options.choices("debug_mode", general.isDebug(), general::setDebug);
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
		OptionValue<?> value = options.choices("item_mod_name", general.showItemModNameTooltip(), general::setItemModNameTooltip);
		if (!general.itemModNameTooltipDisabledByMods.isEmpty()) {
			value.setDisabled(true);
			value.appendDescription(I18n.get("gui.jade.disabled_by_mods"));
			general.itemModNameTooltipDisabledByMods.forEach(value::appendDescription);
		}
		options.choices("hide_from_debug", general.shouldHideFromDebug(), general::setHideFromDebug);
		options.choices("hide_from_tab_list", general.shouldHideFromTabList(), general::setHideFromTabList);
		options.choices("boss_bar_overlap", general.getBossBarOverlapMode(), general::setBossBarOverlapMode);
		options.slider("reach_distance", general.getReachDistance(), general::setReachDistance, 0, 20, FloatUnaryOperator.identity());

		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
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
		options.choices("animation", overlay.getAnimation(), overlay::setAnimation);

		//		IConfigFormatting formatting = JadeClient.CONFIG.get().getFormatting();
		//		options.title("formatting");
		//		options.input("format_mod_name", formatting.getModName(), val -> formatting.setModName(val.isEmpty() || !val.contains("%s") ? formatting.getModName() : val));
		//		options.input("format_title_name", formatting.getTitleName(), val -> formatting.setTitleName(val.isEmpty() || !val.contains("%s") ? formatting.getTitleName() : val));
		//		options.input("format_registry_name", formatting.getRegistryName(), val -> formatting.setRegistryName(val.isEmpty() || !val.contains("%s") ? formatting.getRegistryName() : val));

		options.title("accessibility");
		options.choices("flip_main_hand", overlay.getFlipMainHand(), overlay::setFlipMainHand);
		options.choices("tts_mode", general.getTTSMode(), general::setTTSMode);

		options.title("danger_zone").withStyle(ChatFormatting.RED);
		Component reset = Component.translatable("controls.reset").withStyle(ChatFormatting.RED);
		Component title = Component.translatable(WailaOptionsList.Entry.makeKey("reset_settings")).withStyle(ChatFormatting.RED);
		options.add(new OptionButton(title, new Button(0, 0, 100, 20, reset, w -> {
			minecraft.setScreen(new ConfirmScreen(bl -> {
				if (bl) {
					try {
						Jade.CONFIG.getFile().delete();
						PluginConfig.INSTANCE.getFile().delete();
						Jade.CONFIG.invalidate();
						PluginConfig.INSTANCE.reload();
						rebuildWidgets();
					} catch (Throwable e) {
						Jade.LOGGER.catching(e);
					}
				}
				minecraft.setScreen(this);
				this.options.setScrollAmount(this.options.getMaxScroll());
			}, title, Component.translatable(WailaOptionsList.Entry.makeKey("reset_settings.confirm")), reset, Component.translatable("gui.cancel")));
		})));

		return options;
	}
}
