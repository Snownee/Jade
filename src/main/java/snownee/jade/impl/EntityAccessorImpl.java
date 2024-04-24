package snownee.jade.impl;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Suppliers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import snownee.jade.api.AccessorImpl;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.network.ServerPayloadContext;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.WailaExceptionHandler;

/**
 * Class to get information of entity target and context.
 */
public class EntityAccessorImpl extends AccessorImpl<EntityHitResult> implements EntityAccessor {

	private final Supplier<Entity> entity;

	public EntityAccessorImpl(Builder builder) {
		super(builder.level, builder.player, builder.serverData, builder.hit, builder.connected, builder.showDetails);
		entity = builder.entity;
	}

	public static void handleRequest(SyncData data, ServerPayloadContext context, Consumer<CompoundTag> responseSender) {
		ServerPlayer player = context.player();
		context.execute(() -> {
			EntityAccessor accessor = data.unpack(player);
			if (accessor == null) {
				return;
			}
			Entity entity = accessor.getEntity();
			double maxDistance = Mth.square(player.entityInteractionRange() + 21);
			if (entity == null || player.distanceToSqr(entity) > maxDistance) {
				return;
			}
			List<IServerDataProvider<EntityAccessor>> providers = WailaCommonRegistration.instance().getEntityNBTProviders(entity);
			if (providers.isEmpty()) {
				return;
			}

			CompoundTag tag = accessor.getServerData();
			for (IServerDataProvider<EntityAccessor> provider : providers) {
				try {
					provider.appendServerData(tag, accessor);
				} catch (Exception e) {
					WailaExceptionHandler.handleErr(e, provider, null);
				}
			}

			tag.putInt("EntityId", entity.getId());
			responseSender.accept(tag);
		});
	}

	@Override
	public Entity getEntity() {
		return CommonProxy.wrapPartEntityParent(getRawEntity());
	}

	@Override
	public Entity getRawEntity() {
		return entity.get();
	}

	@Override
	public ItemStack getPickedResult() {
		return CommonProxy.getEntityPickedResult(entity.get(), getPlayer(), getHitResult());
	}

	@NotNull
	@Override
	public Object getTarget() {
		return getEntity();
	}

	@Override
	public boolean verifyData(CompoundTag data) {
		if (!verify) {
			return true;
		}
		if (!data.contains("EntityId")) {
			return false;
		}
		return data.getInt("EntityId") == getEntity().getId();
	}

	public static class Builder implements EntityAccessor.Builder {

		public boolean showDetails;
		private Level level;
		private Player player;
		private CompoundTag serverData;
		private boolean connected;
		private Supplier<EntityHitResult> hit;
		private Supplier<Entity> entity;
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
		public Builder hit(Supplier<EntityHitResult> hit) {
			this.hit = hit;
			return this;
		}

		@Override
		public Builder entity(Supplier<Entity> entity) {
			this.entity = entity;
			return this;
		}

		@Override
		public Builder from(EntityAccessor accessor) {
			level = accessor.getLevel();
			player = accessor.getPlayer();
			serverData = accessor.getServerData();
			connected = accessor.isServerConnected();
			showDetails = accessor.showDetails();
			hit = accessor::getHitResult;
			entity = accessor::getEntity;
			return this;
		}

		@Override
		public EntityAccessor.Builder requireVerification() {
			verify = true;
			return this;
		}

		@Override
		public EntityAccessor build() {
			EntityAccessorImpl accessor = new EntityAccessorImpl(this);
			if (verify) {
				accessor.requireVerification();
			}
			return accessor;
		}
	}

	public record SyncData(boolean showDetails, int id, int partIndex, Vec3 hitVec) {
		public static final StreamCodec<RegistryFriendlyByteBuf, SyncData> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL,
				SyncData::showDetails,
				ByteBufCodecs.VAR_INT,
				SyncData::id,
				ByteBufCodecs.VAR_INT,
				SyncData::partIndex,
				ByteBufCodecs.VECTOR3F.map(Vec3::new, Vec3::toVector3f),
				SyncData::hitVec,
				SyncData::new
		);

		public SyncData(EntityAccessor accessor) {
			this(
					accessor.showDetails(),
					accessor.getEntity().getId(),
					CommonProxy.getPartEntityIndex(accessor.getRawEntity()),
					accessor.getHitResult().getLocation());
		}

		public EntityAccessor unpack(ServerPlayer player) {
			Supplier<Entity> entity = Suppliers.memoize(() -> CommonProxy.getPartEntity(player.level().getEntity(id), partIndex));
			return new EntityAccessorImpl.Builder()
					.level(player.level())
					.player(player)
					.showDetails(showDetails)
					.entity(entity)
					.hit(Suppliers.memoize(() -> new EntityHitResult(entity.get(), hitVec)))
					.build();
		}
	}
}
