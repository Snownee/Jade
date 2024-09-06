package mcp.mobius.waila.compat;

import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import snownee.jade.Jade;

@JeiPlugin
public class JEICompat implements IModPlugin {

	public static final ResourceLocation ID = new ResourceLocation(Jade.MODID, "main");
	private static IJeiRuntime runtime;

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		JEICompat.runtime = runtime;
	}

	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (runtime == null) {
			return;
		}
		boolean showRecipes = WailaClient.showRecipes.consumeClick();
		boolean showUses = WailaClient.showUses.consumeClick();
		if (!showRecipes && !showUses) {
			return;
		}
		Accessor<?> accessor = ObjectDataCenter.get();
		if (accessor == null) {
			return;
		}
		ItemStack stack = accessor.getPickedResult();
		if (stack.isEmpty()) {
			return;
		}

		IRecipesGui gui = runtime.getRecipesGui();
		IRecipeManager manager = runtime.getRecipeManager();
		gui.show(manager.createFocus(showUses ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, stack));
	}
}
