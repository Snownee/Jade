package snownee.jade.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * A generic class to get basic information of target and context.
 */
public abstract class AccessorImpl<T extends HitResult> implements Accessor<T> {

	private final Level level;
	private final Player player;
	private final CompoundTag serverData;
	private final T hit;
	private final boolean serverConnected;

	public AccessorImpl(Level level, Player player, CompoundTag serverData, T hit, boolean serverConnected) {
		this.level = level;
		this.player = player;
		this.serverData = serverData;
		this.hit = hit;
		this.serverConnected = serverConnected;
	}

	@Override
	public Level getLevel() {
		return level;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public CompoundTag getServerData() {
		return serverData == null ? new CompoundTag() : serverData;
	}

	@Override
	public T getHitResult() {
		return hit;
	}

	/**
	 * Returns true if dedicated server has Jade installed.
	 */
	@Override
	public boolean isServerConnected() {
		return serverConnected;
	}

	@Override
	public abstract ItemStack getPickedResult();
}
