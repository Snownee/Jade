package snownee.jade.api;

import org.jspecify.annotations.Nullable;

/**
 * Provides a custom source for the breaking progress bar rendered below the Jade tooltip.
 * <p>
 * Providers are queried on the client render thread while the tooltip is visible. Return {@code null} when the
 * provider does not have an active operation for the current target, allowing remaining providers or Jade's
 * vanilla block-breaking fallback to handle the bar.
 * <p>
 * If server-authoritative data is required, use an {@link IServerDataProvider} to append it to the accessor's server
 * data.
 */
@FunctionalInterface
public interface IBreakingProgressProvider {

	/**
	 * Returns the breaking progress to render for the current target.
	 *
	 * @param accessor the target currently displayed by Jade
	 * @return the current progress, or {@code null} when this provider is not active for the target
	 */
	@Nullable
	BreakingProgress getBreakingProgress(Accessor<?> accessor);

	/**
	 * The state rendered by Jade's breaking progress bar.
	 *
	 * @param progress   normalized progress in the range {@code [0, 1]}; values outside the range are clamped
	 * @param canHarvest whether to use the successful-harvest color instead of the failure color
	 */
	record BreakingProgress(float progress, boolean canHarvest) {
	}
}
