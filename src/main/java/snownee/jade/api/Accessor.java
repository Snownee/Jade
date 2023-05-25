package snownee.jade.api;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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

	boolean showDetails();

	Object getTarget();

	Class<? extends Accessor<?>> getAccessorType();

	void toNetwork(FriendlyByteBuf buf);

	interface ClientHandler<T extends Accessor<?>> {

		boolean shouldDisplay(T accessor);

		boolean shouldRequestData(T accessor);

		void requestData(T accessor);

		boolean verifyData(T accessor);

		IElement getIcon(T accessor);

		void gatherComponents(T accessor, Function<IJadeProvider, ITooltip> tooltipProvider);
	}

}
