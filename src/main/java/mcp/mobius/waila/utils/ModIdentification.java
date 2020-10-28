package mcp.mobius.waila.utils;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.util.Map;

public class ModIdentification {

    private static final Map<String, Info> CONTAINER_CACHE = Maps.newHashMap();
    private static final Info MC_MOD_INFO = new Info("minecraft", "Minecraft");
    static {
        CONTAINER_CACHE.put(MC_MOD_INFO.getId(), MC_MOD_INFO);
    }

    public static Info getModInfo(String namespace) {
        return CONTAINER_CACHE.computeIfAbsent(namespace, s -> ModList.get().getMods().stream()
                .filter(c -> c.getModId().equals(s))
                .map(c -> new Info(c.getModId(), c.getDisplayName()))
                .findFirst()
                .orElse(new ModIdentification.Info(s, s)));
    }

    public static Info getModInfo(ResourceLocation id) {
        return getModInfo(id.getNamespace());
    }

    public static Info getModInfo(Block block) {
        return getModInfo(block.getRegistryName());
    }

    public static Info getModInfo(ItemStack stack) {
        return getModInfo(stack.getItem().getCreatorModId(stack));
    }

    public static Info getModInfo(Item item) {
        return getModInfo(new ItemStack(item));
    }

    public static Info getModInfo(Entity entity) {
        ResourceLocation registryName = entity.getType().getRegistryName();
        return registryName == null ? MC_MOD_INFO : getModInfo(registryName);
    }

    public static class Info {
        private final String id;
        private final String name;

        public Info(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}