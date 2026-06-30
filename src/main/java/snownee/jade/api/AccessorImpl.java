package snownee.jade.api;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.ByteArrayTag;
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
 * Base implementation for Jade accessors.
 *
 * @param <T> hit result type handled by this accessor
 */
public abstract class AccessorImpl<T extends HitResult> implements Accessor<T> {

	private final Level level;
	private final Player player;
	private final Supplier<T> hit;
	private final boolean serverConnected;
	private final boolean showDetails;
	protected ItemStack serversideRep = ItemStack.EMPTY;
	private CompoundTag serverData;
	protected boolean verify;
	private @Nullable RegistryFriendlyByteBuf buffer;

	/**
	 * Creates a new accessor implementation.
	 *
	 * @param level current level
	 * @param player current player
	 * @param serverData synchronized server data, or {@code null}
	 * @param hit supplier for the target hit result
	 * @param serverConnected whether the dedicated server has Jade installed
	 * @param showDetails whether detailed target data should be shown
	 */
	public AccessorImpl(
			Level level,
			Player player,
			@Nullable CompoundTag serverData,
			Supplier<T> hit,
			boolean serverConnected,
			boolean showDetails) {
		this.level = Objects.requireNonNull(level);
		this.player = Objects.requireNonNull(player);
		this.hit = Objects.requireNonNull(hit);
		this.serverConnected = serverConnected;
		this.showDetails = showDetails;
		setServerData(serverData);
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
	public final CompoundTag getServerData() {
		return serverData;
	}

	/**
	 * Do not call this
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	@Override
	public final void setServerData(@Nullable CompoundTag serverData) {
		this.serverData = serverData == null ? new CompoundTag() : serverData;
	}

	private RegistryFriendlyByteBuf buffer() {
		if (buffer == null) {
			buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
		}
		buffer.clear();
		return buffer;
	}

	private static final ThreadLocal<Boolean> IN_JADE_DECODE = ThreadLocal.withInitial(() -> Boolean.FALSE);

	public static boolean jade$isInJadeDecode() {
		//noinspection PointlessBooleanExpression
		return IN_JADE_DECODE.get() == Boolean.TRUE;
	}

	@Override
	public <D> Optional<D> decodeFromNbt(StreamDecoder<RegistryFriendlyByteBuf, D> codec, Tag tag) {
		IN_JADE_DECODE.set(Boolean.TRUE);
		try {
			RegistryFriendlyByteBuf buffer = buffer();
			buffer.writeBytes(((ByteArrayTag) tag).getAsByteArray());
			D decoded = codec.decode(buffer);
			return Optional.of(decoded);
		} catch (Exception e) {
			return Optional.empty();
		} finally {
			IN_JADE_DECODE.set(Boolean.FALSE);
			if (buffer != null) {
				buffer.clear();
			}
		}
	}

	@Override
	public <D> Tag encodeAsNbt(StreamEncoder<RegistryFriendlyByteBuf, D> streamCodec, D value) {
		RegistryFriendlyByteBuf buffer = buffer();
		streamCodec.encode(buffer, value);
		ByteArrayTag tag = new ByteArrayTag(ArrayUtils.subarray(buffer.array(), 0, buffer.readableBytes()));
		buffer.clear();
		return tag;
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

	/**
	 * Marks this accessor as needing verification.
	 */
	public void requireVerification() {
		verify = true;
	}

	@Override
	public boolean shouldVerifyData() {
		return verify;
	}

	@Override
	public float tickRate() {
		return getLevel().tickRateManager().tickrate();
	}

	@Override
	public ItemStack getServersideRep() {
		return serversideRep;
	}

	/**
	 * Sets the server-side representation shown for this accessor.
	 *
	 * @param serversideRep replacement item stack
	 */
	public void setServersideRep(ItemStack serversideRep) {
		this.serversideRep = serversideRep;
	}
}
