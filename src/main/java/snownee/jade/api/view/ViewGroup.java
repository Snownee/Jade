package snownee.jade.api.view;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ViewGroup<T> {

	public final List<T> views;
	@Nullable
	public String id;
	@Nullable
	protected CompoundTag extraData;

	public ViewGroup(List<T> views) {
		this.views = views;
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
		if (groups == null || groups.isEmpty()) {
			return false;
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
			return true;
		}
		return false;
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
