package mcp.mobius.waila.api;

import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * A generic class to get basic information of target and context.
 */
public interface Accessor<T extends HitResult> {

	Level getLevel();

	Player getPlayer();

	CompoundTag getServerData();

	T getHitResult();

	/**
	 * Returns true if dedicated server has Jade installed.
	 */
	boolean isServerConnected();

	/**
	 * Get {@link TooltipPosition} the {@link ITooltip} currently gathering
	 */
	TooltipPosition getTooltipPosition();

	ItemStack getPickedResult();

	boolean shouldDisplay();

	boolean shouldRequestData();

	@Deprecated
	void _requestData(boolean showDetails);

	@Deprecated
	boolean _verifyData(CompoundTag serverData);

	@Deprecated
	IElement _getIcon();

	@Deprecated
	void _gatherComponents(ITooltip tooltip);

	@Deprecated
	void _setTooltipPosition(TooltipPosition tooltipPosition);

	@Deprecated
	Object _getTrackObject();
}
