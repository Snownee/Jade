package snownee.jade.addon.vanilla;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.VanillaPlugin;

public class TrappedChestProvider implements IComponentProvider {

	public static final TrappedChestProvider INSTANCE = new TrappedChestProvider();

	private static final Cache<Block, ITextComponent> CACHE = CacheBuilder.newBuilder().build();
	private static final ITextComponent DEFAULT_NAME = new TranslationTextComponent(Blocks.CHEST.getTranslationKey());

	@Override
	public void append(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.TRAPPED_CHEST)) {
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
			tooltip.remove(CorePlugin.TAG_OBJECT_NAME); //FIXME
			tooltip.add(0, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), name.getString())), CorePlugin.TAG_OBJECT_NAME);
		} catch (Exception e) {
		}
	}

}
