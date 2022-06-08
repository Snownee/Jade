/*package snownee.jade.compat;

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
import net.minecraftforge.common.MinecraftForge;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.Accessor;
import snownee.jade.impl.ObjectDataCenter;

@JeiPlugin
public class JEICompat implements IModPlugin {

	public static final ResourceLocation ID = new ResourceLocation(Jade.MODID, "main");
	private static IJeiRuntime runtime;
	private static IJeiHelpers helpers;

	public JEICompat() {
		if (runtime == null) {
			MinecraftForge.EVENT_BUS.addListener(JEICompat::onKeyPressed);
		}
	}

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

	private static void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (runtime == null || JadeClient.showRecipes == null || JadeClient.showUses == null)
			return;
		if (event.getAction() != 1)
			return;
		if (!JadeClient.showRecipes.isDown() && !JadeClient.showUses.isDown())
			return;
		Accessor<?> accessor = ObjectDataCenter.get();
		if (accessor == null)
			return;
		ItemStack stack = accessor.getPickedResult();
		if (stack.isEmpty())
			return;

		IRecipesGui gui = runtime.getRecipesGui();
		IFocusFactory factory = helpers.getFocusFactory();

		gui.show(factory.createFocus(JadeClient.showUses.isDown() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack));
	}
}
*/