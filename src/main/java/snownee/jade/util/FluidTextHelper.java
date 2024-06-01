package snownee.jade.util;

import java.text.NumberFormat;

import snownee.jade.api.ui.IDisplayHelper;

public class FluidTextHelper {

	public static String getUnicodeMillibuckets(long amount, boolean simplify) {
		if (amount < 100000) {
			return NumberFormat.getNumberInstance().format(amount) + "mB";
		} else {
			return IDisplayHelper.get().humanReadableNumber(amount, "B", true, null);
		}
	}

}
