package snownee.jade.util;

import java.util.List;

public class JadeMetadata {
	protected boolean accessibilityMod = CommonProxy.isModLoaded("minecraft_access");
	protected boolean fastScroll;
	protected boolean smoothScroll;
	protected List<String> disableItemModNameTooltip = List.of();

	public boolean hasAccessibilityMod() {
		return accessibilityMod;
	}

	public boolean hasFastScroll() {
		return fastScroll;
	}

	public boolean hasSmoothScroll() {
		return smoothScroll;
	}

	public List<String> disableItemModNameTooltip() {
		return disableItemModNameTooltip;
	}
}
