package snownee.jade.util;

import java.math.BigDecimal;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

// Modified from: https://github.com/silentsoft/csscolor4j/blob/main/src/main/java/org/silentsoft/csscolor4j/Color.java
public class Color {

	public static final Codec<Integer> CODEC = Codec.STRING.comapFlatMap($ -> {
		try {
			return DataResult.success(Color.valueOf($).toInt());
		} catch (IllegalArgumentException e) {
			return DataResult.error(() -> "Invalid color: " + $);
		}
	}, $ -> Color.rgb($).getHex());

	private int red;
	private int green;
	private int blue;

	//	private double cyan;
	//	private double magenta;
	//	private double yellow;
	//	private double black;

	private double hue;
	private double saturation;
	private double lightness;

	private double opacity;

	private String hex;

	private Color() {

	}

	/**
     * @param red 0 to 255.
     * @param green 0 to 255.
     * @param blue 0 to 255.
     * @return a <code>Color</code> object that contains color information such as red, green, blue, cyan, magenta, yellow, black, hue, saturation, lightness, opacity, and hexadecimal numbers
     * @see #valueOf(String)
     */
	public static Color rgb(int red, int green, int blue) {
		return rgb(red, green, blue, 1.0);
	}

	/**
     * @param red 0 to 255.
     * @param green 0 to 255.
     * @param blue 0 to 255.
     * @param opacity 0.0 to 1.0.
     * @return a <code>Color</code> object that contains color information such as red, green, blue, cyan, magenta, yellow, black, hue, saturation, lightness, opacity, and hexadecimal numbers
     * @see #valueOf(String)
     */
	public static Color rgb(int red, int green, int blue, double opacity) {
		Color color = new Color();

		color.red = red;
		color.green = green;
		color.blue = blue;
		color.opacity = opacity;

		{
			double r = red / 255.0;
			double g = green / 255.0;
			double b = blue / 255.0;

			double max = Math.max(Math.max(r, g), b);
			double min = Math.min(Math.min(r, g), b);
			double delta = max - min;

			{
				color.lightness = (max + min) / 2;
				color.saturation = (delta == 0) ? 0 : delta / (1 - Math.abs((2 * color.lightness) - 1));
				if (delta == 0) {
					color.hue = 0;
				} else if (max == r) {
					color.hue = 60 * (((g - b) / delta) + 0);
				} else if (max == g) {
					color.hue = 60 * (((b - r) / delta) + 2);
				} else if (max == b) {
					color.hue = 60 * (((r - g) / delta) + 4);
				}

				color.hue = color.hue < 0 ? color.hue + 360 : color.hue > 360 ? 360 : color.hue;
			}
			//			{
			//				color.black = 1 - max;
			//				if (color.black == 1 && max == min) {
			//					color.cyan = 0;
			//					color.magenta = 0;
			//					color.yellow = 0;
			//				} else {
			//					color.cyan = (1 - r - color.black) / (1 - color.black);
			//					color.magenta = (1 - g - color.black) / (1 - color.black);
			//					color.yellow = (1 - b - color.black) / (1 - color.black);
			//				}
			//			}
		}

		color.hex = String.format("#%02x%02x%02x", red, green, blue);

		return color;
	}

	//	/**
	//     *
	//     * @param cyan 0.0 to 1.0.
	//     * @param magenta 0.0 to 1.0.
	//     * @param yellow 0.0 to 1.0.
	//     * @param black 0.0 to 1.0.
	//     * @return a <code>Color</code> object that contains color information such as red, green, blue, cyan, magenta, yellow, black, hue, saturation, lightness, opacity, and hexadecimal numbers
	//     * @see #valueOf(String)
	//     */
	//	public static Color cmyk(double cyan, double magenta, double yellow, double black) {
	//		return cmyk(cyan, magenta, yellow, black, 1.0);
	//	}
	//
	//	/**
	//     * @param cyan 0.0 to 1.0.
	//     * @param magenta 0.0 to 1.0.
	//     * @param yellow 0.0 to 1.0.
	//     * @param black 0.0 to 1.0.
	//     * @param opacity 0.0 to 1.0.
	//     * @return a <code>Color</code> object that contains color information such as red, green, blue, cyan, magenta, yellow, black, hue, saturation, lightness, opacity, and hexadecimal numbers
	//     * @see #valueOf(String)
	//     */
	//	public static Color cmyk(double cyan, double magenta, double yellow, double black, double opacity) {
	//		int red = (int) Math.round(255 * (1 - cyan) * (1 - black));
	//		int green = (int) Math.round(255 * (1 - magenta) * (1 - black));
	//		int blue = (int) Math.round(255 * (1 - yellow) * (1 - black));
	//
	//		Color color = rgb(red, green, blue, opacity);
	//		color.cyan = cyan;
	//		color.magenta = magenta;
	//		color.yellow = yellow;
	//		color.black = black;
	//
	//		return color;
	//	}

