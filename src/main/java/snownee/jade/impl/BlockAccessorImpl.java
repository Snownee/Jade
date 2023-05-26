package snownee.jade.impl;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.Jade;
import snownee.jade.api.AccessorImpl;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.WailaExceptionHandler;

/**
 * Class to get information of block target and context.
 */
public class BlockAccessorImpl extends AccessorImpl<BlockHitResult> implements BlockAccessor {

	private final BlockState blockState;
	@Nullable
	private final Supplier<BlockEntity> blockEntity;
	private ItemStack fakeBlock;

	private BlockAccessorImpl(Builder builder) {
		super(builder.level, builder.player, builder.serverData, () -> builder.hit, builder.connected, builder.showDetails);
		blockState = builder.blockState;
		blockEntity = builder.blockEntity;
		fakeBlock = builder.fakeBlock;
	}

	public static void handleRequest(FriendlyByteBuf buf, ServerPlayer player, Consumer<Runnable> executor, Consumer<CompoundTag> responseSender) {
		BlockAccessor accessor;
		try {
			accessor = fromNetwork(buf, player);
		} catch (Exception e) {
			WailaExceptionHandler.handleErr(e, null, null);
			return;
		}
		executor.accept(() -> {
			BlockPos pos = accessor.getPosition();
			ServerLevel world = player.serverLevel();
			if (pos.distSqr(player.blockPosition()) > Jade.MAX_DISTANCE_SQR || !world.isLoaded(pos))
				return;

			BlockEntity tile = accessor.getBlockEntity();
			if (tile == null)
				return;

			List<IServerDataProvider<BlockAccessor>> providers = WailaCommonRegistration.INSTANCE.getBlockNBTProviders(tile);
			if (providers.isEmpty())
				return;

			CompoundTag tag = accessor.getServerData();
			for (IServerDataProvider<BlockAccessor> provider : providers) {
				try {
					provider.appendServerData(tag, accessor);
				} catch (Exception e) {
					WailaExceptionHandler.handleErr(e, provider, null);
				}
			}

			tag.putInt("x", pos.getX());
			tag.putInt("y", pos.getY());
			tag.putInt("z", pos.getZ());
			tag.putString("id", CommonProxy.getId(tile.getType()).toString());
			responseSender.accept(tag);
		});
	}

	public static BlockAccessor fromNetwork(FriendlyByteBuf buf, ServerPlayer player) {
		Builder builder = new Builder();
		builder.level(player.level());
		builder.player(player);
		builder.showDetails(buf.readBoolean());
		builder.hit(buf.readBlockHitResult());
		builder.blockState(Block.stateById(buf.readVarInt()));
		builder.fakeBlock(buf.readItem());
		if (builder.blockState.hasBlockEntity()) {
			// you can only get block entity from the main thread
			builder.blockEntity(Suppliers.memoize(() -> builder.level.getBlockEntity(builder.hit.getBlockPos())));
		}
		return builder.build();
	}

	@Override
	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeBoolean(showDetails());
		buf.writeBlockHitResult(getHitResult());
		buf.writeVarInt(Block.getId(blockState));
		buf.writeItem(fakeBlock);
	}

	@Override
	public Block getBlock() {
		return getBlockState().getBlock();
	}

	@Override
	public BlockState getBlockState() {
		return blockState;
	}

	@Override
	public BlockEntity getBlockEntity() {
		return blockEntity == null ? null : blockEntity.get();
	}

	@Override
	public BlockPos getPosition() {
		return getHitResult().getBlockPos();
	}

	@Override
	public Direction getSide() {
		return getHitResult().getDirection();
	}

	@Override
	public ItemStack getPickedResult() {
		return CommonProxy.getBlockPickedResult(blockState, getPlayer(), getHitResult());
	}

	@Override
	public Object getTarget() {
		return getBlockEntity();
	}

	@Override
	public boolean isFakeBlock() {
		return !fakeBlock.isEmpty();
	}

	@Override
	public ItemStack getFakeBlock() {
		return fakeBlock;
	}

	public void setFakeBlock(ItemStack fakeBlock) {
		this.fakeBlock = fakeBlock;
	}

	public static class Builder implements BlockAccessor.Builder {

		private Level level;
		private Player player;
		private CompoundTag serverData;
		private boolean connected;
		private boolean showDetails;
		private BlockHitResult hit;
		private BlockState blockState = Blocks.AIR.defaultBlockState();
		private Supplier<BlockEntity> blockEntity;
		private ItemStack fakeBlock = ItemStack.EMPTY;

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
		public Builder blockState(BlockState blockState) {
			this.blockState = blockState;
			return this;
		}

		@Override
		public Builder blockEntity(Supplier<BlockEntity> blockEntity) {
			this.blockEntity = blockEntity;
			return this;
		}

		@Override
		public Builder fakeBlock(ItemStack stack) {
			fakeBlock = stack;
			return this;
		}

		@Override
		public Builder from(BlockAccessor accessor) {
			level = accessor.getLevel();
			player = accessor.getPlayer();
			serverData = accessor.getServerData();
			connected = accessor.isServerConnected();
			showDetails = accessor.showDetails();
			hit = accessor.getHitResult();
			blockEntity = accessor::getBlockEntity;
			blockState = accessor.getBlockState();
			fakeBlock = accessor.getFakeBlock();
			return this;
		}

		@Override
		public BlockAccessor build() {
			return new BlockAccessorImpl(this);
		}

	}

}
