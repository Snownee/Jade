package snownee.jade.test;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.StreamServerDataProvider;

public class ExampleDataProvider implements StreamServerDataProvider<BlockAccessor, Integer> {
	public static final ExampleDataProvider INSTANCE = new ExampleDataProvider();

	@Override
	public @Nullable Integer streamData(BlockAccessor accessor) {
		return ((AbstractFurnaceBlockEntity) accessor.getBlockEntity()).litTimeRemaining;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
		return ByteBufCodecs.VAR_INT.cast();
	}

	@Override
	public Identifier getUid() {
		return ExamplePlugin.UID_TEST_FUEL;
	}
}