	/**
     * @param hue 0 to 360.
     * @param saturation 0.0 to 1.0.
     * @param lightness 0.0 to 1.0.
     * @return a <code>Color</code> object that contains color information such as red, green, blue, cyan, magenta, yellow, black, hue, saturation, lightness, opacity, and hexadecimal numbers
     * @see #valueOf(String)
     */
	public static Color hsl(double hue, double saturation, double lightness) {
		return hsl(hue, saturation, lightness, 1.0);
	}

	/**
     * @param hue 0 to 360.
     * @param saturation 0.0 to 1.0.
     * @param lightness 0.0 to 1.0.
     * @param opacity 0.0 to 1.0.
     * @return a <code>Color</code> object that contains color information such as red, green, blue, cyan, magenta, yellow, black, hue, saturation, lightness, opacity, and hexadecimal numbers
     * @see #valueOf(String)
     */
	public static Color hsl(double hue, double saturation, double lightness, double opacity) {
		double _c = (1 - Math.abs((2 * lightness) - 1)) * saturation;
		double _h = hue / 60;
		double _x = _c * (1 - Math.abs((_h % 2) - 1));
		double[] _rgb = new double[] { 0, 0, 0 };
		if (_h >= 0 && _h < 1) {
			_rgb = new double[] { _c, _x, 0 };
		} else if (_h >= 1 && _h < 2) {
			_rgb = new double[] { _x, _c, 0 };
		} else if (_h >= 2 && _h < 3) {
			_rgb = new double[] { 0, _c, _x };
		} else if (_h >= 3 && _h < 4) {
			_rgb = new double[] { 0, _x, _c };
		} else if (_h >= 4 && _h < 5) {
			_rgb = new double[] { _x, 0, _c };
		} else if (_h >= 5 && _h < 6) {
			_rgb = new double[] { _c, 0, _x };
		}
		double _m = lightness - (_c / 2);

		int red = (int) ((_rgb[0] + _m) * 255);
		int green = (int) ((_rgb[1] + _m) * 255);
		int blue = (int) ((_rgb[2] + _m) * 255);

		Color color = rgb(red, green, blue, opacity);
		color.hue = hue;
		color.saturation = saturation;
		color.lightness = lightness;

		return color;
	}

	/**
     * @param value the string to convert
     * @return a <code>Color</code> object that contains color information such as red, green, blue, cyan, magenta, yellow, black, hue, saturation, lightness, opacity, and hexadecimal numbers
     * @see #valueOf(String)
     */
	public static Color hex(String value) {
		value = value.trim();
		String hex = value.startsWith("#") ? value.substring(1) : value;
		String filledHex = fill(hex);

		int red = Integer.parseInt(filledHex.substring(0, 2), 16);
		int green = Integer.parseInt(filledHex.substring(2, 4), 16);
		int blue = Integer.parseInt(filledHex.substring(4, 6), 16);

		double opacity = 1.0;
		if (filledHex.length() == 8) {
			opacity = Integer.parseInt(filledHex.substring(6, 8), 16) / 255.0;
		}

		Color color = rgb(red, green, blue, opacity);
		color.hex = "#".concat(hex);

		return color;
	}

