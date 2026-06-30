package snownee.jade.api.callback;

import snownee.jade.api.Accessor;
import snownee.jade.api.ui.BoxElement;

/**
 * Runs after tooltip content has been collected.
 */
@FunctionalInterface
public interface JadeTooltipCollectedCallback {

	/**
	 * Called once the tooltip tree has been assembled.
	 *
	 * @param rootElement root tooltip element
	 * @param accessor current accessor
	 */
	void onTooltipCollected(BoxElement rootElement, Accessor<?> accessor);

}
