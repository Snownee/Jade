package mcp.mobius.waila.gui;

import org.apache.commons.lang3.StringEscapeUtils;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import mcp.mobius.waila.api.impl.config.WailaConfig.ConfigFormatting;
import mcp.mobius.waila.api.impl.config.WailaConfig.ConfigGeneral;
import mcp.mobius.waila.api.impl.config.WailaConfig.ConfigOverlay;
import mcp.mobius.waila.gui.config.OptionsEntryButton;
import mcp.mobius.waila.gui.config.OptionsListWidget;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueBoolean;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueCycle;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueEnum;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueInput;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueSlider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiConfigWaila extends GuiOptions {

	public GuiConfigWaila(Screen parent) {
		super(parent, new TranslationTextComponent("gui.waila.configuration", Waila.NAME), Waila.CONFIG::save, Waila.CONFIG::invalidate);
	}

	@Override
	public OptionsListWidget getOptions() {
		ConfigGeneral general = Waila.CONFIG.get().getGeneral();
		ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
		ConfigFormatting formatting = Waila.CONFIG.get().getFormatting();
		OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30, Waila.CONFIG::save);
		options.add(new OptionsEntryButton("general", new Button(0, 0, 100, 20, StringTextComponent.EMPTY, w -> {
			minecraft.displayGuiScreen(new GuiOptions(GuiConfigWaila.this, "general") {
				@Override
				public OptionsListWidget getOptions() {
					OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
					options.add(new OptionsEntryValueBoolean("display_tooltip", general.shouldDisplayTooltip(), general::setDisplayTooltip));
					options.add(new OptionsEntryValueEnum<>("display_fluids", FluidMode.values(), general.getDisplayFluids(), general::setDisplayFluids));
					options.add(new OptionsEntryValueBoolean("sneaky_details", general.shouldShiftForDetails(), general::setShiftForDetails));
					options.add(new OptionsEntryValueEnum<>("display_mode", WailaConfig.DisplayMode.values(), general.getDisplayMode(), general::setDisplayMode));
					options.add(new OptionsEntryValueBoolean("hide_from_debug", general.shouldHideFromDebug(), general::setHideFromDebug));
					options.add(new OptionsEntryValueEnum<>("display_item", WailaConfig.IconMode.values(), general.getIconMode(), general::setIconMode));
					options.add(new OptionsEntryValueSlider("reach_distance", general.getReachDistance(), general::setReachDistance, 0, 20));
					options.add(new OptionsEntryValueBoolean("tts", general.shouldEnableTextToSpeech(), general::setEnableTextToSpeech));
					return options;
				}
			});
		})));
		options.add(new OptionsEntryButton("overlay", new Button(0, 0, 100, 20, StringTextComponent.EMPTY, w -> {
			minecraft.displayGuiScreen(new GuiOptions(GuiConfigWaila.this, "overlay") {
				@Override
				public OptionsListWidget getOptions() {
					OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
					options.add(new OptionsEntryValueSlider("overlay_alpha", overlay.getColor().getRawAlpha(), val -> overlay.getColor().setAlpha((int) (float) val), 0, 100));
					options.add(new OptionsEntryValueCycle("overlay_theme", overlay.getColor().getThemes().stream().map(t -> t.getId().toString()).sorted(String::compareToIgnoreCase).toArray(String[]::new), overlay.getColor().getTheme().getId().toString(), val -> overlay.getColor().applyTheme(new ResourceLocation(val))));
					options.add(new OptionsEntryValueBoolean("overlay_square", overlay.getSquare(), overlay::setSquare));
					options.add(new OptionsEntryValueSlider("overlay_pos_x", overlay.getOverlayPosX(), overlay::setOverlayPosX));
					options.add(new OptionsEntryValueSlider("overlay_pos_y", overlay.getOverlayPosY(), overlay::setOverlayPosY));
					options.add(new OptionsEntryValueSlider("overlay_anchor_x", overlay.getAnchorX(), overlay::setAnchorX));
					options.add(new OptionsEntryValueSlider("overlay_anchor_y", overlay.getAnchorY(), overlay::setAnchorY));
					options.add(new OptionsEntryValueBoolean("flip_main_hand", overlay.getFlipMainHand(), overlay::setFlipMainHand));
					return options;
				}
			});
		})));
		options.add(new OptionsEntryButton("formatting", new Button(0, 0, 100, 20, StringTextComponent.EMPTY, w -> {
			minecraft.displayGuiScreen(new GuiOptions(GuiConfigWaila.this, "formatting") {
				@Override
				public OptionsListWidget getOptions() {
					OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
					options.add(new OptionsEntryValueInput<>("format_mod_name", StringEscapeUtils.escapeJava(formatting.getModName()), val -> formatting.setModName(val.isEmpty() || !val.contains("%s") ? formatting.getModName() : val)));
					options.add(new OptionsEntryValueInput<>("format_block_name", StringEscapeUtils.escapeJava(formatting.getBlockName()), val -> formatting.setBlockName(val.isEmpty() || !val.contains("%s") ? formatting.getBlockName() : val)));
					options.add(new OptionsEntryValueInput<>("format_fluid_name", StringEscapeUtils.escapeJava(formatting.getFluidName()), val -> formatting.setFluidName(val.isEmpty() || !val.contains("%s") ? formatting.getFluidName() : val)));
					options.add(new OptionsEntryValueInput<>("format_entity_name", StringEscapeUtils.escapeJava(formatting.getEntityName()), val -> formatting.setEntityName(val.isEmpty() || !val.contains("%s") ? formatting.getEntityName() : val)));
					options.add(new OptionsEntryValueInput<>("format_registry_name", StringEscapeUtils.escapeJava(formatting.getRegistryName()), val -> formatting.setRegistryName(val.isEmpty() || !val.contains("%s") ? formatting.getRegistryName() : val)));
					return options;
				}
			});
		})));
		return options;
	}
}
