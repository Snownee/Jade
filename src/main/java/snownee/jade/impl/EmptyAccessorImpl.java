package snownee.jade.impl;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.AccessorImpl;
import snownee.jade.api.EmptyAccessor;

public class EmptyAccessorImpl extends AccessorImpl<BlockHitResult> implements EmptyAccessor {

	private EmptyAccessorImpl(Builder builder) {
		super(builder.level, builder.player, builder.serverData, Suppliers.ofInstance(builder.hit), builder.connected, builder.showDetails);
	}

	@Nullable
	@Override
	public Object getTarget() {
		return null;
	}

	@Override
	public boolean verifyData(CompoundTag data) {
		return true;
	}

	@Override
	public ItemStack getPickedResult() {
		return ItemStack.EMPTY;
	}

	public static class Builder implements EmptyAccessor.Builder {

		private Level level;
		private Player player;
		private CompoundTag serverData;
		private boolean connected;
		private boolean showDetails;
		private BlockHitResult hit;
		private boolean verify;

		@Override
		public Builder level(Level level) {
			this.level = level;
			return this;
		}

		@Override
		public Builder player(Player player) {
			this.player = player;
			return this;
		}

		@Override
		public Builder serverData(CompoundTag serverData) {
			this.serverData = serverData;
			return this;
		}

		@Override
		public Builder serverConnected(boolean connected) {
			this.connected = connected;
			return this;
		}

		@Override
		public Builder showDetails(boolean showDetails) {
			this.showDetails = showDetails;
			return this;
		}

		@Override
		public Builder hit(BlockHitResult hit) {
			this.hit = hit;
			return this;
		}

		@Override
		public Builder from(EmptyAccessor accessor) {
			level = accessor.getLevel();
			player = accessor.getPlayer();
			serverData = accessor.getServerData().copy();
			connected = accessor.isServerConnected();
			showDetails = accessor.showDetails();
			hit = accessor.getHitResult();
			return this;
		}

		@Override
		public EmptyAccessor.Builder requireVerification() {
			verify = true;
			return this;
		}

		@Override
		public EmptyAccessor build() {
			EmptyAccessorImpl accessor = new EmptyAccessorImpl(this);
			if (verify) {
				accessor.requireVerification();
			}
			return accessor;
		}
	}

	public record SyncData(boolean showDetails, BlockHitResult hit, CompoundTag data) {
		public static final StreamCodec<RegistryFriendlyByteBuf, SyncData> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL,
				SyncData::showDetails,
				StreamCodec.of(FriendlyByteBuf::writeBlockHitResult, FriendlyByteBuf::readBlockHitResult),
				SyncData::hit,
				ByteBufCodecs.COMPOUND_TAG,
				SyncData::data,
				SyncData::new
		);

		public SyncData(EmptyAccessor accessor) {
			this(accessor.showDetails(), accessor.getHitResult(), accessor.getServerData());
		}

		public EmptyAccessor unpack(ServerPlayer player) {
			return new Builder()
					.level(player.level())
					.player(player)
					.showDetails(showDetails)
					.hit(hit)
					.serverData(data)
					.build();
		}
	}
}
