package mcp.mobius.waila.compat;

import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IRecipeRegistration;
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
	private static IJeiHelpers helpers;

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		JEICompat.helpers = registration.getJeiHelpers();
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		JEICompat.runtime = runtime;
	}

	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (runtime == null)
			return;
		if (!WailaClient.showRecipes.isDown() && !WailaClient.showUses.isDown())
			return;
		Accessor<?> accessor = ObjectDataCenter.get();
		if (accessor == null)
			return;
		ItemStack stack = accessor.getPickedResult();
		if (stack.isEmpty())
			return;

		IRecipesGui gui = runtime.getRecipesGui();
		IFocusFactory factory = helpers.getFocusFactory();

		gui.show(factory.createFocus(WailaClient.showUses.isDown() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM, stack));
	}
}
