/*
package snownee.jade.compat;

import org.jspecify.annotations.Nullable;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.JadeIds;

@JeiPlugin
public class JEICompat implements IModPlugin, RecipeLookupPlugin {

	public static final Identifier ID = JadeIds.JADE("main");
	private static IJeiRuntime runtime;
	private static IJeiHelpers helpers;

	@Override
	public Identifier getPluginUid() {
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

	@Override
	public RecipeLookupResult lookup(ItemStack itemStack, @Nullable Identifier specialId, boolean uses) {
		return new RecipeLookupResult(
				"jei", 0.9f, (s, results) -> {
			if (runtime == null || helpers == null) {
				return;
			}
			IRecipesGui gui = runtime.getRecipesGui();
			IFocusFactory factory = helpers.getFocusFactory();
			gui.show(factory.createFocus(
					uses ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT,
					VanillaTypes.ITEM_STACK,
					itemStack));
		});
	}
}
*/
