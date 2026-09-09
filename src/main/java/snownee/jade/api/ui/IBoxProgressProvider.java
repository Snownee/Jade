package snownee.jade.api.ui;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.Accessor;

/**
 * Provides the per-frame state of a {@link BoxElement}'s progress bar.
 * <p>
 * Providers attached to a box are queried every render frame in ascending priority order; the first non-null result is
 * rendered for that frame. Returning {@code null} lets lower-priority providers or the box's static progress
 * ({@link BoxElement#setBoxProgress}) take over. When no source returns a value, the box fades the bar out.
 */
@FunctionalInterface
public interface IBoxProgressProvider {

	/**
	 * Returns the progress bar state for the current frame.
	 *
	 * @param box the box this provider is attached to
	 * @param accessor the target currently displayed by Jade
	 * @param partialTicks partial tick, for interpolating progress values
	 * @return the desired progress bar state, or {@code null} when this provider is not active
	 */
	@Nullable
	BoxProgress getProgress(BoxElement box, Accessor<?> accessor, float partialTicks);

}