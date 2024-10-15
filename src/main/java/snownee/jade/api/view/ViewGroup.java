package snownee.jade.api.view;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class ViewGroup<T> {
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

	public static <B extends ByteBuf, T> StreamCodec<B, Map.Entry<ResourceLocation, List<ViewGroup<T>>>> listCodec(StreamCodec<B, T> viewCodec) {
		return StreamCodec.composite(
				ResourceLocation.STREAM_CODEC,
				Map.Entry::getKey,
				ByteBufCodecs.<B, ViewGroup<T>>list().apply(codec(viewCodec)),
				Map.Entry::getValue,
				Map::entry);
	}

	public List<T> views;
	@Nullable
	public String id;
	@Nullable
	protected CompoundTag extraData;

	public ViewGroup(List<T> views) {
		this(views, Optional.empty(), Optional.empty());
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public ViewGroup(List<T> views, Optional<String> id, Optional<CompoundTag> extraData) {
		this.views = views;
		this.id = id.orElse(null);
		this.extraData = extraData.orElse(null);
	}

	public void save(CompoundTag tag, Function<T, CompoundTag> writer) {
		ListTag list = new ListTag();
		for (var view : views) {
			list.add(writer.apply(view));
		}
		tag.put("Views", list);
		if (id != null) {
			tag.putString("Id", id);
		}
		if (extraData != null) {
			tag.put("Data", extraData);
		}
	}

	public static <T> ViewGroup<T> read(CompoundTag tag, Function<CompoundTag, T> reader) {
		ListTag list = tag.getList("Views", Tag.TAG_COMPOUND);
		List<T> views = Lists.newArrayList();
		for (var view : list) {
			views.add(reader.apply((CompoundTag) view));
		}
		ViewGroup<T> group = new ViewGroup<>(views);
		if (tag.contains("Id")) {
			group.id = tag.getString("Id");
		}
		if (tag.contains("Data")) {
			group.extraData = tag.getCompound("Data");
		}
		return group;
	}

	public static <T> boolean saveList(CompoundTag tag, String key, List<ViewGroup<T>> groups, Function<T, CompoundTag> writer) {
		if (groups == null) {
			return false;
		}
		if (groups.isEmpty()) {
			return true;
		}
		ListTag groupList = new ListTag();
		for (ViewGroup<T> group : groups) {
			if (group.views.isEmpty()) {
				continue;
			}
			CompoundTag groupTag = new CompoundTag();
			group.save(groupTag, writer);
			groupList.add(groupTag);
		}
		if (!groupList.isEmpty()) {
			tag.put(key, groupList);
		}
		return true;
	}

	@Nullable
	public static <T> List<ViewGroup<T>> readList(CompoundTag tag, String key, Function<CompoundTag, T> reader) {
		ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
		if (list.isEmpty()) {
			return null;
		}
		List<ViewGroup<T>> groups = Lists.newArrayList();
		for (var item : list) {
			ViewGroup<T> group = read((CompoundTag) item, reader);
			if (!group.views.isEmpty()) {
				groups.add(group);
			}
		}
		return groups;
	}

	public CompoundTag getExtraData() {
		if (extraData == null) {
			extraData = new CompoundTag();
		}
		return extraData;
	}

	public void setProgress(float progress) {
		getExtraData().putFloat("Progress", progress);
	}
}
