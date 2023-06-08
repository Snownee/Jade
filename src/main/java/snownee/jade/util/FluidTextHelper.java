package snownee.jade.util;

import snownee.jade.api.ui.IDisplayHelper;

public class FluidTextHelper {

	public static String getUnicodeMillibuckets(long amount, boolean simplify) {
		return IDisplayHelper.get().humanReadableNumber(amount, "B", true);
	}

}
