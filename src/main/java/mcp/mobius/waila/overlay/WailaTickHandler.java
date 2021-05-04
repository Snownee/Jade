package mcp.mobius.waila.overlay;

import java.util.List;

import com.mojang.text2speech.Narrator;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ITaggableList;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.MetaDataProvider;
import mcp.mobius.waila.api.impl.TaggableList;
import mcp.mobius.waila.api.impl.TaggedTextComponent;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import mcp.mobius.waila.api.impl.config.WailaConfig.ConfigGeneral;
import mcp.mobius.waila.gui.GuiOptions;
import mcp.mobius.waila.network.MessageRequestEntity;
import mcp.mobius.waila.network.MessageRequestTile;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextProcessing;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Waila.MODID, value = Dist.CLIENT)
public class WailaTickHandler {

	public static WailaTickHandler INSTANCE = new WailaTickHandler();
	private static Narrator narrator;
	private static String lastNarration = "";
	public Tooltip tooltip = null;
	public MetaDataProvider handler = new MetaDataProvider();

	public void tickClient() {
		ConfigGeneral config = Waila.CONFIG.get().getGeneral();
		if (!config.shouldDisplayTooltip()) {
			tooltip = null;
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (!(client.currentScreen instanceof GuiOptions)) {
			if (client.isGamePaused() || client.currentScreen != null || client.keyboardListener == null) {
				return;
			}
		}
		World world = client.world;
		PlayerEntity player = client.player;

		if (world == null || player == null) {
			tooltip = null;
			return;
		}
		RayTracing.INSTANCE.fire();
		RayTraceResult target = RayTracing.INSTANCE.getTarget();

		List<ITextComponent> currentTip = new TaggableList<>(TaggedTextComponent::new);
		List<ITextComponent> currentTipHead = new TaggableList<>(TaggedTextComponent::new);
		List<ITextComponent> currentTipBody = new TaggableList<>(TaggedTextComponent::new);
		List<ITextComponent> currentTipTail = new TaggableList<>(TaggedTextComponent::new);

		if (target == null || target.getType() == RayTraceResult.Type.MISS) {
			tooltip = null;
			return;
		}
		if (target.getType() == RayTraceResult.Type.BLOCK) {
			DataAccessor accessor = DataAccessor.INSTANCE;
			accessor.set(world, player, target);

			if (accessor.serverConnected && accessor.getTileEntity() != null && config.shouldDisplayTooltip()) {
				if (accessor.isTimeElapsed(MetaDataProvider.rateLimiter)) {
					accessor.resetTimer();
					if (WailaRegistrar.INSTANCE.hasNBTProviders(accessor.getBlock()) || WailaRegistrar.INSTANCE.hasNBTProviders(accessor.getTileEntity()))
						Waila.NETWORK.sendToServer(new MessageRequestTile(accessor.getTileEntity()));
				}
				if (DataAccessor.INSTANCE.serverData == null) {
					if (WailaRegistrar.INSTANCE.hasNBTProviders(accessor.getBlock()) || WailaRegistrar.INSTANCE.hasNBTProviders(accessor.getTileEntity()))
						return;
				}
			}

			ItemStack targetStack = RayTracing.INSTANCE.getTargetStack(); // Here we get either the proper stack or the override
			accessor.stack = targetStack;

			//if (!targetStack.isEmpty()) {
			instance().handler.gatherBlockComponents(accessor, currentTipHead, TooltipPosition.HEAD);
			instance().handler.gatherBlockComponents(accessor, currentTipBody, TooltipPosition.BODY);
			instance().handler.gatherBlockComponents(accessor, currentTipTail, TooltipPosition.TAIL);

			combinePositions(player, currentTip, currentTipHead, currentTipBody, currentTipTail);

			tooltip = new Tooltip(currentTip, !targetStack.isEmpty());
			//}
		} else if (target.getType() == RayTraceResult.Type.ENTITY) {
			DataAccessor accessor = DataAccessor.INSTANCE;
			accessor.set(world, player, target);

			if (accessor.serverConnected && accessor.getEntity() != null && config.shouldDisplayTooltip()) {
				if (accessor.isTimeElapsed(MetaDataProvider.rateLimiter)) {
					accessor.resetTimer();
					if (WailaRegistrar.INSTANCE.hasNBTEntityProviders(accessor.getEntity()))
						Waila.NETWORK.sendToServer(new MessageRequestEntity(accessor.getEntity()));
				}
				if (DataAccessor.INSTANCE.serverData == null) {
					if (WailaRegistrar.INSTANCE.hasNBTEntityProviders(accessor.getEntity()))
						return;
				}
			}

			Entity targetEnt = RayTracing.INSTANCE.getTargetEntity(); // This need to be replaced by the override check.

			if (targetEnt != null) {
				instance().handler.gatherEntityComponents(targetEnt, accessor, currentTipHead, TooltipPosition.HEAD);
				instance().handler.gatherEntityComponents(targetEnt, accessor, currentTipBody, TooltipPosition.BODY);
				instance().handler.gatherEntityComponents(targetEnt, accessor, currentTipTail, TooltipPosition.TAIL);

				combinePositions(player, currentTip, currentTipHead, currentTipBody, currentTipTail);

				ItemStack displayItem = RayTracing.INSTANCE.getIdentifierStack();
				tooltip = new Tooltip(currentTip, !displayItem.isEmpty());
			}
		}
	}

	private void combinePositions(PlayerEntity player, List<ITextComponent> currentTip, List<ITextComponent> currentTipHead, List<ITextComponent> currentTipBody, List<ITextComponent> currentTipTail) {
		if (Waila.CONFIG.get().getGeneral().shouldShiftForDetails() && !currentTipBody.isEmpty() && !player.isSecondaryUseActive()) {
			currentTipBody.clear();
			currentTipBody.add(new TranslationTextComponent("tooltip.waila.sneak_for_details").setStyle(Style.EMPTY.setItalic(true)));
		}

		((ITaggableList<ResourceLocation, ITextComponent>) currentTip).absorb((ITaggableList<ResourceLocation, ITextComponent>) currentTipHead);
		((ITaggableList<ResourceLocation, ITextComponent>) currentTip).absorb((ITaggableList<ResourceLocation, ITextComponent>) currentTipBody);
		((ITaggableList<ResourceLocation, ITextComponent>) currentTip).absorb((ITaggableList<ResourceLocation, ITextComponent>) currentTipTail);
	}

	private static Narrator getNarrator() {
		return narrator == null ? narrator = Narrator.getNarrator() : narrator;
	}

	public static WailaTickHandler instance() {
		if (INSTANCE == null)
			INSTANCE = new WailaTickHandler();
		return INSTANCE;
	}

	@SubscribeEvent
	public static void onTooltip(WailaTooltipEvent event) {
		if (event.getCurrentTip().isEmpty())
			return;

		if (!getNarrator().active() || !Waila.CONFIG.get().getGeneral().shouldEnableTextToSpeech())
			return;

		if (Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().gameSettings.chatVisibility != ChatVisibility.HIDDEN)
			return;

		if (event.getAccessor().getBlock() == Blocks.AIR && event.getAccessor().getEntity() == null)
			return;

		if (Minecraft.getInstance().world != null && Minecraft.getInstance().world.getGameTime() % 5 > 0) {
			return;
		}

		ITextComponent component = event.getCurrentTip().get(0);
		if (component instanceof TaggedTextComponent && event.getCurrentTip() instanceof ITaggableList)
			component = ((ITaggableList<ResourceLocation, ITextComponent>) event.getCurrentTip()).getTag(((TaggedTextComponent) component).getTag());
		String narrate = TextProcessing.func_244782_a(component);
		if (lastNarration.equalsIgnoreCase(narrate))
			return;

		getNarrator().clear();
		getNarrator().say(narrate, true);
		lastNarration = narrate;
	}
}
