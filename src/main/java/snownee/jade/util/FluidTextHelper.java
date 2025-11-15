package snownee.jade.util;
/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.text.NumberFormat;
import java.util.function.Supplier;

import com.google.common.math.LongMath;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import snownee.jade.JadeClient;
import snownee.jade.api.ui.NarratableComponent;
import snownee.jade.overlay.DisplayHelper;

/**
 * A few helpers to display fluids.
 */
public class FluidTextHelper {
	private static final boolean FABRIC = "fabric".equals(CommonProxy.getPlatformIdentifier());
	public static final long BUCKET = FABRIC ? 81000 : 1000;

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

	public static NarratableComponent getMillibuckets(long integer, boolean simplify) {
		if (!FABRIC) {
			return makeString(integer, 0, 0, "mB");
		}
		long mb = integer / 81;
		long leftover = integer % BUCKET;
		if (leftover == 0 || integer >= BUCKET * 100) {
			String s = DisplayHelper.INSTANCE.humanReadableNumber(mb, "B", true);
			if (s.endsWith("mB")) {
				return makeString(s.substring(0, s.length() - 2), mb, 0, 0, "mB");
			} else {
				return makeString(s.substring(0, s.length() - 1), mb / 1000L, 0, 0, "B");
			}
		}
		if (integer % 81 == 0) {
			return makeString(mb, 0, 0, "mB");
		}
		if (simplify) {
			long g = LongMath.gcd(leftover, BUCKET);
			if (g >= 1000) {
				return makeString(mb / 1000, leftover / g, BUCKET / g, "B");
			}
		}
		return makeString(mb, integer % 81, 81, "mB");
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
