package snownee.jade.util;

import it.unimi.dsi.fastutil.SafeMath;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;
@FunctionalInterface
public interface FloatUnaryOperator extends UnaryOperator<Float>, DoubleUnaryOperator {
    float apply(float var1);

    static FloatUnaryOperator identity() {
        return (i) -> {
            return i;
        };
    }

    static FloatUnaryOperator negation() {
        return (i) -> {
            return -i;
        };
    }

    /** @deprecated */
    @Deprecated
    default double applyAsDouble(double x) {
        return (double)this.apply(SafeMath.safeDoubleToFloat(x));
    }

    /** @deprecated */
    @Deprecated
    default Float apply(Float x) {
        return this.apply(x);
    }
}
