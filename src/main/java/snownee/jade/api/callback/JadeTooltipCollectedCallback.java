package snownee.jade.api.callback;

import snownee.jade.api.Accessor;
import snownee.jade.api.ui.BoxElement;

@FunctionalInterface
public interface JadeTooltipCollectedCallback {

	void onTooltipCollected(BoxElement rootElement, Accessor<?> accessor);

}
