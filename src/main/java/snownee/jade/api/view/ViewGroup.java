package snownee.jade.api.view;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * Logical group of views that should be rendered together.
 *
 * @param <T> contained view type
 */
public class ViewGroup<T> {
	/**
	 * Returns a codec for a single view group.
	 *
	 * @param viewCodec codec for individual views
	 * @param <B> buffer type
	 * @param <T> view type
	 * @return view-group codec
	 */
	public static <B extends ByteBuf, T> StreamCodec<B, ViewGroup<T>> codec(StreamCodec<B, T> viewCodec) {
		return StreamCodec.composite(
				ByteBufCodecs.<B, T>list().apply(viewCodec),
				$ -> $.views,
				ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
				$ -> Optional.ofNullable($.id),
				ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG),
				$ -> Optional.ofNullable($.extraData),
				ViewGroup::new);
	}

	/**
	 * Returns a codec for a named list of view groups.
	 *
	 * @param viewCodec codec for individual views
	 * @param <B> buffer type
	 * @param <T> view type
	 * @return named list codec
	 */
	public static <B extends ByteBuf, T> StreamCodec<B, Map.Entry<Identifier, List<ViewGroup<T>>>> listCodec(StreamCodec<B, T> viewCodec) {
		return StreamCodec.composite(
				Identifier.STREAM_CODEC,
				Map.Entry::getKey,
				ByteBufCodecs.<B, ViewGroup<T>>list().apply(codec(viewCodec)),
				Map.Entry::getValue,
				Map::entry);
	}

	/**
	 * Views in this group.
	 */
	public List<T> views;
	/**
	 * Optional group identifier.
	 */
	@Nullable
	public String id;
	/**
	 * Optional extra rendering data.
	 */
	@Nullable
	protected CompoundTag extraData;

	/**
	 * Creates a group with no explicit id or extra data.
	 *
	 * @param views contained views
	 */
	public ViewGroup(List<T> views) {
		this(views, Optional.empty(), Optional.empty());
	}

	/**
	 * Creates a group with optional id and extra data.
	 *
	 * @param views contained views
	 * @param id optional group id
	 * @param extraData optional extra data
	 */
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public ViewGroup(List<T> views, Optional<String> id, Optional<CompoundTag> extraData) {
		this.views = views;
		this.id = id.orElse(null);
		this.extraData = extraData.orElse(null);
	}

	/**
	 * Returns the mutable extra data tag.
	 *
	 * @return extra data tag
	 */
	public CompoundTag getExtraData() {
		if (extraData == null) {
			extraData = new CompoundTag();
		}
		return extraData;
	}

	/**
	 * Stores the render progress in the extra data tag.
	 *
	 * @param progress progress value in the {@code 0..1} range
	 */
	public void setProgress(float progress) {
		getExtraData().putFloat("Progress", progress);
	}
}
