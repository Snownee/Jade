package snownee.jade.addon.debug;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

public class EntityLootTableProvider implements StreamServerDataProvider<EntityAccessor, Identifier> {
	public static final EntityLootTableProvider INSTANCE = new EntityLootTableProvider();

	@Override
	public @Nullable Identifier streamData(EntityAccessor accessor) {
		if (!shouldRequestData(accessor)) {
			return null;
		}
		if (accessor.getEntity().getLootTable().isPresent()) {
			return accessor.getEntity().getLootTable().get().identifier();
		}
		return null;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Identifier> streamCodec() {
		return Identifier.STREAM_CODEC.cast();
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		return accessor.getPlayer().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public static class Client extends EntityLootTableProvider implements IEntityComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			//noinspection SimplifyOptionalCallChains
			Identifier lootTable = decodeFromData(accessor).orElse(null);
			if (lootTable != null) {
				tooltip.add(Component.translatable("jade.lootTable", lootTable));
			}
		}

		@Override
		public boolean enabledByDefault() {
			return false;
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.DEBUG_LOOT_TABLE;
	}
}
