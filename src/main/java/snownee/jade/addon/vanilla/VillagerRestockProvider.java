package snownee.jade.addon.vanilla;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.ConfigIcon;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class VillagerRestockProvider implements StreamServerDataProvider<EntityAccessor, VillagerRestockProvider.Data> {
	public static final VillagerRestockProvider INSTANCE = new VillagerRestockProvider();
	private static final int MAX_RESTOCKS_PER_DAY = 2;
	private static final long RESTOCK_COOLDOWN = 2400L;
	private static final long NEW_DAY_INTERVAL = 12000L;

	@Override
	public @Nullable Data streamData(EntityAccessor accessor) {
		Villager villager = (Villager) accessor.getEntity();
		boolean needsRestock = false;
		for (MerchantOffer offer : villager.getOffers()) {
			if (offer.getUses() > 0) {
				needsRestock = true;
				break;
			}
		}
		if (!needsRestock) {
			return null;
		}
		int restocksToday = villager.numberOfRestocksToday;
		long interval = restocksToday < MAX_RESTOCKS_PER_DAY ? RESTOCK_COOLDOWN : NEW_DAY_INTERVAL;
		long remaining = restocksToday == 0 ? 0L : Math.max(0L, villager.lastRestockGameTime + interval - villager.level().getGameTime());
		return new Data(restocksToday, (int) remaining);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
		return Data.STREAM_CODEC;
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_VILLAGER_RESTOCK;
	}

	public record Data(int restocksToday, int ticksUntilRestock) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				Data::restocksToday,
				ByteBufCodecs.VAR_INT,
				Data::ticksUntilRestock,
				Data::new);
	}

	public static class Client implements IEntityComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			Data data = VillagerRestockProvider.INSTANCE.decodeFromData(accessor).orElse(null);
			if (data == null) {
				return;
			}
			if (data.ticksUntilRestock() <= 0) {
				tooltip.add(Component.translatable("jade.villagerRestock.ready"));
			} else if (data.restocksToday() >= MAX_RESTOCKS_PER_DAY) {
				tooltip.add(Component.translatable(
						"jade.villagerRestock.limit",
						IThemeHelper.get().seconds(data.ticksUntilRestock(), accessor.tickRate())));
			} else {
				tooltip.add(Component.translatable(
						"jade.villagerRestock.time",
						IThemeHelper.get().seconds(data.ticksUntilRestock(), accessor.tickRate())));
			}
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_VILLAGER_RESTOCK;
		}

		@Override
		public ConfigIcon getConfigIcon() {
			return ConfigIcon.item(Items.EMERALD);
		}

		@Override
		public boolean enabledByDefault() {
			return false;
		}
	}
}
