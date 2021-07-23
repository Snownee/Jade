package mcp.mobius.waila.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * A generic class to get basic information of target and context.
 */
public abstract class Accessor {

	private final Level world;
	private final Player player;
	private final CompoundTag serverData;
	private final HitResult hit;
	private final boolean serverConnected;

	private TooltipPosition tooltipPosition;

	public Accessor(Level world, Player player, CompoundTag serverData, HitResult hit, boolean serverConnected) {
		this.world = world;
		this.player = player;
		this.serverData = serverData;
		this.hit = hit;
		this.serverConnected = serverConnected;
	}

	public Level getLevel() {
		return world;
	}

	public Player getPlayer() {
		return player;
	}

	public CompoundTag getServerData() {
		return serverData == null ? new CompoundTag() : serverData;
	}

	public HitResult getHitResult() {
		return hit;
	}

	/**
	 * Returns true if dedicated server has Jade installed.
	 */
	public boolean isServerConnected() {
		return serverConnected;
	}

	/**
	 * Get {@link TooltipPosition} the {@link ITooltip} currently gathering
	 */
	public TooltipPosition getTooltipPosition() {
		return tooltipPosition;
	}

	public void setTooltipPosition(TooltipPosition tooltipPosition) {
		this.tooltipPosition = tooltipPosition;
	}

	public abstract ItemStack getPickedResult();
}
