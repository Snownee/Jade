package snownee.jade.addon.vanilla;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum BeehiveProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, Byte> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		int level = state.getValue(BeehiveBlock.HONEY_LEVEL); // 0~5
		IThemeHelper t = IThemeHelper.get();
		MutableComponent value = Component.translatable("jade.fraction", level, 5);
		tooltip.add(Component.translatable("jade.beehive.honey", level == 5 ? t.success(value) : t.info(value)));
		Byte b = decodeFromData(accessor).orElse(null);
		if (b == null) {
			return;
		}
		boolean full = b > 0;
		int bees = Math.abs(b);
		tooltip.add(Component.translatable("jade.beehive.bees", full ? t.success(bees) : t.info(bees)));
	}

	@Override
	public Byte streamData(BlockAccessor accessor) {
		BeehiveBlockEntity beehive = (BeehiveBlockEntity) accessor.getBlockEntity();
		int bees = beehive.getOccupantCount();
		return (byte) (beehive.isFull() ? bees : -bees);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Byte> streamCodec() {
		return ByteBufCodecs.BYTE.cast();
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_BEEHIVE;
	}
}
