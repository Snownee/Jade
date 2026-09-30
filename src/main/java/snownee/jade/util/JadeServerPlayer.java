package snownee.jade.util;

public interface JadeServerPlayer {
	boolean jade$isConnected();

	void jade$setConnected(boolean connected);

	boolean jade$tryAcquireRequest();
}
