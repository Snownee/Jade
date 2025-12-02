package snownee.jade.addon.vanilla;

import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.util.ClientProxy;

public class AnimalOwnerProvider implements StreamServerDataProvider<EntityAccessor, Component> {
	public static final AnimalOwnerProvider INSTANCE = new AnimalOwnerProvider();

	@Override
	@Nullable
	public Component streamData(EntityAccessor accessor) {
		ServerLevel level = (ServerLevel) accessor.getLevel();
		UUID uuid = getOwnerUUID(accessor.getEntity());
		Entity entity = uuid == null ? null : level.getEntity(uuid);
		if (entity != null) {
			return ObjectNameProvider.getEntityName(entity, false);
		}
		String name = lookupPlayerName(uuid, level.getServer().services());
		return name == null ? null : Component.literal(name);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Component> streamCodec() {
		return ComponentSerialization.STREAM_CODEC;
	}

	@Nullable
	public static UUID getOwnerUUID(Entity entity) {
		if (entity instanceof OwnableEntity ownableEntity) {
			EntityReference<LivingEntity> reference = ownableEntity.getOwnerReference();
			if (reference != null) {
				return reference.getUUID();
			}
		}
		return null;
	}

	@Nullable
	public static String lookupPlayerName(@Nullable UUID uuid, Services services) {
		if (uuid == null) {
			return null;
		}
		String name = services.nameToIdCache().get(uuid).map(NameAndId::name).orElse(null);
		if (name != null) {
			return name;
		}
		GameProfile profile = services.profileResolver().fetchById(uuid).orElse(null);
		return profile == null ? null : profile.name();
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		Entity entity = accessor.getEntity();
		return entity instanceof OwnableEntity && getOwnerUUID(entity) == null;
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_ANIMAL_OWNER;
	}

	public static class Client implements IEntityComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			Component name = AnimalOwnerProvider.INSTANCE.decodeFromData(accessor).orElse(null);
			if (name == null) {
				UUID uuid = getOwnerUUID(accessor.getEntity());
				if (uuid == null) {
					return;
				}
				name = Component.literal(Optional.ofNullable(ClientProxy.lookupPlayerName(uuid)).orElse("???"));
			}
			tooltip.add(Component.translatable("jade.owner", name));
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_ANIMAL_OWNER;
		}
	}

}
