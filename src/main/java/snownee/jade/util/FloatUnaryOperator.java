package snownee.jade.util;

import java.util.function.UnaryOperator;

/**
 * A depended class that not exist in fastutil 8.2.1
 * This impl is from fastutil 8.2.12
 */
@FunctionalInterface
public interface FloatUnaryOperator extends UnaryOperator<Float>, java.util.function.DoubleUnaryOperator {
	float apply(float x);

	public static FloatUnaryOperator identity() {
		return  i -> i;
	}

	public static FloatUnaryOperator negation() { return i -> -i; }

	@Deprecated
	@Override
	default double applyAsDouble(final double x) {
		return apply(it.unimi.dsi.fastutil.SafeMath.safeDoubleToFloat(x));
	}

	@Deprecated
	@Override
	@SuppressWarnings("boxing")
	default Float apply(final Float x) {
		return apply(x.floatValue());
	}
}
