package snownee.jade.impl;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import snownee.jade.api.Accessor;
import snownee.jade.overlay.WailaTickHandler;

public final class ObjectDataCenter {

	private ObjectDataCenter() {
	}

	public static int rateLimiter = 250;
	private static Accessor<?> accessor;
	private static CompoundTag serverData;

	private static Object lastObject;
	public static long timeLastUpdate = System.currentTimeMillis();
	public static boolean serverConnected;

	public static void set(@Nullable Accessor<?> accessor) {
		ObjectDataCenter.accessor = accessor;
		if (accessor == null) {
			WailaTickHandler.instance().progressTracker.clear();
			lastObject = null;
			return;
		}

		Object object = accessor._getTrackObject();

		if (object != lastObject) {
			WailaTickHandler.instance().progressTracker.clear();
			lastObject = object;
			serverData = null;
			requestServerData();
		}
	}

	public static Accessor<?> get() {
		return accessor;
	}

	public static void setServerData(CompoundTag tag) {
		serverData = tag;
	}

	public static CompoundTag getServerData() {
		if (accessor == null || serverData == null)
			return null;
		if (accessor._verifyData(serverData))
			return serverData;
		requestServerData();
		return null;
	}

	public static void requestServerData() {
		timeLastUpdate = System.currentTimeMillis() - rateLimiter;
	}

	public static boolean isTimeElapsed(long time) {
		return System.currentTimeMillis() - timeLastUpdate >= time;
	}

	public static void resetTimer() {
		timeLastUpdate = System.currentTimeMillis();
	}
}
