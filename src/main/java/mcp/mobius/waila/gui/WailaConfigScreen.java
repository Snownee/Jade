package mcp.mobius.waila.gui;

import org.apache.commons.lang3.StringEscapeUtils;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.gui.config.OptionsEntryButton;
import mcp.mobius.waila.gui.config.OptionsListWidget;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueBoolean;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueCycle;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueEnum;
import mcp.mobius.waila.gui.config.value.OptionsEntryValueInput;
import mcp.mobius.waila.impl.config.WailaConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class WailaConfigScreen extends OptionsScreen {

    public WailaConfigScreen(Screen parent) {
        super(parent, new TranslationTextComponent("gui.waila.configuration", Waila.NAME), Waila.CONFIG::save, Waila.CONFIG::invalidate);
    }

    @Override
    public OptionsListWidget getOptions() {
        OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30, Waila.CONFIG::save);
        options.add(new OptionsEntryButton(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "general")), new Button(0, 0, 100, 20, new StringTextComponent(""), w -> {
            minecraft.displayGuiScreen(new OptionsScreen(WailaConfigScreen.this, new TranslationTextComponent(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "general")))) {
                @Override
                public OptionsListWidget getOptions() {
                    OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
                    options.add(new OptionsEntryValueBoolean(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "display_tooltip")), Waila.CONFIG.get().getGeneral().shouldDisplayTooltip(), val ->
                            Waila.CONFIG.get().getGeneral().setDisplayTooltip(val)
                    ));
                    options.add(new OptionsEntryValueEnum<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "display_fluids")), FluidMode.values(), Waila.CONFIG.get().getGeneral().getDisplayFluids(), val ->
                            Waila.CONFIG.get().getGeneral().setDisplayFluids(val)
                    ));
                    options.add(new OptionsEntryValueBoolean(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "sneaky_details")), Waila.CONFIG.get().getGeneral().shouldShiftForDetails(), val ->
                            Waila.CONFIG.get().getGeneral().setShiftForDetails(val)
                    ));
                    options.add(new OptionsEntryValueEnum<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "display_mode")), WailaConfig.DisplayMode.values(), Waila.CONFIG.get().getGeneral().getDisplayMode(), val ->
                            Waila.CONFIG.get().getGeneral().setDisplayMode(val)
                    ));
                    options.add(new OptionsEntryValueBoolean(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "hide_from_debug")), Waila.CONFIG.get().getGeneral().shouldHideFromDebug(), val ->
                            Waila.CONFIG.get().getGeneral().setHideFromDebug(val)
                    ));
                    options.add(new OptionsEntryValueBoolean(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "display_item")), Waila.CONFIG.get().getGeneral().shouldShowIcon(), val ->
                            Waila.CONFIG.get().getGeneral().setShowIcon(val)
                    ));
                    options.add(new OptionsEntryValueBoolean(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "tts")), Waila.CONFIG.get().getGeneral().shouldEnableTextToSpeech(), val ->
                            Waila.CONFIG.get().getGeneral().setEnableTextToSpeech(val)
                    ));
                    return options;
                }
            });
        })));
        options.add(new OptionsEntryButton(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay")), new Button(0, 0, 100, 20, new StringTextComponent(""), w -> {
            minecraft.displayGuiScreen(new OptionsScreen(WailaConfigScreen.this, new TranslationTextComponent(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay")))) {
                @Override
                public OptionsListWidget getOptions() {
                    OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_pos_x")), Waila.CONFIG.get().getOverlay().getOverlayPosX(), val ->
                            Waila.CONFIG.get().getOverlay().setOverlayPosX(val)
                            , OptionsEntryValueInput.FLOAT));
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_pos_y")), Waila.CONFIG.get().getOverlay().getOverlayPosY(), val ->
                            Waila.CONFIG.get().getOverlay().setOverlayPosY(val)
                            , OptionsEntryValueInput.FLOAT));
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_scale")), Waila.CONFIG.get().getOverlay().getOverlayScale(), val ->
                            Waila.CONFIG.get().getOverlay().setOverlayScale(val)
                            , OptionsEntryValueInput.FLOAT));
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_anchor_x")), Waila.CONFIG.get().getOverlay().getAnchorX(), val ->
                            Waila.CONFIG.get().getOverlay().setAnchorX(val)
                            , OptionsEntryValueInput.FLOAT));
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_anchor_y")), Waila.CONFIG.get().getOverlay().getAnchorY(), val ->
                            Waila.CONFIG.get().getOverlay().setAnchorY(val)
                            , OptionsEntryValueInput.FLOAT));
                    options.add(new OptionsEntryValueBoolean(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_square")), Waila.CONFIG.get().getOverlay().getSquare(), val ->
                            Waila.CONFIG.get().getOverlay().setSquare(val)));
                    options.add(new OptionsEntryValueBoolean(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "flip_main_hand")), Waila.CONFIG.get().getOverlay().getFlipMainHand(), val ->
                            Waila.CONFIG.get().getOverlay().setFlipMainHand(val)));
                    options.add(new OptionsEntryButton(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_color")), new Button(0, 0, 100, 20, new StringTextComponent(""), w -> {
                        minecraft.displayGuiScreen(new OptionsScreen(WailaConfigScreen.this, new TranslationTextComponent(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_color")))) {
                            @Override
                            public OptionsListWidget getOptions() {
                                OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
                                options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_alpha")), Waila.CONFIG.get().getOverlay().getColor().getRawAlpha(), val ->
                                        Waila.CONFIG.get().getOverlay().getColor().setAlpha(Math.min(100, Math.max(0, val)))
                                        , OptionsEntryValueInput.INTEGER));
                                options.add(new OptionsEntryValueCycle(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay_theme")),
                                        Waila.CONFIG.get().getOverlay().getColor().getThemes().stream().map(t -> t.getId().toString()).sorted(String::compareToIgnoreCase).toArray(String[]::new),
                                        Waila.CONFIG.get().getOverlay().getColor().getTheme().getId().toString(),
                                        val ->
                                                Waila.CONFIG.get().getOverlay().getColor().applyTheme(new ResourceLocation(val))
                                ));
                                return options;
                            }
                        });
                    })));
                    return options;
                }
            });
        })));
        options.add(new OptionsEntryButton(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "formatting")), new Button(0, 0, 100, 20, new StringTextComponent(""), w -> {
            minecraft.displayGuiScreen(new OptionsScreen(WailaConfigScreen.this, new TranslationTextComponent(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "overlay")))) {
                @Override
                public OptionsListWidget getOptions() {
                    OptionsListWidget options = new OptionsListWidget(this, minecraft, width + 45, height, 32, height - 32, 30);
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "format_mod_name")), StringEscapeUtils.escapeJava(Waila.CONFIG.get().getFormatting().getModName()), val ->
                            Waila.CONFIG.get().getFormatting().setModName(val.isEmpty() || !val.contains("%s") ? Waila.CONFIG.get().getFormatting().getModName() : val)
                    ));
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "format_block_name")), StringEscapeUtils.escapeJava(Waila.CONFIG.get().getFormatting().getBlockName()), val ->
                            Waila.CONFIG.get().getFormatting().setBlockName(val.isEmpty() || !val.contains("%s") ? Waila.CONFIG.get().getFormatting().getBlockName() : val)
                    ));
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "format_fluid_name")), StringEscapeUtils.escapeJava(Waila.CONFIG.get().getFormatting().getFluidName()), val ->
                            Waila.CONFIG.get().getFormatting().setFluidName(val.isEmpty() || !val.contains("%s") ? Waila.CONFIG.get().getFormatting().getFluidName() : val)
                    ));
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "format_entity_name")), StringEscapeUtils.escapeJava(Waila.CONFIG.get().getFormatting().getEntityName()), val ->
                            Waila.CONFIG.get().getFormatting().setEntityName(val.isEmpty() || !val.contains("%s") ? Waila.CONFIG.get().getFormatting().getEntityName() : val)
                    ));
                    options.add(new OptionsEntryValueInput<>(Util.makeTranslationKey("config", new ResourceLocation(Waila.MODID, "format_registry_name")), StringEscapeUtils.escapeJava(Waila.CONFIG.get().getFormatting().getRegistryName()), val ->
                            Waila.CONFIG.get().getFormatting().setRegistryName(val.isEmpty() || !val.contains("%s") ? Waila.CONFIG.get().getFormatting().getRegistryName() : val)
                    ));
                    return options;
                }
            });
        })));
        return options;
    }
}
