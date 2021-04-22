package mcp.mobius.waila.addons.minecraft;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.overlay.tooltiprenderers.TooltipRendererProgressBar;
import mcp.mobius.waila.overlay.tooltiprenderers.TooltipRendererSpacer;
import mcp.mobius.waila.overlay.tooltiprenderers.TooltipRendererStack;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.TargetBlock;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.LeashKnotEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.ResourceLocation;

@WailaPlugin
public class PluginMinecraft implements IWailaPlugin {

	static final ResourceLocation RENDER_ITEM = new ResourceLocation("item");
	static final ResourceLocation RENDER_SPACER = new ResourceLocation("spacer");
	static final ResourceLocation RENDER_FURNACE_PROGRESS = new ResourceLocation("furnace_progress");

	static final ResourceLocation CONFIG_DISPLAY_FURNACE = new ResourceLocation("display_furnace_contents");
	static final ResourceLocation CONFIG_HIDE_SILVERFISH = new ResourceLocation("hide_infestations");
	static final ResourceLocation CONFIG_SPAWNER_TYPE = new ResourceLocation("spawner_type");
	static final ResourceLocation CONFIG_CROP_PROGRESS = new ResourceLocation("crop_progress");
	static final ResourceLocation CONFIG_LEVER = new ResourceLocation("lever");
	static final ResourceLocation CONFIG_REPEATER = new ResourceLocation("repeater");
	static final ResourceLocation CONFIG_COMPARATOR = new ResourceLocation("comparator");
	static final ResourceLocation CONFIG_REDSTONE = new ResourceLocation("redstone");
	static final ResourceLocation CONFIG_JUKEBOX = new ResourceLocation("jukebox");

	@Override
	public void register(IRegistrar registrar) {
		registrar.addConfig(CONFIG_DISPLAY_FURNACE, true);
		registrar.addSyncedConfig(CONFIG_HIDE_SILVERFISH, true);
		registrar.addConfig(CONFIG_SPAWNER_TYPE, true);
		registrar.addConfig(CONFIG_CROP_PROGRESS, true);
		registrar.addConfig(CONFIG_LEVER, true);
		registrar.addConfig(CONFIG_REPEATER, true);
		registrar.addConfig(CONFIG_COMPARATOR, true);
		registrar.addConfig(CONFIG_REDSTONE, true);
		registrar.addConfig(CONFIG_JUKEBOX, true);

		registrar.registerTooltipRenderer(RENDER_ITEM, new TooltipRendererStack());
		registrar.registerTooltipRenderer(RENDER_SPACER, new TooltipRendererSpacer());
		registrar.registerTooltipRenderer(RENDER_FURNACE_PROGRESS, new TooltipRendererProgressBar());

		registrar.registerStackProvider(HUDHandlerVanilla.INSTANCE, SilverfishBlock.class);
		registrar.registerStackProvider(HUDHandlerVanilla.INSTANCE, CropsBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.HEAD, SilverfishBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.HEAD, MobSpawnerTileEntity.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, CropsBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, StemBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, CocoaBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, LeverBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, RepeaterBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, ComparatorBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, RedstoneWireBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, TargetBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, DaylightDetectorBlock.class);
		registrar.registerComponentProvider(HUDHandlerVanilla.INSTANCE, TooltipPosition.BODY, JukeboxTileEntity.class);
		registrar.registerBlockDataProvider(HUDHandlerVanilla.INSTANCE, JukeboxTileEntity.class);

		registrar.registerEntityStackProvider(HUDHandlerEntityIcon.INSTANCE, AbstractMinecartEntity.class);
		registrar.registerEntityStackProvider(HUDHandlerEntityIcon.INSTANCE, ItemFrameEntity.class);
		registrar.registerEntityStackProvider(HUDHandlerEntityIcon.INSTANCE, PaintingEntity.class);
		registrar.registerEntityStackProvider(HUDHandlerEntityIcon.INSTANCE, LeashKnotEntity.class);
		registrar.registerComponentProvider(HUDHandlerFurnace.INSTANCE, TooltipPosition.BODY, AbstractFurnaceTileEntity.class);
		registrar.registerBlockDataProvider(HUDHandlerFurnace.INSTANCE, AbstractFurnaceTileEntity.class);
	}
}
