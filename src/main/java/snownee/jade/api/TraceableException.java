package snownee.jade.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

public class TraceableException extends RuntimeException {
	private final String namespace;

	public TraceableException(Throwable cause, String namespace) {
		super("Exception occurred in " + namespace, cause);
		this.namespace = namespace;
	}

	public static RuntimeException create(Throwable cause, @Nullable String namespace) {
		if (namespace == null || ResourceLocation.DEFAULT_NAMESPACE.equals(namespace)) {
			if (cause instanceof RuntimeException runtimeException) {
				return runtimeException;
			} else {
				return new RuntimeException(cause);
			}
		}
		return new TraceableException(cause, namespace);
	}

	public String getNamespace() {
		return namespace;
	}
}
