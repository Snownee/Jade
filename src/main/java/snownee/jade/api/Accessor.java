package snownee.jade.api;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import snownee.jade.api.ui.IElement;

/**
 * A generic class to get basic information of target and context.
 */
public interface Accessor<T extends HitResult> {

	Level getLevel();

	Player getPlayer();

	@NotNull
	CompoundTag getServerData();

	T getHitResult();

	/**
	 * Returns true if dedicated server has Jade installed.
	 */
	boolean isServerConnected();

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
	void _gatherComponents(Function<IJadeProvider, ITooltip> tooltipProvider);

	@Deprecated
	Object _getTrackObject();

}
