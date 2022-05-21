package snownee.jade.api;

public interface IToggleableProvider extends IJadeProvider {

	default boolean isRequired() {
		return false;
	}

	default boolean enabledByDefault() {
		return true;
	}

}
