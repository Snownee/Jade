package snownee.jade.gui;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.InputConstants;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.gui.config.OptionButton;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.impl.config.WailaConfig.ConfigOverlay;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;

public class WailaConfigScreen extends PreviewOptionsScreen {

	private OptionValue<Boolean> squareEntry;
	private OptionValue<Float> opacityEntry;

	public WailaConfigScreen(Screen parent) {
		super(parent, Component.translatable("gui.jade.jade_settings"));
		saver = () -> {
			Jade.CONFIG.save();
			KeyMapping.resetMapping();
			Minecraft.getInstance().options.save();
		};
		ImmutableMap.Builder<KeyMapping, InputConstants.Key> keyMapBuilder = ImmutableMap.builder();
		for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
			if (JadeClient.openConfig.getCategory().equals(keyMapping.getCategory())) {
				keyMapBuilder.put(keyMapping, ClientProxy.getBoundKeyOf(keyMapping));
			}
		}
		var keyMap = keyMapBuilder.build();
		canceller = () -> {
			Jade.CONFIG.invalidate();
			keyMap.forEach(KeyMapping::setKey);
			Minecraft.getInstance().options.save();
		};
	}

	public static OptionsList.Entry editIgnoreList(OptionsList.Entry entry, String fileName, Runnable defaultFactory) {
		entry.getFirstWidget().setWidth(79);
		MutableComponent tooltip = Component.translatable("config.jade.edit_ignore_list");
		entry.addWidget(Button.builder(Component.literal("☰"), b -> {
			new Thread(() -> {
				try {
					Thread.sleep(500);
				} catch (InterruptedException ignored) {
				}
				JadeClient.pleaseWait();
			}).start();
			File file = new File(CommonProxy.getConfigDirectory(), "jade/%s.json".formatted(fileName));
			if (!file.exists()) {
				defaultFactory.run();
			}
			Util.getPlatform().openFile(file);
		}).size(20, 20).tooltip(Tooltip.create(tooltip)).createNarration($ -> tooltip).build(), 80);
		return entry;
	}

	@Override
	public OptionsList createOptions() {
		Objects.requireNonNull(minecraft);
		OptionsList options = new OptionsList(this, minecraft, width - 120, height - 32, 0, 26, Jade.CONFIG::save);

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		options.title("general");
		if (CommonProxy.isDevEnv()) {
			options.choices("debug_mode", general::isDebug, general::setDebug);
		}
		options.choices("display_tooltip", general::shouldDisplayTooltip, general::setDisplayTooltip);
		OptionsList.Entry entry = options.choices("display_entities", general::getDisplayEntities, general::setDisplayEntities);
		editIgnoreList(entry, "hide-entities", () -> WailaClientRegistration.instance().reloadIgnoreLists());
		options.choices("display_bosses", general::getDisplayBosses, general::setDisplayBosses).parent(entry);
		entry = options.choices("display_blocks", general::getDisplayBlocks, general::setDisplayBlocks);
		editIgnoreList(entry, "hide-blocks", () -> WailaClientRegistration.instance().reloadIgnoreLists());
		options.choices("display_fluids", general::getDisplayFluids, general::setDisplayFluids, builder -> {
			builder.withTooltip(mode -> {
				String key = "display_fluids_" + mode.name().toLowerCase(Locale.ENGLISH) + "_desc";
				key = OptionsList.Entry.makeKey(key);
				return I18n.exists(key) ? Tooltip.create(Component.translatable(key)) : null;
			});
		}).parent(entry);
		options.choices("display_mode", general::getDisplayMode, general::setDisplayMode, builder -> {
			builder.withTooltip(mode -> {
				String key = "display_mode_" + mode.name().toLowerCase(Locale.ENGLISH) + "_desc";
				return Tooltip.create(processBuiltInVariables(OptionsList.Entry.makeTitle(key)));
			});
		});
		OptionValue<?> value = options.choices("item_mod_name", general::showItemModNameTooltip, general::setItemModNameTooltip);
		if (!ConfigGeneral.itemModNameTooltipDisabledByMods.isEmpty()) {
			value.setDisabled(true);
			value.appendDescription(Component.translatable("gui.jade.disabled_by_mods"));
			ConfigGeneral.itemModNameTooltipDisabledByMods.stream().map(Component::literal).forEach(value::appendDescription);
			if (value.getFirstWidget() != null && value.getDescription() != null) {
				value.getFirstWidget().setTooltip(MultilineTooltip.create(value.getDescription()));
			}
		}
		options.choices("hide_from_guis", general::shouldHideFromGUIs, general::setHideFromGUIs);
		options.choices("boss_bar_overlap", general::getBossBarOverlapMode, general::setBossBarOverlapMode);
		options.slider("reach_distance", general::getExtendedReach, general::setExtendedReach, 0, 20, f -> Mth.floor(f * 2) / 2F);
		options.choices("perspective_mode", general::getPerspectiveMode, general::setPerspectiveMode, builder -> {
			builder.withTooltip(mode -> {
				String key = "perspective_mode_" + mode.name().toLowerCase(Locale.ENGLISH) + "_desc";
				return Tooltip.create(OptionsList.Entry.makeTitle(key));
			});
		});

		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		options.title("overlay");
		Component adjust = Component.translatable(OptionsList.Entry.makeKey("overlay_pos.adjust"));
		options.add(new OptionButton(Component.translatable(OptionsList.Entry.makeKey("overlay_pos")), Button.builder(adjust, w -> {
			startAdjustingPosition();
		}).size(100, 20)));
		options.choices(
				"overlay_theme",
				() -> overlay.getTheme().id,
				IThemeHelper.get().getThemes().stream().filter($ -> !$.hidden).map($ -> $.id).toList(),
				id -> {
					if (Objects.equals(id, overlay.getTheme().id)) {
						return;
					}
					overlay.applyTheme(id);
					Theme theme = overlay.getTheme();
					if (theme.changeRoundCorner != null) {
						squareEntry.setValue(theme.changeRoundCorner);
					}
					if (theme.changeOpacity != 0) {
						opacityEntry.setValue(theme.changeOpacity);
					}
				},
				id -> Component.translatable(Util.makeDescriptionId("jade.theme", id)));
		squareEntry = options.choices("overlay_square", overlay::getSquare, overlay::setSquare);
		opacityEntry = options.slider("overlay_alpha", overlay::getAlpha, overlay::setAlpha);
		options.forcePreview.add(options.slider(
				"overlay_scale",
				overlay::getOverlayScale,
				overlay::setOverlayScale,
				0.2f,
				2,
				FloatUnaryOperator.identity()));
		options.choices("display_item", overlay::getIconMode, overlay::setIconMode);
		options.choices("animation", overlay::getAnimation, overlay::setAnimation);

		options.title("key_binds");
		options.keybind(JadeClient.openConfig);
		options.keybind(JadeClient.showOverlay);
		options.keybind(JadeClient.toggleLiquid);
		if (ClientProxy.shouldRegisterRecipeViewerKeys()) {
			options.keybind(JadeClient.showRecipes);
			options.keybind(JadeClient.showUses);
		}
		options.keybind(JadeClient.narrate);
		options.keybind(JadeClient.showDetails);

		options.title("accessibility");
		options.choices("accessibility_plugin", general::getEnableAccessibilityPlugin, general::setEnableAccessibilityPlugin);
		options.choices("tts_mode", general::getTTSMode, general::setTTSMode);
		options.choices("flip_main_hand", overlay::getFlipMainHand, overlay::setFlipMainHand);

		options.title("danger_zone").withStyle(ChatFormatting.RED);
		Component reset = Component.translatable("controls.reset").withStyle(ChatFormatting.RED);
		Component title = Component.translatable(OptionsList.Entry.makeKey("reset_settings")).withStyle(ChatFormatting.RED);
		options.add(new OptionButton(title, Button.builder(reset, w -> {
			minecraft.setScreen(new ConfirmScreen(
					bl -> {
						if (bl) {
							for (KeyMapping keyMapping : minecraft.options.keyMappings) {
								if (JadeClient.openConfig.getCategory().equals(keyMapping.getCategory())) {
									keyMapping.setKey(keyMapping.getDefaultKey());
								}
							}
							minecraft.options.save();
							try {
								int themesHash = Jade.CONFIG.get().getHistory().themesHash;
								Preconditions.checkState(Jade.CONFIG.getFile().delete());
								Preconditions.checkState(PluginConfig.INSTANCE.getFile().delete());
								Jade.CONFIG.invalidate();
								Jade.CONFIG.get().getHistory().themesHash = themesHash;
								Jade.CONFIG.save();
								PluginConfig.INSTANCE.reload();
								rebuildWidgets();
							} catch (Throwable e) {
								Jade.LOGGER.error("", e);
							}
						}
						minecraft.setScreen(this);
						this.options.setScrollAmount(this.options.getMaxScroll());
					},
					title,
					Component.translatable(OptionsList.Entry.makeKey("reset_settings.confirm")),
					reset,
					Component.translatable("gui.cancel")));
		}).size(100, 20)));

		return options;
	}

}
