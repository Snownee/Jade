package snownee.jade.addon.vanilla;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

public class FurnaceProvider implements StreamServerDataProvider<BlockAccessor, FurnaceProvider.Data> {
	public static final FurnaceProvider INSTANCE = new FurnaceProvider();

	@Override
	public Data streamData(BlockAccessor accessor) {
		AbstractFurnaceBlockEntity furnace = accessor.typedBlockEntity();
		return new Data(
				furnace.cookingTimer,
				furnace.cookingTotalTime,
				List.of(furnace.getItem(0), furnace.getItem(1), furnace.getItem(2)));
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
		return Data.STREAM_CODEC;
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_FURNACE;
	}

	public record Data(int progress, int total, List<ItemStack> inventory) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				Data::progress,
				ByteBufCodecs.VAR_INT,
				Data::total,
				ItemStack.OPTIONAL_LIST_STREAM_CODEC,
				Data::inventory,
				Data::new);
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			Data data = FurnaceProvider.INSTANCE.decodeFromData(accessor).orElse(null);
			if (data == null) {
				return;
			}
			tooltip.add(JadeUI.item(data.inventory.get(0)).alignSelfCenter());
			tooltip.append(JadeUI.item(data.inventory.get(1)).alignSelfCenter());
			tooltip.append(JadeUI.progressArrow(data.total == 0 ? 0 : (float) data.progress / data.total).alignSelfCenter().settings($ -> {
				return $.paddingHorizontal(3);
			}));
			tooltip.append(JadeUI.item(data.inventory.get(2)).alignSelfCenter());
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_FURNACE;
		}
	}

}
