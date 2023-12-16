package snownee.jade.api;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

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
	private final Supplier<T> hit;
	private final boolean serverConnected;
	private final boolean showDetails;
	protected boolean verify;

	public AccessorImpl(Level level, Player player, CompoundTag serverData, Supplier<T> hit, boolean serverConnected, boolean showDetails) {
		this.level = level;
		this.player = player;
		this.hit = hit;
		this.serverConnected = serverConnected;
		this.showDetails = showDetails;
		this.serverData = serverData == null ? new CompoundTag() : serverData.copy();
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
	public final @NotNull CompoundTag getServerData() {
		return serverData;
	}

	@Override
	public T getHitResult() {
		return hit.get();
	}

	/**
	 * Returns true if dedicated server has Jade installed.
	 */
	@Override
	public boolean isServerConnected() {
		return serverConnected;
	}

	@Override
	public boolean showDetails() {
		return showDetails;
	}

	@Override
	public abstract ItemStack getPickedResult();

	public void requireVerification() {
		verify = true;
	}
}
