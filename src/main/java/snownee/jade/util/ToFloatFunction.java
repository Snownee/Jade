package snownee.jade.util;

@FunctionalInterface
public interface ToFloatFunction<T> {
	float applyAsFloat(T value);
}
