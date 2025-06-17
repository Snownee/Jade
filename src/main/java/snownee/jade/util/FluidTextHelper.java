package snownee.jade.util;

import java.text.NumberFormat;
import java.util.function.Supplier;

import com.google.common.math.LongMath;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import snownee.jade.JadeClient;
import snownee.jade.api.ui.NarratableComponent;

public class FluidTextHelper {
	/**
	 * Return a unicode string representing a fraction, like ¹⁄₈₁.
	 */
	public static String getUnicodeFraction(long numerator, long denominator, boolean simplify) {
		if (numerator < 0 || denominator < 0) {
			throw new IllegalArgumentException("Numerator and denominator must be non negative.");
		}

		if (simplify && denominator != 0) {
			long g = LongMath.gcd(numerator, denominator);
			numerator /= g;
			denominator /= g;
		}

		StringBuilder numString = new StringBuilder();

		while (numerator > 0) {
			numString.append(SUPERSCRIPT[(int) (numerator % 10)]);
			numerator /= 10;
		}

		StringBuilder denomString = new StringBuilder();

		while (denominator > 0) {
			denomString.append(SUBSCRIPT[(int) (denominator % 10)]);
			denominator /= 10;
		}

		return numString.reverse().toString() + FRACTION_BAR + denomString.reverse();
	}

	private static String getFractionNarration(long numerator, long denominator, boolean simplify) {
		if (numerator < 0 || denominator < 0) {
			throw new IllegalArgumentException("Numerator and denominator must be non negative.");
		}

		if (simplify && denominator != 0) {
			long g = LongMath.gcd(numerator, denominator);
			numerator /= g;
			denominator /= g;
		}

		String key = "narration.jade.%s/%s".formatted(numerator, denominator);
		if (I18n.exists(key)) {
			return I18n.get(key);
		}
		return JadeClient.formatString("narration.jade.N/N", numerator, denominator);
	}

	public static NarratableComponent getMillibuckets(long mb, boolean simplify) {
		return makeString(mb, 0, 0, "mB");
	}

	private static NarratableComponent makeString(long integer, long numerator, long denominator, String unit) {
		return makeString(
				integer == 0L && denominator != 0L ? "" : NumberFormat.getNumberInstance().format(integer),
				integer,
				numerator,
				denominator,
				unit);
	}

	private static NarratableComponent makeString(String integerString, long integer, long numerator, long denominator, String unit) {
		String string;
		Supplier<String> narration;
		if (denominator == 0) {
			string = integerString + unit;
			narration = () -> integerString;
		} else if (integerString.isEmpty()) {
			string = getUnicodeFraction(numerator, denominator, true) + unit;
			narration = () -> getFractionNarration(numerator, denominator, true);
		} else {
			string = integerString + " " + getUnicodeFraction(numerator, denominator, true) + unit;
			narration = () -> JadeClient.formatString(
					"narration.jade.integer_and_fraction",
					integerString,
					getFractionNarration(numerator, denominator, true));
		}
		return new NarratableComponent(
				Component.literal(string), () -> {
			double number = integer;
			if (denominator != 0) {
				number += (double) numerator / (double) denominator;
			}
			return JadeClient.formatString("narration.jade.unit." + unit, narration.get(), number);
		});
	}

	private static final char[] SUPERSCRIPT = new char[]{
			'\u2070',
			'\u00b9',
			'\u00b2',
			'\u00b3',
			'\u2074',
			'\u2075',
			'\u2076',
			'\u2077',
			'\u2078',
			'\u2079'};
	private static final char FRACTION_BAR = '\u2044';
	private static final char[] SUBSCRIPT = new char[]{
			'\u2080',
			'\u2081',
			'\u2082',
			'\u2083',
			'\u2084',
			'\u2085',
			'\u2086',
			'\u2087',
			'\u2088',
			'\u2089'};

	private FluidTextHelper() {
	}
}