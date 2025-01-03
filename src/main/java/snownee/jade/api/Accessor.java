package snownee.jade.api;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * A generic class to get basic information of target and context.
 */
public interface Accessor<T extends HitResult> {

	Level getLevel();

	Player getPlayer();

	@NotNull
	CompoundTag getServerData();

	@Deprecated
	DynamicOps<Tag> nbtOps();

	@Deprecated
	<D> Optional<D> readData(MapDecoder<D> codec);

	@Deprecated
	<D> void writeData(MapEncoder<D> codec, D value);

	<D> Optional<D> decodeFromNbt(StreamDecoder<RegistryFriendlyByteBuf, D> codec, Tag tag);

	<D> Tag encodeAsNbt(StreamEncoder<RegistryFriendlyByteBuf, D> codec, D value);

	T getHitResult();

	/**
	 * @return {@code true} if the dedicated server has Jade installed.
	 */
	boolean isServerConnected();

	ItemStack getPickedResult();

	boolean showDetails();

	@Nullable
	Object getTarget();

	Class<? extends Accessor<?>> getAccessorType();

	boolean verifyData(CompoundTag data);

	float tickRate();
}
