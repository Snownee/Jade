package snownee.jade.api;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Describes the current Jade target and its surrounding context.
 *
 * @param <T> the hit result type used to identify the target
 */
public interface Accessor<T extends HitResult> {

	/**
	 * Returns the level the target is being queried in.
	 *
	 * @return the current level
	 */
	Level getLevel();

	/**
	 * Returns the player currently using Jade.
	 *
	 * @return the client or server player
	 */
	Player getPlayer();

	/**
	 * Returns server-synchronized data for this target.
	 *
	 * @return the synchronized tag, never {@code null}
	 */
	CompoundTag getServerData();

	/**
	 * Replaces the synchronized server data.
	 *
	 * <p>This method is internal and should not be called by addons.
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	void setServerData(@Nullable CompoundTag serverData);

	/**
	 * Decodes a value from a tag created with {@link #encodeAsNbt(StreamEncoder, Object)}.
	 *
	 * @param codec the codec to use
	 * @param tag the encoded tag
	 * @param <D> decoded type
	 * @return the decoded value, or {@link Optional#empty()} if decoding fails
	 */
	<D> Optional<D> decodeFromNbt(StreamDecoder<RegistryFriendlyByteBuf, D> codec, Tag tag);

	/**
	 * Encodes a value into a tag suitable for server-data transport.
	 *
	 * @param codec the codec to use
	 * @param value the value to encode
	 * @param <D> encoded type
	 * @return the encoded tag
	 */
	<D> Tag encodeAsNbt(StreamEncoder<RegistryFriendlyByteBuf, D> codec, D value);

	/**
	 * Returns the underlying hit result.
	 *
	 * @return the current hit result
	 */
	T getHitResult();

	/**
	 * Returns whether the dedicated server has Jade installed.
	 *
	 * @return {@code true} if the dedicated server is connected to Jade
	 */
	boolean isServerConnected();

	/**
	 * Returns the item stack shown as the target's pick result.
	 *
	 * @return the display stack
	 */
	ItemStack getPickedResult();

	/**
	 * Returns whether detailed target information should be shown.
	 *
	 * @return {@code true} to show the detailed tooltip variant
	 */
	boolean showDetails();

	/**
	 * Returns the raw target object, if one exists.
	 *
	 * @return the target object or {@code null}
	 */
	@Nullable
	Object getTarget();

	/**
	 * Returns the concrete accessor type.
	 *
	 * @return the accessor class
	 */
	Class<? extends Accessor<?>> getAccessorType();

	/**
	 * Verifies a server data payload against the current target.
	 *
	 * @param data the data to verify
	 * @return {@code true} if the data matches this accessor
	 */
	boolean verifyData(CompoundTag data);

	/**
	 * Returns whether the accessor requires verification.
	 *
	 * @return {@code true} if verification is needed
	 */
	boolean shouldVerifyData();

	/**
	 * Returns the current tick rate of the world.
	 *
	 * @return the world tick rate
	 */
	float tickRate();

	/**
	 * Returns whether the target represents server-side content.
	 *
	 * @return {@code true} if the target has a non-empty server-side representation
	 */
	default boolean isServersideContent() {
		return !getServersideRep().isEmpty();
	}

	/**
	 * Returns the server-side representation of the target, if one is available.
	 *
	 * @return the server-side item stack, or {@link ItemStack#EMPTY}
	 */
	default ItemStack getServersideRep() {
		return ItemStack.EMPTY;
	}
}
