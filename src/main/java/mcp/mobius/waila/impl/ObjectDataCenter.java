package mcp.mobius.waila.impl;

import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.overlay.WailaTickHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public final class ObjectDataCenter {

	private ObjectDataCenter() {
	}

	public static int rateLimiter = 250;
	private static Accessor accessor;
	private static CompoundTag serverData;

	private static Object lastObject;
	public static long timeLastUpdate = System.currentTimeMillis();
	public static boolean serverConnected;

	public static void set(Accessor accessor) {
		ObjectDataCenter.accessor = accessor;
		if (accessor == null) {
			WailaTickHandler.instance().progressTracker.clear();
			lastObject = null;
			return;
		}

		Object object = null;
		if (accessor instanceof BlockAccessor) {
			object = ((BlockAccessor) accessor).getBlockEntity();
		} else if (accessor instanceof EntityAccessor) {
			object = ((EntityAccessor) accessor).getEntity();
		}

		if (object != lastObject) {
			WailaTickHandler.instance().progressTracker.clear();
			lastObject = object;
			serverData = null;
			requestServerData();
		}
	}

	public static Accessor get() {
		return accessor;
	}

	public static void setServerData(CompoundTag tag) {
		serverData = tag;
	}

	public static CompoundTag getServerData() {
		if (accessor instanceof BlockAccessor && isTagCorrectBlockEntity())
			return serverData;

		else if (accessor instanceof EntityAccessor && isTagCorrectEntity())
			return serverData;

		return null;
	}

	private static boolean isTagCorrectBlockEntity() {
		if (serverData == null) {
			requestServerData();
			return false;
		}

		int x = serverData.getInt("x");
		int y = serverData.getInt("y");
		int z = serverData.getInt("z");

		BlockPos hitPos = ((BlockAccessor) accessor).getPosition();
		if (x == hitPos.getX() && y == hitPos.getY() && z == hitPos.getZ())
			return true;
		else {
			requestServerData();
			return false;
		}
	}

	private static boolean isTagCorrectEntity() {
		if (serverData == null || !serverData.contains("WailaEntityID")) {
			requestServerData();
			return false;
		}

		int id = serverData.getInt("WailaEntityID");

		if (id == ((EntityAccessor) accessor).getEntity().getId())
			return true;
		else {
			requestServerData();
			return false;
		}
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
