package snownee.jade.api.ui;

import org.jetbrains.annotations.Nullable;

import snownee.jade.api.ITooltip;
import snownee.jade.impl.ui.StyledElement;

public interface IBoxElement extends IElement, StyledElement {

	ITooltip getTooltip();

	void setBoxProgress(MessageType type, float progress);

	float getBoxProgress();

	void clearBoxProgress();

	/**
	 * Attaches a progress bar provider. Providers are queried every frame in ascending priority order; the first
	 * non-null result wins.
	 *
	 * @param priority lower values are queried first
	 * @param provider per-frame progress bar provider
	 */
	void addProgressProvider(int priority, IBoxProgressProvider provider);

	default void addProgressProvider(IBoxProgressProvider provider) {
		addProgressProvider(0, provider);
	}

	void setIcon(@Nullable IElement icon);

	int padding(ScreenDirection direction);

	void setPadding(ScreenDirection direction, int value);

	@Override
	BoxStyle getStyle();
}
