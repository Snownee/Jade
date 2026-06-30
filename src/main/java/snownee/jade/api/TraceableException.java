package snownee.jade.api;

import java.io.Serial;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

/**
 * Wraps an exception with the Jade namespace that triggered it.
 */
public class TraceableException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = -1306920332552101886L;
	private final String namespace;

	/**
	 * Creates a traceable exception for the given cause and namespace.
	 *
	 * @param cause underlying failure
	 * @param namespace provider namespace that triggered the failure
	 */
	public TraceableException(Throwable cause, String namespace) {
		super("Exception occurred in " + namespace, cause);
		this.namespace = namespace;
	}

	/**
	 * Converts a throwable into the most appropriate runtime exception for logging.
	 *
	 * @param cause underlying failure
	 * @param namespace provider namespace, or {@code null}
	 * @return a runtime exception to throw
	 */
	public static RuntimeException create(Throwable cause, @Nullable String namespace) {
		if (namespace == null || Identifier.DEFAULT_NAMESPACE.equals(namespace)) {
			if (cause instanceof RuntimeException runtimeException) {
				return runtimeException;
			} else {
				return new RuntimeException(cause);
			}
		}
		return new TraceableException(cause, namespace);
	}

	/**
	 * Returns the provider namespace that triggered this exception.
	 *
	 * @return the namespace string
	 */
	public String getNamespace() {
		return namespace;
	}
}
