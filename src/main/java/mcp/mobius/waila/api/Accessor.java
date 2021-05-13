package mcp.mobius.waila.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * A generic class to get basic information of target and context.
 */
public abstract class Accessor {

	private final World world;
	private final PlayerEntity player;
	private final CompoundNBT serverData;
	private final RayTraceResult hit;
	private final boolean serverConnected;

	private TooltipPosition tooltipPosition;

	public Accessor(World world, PlayerEntity player, CompoundNBT serverData, RayTraceResult hit, boolean serverConnected) {
		this.world = world;
		this.player = player;
		this.serverData = serverData;
		this.hit = hit;
		this.serverConnected = serverConnected;
	}

	public World getWorld() {
		return world;
	}

	public PlayerEntity getPlayer() {
		return player;
	}

	public CompoundNBT getServerData() {
		return serverData == null ? new CompoundNBT() : serverData;
	}

	public RayTraceResult getHitResult() {
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
