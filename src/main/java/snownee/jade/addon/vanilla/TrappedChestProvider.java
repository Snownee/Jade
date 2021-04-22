package snownee.jade.addon.vanilla;

import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.addons.core.HUDHandlerBlocks;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITaggableList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.JadePlugin;

public class TrappedChestProvider implements IComponentProvider {

	public static final TrappedChestProvider INSTANCE = new TrappedChestProvider();

	private static final Cache<Block, ITextComponent> CACHE = CacheBuilder.newBuilder().build();
	private static final ITextComponent DEFAULT_NAME = new TranslationTextComponent(Blocks.CHEST.getTranslationKey());

	@Override
	public void appendHead(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.TRAPPED_CHEST)) {
			return;
		}
		try {
			ITextComponent name = CACHE.get(accessor.getBlock(), () -> {
				ResourceLocation trappedName = accessor.getBlock().getRegistryName();
				if (trappedName.getPath().startsWith("trapped_")) {
					ResourceLocation chestName = new ResourceLocation(trappedName.getNamespace(), trappedName.getPath().substring(8));
					Block block = ForgeRegistries.BLOCKS.getValue(chestName);
					if (block != null) {
						return block.getTranslatedName();
					}
				}
				return DEFAULT_NAME;
			});
			((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(HUDHandlerBlocks.OBJECT_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), name.getString())));
		} catch (Exception e) {
		}
	}

}
