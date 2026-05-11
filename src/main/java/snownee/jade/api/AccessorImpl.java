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
 * A generic class to get basic information of target and context.
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

	@Override
	public <D> Optional<D> decodeFromNbt(StreamDecoder<RegistryFriendlyByteBuf, D> codec, Tag tag) {
		try {
			RegistryFriendlyByteBuf buffer = buffer();
			buffer.writeBytes(((ByteArrayTag) tag).getAsByteArray());
			D decoded = codec.decode(buffer);
			return Optional.of(decoded);
		} catch (Exception e) {
			return Optional.empty();
		} finally {
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

	public void setServersideRep(ItemStack serversideRep) {
		this.serversideRep = serversideRep;
	}
}
