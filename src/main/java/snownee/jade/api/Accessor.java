package snownee.jade.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	@NotNull
	CompoundTag getServerData();

	T getHitResult();

	/**
	 * @return {@code true} if the dedicated server has Jade installed.
	 */
	boolean isServerConnected();

	ItemStack getPickedResult();

	boolean showDetails();

	@Nullable
	Object getTarget();

	Class<? extends Accessor<?>> getAccessorType();

	boolean verifyData(CompoundTag data);

}
