package snownee.jade.api.callback;

import org.apache.commons.lang3.mutable.MutableObject;

import snownee.jade.api.Accessor;
import snownee.jade.api.theme.Theme;

/**
 * Runs before Jade starts collecting tooltip content.
 */
@FunctionalInterface
public interface JadeBeforeTooltipCollectCallback {

	/**
	 * Called before tooltip collection begins.
	 *
	 * @param theme current theme, wrapped so it can be replaced
	 * @param accessor current accessor
	 * @return {@code true} to continue collecting, {@code false} to cancel
	 */
	boolean beforeCollecting(MutableObject<Theme> theme, Accessor<?> accessor);

}
