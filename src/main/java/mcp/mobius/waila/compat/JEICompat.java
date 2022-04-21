package mcp.mobius.waila.compat;

import com.mojang.blaze3d.platform.InputConstants;

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
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import snownee.jade.Jade;

@JeiPlugin
public class JEICompat implements IModPlugin {

	public static final ResourceLocation ID = new ResourceLocation(Jade.MODID, "main");
	public static KeyMapping showRecipes;
	public static KeyMapping showUses;
	private static IJeiRuntime runtime;
	private static IJeiHelpers helpers;

	public JEICompat() {
		if (showRecipes == null) {
			showRecipes = new KeyMapping("key.waila.show_recipes", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(323), Jade.NAME);
			showUses = new KeyMapping("key.waila.show_uses", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(324), Jade.NAME);
			ClientRegistry.registerKeyBinding(showRecipes);
			ClientRegistry.registerKeyBinding(showUses);
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
		if (runtime == null || showRecipes == null || showUses == null)
			return;
		if (event.getAction() != 1)
			return;
		if (!showRecipes.isDown() && !showUses.isDown())
			return;
		Accessor<?> accessor = ObjectDataCenter.get();
		if (accessor == null)
			return;
		ItemStack stack = accessor.getPickedResult();
		if (stack.isEmpty())
			return;

		IRecipesGui gui = runtime.getRecipesGui();
		IFocusFactory factory = helpers.getFocusFactory();

		gui.show(factory.createFocus(showUses.isDown() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack));
	}
}
