package snownee.jade.gui;

import java.util.List;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.JadeIds;

public class ToggleButton extends CycleButton<Boolean> {

	public static final int WIDTH = 29;
	public static final int HEIGHT = 17;

	private static final List<Boolean> VALUES = List.of(Boolean.TRUE, Boolean.FALSE);

	private boolean pressed;

	private ToggleButton(
			int x,
			int y,
			int index,
			boolean value,
			Component message,
			Component name,
			CycleButton.OnValueChange<Boolean> onValueChange,
			OptionInstance.TooltipSupplier<Boolean> tooltipSupplier) {
		super(
				x,
				y,
				WIDTH,
				HEIGHT,
				message,
				name,
				index,
				value,
				() -> value,
				CycleButton.ValueListSupplier.create(VALUES),
				ToggleButton::stringify,
				CycleButton::createDefaultNarrationMessage,
				onValueChange,
				tooltipSupplier,
				CycleButton.DisplayState.HIDE,
				(button, v) -> null);
	}

	public static ToggleButton create(int x, int y, Component name, boolean value, CycleButton.OnValueChange<Boolean> onValueChange) {
		return create(x, y, name, value, onValueChange, v -> null);
	}

	public static ToggleButton create(
			int x,
			int y,
			Component name,
			boolean value,
			CycleButton.OnValueChange<Boolean> onValueChange,
			OptionInstance.TooltipSupplier<Boolean> tooltipSupplier) {
		return new ToggleButton(x, y, VALUES.indexOf(value), value, CommonComponents.optionNameValue(name, stringify(value)), name, onValueChange, tooltipSupplier);
	}

	private static Component stringify(boolean value) {
		return value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
	}

	private Identifier currentSprite() {
		String state = !active ? "disabled" : pressed ? "pressed" : isHovered() ? "hovered" : isFocused() ? "focused" : "normal";
		return JadeIds.JADE("toggle/" + (getValue() ? "on_" : "off_") + state);
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, currentSprite(), getX(), getY(), WIDTH, HEIGHT);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		pressed = true;
		super.onClick(event, doubleClick);
	}

	@Override
	public void onRelease(MouseButtonEvent event) {
		pressed = false;
		super.onRelease(event);
	}

	@Override
	protected void onDrag(MouseButtonEvent event, double dx, double dy) {
		if (!isHovered()) {
			pressed = false;
		}
		super.onDrag(event, dx, dy);
	}
}
