package snownee.jade.addon.vanilla;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
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
import snownee.jade.api.ui.IElementHelper;

public enum BrewingStandProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, BrewingStandProvider.Data> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Data data = decodeFromData(accessor).orElse(null);
		if (data == null) {
			return;
		}
		IElementHelper helper = IElementHelper.get();
		tooltip.add(helper.smallItem(new ItemStack(Items.BLAZE_POWDER)).message(null));
		tooltip.append(helper.text(IThemeHelper.get().info(data.fuel)).message(I18n.get("narration.jade.brewingStand.fuel", data.fuel)));
		if (data.time > 0) {
			tooltip.append(helper.spacer(5, 0));
			tooltip.append(helper.smallItem(new ItemStack(Items.CLOCK)).message(" "));
			tooltip.append(IThemeHelper.get().seconds(data.time, accessor.tickRate()));
		}
	}

	@Override
	public Data streamData(BlockAccessor accessor) {
		BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) accessor.getBlockEntity();
		return new Data(brewingStand.fuel, brewingStand.brewTime);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
		return Data.STREAM_CODEC.cast();
	}

	@Override
	public ResourceLocation getUid() {
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
}
