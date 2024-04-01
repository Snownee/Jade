package snownee.jade.api.config;

import java.util.List;

import net.minecraft.resources.ResourceKey;

public class IgnoreList<T> {
	public List<ResourceKey<T>> values = List.of();
	public int version = 1;
}
