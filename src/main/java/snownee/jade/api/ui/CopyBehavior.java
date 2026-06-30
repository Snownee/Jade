package snownee.jade.api.ui;

import net.minecraft.client.KeyboardHandler;

/**
 * Optional clipboard export for a UI element.
 */
public interface CopyBehavior {
	/**
	 * Copies this element to the clipboard.
	 *
	 * @param keyboardHandler keyboard handler to use
	 * @return {@code true} if something was copied
	 */
	boolean copyToClipboard(KeyboardHandler keyboardHandler);
}
