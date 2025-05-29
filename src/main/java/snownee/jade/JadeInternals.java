package snownee.jade;

import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.impl.theme.ThemeHelper;
import snownee.jade.overlay.DisplayHelper;

public final class JadeInternals {

	public static IWailaConfig getWailaConfig() {
		return Jade.config();
	}

	public static IDisplayHelper getDisplayHelper() {
		return DisplayHelper.INSTANCE;
	}

	public static IThemeHelper getThemeHelper() {
		return ThemeHelper.INSTANCE;
	}
}
