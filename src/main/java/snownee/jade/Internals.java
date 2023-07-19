package snownee.jade;

import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.ElementHelper;
import snownee.jade.overlay.DisplayHelper;

public final class Internals {

	public static IWailaConfig getWailaConfig() {
		return Jade.CONFIG.get();
	}

	public static IElementHelper getElementHelper() {
		return ElementHelper.INSTANCE;
	}

	public static IDisplayHelper getDisplayHelper() {
		return DisplayHelper.INSTANCE;
	}
}