	/**
	 * Creates a color from a string representation.<br>
	 * <p>
	 * Supported formats are:
	 * <ul>
	 *     <li><code>rgb[a](red, green, blue[, opacity])</code></li>
	 *     <li><code>cmyk[a](cyan, magenta, yellow, black[, opacity])</code></li>
	 *     <li><code>hsl[a](hue, saturation, lightness[, opacity])</code></li>
	 *     <li>hexadecimal numbers</li>
	 * </ul>
	 * <p>
	 * Examples:
	 * <pre>
	 * Color.valueOf("rgb(255, 0, 153)");
	 * Color.valueOf("rgb(100%, 0%, 60%)");
	 * Color.valueOf("rgb(255 0 153)");
	 * Color.valueOf("rgb(255, 0, 153, 1)");
	 * Color.valueOf("rgb(255, 0, 153, 100%)");
	 * Color.valueOf("rgb(255 0 153 / 1)");
	 * Color.valueOf("rgb(255 0 153 / 100%)");
	 * Color.valueOf("rgb(1e2, .5e1, .5e0, +.25e2%)");
	 *
	 * Color.valueOf("rgba(51, 170, 51, .5)");
	 *
	 * Color.valueOf("cmyk(1, 0, 0, 0)");
	 *
	 * Color.valueOf("hsl(270, 60%, 70%)");
	 * Color.valueOf("hsl(270deg, 60%, 70%)");
	 * Color.valueOf("hsl(4.71239rad, 60%, 70%)");
	 * Color.valueOf("hsl(300grad, 60%, 70%)");
	 * Color.valueOf("hsl(.75turn, 60%, 70%)");
	 *
	 * Color.valueOf("black");
	 * Color.valueOf("rebeccapurple");
	 *
	 * Color.valueOf("#f09");
	 * Color.valueOf("#ff0099");
	 * Color.valueOf("#ff0099ff");
	 * </pre>
	 *
	 * @param value the string to convert
	 * @return a <code>Color</code> object that contains color information such as red, green, blue, cyan, magenta, yellow, black, hue, saturation, lightness, opacity, and hexadecimal numbers
	 * @throws IllegalArgumentException if the given string value cannot be recognized as <code>rgb</code>, <code>cmyk</code>, <code>hsl</code>, {@link NamedColor named color} or hexadecimal numbers
	 * @see #rgb(int, int, int)
	 * @see #rgb(int, int, int, double)
	 * @see #hsl(double, double, double)
	 * @see #hsl(double, double, double, double)
	 * @see #hex(String)
	 */
	public static Color valueOf(String value) throws IllegalArgumentException {
		value = value.trim().toLowerCase();

		if (value.contains("rgb")) {
			String[] rgb = split(value);
			if (rgb[0].contains("%") || rgb[1].contains("%") || rgb[2].contains("%")) {
				boolean makeSense = rgb[0].contains("%") && rgb[1].contains("%") && rgb[2].contains("%");
				if (!makeSense) {
					throw new IllegalArgumentException("Cannot mix numbers and percentages in RGB calculations.");
				}
			}
			int red = parseInt(rgb[0], 255);
			int green = parseInt(rgb[1], 255);
			int blue = parseInt(rgb[2], 255);
			double opacity = rgb.length >= 4 ? parseDouble(rgb[3], 1) : 1.0;

			return rgb(red, green, blue, opacity);
			//		} else if (value.contains("cmyk")) {
			//			String[] cmyk = split(value);
			//			double cyan = parseDouble(cmyk[0], 1);
			//			double magenta = parseDouble(cmyk[1], 1);
			//			double yellow = parseDouble(cmyk[2], 1);
			//			double black = parseDouble(cmyk[3], 1);
			//			double opacity = cmyk.length >= 5 ? parseDouble(cmyk[4], 1) : 1.0;
			//
			//			return cmyk(cyan, magenta, yellow, black, opacity);
		} else if (value.contains("hsl")) {
			String[] hsl = split(value);
			double hue = toDegrees(hsl[0]);
			double saturation = parseDouble(hsl[1], 1);
			double lightness = parseDouble(hsl[2], 1);
			double opacity = hsl.length >= 4 ? parseDouble(hsl[3], 1) : 1.0;

			return hsl(hue, saturation, lightness, opacity);
		} else if ("transparent".equals(value)) {
			return rgb(0, 0, 0, 0);
		} else if (value.startsWith("#") || value.length() == 3 || value.length() == 4 || value.length() == 6 || value.length() == 8) {
			/* This else if statement must be at the end. */
			return hex(value);
		}

		throw new IllegalArgumentException(value + " cannot be recognized.");
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	//	public double getCyan() {
	//		return cyan;
	//	}
	//
	//	public double getMagenta() {
	//		return magenta;
	//	}
	//
	//	public double getYellow() {
	//		return yellow;
	//	}
	//
	//	public double getBlack() {
	//		return black;
	//	}

	public double getHue() {
		return hue;
	}

	public double getSaturation() {
		return saturation;
	}

	public double getLightness() {
		return lightness;
	}

	public double getOpacity() {
		return opacity;
	}

	public String getHex() {
		return hex;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Color color = (Color) o;
		return red == color.red && green == color.green && blue == color.blue && /*Double.compare(color.cyan, cyan) == 0 && Double.compare(color.magenta, magenta) == 0 && Double.compare(color.yellow, yellow) == 0 && Double.compare(color.black, black) == 0 &&*/ Double.compare(color.hue, hue) == 0 && Double.compare(color.saturation, saturation) == 0 && Double.compare(color.lightness, lightness) == 0 && Double.compare(color.opacity, opacity) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(red, green, blue, /*cyan, magenta, yellow, black, */hue, saturation, lightness, opacity);
	}

	@Override
	public String toString() {
		return "Color{" + "red=" + red + ", green=" + green + ", blue=" + blue + /*", cyan=" + cyan + ", magenta=" + magenta + ", yellow=" + yellow + ", black=" + black +*/ ", hue=" + hue + ", saturation=" + saturation + ", lightness=" + lightness + ", opacity=" + opacity + ", hex='" + hex + '\'' + '}';
	}

	private static String fill(String hex) {
		if (hex.length() == 3 || hex.length() == 4) {
			String value = "";
			for (char letter : hex.toCharArray()) {
				value = value.concat(String.valueOf(new char[] { letter, letter }));
			}
			return value;
		}
		return hex;
	}

	private static double parseDouble(String value, double limit) {
		boolean hasPercent = value.contains("%");
		value = value.replace("%", "").trim();
		double number = Double.valueOf(value);
		return hasPercent ? BigDecimal.valueOf(number).multiply(BigDecimal.valueOf((limit / 100.0))).doubleValue() : number;
	}

	private static int parseInt(String value, double limit) {
		double number = parseDouble(value, limit);
		int round = (int) Math.round(number);
		return round > limit ? (int) limit : round;
	}

	private static String[] split(String value) {
		value = value.replace("/", " ");
		value = value.replaceAll("(\\s)+", " ");
		value = value.substring(value.indexOf("(") + 1, value.indexOf(")"));
		return value.contains(",") ? value.split(",") : value.split(" ");
	}

	private static double toDegrees(String value) {
		value = value.toLowerCase().trim();
		if (value.contains("deg")) {
			return Double.valueOf(value.replace("deg", "").trim());
		} else if (value.contains("grad")) {
			return (Double.valueOf(value.replace("grad", "").trim()) / 400.0) * 360.0;
		} else if (value.contains("rad")) {
			return Double.valueOf(value.replace("rad", "").trim()) * (180.0 / Math.PI);
		} else if (value.contains("turn")) {
			return Double.valueOf(value.replace("turn", "").trim()) * 360.0;
		}
		return parseDouble(value, 360);
	}

	public int toInt() {
		return (((int) (opacity * 255) & 0xff) << 24) + ((red & 0xff) << 16) + ((green & 0xff) << 8) + (blue & 0xff);
	}

	public static Color rgb(int color) {
		double a = (color >> 24 & 255) / 255D;
		if (a == 0)
			a = 1;
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		return rgb(r, g, b, a);
	}
}
