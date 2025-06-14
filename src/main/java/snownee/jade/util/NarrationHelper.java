package snownee.jade.util;

import snownee.jade.JadeClient;

public final class NarrationHelper {
	private NarrationHelper() {}

	public static String number(int i) {
		return i >= 0 ? Integer.toString(i) : JadeClient.formatString("narration.jade.negative", -i);
	}
}
