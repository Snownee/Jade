package snownee.jade.addon.vanilla;

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.JadeUI;

public class BrewingStandProvider implements StreamServerDataProvider<BlockAccessor, BrewingStandProvider.Data> {
	public static final BrewingStandProvider INSTANCE = new BrewingStandProvider();

	@Override
	public boolean shouldRequestData(BlockAccessor accessor) {
		return accessor.getBlockEntity() instanceof BrewingStandBlockEntity;
	}

	@Override
	@Nullable
	public Data streamData(BlockAccessor accessor) {
		if (!(accessor.getBlockEntity() instanceof BrewingStandBlockEntity brewingStand)) {
			return null;
		}
		return new Data(brewingStand.fuel, brewingStand.brewTime);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
		return Data.STREAM_CODEC.cast();
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_BREWING_STAND;
	}

	public record Data(int fuel, int time) {
		public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				Data::fuel,
				ByteBufCodecs.VAR_INT,
				Data::time,
				Data::new);
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			Data data = BrewingStandProvider.INSTANCE.decodeFromData(accessor).orElse(null);
			if (data == null) {
				return;
			}
			tooltip.add(JadeUI.smallItem(new ItemStack(Items.BLAZE_POWDER)).narration(""));
			tooltip.append(JadeUI.text(IThemeHelper.get().info(data.fuel))
					.narration(Component.translatable("narration.jade.brewingStand.fuel", data.fuel)));
			if (data.time > 0) {
				tooltip.append(JadeUI.spacer(5, 0));
				tooltip.append(JadeUI.smallItem(new ItemStack(Items.CLOCK)).narration(""));
				tooltip.append(IThemeHelper.get().seconds(data.time, accessor.tickRate()));
			}
		}

		@Override
		public Identifier getUid() {
			return JadeIds.MC_BREWING_STAND;
		}
	}
}
