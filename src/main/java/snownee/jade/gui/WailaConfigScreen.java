package snownee.jade.gui;

import java.io.File;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.JadeKeys;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.gui.config.OptionButton;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.CycleOptionValue;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.theme.ThemeHelper;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.ModIdentification;

public class WailaConfigScreen extends PreviewOptionsScreen {

	private @Nullable CycleOptionValue<Identifier> styleEntry;
	private @Nullable OptionValue<Float> opacityEntry;

	public WailaConfigScreen(@Nullable Screen parent) {
		super(parent, Component.translatable("gui.jade.jade_settings"));
		saver = () -> {
			IWailaConfig.get().save();
			JadeClient.refreshKeyState();
			KeyMapping.resetMapping();
			Minecraft.getInstance().options.save();
		};
		Runnable runnable = JadeClient.recoverKeysAction($ -> JadeKeys.openConfig().getCategory().equals($.getCategory()));
		canceller = () -> {
			IWailaConfig.get().invalidate();
			runnable.run();
		};
	}

	@SuppressWarnings("UnusedReturnValue")
	public static OptionsList.Entry editIgnoreList(OptionsList.Entry entry, String fileName, Runnable defaultFactory) {
		Objects.requireNonNull(entry.mainWidget()).setWidth(79);
		MutableComponent tooltip = Component.translatable("config.jade.edit_ignore_list");
		entry.addWidget(
				Button.builder(
						Component.literal("☰"), b -> {
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
	public OptionsList createOptions(OptionsList options) {
		IWailaConfig.General general = IWailaConfig.get().general();
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
		options.choices("display_fluids", general::getDisplayFluids, general::setDisplayFluids).parent(entry);
		options.choices("display_mode", general::getDisplayMode, general::setDisplayMode);
		OptionValue<?> value = options.choices("item_mod_name", general::showItemModNameTooltip, general::setItemModNameTooltip);
		List<String> modNames = ClientProxy.metadata.disableItemModNameTooltip().stream()
				.map($ -> ModIdentification.getModFullName($).orElse($))
				.toList();
		if (!modNames.isEmpty()) {
			value.setDisabled(true);
			value.appendDescription(Component.translatable("gui.jade.disabled_by_mods"));
			modNames.stream().map(Component::literal).forEach(value::appendDescription);
			AbstractWidget mainWidget = value.mainWidget();
			if (mainWidget != null) {
				mainWidget.setTooltip(MultilineTooltip.create(value.getDescription()));
			}
		}

		options.choices("hide_from_guis", general::shouldHideFromGUIs, general::setHideFromGUIs);
		options.choices("boss_bar_overlap", general::getBossBarOverlapMode, general::setBossBarOverlapMode);
		options.slider("reach_distance", general::getExtendedReach, general::setExtendedReach, 0, 20, f -> Mth.floor(f * 2) / 2F);
		options.choices("perspective_mode", general::getPerspectiveMode, general::setPerspectiveMode);

		IWailaConfig.Overlay overlay = IWailaConfig.get().overlay();
		options.title("overlay");
		Component adjust = Component.translatable(OptionsList.Entry.makeKey("overlay_pos.adjust"));
		options.add(new OptionButton(
				Component.translatable(OptionsList.Entry.makeKey("overlay_pos")),
				Button.builder(adjust, w -> startAdjustingPosition()).size(100, 20)));
		CycleButton.ValueListSupplier<Identifier> valuesSupplier = new CycleButton.ValueListSupplier<>() {
			@Override
			public List<Identifier> getSelectedList() {
				return getDefaultList();
			}

			@Override
			public List<Identifier> getDefaultList() {
				Identifier mainId = overlay.getTheme().mainId();
				return IThemeHelper.get().getThemes().stream()
						.filter($ -> $.mainId().equals(mainId))
						.map(Theme::fullId)
						.toList();
			}
		};
		var themeEntry = options.add(new CycleOptionValue<>(
				"overlay_theme",
				CycleButton.builder(
								id -> Component.translatable(Util.makeDescriptionId("jade.theme", id)),
								overlay.getTheme().mainId())
						.withValues(IThemeHelper.get().getThemes().stream()
								.filter($ -> $.styleId().isEmpty())
								.map(Theme::fullId)
								.toList()),
				() -> overlay.getTheme().mainId(),
				id -> {
					if (Objects.equals(id, overlay.getTheme().mainId())) {
						return;
					}
					if (!ThemeHelper.INSTANCE.hasTheme(id)) {
						return;
					}
					overlay.applyTheme(id);
					Theme theme = overlay.getTheme();
					if (theme.changeOpacity != 0) {
						Objects.requireNonNull(opacityEntry).setValue(theme.changeOpacity);
					}
					Objects.requireNonNull(styleEntry).updateValue();
				}));
		styleEntry = options.add(new CycleOptionValue<>(
				"theme_style",
				CycleButton.builder(id -> Component.translatable(ThemeHelper.INSTANCE.getTheme(id).styleName), overlay.getTheme().fullId())
						.withValues(valuesSupplier),
				() -> Objects.requireNonNull(overlay.getTheme().id),
				overlay::applyTheme) {
			@Override
			public void updateValue() {
				super.updateValue();
				if (valuesSupplier.getDefaultList().size() > 1) {
					button.active = true;
				} else {
					button.active = false;
					button.setMessage(Component.translatable("jade.unavailable"));
				}
			}
		});
		styleEntry.parent(themeEntry);
		styleEntry.updateValue();
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
		options.keybind(JadeKeys.openConfig());
		options.keybind(JadeKeys.showOverlay());
		options.keybind(JadeKeys.toggleLiquid());
		if (JadeKeys.hasRecipeViewerKeys()) {
			options.keybind(JadeKeys.showRecipes());
			options.keybind(JadeKeys.showUses());
		}
		options.keybind(JadeKeys.narrate());
		options.keybind(JadeKeys.showDetails());

		IWailaConfig.Accessibility accessibility = IWailaConfig.get().accessibility();
		options.title("accessibility");
		options.choices("accessibility_plugin", accessibility::getEnableAccessibilityPlugin, accessibility::setEnableAccessibilityPlugin);
		options.choices("tts_mode", accessibility::getTTSMode, accessibility::setTTSMode);
		options.choices("narrate_keys", accessibility::getNarrateKeys, accessibility::setNarrateKeys);
		options.slider("text_background_opacity", accessibility::getTextBackgroundOpacity, accessibility::setTextBackgroundOpacity);
		options.choices("flip_main_hand", accessibility::getFlipMainHand, accessibility::setFlipMainHand);

		OptionsList.Title dangerZone = options.title("danger_zone");
		dangerZone.setTitle(dangerZone.title().copy().withStyle(ChatFormatting.RED));
		options.add(new OptionButton(
				"reload_plugins", Button.builder(
				OptionsList.Entry.makeTitle("reload_plugins.button"), w -> {
					w.active = false;
					Jade.loadPlugins();
					w.active = true;
					Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0f));
				}).size(100, 20).build()));
		Component reset = Component.translatable("controls.reset").withStyle(ChatFormatting.RED);
		Component title = Component.translatable(OptionsList.Entry.makeKey("reset_settings")).withStyle(ChatFormatting.RED);
		options.add(new OptionButton(
				title, Button.builder(
				reset, w -> {
					minecraft.gui.setScreen(new ConfirmScreen(
							bl -> {
								if (bl) {
									for (KeyMapping keyMapping : minecraft.options.keyMappings) {
										if (JadeKeys.openConfig().getCategory().equals(keyMapping.getCategory())) {
											keyMapping.setKey(keyMapping.getDefaultKey());
										}
									}
									minecraft.options.save();
									try {
										Jade.resetConfig();
										rebuildWidgets();
									} catch (Throwable e) {
										Jade.LOGGER.error("", e);
									}
								}
								minecraft.gui.setScreen(this);
								options().setScrollAmount(options().maxScrollAmount());
							},
							title,
							Component.translatable(OptionsList.Entry.makeKey("reset_settings.confirm")),
							reset,
							Component.translatable("gui.cancel")));
				}).size(100, 20)));

		return options;
	}

}
