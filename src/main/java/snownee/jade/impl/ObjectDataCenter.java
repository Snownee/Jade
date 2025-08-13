package snownee.jade.impl;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import snownee.jade.JadeClient;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;

public final class ObjectDataCenter {

	public static int rateLimiter = 250;
	public static long timeLastUpdate = System.currentTimeMillis();
	public static boolean serverConnected;
	private static @Nullable Object lastObject;

	private ObjectDataCenter() {
	}

	public static void set(Accessor<?> accessor) {
		Object object = accessor.getTarget();
		if (object == null && accessor instanceof BlockAccessor blockAccessor) {
			object = blockAccessor.getBlock();
		}

		if (!Objects.equals(object, lastObject)) {
			JadeClient.tickHandler().progressTracker.clear();
			lastObject = object;
			requestServerData();
		}
	}

	public static void requestServerData() {
		timeLastUpdate = 0;
	}

	public static boolean isTimeElapsed(long time) {
		return System.currentTimeMillis() - timeLastUpdate >= time;
	}

	public static void resetTimer() {
		timeLastUpdate = System.currentTimeMillis();
	}

	public static void disconnect() {
		serverConnected = false;
		lastObject = null;
		JadeClient.tickHandler().clearState();
	}
}
